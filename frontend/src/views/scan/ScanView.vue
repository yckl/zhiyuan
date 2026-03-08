<template>
  <div class="scan-page">
    <div class="nav-bar">
      <div class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="title">扫一扫</div>
      <div class="placeholder"></div>
    </div>

    <div class="scan-container">
      <!-- 核心扫描渲染区域 -->
      <div id="reader"></div>
      
      <!-- 权限或加载错误引导层 -->
      <div v-if="errorMsg" class="error-overlay">
        <el-result icon="warning" title="摄像头启动失败">
          <template #sub-title>
            <div class="error-tips">
              <p>{{ errorMsg }}</p>
              <ul class="tips-list">
                <li>请确保已授予摄像头访问权限</li>
                <li>请检查是否正使用 HTTPS 协议访问</li>
                <li>部分浏览器需手动允许“不安全内容"</li>
              </ul>
            </div>
          </template>
          <template #extra>
            <el-button type="primary" size="large" @click="retryInit">重新尝试</el-button>
            <el-button size="large" @click="handleBack">取消返回</el-button>
          </template>
        </el-result>
      </div>

    <!-- 扫描扫描装饰框 (仅在活跃时显示) -->
      <div class="scan-overlay" v-if="!errorMsg && !isProcessing">
        <div class="scan-box">
          <div class="scan-line"></div>
          <div class="corner top-left"></div>
          <div class="corner top-right"></div>
          <div class="corner bottom-left"></div>
          <div class="corner bottom-right"></div>
        </div>
        <div class="tip-container">
          <p class="tip">将二维码对准中心框，即可自动签到</p>
        </div>
      </div>
      
      <!-- 逻辑处理遮罩 -->
      <div v-if="isProcessing" class="processing-overlay" v-loading="true" 
           element-loading-text="正在完成签到..." 
           element-loading-background="rgba(0, 0, 0, 0.7)">
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { Html5Qrcode } from 'html5-qrcode'
import { request } from '@/utils/request'

const router = useRouter()
let html5QrCode: Html5Qrcode | null = null
const isProcessing = ref(false)
const errorMsg = ref('')

/**
 * 核心：初始化扫描逻辑 (高级 start 方法)
 */
const initScanner = async () => {
  errorMsg.value = ''
  
  // 确保 DOM 准备就绪
  await nextTick()
  const readerElement = document.getElementById('reader')
  if (!readerElement) {
    console.error("未找到?#reader 容器")
    return
  }
  
  try {
    // 1. 创建实例
    html5QrCode = new Html5Qrcode("reader")

    // 2. 配置项
    const config = {
      fps: 15,
      qrbox: { width: 250, height: 250 },
      aspectRatio: 1.0,
      showTorchButtonIfSupported: true // [增强] 支持手电筒
    }

    // 3. 启动：尝试多种?facingMode 策略
    console.log("正在尝试启动后置摄像头...")
    try {
      // 策略 A: environment (ideal) - 兼容性最好
      await html5QrCode.start(
        { facingMode: "environment" },
        config,
        onScanSuccess,
        onScanFailure
      )
    } catch (err) {
      console.warn("标准环境模式启动失败，尝试降级策略...", err)
      // 策略 B: 宽松模式
      await html5QrCode.start(
        { facingMode: { ideal: "environment" } },
        config,
        onScanSuccess,
        onScanFailure
      )
    }

    console.log("摄像头启动成功")
  } catch (err: any) {
    console.error("摄像头最终初始化失败", err)
    errorMsg.value = `摄像头启动失败： ${err.message || '未知错误'}`
  }
}

/**
 * 扫码成功回调
 */
const onScanSuccess = async (decodedText: string) => {
  if (isProcessing.value) return
  isProcessing.value = true

  console.log('原始扫描结果:', decodedText)

  // 0. 权限检查
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再完成签到')
    router.push({ path: '/login', query: { redirect: '/scan/index' } })
    return
  }

  // 1. 尝试从文本中提取纯数字 (兼容 123、 http://.../123、 xxxx:123)
  const match = decodedText.match(/\d+/)
  if (!match) {
    ElMessage.error('二维码内容无效，未识别到活动ID')
    isProcessing.value = false
    return
  }

  // 2. 转换为数字类型 (对应后端 Long)
  const targetId = Number(match[0])
  if (isNaN(targetId) || targetId <= 0) {
    ElMessage.error('二维码解析出的 ID 无效')
    isProcessing.value = false
    return
  }

  // 3. 核心：暂停扫描，防止连续触发
  if (html5QrCode && html5QrCode.isScanning) {
    try {
      await html5QrCode.pause()
    } catch (e) {
      console.warn("暂停相机失败", e)
    }
  }

  try {
    // 4. 调用后端签到接口 (确保 Key ?activityId)
    const res = await request.post('/activity/checkin', { activityId: targetId })

    if (res.code === 200) {
      playSuccessSound()
      ElMessage.success(res.message || '签到成功！')

      setTimeout(() => {
        router.push('/profile/my-activities')
      }, 1500)
    } else {
      throw new Error(res.message || '签到失败，请确保您已正常报名本活动')
    }
  } catch (error: any) {
    ElMessageBox.confirm(
      error.message,
      '签到失败',
      {
        confirmButtonText: '再次扫码',
        cancelButtonText: '返回',
        type: 'error',
      }
    ).then(async () => {
      isProcessing.value = false
      if (html5QrCode) {
        try {
          await html5QrCode.resume()
        } catch (e) {
          console.warn('恢复相机失败，尝试重新初始化:', e)
          initScanner()
        }
      }
    }).catch(() => {
      router.back()
    })
  }
}

const onScanFailure = () => { /* 静默忽略常见扫描波动 */ }

/**
 * 辅助：释放相机资源
 */
const stopCamera = async () => {
  if (html5QrCode && html5QrCode.isScanning) {
    try {
      await html5QrCode.stop()
      // 注意：stop 之后不需要完全销毁实例，但为了保险我们清空引用
    } catch (e) {
      console.warn("停止相机失败", e)
    }
  }
}

/**
 * 辅助：播放音效
 */
const playSuccessSound = () => {
  const audio = new Audio('https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3')
  audio.play().catch(() => {})
}

const retryInit = () => {
  stopCamera().then(() => initScanner())
}

const handleBack = () => {
  stopCamera().then(() => router.back())
}

onMounted(() => {
  initScanner()
})

onUnmounted(() => {
  stopCamera()
})
</script>

<style lang="scss" scoped>
.scan-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #000;
  z-index: 2000;
  display: flex;
  flex-direction: column;
}

.nav-bar {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: rgba(0, 0, 0, 0.9);
  color: #fff;
  z-index: 100;
  
  .back-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    .el-icon { font-size: 22px; }
  }

  .title { font-size: 17px; font-weight: 600; }
  .placeholder { width: 36px; }
}

.scan-container {
  flex: 1;
  position: relative;
  overflow: hidden;

  #reader {
    width: 100%;
    height: 60vh !important; /* [修正] 强制高度，防止塌陷 */
    min-height: 300px;
    background-color: #000; /* [修正] 黑色背景 */
    margin: 0 auto;
    
    /* 强力隐藏高级库自带的 UI */
    :deep(button), :deep(select), :deep(span), :deep(img) { 
        display: none !important; opacity: 0; pointer-events: none; 
    }
    
    :deep(video) { 
        width: 100% !important; 
        height: 100% !important; 
        object-fit: cover !important; 
    }
  }
}

/* 扫描装饰框 */
.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.3);

  .scan-box {
    width: 250px;
    height: 250px;
    position: relative;
    box-shadow: 0 0 0 1000px rgba(0, 0, 0, 0.5); // 核心：黑色半透明遮照，仅中间镂空
    
    .corner {
      position: absolute;
      width: 24px;
      height: 24px;
      border: 4px solid #00BFA6;
    }
    
    .top-left { top: -2px; left: -2px; border-right: 0; border-bottom: 0; }
    .top-right { top: -2px; right: -2px; border-left: 0; border-bottom: 0; }
    .bottom-left { bottom: -2px; left: -2px; border-right: 0; border-top: 0; }
    .bottom-right { bottom: -2px; right: -2px; border-left: 0; border-top: 0; }
    
    .scan-line {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 3px;
      background: linear-gradient(90deg, transparent, #00BFA6, transparent);
      box-shadow: 0px 0px 15px rgba(0, 191, 166, 0.8);
      animation: scanMove 3s infinite linear;
    }
  }

  .tip-container {
    margin-top: 50px;
    background: rgba(0, 0, 0, 0.7);
    padding: 8px 18px;
    border-radius: 20px;
    .tip { color: #fff; font-size: 14px; margin: 0; opacity: 0.9; }
  }
}

@keyframes scanMove {
  0% { top: 0; }
  100% { top: 100%; }
}

/* 错误层样式 */
.error-overlay {
  position: absolute;
  inset: 0;
  z-index: 200;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px;

  .error-tips {
    text-align: left;
    color: rgba(255, 255, 255, 0.8);
    .tips-list {
      padding-left: 20px;
      font-size: 14px;
      line-height: 2;
    }
  }
  
  :deep(.el-result__title) { color: #fff; font-size: 20px; }
}

.processing-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 500;
  backdrop-filter: blur(4px);
}
</style>

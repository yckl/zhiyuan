<template>
  <div class="checkin-container" v-loading="pageLoading">
    <!-- 1. 顶部状态栏 (极光青渐? -->
    <div class="stats-header">
      <div class="header-main">
        <div class="activity-info">
          <h2 class="activity-title">{{ activityTitle || '加载中...' }}</h2>
          <p class="activity-subtitle">现场核销控制</p>
        </div>
        <div class="stats-pill">
          <span class="count">{{ stats.checked }}</span>
          <span class="total">/ {{ stats.total }}</span>
        </div>
      </div>
      <div class="progress-container">
        <div class="progress-bar" :style="{ width: stats.progress + '%' }"></div>
      </div>
    </div>

    <!-- 2. 核心核销?(动?QR + 扫码) -->
    <div class="checkin-hub">
      <div class="hub-tabs">
        <div 
          class="hub-tab-item" 
          :class="{ active: activeHubTab === 'qr' }"
          @click="activeHubTab = 'qr'"
        >展示二维码</div>
        <div 
          class="hub-tab-item" 
          :class="{ active: activeHubTab === 'scan' }"
          @click="activeHubTab = 'scan'"
        >扫描学生</div>
      </div>

      <div class="hub-content">
        <!-- 二维码展示模式 -->
        <div v-if="activeHubTab === 'qr'" class="qr-display-mode">
          <div class="qr-card">
            <qrcode-vue 
              v-if="qrToken" 
              :value="qrToken" 
              :size="200" 
              level="H" 
              class="qr-code" 
            />
            <div v-else class="qr-placeholder">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
            <div class="refresh-hint">
              <el-icon><Refresh /></el-icon>
              <span>{{ countdown }}s 后自动刷新</span>
            </div>
          </div>
          <div class="backup-code-zone">
            <span class="label">备用数字</span>
            <div class="code-digits">
              <span v-for="(d, i) in backupCode" :key="i" class="digit">{{ d }}</span>
            </div>
          </div>
        </div>

        <!-- 摄像头扫描模式 -->
        <div v-else class="camera-scan-mode">
          <div id="reader" class="scanner-box"></div>
          <div class="scan-frame">
            <div class="corner tl"></div>
            <div class="corner tr"></div>
            <div class="corner bl"></div>
            <div class="corner br"></div>
            <div class="scan-line"></div>
          </div>
          <p class="scan-tips">请将学生出示的二维码置于框内</p>
          <div class="fallback-upload">
            <label class="upload-btn">
              <el-icon><Picture /></el-icon> 从相册选择二维码
              <input type="file" accept="image/*" @change="handleFileUpload" style="display: none;" />
            </label>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 名单管理区域 (标签页切换) -->
    <div class="management-zone">
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="待核销" name="unchecked">
          <div class="list-wrapper">
            <div v-for="item in uncheckedList" :key="item.id" class="user-card">
              <div class="user-info">
                <el-avatar :size="40" :src="item.volunteerAvatar">{{ item.volunteerName?.charAt(0) }}</el-avatar>
                <div class="text">
                  <span class="name">{{ item.volunteerName }}</span>
                  <span class="sid">{{ item.studentId }}</span>
                </div>
              </div>
              <button class="action-btn confirm" @click="handleManualCheckin(item)">确认到场</button>
            </div>
            <el-empty v-if="uncheckedList.length === 0" description="暂无待核销人员" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="已核销" name="checked">
          <div class="list-wrapper">
            <div v-for="item in checkedList" :key="item.id" class="user-card done">
              <div class="user-info">
                <el-avatar :size="40" :src="item.volunteerAvatar">{{ item.volunteerName?.charAt(0) }}</el-avatar>
                <div class="text">
                  <span class="name">{{ item.volunteerName }}</span>
                  <span class="time">{{ formatTime(item.signInTime) }} 已核销</span>
                </div>
              </div>
              <button class="action-btn rollback" @click="handleRollback(item)">撤回</button>
            </div>
            <el-empty v-if="checkedList.length === 0" description="暂无已核销人员" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 4. 底部活动选择区域 (抽屉) -->
    <div class="activity-selector" @click="showActivityDrawer = true">
      <el-icon><Menu /></el-icon>
      <span>切换活动</span>
    </div>

    <el-drawer
      v-model="showActivityDrawer"
      direction="btt"
      size="50%"
      title="选择核销活动"
      custom-class="ios-drawer"
    >
      <div class="drawer-list">
        <div 
          v-for="a in activities" 
          :key="a.id" 
          class="drawer-item"
          :class="{ active: currentActivityId === a.id }"
          @click="selectActivity(a)"
        >
          <span class="title">{{ a.title }}</span>
          <el-icon v-if="currentActivityId === a.id"><Check /></el-icon>
        </div>
      </div>
    </el-drawer>

    <!-- 成功提示音效/触感反馈模拟环境 -->
    <audio ref="successAudio" src="https://assets.mixkit.co/active_storage/sfx/2211/2211-preview.mp3" style="display:none"></audio>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { request } from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, Refresh, Check, Menu, Picture } from '@element-plus/icons-vue'
import QrcodeVue from 'qrcode.vue'
import { Html5QrcodeScanner } from 'html5-qrcode'
import dayjs from 'dayjs'

const pageLoading = ref(false)
const currentActivityId = ref<number | null>(null)
const activityTitle = ref('')
const activeHubTab = ref<'qr' | 'scan'>('qr')
const activeTab = ref('unchecked')
const showActivityDrawer = ref(false)

const qrToken = ref('')
const backupCode = ref<string[]>([])
const countdown = ref(30)
let timer: any = null

const stats = reactive({
  total: 0,
  checked: 0,
  progress: 0
})

const registrations = ref<any[]>([])
const activities = ref<any[]>([])

const uncheckedList = computed(() => registrations.value.filter(r => r.status === 1 || r.status === 'pending'))
const checkedList = computed(() => registrations.value.filter(r => r.status === 2 || r.status === 'checked_in'))

// 1. 获取活动列表
const fetchActivities = async () => {
  try {
    const res = await request.get('/activity/my', { page: 1, size: 50 })
    activities.value = res.data?.records || []
    if (activities.value.length > 0 && !currentActivityId.value) {
      selectActivity(activities.value[0])
    }
  } catch (e) {
    ElMessage.error('获取活动列表失败')
  }
}

// 2. 选择活动
const selectActivity = (a: any) => {
  currentActivityId.value = a.id
  activityTitle.value = a.title
  showActivityDrawer.value = false
  fetchData()
}

// 3. 获取核心数据 (名单 + 统计)
const fetchData = async () => {
  if (!currentActivityId.value) return
  pageLoading.value = true
  try {
    const [regRes, statsRes] = await Promise.all([
      request.get(`/organizer/checkin/activity/${currentActivityId.value}/attendees`),
      request.get(`/organizer/checkin/stats/${currentActivityId.value}`)
    ])
    registrations.value = regRes.data?.attendees || []
    Object.assign(stats, statsRes.data)
    
    // 强制同步统计数字 (防止后端统计接口延迟)
    stats.total = registrations.value.length
    stats.checked = checkedList.value.length
    
    console.log('名单获取成功, 总人数:', stats.total, '已到:', stats.checked)
    console.log('API 返回的已签到名单:', checkedList.value)
    
    updateQRCode()
  } catch (e) {
    console.error('获取名单失败:', e)
    ElMessage.error('数据同步失败')
  } finally {
    pageLoading.value = false
  }
}

// 自动监听活动切换，确保数据实时刷新
watch(() => currentActivityId.value, (newId) => {
  if (newId) {
    fetchData()
  }
}, { immediate: true })

// 4. 二维码逻辑
const updateQRCode = async () => {
  if (!currentActivityId.value) return
  try {
    const res = await request.get(`/organizer/checkin/activity/${currentActivityId.value}/qrcode`)
    qrToken.value = res.data.qrcodeContent
    backupCode.value = res.data.checkinCode.split('')
    countdown.value = 30
    resetTimer()
  } catch (e) {
    console.error('QR刷新失败')
  }
}

const resetTimer = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--
    } else {
      updateQRCode()
    }
  }, 1000)
}

// 5. 核销动作
const handleManualCheckin = async (item: any) => {
  try {
    await request.post('/organizer/checkin/manual', { registrationId: item.id })
    playSuccessEffect()
    ElMessage.success(`${item.volunteerName} 核销成功`)
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '核销失败')
  }
}

const handleRollback = async (item: any) => {
  try {
    await ElMessageBox.confirm(`确定要撤销 ${item.volunteerName} 的核销记录吗？`, '提示')
    await request.post(`/organizer/checkin/rollback/${item.id}`)
    ElMessage.success('已撤销')
    fetchData()
  } catch (e) {
    // 
  }
}

// 6. 扫码逻辑 (html5-qrcode)
let scanner: any = null
watch(activeHubTab, (val) => {
  if (val === 'scan') {
    initScanner()
  } else {
    stopScanner()
  }
})

const initScanner = () => {
  setTimeout(() => {
    scanner = new Html5QrcodeScanner("reader", { 
      fps: 10, 
      qrbox: { width: 250, height: 250 },
      aspectRatio: 1.0
    }, false)
    scanner.render(onScanSuccess, onScanFailure)
  }, 100)
}

const stopScanner = () => {
  if (scanner) {
    scanner.clear().catch((e: any) => console.error(e))
  }
}

const onScanSuccess = async (decodedText: string) => {
  // volunteer://checkin?token=xxx&activityId=xxx
  if (decodedText.startsWith('volunteer://checkin')) {
    const url = new URL(decodedText.replace('volunteer://', 'http://'))
    const token = url.searchParams.get('token')
    if (token) {
      try {
        await request.post('/organizer/checkin/verify', { token })
        playSuccessEffect()
        ElMessage.success('扫码核销成功')
        fetchData()
      } catch (e: any) {
        ElMessage.error(e.response?.data?.message || '核销失败')
      }
    }
  } else {
    ElMessage.error('无法识别的二维码格式')
  }
}

const onScanFailure = () => {
  // 静默失败
}

let isProcessingFile = false
import { Html5Qrcode } from 'html5-qrcode'
const handleFileUpload = async (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file || isProcessingFile) return

  try {
    isProcessingFile = true
    const elId: string = scanner ? scanner.elementId : "reader"
    
    // Stop continuous scanner if running
    if (scanner && typeof scanner.getState === 'function' && scanner.getState() === 2) { // 2 = SCANNING
        await scanner.stop()
    }

    const html5QrCode = new Html5Qrcode(elId as string)
    const decodedText = await html5QrCode.scanFile(file, true)
    
    // Call the success handler
    await onScanSuccess(decodedText)
  } catch (errorObj: any) {
    console.error('File scan error:', errorObj)
    const errMsg = typeof errorObj === 'string' ? errorObj : (errorObj?.message || '')
    ElMessage.error(errMsg.includes('No QR code found') ? '未找到二维码' : '解析图片失败')
    
    // If we stopped the scanner, restart it
    if (activeHubTab.value === 'scan') {
      try {
        if (!scanner || (typeof scanner.getState === 'function' && scanner.getState() !== 2)) {
          initScanner()
        }
      } catch(e) {}
    }
  } finally {
      isProcessingFile = false;
      // Reset input value so same file can be selected again
      (event.target as HTMLInputElement).value = '';
  }
}

// 7. 辅助函数
const successAudio = ref<HTMLAudioElement | null>(null)
const playSuccessEffect = () => {
  if (successAudio.value) successAudio.value.play().catch(() => {})
  if ('vibrate' in navigator) navigator.vibrate(100)
}

const formatTime = (t: string) => t ? dayjs(t).format('HH:mm') : ''

onMounted(() => {
  fetchActivities()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  stopScanner()
})
</script>

<style scoped lang="scss">
.checkin-container {
  min-height: 100vh;
  background: #F2F2F7;
  padding-bottom: 140px;
  position: relative;
}

/* 1. Header */
.stats-header {
  background: linear-gradient(135deg, #0093E9 0%, #80D0C7 100%);
  padding: 30px 20px 24px;
  color: #fff;
  border-radius: 0 0 24px 24px;
  box-shadow: 0 4px 20px rgba(0, 147, 233, 0.2);

  .header-main {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .activity-title {
    font-size: 20px;
    font-weight: 800;
    margin: 0;
    letter-spacing: -0.5px;
  }
  .activity-subtitle {
    font-size: 12px;
    opacity: 0.8;
    margin: 4px 0 0;
  }

  .stats-pill {
    background: rgba(255, 255, 255, 0.2);
    padding: 8px 16px;
    border-radius: 100px;
    backdrop-filter: blur(10px);
    .count { font-size: 24px; font-weight: 800; }
    .total { font-size: 14px; opacity: 0.8; margin-left: 2px; }
  }

  .progress-container {
    height: 6px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 10px;
    overflow: hidden;
    .progress-bar {
      height: 100%;
      background: #fff;
      transition: width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
  }
}

/* 2. Hub Section */
.checkin-hub {
  margin: 20px 16px;
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.04);

  .hub-tabs {
    display: flex;
    background: #F2F2F7;
    padding: 4px;
    border-radius: 12px;
    margin-bottom: 20px;

    .hub-tab-item {
      flex: 1;
      text-align: center;
      padding: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #8E8E93;
      border-radius: 8px;
      transition: all 0.3s;
      &:active { transform: scale(0.96); }
      &.active {
        background: #fff;
        color: #0093E9;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      }
    }
  }
}

.qr-display-mode {
  text-align: center;
  .qr-card {
    padding: 20px;
    background: #F8F9FA;
    border-radius: 16px;
    display: inline-block;
    .qr-code { margin: 0 auto; }
    .qr-placeholder { width: 200px; height: 200px; display: flex; align-items: center; justify-content: center; font-size: 40px; color: #0093E9; }
  }
  .refresh-hint {
    margin-top: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
    color: #8E8E93;
    font-size: 12px;
    .el-icon { animation: rotate 2s linear infinite; }
  }
}

@keyframes rotate { from { transform: rotate(0); } to { transform: rotate(360deg); } }

.backup-code-zone {
  margin-top: 24px;
  .label { font-size: 12px; color: #8E8E93; text-transform: uppercase; letter-spacing: 1px; }
  .code-digits {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 8px;
    .digit {
      width: 40px; height: 50px;
      background: #F2F2F7;
      border-radius: 8px;
      display: flex; align-items: center; justify-content: center;
      font-size: 24px; font-weight: 800; color: #1C1C1E;
    }
  }
}

.camera-scan-mode {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  height: 300px;
  background: #000;

  .scanner-box { width: 100%; height: 100%; }

  .scan-overlay {
    position: absolute; inset: 0;
    pointer-events: none;
    display: flex; flex-direction: column; align-items: center; justify-content: center;

    .scan-frame {
      width: 200px; height: 200px;
      position: relative;
      .corner { position: absolute; width: 20px; height: 20px; border: 4px solid #0093E9; }
      .tl { top: 0; left: 0; border-right: none; border-bottom: none; }
      .tr { top: 0; right: 0; border-left: none; border-bottom: none; }
      .bl { bottom: 0; left: 0; border-right: none; border-top: none; }
      .br { bottom: 0; right: 0; border-left: none; border-top: none; }
      
      .scan-line {
        position: absolute; top: 0; left: 0; width: 100%; height: 2px;
        background: linear-gradient(to right, transparent, #0093E9, transparent);
        box-shadow: 0 0 10px #0093E9;
        animation: scanLineMove 2.5s infinite linear;
      }
    }
    .scan-tips { color: #fff; font-size: 12px; margin-top: 20px; background: rgba(0,0,0,0.5); padding: 4px 12px; border-radius: 20px; }
  }
}

  @keyframes scanLineMove { 0% { top: 0; } 100% { top: 100%; } }

  .fallback-upload {
    margin-top: 25px;
    pointer-events: auto; /* Re-enable pointer events inside the overlay */
    z-index: 10;
    
    .upload-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      border-radius: 20px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(8px);
      border: 1px solid rgba(255, 255, 255, 0.4);
      color: #fff;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:active {
        background: rgba(255, 255, 255, 0.3);
        transform: scale(0.96);
      }
    }
  }

/* 3. Management Zone */
.management-zone {
  margin: 0 16px;
  .custom-tabs :deep(.el-tabs__header) {
    border-bottom: none;
    margin-bottom: 12px;
  }
  .custom-tabs :deep(.el-tabs__nav) {
    width: 100%;
    .el-tabs__item { flex: 1; text-align: center; height: 44px; font-size: 16px; font-weight: 700; color: #8E8E93; }
    .el-tabs__item.is-active { color: #0093E9; }
    .el-tabs__active-bar { background-color: #0093E9; height: 3px; border-radius: 3px; }
  }
}

.list-wrapper {
  display: flex; flex-direction: column; gap: 12px;
}

.user-card {
  background: #fff;
  padding: 14px 16px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;

  .user-info {
    display: flex; align-items: center; gap: 12px;
    .text { display: flex; flex-direction: column; }
    .name { font-size: 16px; font-weight: 700; color: #1C1C1E; }
    .sid { font-size: 12px; color: #8E8E93; }
    .time { font-size: 12px; color: #34C759; font-weight: 600; }
  }

  .action-btn {
    padding: 8px 16px;
    border-radius: 100px;
    font-size: 13px;
    font-weight: 700;
    border: none;
    transition: all 0.2s;
    &:active { transform: scale(0.92); }
    
    &.confirm { background: #0093E9; color: #fff; box-shadow: 0 4px 12px rgba(0,147,233,0.2); }
    &.rollback { background: #F2F2F7; color: #FF3B30; }
  }

  &.done { border-left: 4px solid #34C759; }
}

/* 4. Activity Selector */
.activity-selector {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(15px);
  padding: 10px 24px;
  border-radius: 100px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  display: flex; align-items: center; gap: 10px;
  font-weight: 700; color: #0093E9;
  z-index: 100;
  border: 1px solid rgba(0, 147, 233, 0.1);
  &:active { transform: translateX(-50%) scale(0.95); }
}

/* 针对 PC 端的样式覆盖 */
@media (min-width: 768px) {
  .checkin-container {
    position: relative;
  }
  .activity-selector {
    /* 1. PC 端高阶居中布局 (参照用户要求：宽大、剔透) */
    position: absolute;
    top: 28px; /* 调整高度使其在蓝色页眉中垂直居中 */
    left: 50%;
    transform: translateX(-50%) !important;
    bottom: auto !important;
    z-index: 1000;
    
    /* 2. 极致玻璃态：大尺寸、柔和阴影、高模糊 */
    background: rgba(255, 255, 255, 0.08) !important;
    border: 1px solid rgba(255, 255, 255, 0.2) !important;
    color: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(20px) !important;
    -webkit-backdrop-filter: blur(20px) !important;
    height: 44px !important;
    min-width: 320px !important; /* 加宽后的胶囊感更高级 */
    padding: 0 40px !important;
    border-radius: 22px !important;
    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1) !important;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 0.5px;
    
    /* 3. 解决“内闪”问题：限定 transition 属性而非 all */
    transition: background 0.3s cubic-bezier(0.4, 0, 0.2, 1), 
                border-color 0.3s ease, 
                box-shadow 0.4s ease !important;
    
    &:hover {
      background: rgba(255, 255, 255, 0.18) !important;
      border-color: rgba(255, 255, 255, 0.4) !important;
      box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15) !important;
      color: #fff !important;
    }
    
    &:active {
      background: rgba(255, 255, 255, 0.12) !important;
      /* PC 端移除 scale 以保证交互稳定性 */
      transform: translateX(-50%) !important;
    }
  }
}

.drawer-list {
  padding: 10px 0;
  .drawer-item {
    padding: 16px 20px;
    display: flex; justify-content: space-between; align-items: center;
    border-bottom: 0.5px solid rgba(0,0,0,0.05);
    .title { font-size: 16px; color: #1C1C1E; }
    &.active { .title { color: #0093E9; font-weight: 700; } .el-icon { color: #0093E9; } }
  }
}
</style>

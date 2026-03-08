<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统全局配置</span>
        </div>
      </template>

      <el-form label-width="150px" v-loading="loading" style="max-width: 600px; margin-top: 20px">
        <el-form-item label="系统名称">
           <el-input v-model="form.site_name" />
        </el-form-item>
        <el-form-item label="系统Logo">
           <div style="display:flex; align-items:center; gap:10px;">
              <el-input v-model="form.site_logo" />
              <!-- Could add upload here too -->
           </div>
        </el-form-item>
        <el-form-item label="每小时服务积分">
           <el-input-number v-model="form.service_points_per_hour" :min="0" />
           <span class="help-text">志愿者每服务1小时获得的的积分</span>
        </el-form-item>
        <el-form-item label="活动自动审核">
           <el-switch v-model="form.activity_auto_audit_bool" active-text="开启" inactive-text="关闭" />
           <div class="help-block">开启后，组织者发布的活动将直接上</div>
        </el-form-item>
        <el-form-item label="系统维护模式">
           <el-switch v-model="form.maintenance_mode_bool" active-text="开启" inactive-text="关闭" />
           <div class="help-block">开启后，除管理员外其他用户无法登录</div>
        </el-form-item>
        
        <el-form-item>
           <el-button type="primary" @click="handleSave">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const form = reactive({
    site_name: '',
    site_logo: '',
    service_points_per_hour: 0,
    activity_auto_audit: 'false',
    activity_auto_audit_bool: false,
    maintenance_mode: 'false',
    maintenance_mode_bool: false
})

const fetchData = async () => {
    loading.value = true
    try {
        const res = await request.get('/admin/system/config')
        const data = res.data
        form.site_name = data.site_name || ''
        form.site_logo = data.site_logo || ''
        form.service_points_per_hour = parseFloat(data.service_points_per_hour || '0')
        form.activity_auto_audit = data.activity_auto_audit || 'false'
        form.maintenance_mode = data.maintenance_mode || 'false'
        
        form.activity_auto_audit_bool = form.activity_auto_audit === 'true'
        form.maintenance_mode_bool = form.maintenance_mode === 'true'
    } finally {
        loading.value = false
    }
}

const handleSave = async () => {
    try {
        // Convert bool to string
        const payload = {
            site_name: form.site_name,
            site_logo: form.site_logo,
            service_points_per_hour: form.service_points_per_hour.toString(),
            activity_auto_audit: form.activity_auto_audit_bool ? 'true' : 'false',
            maintenance_mode: form.maintenance_mode_bool ? 'true' : 'false'
        }
        await request.put('/admin/system/config/update', payload)
        ElMessage.success('配置已更新')
        fetchData() // Refresh
    } catch {}
}

onMounted(fetchData)
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: var(--bg-page);
  min-height: calc(100vh - 84px);
}

.card-header {
  font-weight: bold;
  color: var(--text-primary);
}

.help-text {
  margin-left: 10px;
  color: var(--text-muted);
  font-size: 13px;
}

.help-block {
  margin-top: 4px;
  color: var(--text-muted);
  font-size: 12px;
}

:deep(.el-card) {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: 12px;
}

@media (max-width: 768px) {
  .app-container {
    padding: 12px;
  }
  
  :deep(.el-form) {
    max-width: 100% !important;
  }
  
  :deep(.el-form-item) {
    flex-direction: column;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .el-form-item__label {
      text-align: left;
      margin-bottom: 8px;
      width: 100% !important;
    }
    
    .el-form-item__content {
      margin-left: 0 !important;
      width: 100%;
      
      .el-input, .el-input-number {
        width: 100% !important;
      }
    }
  }
  
  .help-text {
    margin-left: 0;
    margin-top: 4px;
    display: block;
  }
}
</style>

<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="welcome-header">
          <el-avatar :size="64" :src="avatarUrl">
            {{ userInfo?.name?.charAt(0) }}
          </el-avatar>
          <div class="welcome-info">
            <h2>欢迎回来, {{ userInfo?.name || userInfo?.username }}</h2>
            <p>{{ currentTime }}</p>
          </div>
        </div>
      </template>
      <div class="role-info">
        <el-tag>{{ roleLabel }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)
const baseAPI = import.meta.env.VITE_BASE_API || '/api'
// 角色标签
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'USER': '普通用户'
  }
  return roleMap[userInfo.value?.roleCode] || '未知角色'
})

const avatarUrl = computed(() => {
  return userInfo.value?.avatar ? baseAPI + userInfo.value.avatar : '';
})

// 当前时间
const currentTime = ref('')
let timeInterval = null // 保存定时器引用

const updateTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric', 
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

onMounted(() => {
  updateTime()
  // 每分钟更新一次时间
  timeInterval = setInterval(updateTime, 60000)
})

onUnmounted(() => {
  // 清除定时器
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }
})
</script>

<style lang="scss" scoped>
.dashboard {
  .welcome-card {
    margin-bottom: 20px;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .welcome-header {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .el-avatar {
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.1);
        }
      }
      
      .welcome-info {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          background: linear-gradient(to right, #409eff, #67c23a);
          -webkit-background-clip: text;
          -webkit-text-fill-color: transparent;
        }
        p {
          margin: 0;
          color: #666;
        }
      }
    }
    
    .role-info {
      margin-top: 16px;
    }
  }
}


</style> 
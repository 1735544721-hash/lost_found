<template>
  <div class="notification-bell">
    <el-badge 
      :value="unreadCount" 
      :hidden="unreadCount === 0"
      :max="99"
      class="notification-badge"
    >
      <el-button 
        circle 
        @click="toggleDropdown"
        :class="{ 'bell-active': showDropdown }"
        class="bell-button"
      >
        <el-icon :size="18">
          <Bell />
        </el-icon>
      </el-button>
    </el-badge>

    <!-- 通知下拉框 -->
    <el-popover
      :visible="showDropdown"
      @update:visible="showDropdown = $event"
      placement="bottom-end"
      :width="350"
      trigger="manual"
      popper-class="notification-popover"
    >
      <template #reference>
        <span></span>
      </template>

      <div class="notification-dropdown">
        <!-- 头部 -->
        <div class="notification-header">
          <span class="title">通知消息</span>
          <div class="header-actions">
            <el-button 
              type="text" 
              size="small"
              @click="markAllAsRead"
              :disabled="unreadCount === 0"
            >
              全部已读
            </el-button>
            <el-button 
              type="text" 
              size="small"
              @click="goToNotificationPage"
            >
              查看全部
            </el-button>
          </div>
        </div>

        <!-- 通知列表 -->
        <div class="notification-list">
          <el-scrollbar height="300px">
            <div v-if="notifications.length === 0" class="empty-state">
              <el-icon :size="48" color="#C0C4CC">
                <Bell />
              </el-icon>
              <p>暂无通知消息</p>
            </div>

            <div
              v-for="notification in notifications"
              :key="notification.id"
              :class="[
                'notification-item',
                { 'unread': notification.isRead === 0 }
              ]"
              @click="handleNotificationClick(notification)"
            >
              <div class="notification-icon">
                <el-icon 
                  :size="16" 
                  :color="getNotificationIconColor(notification.type)"
                >
                  <component :is="getNotificationIcon(notification.type)" />
                </el-icon>
              </div>

              <div class="notification-content">
                <div class="notification-title">
                  {{ notification.title }}
                </div>
                <div class="notification-message">
                  {{ notification.content }}
                </div>
                <div class="notification-time">
                  {{ formatTime(notification.createTime) }}
                </div>
              </div>

              <div class="notification-actions">
                <el-button
                  v-if="notification.isRead === 0"
                  type="text"
                  size="small"
                  @click.stop="markAsRead(notification.id)"
                >
                  标记已读
                </el-button>
                <el-button
                  type="text"
                  size="small"
                  @click.stop="deleteNotification(notification.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </el-scrollbar>
        </div>

        <!-- 底部 -->
        <div class="notification-footer">
          <el-button 
            type="primary" 
            size="small" 
            @click="goToNotificationPage"
            style="width: 100%"
          >
            查看所有通知
          </el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Bell, 
  Message, 
  Warning, 
  InfoFilled,
  SuccessFilled 
} from '@element-plus/icons-vue'
import { useNotificationPolling } from '@/utils/realtime'
import request from '@/utils/request'

const router = useRouter()

const showDropdown = ref(false)
const notifications = ref([])

// 使用通知轮询
const { unreadCount, refreshNotifications } = useNotificationPolling()

const toggleDropdown = async () => {
  showDropdown.value = !showDropdown.value
  
  if (showDropdown.value) {
    await loadNotifications()
  }
}

const loadNotifications = async () => {
  try {
    const params = {
      currentPage: 1,
      size: 10
    }
    await request.get('/notification/list', params, {
      onSuccess: (res) => {
        notifications.value = res.records || []
      }
    })
    
  } catch (error) {
    console.error('加载通知失败:', error)
  }
}

const markAsRead = async (notificationId) => {
  try {
    await request.put(`/notification/${notificationId}/read`)
    
    // 更新本地状态
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.isRead = 1
    }
    
    // 刷新未读数量
    refreshNotifications()
    
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const markAllAsRead = async () => {
  try {
    await request.put('/notification/read-all')
    
    // 更新本地状态
    notifications.value.forEach(notification => {
      notification.isRead = 1
    })
    
    // 刷新未读数量
    refreshNotifications()
    
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const deleteNotification = async (notificationId) => {
  try {
    await request.delete(`/notification/${notificationId}`)
    
    // 从列表中移除
    const index = notifications.value.findIndex(n => n.id === notificationId)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
    
    // 刷新未读数量
    refreshNotifications()
    
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleNotificationClick = (notification) => {
  // 如果未读，先标记为已读
  if (notification.isRead === 0) {
    markAsRead(notification.id)
  }
  
  // 根据通知类型跳转到相应页面
  // if (notification.relatedId) {
  //   switch (notification.type) {
  //     case 1: // 申请消息
  //       router.push(`/claim-applications`)
  //       break
  //     case 2: // 审核消息
  //       router.push(`/my-applications`)
  //       break
  //     default:
  //       break
  //   }
  // }
  
  showDropdown.value = false
}

const goToNotificationPage = () => {
  router.push('/notification')
  showDropdown.value = false
}

const getNotificationIcon = (type) => {
  const iconMap = {
    0: InfoFilled,    // 系统消息
    1: Message,       // 申请消息
    2: SuccessFilled  // 审核消息
  }
  return iconMap[type] || InfoFilled
}

const getNotificationIconColor = (type) => {
  const colorMap = {
    0: '#409EFF',  // 系统消息 - 蓝色
    1: '#E6A23C',  // 申请消息 - 橙色
    2: '#67C23A'   // 审核消息 - 绿色
  }
  return colorMap[type] || '#409EFF'
}

const formatTime = (timeString) => {
  const time = new Date(timeString)
  const now = new Date()
  const diff = now - time
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return time.toLocaleDateString()
  }
}

// 点击外部关闭下拉框
const handleClickOutside = (event) => {
  if (!event.target.closest('.notification-bell')) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-bell {
  position: relative;
  display: inline-block;
}

.notification-badge {
  display: block;
}

.bell-button {
  border: none;
  background: transparent;
  color: #606266;
  transition: all 0.3s;
}

.bell-button:hover,
.bell-active {
  color: #409EFF;
  background-color: #ecf5ff;
}

.notification-dropdown {
  padding: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  background-color: #fafafa;
}

.notification-header .title {
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.notification-list {
  max-height: 300px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-item.unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background-color: #409EFF;
}

.notification-icon {
  margin-right: 12px;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-message {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notification-time {
  font-size: 12px;
  color: #909399;
}

.notification-actions {
  margin-left: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background-color: #fafafa;
}
</style>

<style>
.notification-popover {
  padding: 0 !important;
}
</style>

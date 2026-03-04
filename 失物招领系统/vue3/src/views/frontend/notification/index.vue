<template>
  <div class="notification-page">
    <div class="page-header">
      <h1>我的通知</h1>
      <div class="actions">
        <el-button 
          type="primary" 
          :disabled="unreadCount === 0"
          @click="markAllAsRead"
        >
          全部标为已读
        </el-button>
      </div>
    </div>

    <el-card class="notification-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="filter-form">
            <el-radio-group v-model="typeFilter" @change="handleFilterChange">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="0">系统消息</el-radio-button>
              <el-radio-button :label="1">申请消息</el-radio-button>
              <el-radio-button :label="2">审核消息</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="notifications"
        style="width: 100%"
        :empty-text="'暂无通知消息'"
      >
        <el-table-column prop="title" label="标题" min-width="180">
          <template #default="{ row }">
            <div class="notification-title" :class="{ 'unread': row.isRead === 0 }">
              <el-badge v-if="row.isRead === 0" is-dot />
              {{ row.title }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="内容" min-width="300">
          <template #default="{ row }">
            <div class="notification-content">
              {{ row.content }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="typeDescription" label="类型" width="120" />
        
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.isRead === 0"
              link
              type="primary"
              @click="markAsRead(row.id)"
            >
              标为已读
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
// import { formatDateTime } from '@/utils/dateUtils'
import DateUtils from '@/utils/dateUtils'
const router = useRouter()


// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return DateUtils.formatDateTime(dateStr)
}
// 表格数据
const notifications = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const typeFilter = ref(null)
const unreadCount = ref(0)

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const params = {
      currentPage: currentPage.value,
      size: pageSize.value,
      type: typeFilter.value
    }
    await request.get('/notification/list', params, {
      onSuccess: (res) => {
        notifications.value = res.records || []
        total.value = res.total || 0
        
        // 计算未读数量
        unreadCount.value = notifications.value.filter(item => item.isRead === 0).length
      }
    })
  } catch (error) {
    console.error('获取通知列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 标记为已读
const markAsRead = async (id) => {
  try {
    await request.put(`/notification/${id}/read`, {}, {
      successMsg: '已标记为已读',
      showDefaultMsg: false,
      onSuccess: () => {
        // 更新本地状态
        const notification = notifications.value.find(n => n.id === id)
        if (notification) {
          notification.isRead = 1
          unreadCount.value--
        }
      }
    })
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    await request.put('/notification/read-all', {}, {
      successMsg: '全部标记为已读',
      onSuccess: () => {
        // 更新本地状态
        notifications.value.forEach(notification => {
          notification.isRead = 1
        })
        unreadCount.value = 0
      }
    })
  } catch (error) {
    console.error('全部标记已读失败:', error)
  }
}

// 删除通知
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await request.delete(`/notification/${id}`, {
      successMsg: '删除成功',
      onSuccess: () => {
        const index = notifications.value.findIndex(n => n.id === id)
        if (index > -1) {
          // 如果删除的是未读通知，更新未读计数
          if (notifications.value[index].isRead === 0) {
            unreadCount.value--
          }
          notifications.value.splice(index, 1)
          total.value--
        }
      }
    })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
    }
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchNotifications()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchNotifications()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
})
</script>

<style lang="scss" scoped>
.notification-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      margin: 0;
      font-size: 24px;
    }
  }
  
  .notification-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }
  
  .notification-title {
    display: flex;
    align-items: center;
    
    &.unread {
      font-weight: bold;
    }
    
    .el-badge {
      margin-right: 8px;
    }
  }
  
  .notification-content {
    color: #606266;
    white-space: normal;
    word-break: break-all;
    line-height: 1.5;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 
<template>
  <div class="lost-detail-page">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="!lostItem.id" class="not-found">
      <el-empty description="未找到该失物信息" />
      <el-button @click="$router.push('/lost')">返回列表</el-button>
    </div>
    
    <div v-else class="detail-container">
      <div class="detail-header">
        <div class="detail-back">
          <el-button plain @click="$router.push('/lost')">
            <el-icon><ArrowLeft /></el-icon> 返回列表
          </el-button>
        </div>
        <div class="detail-status" :class="getStatusClass(lostItem.status)">
          {{ getStatusText(lostItem.status) }}
        </div>
      </div>
      
      <h1 class="detail-title">{{ lostItem.title }}</h1>
      
      <div class="detail-meta">
        <div class="meta-item">
          <el-icon><User /></el-icon>
          <span>{{ lostItem.username || '匿名用户' }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Timer /></el-icon>
          <span>{{ formatTime(lostItem.lostTime) }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Location /></el-icon>
          <span>{{ lostItem.lostPlace || '未知地点' }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Notebook /></el-icon>
          <span>{{ lostItem.categoryName || '未分类' }}</span>
        </div>
        <div class="meta-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDateTime(lostItem.createTime) }}</span>
        </div>
      </div>
      
      <!-- 图片轮播 -->
      <div class="detail-images" v-if="images && images.length > 0">
        <el-carousel :interval="4000" type="card" height="400px">
          <el-carousel-item v-for="(image, index) in images" :key="index">
            <el-image :src="image" fit="contain" />
          </el-carousel-item>
        </el-carousel>
      </div>
      
      <div class="detail-content">
        <h3 class="content-title">物品描述</h3>
        <div class="content-text">
          {{ lostItem.description || '暂无描述' }}
        </div>
      </div>
      
      <!-- 认领申请按钮 -->
      <ClaimButton 
        :item-id="lostItem.id"
        :item-type="1"
        :item-status="lostItem.status"
        :user-id="lostItem.userId"
        @claim-success="handleClaimSuccess"
      />
      
      <div class="detail-footer">
        <el-button type="primary" @click="showContactDialog" v-if="lostItem.status === 0">
          <el-icon><Phone /></el-icon> 联系发布者
        </el-button>
        <el-button @click="handleShare">
          <el-icon><Share /></el-icon> 分享
        </el-button>
        
        <!-- 管理员或发布者操作 -->
        <div v-if="canManage" class="admin-actions">
          <el-button type="warning" @click="handleEdit">
            <el-icon><Edit /></el-icon> 编辑
          </el-button>
          <el-button type="danger" @click="handleDelete">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
          <el-button v-if="lostItem.status === 0" @click="handleClose">关闭认领</el-button>
        </div>
      </div>
    </div>
    
    <!-- 联系方式对话框 -->
    <el-dialog v-model="contactDialogVisible" title="联系方式" width="30%">
      <div class="contact-info" v-if="lostItem.id">
        <div class="info-item">
          <div class="info-label">联系人:</div>
          <div class="info-value">{{ lostItem.contactName || lostItem.userName || '未提供' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">联系电话:</div>
          <div class="info-value">{{ lostItem.contactPhone || '未提供' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">联系邮箱:</div>
          <div class="info-value">{{ lostItem.contactEmail || '未提供' }}</div>
        </div>
        <div class="info-item">
          <div class="info-label">备注:</div>
          <div class="info-value">{{ lostItem.contactNote || '无' }}</div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="contactDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享" width="30%">
      <div class="share-info">
        <p>复制以下链接分享给朋友</p>
        <el-input v-model="shareUrl" readonly>
          <template #append>
            <el-button @click="copyShareUrl">复制</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, User, Timer, Location, Notebook, Calendar, Phone, Share, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import ClaimButton from '@/components/frontend/ClaimButton.vue'
import DateUtils from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 失物信息
const lostItem = ref({})
const loading = ref(true)

// 弹窗控制
const contactDialogVisible = ref(false)
const shareDialogVisible = ref(false)
const shareUrl = ref('')

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 图片列表
const images = computed(() => {
  if (!lostItem.value || !lostItem.value.images) return []
  
  // 确保处理字符串或数组的情况
  let imageArray = []
  if (typeof lostItem.value.images === 'string') {
    imageArray = lostItem.value.images.split(',').filter(img => img && img.trim() !== '')
  } else if (Array.isArray(lostItem.value.images)) {
    imageArray = lostItem.value.images.filter(img => img && img.trim() !== '')
  }
  
  // 统一处理图片路径
  return imageArray.map(img => {
    if (!img) return ''
    // 创建临时变量以便于调试
    const finalUrl = img.startsWith('http') ? img : baseAPI + img
    return finalUrl
  }).filter(url => url && url.trim() !== '')
})

// 判断是否有管理权限
const canManage = computed(() => {
  if (!userStore.userInfo) return false
  // 管理员或创建者可以管理
  return userStore.userInfo.roleCode === 'ADMIN' ||
         lostItem.value.userId === userStore.userInfo.id
})

// 获取失物详情
const fetchLostItemDetail = async () => {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    await request.get(`/lost-item/${id}`, null, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        lostItem.value = data || {}
      },
      onError: (error) => {
        console.error('获取失物详情失败:', error)
        lostItem.value = {}
      }
    })
  } catch (error) {
    console.error('获取失物详情失败:', error)
    lostItem.value = {}
  } finally {
    loading.value = false
  }
}

// 认领申请成功回调
const handleClaimSuccess = () => {
  ElMessage.success('申请提交成功，请等待审核')
}

// 获取状态文字
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待认领'
    case 1: return '已认领'
    case 2: return '已关闭'
    default: return '未知'
  }
}

// 获取状态类名
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-claimed'
    case 2: return 'status-closed'
    default: ''
  }
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return '未知时间'
  return DateUtils.formatDate(dateStr)
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  return DateUtils.formatDateTime(dateStr)
}

// 显示联系对话框
const showContactDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再联系发布者')
    router.push('/login')
    return
  }
  contactDialogVisible.value = true
}

// 处理分享
const handleShare = () => {
  shareUrl.value = window.location.href
  shareDialogVisible.value = true
}

// 复制分享链接
const copyShareUrl = () => {
  navigator.clipboard.writeText(shareUrl.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
    shareDialogVisible.value = false
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 编辑失物信息
const handleEdit = () => {
  router.push(`/lost/edit/${lostItem.value.id}`)
}

// 删除失物信息
const handleDelete = () => {
  ElMessageBox.confirm(
    '确定要删除这条失物信息吗？删除后无法恢复！',
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.delete(`/lost-item/${lostItem.value.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          router.push('/lost')
        }
      })
    } catch (error) {
      console.error('删除失物信息失败:', error)
    }
  }).catch(() => {
    // 取消删除操作
  })
}

// 关闭认领
const handleClose = () => {
  ElMessageBox.confirm(
    '确定要关闭这条失物认领吗？',
    '关闭确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.put(
        `/lost-item/${lostItem.value.id}/status`,
        { status: 2 },
        {
          successMsg: '关闭成功',
          onSuccess: () => {
            lostItem.value.status = 2
          }
        }
      )
    } catch (error) {
      console.error('关闭失物认领失败:', error)
    }
  }).catch(() => {
    // 取消操作
  })
}

// 初始化
onMounted(() => {
  fetchLostItemDetail()
})
</script>

<style lang="scss">
// UI设计变量 - 全局主题
:root {
  // 静谧蓝系列
  --serene-blue-50: #f0f4ff;
  --serene-blue-100: #e0ebff;
  --serene-blue-200: #c7d7fe;
  --serene-blue-300: #a5b8fc;
  --serene-blue-400: #818cf8;
  --serene-blue-500: #6366f1;
  --serene-blue-600: #4f46e5;
  --serene-blue-700: #4338ca;
  --serene-blue-800: #3730a3;
  
  // 暖阳黄系列
  --warm-yellow-50: #fffbeb;
  --warm-yellow-100: #fef3c7;
  --warm-yellow-200: #fde68a;
  --warm-yellow-300: #fcd34d;
  --warm-yellow-400: #fbbf24;
  --warm-yellow-500: #f59e0b;
  --warm-yellow-600: #d97706;
  
  // 中性灰系列
  --neutral-gray-50: #f9fafb;
  --neutral-gray-100: #f3f4f6;
  --neutral-gray-200: #e5e7eb;
  --neutral-gray-300: #d1d5db;
  --neutral-gray-400: #9ca3af;
  --neutral-gray-500: #6b7280;
  --neutral-gray-600: #4b5563;
  --neutral-gray-700: #374151;
  --neutral-gray-800: #1f2937;
  --neutral-gray-900: #111827;
  
  // 设计系统
  --border-radius-sm: 6px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
  --border-radius-xl: 16px;
  --border-radius-2xl: 24px;
  
  // 阴影
  --shadow-soft: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  --shadow-medium: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-large: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  
  // 间距
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --spacing-2xl: 48px;
}
</style>

<style lang="scss" scoped>

.lost-detail-page {
  font-family: 'Source Han Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.6;
  color: var(--neutral-gray-800);
  
  .loading-container {
    padding: var(--spacing-2xl) 0;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .not-found {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: var(--spacing-2xl) 0;
    background: white;
    border-radius: var(--border-radius-xl);
    box-shadow: var(--shadow-soft);
    border: 1px solid var(--neutral-gray-200);
  }
  
  .detail-container {
    background: white;
    border-radius: var(--border-radius-2xl);
    box-shadow: var(--shadow-medium);
    border: 1px solid var(--neutral-gray-200);
    padding: var(--spacing-2xl);
    margin-bottom: var(--spacing-xl);
    overflow: hidden;
    animation: fadeInUp 0.6s ease-out;
    
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-xl);
      padding-bottom: var(--spacing-lg);
      border-bottom: 2px solid var(--neutral-gray-100);
      
      .detail-back {
        .el-button {
          border-radius: var(--border-radius-lg);
          border: 2px solid var(--neutral-gray-200);
          color: var(--neutral-gray-600);
          background: white;
          transition: all 0.3s ease;
          
          &:hover {
            border-color: var(--serene-blue-400);
            color: var(--serene-blue-600);
            transform: translateX(-4px);
          }
        }
      }
      
      .detail-status {
        padding: var(--spacing-sm) var(--spacing-lg);
        border-radius: var(--border-radius-xl);
        font-weight: 600;
        font-size: 14px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        
        &.status-pending {
          background: var(--neutral-gray-100);
          color: var(--neutral-gray-700);
          border: 1px solid var(--neutral-gray-300);
        }
        
        &.status-claimed {
          background: var(--warm-yellow-100);
          color: var(--warm-yellow-800);
          border: 1px solid var(--warm-yellow-300);
        }
        
        &.status-closed {
          background: var(--neutral-gray-800);
          color: white;
        }
      }
    }
    
    .detail-title {
      font-size: 36px;
      font-weight: 700;
      margin-bottom: var(--spacing-xl);
      color: var(--neutral-gray-900);
      line-height: 1.2;
    }
    
    .detail-meta {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: var(--spacing-lg);
      margin-bottom: var(--spacing-2xl);
      padding: var(--spacing-xl);
      background: var(--neutral-gray-50);
      border-radius: var(--border-radius-xl);
      border: 1px solid var(--neutral-gray-200);
      
      .meta-item {
        display: flex;
        align-items: center;
        color: var(--neutral-gray-600);
        padding: var(--spacing-md);
        background: white;
        border-radius: var(--border-radius-lg);
        box-shadow: var(--shadow-soft);
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: var(--shadow-medium);
        }
        
        .el-icon {
          margin-right: var(--spacing-md);
          color: var(--serene-blue-600);
          font-size: 18px;
        }
        
        span {
          font-weight: 500;
          color: var(--neutral-gray-800);
        }
      }
    }
    
    .detail-images {
      margin: var(--spacing-2xl) 0;
      
      .el-carousel {
        border-radius: var(--border-radius-xl);
        overflow: hidden;
        box-shadow: var(--shadow-large);
        
        .el-carousel__item {
          display: flex;
          justify-content: center;
          align-items: center;
          background: var(--neutral-gray-50);
          
          .el-image {
            height: 100%;
            width: 100%;
            object-fit: contain;
          }
        }
        
        :deep(.el-carousel__button) {
          background: var(--serene-blue-600);
          border-radius: 50%;
          width: 12px;
          height: 12px;
        }
        
        :deep(.el-carousel__arrow) {
          background: rgba(255, 255, 255, 0.9);
          color: var(--serene-blue-600);
          border-radius: 50%;
          width: 48px;
          height: 48px;
          
          &:hover {
            background: white;
            transform: scale(1.1);
          }
        }
      }
    }
    
    .detail-content {
      margin: var(--spacing-2xl) 0;
      
      .content-title {
        font-size: 24px;
        font-weight: 700;
        margin-bottom: var(--spacing-lg);
        color: var(--neutral-gray-900);
        position: relative;
        padding-left: var(--spacing-lg);
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 24px;
          background: linear-gradient(135deg, var(--serene-blue-600), var(--warm-yellow-500));
          border-radius: var(--border-radius-sm);
        }
      }
      
      .content-text {
        line-height: 1.8;
        color: var(--neutral-gray-700);
        white-space: pre-line;
        min-height: 120px;
        padding: var(--spacing-xl);
        background: var(--neutral-gray-50);
        border-radius: var(--border-radius-xl);
        border: 1px solid var(--neutral-gray-200);
        font-size: 16px;
      }
    }
    
    .detail-footer {
      margin-top: var(--spacing-2xl);
      padding-top: var(--spacing-xl);
      border-top: 2px solid var(--neutral-gray-100);
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: var(--spacing-lg);
      
      .el-button {
        border-radius: var(--border-radius-lg);
        padding: 12px 24px;
        font-weight: 500;
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        
        &.el-button--primary {
          background: var(--serene-blue-600);
          border-color: var(--serene-blue-600);
          
          &:hover {
            background: var(--serene-blue-700);
            border-color: var(--serene-blue-700);
            transform: translateY(-2px);
            box-shadow: var(--shadow-large);
          }
        }
        
        &:not(.el-button--primary):not(.el-button--warning):not(.el-button--danger) {
          background: white;
          color: var(--neutral-gray-600);
          border: 2px solid var(--neutral-gray-200);
          
          &:hover {
            border-color: var(--serene-blue-400);
            color: var(--serene-blue-600);
            transform: translateY(-2px);
          }
        }
        
        &.el-button--warning {
          background: var(--warm-yellow-500);
          border-color: var(--warm-yellow-500);
          
          &:hover {
            background: var(--warm-yellow-600);
            border-color: var(--warm-yellow-600);
            transform: translateY(-2px);
          }
        }
        
        &.el-button--danger {
          &:hover {
            transform: translateY(-2px);
          }
        }
      }
      
      .admin-actions {
        display: flex;
        gap: var(--spacing-md);
        flex-wrap: wrap;
      }
    }
  }
  
  .contact-info, .share-info {
    .info-item {
      display: flex;
      margin-bottom: var(--spacing-md);
      padding: var(--spacing-md);
      background: var(--neutral-gray-50);
      border-radius: var(--border-radius-lg);
      border: 1px solid var(--neutral-gray-200);
      
      .info-label {
        width: 100px;
        font-weight: 600;
        color: var(--neutral-gray-700);
      }
      
      .info-value {
        color: var(--neutral-gray-800);
        font-weight: 500;
      }
    }
    
    p {
      margin-bottom: var(--spacing-md);
      color: var(--neutral-gray-600);
      font-size: 15px;
    }
  }
}

// 动画定义
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .lost-detail-page {
    .detail-container {
      padding: var(--spacing-lg);
      border-radius: var(--border-radius-xl);
      
      .detail-title {
        font-size: 28px;
      }
      
      .detail-meta {
        grid-template-columns: 1fr;
        gap: var(--spacing-md);
        padding: var(--spacing-lg);
        
        .meta-item {
          padding: var(--spacing-sm);
        }
      }
      
      .detail-images {
        margin: var(--spacing-lg) 0;
        
        .el-carousel {
          :deep(.el-carousel__arrow) {
            width: 40px;
            height: 40px;
          }
        }
      }
      
      .detail-content {
        .content-title {
          font-size: 20px;
          padding-left: var(--spacing-md);
          
          &::before {
            width: 3px;
            height: 20px;
          }
        }
        
        .content-text {
          padding: var(--spacing-lg);
        }
      }
      
      .detail-footer {
        flex-direction: column;
        align-items: stretch;
        
        .admin-actions {
          justify-content: center;
        }
      }
    }
  }
}
</style> 
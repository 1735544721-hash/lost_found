<template>
  <div class="found-detail-container" v-loading="loading">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">招领信息详情</span>
      </template>
    </el-page-header>

    <div class="detail-content" v-if="foundItem">
      <el-card shadow="never">
        <div class="detail-header">
          <h1>{{ foundItem.title }}</h1>
          <div class="status-tags">
            <el-tag v-if="foundItem.status === 0" type="info">待认领</el-tag>
            <el-tag v-else-if="foundItem.status === 1" type="success">已认领</el-tag>
            <el-tag v-else-if="foundItem.status === 2" type="danger">已关闭</el-tag>
          </div>
        </div>

        <div class="item-info">
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span class="label">发布人：</span>
            <span class="value">{{ foundItem.username }}</span>
          </div>
          <div class="info-item">
            <el-icon><Collection /></el-icon>
            <span class="label">分类：</span>
            <span class="value">{{ foundItem.categoryName }}</span>
          </div>
          <div class="info-item">
            <el-icon><LocationFilled /></el-icon>
            <span class="label">拾取地点：</span>
            <span class="value">{{ foundItem.foundPlace }}</span>
          </div>
          <div class="info-item">
            <el-icon><Timer /></el-icon>
            <span class="label">拾取时间：</span>
            <span class="value">{{ formatDateTime(foundItem.foundTime) }}</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span class="label">发布时间：</span>
            <span class="value">{{ formatDateTime(foundItem.createTime) }}</span>
          </div>
        </div>

        <div class="contact-info">
          <h3>联系方式</h3>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span class="label">联系人：</span>
            <span class="value">{{ foundItem.contactName }}</span>
          </div>
          <div class="info-item">
            <el-icon><Phone /></el-icon>
            <span class="label">联系电话：</span>
            <span class="value">{{ foundItem.contactPhone }}</span>
          </div>
        </div>

        <div class="item-description">
          <h3>物品描述</h3>
          <p>{{ foundItem.description || '暂无描述' }}</p>
        </div>

        <div class="item-images" v-if="images && images.length > 0">
          <h3>物品图片</h3>
          <el-image-viewer v-if="showViewer" :url-list="images" :initial-index="imageIndex" @close="closeViewer" />
          <div class="image-list">
            <div class="image-item" v-for="(image, index) in images" :key="index" @click="previewImage(index)">
              <el-image :src="image" fit="cover" loading="lazy">
                <template #error>
                  <div class="image-error">
                    <el-icon><PictureFilled /></el-icon>
                    <span>加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
        </div>

        <!-- 认领申请按钮 -->
        <ClaimButton 
          :item-id="foundItem.id"
          :item-type="0"
          :item-status="foundItem.status"
          :user-id="foundItem.userId"
          @claim-success="handleClaimSuccess"
        />

        <div class="action-buttons">
          <template v-if="isMyPost">
            <el-button type="primary" @click="handleEdit" v-if="foundItem.status === 0">
              <el-icon><EditPen /></el-icon>编辑
            </el-button>
            <el-button type="success" @click="handleChangeStatus(1)" v-if="foundItem.status === 0">
              <el-icon><Check /></el-icon>标记为已认领
            </el-button>
            <el-button type="danger" @click="handleChangeStatus(2)" v-if="foundItem.status === 0">
              <el-icon><CircleClose /></el-icon>关闭
            </el-button>
            <el-button type="info" @click="handleChangeStatus(0)" v-else>
              <el-icon><RefreshRight /></el-icon>重新开启
            </el-button>
          </template>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  User, LocationFilled, Timer, Calendar, Collection, Phone, 
  PictureFilled, EditPen, Check, CircleClose, RefreshRight, Opportunity
} from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'
import request from '@/utils/request'
import ClaimButton from '@/components/frontend/ClaimButton.vue'

// 日期格式化函数
const formatDateTime = (date) => {
  return DateUtils.formatDateTime(date)
}

// 变量定义
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(true)
const foundItem = ref(null)
const showViewer = ref(false)
const imageIndex = ref(0)

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 获取招领信息详情
const fetchFoundItem = async () => {
  loading.value = true
  const id = route.params.id
  
  try {
    await request.get(`/found-item/${id}`, {}, {
      onSuccess: (data) => {
        foundItem.value = data
      },
      onError: () => {
        router.push('/404')
      }
    })
  } catch (error) {
    console.error('获取招领信息详情失败:', error)
    router.push('/404')
  } finally {
    loading.value = false
  }
}

// 判断是否是当前用户发布的招领信息
const isMyPost = computed(() => {
  if (!foundItem.value || !userStore.isLoggedIn || !userStore.userInfo) {
    return false
  }
  return foundItem.value.userId === userStore.userInfo.id
})

// 图片列表
const images = computed(() => {
  if (!foundItem.value || !foundItem.value.images) return []
  
  // 确保处理字符串或数组的情况
  let imageArray = []
  if (typeof foundItem.value.images === 'string') {
    imageArray = foundItem.value.images.split(',').filter(img => img && img.trim() !== '')
  } else if (Array.isArray(foundItem.value.images)) {
    imageArray = foundItem.value.images.filter(img => img && img.trim() !== '')
  }
  
  // 统一处理图片路径
  return imageArray.map(img => {
    if (!img) return ''
    // 创建临时变量以便于调试
    const finalUrl = img.startsWith('http') ? img : baseAPI + img
    return finalUrl
  }).filter(url => url && url.trim() !== '')
})

// 图片预览
const previewImage = (index) => {
  imageIndex.value = index
  showViewer.value = true
}

const closeViewer = () => {
  showViewer.value = false
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 编辑招领信息
const handleEdit = () => {
  router.push(`/found/edit/${foundItem.value.id}`)
}

// 认领申请成功
const handleClaimSuccess = () => {
  ElMessage.success('申请提交成功，请等待审核')
}

// 更改招领信息状态
const handleChangeStatus = (status) => {
  let confirmText = ''
  let statusText = ''
  
  if (status === 0) {
    confirmText = '确定要重新开启这条招领信息吗？'
    statusText = '重新开启成功'
  } else if (status === 1) {
    confirmText = '确定要标记为已认领吗？'
    statusText = '标记为已认领成功'
  } else if (status === 2) {
    confirmText = '确定要关闭这条招领信息吗？'
    statusText = '关闭成功'
  }
  
  ElMessageBox.confirm(confirmText, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/found-item/status/${foundItem.value.id}/${status}`, {}, {
        successMsg: statusText,
        onSuccess: () => {
          fetchFoundItem()
        }
      })
    } catch (error) {
      console.error('更新状态失败:', error)
    }
  }).catch(() => {})
}

// 页面加载时获取招领信息详情
onMounted(() => {
  fetchFoundItem()
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

.found-detail-container {
  font-family: 'Source Han Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.6;
  color: var(--neutral-gray-800);
  
  :deep(.el-page-header) {
    padding: var(--spacing-xl) 0;
    margin-bottom: var(--spacing-xl);
    
    .el-page-header__back {
      .el-icon {
        color: var(--serene-blue-600);
        transition: all 0.3s ease;
        
        &:hover {
          color: var(--serene-blue-700);
          transform: translateX(-4px);
        }
      }
    }
    
    .el-page-header__title {
      color: var(--neutral-gray-600);
      font-weight: 500;
    }
    
    .el-page-header__content {
      .page-title {
        font-size: 24px;
        font-weight: 700;
        color: var(--neutral-gray-900);
      }
    }
  }
  
  .detail-content {
    margin-top: var(--spacing-xl);
    animation: fadeInUp 0.6s ease-out;
    
    :deep(.el-card) {
      border-radius: var(--border-radius-2xl);
      border: 1px solid var(--neutral-gray-200);
      box-shadow: var(--shadow-medium);
      
      .el-card__body {
        padding: var(--spacing-2xl);
      }
    }
    
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-2xl);
      border-bottom: 2px solid var(--neutral-gray-100);
      padding-bottom: var(--spacing-xl);
      
      h1 {
        margin: 0;
        font-size: 32px;
        font-weight: 700;
        color: var(--neutral-gray-900);
        line-height: 1.2;
      }
      
      .status-tags {
        .el-tag {
          border-radius: var(--border-radius-lg);
          padding: var(--spacing-sm) var(--spacing-lg);
          font-weight: 600;
          font-size: 14px;
          text-transform: uppercase;
          letter-spacing: 0.5px;
          
          &.el-tag--info {
            background: var(--neutral-gray-100);
            color: var(--neutral-gray-700);
            border-color: var(--neutral-gray-300);
          }
          
          &.el-tag--success {
            background: var(--warm-yellow-100);
            color: var(--warm-yellow-800);
            border-color: var(--warm-yellow-300);
          }
          
          &.el-tag--danger {
            background: var(--neutral-gray-800);
            color: white;
            border-color: var(--neutral-gray-800);
          }
        }
      }
    }
    
    .item-info, .contact-info {
      margin-bottom: var(--spacing-2xl);
      padding: var(--spacing-xl);
      background: var(--neutral-gray-50);
      border-radius: var(--border-radius-xl);
      border: 1px solid var(--neutral-gray-200);
      
      h3 {
        font-size: 20px;
        font-weight: 700;
        margin: 0 0 var(--spacing-lg) 0;
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
          height: 20px;
          background: linear-gradient(135deg, var(--serene-blue-600), var(--warm-yellow-500));
          border-radius: var(--border-radius-sm);
        }
      }
      
      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: var(--spacing-md);
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
        
        .label {
          color: var(--neutral-gray-600);
          margin-right: var(--spacing-sm);
          font-weight: 500;
        }
        
        .value {
          color: var(--neutral-gray-800);
          font-weight: 600;
        }
      }
    }
    
    .item-description {
      margin-bottom: var(--spacing-2xl);
      padding: var(--spacing-xl);
      background: var(--neutral-gray-50);
      border-radius: var(--border-radius-xl);
      border: 1px solid var(--neutral-gray-200);
      
      h3 {
        font-size: 20px;
        font-weight: 700;
        margin: 0 0 var(--spacing-lg) 0;
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
          height: 20px;
          background: linear-gradient(135deg, var(--serene-blue-600), var(--warm-yellow-500));
          border-radius: var(--border-radius-sm);
        }
      }
      
      p {
        margin: 0;
        color: var(--neutral-gray-700);
        line-height: 1.8;
        white-space: pre-wrap;
        font-size: 16px;
        padding: var(--spacing-lg);
        background: white;
        border-radius: var(--border-radius-lg);
        box-shadow: var(--shadow-soft);
        min-height: 80px;
      }
    }
    
    .item-images {
      margin-bottom: var(--spacing-2xl);
      
      h3 {
        font-size: 20px;
        font-weight: 700;
        margin: 0 0 var(--spacing-lg) 0;
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
          height: 20px;
          background: linear-gradient(135deg, var(--serene-blue-600), var(--warm-yellow-500));
          border-radius: var(--border-radius-sm);
        }
      }
      
      .image-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: var(--spacing-lg);
        
        .image-item {
          height: 200px;
          cursor: pointer;
          border-radius: var(--border-radius-xl);
          overflow: hidden;
          box-shadow: var(--shadow-medium);
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          
          &:hover {
            transform: translateY(-4px);
            box-shadow: var(--shadow-large);
          }
          
          .el-image {
            width: 100%;
            height: 100%;
            display: block;
            transition: transform 0.3s ease;
            
            &:hover {
              transform: scale(1.05);
            }
          }
          
          .image-error {
            width: 100%;
            height: 100%;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: var(--neutral-gray-50);
            color: var(--neutral-gray-400);
            
            .el-icon {
              font-size: 32px;
              margin-bottom: var(--spacing-sm);
            }
            
            span {
              font-size: 14px;
              font-weight: 500;
            }
          }
        }
      }
    }
    
    .action-buttons {
      display: flex;
      justify-content: flex-start;
      flex-wrap: wrap;
      gap: var(--spacing-md);
      border-top: 2px solid var(--neutral-gray-100);
      padding-top: var(--spacing-xl);
      margin-top: var(--spacing-2xl);
      
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
        
        &.el-button--success {
          background: var(--warm-yellow-500);
          border-color: var(--warm-yellow-500);
          color: white;
          
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
        
        &.el-button--info {
          background: var(--neutral-gray-600);
          border-color: var(--neutral-gray-600);
          
          &:hover {
            background: var(--neutral-gray-700);
            border-color: var(--neutral-gray-700);
            transform: translateY(-2px);
          }
        }
        
        .el-icon {
          margin-right: var(--spacing-sm);
        }
      }
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
  .found-detail-container {
    .detail-content {
      :deep(.el-card) {
        .el-card__body {
          padding: var(--spacing-lg);
        }
      }
      
      .detail-header {
        flex-direction: column;
        align-items: flex-start;
        gap: var(--spacing-md);
        
        h1 {
          font-size: 24px;
        }
      }
      
      .item-info, .contact-info, .item-description {
        padding: var(--spacing-lg);
        
        h3 {
          font-size: 18px;
          padding-left: var(--spacing-md);
          
          &::before {
            width: 3px;
            height: 18px;
          }
        }
        
        .info-item {
          padding: var(--spacing-sm);
          
          .el-icon {
            margin-right: var(--spacing-sm);
            font-size: 16px;
          }
        }
      }
      
      .item-images {
        .image-list {
          grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
          gap: var(--spacing-md);
          
          .image-item {
            height: 150px;
          }
        }
      }
      
      .action-buttons {
        justify-content: center;
        
        .el-button {
          flex: 1;
          min-width: 120px;
        }
      }
    }
  }
}

@media (max-width: 480px) {
  .found-detail-container {
    .detail-content {
      .item-images {
        .image-list {
          grid-template-columns: repeat(2, 1fr);
          gap: var(--spacing-sm);
        }
      }
      
      .action-buttons {
        flex-direction: column;
        
        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style> 
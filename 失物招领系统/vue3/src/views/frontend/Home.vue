<template>
  <div class="home-page">
    <!-- 顶部Banner -->
    <div class="banner">
      <div class="banner-content">
        <h1>失物招领系统</h1>
        <p>快速找回您的失物，或帮助他人找回失物</p>
        <div class="banner-actions">
          <el-button type="primary" size="large" @click="$router.push('/lost')">浏览失物信息</el-button>
          <el-button size="large" @click="$router.push('/lost/publish')" v-if="isLoggedIn">发布失物信息</el-button>
        </div>
      </div>
    </div>
    
    <!-- 统计数据 -->
    <div class="stats-section">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-number">{{ statistics.totalItems || 0 }}</div>
          <div class="stat-label">失物总数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ statistics.totalClaimed || 0 }}</div>
          <div class="stat-label">已认领</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ statistics.totalPending || 0 }}</div>
          <div class="stat-label">待认领</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ statistics.totalUsers || 0 }}</div>
          <div class="stat-label">用户数</div>
        </div>
      </div>
    </div>
    
    <!-- 最新失物信息 -->
    <div class="recent-items-section">
      <div class="section-header">
        <h2>最新失物信息</h2>
        <el-button text @click="$router.push('/lost')">查看全部 <el-icon><ArrowRight /></el-icon></el-button>
      </div>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <el-empty v-else-if="!recentItems.length" description="暂无失物信息" />
      
      <div v-else class="items-grid">
        <el-card 
          v-for="item in recentItems" 
          :key="item.id" 
          class="item-card" 
          shadow="hover"
          @click="viewDetail(item)"
        >
          <div class="item-image">
            <el-image 
              :src="getFirstImage(item.images)"
              fit="cover"
              lazy
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="item-category">{{ item.categoryName || '未分类' }}</div>
          </div>
          <div class="item-content">
            <h3 class="item-title">{{ item.title }}</h3>
            <p class="item-location">
              <el-icon><Location /></el-icon>
              <span>{{ item.lostPlace || '未知地点' }}</span>
            </p>
            <div class="item-footer">
              <span class="item-date">{{ formatDate(item.createTime) }}</span>
              <el-tag size="small" :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>
    
    <!-- 使用指南 -->
    <div class="guide-section">
      <h2>如何使用</h2>
      
      <div class="guide-steps">
        <div class="guide-step">
          <div class="step-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="step-content">
            <h3>第一步：注册登录</h3>
            <p>注册账号并登录系统，开始使用失物招领服务</p>
          </div>
        </div>
        
        <div class="guide-step">
          <div class="step-icon">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="step-content">
            <h3>第二步：发布失物</h3>
            <p>发布您丢失的物品信息，包括详细描述和图片</p>
          </div>
        </div>
        
        <div class="guide-step">
          <div class="step-icon">
            <el-icon><Search /></el-icon>
          </div>
          <div class="step-content">
            <h3>第三步：寻找物品</h3>
            <p>浏览或搜索失物信息，找到您丢失的物品</p>
          </div>
        </div>
        
        <div class="guide-step">
          <div class="step-icon">
            <el-icon><Phone /></el-icon>
          </div>
          <div class="step-content">
            <h3>第四步：联系认领</h3>
            <p>联系发布者并安排认领失物</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 关于我们 -->
    <div class="about-section">
      <h2>关于失物招领系统</h2>
      <p>
        失物招领系统旨在为用户提供一个便捷的平台，帮助人们找回丢失的物品或归还拾获的物品。
        我们致力于为社区创造更加和谐、互助的环境，让每一件失物都能够回到主人手中。
      </p>
      <p>
        如果您有任何问题或建议，请随时与我们联系。感谢您使用我们的系统！
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { ArrowRight, Picture, Location, UserFilled, Plus, Search, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 数据状态
const loading = ref(false)
const recentItems = ref([])
const statistics = ref({
  totalItems: 0,
  totalClaimed: 0,
  totalPending: 0,
  totalUsers: 0
})

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 获取最新失物信息
const fetchRecentItems = async () => {
  loading.value = true
  try {
    await request.get('/lost-item/page', {
      currentPage: 1,
      size: 8,
      status: 0 // 只获取待认领的
    }, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        recentItems.value = res.records || []
      }
    })
  } catch (error) {
    console.error('获取最新失物信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    await request.get('/lost-item/statistics', null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        statistics.value = res || {
          totalItems: 0,
          totalClaimed: 0,
          totalPending: 0,
          totalUsers: 0
        }
      }
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取第一张图片
const getFirstImage = (images) => {
  if (!images) return ''
  const imageArray = images.split(',').filter(img => img)
  if (imageArray.length === 0) return ''
  const firstImage = imageArray[0]
  return firstImage.startsWith('http') ? firstImage : baseAPI + firstImage
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString()
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

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info'
    case 1: return 'success'
    case 2: return 'danger'
    default: return ''
  }
}

// 查看详情
const viewDetail = (item) => {
  router.push(`/lost/detail/${item.id}`)
}

// 初始化
onMounted(() => {
  fetchRecentItems()
  fetchStatistics()
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
  --serene-blue-900: #312e81;
  
  // 暖阳黄系列
  --warm-yellow-50: #fffbeb;
  --warm-yellow-100: #fef3c7;
  --warm-yellow-200: #fde68a;
  --warm-yellow-300: #fcd34d;
  --warm-yellow-400: #fbbf24;
  --warm-yellow-500: #f59e0b;
  --warm-yellow-600: #d97706;
  --warm-yellow-700: #b45309;
  --warm-yellow-800: #92400e;
  
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

.home-page {
  font-family: 'Source Han Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.6;
  color: var(--neutral-gray-800);
  
  .banner {
    height: 480px;
    background: linear-gradient(135deg, var(--serene-blue-50) 0%, var(--warm-yellow-50) 100%);
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: var(--spacing-2xl);
    margin-bottom: var(--spacing-2xl);
    border-radius: var(--border-radius-xl);
    overflow: hidden;
    
    // 背景装饰
    &::before {
      content: '';
      position: absolute;
      top: -50%;
      right: -50%;
      width: 100%;
      height: 100%;
      background: radial-gradient(circle, var(--serene-blue-100) 0%, transparent 50%);
      opacity: 0.6;
      animation: float 6s ease-in-out infinite;
    }
    
    &::after {
      content: '';
      position: absolute;
      bottom: -30%;
      left: -30%;
      width: 80%;
      height: 80%;
      background: radial-gradient(circle, var(--warm-yellow-100) 0%, transparent 50%);
      opacity: 0.4;
      animation: float 8s ease-in-out infinite reverse;
    }
    
    .banner-content {
      max-width: 720px;
      position: relative;
      z-index: 2;
      animation: fadeInUp 1s ease-out;
      
      h1 {
        font-size: 48px;
        font-weight: 700;
        margin-bottom: var(--spacing-lg);
        color: var(--serene-blue-800);
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      p {
        font-size: 20px;
        margin-bottom: var(--spacing-2xl);
        color: var(--neutral-gray-600);
        font-weight: 400;
      }
      
      .banner-actions {
        display: flex;
        justify-content: center;
        gap: var(--spacing-md);
        flex-wrap: wrap;
        
        .el-button {
          border-radius: var(--border-radius-lg);
          padding: 14px 28px;
          font-size: 16px;
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
          
          &:not(.el-button--primary) {
            background: white;
            color: var(--serene-blue-600);
            border: 2px solid var(--serene-blue-200);
            
            &:hover {
              background: var(--serene-blue-50);
              border-color: var(--serene-blue-600);
              transform: translateY(-2px);
              box-shadow: var(--shadow-medium);
            }
          }
        }
      }
    }
  }
  
  .stats-section {
    margin-bottom: var(--spacing-2xl);
    
    .stats-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: var(--spacing-lg);
      
      .stat-item {
        background: white;
        border-radius: var(--border-radius-xl);
        padding: var(--spacing-xl);
        text-align: center;
        box-shadow: var(--shadow-soft);
        border: 1px solid var(--neutral-gray-200);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 4px;
          background: linear-gradient(90deg, var(--serene-blue-500), var(--warm-yellow-400));
        }
        
        &:hover {
          transform: translateY(-4px);
          box-shadow: var(--shadow-large);
          border-color: var(--serene-blue-200);
        }
        
        .stat-number {
          font-size: 40px;
          font-weight: 700;
          color: var(--serene-blue-700);
          margin-bottom: var(--spacing-sm);
          font-family: 'Source Han Sans', monospace;
        }
        
        .stat-label {
          font-size: 16px;
          color: var(--neutral-gray-600);
          font-weight: 500;
        }
      }
    }
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-xl);
    
    h2 {
      font-size: 32px;
      font-weight: 700;
      margin: 0;
      color: var(--neutral-gray-800);
      position: relative;
      padding-left: var(--spacing-lg);
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 6px;
        height: 32px;
        background: linear-gradient(135deg, var(--serene-blue-600), var(--warm-yellow-500));
        border-radius: var(--border-radius-sm);
      }
    }
    
    .el-button {
      color: var(--serene-blue-600);
      font-weight: 500;
      transition: all 0.3s ease;
      
      &:hover {
        color: var(--serene-blue-700);
        transform: translateX(4px);
      }
    }
  }
  
  .recent-items-section {
    margin-bottom: var(--spacing-2xl);
    
    .loading-container {
      padding: var(--spacing-2xl) 0;
    }
    
    .items-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: var(--spacing-xl);
      
      .item-card {
        cursor: pointer;
        border-radius: var(--border-radius-xl);
        overflow: hidden;
        border: 1px solid var(--neutral-gray-200);
        transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        background: white;
        
        &:hover {
          transform: translateY(-8px);
          box-shadow: var(--shadow-large);
          border-color: var(--serene-blue-300);
        }
        
        .item-image {
          position: relative;
          height: 220px;
          overflow: hidden;
          
          .el-image {
            width: 100%;
            height: 100%;
            transition: transform 0.3s ease;
          }
          
          &:hover .el-image {
            transform: scale(1.05);
          }
          
          .image-placeholder {
            width: 100%;
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            background: var(--neutral-gray-50);
            
            .el-icon {
              font-size: 48px;
              color: var(--neutral-gray-300);
            }
          }
          
          .item-category {
            position: absolute;
            top: var(--spacing-md);
            left: var(--spacing-md);
            background: var(--serene-blue-600);
            color: white;
            padding: var(--spacing-xs) var(--spacing-md);
            border-radius: var(--border-radius-lg);
            font-size: 13px;
            font-weight: 500;
            backdrop-filter: blur(10px);
            box-shadow: var(--shadow-soft);
          }
        }
        
        .item-content {
          padding: var(--spacing-lg);
          
          .item-title {
            margin: 0 0 var(--spacing-md) 0;
            font-size: 18px;
            font-weight: 600;
            color: var(--neutral-gray-800);
            line-height: 1.4;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }
          
          .item-location {
            margin: var(--spacing-sm) 0;
            font-size: 14px;
            color: var(--neutral-gray-500);
            display: flex;
            align-items: center;
            
            .el-icon {
              margin-right: var(--spacing-sm);
              color: var(--warm-yellow-600);
            }
            
            span {
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
          }
          
          .item-footer {
            margin-top: var(--spacing-lg);
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-top: var(--spacing-md);
            border-top: 1px solid var(--neutral-gray-100);
            
            .item-date {
              font-size: 13px;
              color: var(--neutral-gray-400);
              font-weight: 500;
            }
            
            .el-tag {
              border-radius: var(--border-radius-md);
              font-weight: 500;
              
              &.el-tag--info {
                background: var(--neutral-gray-100);
                color: var(--neutral-gray-600);
                border-color: var(--neutral-gray-200);
              }
              
              &.el-tag--success {
                background: var(--warm-yellow-50);
                color: var(--warm-yellow-700);
                border-color: var(--warm-yellow-200);
              }
            }
          }
        }
      }
    }
  }
  
  .guide-section {
    margin-bottom: var(--spacing-2xl);
    
    h2 {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: var(--spacing-2xl);
      text-align: center;
      color: var(--neutral-gray-800);
    }
    
    .guide-steps {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: var(--spacing-xl);
      
      .guide-step {
        display: flex;
        align-items: flex-start;
        background: white;
        border-radius: var(--border-radius-xl);
        padding: var(--spacing-xl);
        box-shadow: var(--shadow-soft);
        border: 1px solid var(--neutral-gray-200);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        position: relative;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          height: 4px;
          background: linear-gradient(90deg, var(--serene-blue-500), var(--warm-yellow-400));
        }
        
        &:hover {
          transform: translateY(-4px);
          box-shadow: var(--shadow-large);
          border-color: var(--serene-blue-200);
        }
        
        .step-icon {
          width: 64px;
          height: 64px;
          border-radius: 50%;
          background: linear-gradient(135deg, var(--serene-blue-600), var(--serene-blue-700));
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: var(--spacing-lg);
          flex-shrink: 0;
          box-shadow: var(--shadow-medium);
          
          .el-icon {
            font-size: 28px;
          }
        }
        
        .step-content {
          flex: 1;
          
          h3 {
            margin: 0 0 var(--spacing-md) 0;
            font-size: 20px;
            font-weight: 600;
            color: var(--neutral-gray-800);
          }
          
          p {
            margin: 0;
            color: var(--neutral-gray-600);
            font-size: 15px;
            line-height: 1.6;
          }
        }
      }
    }
  }
  
  .about-section {
    background: white;
    border-radius: var(--border-radius-xl);
    padding: var(--spacing-2xl);
    box-shadow: var(--shadow-soft);
    border: 1px solid var(--neutral-gray-200);
    
    h2 {
      font-size: 28px;
      font-weight: 700;
      margin-bottom: var(--spacing-xl);
      color: var(--neutral-gray-800);
      text-align: center;
    }
    
    p {
      margin-bottom: var(--spacing-lg);
      line-height: 1.8;
      color: var(--neutral-gray-600);
      font-size: 16px;
      
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

// 动画定义
@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
  }
}

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

@media (max-width: 768px) {
  .home-page {
    .banner {
      height: 360px;
      padding: var(--spacing-lg);
      margin-bottom: var(--spacing-xl);
      
      .banner-content {
        h1 {
          font-size: 36px;
        }
        
        p {
          font-size: 18px;
        }
        
        .banner-actions {
          flex-direction: column;
          align-items: stretch;
          
          .el-button {
            padding: 16px 24px;
            font-size: 16px;
          }
        }
      }
    }
    
    .stats-section {
      margin-bottom: var(--spacing-xl);
      
      .stats-container {
        grid-template-columns: repeat(2, 1fr);
        gap: var(--spacing-md);
        
        .stat-item {
          padding: var(--spacing-lg);
          
          .stat-number {
            font-size: 32px;
          }
        }
      }
    }
    
    .section-header {
      h2 {
        font-size: 28px;
      }
    }
    
    .recent-items-section {
      .items-grid {
        grid-template-columns: 1fr;
        gap: var(--spacing-lg);
      }
    }
    
    .guide-section {
      .guide-steps {
        grid-template-columns: 1fr;
        gap: var(--spacing-lg);
        
        .guide-step {
          padding: var(--spacing-lg);
          
          .step-icon {
            width: 56px;
            height: 56px;
            margin-right: var(--spacing-md);
          }
        }
      }
    }
    
    .about-section {
      padding: var(--spacing-xl);
      
      h2 {
        font-size: 24px;
      }
    }
  }
}

@media (max-width: 480px) {
  .home-page {
    .banner {
      height: 320px;
      padding: var(--spacing-md);
      
      .banner-content {
        h1 {
          font-size: 28px;
        }
        
        p {
          font-size: 16px;
        }
      }
    }
    
    .stats-section {
      .stats-container {
        grid-template-columns: 1fr;
        gap: var(--spacing-md);
      }
    }
    
    .section-header {
      h2 {
        font-size: 24px;
        padding-left: var(--spacing-md);
        
        &::before {
          width: 4px;
          height: 24px;
        }
      }
    }
  }
}
</style>



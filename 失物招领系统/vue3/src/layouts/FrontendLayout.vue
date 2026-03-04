<template>
  <div class="campus-lost-found">
    <!-- 温馨的顶部导航 -->
    <header class="warm-header">
      <div class="header-container">
        <div class="brand-section" @click="navigateTo('/')">
          <div class="logo-wrapper">
            <img src="../assets/logo.svg" alt="Logo" class="logo" />
            <div class="logo-glow"></div>
          </div>
  
        </div>
        
        <!-- 主要功能导航 - 卡片式 -->
        <nav class="feature-nav">
          <div class="nav-card" :class="{ active: activeMenu === '/' }" @click="navigateTo('/')">
            <div class="nav-icon">
              <el-icon><HomeFilled /></el-icon>
            </div>
            <span>首页</span>
          </div>
          <div class="nav-card lost-card" :class="{ active: activeMenu === '/lost' }" @click="navigateTo('/lost')">
            <div class="nav-icon">
              <el-icon><Search /></el-icon>
            </div>
            <span>寻找失物</span>
          </div>
          <div class="nav-card found-card" :class="{ active: activeMenu === '/found' }" @click="navigateTo('/found')">
            <div class="nav-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <span>招领信息</span>
          </div>
        </nav>
        
        <!-- 用户区域 -->
        <div class="user-section">
          <div class="search-area" v-if="!isMobile">
            <div class="search-wrapper">
              <el-input
                v-model="searchQuery"
                placeholder="搜索你遗失的物品..."
                @keyup.enter="handleSearch"
                clearable
                class="warm-search"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </div>
          
          <!-- 已登录用户 -->
          <div class="logged-user" v-if="isLoggedIn">
            <div class="user-welcome">
              <span class="welcome-text">欢迎回来</span>
              <span class="user-name">{{ username }}</span>
            </div>
            
            <!-- 通知铃铛 -->
            <div class="notification-bubble" @click="navigateTo('/notification')">
              <div class="bell-icon">
                <el-icon><Bell /></el-icon>
              </div>
            </div>
            
            <!-- 用户头像下拉 -->
            <el-dropdown @command="handleCommand" trigger="hover" class="user-dropdown">
              <div class="avatar-wrapper">
                <el-avatar :src="userAvatar" :size="48" class="user-avatar">
                  {{ userInitial }}
                </el-avatar>
                <div class="online-dot"></div>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="warm-dropdown">
                  <div class="dropdown-header">
                    <div class="user-info">
                      <strong>{{ username }}</strong>
                      <span class="role-tag">{{ isAdmin ? '管理员' : '普通用户' }}</span>
                    </div>
                  </div>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    个人资料
                  </el-dropdown-item>
                  <el-dropdown-item command="myClaims">
                    <el-icon><Document /></el-icon>
                    我的申请
                  </el-dropdown-item>
                  <el-dropdown-item command="myAudits">
                    <el-icon><Checked /></el-icon>
                    待我审核
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isAdmin" divided command="admin">
                    <el-icon><Setting /></el-icon>
                    管理后台
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <!-- 未登录用户 -->
          <div class="guest-actions" v-else>
            <el-button @click="navigateTo('/login')" class="warm-btn login-btn">
              <el-icon><User /></el-icon>
              登录
            </el-button>
            <el-button type="primary" @click="navigateTo('/register')" class="warm-btn register-btn">
              加入我们
            </el-button>
          </div>
        </div>
      </div>
    </header>
    
    <!-- 主内容区域 -->
    <main class="main-content">
      <keep-alive>
        <router-view v-slot="{ Component }">
          <transition name="page-transition" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </keep-alive>
    </main>
    
    <!-- 浮动功能卡片 -->
    <div class="floating-features" v-if="isLoggedIn">
      <div class="feature-card publish-lost" @click="navigateTo('/lost/publish')">
        <div class="card-icon">
          <el-icon><Plus /></el-icon>
        </div>
        <div class="card-text">
          <span class="title">丢失了？</span>
          <span class="subtitle">发布失物信息</span>
        </div>
      </div>
      
      <div class="feature-card publish-found" @click="navigateTo('/found/publish')">
        <div class="card-icon">
          <el-icon><Upload /></el-icon>
        </div>
        <div class="card-text">
          <span class="title">捡到了？</span>
          <span class="subtitle">发布招领信息</span>
        </div>
      </div>
    </div>
    
    <!-- 移动端底部导航 -->
    <div class="mobile-bottom-nav" v-if="isMobile">
      <div class="bottom-nav-item" :class="{ active: activeMenu === '/' }" @click="navigateTo('/')">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="bottom-nav-item" :class="{ active: activeMenu === '/lost' }" @click="navigateTo('/lost')">
        <el-icon><Search /></el-icon>
        <span>失物</span>
      </div>
      <div class="bottom-nav-item" :class="{ active: activeMenu === '/found' }" @click="navigateTo('/found')">
        <el-icon><Collection /></el-icon>
        <span>招领</span>
      </div>
      <div class="bottom-nav-item" v-if="isLoggedIn" :class="{ active: activeMenu === '/notification' }" @click="navigateTo('/notification')">
        <el-icon><Bell /></el-icon>
        <span>通知</span>
      </div>
      <div class="bottom-nav-item" v-if="isLoggedIn" :class="{ active: activeMenu.includes('/profile') }" @click="navigateTo('/profile')">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
      <div class="bottom-nav-item" v-else @click="navigateTo('/login')">
        <el-icon><User /></el-icon>
        <span>登录</span>
      </div>
    </div>
    
    <!-- 简洁页脚 -->
    <footer class="warm-footer" v-if="!isMobile">
      <div class="footer-content">
        <p>&copy; {{ new Date().getFullYear() }} 校园失物招领系统 · 传递温暖，连接心灵</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'
import { 
  User, Plus, SwitchButton, Document, Checked,
  HomeFilled, Search, Collection, Setting, Upload, Bell
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式状态
const searchQuery = ref('')
const isMobile = ref(false)

// 计算属性
const activeMenu = computed(() => route.path)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const username = computed(() => userStore.userInfo?.username || '')
const userAvatar = computed(() => {
  if (!userStore.userInfo?.avatar) return ''
  const baseAPI = import.meta.env.VITE_BASE_API || '/api'
  return userStore.userInfo.avatar.startsWith('http') 
    ? userStore.userInfo.avatar 
    : baseAPI + userStore.userInfo.avatar
})
const userInitial = computed(() => {
  if (!username.value) return ''
  return username.value.charAt(0).toUpperCase()
})
const isAdmin = computed(() => userStore.userInfo?.roleCode === 'ADMIN')

// 方法
const navigateTo = (path) => {
  router.push(path)
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/lost',
      query: { search: searchQuery.value }
    })
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      navigateTo('/profile')
      break
    case 'myClaims':
      navigateTo('/claim')
      break
    case 'myAudits':
      navigateTo('/claim/audit')
      break
    case 'admin':
      navigateTo('/back')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  ElMessageBox.confirm(
    '确定要退出登录吗?',
    '温馨提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    userStore.logout()
    navigateTo('/login')
  }).catch(() => {})
}

// 检查屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
}

// 初始化
onMounted(() => {
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})
</script>

<style lang="scss">
// 全局CSS变量 - 温暖色调
:root {
  // 静谧蓝 - 主色调
  --warm-primary: #4A90E2;
  --warm-primary-light: #7BB3F0;
  --warm-primary-dark: #2E5B9A;
  
  // 暖阳黄 - 强调色
  --warm-accent: #FFB84D;
  --warm-accent-light: #FFD084;
  --warm-accent-dark: #E6A441;
  
  // 温馨色彩
  --warm-pink: #FF6B9D;
  --warm-pink-light: #FF9EC7;
  --warm-green: #4ECDC4;
  --warm-green-light: #7EE8E1;
  --warm-orange: #FFA726;
  --warm-purple: #AB47BC;
  
  // 中性色 - 温暖调整
  --warm-white: #FFFFFF;
  --warm-bg: #FEFEFE;
  --warm-light: #F8F9FA;
  --warm-gray-50: #F7F8FC;
  --warm-gray-100: #EEF2F6;
  --warm-gray-200: #E2E8F0;
  --warm-gray-300: #CBD5E0;
  --warm-gray-400: #A0AEC0;
  --warm-gray-500: #718096;
  --warm-gray-600: #4A5568;
  --warm-gray-700: #2D3748;
  --warm-gray-800: #1A202C;
  
  // 毛玻璃效果
  --glass-background: rgba(255, 255, 255, 0.8);
  --glass-backdrop: blur(20px);
  --glass-border: rgba(255, 255, 255, 0.18);
  
  // 阴影 - 柔和
  --warm-shadow-soft: 0 2px 12px rgba(0, 0, 0, 0.08);
  --warm-shadow-medium: 0 4px 20px rgba(0, 0, 0, 0.12);
  --warm-shadow-strong: 0 8px 32px rgba(0, 0, 0, 0.16);
  --warm-shadow-glow: 0 0 20px rgba(74, 144, 226, 0.3);
  
  // 圆角
  --warm-radius-sm: 8px;
  --warm-radius-md: 12px;
  --warm-radius-lg: 16px;
  --warm-radius-xl: 20px;
}
</style>

<style lang="scss" scoped>

// 字体设置
* {
  font-family: "Source Han Sans", "Noto Sans CJK SC", "PingFang SC", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.campus-lost-found {
  min-height: 100vh;
  background: var(--warm-bg);
  display: flex;
  flex-direction: column;
}

// 毛玻璃效果的顶部导航
.warm-header {
  background: var(--glass-background);
  backdrop-filter: var(--glass-backdrop);
  -webkit-backdrop-filter: var(--glass-backdrop);
  border-bottom: 1px solid var(--glass-border);
  box-shadow: var(--warm-shadow-soft);
  position: sticky;
  top: 0;
  z-index: 100;
  
  .header-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 16px 24px;
    display: flex;
    align-items: center;
    gap: 32px;
    
    .brand-section {
      display: flex;
      align-items: center;
      gap: 16px;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
      }
      
      .logo-wrapper {
        position: relative;
        
        .logo {
       
          border-radius: var(--warm-radius-md);
          object-fit: contain;
          transition: all 0.3s ease;
        }
        
        .logo-glow {
          position: absolute;
          top: -4px;
          left: -4px;
          right: -4px;
          bottom: -4px;
          background: var(--warm-gradient-main);
          border-radius: var(--warm-radius-lg);
          opacity: 0;
          transition: opacity 0.3s ease;
        }
        
        &:hover .logo-glow {
          opacity: 0.2;
        }
      }
      
      .brand-info {
        .brand-title {
          font-size: 24px;
          font-weight: 700;
          margin: 0;
          color: var(--warm-primary);
        }
        
      }
    }
    
    .feature-nav {
      display: flex;
      gap: 8px;
      flex: 1;
      
      .nav-card {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 20px;
        border-radius: var(--warm-radius-lg);
        cursor: pointer;
        transition: all 0.3s ease;
        font-weight: 500;
        color: var(--warm-gray-600);
        background: var(--warm-gray-50);
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: var(--warm-shadow-medium);
        }
        
        &.active {
          background: var(--warm-primary);
          color: white;
          box-shadow: var(--warm-shadow-glow);
        }
        
        &.lost-card:not(.active) {
          background: rgba(255, 107, 157, 0.1);
          
          &:hover {
            background: rgba(255, 107, 157, 0.2);
          }
        }
        
        &.found-card:not(.active) {
          background: rgba(78, 205, 196, 0.1);
          
          &:hover {
            background: rgba(78, 205, 196, 0.2);
          }
        }
        
        .nav-icon {
          font-size: 20px;
        }
      }
    }
    
    .user-section {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .search-area {
        .search-wrapper {
          width: 300px;
          
          .warm-search {
            :deep(.el-input__wrapper) {
              border-radius: var(--warm-radius-lg);
              border: 2px solid var(--warm-gray-200);
              box-shadow: none;
              background: var(--warm-gray-50);
              
              &:hover {
                border-color: var(--warm-primary-light);
              }
              
              &.is-focus {
                border-color: var(--warm-primary);
                background: white;
                box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.1);
              }
            }
          }
        }
      }
      
      .logged-user {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .user-welcome {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          
          .welcome-text {
            font-size: 12px;
            color: var(--warm-gray-500);
          }
          
          .user-name {
            font-weight: 600;
            color: var(--warm-gray-700);
          }
        }
        
        .notification-bubble {
          position: relative;
          cursor: pointer;
          
          .bell-icon {
            width: 40px;
            height: 40px;
            border-radius: var(--warm-radius-md);
            background: var(--warm-gray-50);
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
            
            &:hover {
              background: var(--warm-primary-light);
              color: white;
              transform: translateY(-1px);
            }
            
            .el-icon {
              font-size: 18px;
            }
          }
        }
        
        .user-dropdown {
          .avatar-wrapper {
            position: relative;
            cursor: pointer;
            
            .user-avatar {
              border: 3px solid var(--warm-gray-200);
              transition: all 0.3s ease;
              
              &:hover {
                border-color: var(--warm-primary);
                transform: translateY(-2px);
                box-shadow: var(--warm-shadow-medium);
              }
            }
            
            .online-dot {
              position: absolute;
              bottom: 2px;
              right: 2px;
              width: 12px;
              height: 12px;
              background: var(--warm-green);
              border: 2px solid white;
              border-radius: 50%;
              animation: pulse 2s infinite;
            }
          }
        }
      }
      
      .guest-actions {
        display: flex;
        gap: 12px;
        
        .warm-btn {
          border-radius: var(--warm-radius-lg);
          font-weight: 500;
          padding: 12px 24px;
          transition: all 0.3s ease;
          
          &.login-btn {
            border: 2px solid var(--warm-gray-300);
            color: var(--warm-gray-600);
            background: transparent;
            
            &:hover {
              border-color: var(--warm-primary);
              color: var(--warm-primary);
              transform: translateY(-1px);
            }
          }
          
          &.register-btn {
            background: var(--warm-primary);
            border: none;
            
            &:hover {
              transform: translateY(-2px);
              box-shadow: var(--warm-shadow-glow);
            }
          }
        }
      }
    }
  }
}

// 主内容区
.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 24px;
}

// 浮动功能卡片
.floating-features {
  position: fixed;
  right: 24px;
  bottom: 120px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  z-index: 50;
  
  .feature-card {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    background: white;
    border-radius: var(--warm-radius-xl);
    box-shadow: var(--warm-shadow-strong);
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateX(-8px) translateY(-2px);
    }
    
    &.publish-lost {
      background: var(--warm-pink);
      color: white;
    }
    
    &.publish-found {
      background: var(--warm-green);
      color: white;
    }
    
    .card-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--warm-radius-md);
      background: rgba(255, 255, 255, 0.2);
      display: flex;
      align-items: center;
      justify-content: center;
      
      .el-icon {
        font-size: 24px;
      }
    }
    
    .card-text {
      display: flex;
      flex-direction: column;
      
      .title {
        font-weight: 700;
        font-size: 16px;
      }
      
      .subtitle {
        font-size: 12px;
        opacity: 0.8;
      }
    }
  }
}

// 移动端底部导航
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid var(--warm-gray-200);
  display: flex;
  justify-content: space-around;
  padding: 8px 0 12px;
  z-index: 100;
  
  .bottom-nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 8px 12px;
    border-radius: var(--warm-radius-md);
    cursor: pointer;
    transition: all 0.3s ease;
    color: var(--warm-gray-500);
    font-size: 12px;
    font-weight: 500;
    
    &.active {
      color: var(--warm-primary);
      background: rgba(74, 144, 226, 0.1);
    }
    
    .el-icon {
      font-size: 24px;
    }
  }
}

// 简洁页脚
.warm-footer {
  background: var(--warm-gray-800);
  color: white;
  margin-top: auto;
  
  .footer-content {
    max-width: 1400px;
    margin: 0 auto;
    padding: 32px 24px;
    text-align: center;
    
    p {
      color: var(--warm-gray-400);
      font-size: 14px;
      margin: 0;
      line-height: 1.6;
    }
  }
}

// 页面过渡动画
.page-transition-enter-active,
.page-transition-leave-active {
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}

.page-transition-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.page-transition-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

// 脉冲动画
@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}

// 下拉菜单样式
:deep(.warm-dropdown) {
  border-radius: var(--warm-radius-lg);
  border: 1px solid var(--warm-gray-200);
  box-shadow: var(--warm-shadow-strong);
  
  .dropdown-header {
    padding: 16px 20px;
    border-bottom: 1px solid var(--warm-gray-200);
    background: var(--warm-gray-50);
    
    .user-info {
      strong {
        display: block;
        color: var(--warm-gray-800);
        margin-bottom: 4px;
      }
      
      .role-tag {
        font-size: 12px;
        color: var(--warm-gray-500);
        background: var(--warm-gray-200);
        padding: 2px 8px;
        border-radius: var(--warm-radius-sm);
      }
    }
  }
  
  .el-dropdown-menu__item {
    padding: 12px 20px;
    border-radius: var(--warm-radius-sm);
    margin: 4px 8px;
    
    &:hover {
      background: rgba(74, 144, 226, 0.1);
      color: var(--warm-primary);
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .warm-header {
    .header-container {
      padding: 12px 16px;
      gap: 16px;
      
      .brand-section {
        flex-shrink: 0;
        min-width: 0;
        
        .brand-info {
          .brand-title {
            font-size: 18px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
      }
      
      .feature-nav {
        display: none;
      }
      
      .user-section {
        .search-area {
          display: none;
        }
        
        .logged-user {
          .user-welcome {
            display: none;
          }
        }
      }
    }
  }
  
  .main-content {
    padding: 16px;
    margin-bottom: 80px;
  }
  
  .floating-features {
    display: none;
  }
  
  .warm-footer {
    .footer-content {
      padding: 24px 16px;
    }
  }
}

@media (max-width: 480px) {
  .warm-header {
    .header-container {
      .brand-section {
        .logo-wrapper .logo {
          width: 36px;
          height: 36px;
        }
        
        .brand-info .brand-title {
          font-size: 16px;
        }
      }
      
      .user-section {
        .guest-actions {
          gap: 8px;
          
          .warm-btn {
            padding: 8px 16px;
            font-size: 14px;
          }
        }
      }
    }
  }
}
</style>
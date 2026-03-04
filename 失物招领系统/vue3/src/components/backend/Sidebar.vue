<template>
  <div class="sidebar-container" :class="{ 'is-collapsed': isCollapsed }">
    <div class="logo">
      <img src="@/assets/logo.svg" alt="校园失物招领" class="logo-svg" v-show="!isCollapsed" />
      <span class="logo-icon" v-show="isCollapsed">🔍</span>
    </div>
    <div class="menu-wrapper">
      <el-menu :default-active="activeMenu" :collapse="isCollapsed" :collapse-transition="false" mode="vertical" class="sidebar-menu"
        text-color="#bfcbd9" active-text-color="#409EFF" router>
        
        <!-- 固定菜单项 -->
        <el-menu-item index="/back/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-menu-item index="/back/category">
          <el-icon><Folder /></el-icon>
          <template #title>物品分类管理</template>
        </el-menu-item>
        
        <el-menu-item index="/back/lost">
          <el-icon><Search /></el-icon>
          <template #title>失物管理</template>
        </el-menu-item>
        
        <el-menu-item index="/back/found">
          <el-icon><Collection /></el-icon>
          <template #title>招领管理</template>
        </el-menu-item>
        
        <el-menu-item index="/back/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <el-menu-item index="/back/profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>个人信息</template>
        </el-menu-item>
        
        <el-menu-item index="/back/claim">
          <el-icon><DocumentChecked /></el-icon>
          <template #title>认领申请管理</template>
        </el-menu-item>
        
      </el-menu>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { 
  HomeFilled, 
  User, 
  UserFilled,
  Folder,
  Search,
  Collection,
  DocumentChecked
} from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)

// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%; 
  min-height: 100vh;
  background: linear-gradient(180deg, #1c1c1c 0%, #2d2d2d 100%);
  display: flex;
  flex-direction: column;
  width: 220px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  
  &.is-collapsed {
    width: 64px;
    
    .logo {
      padding: 0;
      justify-content: center;
      
      .logo-icon {
        margin: 0;
      }
    }

    :deep(.el-menu) {
      .el-sub-menu__title span,
      .el-menu-item span {
        opacity: 0;
        transition: opacity 0.2s;
      }
    }
  }
  
  .logo {
    height: 60px;
    flex-shrink: 0;
    line-height: 60px;
    text-align: center;
    background: rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    overflow: hidden;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    
    .logo-svg {
      height: 40px;
      width: auto;
      max-width: 180px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .logo-icon {
      font-size: 24px;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    
    .logo-text {
      color: #ffffff;
      font-size: 18px;
      font-weight: 600;
      white-space: nowrap;
      opacity: 1;
      transition: opacity 0.2s;
    }
  }

  .menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 3px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }
  }

  :deep(.sidebar-menu) {
    border: none;
    background: transparent;
    transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    .el-menu-item, .el-sub-menu__title {
      height: 50px;
      line-height: 50px;
      color: rgba(255, 255, 255, 0.7);
      background: transparent;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      span {
        opacity: 1;
        transition: opacity 0.3s;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.05) !important;
        color: #ffffff;
      }
    }

    .el-menu-item.is-active {
      background: #000000 !important;
      color: #ffffff !important;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        width: 3px;
        height: 100%;
        background: #1890ff;
      }
    }

    .el-sub-menu {
      &.is-opened {
        > .el-sub-menu__title {
          color: #ffffff;
          background: rgba(0, 0, 0, 0.2) !important;
        }
      }

      .el-menu {
        background: rgba(0, 0, 0, 0.3);
        
        .el-menu-item {
          background: transparent;
          
          &:hover {
            background: rgba(255, 255, 255, 0.05) !important;
          }
          
          &.is-active {
            background: #000000 !important;
          }
        }
      }
    }

    // 折叠状态下的弹出菜单样式
    &.el-menu--collapse {
      .el-sub-menu {
        &.is-opened {
          > .el-sub-menu__title {
            background: transparent !important;
          }
        }
      }
    }
  }

  .el-icon {
    vertical-align: middle;
    margin-right: 5px;
    width: 24px;
    text-align: center;
    color: inherit;
  }

  span {
    vertical-align: middle;
  }
}
</style> 
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import BackendLayout from '@/layouts/BackendLayout.vue'
import FrontendLayout from '../layouts/FrontendLayout.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import Dashboard from '../views/backend/Dashboard.vue'

// 后台路由
export const backendRoutes = [
  {
    path: '/back',
    component: BackendLayout,
    redirect: '/back/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '控制台', icon: 'HomeFilled' }
      },
      {
        path: 'user',
        name: 'UserManagement',
        component: () => import('@/views/backend/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'profile',
        name: 'BackendProfile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人中心', icon: 'UserFilled' }
      },
      {
        path: 'category',
        name: 'CategoryManagement',
        component: () => import('../views/backend/category/index.vue'),
        meta: { title: '物品分类管理', icon: 'Management' }
      },
      {
        path: 'lost',
        name: 'LostItemManagement',
        component: () => import('../views/backend/lost/index.vue'),
        meta: { title: '失物管理', icon: 'List' }
      },
      {
        path: 'found',
        name: 'FoundItemManagement',
        component: () => import('../views/backend/found/index.vue'),
        meta: { title: '招领管理', icon: 'Collection' }
      },
      {
        path: 'claim',
        name: 'ClaimManagement',
        component: () => import('../views/backend/claim/index.vue'),
        meta: { title: '认领申请管理', icon: 'Checked' }
      }
    ]
  }
]

// 前台路由
const frontendRoutes = [
  {
    path: '/',
    component: FrontendLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('../views/frontend/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('../views/frontend/notification/index.vue'),
        meta: { title: '我的通知', requiresAuth: true }
      },
      {
        path: 'lost',
        name: 'LostItemList',
        component: () => import('../views/frontend/lost/index.vue'),
        meta: { title: '失物信息' }
      },
      {
        path: 'lost/detail/:id',
        name: 'LostItemDetail',
        component: () => import('../views/frontend/lost/detail.vue'),
        meta: { title: '失物详情' }
      },
      {
        path: 'lost/publish',
        name: 'LostItemPublish',
        component: () => import('../views/frontend/lost/publish.vue'),
        meta: { title: '发布失物信息', requiresAuth: true }
      },
      {
        path: 'lost/edit/:id',
        name: 'LostItemEdit',
        component: () => import('../views/frontend/lost/edit.vue'),
        meta: { title: '编辑失物信息', requiresAuth: true }
      },
      {
        path: 'found',
        name: 'FoundItemList',
        component: () => import('../views/frontend/found/index.vue'),
        meta: { title: '招领信息' }
      },
      {
        path: 'found/detail/:id',
        name: 'FoundItemDetail',
        component: () => import('../views/frontend/found/detail.vue'),
        meta: { title: '招领详情' }
      },
      {
        path: 'found/publish',
        name: 'FoundItemPublish',
        component: () => import('../views/frontend/found/publish.vue'),
        meta: { title: '发布招领信息', requiresAuth: true }
      },
      {
        path: 'found/edit/:id',
        name: 'FoundItemEdit',
        component: () => import('../views/frontend/found/edit.vue'),
        meta: { title: '编辑招领信息', requiresAuth: true }
      },
      {
        path: 'claim',
        name: 'MyClaims',
        component: () => import('../views/frontend/claim/index.vue'),
        meta: { title: '我的申请', requiresAuth: true }
      },
      {
        path: 'claim/audit',
        name: 'MyClaimsAudit',
        component: () => import('../views/frontend/claim/audit.vue'),
        meta: { title: '待我审核', requiresAuth: true }
      },
  
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { title: '注册' }
  }
]

// 错误页面路由
const errorRoutes = [
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { title: '404 Not Found' }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

// 路由配置
const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...frontendRoutes,
    ...backendRoutes,
    ...errorRoutes
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 失物招领系统`
  }

  const userStore = useUserStore()
  console.log("Current route:", to.path)
  console.log("User status:", {
    isLoggedIn: userStore.isLoggedIn,
    isUser: userStore.isUser
  })

  // 检查是否需要登录权限
  if (to.matched.some(record => record.meta.requiresAuth) && !userStore.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // 已登录用户的路由控制
  if (userStore.isLoggedIn) {
    // 处理登录页面访问
    if (to.path === '/login') {
      next(userStore.isUser ? '/' : '/back/dashboard')
      return
    }

    if (!userStore.isUser) {
      // 非普通用户只能访问后台路由
      if (to.path.startsWith('/back')) {
        next()
      } else {
        next('/back/dashboard')
      }
      return
    } else {
      // 普通用户只能访问前台路由
      if (to.path.startsWith('/back')) {
        next('/')
      } else {
        next()
      }
      return
    }
  }

  next()
})

export default router

<template>
  <div class="auth-container">
    <div class="auth-box" :class="{ 'shake': shakeAnimation }">
      <div class="auth-header">
        <div class="logo">LF</div>
        <h1 class="title">校园失物招领</h1>
        <div class="subtitle">CAMPUS LOST & FOUND</div>
        <div class="welcome-text">让每一份遗失都能重新找到归宿</div>
        <div class="slogan">连接校园，传递温暖</div>
      </div>

      <el-form
        :model="loginForm"
        :rules="rules"
        ref="formRef"
        class="auth-form"
        @keyup.enter="handleSubmit"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名或邮箱"
            size="large"
            clearable
            @keyup.enter="handleQuickSubmit">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleQuickSubmit">
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 记住我选项 -->
        <div class="remember-me-container">
          <el-checkbox v-model="rememberMe" size="small">
            记住我
          </el-checkbox>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
            class="auth-button"
          >
            <span v-if="!loading">登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>

        <div class="auth-links">
          <!-- <router-link to="/forget" class="forget-link">忘记密码？</router-link> -->
          <router-link to="/register">还没有账号？立即注册</router-link>
        </div>
      </el-form>

      <!-- 成功提示 -->
      <div v-if="showSuccess" class="success-overlay">
        <div class="success-icon">✓</div>
        <div class="success-text">登录成功！正在跳转...</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)
const shakeAnimation = ref(false)
const showSuccess = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' },
    { min: 3, message: '用户名至少3个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ]
}

// 页面加载时恢复记住的登录信息
onMounted(() => {
  // 先检查是否有记住我的设置
  const remembered = localStorage.getItem('rememberMe') === 'true'
  if (remembered) {
    rememberMe.value = true
    // 恢复用户名
    const savedUsername = localStorage.getItem('rememberedUsername')
    if (savedUsername) {
      loginForm.username = savedUsername
    }
    console.log('已恢复记住的用户名:', savedUsername)
  }
})

const handleSubmit = () => {
  formRef.value.validate(valid => {
    if (valid) {
      handleLogin()
    } else {
      // 表单验证失败，触发震动动画
      triggerShake()
    }
  })
}

const handleQuickSubmit = () => {
  handleSubmit()
}

const triggerShake = () => {
  shakeAnimation.value = true
  setTimeout(() => {
    shakeAnimation.value = false
  }, 600)
}

const showSuccessAnimation = () => {
  showSuccess.value = true
  setTimeout(() => {
    showSuccess.value = false
  }, 2000)
}

const handleLogin = async () => {
  loading.value = true
  try {
    // 保存记住我的设置
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
      localStorage.setItem('rememberedUsername', loginForm.username)
      console.log('已保存记住我设置和用户名:', loginForm.username)
    } else {
      localStorage.removeItem('rememberMe')
      localStorage.removeItem('rememberedUsername')
      console.log('已清除记住我设置')
    }

    // 统一使用用户登录接口
    await request.post("/user/login", loginForm, {
      successMsg: "登录成功",
      showDefaultMsg: true,
      onSuccess: async (data) => {
        userStore.setUserInfo(data)

        // 显示成功动画
        showSuccessAnimation()

        // 延迟跳转以显示动画
        setTimeout(async () => {
          // 根据返回的角色决定跳转路径
          if (data.roleCode !== 'USER') {
            // // 管理员登录，设置菜单
            // userStore.setMenus(data.menuList)
            // 直接导航到后台仪表盘
            await router.isReady()
            router.push(route.query.redirect || '/back/dashboard')
          } else {
            // 普通用户登录，直接跳转到前台
            const redirect = route.query.redirect || '/'
            router.push(redirect)
          }
        }, 1500)
      },
      onError: (error) => {
        console.error('登录失败:', error)
        triggerShake()
      }
    })
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
// 校园失物招领系统 - 认证页面样式 (扁平化设计)

// CSS 变量定义 - 按照UI设计规范
:root {
  // 静谧蓝色系
  --campus-primary: #4A90E2;        // 静谧蓝主色
  --campus-primary-light: #7BB3F0;  // 静谧蓝浅色
  --campus-primary-dark: #2E5B9A;   // 静谧蓝深色
  
  // 暖阳黄色系
  --campus-secondary: #FFB84D;      // 暖阳黄主色
  --campus-secondary-light: #FFD084; // 暖阳黄浅色
  --campus-secondary-dark: #E6A441;  // 暖阳黄深色
  
  // 中性灰色系
  --campus-neutral-50: #F8F9FA;     // 极浅灰
  --campus-neutral-100: #F1F3F4;    // 浅灰
  --campus-neutral-200: #E8EAED;    // 浅中灰
  --campus-neutral-300: #DADCE0;    // 中灰
  --campus-neutral-400: #BDC1C6;    // 深中灰
  --campus-neutral-500: #9AA0A6;    // 深灰
  --campus-neutral-600: #80868B;    // 较深灰
  --campus-neutral-700: #5F6368;    // 深灰
  --campus-neutral-800: #3C4043;    // 很深灰
  --campus-neutral-900: #202124;    // 最深灰
  
  // 功能色
  --campus-success: #34A853;        // 成功绿
  --campus-warning: #FBBC04;        // 警告黄
  --campus-danger: #EA4335;         // 错误红
  --campus-info: #4285F4;          // 信息蓝
  
  // 文本色
  --campus-text-primary: var(--campus-neutral-900);
  --campus-text-secondary: var(--campus-neutral-700);
  --campus-text-tertiary: var(--campus-neutral-500);
  --campus-text-white: #FFFFFF;
  
  // 背景色
  --campus-bg-primary: var(--campus-neutral-50);
  --campus-bg-secondary: var(--campus-neutral-100);
  --campus-bg-card: #FFFFFF;
  
  // 边框和阴影
  --campus-border: var(--campus-neutral-200);
  --campus-border-light: var(--campus-neutral-100);
  --campus-shadow: 0 2px 8px rgba(60, 64, 67, 0.1);
  --campus-shadow-lg: 0 4px 16px rgba(60, 64, 67, 0.15);
  --campus-shadow-xl: 0 8px 32px rgba(60, 64, 67, 0.2);
}

.auth-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  font-family: "Source Han Sans", "Noto Sans CJK SC", "PingFang SC", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background: linear-gradient(135deg, 
    var(--campus-primary) 0%, 
    var(--campus-primary-light) 50%,
    var(--campus-secondary-light) 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
  animation: fadeIn 0.8s ease-out;

  // 微妙的背景纹理
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 80%, rgba(255, 184, 77, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 40% 60%, rgba(74, 144, 226, 0.05) 0%, transparent 50%);
    animation: gentleFloat 20s ease-in-out infinite;
  }
}

.auth-box {
  background: var(--campus-bg-card);
  border-radius: 12px;  // 轻微圆角
  padding: 48px 40px;
  width: 100%;
  max-width: 480px;
  box-shadow: var(--campus-shadow-xl);
  border: 1px solid var(--campus-border-light);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  animation: slideUp 0.6s ease-out;

  &:hover {
    box-shadow: var(--campus-shadow-xl);
    transform: translateY(-2px);
  }

  @media (max-width: 640px) {
    padding: 32px 24px;
    margin: 16px;
    border-radius: 8px;  // 轻微圆角
  }
}

.auth-header {
  text-align: center;
  margin-bottom: 40px;
  animation: fadeInDown 0.8s ease-out;

  .logo {
    width: 72px;
    height: 72px;
    background: linear-gradient(135deg, 
      var(--campus-primary) 0%, 
      var(--campus-secondary) 100%);
    border-radius: 16px;  // 轻微圆角
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    font-weight: 700;
    color: var(--campus-text-white);
    margin: 0 auto 24px;
    border: none;
    box-shadow: var(--campus-shadow);
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.05);
      box-shadow: var(--campus-shadow-lg);
    }
  }

  .title {
    font-size: 26px;
    font-weight: 700;
    color: var(--campus-text-primary);
    margin: 0 0 8px 0;
    letter-spacing: -0.3px;
    font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
  }

  .subtitle {
    font-size: 13px;
    color: var(--campus-text-secondary);
    font-weight: 500;
    letter-spacing: 1.5px;
    margin-bottom: 16px;
    text-transform: uppercase;
  }

  .welcome-text {
    font-size: 14px;
    color: var(--campus-text-secondary);
    margin-top: 12px;
    font-weight: 400;
    line-height: 1.5;
  }

  .slogan {
    font-size: 12px;
    color: var(--campus-text-tertiary);
    margin-top: 8px;
    font-weight: 500;
    letter-spacing: 0.5px;
  }
}

.auth-form {
  animation: fadeInUp 0.6s ease-out 0.2s both;

  .el-form-item {
    margin-bottom: 24px;
    transition: all 0.3s ease;

    &:last-of-type {
      margin-bottom: 0;
    }

    // 输入框样式优化
    :deep(.el-input__wrapper) {
      border-radius: 8px !important;  // 轻微圆角
      border: 1px solid var(--campus-border) !important;
      box-shadow: none !important;
      transition: all 0.3s ease !important;

      &:hover {
        border-color: var(--campus-primary-light) !important;
      }

      &.is-focus {
        border-color: var(--campus-primary) !important;
        box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.1) !important;
      }
    }

    // 图标颜色
    :deep(.el-input__prefix-inner),
    :deep(.el-input__suffix-inner) {
      .el-icon {
        color: var(--campus-primary) !important;
        transition: color 0.3s ease !important;
      }
    }
  }

  .auth-button {
    width: 100%;
    height: 48px;
    border-radius: 8px;  // 轻微圆角
    font-size: 16px;
    font-weight: 600;
    font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
    background: linear-gradient(135deg, 
      var(--campus-primary) 0%, 
      var(--campus-primary-light) 100%);
    border: none;
    color: var(--campus-text-white);
    transition: all 0.3s ease;
    box-shadow: var(--campus-shadow);

    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--campus-shadow-lg);
      background: linear-gradient(135deg, 
        var(--campus-primary-dark) 0%, 
        var(--campus-primary) 100%);
    }

    &:active {
      transform: translateY(0);
      transition: transform 0.1s ease;
    }

    &:focus {
      outline: none;
      box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.2);
    }
  }
}

.auth-links {
  text-align: center;
  margin-top: 24px;
  animation: fadeIn 0.8s ease-out 0.4s both;

  a {
    color: var(--campus-primary);
    text-decoration: none;
    font-weight: 500;
    font-size: 14px;
    font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
    transition: all 0.3s ease;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      width: 0;
      height: 2px;
      bottom: -2px;
      left: 50%;
      background: var(--campus-secondary);
      transition: all 0.3s ease;
      transform: translateX(-50%);
    }

    &:hover {
      color: var(--campus-primary-dark);
      
      &::after {
        width: 100%;
      }
    }
  }
}

/* 登录页面特有样式 */
.remember-me-container {
  margin-bottom: 20px;
  position: relative;
  z-index: 1000;
  color: black;
  animation: fadeInUp 0.6s ease-out 0.3s both;

  :deep(.el-checkbox) {
    .el-checkbox__label {
      font-size: 14px;
      color: var(--campus-text-secondary);
      font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
    }

    .el-checkbox__inner {
      border-radius: 4px;  // 轻微圆角
      border-color: var(--campus-border);
      
      &:hover {
        border-color: var(--campus-primary);
      }
    }

    &.is-checked .el-checkbox__inner {
      background-color: var(--campus-primary);
      border-color: var(--campus-primary);
        &::after {
        border-color: #252424 !important;
      }
    }
    
  }
}

// 震动效果用于表单验证反馈
.shake {
  animation: shake 0.4s ease-in-out;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4px); }
  75% { transform: translateX(4px); }
}

// 成功覆盖层 - 符合UI规范
.success-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--campus-success) 0%, #28a745 100%);
  border-radius: 12px;  // 轻微圆角
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 100;
  animation: successFadeIn 0.5s ease-out;

  .success-icon {
    font-size: 56px;
    color: var(--campus-text-white);
    margin-bottom: 16px;
    animation: successBounce 0.6s ease-out 0.2s both;
  }

  .success-text {
    color: var(--campus-text-white);
    font-size: 16px;
    font-weight: 500;
    font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
    animation: fadeInUp 0.5s ease-out 0.4s both;
  }
}

// 动画定义 - 平滑的淡入淡出过渡
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes successFadeIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes successBounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes gentleFloat {
  0%, 100% { 
    transform: translate(0, 0) rotate(0deg);
    opacity: 1;
  }
  33% { 
    transform: translate(10px, -10px) rotate(1deg);
    opacity: 0.8;
  }
  66% { 
    transform: translate(-5px, 5px) rotate(-1deg);
    opacity: 0.9;
  }
}
</style>
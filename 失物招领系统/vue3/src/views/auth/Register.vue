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
        :model="registerForm"
        :rules="rules"
        ref="formRef"
        class="auth-form"
        @keyup.enter="handleSubmit"
      >
        <div class="form-row">
          <el-form-item prop="username" class="form-item-half">
            <el-input
              v-model="registerForm.username"
              :prefix-icon="User"
              placeholder="用户名"
              size="large"
              clearable>
            </el-input>
          </el-form-item>
          <el-form-item prop="name" class="form-item-half">
            <el-input
              v-model="registerForm.name"
              :prefix-icon="UserFilled"
              placeholder="真实姓名"
              size="large"
              clearable>
            </el-input>
          </el-form-item>
        </div>

        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            :prefix-icon="Message"
            placeholder="邮箱地址"
            size="large"
            clearable>
          </el-input>
        </el-form-item>

        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            :prefix-icon="Phone"
            placeholder="手机号码（选填）"
            size="large"
            clearable>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            :prefix-icon="Lock"
            type="password"
            placeholder="设置密码"
            size="large"
            show-password>
          </el-input>
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            :prefix-icon="Lock"
            type="password"
            placeholder="确认密码"
            size="large"
            show-password>
          </el-input>
        </el-form-item>

        <!-- 用户协议 -->
        <div class="agreement-container">
          <el-checkbox v-model="registerForm.agreement" size="small">
            我已阅读并同意
            <a href="#" @click.prevent="showAgreement" class="agreement-link">《用户协议》</a>
            和
            <a href="#" @click.prevent="showPrivacy" class="agreement-link">《隐私政策》</a>
          </el-checkbox>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
            class="auth-button"
          >
            <span v-if="!loading">注册</span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>

        <div class="auth-links">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>

      <!-- 成功提示 -->
      <div v-if="showSuccess" class="success-overlay">
        <div class="success-icon">✓</div>
        <div class="success-text">注册成功！正在跳转到登录页...</div>
      </div>
    </div>

    <!-- 用户协议弹窗 -->
    <el-dialog
      v-model="agreementVisible"
      title="用户协议"
      width="600px"
      :before-close="handleCloseAgreement"
    >
      <div class="agreement-content">
        <h3>校园失物招领系统用户协议</h3>
        <p>欢迎使用校园失物招领系统！请仔细阅读以下条款：</p>
        <ol>
          <li>用户应提供真实、准确的个人信息</li>
          <li>禁止发布虚假的失物招领信息</li>
          <li>用户应妥善保管账号密码</li>
          <li>系统仅作为信息发布平台，不承担物品保管责任</li>
          <li>用户应遵守校园相关规定</li>
        </ol>
      </div>
      <template #footer>
        <el-button @click="agreementVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message, Phone, UserFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const agreementVisible = ref(false)
const shakeAnimation = ref(false)
const showSuccess = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  name: '',
  agreement: false,
  roleCode: 'USER', // 默认注册为普通用户
})

const validatePass2 = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const validateAgreement = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度必须在3到50个字符之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度必须在2到20个字符之间', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { validator: validateEmail, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度必须在6到100个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validatePass2, trigger: 'blur' }
  ],
  agreement: [
    { validator: validateAgreement, trigger: 'change' }
  ]
}

const handleSubmit = () => {
  formRef.value.validate(valid => {
    if (valid) {
      handleRegister()
    } else {
      // 表单验证失败，触发震动动画
      triggerShake()
    }
  })
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

const handleRegister = async () => {
  loading.value = true
  try {
    const { confirmPassword, agreement, ...registerData } = registerForm
    await request.post("/user/add", registerData, {
      successMsg: "注册成功",
      showDefaultMsg: true,
      onSuccess: () => {
        // 显示成功动画
        showSuccessAnimation()

        // 延迟跳转
        setTimeout(() => {
          router.push('/login')
        }, 2000)
      },
      onError: (error) => {
        console.error('注册失败:', error)
        triggerShake()
      }
    })
  } finally {
    loading.value = false
  }
}

const showAgreement = () => {
  agreementVisible.value = true
}

const showPrivacy = () => {
  // 可以显示隐私政策弹窗
  agreementVisible.value = true
}

const handleCloseAgreement = () => {
  agreementVisible.value = false
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
  max-width: 520px;  // 注册页面稍宽一些
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
  margin-bottom: 32px;
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
    margin: 0 auto 20px;
    border: none;
    box-shadow: var(--campus-shadow);
    transition: all 0.3s ease;

    &:hover {
      transform: scale(1.05);
      box-shadow: var(--campus-shadow-lg);
    }
  }

  .title {
    font-size: 24px;
    font-weight: 700;
    color: var(--campus-text-primary);
    margin: 0 0 6px 0;
    letter-spacing: -0.3px;
    font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
  }

  .subtitle {
    font-size: 12px;
    color: var(--campus-text-secondary);
    font-weight: 500;
    letter-spacing: 1.5px;
    margin-bottom: 12px;
    text-transform: uppercase;
  }

  .welcome-text {
    font-size: 13px;
    color: var(--campus-text-secondary);
    margin-top: 8px;
    font-weight: 400;
    line-height: 1.5;
  }

  .slogan {
    font-size: 11px;
    color: var(--campus-text-tertiary);
    margin-top: 6px;
    font-weight: 500;
    letter-spacing: 0.5px;
  }
}

.auth-form {
  animation: fadeInUp 0.6s ease-out 0.2s both;

  .el-form-item {
    margin-bottom: 20px;
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
  margin-top: 20px;
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

/* 注册页面特有样式 */
// 表单行布局
.form-row {
  display: flex;
  gap: 16px;
  animation: fadeInUp 0.6s ease-out 0.25s both;

  .form-item-half {
    flex: 1;
    margin-bottom: 20px !important;
  }
}

// 协议复选框样式
.agreement-container {
  margin-bottom: 24px;
  position: relative;
  z-index: 1000;
  animation: fadeInUp 0.6s ease-out 0.35s both;

  :deep(.el-checkbox) {
    .el-checkbox__label {
      font-size: 13px;
      color: var(--campus-text-secondary);
      font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
      line-height: 1.4;
    }

    .el-checkbox__inner {
      border-radius: 4px;  // 轻微圆角
      border-color: var(--campus-border);
      transition: all 0.3s ease;
      
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

  .agreement-link {
    color: var(--campus-primary);
    text-decoration: none;
    font-weight: 500;
    transition: all 0.3s ease;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      width: 0;
      height: 1px;
      bottom: -1px;
      left: 0;
      background: var(--campus-secondary);
      transition: width 0.3s ease;
    }

    &:hover {
      color: var(--campus-primary-dark);
      
      &::after {
        width: 100%;
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

// 用户协议弹窗样式
:deep(.el-dialog) {
  border-radius: 12px;  // 轻微圆角
  overflow: hidden;
  box-shadow: var(--campus-shadow-xl);
  border: 1px solid var(--campus-border-light);

  .el-dialog__header {
    background: linear-gradient(135deg,
      var(--campus-primary) 0%,
      var(--campus-primary-light) 100%);
    color: white;
    padding: 20px 24px;

    .el-dialog__title {
      font-weight: 600;
      font-size: 17px;
      font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
    }
  }

  .el-dialog__body {
    padding: 24px;
    background: var(--campus-bg-card);

    .agreement-content {
      h3 {
        color: var(--campus-text-primary);
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 600;
        font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
      }

      p {
        color: var(--campus-text-secondary);
        margin-bottom: 16px;
        line-height: 1.6;
        font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
      }

      ol {
        color: var(--campus-text-secondary);
        padding-left: 20px;
        font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;

        li {
          margin-bottom: 10px;
          line-height: 1.6;
          padding-left: 8px;
        }
      }
    }
  }

  .el-dialog__footer {
    padding: 16px 24px;
    background: var(--campus-bg-secondary);
    border-top: 1px solid var(--campus-border-light);

    .el-button {
      border-radius: 8px;  // 轻微圆角
      font-family: "Source Han Sans", "Noto Sans CJK SC", sans-serif;
      font-weight: 500;
      transition: all 0.3s ease;

      &--default {
        border-color: var(--campus-border);
        color: var(--campus-text-secondary);

        &:hover {
          border-color: var(--campus-primary);
          color: var(--campus-primary);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 0;

    .form-item-half {
      margin-bottom: 24px !important;
    }
  }
}
</style>
<template>
  <div class="ui-showcase">
    <div class="showcase-container">
      <h1 class="showcase-title">校园失物招领系统 UI 升级展示</h1>
      
      <!-- 认证页面预览 -->
      <div class="showcase-section">
        <h2>认证页面效果</h2>
        <div class="auth-preview">
          <div class="auth-box glass-card">
            <div class="auth-header">
              <div class="logo-container">
                <div class="logo">🎓</div>
              </div>
              <h1 class="title">校园失物招领</h1>
              <div class="subtitle">CAMPUS LOST & FOUND</div>
              <div class="welcome-text">让每一份遗失都能重新找到归宿</div>
            </div>
            
            <div class="form-demo">
              <el-input 
                placeholder="请输入用户名或邮箱" 
                size="large"
                class="demo-input">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
              
              <el-input 
                placeholder="请输入密码" 
                type="password"
                size="large"
                class="demo-input">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
              
              <el-button type="primary" size="large" class="demo-button">
                登录
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 组件展示 -->
      <div class="showcase-section">
        <h2>组件样式展示</h2>
        <div class="components-grid">
          <div class="component-item glass-card">
            <h3>按钮组件</h3>
            <div class="button-group">
              <el-button type="primary">主要按钮</el-button>
              <el-button>默认按钮</el-button>
              <el-button type="success">成功按钮</el-button>
            </div>
          </div>
          
          <div class="component-item glass-card">
            <h3>表单组件</h3>
            <el-form>
              <el-form-item label="用户名">
                <el-input placeholder="请输入用户名" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input placeholder="请输入邮箱" />
              </el-form-item>
            </el-form>
          </div>
          
          <div class="component-item glass-card">
            <h3>卡片组件</h3>
            <el-card>
              <p>这是一个使用校园主题的卡片组件，具有磨砂玻璃效果和柔和的阴影。</p>
            </el-card>
          </div>
        </div>
      </div>
      
      <!-- 色彩系统展示 -->
      <div class="showcase-section">
        <h2>色彩系统</h2>
        <div class="color-palette">
          <div class="color-item">
            <div class="color-swatch primary"></div>
            <span>主色调</span>
          </div>
          <div class="color-item">
            <div class="color-swatch secondary"></div>
            <span>辅助色</span>
          </div>
          <div class="color-item">
            <div class="color-swatch accent"></div>
            <span>强调色</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { User, Lock } from '@element-plus/icons-vue'
</script>

<style lang="scss" scoped>
.ui-showcase {
  min-height: 100vh;
  background: linear-gradient(135deg,
    var(--campus-primary) 0%,
    var(--campus-secondary) 35%,
    var(--campus-accent) 70%,
    var(--campus-primary-light) 100%);
  padding: 40px 20px;
}

.showcase-container {
  max-width: 1200px;
  margin: 0 auto;
}

.showcase-title {
  text-align: center;
  font-size: 36px;
  font-weight: 800;
  color: white;
  margin-bottom: 48px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.showcase-section {
  margin-bottom: 48px;
  
  h2 {
    color: white;
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 24px;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  }
}

.auth-preview {
  display: flex;
  justify-content: center;
  
  .auth-box {
    width: 440px;
    max-width: 100%;
  }
  
  .auth-header {
    text-align: center;
    margin-bottom: 32px;
    
    .logo {
      font-size: 64px;
      margin-bottom: 16px;
    }
    
    .title {
      font-size: 28px;
      font-weight: 800;
      color: var(--campus-text-primary);
      margin: 16px 0 8px;
    }
    
    .subtitle {
      font-size: 13px;
      color: var(--campus-text-secondary);
      text-transform: uppercase;
      letter-spacing: 2px;
      font-weight: 600;
      margin-bottom: 8px;
    }
    
    .welcome-text {
      font-size: 12px;
      color: var(--campus-text-secondary);
      font-style: italic;
    }
  }
  
  .form-demo {
    .demo-input {
      margin-bottom: 20px;
      height: 52px;
    }
    
    .demo-button {
      width: 100%;
      height: 56px;
      margin-top: 16px;
    }
  }
}

.components-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.component-item {
  h3 {
    color: var(--campus-text-primary);
    font-size: 18px;
    font-weight: 700;
    margin-bottom: 16px;
  }
}

.button-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.color-palette {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.color-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  
  span {
    color: white;
    font-weight: 600;
    font-size: 14px;
  }
}

.color-swatch {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  
  &.primary {
    background: var(--campus-primary);
  }
  
  &.secondary {
    background: var(--campus-secondary);
  }
  
  &.accent {
    background: var(--campus-accent);
  }
}

@media (max-width: 768px) {
  .showcase-title {
    font-size: 28px;
  }
  
  .components-grid {
    grid-template-columns: 1fr;
  }
  
  .color-palette {
    justify-content: center;
  }
}
</style>

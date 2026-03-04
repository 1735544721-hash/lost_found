<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <h2>页面未找到</h2>
      <p>抱歉，您访问的页面不存在</p>
      <router-link to="/" class="back-home">
        返回首页
      </router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.content {
  padding: 20px;
}

h1 {
  font-size: 120px;
  margin: 0;
  color: #1890ff;
}

h2 {
  font-size: 30px;
  margin: 20px 0;
  color: #333;
}

p {
  color: #666;
  margin-bottom: 20px;
}

.back-home {
  display: inline-block;
  padding: 10px 20px;
  background: #1890ff;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background 0.3s;
}

.back-home:hover {
  background: #40a9ff;
}
</style> 
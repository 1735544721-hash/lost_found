<template>
  <div class="role-list">
    <el-alert
      title="系统内置了两种角色：管理员(ADMIN)和普通用户(USER)"
      type="info"
      show-icon
      :closable="false"
    />
    
    <div class="role-table">
      <el-table :data="roleData" stripe border style="width: 100%; margin-top: 20px;">
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="code" label="角色编码" width="180" />
        <el-table-column prop="name" label="角色名称" width="180" />
        <el-table-column prop="description" label="角色描述" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 角色数据 - 系统目前只有两种内置角色
const roleData = ref([
  {
    code: 'ADMIN',
    name: '管理员',
    description: '系统管理员，拥有所有操作权限'
  },
  {
    code: 'USER',
    name: '普通用户',
    description: '普通用户，拥有基本操作权限'
  }
])
</script>

<style lang="scss" scoped>
.role-list {
  padding: 20px 0;
}
</style> 
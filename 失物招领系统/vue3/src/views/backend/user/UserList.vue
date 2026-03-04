<template>
  <div class="user-list">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleCode" placeholder="请选择角色" clearable>
            <el-option label="管理员" value="ADMIN" />
            <el-option label="用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格区域 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      border
      style="width: 100%"
    >
      <el-table-column type="index" label="序号" width="60" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="sex" label="性别" width="60" />
      <el-table-column prop="roleCode" label="角色">
        <template #default="scope">
          <el-tag :type="scope.row.roleCode === 'ADMIN' ? 'danger' : 'primary'">
            {{ scope.row.roleCode === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          <el-button 
            size="small" 
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            @click="handleStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="primary" @click="handleResetPwd(scope.row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 用户表单对话框 -->
    <el-dialog 
      :title="formTitle" 
      v-model="dialogVisible" 
      width="500px"
      append-to-body
    >
      <el-form 
        ref="userFormRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="userForm.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="userForm.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="userForm.roleCode" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="普通用户" value="USER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog 
      title="重置密码" 
      v-model="resetPwdDialogVisible" 
      width="400px"
      append-to-body
    >
      <el-form 
        ref="resetPwdFormRef"
        :model="resetPwdForm"
        :rules="resetPwdRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" placeholder="请输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPwdDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPwd" :loading="resetPwdLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

// 表格数据
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索表单
const searchForm = reactive({
  username: '',
  name: '',
  roleCode: ''
})

// 用户表单
const userFormRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')
const userForm = reactive({
  id: '',
  username: '',
  password: '',
  name: '',
  email: '',
  phone: '',
  sex: '男',
  roleCode: 'USER',
  status: 1
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (rule, value, callback) => {
      if (isEdit.value) {
        callback()
      } else if (!value) {
        callback(new Error('请输入密码'))
      } else {
        callback()
      }
    } },
    { min: 6, message: '密码长度不能小于 6 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 重置密码表单
const resetPwdFormRef = ref(null)
const resetPwdDialogVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdForm = reactive({
  id: '',
  newPassword: ''
})

// 重置密码验证规则
const resetPwdRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于 6 个字符', trigger: 'blur' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    await request.get('/user/page', {
      username: searchForm.username,
      name: searchForm.name,
      roleCode: searchForm.roleCode,
      currentPage: currentPage.value,
      size: pageSize.value
    },{
      onSuccess:(res=>{
        tableData.value = res.records || []
        total.value = res.total || 0
      })
    })

  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = ''
  })
  fetchUsers()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchUsers()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchUsers()
}

// 新增用户
const handleAdd = () => {
  resetUserForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  resetUserForm()
  isEdit.value = true
  userForm.id = row.id
  userForm.username = row.username
  userForm.name = row.name || ''
  userForm.email = row.email || ''
  userForm.phone = row.phone || ''
  userForm.sex = row.sex || '男'
  userForm.roleCode = row.roleCode || 'USER'
  userForm.status = row.status
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${row.username} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.delete(`/user/delete/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchUsers()
        }
      })
    } catch (error) {
      console.error('删除用户失败:', error)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 修改用户状态
const handleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.put(`/user/status/${row.id}?status=${newStatus}`, null, {
      successMsg: `${newStatus === 1 ? '启用' : '禁用'}成功`,
      onSuccess: () => {
        row.status = newStatus
      }
    })
  } catch (error) {
    console.error('修改状态失败:', error)
  }
}

// 重置密码
const handleResetPwd = (row) => {
  resetPwdForm.id = row.id
  resetPwdForm.newPassword = ''
  resetPwdDialogVisible.value = true
}

// 提交重置密码
const submitResetPwd = async () => {
  if (!resetPwdFormRef.value) return
  
  try {
    await resetPwdFormRef.value.validate()
    
    resetPwdLoading.value = true
    await request.get(`/user/forget`, {
      email: tableData.value.find(user => user.id === resetPwdForm.id).email,
      newPassword: resetPwdForm.newPassword
    }, {
      successMsg: '密码重置成功',
      onSuccess: () => {
        resetPwdDialogVisible.value = false
      }
    })
  } catch (error) {
    console.error('重置密码失败:', error)
  } finally {
    resetPwdLoading.value = false
  }
}

// 重置用户表单
const resetUserForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  userForm.id = ''
  userForm.username = ''
  userForm.password = ''
  userForm.name = ''
  userForm.email = ''
  userForm.phone = ''
  userForm.sex = '男'
  userForm.roleCode = 'USER'
  userForm.status = 1
}

// 提交用户表单
const submitForm = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    
    submitLoading.value = true
    if (isEdit.value) {
      // 编辑用户
      await request.put(`/user/${userForm.id}`, {
        name: userForm.name,
        email: userForm.email,
        phone: userForm.phone,
        sex: userForm.sex,
        roleCode: userForm.roleCode,
        status: userForm.status
      }, {
        successMsg: '更新成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchUsers()
        }
      })
    } else {
      // 新增用户
      await request.post('/user/add', userForm, {
        successMsg: '添加成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchUsers()
        }
      })
    }
  } catch (error) {
    console.error('提交表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-list {
  .search-container {
    margin-bottom: 20px;
    padding: 15px;
    background-color: #fff;
    border-radius: 4px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 
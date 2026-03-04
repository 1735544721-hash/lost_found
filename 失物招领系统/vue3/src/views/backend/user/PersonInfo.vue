<template>
  <div class="person-info">
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="handleEdit" v-if="!isEditing">
            编辑信息
          </el-button>
          <div v-else>
            <el-button type="primary" @click="handleSave" :loading="saving">
              保存
            </el-button>
            <el-button @click="handleCancel">取消</el-button>
          </div>
        </div>
      </template>

      <div class="info-content">
        <!-- 添加头像上传部分 -->
        <div class="avatar-container">
          <el-avatar :size="100" :src="avatarUrl" @error="() => false" />
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="true"
            :show-file-list="false"
            :http-request="customUploadAvatar"
            :before-upload="beforeAvatarUpload"
            :disabled="!isEditing"
          >
            <el-button size="small" type="primary" :disabled="!isEditing">更换头像</el-button>
          </el-upload>
        </div>

        <el-form 
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          :disabled="!isEditing"
          class="info-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" disabled />
          </el-form-item>

          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" placeholder="请输入姓名" />
          </el-form-item>

          <!-- 添加性别选择 -->
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="form.sex">
              <el-radio label="男">男</el-radio>
              <el-radio label="女">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleChangePassword"
            :loading="changingPassword"
          >
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const baseAPI = import.meta.env.VITE_BASE_API || '/api'
const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const isEditing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)

// 表单数据
const form = reactive({
  id: '',
  username: '',
  name: '',
  email: '',
  phone: '',
  sex: '男',
  avatar: ''
})

// 头像地址
const avatarUrl = computed(() => {
  return form.avatar ? baseAPI + form.avatar : '';
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  sex: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 密码验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    // 获取当前用户的最新信息
    const userId = userStore.userInfo.id
    const res = await request.get(`/user/${userId}`, null, {
      showDefaultMsg: false
    })
    
    // 直接更新表单数据
    form.id = res.id || userStore.userInfo.id
    form.username = res.username || ''
    form.name = res.name || ''
    form.email = res.email || ''
    form.phone = res.phone || ''
    form.sex = res.sex || '男'
    form.avatar = res.avatar || ''
    
    console.log('用户信息加载成功:', form)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义头像上传方法
const customUploadAvatar = async (options) => {
  try {
    const { file } = options

    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('file', file)

    // 设置自定义上传选项
    const uploadOptions = {
      headers: {
        token: localStorage.getItem('token') || '',
      },
      // 不进行JSON处理
      transformRequest: [(data) => data],
      // 自定义成功消息
      successMsg: '头像上传成功',
      // 自定义错误消息
      errorMsg: '头像上传失败',
      // 成功回调
      onSuccess: async (data) => {
        // 更新用户头像
        form.avatar = data

        // 保存到后端
        await updateUserAvatar(data)

        // 通知上传成功
        options.onSuccess({ data })
      },
      // 错误回调
      onError: (error) => {
        console.error('头像上传错误:', error)
        options.onError(new Error(error.message || '上传失败'))
      },
    }

    // 发送上传请求
    await request.post('/file/upload/img', formData, uploadOptions)
  } catch (error) {
    options.onError(error)
    console.error('头像上传过程发生错误:', error)
  }
}

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    await request.put(
      `/user/${form.id}`,
      { avatar: avatarPath },
      {
        showDefaultMsg: false,
        successMsg: '头像更新成功',
        onSuccess: (data) => {
          // 更新本地用户信息
          const updatedUserInfo = { ...userStore.userInfo, avatar: avatarPath }
          userStore.updateUserInfo(updatedUserInfo)
        },
        onError: (error) => {
          console.error('头像信息保存失败', error)
          ElMessage.error('头像信息保存失败')
        },
      }
    )
  } catch (error) {
    console.error('头像信息保存失败', error)
    ElMessage.error('头像信息保存失败')
    throw error
  }
}

// 编辑信息
const handleEdit = () => {
  isEditing.value = true
}

// 取消编辑
const handleCancel = () => {
  isEditing.value = false
  fetchUserInfo() // 重新获取数据，恢复原值
}

// 保存信息
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    await request.put(
      `/user/${form.id}`,
      {
        name: form.name,
        email: form.email,
        phone: form.phone,
        sex: form.sex
      },
      {
        showDefaultMsg: false,
        successMsg: '个人信息更新成功',
        onSuccess: (data) => {
          isEditing.value = false
          // 更新store中的用户信息
          userStore.updateUserInfo({
            ...userStore.userInfo,
            name: form.name,
            email: form.email,
            phone: form.phone,
            sex: form.sex
          })
        },
        onError: (error) => {
          console.error('更新用户信息失败:', error)
          ElMessage.error('更新用户信息失败')
        }
      }
    )
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error('更新用户信息失败')
  } finally {
    saving.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
    changingPassword.value = true

    await request.put(
      `/user/password/${form.id}`,
      {
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      },
      {
        showDefaultMsg: false,
        successMsg: '密码修改成功',
        onSuccess: (data) => {
          // 清空密码表单
          passwordFormRef.value.resetFields()
          
          // 提示用户重新登录
          ElMessageBox.confirm('密码已修改，需要重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning',
          }).then(() => {
            // 清除用户信息并跳转到登录页
            userStore.clearUserInfo()
            window.location.href = '/login'
          })
        },
        onError: (error) => {
          console.error('密码修改失败', error)
          ElMessage.error('密码修改失败')
        }
      }
    )
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style lang="scss" scoped>
.person-info {
  padding: 20px;
  
  .info-card,
  .password-card {
    margin-bottom: 20px;
    border-radius: 8px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .info-content {
    display: flex;
    flex-direction: column;
    gap: 30px;
    
    @media (min-width: 768px) {
      flex-direction: row;
    }
  }
  
  .avatar-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
    
    .avatar-uploader {
      text-align: center;
      margin-top: 10px;
    }
  }
  
  .info-form {
    flex: 1;
    max-width: 500px;
    margin: 0 auto;
  }

  :deep(.el-form) {
    max-width: 500px;
    margin: 0 auto;
  }
}
</style> 
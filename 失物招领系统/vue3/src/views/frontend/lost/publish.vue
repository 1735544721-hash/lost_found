<template>
  <div class="lost-publish-page">
    <div class="form-header">
      <h1>发布失物信息</h1>
      <el-button @click="$router.push('/lost')">
        <el-icon><ArrowLeft /></el-icon> 返回列表
      </el-button>
    </div>
    
    <el-card class="form-card">
      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-width="100px"
        label-position="top"
        v-loading="loading"
      >
        <el-form-item label="物品标题" prop="title">
          <el-input v-model="formData.title" placeholder="请输入物品标题" maxlength="50" show-word-limit />
        </el-form-item>
        
        <el-form-item label="物品分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择物品分类" style="width: 100%">
            <el-option 
              v-for="item in categoryOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="丢失地点" prop="lostPlace">
          <el-input v-model="formData.lostPlace" placeholder="请输入丢失地点" maxlength="100" />
        </el-form-item>
        
        <el-form-item label="丢失时间" prop="lostTime">
          <el-date-picker
            v-model="formData.lostTime"
            type="datetime"
            placeholder="请选择丢失时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="物品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="5"
            placeholder="请详细描述物品特征、丢失经过等信息，以便他人辨认"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="物品图片">
          <el-upload
            class="upload-container"
            action="#"
            list-type="picture-card"
            :http-request="customUpload"
            :before-upload="beforeImageUpload"
            :on-remove="handleRemoveImage"
            :limit="5"
            :file-list="fileList"
            :on-exceed="handleExceed"
            :on-preview="handlePreviewImage"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                请上传清晰的物品照片，最多5张，每张不超过2MB，支持jpg、png、jpeg格式
              </div>
            </template>
          </el-upload>
          
          <el-dialog v-model="previewVisible" append-to-body>
            <img w-full :src="previewUrl" alt="Preview Image" />
          </el-dialog>
        </el-form-item>
        
        <el-divider content-position="center">联系方式</el-divider>
        
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="formData.contactName" placeholder="请输入联系人姓名" maxlength="20" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" maxlength="20" />
        </el-form-item>
        
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" maxlength="50" />
        </el-form-item>
        
        <el-form-item label="备注信息">
          <el-input
            v-model="formData.contactNote"
            type="textarea"
            :rows="3"
            placeholder="其他联系方式或备注信息"
            maxlength="200"
          />
        </el-form-item>
        
        <el-form-item>
          <div class="form-actions">
            <el-button @click="resetForm">重置</el-button>
            <el-button type="primary" @click="submitForm" :loading="submitting">发布</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

// 检查用户是否登录
if (!userStore.isLoggedIn) {
  ElMessage.warning('请先登录后再发布失物信息')
  router.push('/login')
}

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 格式化日期的辅助函数
const formatDate = (date, format) => {
  if (!date) return ''
  return DateUtils.format(date, format)
}

// 表单数据
const formData = reactive({
  title: '',
  categoryId: '',
  lostPlace: '',
  lostTime: '',
  description: '',
  images: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  contactNote: ''
})

// 分类选项
const categoryOptions = ref([])

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入物品标题', trigger: 'blur' },
    { min: 2, max: 50, message: '标题长度在2到50个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择物品分类', trigger: 'change' }
  ],
  lostPlace: [
    { required: true, message: '请输入丢失地点', trigger: 'blur' }
  ],
  lostTime: [
    { required: true, message: '请选择丢失时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入物品描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在10到500个字符之间', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  contactEmail: [
    { required: false, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    await request.get('/category/list', null, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        categoryOptions.value = data || []
        if (categoryOptions.value.length > 0) {
          formData.categoryId = categoryOptions.value[0].id
        }
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 图片上传前的校验
const beforeImageUpload = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png']
  const isValidType = validTypes.includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isValidType) {
    ElMessage.error('只能上传JPG/JPEG/PNG格式的图片!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
    return false
  }
  
  return true
}

// 自定义上传方法
const customUpload = async (options) => {
  try {
    const { file } = options
    
    const formData = new FormData()
    formData.append('file', file)
    
    const uploadOptions = {
      headers: {
        token: localStorage.getItem('token') || '',
      },
      transformRequest: [(data) => data],
      successMsg: '图片上传成功',
      errorMsg: '图片上传失败',
      onSuccess: (data) => {
        if (!data) {
          options.onError(new Error('上传失败：服务器返回数据为空'))
          return
        }
        
        // 保存图片信息
        const fullUrl = baseAPI + data
        fileList.value.push({
          name: file.name,
          url: fullUrl,
          raw: data // 保存原始路径
        })
        options.onSuccess()
      },
      onError: (error) => {
        console.error('图片上传错误:', error)
        options.onError(new Error(error.message || '上传失败'))
      },
    }
    
    await request.post('/file/upload/img', formData, uploadOptions)
  } catch (error) {
    options.onError(error)
    console.error('图片上传过程发生错误:', error)
  }
}

// 处理超过文件数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片！')
}

// 删除图片
const handleRemoveImage = (file) => {
  fileList.value = fileList.value.filter(item => item.url !== file.url)
}

// 预览图片
const handlePreviewImage = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

// 重置表单
const resetForm = () => {
  formRef.value.resetFields()
  fileList.value = []
}

// 提交表单
const submitForm = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    ElMessage.warning('请先登录后再发布失物信息')
    router.push('/login')
    return
  }
  
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    // 改进图片处理逻辑
    let finalImages = []
    
    // 处理上传的图片
    if (fileList.value && fileList.value.length > 0) {
      const newImages = fileList.value.map(file => file.raw).filter(img => img)
      finalImages = [...finalImages, ...newImages]
    }
    
    // 转换为字符串格式
    formData.images = finalImages.join(',')
    
    submitting.value = true
    
    try {
      // 提交失物信息
      await request.post('/lost-item', {
        ...formData,
        userId: userStore.userInfo.id,
        status: 0 // 待认领状态
      }, {
        successMsg: '发布成功',
        onSuccess: () => {
          ElMessageBox.confirm(
            '失物信息发布成功，是否返回列表？',
            '提示',
            {
              confirmButtonText: '返回列表',
              cancelButtonText: '继续发布',
              type: 'success'
            }
          ).then(() => {
            router.push('/lost')
          }).catch(() => {
            resetForm()
          })
        }
      })
    } catch (error) {
      console.error('发布失物信息失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 初始化联系人信息
const initContactInfo = () => {
  if (userStore.userInfo) {
    formData.contactName = userStore.userInfo.realName || userStore.userInfo.username || ''
    formData.contactPhone = userStore.userInfo.phone || ''
    formData.contactEmail = userStore.userInfo.email || ''
  }
}

// 初始化
onMounted(() => {
  fetchCategories()
  initContactInfo()
})
</script>

<style lang="scss">
// UI设计变量 - 全局主题
:root {
  // 静谧蓝系列
  --serene-blue-50: #f0f4ff;
  --serene-blue-100: #e0ebff;
  --serene-blue-200: #c7d7fe;
  --serene-blue-300: #a5b8fc;
  --serene-blue-400: #818cf8;
  --serene-blue-500: #6366f1;
  --serene-blue-600: #4f46e5;
  --serene-blue-700: #4338ca;
  --serene-blue-800: #3730a3;
  
  // 暖阳黄系列
  --warm-yellow-50: #fffbeb;
  --warm-yellow-100: #fef3c7;
  --warm-yellow-200: #fde68a;
  --warm-yellow-300: #fcd34d;
  --warm-yellow-400: #fbbf24;
  --warm-yellow-500: #f59e0b;
  --warm-yellow-600: #d97706;
  
  // 中性灰系列
  --neutral-gray-50: #f9fafb;
  --neutral-gray-100: #f3f4f6;
  --neutral-gray-200: #e5e7eb;
  --neutral-gray-300: #d1d5db;
  --neutral-gray-400: #9ca3af;
  --neutral-gray-500: #6b7280;
  --neutral-gray-600: #4b5563;
  --neutral-gray-700: #374151;
  --neutral-gray-800: #1f2937;
  --neutral-gray-900: #111827;
  
  // 设计系统
  --border-radius-sm: 6px;
  --border-radius-md: 8px;
  --border-radius-lg: 12px;
  --border-radius-xl: 16px;
  --border-radius-2xl: 24px;
  
  // 阴影
  --shadow-soft: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  --shadow-medium: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-large: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  
  // 间距
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 16px;
  --spacing-lg: 24px;
  --spacing-xl: 32px;
  --spacing-2xl: 48px;
}
</style>

<style lang="scss" scoped>
.lost-publish-page {
  font-family: 'Source Han Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.6;
  color: var(--neutral-gray-800);
  animation: fadeInUp 0.6s ease-out;
  
  .form-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-2xl);
    padding-bottom: var(--spacing-lg);
    border-bottom: 2px solid var(--neutral-gray-100);
    
    h1 {
      margin: 0;
      font-size: 32px;
      font-weight: 700;
      color: var(--neutral-gray-900);
    }
    
    .el-button {
      border-radius: var(--border-radius-lg);
      border: 2px solid var(--neutral-gray-200);
      color: var(--neutral-gray-600);
      background: white;
      padding: 12px 20px;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: var(--serene-blue-400);
        color: var(--serene-blue-600);
        transform: translateX(-4px);
      }
    }
  }
  
  .form-card {
    margin-bottom: var(--spacing-xl);
    border-radius: var(--border-radius-2xl);
    border: 1px solid var(--neutral-gray-200);
    box-shadow: var(--shadow-medium);
    
    :deep(.el-card__body) {
      padding: var(--spacing-2xl);
    }
    
    :deep(.el-form) {
      max-width: 900px;
      margin: 0 auto;
      
      .el-form-item {
        margin-bottom: var(--spacing-xl);
        
        .el-form-item__label {
          font-weight: 600;
          color: var(--neutral-gray-700);
          font-size: 16px;
          margin-bottom: var(--spacing-sm);
        }
        
        .el-input {
          .el-input__wrapper {
            border-radius: var(--border-radius-lg);
            border: 2px solid var(--neutral-gray-200);
            transition: all 0.3s ease;
            
            &:hover {
              border-color: var(--serene-blue-300);
            }
            
            &.is-focus {
              border-color: var(--serene-blue-600);
              box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
            }
          }
        }
        
        .el-select {
          .el-select__wrapper {
            border-radius: var(--border-radius-lg);
            border: 2px solid var(--neutral-gray-200);
            transition: all 0.3s ease;
            
            &:hover {
              border-color: var(--serene-blue-300);
            }
            
            &.is-focus {
              border-color: var(--serene-blue-600);
              box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
            }
          }
        }
        
        .el-date-editor {
          .el-input__wrapper {
            border-radius: var(--border-radius-lg);
            border: 2px solid var(--neutral-gray-200);
            transition: all 0.3s ease;
            
            &:hover {
              border-color: var(--serene-blue-300);
            }
            
            &.is-focus {
              border-color: var(--serene-blue-600);
              box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
            }
          }
        }
        
        .el-textarea {
          .el-textarea__inner {
            border-radius: var(--border-radius-lg);
            border: 2px solid var(--neutral-gray-200);
            transition: all 0.3s ease;
            font-family: inherit;
            
            &:hover {
              border-color: var(--serene-blue-300);
            }
            
            &:focus {
              border-color: var(--serene-blue-600);
              box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
            }
          }
        }
        
        .el-divider {
          border-color: var(--neutral-gray-200);
          margin: var(--spacing-2xl) 0;
          
          .el-divider__text {
            background: white;
            color: var(--neutral-gray-600);
            font-weight: 600;
          }
        }
      }
    }
  }
  
  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: var(--spacing-md);
    
    .el-button {
      border-radius: var(--border-radius-lg);
      padding: 12px 24px;
      font-weight: 500;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      
      &.el-button--primary {
        background: var(--serene-blue-600);
        border-color: var(--serene-blue-600);
        
        &:hover {
          background: var(--serene-blue-700);
          border-color: var(--serene-blue-700);
          transform: translateY(-2px);
          box-shadow: var(--shadow-large);
        }
      }
      
      &:not(.el-button--primary) {
        background: white;
        color: var(--neutral-gray-600);
        border: 2px solid var(--neutral-gray-200);
        
        &:hover {
          border-color: var(--serene-blue-400);
          color: var(--serene-blue-600);
          transform: translateY(-2px);
        }
      }
    }
  }
  
  .upload-container {
    width: 100%;
    
    :deep(.el-upload--picture-card) {
      background: var(--neutral-gray-50);
      border: 2px dashed var(--neutral-gray-300);
      border-radius: var(--border-radius-xl);
      width: 160px;
      height: 160px;
      transition: all 0.3s ease;
      
      &:hover {
        border-color: var(--serene-blue-400);
        background: var(--serene-blue-50);
      }
      
      .el-icon {
        color: var(--serene-blue-600);
        font-size: 32px;
      }
    }
    
    :deep(.el-upload-list--picture-card) {
      .el-upload-list__item {
        width: 160px;
        height: 160px;
        border-radius: var(--border-radius-xl);
        border: 2px solid var(--neutral-gray-200);
      }
    }
    
    :deep(.el-upload__tip) {
      color: var(--neutral-gray-500);
      font-size: 14px;
      margin-top: var(--spacing-md);
    }
  }
}

// 动画定义
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .lost-publish-page {
    .form-header {
      flex-direction: column;
      align-items: flex-start;
      gap: var(--spacing-md);
      
      h1 {
        font-size: 24px;
      }
    }
    
    .form-card {
      :deep(.el-card__body) {
        padding: var(--spacing-lg);
      }
    }
    
    .form-actions {
      justify-content: center;
      flex-direction: column;
      
      .el-button {
        width: 100%;
      }
    }
    
    .upload-container {
      :deep(.el-upload--picture-card) {
        width: 120px;
        height: 120px;
      }
      
      :deep(.el-upload-list--picture-card) {
        .el-upload-list__item {
          width: 120px;
          height: 120px;
        }
      }
    }
  }
}
</style> 
<template>
  <div class="edit-found-container">
    <el-page-header @back="goBack" title="返回">
      <template #content>
        <span class="page-title">编辑招领信息</span>
      </template>
    </el-page-header>

    <div class="edit-content" v-loading="loading">
      <el-card shadow="never" v-if="foundForm">
        <el-form
          ref="formRef"
          :model="foundForm"
          :rules="rules"
          label-width="100px"
          class="edit-form"
        >
          <el-form-item label="标题" prop="title">
            <el-input v-model="foundForm.title" placeholder="请输入招领信息标题（最多50个字符）" maxlength="50" show-word-limit></el-input>
          </el-form-item>
          
          <el-form-item label="物品分类" prop="categoryId">
            <el-select v-model="foundForm.categoryId" placeholder="请选择物品分类" style="width: 100%;">
              <el-option
                v-for="item in categoryOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="拾取地点" prop="foundPlace">
            <el-input v-model="foundForm.foundPlace" placeholder="请输入拾取地点"></el-input>
          </el-form-item>
          
          <el-form-item label="拾取时间" prop="foundTime">
            <el-date-picker
              v-model="foundForm.foundTime"
              type="datetime"
              placeholder="请选择拾取时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%;"
              :disabled-date="disabledDate"
            ></el-date-picker>
          </el-form-item>
          
          <el-form-item label="联系人" prop="contactName">
            <el-input v-model="foundForm.contactName" placeholder="请输入联系人姓名"></el-input>
          </el-form-item>
          
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="foundForm.contactPhone" placeholder="请输入联系电话"></el-input>
          </el-form-item>
          
          <el-form-item label="详细描述" prop="description">
            <el-input
              v-model="foundForm.description"
              type="textarea"
              :rows="6"
              placeholder="请详细描述物品特征、拾取过程等信息，以便失主认领"
              maxlength="500"
              show-word-limit
            ></el-input>
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
                  可上传JPG/JPEG/PNG图片，最多5张，单张不超过5MB
                </div>
              </template>
            </el-upload>
            
            <el-dialog v-model="previewVisible" append-to-body>
              <img w-full :src="previewUrl" alt="Preview Image" />
            </el-dialog>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="submitForm" :loading="submitting">保存修改</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Picture } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import DateUtils from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(true)
const submitting = ref(false)
const categoryOptions = ref([])
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 格式化日期的辅助函数
const formatDate = (date, format) => {
  if (!date) return ''
  return DateUtils.format(date, format)
}

// 表单数据
const foundForm = ref({
  id: '',
  title: '',
  categoryId: '',
  foundPlace: '',
  foundTime: '',
  contactName: '',
  contactPhone: '',
  description: '',
  images: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入招领信息标题', trigger: 'blur' },
    { min: 3, max: 50, message: '标题长度应在3到50个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择物品分类', trigger: 'change' }
  ],
  foundPlace: [
    { required: true, message: '请输入拾取地点', trigger: 'blur' }
  ],
  foundTime: [
    { required: true, message: '请选择拾取时间', trigger: 'change' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度应在10到500个字符之间', trigger: 'blur' }
  ]
}

// 图片上传前的校验
const beforeImageUpload = (file) => {
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png']
  const isValidType = validTypes.includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isValidType) {
    ElMessage.error('只能上传JPG/JPEG/PNG格式的图片!')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
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
        
        // 保存图片信息，增加错误处理
        try {
          const fullUrl = baseAPI + data;
        fileList.value.push({
          name: file.name,
            url: fullUrl,
          raw: data // 保存原始路径
        })
        options.onSuccess()
        } catch (err) {
          console.error('处理上传图片数据失败:', err)
          options.onError(new Error('处理上传图片数据失败'))
        }
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

// 删除图片
const handleRemoveImage = (file) => {
  fileList.value = fileList.value.filter(item => item.url !== file.url)
}

// 超出上传数量限制
const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片')
}

// 预览图片
const handlePreviewImage = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

// 日期禁用（不能选择未来日期）
const disabledDate = (time) => {
  return time > Date.now()
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    await request.get('/category/list', null, {
      onSuccess: (data) => {
        categoryOptions.value = data || []
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 生成完整图片URL
const imageUrl = (path) => {
  if (!path) return ''
  return path.startsWith('http') ? path : baseAPI + path
}

// 获取招领物详情
const fetchFoundItemDetail = async () => {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }
  
  loading.value = true
  try {
    await request.get(`/found-item/${id}`, null, {
      showDefaultMsg: false,
      onSuccess: (data) => {
        if (!data) {
          return
        }
        
        // 检查当前用户是否有权限编辑
        if (data.userId !== userStore.userInfo?.id && userStore.userInfo?.roleCode !== 'ADMIN') {
          ElMessage.error('您没有权限编辑此招领信息')
          foundForm.value.id = ''
          return
        }
        
        // 填充表单数据
        Object.keys(foundForm.value).forEach(key => {
          if (data[key] !== undefined) {
            foundForm.value[key] = data[key]
          }
        })
        
        // 处理图片
        if (data.images) {
          const imageArray = typeof data.images === 'string' ? 
            data.images.split(',').filter(img => img) : 
            Array.isArray(data.images) ? data.images.filter(img => img) : [];
          
          fileList.value = imageArray.map(img => ({
            name: img.substring(img.lastIndexOf('/') + 1),
            url: imageUrl(img),
            raw: img // 保存原始路径
          }));
        } else {
          fileList.value = [];
        }
      },
      onError: (error) => {
        console.error('获取招领物详情失败:', error)
        foundForm.value.id = ''
      }
    })
  } catch (error) {
    console.error('获取招领物详情失败:', error)
    foundForm.value.id = ''
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    
    submitting.value = true
    
    try {
      // 处理图片路径
      const imageUrls = fileList.value.map(file => file.raw).filter(img => img).join(',');
      foundForm.value.images = imageUrls
      // 提交招领信息
      await request.put(`/found-item`, {
        ...foundForm.value
        // images: imageUrls
      }, {
        successMsg: '更新成功',
        onSuccess: () => {
            router.push(`/found/detail/${foundForm.value.id}`)
        }
      })
    } catch (error) {
      console.error('更新招领信息失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  fetchFoundItemDetail()
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchFoundItemDetail()
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
.edit-found-container {
  font-family: 'Source Han Sans', -apple-system, BlinkMacSystemFont, sans-serif;
  line-height: 1.6;
  color: var(--neutral-gray-800);
  animation: fadeInUp 0.6s ease-out;
  
  :deep(.el-page-header) {
    padding: var(--spacing-xl) 0;
    margin-bottom: var(--spacing-xl);
    
    .el-page-header__back {
      .el-icon {
        color: var(--serene-blue-600);
        transition: all 0.3s ease;
        
        &:hover {
          color: var(--serene-blue-700);
          transform: translateX(-4px);
        }
      }
    }
    
    .el-page-header__title {
      color: var(--neutral-gray-600);
      font-weight: 500;
    }
    
    .el-page-header__content {
      .page-title {
        font-size: 24px;
        font-weight: 700;
        color: var(--neutral-gray-900);
      }
    }
  }
  
  .edit-content {
    margin-top: var(--spacing-xl);
    
    :deep(.el-card) {
      border-radius: var(--border-radius-2xl);
      border: 1px solid var(--neutral-gray-200);
      box-shadow: var(--shadow-medium);
      
      .el-card__body {
        padding: var(--spacing-2xl);
      }
    }
    
    .edit-form {
      max-width: 900px;
      margin: 0 auto;
      
      :deep(.el-form-item) {
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
  .edit-found-container {
    .edit-content {
      :deep(.el-card) {
        .el-card__body {
          padding: var(--spacing-lg);
        }
      }
      
      .edit-form {
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
  }
}
</style> 
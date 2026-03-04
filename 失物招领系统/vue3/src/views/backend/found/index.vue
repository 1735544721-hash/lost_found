<template>
  <div class="found-management">
    <el-card class="search-card" shadow="never">
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题" clearable></el-input>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
              <el-option
                v-for="item in categoryOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option :value="0" label="待认领"></el-option>
              <el-option :value="1" label="已认领"></el-option>
              <el-option :value="2" label="已关闭"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card class="data-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>招领信息列表</span>
        </div>
      </template>
      
      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading"
        border
        stripe
      >
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100"></el-table-column>
        <el-table-column prop="foundPlace" label="拾取地点" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="foundTime" label="拾取时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.foundTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="发布人" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="info">待认领</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="success">已认领</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="danger">已关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-dropdown>
              <el-button link type="primary" size="small">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleStatusChange(scope.row, 0)" v-if="scope.row.status !== 0">
                    标记为待认领
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleStatusChange(scope.row, 1)" v-if="scope.row.status !== 1">
                    标记为已认领
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleStatusChange(scope.row, 2)" v-if="scope.row.status !== 2">
                    标记为已关闭
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleDelete(scope.row)" style="color: #f56c6c;">
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
    
    <!-- 查看招领信息弹窗 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="查看招领信息"
      width="700px"
      center
    >
      <div class="view-dialog-content" v-loading="dialogLoading">
        <div class="view-item">
          <div class="label">标题：</div>
          <div class="value">{{ currentItem.title }}</div>
        </div>
        <div class="view-item">
          <div class="label">分类：</div>
          <div class="value">{{ currentItem.categoryName }}</div>
        </div>
        <div class="view-item">
          <div class="label">拾取地点：</div>
          <div class="value">{{ currentItem.foundPlace }}</div>
        </div>
        <div class="view-item">
          <div class="label">拾取时间：</div>
          <div class="value">{{ formatDateTime(currentItem.foundTime) }}</div>
        </div>
        <div class="view-item">
          <div class="label">联系人：</div>
          <div class="value">{{ currentItem.contactName }}</div>
        </div>
        <div class="view-item">
          <div class="label">联系电话：</div>
          <div class="value">{{ currentItem.contactPhone }}</div>
        </div>
        <div class="view-item description">
          <div class="label">描述：</div>
          <div class="value">{{ currentItem.description }}</div>
        </div>
        <div class="view-item">
          <div class="label">状态：</div>
          <div class="value">
            <el-tag v-if="currentItem.status === 0" type="info">待认领</el-tag>
            <el-tag v-else-if="currentItem.status === 1" type="success">已认领</el-tag>
            <el-tag v-else-if="currentItem.status === 2" type="danger">已关闭</el-tag>
          </div>
        </div>
        <div class="view-item">
          <div class="label">发布人：</div>
          <div class="value">{{ currentItem.username }}</div>
        </div>
        <div class="view-item">
          <div class="label">发布时间：</div>
          <div class="value">{{ formatDateTime(currentItem.createTime) }}</div>
        </div>
        <div class="view-item images" v-if="currentItem.images">
          <div class="label">图片：</div>
          <div class="value image-list">
            <el-image 
              v-for="(img, index) in imageList" 
              :key="index"
              :src="baseAPI + img"
              :preview-src-list="imageUrlList"
              fit="cover"
              class="item-image"
            ></el-image>
          </div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑招领信息弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="dialogTitle"
      width="700px"
      center
    >
      <el-form 
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        v-loading="dialogLoading"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题"></el-input>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="拾取地点" prop="foundPlace">
          <el-input v-model="editForm.foundPlace" placeholder="请输入拾取地点"></el-input>
        </el-form-item>
        <el-form-item label="拾取时间" prop="foundTime">
          <el-date-picker
            v-model="editForm.foundTime"
            type="datetime"
            placeholder="选择拾取时间"
            style="width: 100%"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="editForm.contactName" placeholder="请输入联系人姓名"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="editForm.contactPhone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入招领物品描述"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option :value="0" label="待认领"></el-option>
            <el-option :value="1" label="已认领"></el-option>
            <el-option :value="2" label="已关闭"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditForm" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const categoryOptions = ref([])
const searchForm = ref({
  title: '',
  categoryId: '',
  status: ''
})

// 日期格式化函数
const formatDateTime = (date) => {
  return DateUtils.formatDateTime(date)
}

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 弹窗相关
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const dialogLoading = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑招领信息' : '新增招领信息')
const editFormRef = ref(null)

// 当前操作的对象
const currentItem = ref({})
const imageList = computed(() => {
  if (!currentItem.value.images) return []
  return currentItem.value.images.split(',')
})
const imageUrlList = computed(() => {
  return imageList.value.map(img => baseAPI + img)
})

// 编辑表单
const editForm = ref({
  id: '',
  title: '',
  categoryId: '',
  foundPlace: '',
  foundTime: '',
  contactName: '',
  contactPhone: '',
  description: '',
  status: 0,
  images: ''
})

// 表单校验规则
const editRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
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
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    await request.get('/category/list', {}, {
      onSuccess: (data) => {
        categoryOptions.value = data || []
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取招领信息列表
const fetchFoundItems = async () => {
  loading.value = true
  try {
    const params = {
      title: searchForm.value.title,
      categoryId: searchForm.value.categoryId,
      status: searchForm.value.status,
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/found-item/page', params, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取招领信息列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 查看招领信息
const handleView = async (row) => {
  viewDialogVisible.value = true
  dialogLoading.value = true
  
  try {
    await request.get(`/found-item/${row.id}`, {}, {
      onSuccess: (data) => {
        currentItem.value = data
      }
    })
  } catch (error) {
    console.error('获取招领详情失败:', error)
  } finally {
    dialogLoading.value = false
  }
}

// 编辑招领信息
const handleEdit = async (row) => {
  editDialogVisible.value = true
  isEdit.value = true
  dialogLoading.value = true
  
  try {
    await request.get(`/found-item/${row.id}`, {}, {
      onSuccess: (data) => {
        // 重置表单
        editFormRef.value?.resetFields()
        // 填充表单
        editForm.value = { ...data }
      }
    })
  } catch (error) {
    console.error('获取招领详情失败:', error)
  } finally {
    dialogLoading.value = false
  }
}

// 提交编辑表单
const submitEditForm = () => {
  editFormRef.value.validate(async valid => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      if (isEdit.value) {
        // 编辑
        await request.put(`/found-item`, editForm.value, {
          successMsg: '更新成功',
          onSuccess: () => {
            editDialogVisible.value = false
            fetchFoundItems()
          }
        })
      } else {
        // 新增
        await request.post('/found-item', editForm.value, {
          successMsg: '添加成功',
          onSuccess: () => {
            editDialogVisible.value = false
            fetchFoundItems()
          }
        })
      }
    } catch (error) {
      console.error('保存招领信息失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

// 更改招领信息状态
const handleStatusChange = async (row, status) => {
  try {
    await request.put(`/found-item/status/${row.id}/${status}`, {}, {
      successMsg: '更新状态成功',
      onSuccess: () => {
        fetchFoundItems()
      }
    })
  } catch (error) {
    console.error('更新状态失败:', error)
  }
}

// 删除招领信息
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这条招领信息吗？删除后将无法恢复！',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.delete(`/found-item/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchFoundItems()
        }
      })
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {})
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchFoundItems()
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    title: '',
    categoryId: '',
    status: ''
  }
  currentPage.value = 1
  fetchFoundItems()
}

// 每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchFoundItems()
}

// 页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchFoundItems()
}

// 生命周期钩子
onMounted(() => {
  fetchCategories()
  fetchFoundItems()
})
</script>

<style lang="scss" scoped>
.found-management {
  .search-card {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .el-dropdown {
    margin-left: 8px;
  }
  
  .view-dialog-content {
    .view-item {
      display: flex;
      margin-bottom: 15px;

      .label {
        width: 100px;
        font-weight: bold;
        flex-shrink: 0;
      }

      .value {
        flex: 1;
      }

      &.description {
        .value {
          white-space: pre-wrap;
        }
      }

      &.images {
        .image-list {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;

          .item-image {
            width: 120px;
            height: 120px;
            border-radius: 4px;
          }
        }
      }
    }
  }
}
</style> 
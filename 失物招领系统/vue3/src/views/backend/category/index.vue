<template>
  <div class="category-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>物品分类管理</span>
          <el-button type="primary" @click="handleAdd">添加分类</el-button>
        </div>
      </template>
      
      <!-- 搜索区域 -->
      <div class="search-container">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="分类名称">
            <el-input v-model="searchForm.name" placeholder="请输入分类名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchCategories">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
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
        <el-table-column type="index" label="序号" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序号" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(scope.row)"
              :disabled="hasItems(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 分类表单对话框 -->
    <el-dialog
      :title="formTitle"
      v-model="dialogVisible"
      width="500px"
      append-to-body
    >
      <el-form
        ref="categoryFormRef"
        :model="categoryForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
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
  name: ''
})

// 分类表单
const categoryFormRef = ref(null)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formTitle = computed(() => isEdit.value ? '编辑分类' : '添加分类')
const categoryForm = reactive({
  id: '',
  name: '',
  sort: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序号', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    await request.get('/category/page', {
      name: searchForm.name,
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.name = ''
  fetchCategories()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchCategories()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchCategories()
}

// 添加分类
const handleAdd = () => {
  resetCategoryForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  resetCategoryForm()
  isEdit.value = true
  categoryForm.id = row.id
  categoryForm.name = row.name
  categoryForm.sort = row.sort
  dialogVisible.value = true
}

// 删除分类
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除分类 ${row.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.delete(`/category/${row.id}`, {
        successMsg: '删除成功',
        onSuccess: () => {
          fetchCategories()
        }
      })
    } catch (error) {
      console.error('删除分类失败:', error)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 重置分类表单
const resetCategoryForm = () => {
  if (categoryFormRef.value) {
    categoryFormRef.value.resetFields()
  }
  categoryForm.id = ''
  categoryForm.name = ''
  categoryForm.sort = 0
}

// 提交分类表单
const submitForm = async () => {
  if (!categoryFormRef.value) return
  
  try {
    await categoryFormRef.value.validate()
    
    submitLoading.value = true
    if (isEdit.value) {
      // 编辑分类
      await request.put(`/category/${categoryForm.id}`, categoryForm, {
        successMsg: '更新成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchCategories()
        }
      })
    } else {
      // 添加分类
      await request.post('/category', categoryForm, {
        successMsg: '添加成功',
        onSuccess: () => {
          dialogVisible.value = false
          fetchCategories()
        }
      })
    }
  } catch (error) {
    console.error('提交表单失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 判断分类是否有关联物品
const hasItems = (category) => {
  // 目前没有实现查询分类是否有关联物品的接口，默认返回false
  return false
}

// 初始化
onMounted(() => {
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.category-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .search-container {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 
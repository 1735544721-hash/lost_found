<template>
  <div class="lost-list-page">
    <div class="page-header">
      <h1 class="page-title">失物信息</h1>
      <div class="page-actions" v-if="isLoggedIn">
        <el-button type="primary" @click="$router.push('/lost/publish')">
          <el-icon><Plus /></el-icon> 发布失物信息
        </el-button>
      </div>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.title" placeholder="请输入标题关键词" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchLostItems">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据加载中 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <!-- 无数据提示 -->
    <el-empty v-else-if="!lostItems.length" description="暂无失物信息" />
    
    <!-- 失物列表 -->
    <div v-else class="lost-list">
      <el-row :gutter="20">
        <el-col v-for="item in lostItems" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
          <el-card class="lost-item-card" shadow="hover" @click="viewDetail(item)">
            <div class="item-image">
              <el-image
                :src="getFirstImage(item.images)"
                fit="cover"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="item-category">{{ item.categoryName || '未分类' }}</div>
              <div class="item-status" :class="getStatusClass(item.status)">
                {{ getStatusText(item.status) }}
              </div>
            </div>
            <div class="item-content">
              <h3 class="item-title">{{ item.title }}</h3>
              <p class="item-place">
                <el-icon><Location /></el-icon>
                <span>{{ item.lostPlace || '未知地点' }}</span>
              </p>
              <p class="item-time">
                <el-icon><Timer /></el-icon>
                <span>{{ formatTime(item.lostTime) }}</span>
              </p>
              <div class="item-footer">
                <span class="item-date">{{ formatDate(item.createTime) }}</span>
                <el-button size="small" @click.stop="viewDetail(item)">查看详情</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'
import { Plus, Location, Timer, Picture } from '@element-plus/icons-vue'
import { ElLoading } from 'element-plus'
import DateUtils from '@/utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 失物数据
const lostItems = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 分类选项
const categoryOptions = ref([])

// 搜索表单
const searchForm = reactive({
  title: '',
  categoryId: ''
})

// API基础路径
const baseAPI = import.meta.env.VITE_BASE_API || '/api'

// 获取失物列表
const fetchLostItems = async () => {
  loading.value = true
  try {
    await request.get('/lost-item/page', {
      title: searchForm.title,
      categoryId: searchForm.categoryId,
      status: 0, // 只显示待认领的
      currentPage: currentPage.value,
      size: pageSize.value
    }, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        lostItems.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取失物信息列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    await request.get('/category/list', null, {
      showDefaultMsg: false,
      onSuccess: (res) => {
        categoryOptions.value = res || []
      }
    })
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchForm.title = ''
  searchForm.categoryId = ''
  fetchLostItems()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchLostItems()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchLostItems()
}

// 查看详情
const viewDetail = (item) => {
  router.push(`/lost/detail/${item.id}`)
}

// 获取状态文字
const getStatusText = (status) => {
  switch (status) {
    case 0: return '待认领'
    case 1: return '已认领'
    case 2: return '已关闭'
    default: return '未知'
  }
}

// 获取状态类名
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-pending'
    case 1: return 'status-claimed'
    case 2: return 'status-closed'
    default: return ''
  }
}

// 获取第一张图片
const getFirstImage = (images) => {
  if (!images) return ''
  const imageArray = images.split(',').filter(img => img)
  if (imageArray.length === 0) return ''
  const firstImage = imageArray[0]
  return firstImage.startsWith('http') ? firstImage : baseAPI + firstImage
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return DateUtils.formatDate(dateStr)
}

// 格式化时间
const formatTime = (dateStr) => {
  if (!dateStr) return '未知时间'
  return DateUtils.formatDate(dateStr)
}

// 初始化
onMounted(() => {
  fetchCategories()
  fetchLostItems()
})

// 添加activated生命週期鉤子，當從其他頁面返回時重新加載數據
onActivated(() => {
  fetchLostItems()
})
</script>

<style lang="scss" scoped>
.lost-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .page-title {
      font-size: 24px;
      margin: 0;
    }
  }
  
  .search-area {
    background-color: #fff;
    padding: 20px;
    border-radius: 4px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  }
  
  .loading-container {
    padding: 40px 0;
  }
  
  .lost-list {
    margin-bottom: 20px;
    
    .lost-item-card {
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
      }
      
      .item-image {
        position: relative;
        height: 200px;
        overflow: hidden;
        
        .el-image {
          width: 100%;
          height: 100%;
        }
        
        .image-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          justify-content: center;
          align-items: center;
          background-color: #f5f7fa;
          
          .el-icon {
            font-size: 40px;
            color: #c0c4cc;
          }
        }
        
        .item-category {
          position: absolute;
          top: 10px;
          left: 10px;
          background-color: rgba(0, 0, 0, 0.5);
          color: #fff;
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 12px;
        }
        
        .item-status {
          position: absolute;
          top: 10px;
          right: 10px;
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 12px;
          
          &.status-pending {
            background-color: #909399;
            color: #fff;
          }
          
          &.status-claimed {
            background-color: #67c23a;
            color: #fff;
          }
          
          &.status-closed {
            background-color: #f56c6c;
            color: #fff;
          }
        }
      }
      
      .item-content {
        padding: 10px 0;
        
        .item-title {
          margin: 5px 0;
          font-size: 16px;
          font-weight: bold;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .item-place, .item-time {
          margin: 5px 0;
          font-size: 14px;
          color: #666;
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-right: 5px;
          }
          
          span {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        
        .item-footer {
          margin-top: 10px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .item-date {
            font-size: 12px;
            color: #999;
          }
        }
      }
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: center;
  }
}
</style> 
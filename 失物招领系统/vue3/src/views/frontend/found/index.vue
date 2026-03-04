<template>
  <div class="found-list-container">
    <div class="page-header">
      <h1>招领信息</h1>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/found/publish')" v-if="isLoggedIn">
          <el-icon><Plus /></el-icon>发布招领信息
        </el-button>
      </div>
    </div>

    <el-card class="search-card">
      <div class="search-container">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="请输入标题关键词" clearable></el-input>
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
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <div class="found-list">
      <el-empty description="暂无招领信息" v-if="foundList.length === 0 && !loading"></el-empty>
      
      <el-row :gutter="20" v-loading="loading" element-loading-text="加载中...">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in foundList" :key="item.id" class="item-col">
          <el-card class="found-card" @click="goToDetail(item.id)" shadow="hover">
            <div class="card-image">
              <img v-if="item.images" :src="getFirstImage(item.images)" alt="物品图片" />
              <div v-else class="no-image">
                <el-icon><PictureFilled /></el-icon>
                <span>暂无图片</span>
              </div>
            </div>
            <div class="card-content">
              <h3 class="item-title">{{ item.title }}</h3>
              <div class="item-info">
                <div class="info-row">
                  <el-icon><LocationFilled /></el-icon>
                  <span>{{ item.foundPlace }}</span>
                </div>
                <div class="info-row">
                  <el-icon><Timer /></el-icon>
                  <span>{{ formatDate(item.foundTime) }}</span>
                </div>
                <div class="info-row">
                  <el-icon><Collection /></el-icon>
                  <span>{{ item.categoryName }}</span>
                </div>
              </div>
              <div class="item-status">
                <el-tag v-if="item.status === 0" type="info">待认领</el-tag>
                <el-tag v-else-if="item.status === 1" type="success">已认领</el-tag>
                <el-tag v-else-if="item.status === 2" type="danger">已关闭</el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handleCurrentChange"
          hide-on-single-page
        ></el-pagination>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Plus, PictureFilled, LocationFilled, Timer, Collection } from '@element-plus/icons-vue'
import DateUtils from '@/utils/dateUtils'
import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 日期格式化函数
const formatDate = (date) => {
  return DateUtils.formatDate(date)
}

const loading = ref(false)
const foundList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const categoryOptions = ref([])
const searchForm = ref({
  title: '',
  categoryId: ''
})

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
      status: 0, // 默认只显示待认领的招领信息
      currentPage: currentPage.value,
      size: pageSize.value
    }
    
    await request.get('/found-item/page', params, {
      onSuccess: (res) => {
        foundList.value = res.records || []
        total.value = res.total || 0
      }
    })
  } catch (error) {
    console.error('获取招领信息列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取第一张图片
const getFirstImage = (images) => {
  if (!images) return ''
  const imageList = images.split(',')
  const firstImage = imageList[0]
  const baseAPI = import.meta.env.VITE_BASE_API || '/api'
  return baseAPI + firstImage
}

// 查看招领信息详情
const goToDetail = (id) => {
  router.push(`/found/detail/${id}`)
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
    categoryId: ''
  }
  currentPage.value = 1
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

// 添加activated生命週期鉤子，當從其他頁面返回時重新加載數據
onActivated(() => {
  fetchFoundItems()
})
</script>

<style lang="scss" scoped>
.found-list-container {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h1 {
      font-size: 24px;
      margin: 0;
    }
  }
  
  .search-card {
    margin-bottom: 20px;
    
    .search-container {
      padding: 0;
    }
  }
  
  .found-list {
    margin-top: 20px;
    
    .item-col {
      margin-bottom: 20px;
    }
    
    .found-card {
      height: 100%;
      cursor: pointer;
      transition: transform 0.3s;
      
      &:hover {
        transform: translateY(-5px);
      }
      
      .card-image {
        height: 180px;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f7fa;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .no-image {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          color: #909399;
          
          .el-icon {
            font-size: 40px;
            margin-bottom: 10px;
          }
        }
      }
      
      .card-content {
        padding: 10px 0;
        
        .item-title {
          font-size: 16px;
          margin: 0 0 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .item-info {
          margin-bottom: 10px;
          
          .info-row {
            display: flex;
            align-items: center;
            margin-bottom: 5px;
            font-size: 14px;
            color: #606266;
            
            .el-icon {
              margin-right: 5px;
              color: #909399;
            }
            
            span {
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
          }
        }
        
        .item-status {
          display: flex;
          justify-content: flex-end;
        }
      }
    }
  }
  
  .pagination-container {
    margin-top: 30px;
    display: flex;
    justify-content: center;
  }
}
</style> 
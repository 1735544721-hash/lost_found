<template>
  <div class="advanced-search">
    <el-card class="search-card">
      <template #header>
        <div class="search-header">
          <span>高级搜索</span>
          <el-button 
            type="text" 
            @click="toggleExpanded"
            class="expand-btn"
          >
            {{ isExpanded ? '收起' : '展开' }}
            <el-icon><ArrowDown v-if="!isExpanded" /><ArrowUp v-else /></el-icon>
          </el-button>
        </div>
      </template>

      <!-- 基础搜索 -->
      <div class="basic-search">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入关键词搜索..."
          clearable
          @input="onSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button 
              type="primary" 
              @click="handleSearch"
              :loading="isSearching"
            >
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 高级搜索选项 -->
      <el-collapse-transition>
        <div v-show="isExpanded" class="advanced-options">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="物品分类">
                <el-select 
                  v-model="searchForm.categoryId" 
                  placeholder="请选择分类"
                  clearable
                  @change="onSearch"
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.name"
                    :value="category.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="物品状态">
                <el-select 
                  v-model="searchForm.status" 
                  placeholder="请选择状态"
                  clearable
                  @change="onSearch"
                >
                  <el-option label="待认领" :value="0" />
                  <el-option label="已认领" :value="1" />
                  <el-option label="已完成" :value="2" />
                  <el-option label="已关闭" :value="3" />
                  <el-option label="已过期" :value="4" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="发布时间">
                <el-date-picker
                  v-model="searchForm.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="onSearch"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="发布地点">
                <el-input
                  v-model="searchForm.location"
                  placeholder="请输入地点"
                  clearable
                  @input="onSearch"
                />
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="排序方式">
                <el-select 
                  v-model="searchForm.sortBy" 
                  placeholder="请选择排序"
                  @change="onSearch"
                >
                  <el-option label="发布时间（最新）" value="createTime_desc" />
                  <el-option label="发布时间（最早）" value="createTime_asc" />
                  <el-option label="更新时间（最新）" value="updateTime_desc" />
                  <el-option label="标题（A-Z）" value="title_asc" />
                  <el-option label="标题（Z-A）" value="title_desc" />
                </el-select>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item label="操作">
                <el-button @click="resetSearch">重置</el-button>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-collapse-transition>

      <!-- 搜索历史 -->
      <div v-if="searchHistory.length > 0" class="search-history">
        <div class="history-header">
          <span>搜索历史</span>
          <el-button type="text" @click="clearHistory">清空</el-button>
        </div>
        <div class="history-tags">
          <el-tag
            v-for="(item, index) in searchHistory"
            :key="index"
            @click="applyHistorySearch(item)"
            class="history-tag"
            closable
            @close="removeHistoryItem(index)"
          >
            {{ item.keyword || '高级搜索' }}
          </el-tag>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div v-if="hotSearches.length > 0" class="hot-searches">
        <div class="hot-header">
          <span>热门搜索</span>
        </div>
        <div class="hot-tags">
          <el-tag
            v-for="(item, index) in hotSearches"
            :key="index"
            @click="applyHotSearch(item)"
            class="hot-tag"
            type="info"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ArrowDown, ArrowUp, Search } from '@element-plus/icons-vue'
import { useSearchDebounce } from '@/utils/realtime'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['search'])

const isExpanded = ref(false)
const searchHistory = ref([])
const hotSearches = ref(['手机', '钱包', '身份证', '钥匙', '书包', '眼镜'])

const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: null,
  dateRange: null,
  location: '',
  sortBy: 'createTime_desc'
})

// 使用搜索防抖
const { isSearching, debouncedSearch } = useSearchDebounce(
  async (query) => {
    await handleSearch()
  },
  800
)

const toggleExpanded = () => {
  isExpanded.value = !isExpanded.value
}

const onSearch = () => {
  debouncedSearch(searchForm.keyword)
}

const handleSearch = async () => {
  const searchParams = { ...searchForm }
  
  // 处理日期范围
  if (searchParams.dateRange && searchParams.dateRange.length === 2) {
    searchParams.startDate = searchParams.dateRange[0]
    searchParams.endDate = searchParams.dateRange[1]
  }
  delete searchParams.dateRange

  // 保存搜索历史
  saveSearchHistory(searchParams)

  // 触发搜索事件
  emit('search', searchParams)
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    categoryId: null,
    status: null,
    dateRange: null,
    location: '',
    sortBy: 'createTime_desc'
  })
  handleSearch()
}

const saveSearchHistory = (params) => {
  // 避免重复保存相同的搜索
  const exists = searchHistory.value.some(item => 
    JSON.stringify(item) === JSON.stringify(params)
  )
  
  if (!exists && (params.keyword || params.categoryId || params.status)) {
    searchHistory.value.unshift(params)
    
    // 限制历史记录数量
    if (searchHistory.value.length > 10) {
      searchHistory.value = searchHistory.value.slice(0, 10)
    }
    
    // 保存到本地存储
    localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  }
}

const applyHistorySearch = (historyItem) => {
  Object.assign(searchForm, historyItem)
  handleSearch()
}

const applyHotSearch = (keyword) => {
  searchForm.keyword = keyword
  handleSearch()
}

const removeHistoryItem = (index) => {
  searchHistory.value.splice(index, 1)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
}

// 加载搜索历史
onMounted(() => {
  const saved = localStorage.getItem('searchHistory')
  if (saved) {
    try {
      searchHistory.value = JSON.parse(saved)
    } catch (e) {
      console.error('加载搜索历史失败:', e)
    }
  }
})
</script>

<style scoped>
.advanced-search {
  margin-bottom: 20px;
}

.search-card {
  border-radius: 8px;
}

.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.expand-btn {
  padding: 0;
}

.basic-search {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
}

.advanced-options {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.search-history,
.hot-searches {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.history-header,
.hot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #666;
}

.history-tags,
.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag,
.hot-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.history-tag:hover,
.hot-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>

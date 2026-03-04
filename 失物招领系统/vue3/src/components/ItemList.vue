<template>
  <div class="item-list">
    <!-- 列表头部 -->
    <div class="list-header">
      <div class="header-left">
        <span class="total-count">共 {{ total }} 条记录</span>
        <el-button-group class="view-mode">
          <el-button 
            :type="viewMode === 'card' ? 'primary' : ''"
            @click="viewMode = 'card'"
            size="small"
          >
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button 
            :type="viewMode === 'list' ? 'primary' : ''"
            @click="viewMode = 'list'"
            size="small"
          >
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
      </div>
      
      <div class="header-right">
        <el-button 
          @click="refreshData"
          :loading="loading"
          size="small"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
        
        <el-dropdown @command="handleBatchAction">
          <el-button size="small">
            批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="export">导出选中</el-dropdown-item>
              <el-dropdown-item command="delete" divided>删除选中</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && items.length === 0" class="loading-state">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="items.length === 0" class="empty-state">
      <el-empty description="暂无数据">
        <el-button type="primary" @click="$emit('refresh')">
          刷新数据
        </el-button>
      </el-empty>
    </div>

    <!-- 卡片视图 -->
    <div v-else-if="viewMode === 'card'" class="card-view">
      <el-row :gutter="20">
        <el-col 
          v-for="item in items" 
          :key="item.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          class="card-col"
        >
          <el-card 
            :class="['item-card', getStatusClass(item.status)]"
            shadow="hover"
            @click="handleItemClick(item)"
          >
            <!-- 选择框 -->
            <div class="card-checkbox">
              <el-checkbox 
                v-model="selectedItems"
                :label="item.id"
                @click.stop
              />
            </div>

            <!-- 状态标签 -->
            <div class="status-tag">
              <el-tag 
                :type="getStatusTagType(item.status)"
                size="small"
              >
                {{ getStatusText(item.status) }}
              </el-tag>
            </div>

            <!-- 图片 -->
            <div class="item-image">
              <el-image
                v-if="item.images"
                :src="getFirstImage(item.images)"
                fit="cover"
                :preview-src-list="getImageList(item.images)"
                preview-teleported
              >
                <template #error>
                  <div class="image-slot">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="image-slot">
                <el-icon><Picture /></el-icon>
              </div>
            </div>

            <!-- 内容 -->
            <div class="card-content">
              <h3 class="item-title">{{ item.title }}</h3>
              <p class="item-description">{{ item.description }}</p>
              
              <div class="item-meta">
                <div class="meta-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ getLocationText(item) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(getTimeText(item)) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><User /></el-icon>
                  <span>{{ item.contactName }}</span>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button 
                type="primary" 
                size="small"
                @click.stop="handleAction(item, 'view')"
              >
                查看详情
              </el-button>
              <el-button 
                v-if="canClaim(item)"
                type="success" 
                size="small"
                @click.stop="handleAction(item, 'claim')"
              >
                申请认领
              </el-button>
              <el-button 
                v-if="canEdit(item)"
                size="small"
                @click.stop="handleAction(item, 'edit')"
              >
                编辑
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 列表视图 -->
    <div v-else class="list-view">
      <el-table
        :data="items"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        @row-click="handleItemClick"
        row-class-name="table-row"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column label="标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title }}</span>
              <el-tag 
                :type="getStatusTagType(row.status)"
                size="small"
                class="status-tag-inline"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="描述" min-width="300">
          <template #default="{ row }">
            <el-tooltip :content="row.description" placement="top">
              <span class="description-text">{{ row.description }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column label="地点" width="150">
          <template #default="{ row }">
            {{ getLocationText(row) }}
          </template>
        </el-table-column>

        <el-table-column label="时间" width="120">
          <template #default="{ row }">
            {{ formatTime(getTimeText(row)) }}
          </template>
        </el-table-column>

        <el-table-column label="联系人" width="100">
          <template #default="{ row }">
            {{ row.contactName }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small"
              @click.stop="handleAction(row, 'view')"
            >
              查看
            </el-button>
            <el-button 
              v-if="canClaim(row)"
              type="success" 
              size="small"
              @click.stop="handleAction(row, 'claim')"
            >
              认领
            </el-button>
            <el-button 
              v-if="canEdit(row)"
              size="small"
              @click.stop="handleAction(row, 'edit')"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { 
  Grid, 
  List, 
  Refresh, 
  ArrowDown,
  Picture,
  Location,
  Clock,
  User
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  items: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  loading: {
    type: Boolean,
    default: false
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 20
  }
})

const emit = defineEmits([
  'refresh',
  'page-change',
  'size-change',
  'item-click',
  'action',
  'batch-action'
])

const userStore = useUserStore()

const viewMode = ref('card')
const selectedItems = ref([])

const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-claimed',
    2: 'status-completed',
    3: 'status-closed',
    4: 'status-expired'
  }
  return classMap[status] || ''
}

const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'info',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待认领',
    1: '已认领',
    2: '已完成',
    3: '已关闭',
    4: '已过期'
  }
  return textMap[status] || '未知'
}

const getFirstImage = (images) => {
  if (!images) return ''
  const imageList = images.split(',')
  return imageList[0]?.trim() || ''
}

const getImageList = (images) => {
  if (!images) return []
  return images.split(',').map(img => img.trim()).filter(Boolean)
}

const getLocationText = (item) => {
  return item.lostPlace || item.foundPlace || '未知地点'
}

const getTimeText = (item) => {
  return item.lostTime || item.foundTime || item.createTime
}

const formatTime = (timeString) => {
  if (!timeString) return ''
  const time = new Date(timeString)
  const now = new Date()
  const diff = now - time
  
  const day = 24 * 60 * 60 * 1000
  
  if (diff < day) {
    return '今天'
  } else if (diff < 2 * day) {
    return '昨天'
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return time.toLocaleDateString()
  }
}

const canClaim = (item) => {
  return item.status === 0 && userStore.isLoggedIn
}

const canEdit = (item) => {
  return userStore.isLoggedIn && 
         (userStore.user?.roleCode === 'ADMIN' || 
          userStore.user?.id === item.userId)
}

const refreshData = () => {
  emit('refresh')
}

const handleItemClick = (item) => {
  emit('item-click', item)
}

const handleAction = (item, action) => {
  emit('action', { item, action })
}

const handleSelectionChange = (selection) => {
  selectedItems.value = selection.map(item => item.id)
}

const handleBatchAction = (command) => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请先选择要操作的项目')
    return
  }
  
  emit('batch-action', {
    command,
    items: selectedItems.value
  })
}

const handleCurrentChange = (page) => {
  emit('page-change', page)
}

const handleSizeChange = (size) => {
  emit('size-change', size)
}
</script>

<style scoped>
.item-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 4px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.total-count {
  color: #666;
  font-size: 14px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.loading-state {
  padding: 20px;
}

.empty-state {
  padding: 40px 20px;
}

/* 卡片视图样式 */
.card-view {
  margin-bottom: 20px;
}

.card-col {
  margin-bottom: 20px;
}

.item-card {
  position: relative;
  cursor: pointer;
  transition: all 0.3s;
  height: 100%;
}

.item-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.card-checkbox {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 10;
}

.status-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
}

.item-image {
  height: 160px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.item-image .el-image {
  width: 100%;
  height: 100%;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.card-content {
  padding: 0 4px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.item-meta {
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.meta-item .el-icon {
  font-size: 14px;
}

.card-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 列表视图样式 */
.list-view {
  margin-bottom: 20px;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-tag-inline {
  flex-shrink: 0;
}

.description-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

/* 状态样式 */
.status-pending {
  border-left: 4px solid #E6A23C;
}

.status-claimed {
  border-left: 4px solid #67C23A;
}

.status-completed {
  border-left: 4px solid #409EFF;
}

.status-closed {
  border-left: 4px solid #909399;
}

.status-expired {
  border-left: 4px solid #F56C6C;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>

<template>
  <div class="my-claims">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的申请</span>
        </div>
      </template>
      
      <div class="filter-container">
        <el-select v-model="searchForm.status" placeholder="申请状态" clearable>
          <el-option label="全部" :value="null" />
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchClaims">查询</el-button>
      </div>
      
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="itemTitle" label="物品标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="物品类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.itemType === 0 ? 'success' : 'warning'">
              {{ scope.row.itemType === 0 ? '招领' : '失物' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="申请说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="申请状态" width="100">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" :formatter="formatDate" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
            <el-button 
              v-if="scope.row.status === 0" 
              size="small" 
              type="danger"
              @click="cancelClaim(scope.row)"
            >取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="600px"
    >
      <div v-if="currentClaim">
        <div class="detail-item">
          <span class="label">物品标题：</span>
          <span>{{ currentClaim.itemTitle }}</span>
        </div>
        <div class="detail-item">
          <span class="label">物品类型：</span>
          <span>{{ currentClaim.itemType === 0 ? '招领物品' : '失物信息' }}</span>
        </div>
        <div class="detail-item">
          <span class="label">申请说明：</span>
          <span>{{ currentClaim.description }}</span>
        </div>
        <div class="detail-item">
          <span class="label">申请状态：</span>
          <el-tag :type="statusTagType(currentClaim.status)">{{ statusText(currentClaim.status) }}</el-tag>
        </div>
        <div class="detail-item">
          <span class="label">申请时间：</span>
          <span>{{ formatDate(currentClaim) }}</span>
        </div>
        <div v-if="currentClaim.status !== 0" class="detail-item">
          <span class="label">审核时间：</span>
          <span>{{ currentClaim.auditTime }}</span>
        </div>
        <div v-if="currentClaim.status !== 0" class="detail-item">
          <span class="label">审核人：</span>
          <span>{{ currentClaim.auditName || currentClaim.auditUsername }}</span>
        </div>
        <div v-if="currentClaim.status !== 0" class="detail-item">
          <span class="label">审核备注：</span>
          <span>{{ currentClaim.auditRemark || '-' }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

// 列表数据
const tableData = ref([]);
const loading = ref(false);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

// 查询条件
const searchForm = ref({
  status: null
});

// 详情对话框
const detailDialogVisible = ref(false);
const currentClaim = ref(null);

// 获取申请列表
const fetchClaims = async () => {
  loading.value = true;
  try {
    await request.get('/claim/my', {
      currentPage: currentPage.value,
      size: pageSize.value,
      status: searchForm.value.status
    }, {
      onSuccess: (res) => {
        tableData.value = res.records || [];
        total.value = res.total || 0;
      }
    });
  } catch (error) {
    console.error('获取认领申请列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 查看详情
const viewDetail = (row) => {
  currentClaim.value = row;
  detailDialogVisible.value = true;
};

// 取消申请
const cancelClaim = (row) => {
  ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.put(`/claim/cancel/${row.id}`, null, {
        successMsg: '取消申请成功',
        onSuccess: () => {
          fetchClaims();
        }
      });
    } catch (error) {
      console.error('取消申请失败:', error);
    }
  }).catch(() => {});
};

// 分页
const handleCurrentChange = (page) => {
  currentPage.value = page;
  fetchClaims();
};

// 格式化日期
const formatDate = (row) => {
  return row.createTime ? row.createTime.replace('T', ' ').substring(0, 19) : '';
};

// 状态标签类型
const statusTagType = (status) => {
  switch (status) {
    case 0: return 'info';
    case 1: return 'success';
    case 2: return 'danger';
    case 3: return 'warning';
    default: return 'info';
  }
};

// 状态文字
const statusText = (status) => {
  switch (status) {
    case 0: return '待审核';
    case 1: return '已通过';
    case 2: return '已拒绝';
    case 3: return '已取消';
    default: return '未知';
  }
};

onMounted(() => {
  fetchClaims();
});
</script>

<style scoped>
.my-claims {
  padding: 20px;
}

.filter-container {
  display: flex;
  margin-bottom: 20px;
  gap: 15px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.detail-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.label {
  width: 100px;
  color: #606266;
  font-weight: bold;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 
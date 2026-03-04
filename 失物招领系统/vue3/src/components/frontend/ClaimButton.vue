<template>
  <div class="claim-button">
    <el-button 
      type="primary" 
      size="large" 
      :disabled="disabled" 
      @click="showClaimDialog"
    >
      {{ buttonText }}
    </el-button>
    
    <!-- 认领申请对话框 -->
    <el-dialog
      v-model="claimDialogVisible"
      title="申请认领"
      width="500px"
    >
      <el-form 
        ref="claimFormRef" 
        :model="claimForm" 
        :rules="rules" 
        label-width="100px"
      >
        <el-form-item 
          label="申请说明" 
          prop="description"
        >
          <el-input 
            v-model="claimForm.description" 
            type="textarea" 
            rows="5" 
            placeholder="请详细描述物品特征或遗失/拾获场景，以证明您是物品的合法所有者"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="claimDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitClaim">提交申请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import request from '@/utils/request';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';

const router = useRouter();
const userStore = useUserStore();

const props = defineProps({
  // 物品ID
  itemId: {
    type: Number,
    required: true
  },
  // 物品类型 0:招领物品, 1:失物信息
  itemType: {
    type: Number,
    required: true
  },
  // 物品状态 0:待认领, 1:已认领, 2:已关闭
  itemStatus: {
    type: Number,
    required: true
  },
  // 物品所属用户ID
  userId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['claim-success']);

// 认领对话框显示控制
const claimDialogVisible = ref(false);
const submitting = ref(false);

// 认领表单
const claimFormRef = ref(null);
const claimForm = ref({
  itemId: props.itemId,
  itemType: props.itemType,
  description: ''
});

// 表单验证规则
const rules = {
  description: [
    { required: true, message: '请输入申请说明', trigger: 'blur' },
    { min: 10, message: '申请说明至少需要10个字符', trigger: 'blur' }
  ]
};

// 按钮是否禁用（已认领/已关闭/自己发布的物品）
const disabled = computed(() => {
  // 物品已经被认领或已关闭
  if (props.itemStatus !== 0) {
    return true;
  }
  
  // 用户未登录
  if (!userStore.userInfo) {
    return false; // 未登录时不禁用，点击会跳转登录
  }
  
  // 是自己发布的物品
  if (props.userId === userStore.userInfo.id) {
    return true;
  }
  
  return false;
});

// 按钮文字
const buttonText = computed(() => {
  if (props.itemStatus === 1) {
    return '已被认领';
  }
  
  if (props.itemStatus === 2) {
    return '已关闭';
  }
  
  if (userStore.userInfo && props.userId === userStore.userInfo.id) {
    return '不可认领自己的物品';
  }
  
  return '申请认领';
});

// 显示认领对话框
const showClaimDialog = () => {
  // 检查用户是否登录
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录后再申请认领');
    router.push('/login');
    return;
  }
  
  claimDialogVisible.value = true;
};

// 提交认领申请
const submitClaim = () => {
  claimFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        await request.post('/claim', {
          itemId: props.itemId,
          itemType: props.itemType,
          description: claimForm.value.description
        }, {
          successMsg: '申请提交成功，请等待审核',
          onSuccess: () => {
            claimDialogVisible.value = false;
            claimForm.value.description = '';
            emit('claim-success');
          }
        });
      } catch (error) {
        console.error('提交认领申请失败:', error);
      } finally {
        submitting.value = false;
      }
    }
  });
};
</script>

<style scoped>
.claim-button {
  margin-top: 20px;
  text-align: center;
}
</style> 
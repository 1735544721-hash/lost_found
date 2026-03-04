<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="400px"
    :before-close="handleClose"
    center
  >
    <div class="confirm-content">
      <div class="icon-wrapper">
        <el-icon :size="48" :color="iconColor">
          <component :is="iconComponent" />
        </el-icon>
      </div>
      
      <div class="message">
        {{ message }}
      </div>
      
      <div v-if="showInput" class="input-section">
        <el-input
          v-model="inputValue"
          :placeholder="inputPlaceholder"
          :type="inputType"
          :rows="inputType === 'textarea' ? 3 : 1"
          maxlength="200"
          show-word-limit
        />
      </div>
      
      <div v-if="showDetails" class="details">
        <el-collapse>
          <el-collapse-item title="详细信息" name="details">
            <div v-html="details"></div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">
          {{ cancelText }}
        </el-button>
        <el-button 
          :type="confirmType" 
          @click="handleConfirm"
          :loading="loading"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { 
  Warning, 
  QuestionFilled, 
  InfoFilled, 
  SuccessFilled, 
  CircleCloseFilled 
} from '@element-plus/icons-vue'

const props = defineProps({
  type: {
    type: String,
    default: 'warning', // warning, info, success, error, question
    validator: (value) => ['warning', 'info', 'success', 'error', 'question'].includes(value)
  },
  title: {
    type: String,
    default: '确认操作'
  },
  message: {
    type: String,
    required: true
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  showInput: {
    type: Boolean,
    default: false
  },
  inputPlaceholder: {
    type: String,
    default: '请输入'
  },
  inputType: {
    type: String,
    default: 'text' // text, textarea, password
  },
  details: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['confirm', 'cancel', 'close'])

const visible = ref(false)
const inputValue = ref('')

const showDetails = computed(() => !!props.details)

const iconComponent = computed(() => {
  const iconMap = {
    warning: Warning,
    info: InfoFilled,
    success: SuccessFilled,
    error: CircleCloseFilled,
    question: QuestionFilled
  }
  return iconMap[props.type] || Warning
})

const iconColor = computed(() => {
  const colorMap = {
    warning: '#E6A23C',
    info: '#409EFF',
    success: '#67C23A',
    error: '#F56C6C',
    question: '#909399'
  }
  return colorMap[props.type] || '#E6A23C'
})

const confirmType = computed(() => {
  const typeMap = {
    warning: 'warning',
    info: 'primary',
    success: 'success',
    error: 'danger',
    question: 'primary'
  }
  return typeMap[props.type] || 'warning'
})

const show = () => {
  visible.value = true
  inputValue.value = ''
}

const hide = () => {
  visible.value = false
}

const handleConfirm = () => {
  const result = props.showInput ? inputValue.value : true
  emit('confirm', result)
  if (!props.loading) {
    hide()
  }
}

const handleCancel = () => {
  emit('cancel')
  hide()
}

const handleClose = () => {
  emit('close')
  hide()
}

// 暴露方法给父组件
defineExpose({
  show,
  hide
})
</script>

<style scoped>
.confirm-content {
  text-align: center;
  padding: 20px 0;
}

.icon-wrapper {
  margin-bottom: 20px;
}

.message {
  font-size: 16px;
  color: #333;
  margin-bottom: 20px;
  line-height: 1.5;
}

.input-section {
  margin: 20px 0;
  text-align: left;
}

.details {
  margin-top: 20px;
  text-align: left;
}

.dialog-footer {
  text-align: center;
}

.dialog-footer .el-button {
  margin: 0 10px;
  min-width: 80px;
}
</style>

/**
 * 实时更新工具类
 * 提供数据实时刷新、状态同步等功能
 */

import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

/**
 * 实时数据更新 Hook
 * @param {Function} fetchFunction 数据获取函数
 * @param {number} interval 更新间隔（毫秒）
 * @param {boolean} immediate 是否立即执行
 */
export function useRealTimeData(fetchFunction, interval = 30000, immediate = true) {
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)
  let timer = null

  const fetch = async () => {
    try {
      loading.value = true
      error.value = null
      const result = await fetchFunction()
      data.value = result
    } catch (err) {
      error.value = err
      console.error('实时数据更新失败:', err)
    } finally {
      loading.value = false
    }
  }

  const start = () => {
    if (timer) return
    
    if (immediate) {
      fetch()
    }
    
    timer = setInterval(fetch, interval)
  }

  const stop = () => {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  const refresh = () => {
    fetch()
  }

  onMounted(() => {
    start()
  })

  onUnmounted(() => {
    stop()
  })

  return {
    data,
    loading,
    error,
    start,
    stop,
    refresh
  }
}

/**
 * 通知轮询 Hook
 */
export function useNotificationPolling() {
  const unreadCount = ref(0)
  const notifications = ref([])
  
  const { data: notificationData, refresh: refreshNotifications } = useRealTimeData(
    async () => {
      try {
        let result = 0
        await request.get('/notification/unread-count', {}, {
          onSuccess: (data) => {
            result = data || 0
          }
        })
        return result
      } catch (error) {
        console.error('获取未读通知数量失败:', error)
        return 0
      }
    },
    60000 // 每分钟检查一次
  )

  // 监听通知数据变化
  watch(() => notificationData.value, (newCount) => {
    if (newCount !== null && newCount !== unreadCount.value) {
      if (newCount > unreadCount.value && unreadCount.value > 0) {
        ElMessage.info(`您有 ${newCount - unreadCount.value} 条新通知`)
      }
      unreadCount.value = newCount
    }
  })

  return {
    unreadCount,
    notifications,
    refreshNotifications
  }
}

/**
 * 页面可见性检测 Hook
 * 当页面重新可见时自动刷新数据
 */
export function usePageVisibility(callback) {
  const isVisible = ref(!document.hidden)

  const handleVisibilityChange = () => {
    const visible = !document.hidden
    isVisible.value = visible
    
    if (visible && callback) {
      callback()
    }
  }

  onMounted(() => {
    document.addEventListener('visibilitychange', handleVisibilityChange)
  })

  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })

  return {
    isVisible
  }
}

/**
 * 自动保存 Hook
 * 在用户输入时自动保存草稿
 */
export function useAutoSave(saveFunction, delay = 2000) {
  let timer = null
  const isSaving = ref(false)
  const lastSaved = ref(null)

  const save = async (data) => {
    try {
      isSaving.value = true
      await saveFunction(data)
      lastSaved.value = new Date()
      ElMessage.success('草稿已自动保存')
    } catch (error) {
      console.error('自动保存失败:', error)
    } finally {
      isSaving.value = false
    }
  }

  const debouncedSave = (data) => {
    if (timer) {
      clearTimeout(timer)
    }
    
    timer = setTimeout(() => {
      save(data)
    }, delay)
  }

  const cancelSave = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  onUnmounted(() => {
    cancelSave()
  })

  return {
    debouncedSave,
    cancelSave,
    isSaving,
    lastSaved
  }
}

/**
 * 操作确认 Hook
 */
export function useConfirm() {
  const confirm = async (message, title = '确认操作', options = {}) => {
    const defaultOptions = {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      ...options
    }

    try {
      await ElMessageBox.confirm(message, title, defaultOptions)
      return true
    } catch {
      return false
    }
  }

  const confirmDelete = async (itemName = '此项') => {
    return await confirm(
      `确定要删除${itemName}吗？此操作不可撤销。`,
      '确认删除',
      { type: 'error' }
    )
  }

  const confirmSubmit = async (message = '确定要提交吗？') => {
    return await confirm(message, '确认提交', { type: 'info' })
  }

  return {
    confirm,
    confirmDelete,
    confirmSubmit
  }
}

/**
 * 搜索防抖 Hook
 */
export function useSearchDebounce(searchFunction, delay = 500) {
  const searchText = ref('')
  const isSearching = ref(false)
  let timer = null

  const search = async (query) => {
    try {
      isSearching.value = true
      await searchFunction(query)
    } catch (error) {
      console.error('搜索失败:', error)
    } finally {
      isSearching.value = false
    }
  }

  const debouncedSearch = (query) => {
    if (timer) {
      clearTimeout(timer)
    }
    
    timer = setTimeout(() => {
      search(query)
    }, delay)
  }

  // 监听搜索文本变化
  watch(searchText, (newValue) => {
    debouncedSearch(newValue)
  })

  onUnmounted(() => {
    if (timer) {
      clearTimeout(timer)
    }
  })

  return {
    searchText,
    isSearching,
    debouncedSearch
  }
}

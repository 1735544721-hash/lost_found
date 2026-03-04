import axios from 'axios'
import { ElMessage } from 'element-plus'



/**
 * 封装的 axios 请求工具
 * 
 * 特性：
 * 1. 自动携带 token
 * 2. 统一的错误处理
 * 3. 支持自定义成功/错误提示
 * 4. 支持自定义成功/错误回调
 * 
 * 配置选项：
 * @param {boolean} showDefaultMsg - 是否显示默认的成功/错误提示，默认为 true
 * @param {string} successMsg - 自定义成功提示消息
 * @param {string} errorMsg - 自定义错误提示消息
 * @param {Function} onSuccess - 成功回调函数，参数为响应数据
 * @param {Function} onError - 错误回调函数，参数为错误信息
 */

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api', // API 的基础URL
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      // 直接使用 token，不加 Bearer 前缀
      config.headers['token'] = token
    }
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    const config = response.config || {}

    // 如果请求配置中指定了不需要默认的成功/错误提示，则跳过
    const showDefaultMsg = config.showDefaultMsg !== false

    // 检查响应状态
    if (res.code === "200") {
      try {
        // 自定义成功提示
        if (config.successMsg) {
          ElMessage.success(config.successMsg)
        } else if (showDefaultMsg && config.method && config.method.toLowerCase() !== 'get') {
          // 非 GET 请求默认显示操作成功
          ElMessage.success('操作成功')
        }
        
        // 自定义成功回调
        if (typeof config.onSuccess === 'function') {
          config.onSuccess(res.data)
        }
        
        return res.data
      } catch (err) {
        console.error('Success handler error:', err)
        return res.data
      }
    } else {
      // 错误处理
      try {
        // 自定义错误提示
        if (config.errorMsg) {
          ElMessage.error(config.errorMsg)
        } else if (showDefaultMsg) {
          // 根据后端返回的错误码显示对应的错误信息
          let errorMessage = res.msg || '请求失败'
          
          switch (res.code) {
            case "401":
              errorMessage = '未登录或登录已过期，请重新登录'
              break
            case "403":
              errorMessage = '没有权限进行此操作'
              break
            case "404":
              errorMessage = '请求的资源不存在'
              break
            case "500":
              errorMessage = '服务器内部错误'
              break
            default:
              // 如果后端返回了具体错误信息，优先使用后端的错误信息
              errorMessage = res.msg || `请求失败(${res.code})`
          }
          
          ElMessage.error(errorMessage)
        }
        
        // 自定义错误回调
        if (typeof config.onError === 'function') {
          config.onError(res)
        }
        
        // 返回一个被拒绝的 Promise，但保留原始错误信息
        return Promise.reject({
          code: res.code,
          message: res.msg || '请求失败',
          data: res.data,
          type: 'business' // 标记这是业务错误
        })
      } catch (err) {
        console.error('Error handler error:', err)
        return Promise.reject(err)
      }
    }
  },
  error => {
    const config = error.config || {}
    
    // 错误处理
    if (typeof config.onError === 'function') {
      try {
        config.onError(error)
      } catch (err) {
        console.error('Error callback error:', err)
      }
    }
    
    // 显示错误信息
    if (config.showDefaultMsg !== false) {
      let message = config.errorMsg || '网络请求失败，请稍后重试'
      
      if (error.response) {
        switch (error.response.status) {
          case 400:
            message = '请求参数错误'
            break
          case 401:
            message = '未授权，请重新登录'
            break
          case 403:
            message = '拒绝访问'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 408:
            message = '请求超时'
            break
          case 500:
            message = '服务器内部错误'
            break
          case 501:
            message = '服务未实现'
            break
          case 502:
            message = '网关错误'
            break
          case 503:
            message = '服务不可用'
            break
          case 504:
            message = '网关超时'
            break
          default:
            message = error.response.data?.msg || `请求失败(${error.response.status})`
        }
      } else if (error.code === 'ECONNABORTED') {
        message = '请求超时，请检查网络连接'
      } else if (error.message?.includes('Network Error')) {
        message = '网络连接失败，请检查网络设置'
      }
      
      // 使用 ElMessage 显示错误，并设置较长的显示时间
      ElMessage({
        message: message,
        type: 'error',
        duration: 5000,
        showClose: true
      })
    }
    
    // 返回一个被拒绝的 Promise，但包含更多信息
    return Promise.reject({
      code: error.response?.status,
      message: error.message,
      data: error.response?.data,
      type: 'http', // 标记这是 HTTP 错误
      originalError: error
    })
  }
)

// 扩展请求方法
const request = {
  get(url, params, config = {}) {
    return service.get(url, { params, ...config })
  },
  
  post(url, data, config = {}) {
    return service.post(url, data, config)
  },
  
  put(url, data, config = {}) {
    return service.put(url, data, config)
  },
  
  delete(url, config = {}) {
    return service.delete(url, config)
  }
}

/**
 * 请求方法使用示例：
 * 
 * 1. 基础请求：
 * // GET 请求
 * request.get('/api/users', { page: 1 })
 * 
 * // POST 请求
 * request.post('/api/users', { name: 'Tom', age: 20 })
 * 
 * // PUT 请求
 * request.put('/api/users/1', { name: 'Tom' })
 * 
 * // DELETE 请求
 * request.delete('/api/users/1')
 * 
 * 2. 自定义提示消息：
 * request.post('/api/users', data, {
 *   successMsg: '添加用户成功！',
 *   errorMsg: '添加用户失败，请重试'
 * })
 * 
 * 3. 关闭默认提示：
 * request.post('/api/users', data, {
 *   showDefaultMsg: false
 * })
 * 
 * 4. 使用回调函数：
 * request.post('/api/users', data, {
 *   onSuccess: (data) => {
 *     console.log('请求成功：', data)
 *   },
 *   onError: (error) => {
 *     console.log('请求失败：', error)
 *   }
 * })
 * 
 * 5. 完整示例：
 * request.post('/api/users', data, {
 *   successMsg: '添加成功',
 *   errorMsg: '添加失败',
 *   showDefaultMsg: true,
 *   onSuccess: (data) => {
 *     // 处理成功逻辑
 *   },
 *   onError: (error) => {
 *     // 处理错误逻辑
 *   }
 * })
 */

export default request 
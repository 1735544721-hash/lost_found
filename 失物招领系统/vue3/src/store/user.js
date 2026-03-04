import { defineStore } from 'pinia'
import request from '@/utils/request'
import { login, register } from '@/api/user'
// import { setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
 
  }),

  getters: {
    // 判断是否登录
    isLoggedIn: (state) => !!state.token,
    // 判断是否是管理员
    isAdmin: (state) => state.userInfo?.roleCode === 'ADMIN',
    // 判断是否是普通用户
    isUser: (state) => state.userInfo?.roleCode === 'USER'
  },

  actions: {
    // 更新用户信息
    updateUserInfo(data) {
      if (!data) return
      this.userInfo = data
      localStorage.setItem('userInfo', JSON.stringify(data))
    },
    
    setUserInfo(data) {
      if (!data) return
      
      this.userInfo = data.userInfo || data
      this.token = data.token
      this.role = data.roleCode
      
      // 存储到 LocalStorage
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      localStorage.setItem('token', this.token || '')
      localStorage.setItem('role', this.role || '')
    },
    clearUserInfo() {
      this.userInfo = null
      this.token = ''
      this.role = ''
 
      // 清除 LocalStorage
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
      localStorage.removeItem('role')
     
    },

    // 获取用户信息和菜单 - 从localStorage恢复
    async getUserInfo() {
      const userInfo = JSON.parse(localStorage.getItem('userInfo'))
 
      
      if (userInfo) {
        this.userInfo = userInfo
      
        return { userInfo }
      }
      
      // 如果没有缓存的数据，清除状态并抛出错误
      this.clearUserInfo()
      throw new Error('No cached user info')
    },
    // 登录
    async login(loginForm) {
      try {
        const res = await request.post('/user/login', loginForm)
        this.setUserInfo(res)
        return res
      } catch (error) {
        this.clearUserInfo()
        throw error
      }
    },
    // 注册

    // 退出登录
    async logout() {
      this.clearUserInfo()
    },
    // 检查登录状态
    checkLoginStatus() {
      return !!this.token
    }
  }
})
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'

const service = axios.create({
  baseURL: '',
  timeout: 15000
})

let redirecting = false

service.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    const code = res.code
    if (code === 200 || code === '200') {
      return res
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(res)
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    if (status === 401) {
      if (!redirecting) {
        redirecting = true
        removeToken()
        ElMessage.error(data?.msg || '登录已失效，请重新登录')
        router.replace(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
        setTimeout(() => {
          redirecting = false
        }, 1500)
      }
    } else if (status === 403) {
      ElMessage.error(data?.msg || '无操作权限')
    } else {
      ElMessage.error(data?.msg || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service

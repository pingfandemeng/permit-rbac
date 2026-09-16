import request from '@/utils/request'

export function getCaptcha() {
  return request({ url: '/api/auth/captcha', method: 'get' })
}

export function login(data) {
  return request({ url: '/api/auth/login', method: 'post', data })
}

export function logout() {
  return request({ url: '/api/auth/logout', method: 'post' })
}

export function getAuthInfo() {
  return request({ url: '/api/auth/info', method: 'get' })
}

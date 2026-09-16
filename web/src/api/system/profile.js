import request from '@/utils/request'

export function getProfile() {
  return request({ url: '/api/system/profile', method: 'get' })
}

export function updateProfile(data) {
  return request({ url: '/api/system/profile', method: 'put', data })
}

export function updatePassword(data) {
  return request({ url: '/api/system/profile/password', method: 'put', data })
}

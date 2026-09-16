import request from '@/utils/request'

export function listConfig(query) {
  return request({ url: '/api/system/config', method: 'get', params: query })
}

export function updateConfig(data) {
  return request({ url: '/api/system/config', method: 'put', data })
}

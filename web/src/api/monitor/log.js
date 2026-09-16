import request from '@/utils/request'

export function listLoginLog(query) {
  return request({ url: '/api/monitor/loginlog', method: 'get', params: query })
}

export function listOperLog(query) {
  return request({ url: '/api/monitor/operlog', method: 'get', params: query })
}

export function listOnline(query) {
  return request({ url: '/api/monitor/online', method: 'get', params: query })
}

export function forceLogout(userId) {
  return request({ url: `/api/monitor/online/${userId}`, method: 'delete' })
}

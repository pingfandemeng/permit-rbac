import request from '@/utils/request'

export function listUser(query) {
  return request({ url: '/api/system/user', method: 'get', params: query })
}

export function getUser(userId) {
  return request({ url: `/api/system/user/${userId}`, method: 'get' })
}

export function addUser(data) {
  return request({ url: '/api/system/user', method: 'post', data })
}

export function updateUser(data) {
  return request({ url: '/api/system/user', method: 'put', data })
}

export function deleteUser(userId) {
  return request({ url: `/api/system/user/${userId}`, method: 'delete' })
}

export function changeUserStatus(userId, status) {
  return request({ url: `/api/system/user/${userId}/status`, method: 'put', data: { userId, status } })
}

export function resetUserPwd(userId) {
  return request({ url: '/api/system/user/resetPwd', method: 'put', data: { userId } })
}

export function assignUserRoles(userId, roleIds) {
  return request({ url: `/api/system/user/${userId}/roles`, method: 'put', data: { userId, roleIds } })
}

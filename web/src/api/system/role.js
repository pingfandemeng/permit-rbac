import request from '@/utils/request'

export function listRole(query) {
  return request({ url: '/api/system/role', method: 'get', params: query })
}

export function roleOptions() {
  return request({ url: '/api/system/role/options', method: 'get' })
}

export function getRole(roleId) {
  return request({ url: `/api/system/role/${roleId}`, method: 'get' })
}

export function addRole(data) {
  return request({ url: '/api/system/role', method: 'post', data })
}

export function updateRole(data) {
  return request({ url: '/api/system/role', method: 'put', data })
}

export function deleteRole(roleId) {
  return request({ url: `/api/system/role/${roleId}`, method: 'delete' })
}

export function assignRoleMenus(roleId, menuIds) {
  return request({ url: `/api/system/role/${roleId}/menus`, method: 'put', data: { menuIds } })
}

export function assignRoleDataScope(roleId, data) {
  return request({ url: `/api/system/role/${roleId}/dataScope`, method: 'put', data })
}

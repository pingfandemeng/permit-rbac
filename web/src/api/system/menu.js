import request from '@/utils/request'

export function listMenu() {
  return request({ url: '/api/system/menu', method: 'get' })
}

export function menuTree() {
  return request({ url: '/api/system/menu/tree', method: 'get' })
}

export function addMenu(data) {
  return request({ url: '/api/system/menu', method: 'post', data })
}

export function updateMenu(data) {
  return request({ url: '/api/system/menu', method: 'put', data })
}

export function deleteMenu(menuId) {
  return request({ url: `/api/system/menu/${menuId}`, method: 'delete' })
}

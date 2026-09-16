import request from '@/utils/request'

export function listDept() {
  return request({ url: '/api/system/dept', method: 'get' })
}

export function deptTree() {
  return request({ url: '/api/system/dept/tree', method: 'get' })
}

export function addDept(data) {
  return request({ url: '/api/system/dept', method: 'post', data })
}

export function updateDept(data) {
  return request({ url: '/api/system/dept', method: 'put', data })
}

export function deleteDept(deptId) {
  return request({ url: `/api/system/dept/${deptId}`, method: 'delete' })
}

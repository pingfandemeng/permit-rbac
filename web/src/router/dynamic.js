const modules = import.meta.glob('/src/views/**/*.vue')

export function resolveComponent(component) {
  if (!component || component === 'Layout') {
    return () => import('@/layout/index.vue')
  }
  if (component === 'ParentView') {
    return () => import('@/layout/ParentView.vue')
  }
  const key = `/src/views/${component}.vue`
  return modules[key] || (() => import('@/views/error/404.vue'))
}

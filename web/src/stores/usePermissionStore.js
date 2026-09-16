import { defineStore } from 'pinia'
import { resolveComponent } from '@/router/dynamic'

function filterHidden(routers) {
  return (routers || []).filter((item) => !item.hidden)
}

function convertRoutes(routers, parentPath = '') {
  const result = []
  ;(routers || []).forEach((item) => {
    const rawPath = item.path || ''
    if (rawPath === 'profile' || rawPath === '/profile') {
      return
    }
    const route = {
      path: item.path,
      name: item.name,
      component: resolveComponent(item.component),
      meta: {
        title: item.meta?.title,
        icon: item.meta?.icon,
        hidden: !!item.hidden
      },
      hidden: !!item.hidden
    }
    if (item.children && item.children.length) {
      route.children = convertRoutes(item.children, item.path)
      if (!route.redirect && route.children.length) {
        const first = route.children.find((c) => !c.hidden) || route.children[0]
        const base = item.path.startsWith('/') ? item.path : `${parentPath}/${item.path}`
        route.redirect = `${base.replace(/\/$/, '')}/${first.path}`.replace(/\/+/g, '/')
      }
    }
    result.push(route)
  })
  return result
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routers: [],
    sidebar: [],
    added: false
  }),
  actions: {
    generate(rawRouters) {
      const access = convertRoutes(rawRouters)
      this.routers = access
      this.sidebar = filterHidden(access)
      this.added = true
      return access
    },
    reset() {
      this.routers = []
      this.sidebar = []
      this.added = false
    }
  }
})

import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores/useUserStore'
import { usePermissionStore } from '@/stores/usePermissionStore'

NProgress.configure({ showSpinner: false })

const WHITE = ['/login']

export function setupPermission(router) {
  router.beforeEach(async (to, from, next) => {
    NProgress.start()
    const token = getToken()
    if (!token) {
      if (WHITE.includes(to.path) || to.meta.public) {
        next()
        return
      }
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      return
    }
    if (to.path === '/login') {
      next({ path: '/' })
      return
    }
    const userStore = useUserStore()
    const permissionStore = usePermissionStore()
    if (!permissionStore.added) {
      try {
        const info = await userStore.loadInfo()
        if (info.pwdResetRequired && to.path !== '/reset-password') {
          next('/reset-password')
          return
        }
        const access = permissionStore.generate(info.routers || [])
        access.forEach((route) => router.addRoute(route))
        router.addRoute({
          path: '/:pathMatch(.*)*',
          name: 'NotFound',
          component: () => import('@/views/error/404.vue'),
          meta: { hidden: true }
        })
        next({ ...to, replace: true })
      } catch (e) {
        await userStore.logout()
        permissionStore.reset()
        next('/login')
      }
      return
    }
    if (userStore.pwdResetRequired && to.path !== '/reset-password') {
      next('/reset-password')
      return
    }
    next()
  })
  router.afterEach(() => {
    NProgress.done()
  })
}

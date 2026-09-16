import { defineStore } from 'pinia'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { getAuthInfo, login as loginApi, logout as logoutApi } from '@/api/auth'
import { encryptPassword } from '@/utils/crypto'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userId: null,
    username: '',
    nickname: '',
    roles: [],
    permissions: [],
    pwdResetRequired: false,
    publicKey: ''
  }),
  persist: {
    paths: ['token', 'username', 'nickname']
  },
  actions: {
    async login(form, publicKey) {
      const password = encryptPassword(form.password, publicKey)
      const res = await loginApi({
        username: form.username,
        password,
        captchaCode: form.captchaCode,
        captchaKey: form.captchaKey
      })
      this.token = res.data.token
      this.pwdResetRequired = !!res.data.pwdResetRequired
      setToken(this.token)
      return res.data
    },
    async loadInfo() {
      const res = await getAuthInfo()
      const data = res.data
      this.userId = data.userId
      this.username = data.username
      this.nickname = data.nickname
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      this.pwdResetRequired = !!data.pwdResetRequired
      return data
    },
    hasPermission(codes) {
      if (!codes || !codes.length) {
        return true
      }
      if (this.permissions.includes('*:*:*')) {
        return true
      }
      return codes.some((code) => this.permissions.includes(code))
    },
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        /* ignore */
      }
      this.token = ''
      this.permissions = []
      this.roles = []
      removeToken()
    }
  }
})

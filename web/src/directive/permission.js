import { useUserStore } from '@/stores/useUserStore'

export default {
  mounted(el, binding) {
    const codes = binding.value || []
    const store = useUserStore()
    if (codes.length && !store.hasPermission(codes)) {
      el.parentNode?.removeChild(el)
    }
  }
}

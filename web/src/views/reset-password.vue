<template>
  <div class="page-card reset-card">
    <h2>修改密码</h2>
    <p class="desc">首次登录或管理员重置后必须修改密码。完成后需使用新密码重新登录。</p>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 460px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">保存并重新登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { updatePassword } from '@/api/system/profile'
import { encryptPassword } from '@/utils/crypto'
import { useUserStore } from '@/stores/useUserStore'
import { usePermissionStore } from '@/stores/usePermissionStore'
import { resetRouter } from '@/router'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const formRef = ref()
const loading = ref(false)
const publicKey = ref('')
const form = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }]
}

onMounted(async () => {
  const res = await getCaptcha()
  publicKey.value = res.data.publicKey
})

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await updatePassword({
      oldPassword: encryptPassword(form.oldPassword, publicKey.value),
      newPassword: encryptPassword(form.newPassword, publicKey.value),
      confirmPassword: encryptPassword(form.confirmPassword, publicKey.value)
    })
    ElMessage.success('密码已更新，请重新登录')
    await userStore.logout()
    permissionStore.reset()
    resetRouter()
    router.replace('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-card { max-width: 640px; margin: 40px auto; }
.desc { color: #6b7c86; margin-bottom: 24px; }
</style>

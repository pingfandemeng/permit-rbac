<template>
  <div class="login-page">
    <div class="login-panel">
      <div class="intro">
        <h1>权限管理平台</h1>
        <p>统一账号、角色、菜单与数据范围。管理员在此完成授权，业务系统复用同一登录态。</p>
        <ul>
          <li>图形验证码登录 · Sa-Token 会话</li>
          <li>按钮级权限与数据范围拦截</li>
          <li>登录 / 操作审计可追溯</li>
        </ul>
      </div>
      <div class="form-wrap">
        <h2>登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="onSubmit">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="密码" prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="captchaCode">
            <div class="captcha-row">
              <el-input v-model="form.captchaCode" placeholder="验证码" />
              <img v-if="captchaImg" :src="captchaImg" class="captcha-img" alt="captcha" @click="loadCaptcha" />
            </div>
          </el-form-item>
          <el-button type="primary" :loading="loading" class="submit" @click="onSubmit">登 录</el-button>
        </el-form>
        <p class="hint">初始账号 admin / Admin@123456，首次登录须修改密码。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/useUserStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const captchaImg = ref('')
const publicKey = ref('')
const form = reactive({
  username: 'admin',
  password: 'Admin@123456',
  captchaCode: '',
  captchaKey: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function loadCaptcha() {
  const res = await getCaptcha()
  form.captchaKey = res.data.captchaKey
  captchaImg.value = res.data.img
  publicKey.value = res.data.publicKey
  form.captchaCode = ''
}

async function onSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await userStore.login(form, publicKey.value)
    ElMessage.success('登录成功')
    if (data.pwdResetRequired) {
      router.replace('/reset-password')
    } else {
      router.replace(route.query.redirect || '/')
    }
  } catch (e) {
    await loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(loadCaptcha)
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 20%, rgba(47, 158, 143, 0.18), transparent 40%),
    linear-gradient(135deg, #10232d 0%, #1d3b48 100%);
}
.login-panel {
  width: min(920px, calc(100vw - 32px));
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.28);
}
.intro {
  background: #16313f;
  color: #e8f4f1;
  padding: 48px 40px;
}
.intro h1 {
  margin: 0 0 16px;
  font-size: 28px;
}
.intro p, .intro li {
  color: #b9cdc8;
  line-height: 1.7;
}
.form-wrap {
  padding: 48px 40px;
}
.form-wrap h2 {
  margin: 0 0 24px;
}
.submit {
  width: 100%;
  height: 40px;
}
.captcha-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.captcha-img {
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
}
.hint {
  margin-top: 16px;
  color: #7a8a94;
  font-size: 12px;
}
@media (max-width: 768px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
  .intro {
    display: none;
  }
}
</style>

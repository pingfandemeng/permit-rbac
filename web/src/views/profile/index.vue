<template>
  <div class="page-card" v-loading="loading">
    <h2>个人中心</h2>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ profile.nickname }}</el-descriptions-item>
      <el-descriptions-item label="部门">{{ profile.deptName }}</el-descriptions-item>
      <el-descriptions-item label="手机号">{{ profile.phone }}</el-descriptions-item>
      <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ (profile.roles || []).join('、') }}</el-descriptions-item>
    </el-descriptions>
    <h3 style="margin-top:24px">权限标识</h3>
    <el-tag v-for="p in profile.permissions || []" :key="p" style="margin: 0 8px 8px 0">{{ p }}</el-tag>
    <h3 style="margin-top:24px">修改资料</h3>
    <el-form :model="form" label-width="80px" style="max-width:480px">
      <el-form-item label="姓名"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      <el-form-item><el-button type="primary" @click="save">保存</el-button></el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '@/api/system/profile'
const loading = ref(false)
const profile = ref({})
const form = reactive({ nickname: '', phone: '', email: '' })
async function load() {
  loading.value = true
  try {
    const res = await getProfile()
    profile.value = res.data
    Object.assign(form, { nickname: res.data.nickname, phone: res.data.phone, email: res.data.email })
  } finally { loading.value = false }
}
async function save() {
  await updateProfile(form)
  ElMessage.success('已保存')
  load()
}
onMounted(load)
</script>

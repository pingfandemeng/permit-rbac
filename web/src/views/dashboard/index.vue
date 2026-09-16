<template>
  <div class="page-card">
    <h2>工作台</h2>
    <p class="welcome">你好，{{ userStore.nickname || userStore.username }}。从左侧进入用户、角色、菜单与部门配置。</p>
    <el-row :gutter="16">
      <el-col :xs="24" :sm="8">
        <div class="stat">
          <div class="label">当前账号</div>
          <div class="value">{{ userStore.username }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat">
          <div class="label">角色</div>
          <div class="value">{{ (userStore.roles || []).join('、') || '—' }}</div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="8">
        <div class="stat">
          <div class="label">权限标识</div>
          <div class="value">{{ userStore.permissions?.length || 0 }}</div>
        </div>
      </el-col>
    </el-row>
    <el-alert
      title="冒烟建议：创建角色并勾选菜单 → 新建用户分配该角色 → 退出后用新用户登录，确认菜单与数据范围。"
      type="info"
      show-icon
      :closable="false"
      style="margin-top: 20px"
    />
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/useUserStore'
const userStore = useUserStore()
</script>

<style scoped>
.welcome { color: #5d6e78; margin-bottom: 20px; }
.stat {
  background: #f7faf9;
  border: 1px solid #e4eeeb;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}
.label { color: #7a8a94; font-size: 13px; }
.value { margin-top: 8px; font-size: 20px; font-weight: 600; }
</style>

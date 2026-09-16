<template>
  <div class="layout">
    <aside class="layout-aside" :class="{ collapsed: appStore.collapsed }">
      <div class="brand">
        <span class="brand-mark">P</span>
        <span v-if="!appStore.collapsed" class="brand-text">权限管理平台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="active"
          :collapse="appStore.collapsed"
          background-color="#16313f"
          text-color="#c5d4dc"
          active-text-color="#5ee0c8"
          router
          unique-opened
        >
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <template v-for="item in permissionStore.sidebar" :key="item.path">
            <el-sub-menu v-if="item.children && item.children.length" :index="item.path">
              <template #title>
                <el-icon><component :is="iconOf(item.meta?.icon)" /></el-icon>
                <span>{{ item.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in item.children.filter((c) => !c.hidden)"
                :key="fullPath(item.path, child.path)"
                :index="fullPath(item.path, child.path)"
              >
                {{ child.meta?.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else-if="!item.hidden" :index="item.path">
              <el-icon><component :is="iconOf(item.meta?.icon)" /></el-icon>
              <span>{{ item.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>
    <section class="layout-main">
      <header class="layout-header">
        <div class="left">
          <el-icon class="fold" @click="appStore.toggle()"><Fold v-if="!appStore.collapsed" /><Expand v-else /></el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta?.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-dropdown @command="onCommand">
            <span class="user">
              {{ userStore.nickname || userStore.username }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="app-main">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, Expand, Fold, HomeFilled, Menu, Monitor, Setting, User } from '@element-plus/icons-vue'
import { useAppStore } from '@/stores/useAppStore'
import { useUserStore } from '@/stores/useUserStore'
import { usePermissionStore } from '@/stores/usePermissionStore'
import { resetRouter } from '@/router'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const active = computed(() => route.path)

function fullPath(parent, child) {
  if (child.startsWith('/')) return child
  const base = parent.startsWith('/') ? parent : `/${parent}`
  return `${base}/${child}`.replace(/\/+/g, '/')
}

function iconOf(name) {
  const map = { setting: Setting, monitor: Monitor, user: User, system: Setting }
  return map[name] || Menu
}

async function onCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'password') {
    router.push('/reset-password')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '提示', { type: 'warning' })
    await userStore.logout()
    permissionStore.reset()
    resetRouter()
    router.replace('/login')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}
.layout-aside {
  width: 220px;
  background: #16313f;
  color: #fff;
  transition: width 0.2s;
  display: flex;
  flex-direction: column;
}
.layout-aside.collapsed {
  width: 64px;
}
.brand {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.brand-mark {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #2f9e8f;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.brand-text {
  font-weight: 600;
  letter-spacing: 0.5px;
}
.layout-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.layout-header {
  height: 56px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e8edf1;
}
.left, .right, .user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.fold {
  cursor: pointer;
  font-size: 18px;
}
.user {
  cursor: pointer;
}
:deep(.el-menu) {
  border-right: none;
}
</style>

<template>
  <div class="page-card">
    <el-form :inline="true" :model="query" class="query-form" @submit.prevent="load">
      <el-form-item label="用户名">
        <el-input v-model="query.username" clearable />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="query.phone" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="toolbar">
      <el-button v-permission="['system:user:add']" type="primary" @click="open(null)">新增用户</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="username" label="用户名" min-width="110" />
      <el-table-column prop="nickname" label="姓名" min-width="110" />
      <el-table-column prop="deptName" label="部门" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="['system:user:edit']" link type="primary" @click="open(row.userId)">编辑</el-button>
          <el-button v-permission="['system:user:edit']" link type="primary" @click="toggle(row)">{{ row.status === '0' ? '停用' : '启用' }}</el-button>
          <el-button v-permission="['system:user:resetPwd']" link type="primary" @click="resetPwd(row)">重置密码</el-button>
          <el-button v-permission="['system:user:remove']" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 12px; justify-content: flex-end"
      @change="load"
    />
    <UserDialog v-model="dialogVisible" :user-id="currentId" @success="load" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { changeUserStatus, deleteUser, listUser, resetUserPwd } from '@/api/system/user'
import UserDialog from './UserDialog.vue'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentId = ref(null)
const query = reactive({ username: '', phone: '', status: '', pageNum: 1, pageSize: 10 })

async function load() {
  loading.value = true
  try {
    const res = await listUser(query)
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}
function reset() {
  query.username = ''
  query.phone = ''
  query.status = ''
  query.pageNum = 1
  load()
}
function open(id) {
  currentId.value = id
  dialogVisible.value = true
}
async function toggle(row) {
  await changeUserStatus(row.userId, row.status === '0' ? '1' : '0')
  ElMessage.success('已更新状态')
  load()
}
async function resetPwd(row) {
  await ElMessageBox.confirm(`将 ${row.username} 的密码重置为系统默认密码？`, '重置密码', { type: 'warning' })
  await resetUserPwd(row.userId)
  ElMessage.success('已重置，用户下次登录需修改密码')
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.username}？`, '提示', { type: 'warning' })
  await deleteUser(row.userId)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

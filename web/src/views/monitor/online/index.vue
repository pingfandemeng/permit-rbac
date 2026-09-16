<template>
  <div class="page-card">
    <el-table :data="rows" border>
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="nickname" label="姓名" />
      <el-table-column prop="loginIp" label="登录 IP" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button v-permission="['monitor:online:forceLogout']" link type="danger" @click="kick(row)">强制下线</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" style="margin-top:12px;justify-content:flex-end" @change="load" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { forceLogout, listOnline } from '@/api/monitor/log'
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })
async function load() {
  const res = await listOnline(query)
  rows.value = res.data.rows || []
  total.value = res.data.total || 0
}
async function kick(row) {
  await ElMessageBox.confirm(`强制下线 ${row.username}？`, '提示', { type: 'warning' })
  await forceLogout(row.userId)
  ElMessage.success('已下线')
  load()
}
onMounted(load)
</script>

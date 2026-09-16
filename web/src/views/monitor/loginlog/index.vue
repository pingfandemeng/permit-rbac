<template>
  <div class="page-card">
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="账号"><el-input v-model="query.username" clearable /></el-form-item>
      <el-form-item label="结果">
        <el-select v-model="query.status" clearable style="width:120px">
          <el-option label="成功" value="0" />
          <el-option label="失败" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="rows" border>
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="loginIp" label="IP" />
      <el-table-column prop="browser" label="浏览器" />
      <el-table-column prop="os" label="系统" />
      <el-table-column prop="status" label="结果" width="80">
        <template #default="{ row }">{{ row.status === '0' ? '成功' : '失败' }}</template>
      </el-table-column>
      <el-table-column prop="msg" label="说明" />
      <el-table-column prop="loginTime" label="时间" width="170" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" style="margin-top:12px;justify-content:flex-end" @change="load" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listLoginLog } from '@/api/monitor/log'
const rows = ref([])
const total = ref(0)
const query = reactive({ username: '', status: '', pageNum: 1, pageSize: 10 })
async function load() {
  const res = await listLoginLog(query)
  rows.value = res.data.rows || []
  total.value = res.data.total || 0
}
onMounted(load)
</script>

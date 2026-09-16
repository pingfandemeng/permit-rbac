<template>
  <div class="page-card">
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="操作人"><el-input v-model="query.operName" clearable /></el-form-item>
      <el-form-item label="模块"><el-input v-model="query.title" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="rows" border>
      <el-table-column prop="title" label="模块" />
      <el-table-column prop="operName" label="操作人" />
      <el-table-column prop="operType" label="类型" width="80">
        <template #default="{ row }">{{ { 1: '新增', 2: '修改', 3: '删除', 4: '查询', 5: '导出' }[row.operType] || row.operType }}</template>
      </el-table-column>
      <el-table-column prop="operIp" label="IP" />
      <el-table-column prop="status" label="结果" width="80">
        <template #default="{ row }">{{ row.status === '0' ? '成功' : '失败' }}</template>
      </el-table-column>
      <el-table-column prop="costTime" label="耗时(ms)" width="100" />
      <el-table-column prop="operTime" label="时间" width="170" />
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" style="margin-top:12px;justify-content:flex-end" @change="load" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { listOperLog } from '@/api/monitor/log'
const rows = ref([])
const total = ref(0)
const query = reactive({ operName: '', title: '', pageNum: 1, pageSize: 10 })
async function load() {
  const res = await listOperLog(query)
  rows.value = res.data.rows || []
  total.value = res.data.total || 0
}
onMounted(load)
</script>

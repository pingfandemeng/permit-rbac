<template>
  <div class="page-card">
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="参数名称"><el-input v-model="query.configName" clearable /></el-form-item>
      <el-form-item label="参数键"><el-input v-model="query.configKey" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="load">查询</el-button></el-form-item>
    </el-form>
    <el-table :data="rows" border>
      <el-table-column prop="configName" label="参数名称" min-width="160" />
      <el-table-column prop="configKey" label="参数键" min-width="200" />
      <el-table-column prop="configValue" label="参数值" min-width="160" />
      <el-table-column prop="remark" label="备注" min-width="200" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-permission="['system:config:edit']" link type="primary" @click="edit(row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" style="margin-top:12px;justify-content:flex-end" @change="load" />
    <el-dialog v-model="visible" title="修改参数" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="参数键"><el-input v-model="form.configKey" disabled /></el-form-item>
        <el-form-item label="参数值"><el-input v-model="form.configValue" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
        <el-alert v-if="form.configKey && form.configKey.indexOf('session') >= 0" title="会话超时参数修改后需重启后端生效" type="warning" :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listConfig, updateConfig } from '@/api/system/config'

const rows = ref([])
const total = ref(0)
const visible = ref(false)
const query = reactive({ configName: '', configKey: '', pageNum: 1, pageSize: 10 })
const form = reactive({ configId: null, configKey: '', configValue: '', remark: '' })

async function load() {
  const res = await listConfig(query)
  rows.value = res.data.rows || []
  total.value = res.data.total || 0
}
function edit(row) {
  Object.assign(form, row)
  if (row.configKey === 'sys.user.initPassword') form.configValue = ''
  visible.value = true
}
async function save() {
  await updateConfig(form)
  ElMessage.success('已保存')
  visible.value = false
  load()
}
onMounted(load)
</script>

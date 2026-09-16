<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button v-permission="['system:dept:add']" type="primary" @click="open(null)">新增部门</el-button>
    </div>
    <el-table :data="rows" row-key="deptId" border default-expand-all :tree-props="{ children: 'children' }">
      <el-table-column prop="deptName" label="部门名称" min-width="180" />
      <el-table-column prop="deptKey" label="部门编码" />
      <el-table-column prop="orderNum" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === '0' ? '正常' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-permission="['system:dept:add']" link type="primary" @click="open(null, row.deptId)">新增</el-button>
          <el-button v-permission="['system:dept:edit']" link type="primary" @click="open(row)">编辑</el-button>
          <el-button v-permission="['system:dept:remove']" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" :title="form.deptId ? '编辑部门' : '新增部门'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级">
          <el-tree-select v-model="form.parentId" :data="parentOptions" node-key="deptId" :props="{ label: 'deptName', children: 'children' }" check-strictly />
        </el-form-item>
        <el-form-item label="名称"><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.deptKey" :disabled="!!form.deptId" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group>
        </el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { addDept, deleteDept, listDept, updateDept } from '@/api/system/dept'

const rows = ref([])
const visible = ref(false)
const form = reactive({ deptId: null, parentId: 0, deptName: '', deptKey: '', orderNum: 0, status: '0', version: 0 })
const parentOptions = ref([])

async function load() {
  const res = await listDept()
  rows.value = res.data || []
}
function open(row, parentId) {
  parentOptions.value = [{ deptId: 0, deptName: '根节点', children: rows.value }]
  if (row) Object.assign(form, row)
  else Object.assign(form, { deptId: null, parentId: parentId || 0, deptName: '', deptKey: '', orderNum: 0, status: '0', version: 0 })
  visible.value = true
}
async function save() {
  if (form.deptId) await updateDept(form)
  else await addDept(form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该部门？', '提示', { type: 'warning' })
  await deleteDept(row.deptId)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

<template>
  <div class="page-card">
    <el-form :inline="true" :model="query" class="query-form">
      <el-form-item label="角色名称"><el-input v-model="query.roleName" clearable /></el-form-item>
      <el-form-item label="角色编码"><el-input v-model="query.roleKey" clearable /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button v-permission="['system:role:add']" type="primary" @click="openCreate">新增角色</el-button>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleKey" label="角色编码" />
      <el-table-column prop="dataScope" label="数据范围" width="140">
        <template #default="{ row }">{{ scopeText(row.dataScope) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">{{ row.status === '0' ? '正常' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button v-permission="['system:role:edit']" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="['system:role:edit']" link type="primary" @click="openMenus(row)">菜单权限</el-button>
          <el-button v-permission="['system:role:edit']" link type="primary" @click="openScope(row)">数据权限</el-button>
          <el-button v-permission="['system:role:remove']" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" layout="total, prev, pager, next" style="margin-top:12px;justify-content:flex-end" @change="load" />

    <el-dialog v-model="formVisible" :title="form.roleId ? '编辑角色' : '新增角色'" width="480px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="角色名称"><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item v-if="!form.roleId" label="角色编码"><el-input v-model="form.roleKey" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status"><el-radio label="0">正常</el-radio><el-radio label="1">停用</el-radio></el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="menuVisible" title="分配菜单权限" width="520px">
      <el-tree ref="menuTreeRef" :data="menus" show-checkbox node-key="menuId" :props="{ label: 'menuName', children: 'children' }" :default-expand-all="true" />
      <template #footer>
        <el-button @click="menuVisible=false">取消</el-button>
        <el-button type="primary" @click="saveMenus">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scopeVisible" title="数据权限" width="520px">
      <el-form label-width="100px">
        <el-form-item label="数据范围">
          <el-select v-model="scopeForm.dataScope" style="width:100%">
            <el-option label="全部数据" value="1" />
            <el-option label="本部门及以下" value="2" />
            <el-option label="本部门" value="3" />
            <el-option label="仅本人" value="4" />
            <el-option label="自定义" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="scopeForm.dataScope === '5'" label="可见部门">
          <el-tree ref="deptTreeRef" :data="depts" show-checkbox node-key="deptId" :props="{ label: 'deptName', children: 'children' }" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scopeVisible=false">取消</el-button>
        <el-button type="primary" @click="saveScope">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addRole, assignRoleDataScope, assignRoleMenus, deleteRole, getRole, listRole, updateRole } from '@/api/system/role'
import { menuTree } from '@/api/system/menu'
import { deptTree } from '@/api/system/dept'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ roleName: '', roleKey: '', pageNum: 1, pageSize: 10 })
const formVisible = ref(false)
const form = reactive({ roleId: null, roleName: '', roleKey: '', sort: 0, status: '0', remark: '', version: 0 })
const menuVisible = ref(false)
const menus = ref([])
const menuTreeRef = ref()
const currentRoleId = ref(null)
const scopeVisible = ref(false)
const scopeForm = reactive({ dataScope: '2' })
const depts = ref([])
const deptTreeRef = ref()

const texts = { 1: '全部数据', 2: '本部门及以下', 3: '本部门', 4: '仅本人', 5: '自定义' }
function scopeText(v) { return texts[v] || v }

async function load() {
  loading.value = true
  try {
    const res = await listRole(query)
    rows.value = res.data.rows || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}
function openCreate() {
  Object.assign(form, { roleId: null, roleName: '', roleKey: '', sort: 0, status: '0', remark: '', version: 0 })
  formVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  formVisible.value = true
}
async function save() {
  if (form.roleId) await updateRole(form)
  else await addRole(form)
  ElMessage.success('保存成功')
  formVisible.value = false
  load()
}
async function openMenus(row) {
  currentRoleId.value = row.roleId
  const [tree, detail] = await Promise.all([menuTree(), getRole(row.roleId)])
  menus.value = tree.data || []
  menuVisible.value = true
  await nextTick()
  menuTreeRef.value.setCheckedKeys(detail.data.menuIds || [])
}
async function saveMenus() {
  const ids = [...menuTreeRef.value.getCheckedKeys(), ...menuTreeRef.value.getHalfCheckedKeys()]
  await assignRoleMenus(currentRoleId.value, ids)
  ElMessage.success('菜单权限已更新，用户刷新后生效')
  menuVisible.value = false
}
async function openScope(row) {
  currentRoleId.value = row.roleId
  const [tree, detail] = await Promise.all([deptTree(), getRole(row.roleId)])
  depts.value = tree.data || []
  scopeForm.dataScope = detail.data.dataScope
  scopeVisible.value = true
  await nextTick()
  if (scopeForm.dataScope === '5') {
    deptTreeRef.value?.setCheckedKeys(detail.data.deptIds || [])
  }
}
async function saveScope() {
  const payload = { dataScope: scopeForm.dataScope, deptIds: [] }
  if (scopeForm.dataScope === '5') {
    payload.deptIds = deptTreeRef.value.getCheckedKeys()
  }
  await assignRoleDataScope(currentRoleId.value, payload)
  ElMessage.success('数据权限已更新')
  scopeVisible.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除角色 ${row.roleName}？`, '提示', { type: 'warning' })
  await deleteRole(row.roleId)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

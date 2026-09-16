<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button v-permission="['system:menu:add']" type="primary" @click="open(null)">新增菜单</el-button>
    </div>
    <el-table :data="rows" row-key="menuId" border default-expand-all :tree-props="{ children: 'children' }">
      <el-table-column prop="menuName" label="名称" min-width="180" />
      <el-table-column prop="menuType" label="类型" width="80">
        <template #default="{ row }">{{ { M: '目录', C: '菜单', F: '按钮' }[row.menuType] }}</template>
      </el-table-column>
      <el-table-column prop="path" label="路由" min-width="120" />
      <el-table-column prop="component" label="组件" min-width="160" />
      <el-table-column prop="perms" label="权限标识" min-width="180" />
      <el-table-column prop="orderNum" label="排序" width="70" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button v-permission="['system:menu:add']" link type="primary" @click="open(null, row.menuId)">新增</el-button>
          <el-button v-permission="['system:menu:edit']" link type="primary" @click="open(row)">编辑</el-button>
          <el-button v-permission="['system:menu:remove']" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" :title="form.menuId ? '编辑菜单' : '新增菜单'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级"><el-tree-select v-model="form.parentId" :data="parentOptions" node-key="menuId" :props="{ label: 'menuName', children: 'children' }" check-strictly /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">目录</el-radio><el-radio label="C">菜单</el-radio><el-radio label="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由"><el-input v-model="form.path" /></el-form-item>
        <el-form-item v-if="form.menuType==='C'" label="组件"><el-input v-model="form.component" placeholder="system/user/index" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.perms" placeholder="module:feature:action" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
        <el-form-item label="显示">
          <el-radio-group v-model="form.visible"><el-radio label="0">显示</el-radio><el-radio label="1">隐藏</el-radio></el-radio-group>
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
import { addMenu, deleteMenu, listMenu, updateMenu } from '@/api/system/menu'

const rows = ref([])
const visible = ref(false)
const form = reactive({ menuId: null, parentId: 0, menuName: '', menuType: 'C', path: '', component: '', perms: '', icon: '', orderNum: 0, visible: '0', version: 0 })
const parentOptions = ref([])

async function load() {
  const res = await listMenu()
  rows.value = res.data || []
}
function open(row, parentId) {
  parentOptions.value = [{ menuId: 0, menuName: '根节点', children: rows.value }]
  if (row) Object.assign(form, row)
  else Object.assign(form, { menuId: null, parentId: parentId || 0, menuName: '', menuType: 'C', path: '', component: '', perms: '', icon: '', orderNum: 0, visible: '0', version: 0 })
  visible.value = true
}
async function save() {
  if (form.menuId) await updateMenu(form)
  else await addMenu(form)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该菜单？', '提示', { type: 'warning' })
  await deleteMenu(row.menuId)
  ElMessage.success('已删除')
  load()
}
onMounted(load)
</script>

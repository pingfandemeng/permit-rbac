<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑用户' : '新增用户'" width="560px" destroy-on-close @closed="emit('closed')">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item v-if="!isEdit" label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="姓名" prop="nickname">
        <el-input v-model="form.nickname" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="部门" prop="deptId">
        <el-tree-select
          v-model="form.deptId"
          :data="deptOptions"
          node-key="deptId"
          :props="{ label: 'deptName', children: 'children' }"
          check-strictly
          placeholder="选择部门"
        />
      </el-form-item>
      <el-form-item label="角色" prop="roleIds">
        <el-select v-model="form.roleIds" multiple filterable placeholder="选择角色" style="width: 100%">
          <el-option v-for="r in roleOptions" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addUser, getUser, updateUser } from '@/api/system/user'
import { deptTree } from '@/api/system/dept'
import { roleOptions as fetchRoles } from '@/api/system/role'

const props = defineProps({
  modelValue: Boolean,
  userId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue', 'success', 'closed'])
const visible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const deptOptions = ref([])
const roleOptions = ref([])
const form = reactive({
  userId: null, username: '', nickname: '', phone: '', email: '', deptId: null, roleIds: [], remark: '', version: 0
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择部门', trigger: 'change' }]
}

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    const [d, r] = await Promise.all([deptTree(), fetchRoles()])
    deptOptions.value = d.data || []
    roleOptions.value = r.data || []
    isEdit.value = !!props.userId
    if (props.userId) {
      const res = await getUser(props.userId)
      Object.assign(form, res.data)
    } else {
      Object.assign(form, { userId: null, username: '', nickname: '', phone: '', email: '', deptId: null, roleIds: [], remark: '', version: 0 })
    }
  }
})
watch(visible, (val) => emit('update:modelValue', val))

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateUser(form)
    } else {
      await addUser(form)
    }
    ElMessage.success('保存成功')
    emit('success')
    visible.value = false
  } finally {
    saving.value = false
  }
}
</script>

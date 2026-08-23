<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="queryParams" @submit.prevent="handleQuery">
      <el-form-item label="字典名称">
        <el-input v-model="queryParams.dictName" placeholder="请输入字典名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="字典类型">
        <el-input v-model="queryParams.dictType" placeholder="请输入字典类型" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain :disabled="multiple" @click="handleDelete()">删除</el-button>
      </el-col>
    </el-row>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="typeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="字典编号" prop="id" width="100" />
      <el-table-column label="字典名称" prop="dictName" />
      <el-table-column label="字典类型" prop="dictType" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="180" />
      <el-table-column label="操作" align="center" width="180">
        <template #default="scope">
          <el-button link type="primary" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <el-pagination
      v-show="total > 0"
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="getList"
      @current-change="getList"
    />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="form.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" placeholder="请输入字典类型编码" :disabled="form.id !== undefined" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageDictType, addDictType, updateDictType, deleteDictType } from '@/api/dictType'

const loading = ref(true)          // 表格加载状态
const typeList = ref([])           // 类型列表
const total = ref(0)               // 总记录数
const ids = ref([])                // 多选 id 数组
const multiple = ref(true)         // 是否禁用批量删除按钮

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dictName: '',
  dictType: '',
  status: ''
})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: ''
})

// 表单引用和表单数据
const formRef = ref(null)
const form = reactive({
  id: undefined,
  dictName: '',
  dictType: '',
  status: '0',
  remark: ''
})

// 表单校验规则
const rules = {
  dictName: [{ required: true, message: '字典名称不能为空', trigger: 'blur' }],
  dictType: [{ required: true, message: '字典类型编码不能为空', trigger: 'blur' }]
}

// 获取列表数据
async function getList() {
  loading.value = true
  try {
    const res = await pageDictType(queryParams)
    typeList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

// 搜索
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

// 重置搜索
function resetQuery() {
  queryParams.dictName = ''
  queryParams.dictType = ''
  queryParams.status = ''
  handleQuery()
}

// 新增
function handleAdd() {
  resetForm()
  dialog.visible = true
  dialog.title = '新增字典类型'
}

// 修改
function handleUpdate(row) {
  resetForm()
  dialog.visible = true
  dialog.title = '修改字典类型'
  form.id = row.id
  form.dictName = row.dictName
  form.dictType = row.dictType
  form.status = row.status
  form.remark = row.remark
}

// 删除（支持单个和批量）
function handleDelete(row) {
  const deleteIds = row.id ? [row.id] : ids.value
  ElMessageBox.confirm('是否确认删除选中的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDictType(deleteIds)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

// 表格多选变化
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

// 提交表单
async function submitForm() {
  await formRef.value.validate()
  if (form.id !== undefined) {
    await updateDictType(form)
    ElMessage.success('修改成功')
  } else {
    await addDictType(form)
    ElMessage.success('新增成功')
  }
  dialog.visible = false
  getList()
}

// 重置表单
function resetForm() {
  form.id = undefined
  form.dictName = ''
  form.dictType = ''
  form.status = '0'
  form.remark = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 页面加载时获取列表
onMounted(() => {
  getList()
})
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
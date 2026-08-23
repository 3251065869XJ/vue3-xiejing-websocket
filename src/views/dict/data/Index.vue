<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :inline="true" :model="queryParams" @submit.prevent="handleQuery">
      <el-form-item label="字典类型">
        <el-select v-model="queryParams.dictType" placeholder="请选择字典类型" clearable @change="handleQuery">
          <el-option v-for="item in typeOptions" :key="item.dictType" :label="item.dictName" :value="item.dictType" />
        </el-select>
      </el-form-item>
      <el-form-item label="字典标签">
        <el-input v-model="queryParams.dictLabel" placeholder="请输入字典标签" clearable @keyup.enter="handleQuery" />
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
    <el-table v-loading="loading" :data="dataList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="字典类型" prop="dictType" width="150" />
      <el-table-column label="字典标签" prop="dictLabel" />
      <el-table-column label="字典键值" prop="dictValue" />
      <el-table-column label="排序" prop="dictSort" width="80" />
      <el-table-column label="默认" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isDefault === 'Y'" type="success">默认</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
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
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType">
          <el-select v-model="form.dictType" placeholder="请选择字典类型" :disabled="form.id !== undefined">
            <el-option v-for="item in typeOptions" :key="item.dictType" :label="item.dictName" :value="item.dictType" />
          </el-select>
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="请输入字典键值" />
        </el-form-item>
        <el-form-item label="排序" prop="dictSort">
          <el-input-number v-model="form.dictSort" :min="0" />
        </el-form-item>
        <el-form-item label="是否默认" prop="isDefault">
          <el-radio-group v-model="form.isDefault">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
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
import { pageDictData, addDictData, updateDictData, deleteDictData } from '@/api/dictData'
import { pageDictType } from '@/api/dictType'

const loading = ref(true)
const dataList = ref([])
const total = ref(0)
const ids = ref([])
const multiple = ref(true)
const typeOptions = ref([]) // 字典类型下拉选项

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  status: ''
})

const dialog = reactive({
  visible: false,
  title: ''
})

const formRef = ref(null)
const form = reactive({
  id: undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  dictSort: 0,
  isDefault: 'N',
  status: '0',
  remark: ''
})

const rules = {
  dictType: [{ required: true, message: '字典类型不能为空', trigger: 'change' }],
  dictLabel: [{ required: true, message: '字典标签不能为空', trigger: 'blur' }],
  dictValue: [{ required: true, message: '字典键值不能为空', trigger: 'blur' }]
}

// 获取所有字典类型（用于下拉选择）
async function getTypeOptions() {
  const res = await pageDictType({ pageNum: 1, pageSize: 1000 })
  typeOptions.value = res.data.records
}

// 获取列表数据
async function getList() {
  loading.value = true
  try {
    const res = await pageDictData(queryParams)
    dataList.value = res.data.records
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
  queryParams.dictType = ''
  queryParams.dictLabel = ''
  queryParams.dictValue = ''
  queryParams.status = ''
  handleQuery()
}

// 新增
function handleAdd() {
  resetForm()
  dialog.visible = true
  dialog.title = '新增字典数据'
}

// 修改
function handleUpdate(row) {
  resetForm()
  dialog.visible = true
  dialog.title = '修改字典数据'
  form.id = row.id
  form.dictType = row.dictType
  form.dictLabel = row.dictLabel
  form.dictValue = row.dictValue
  form.dictSort = row.dictSort
  form.isDefault = row.isDefault
  form.status = row.status
  form.remark = row.remark
}

// 删除
function handleDelete(row) {
  const deleteIds = row.id ? [row.id] : ids.value
  ElMessageBox.confirm('是否确认删除选中的数据项？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteDictData(deleteIds)
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
    await updateDictData(form)
    ElMessage.success('修改成功')
  } else {
    await addDictData(form)
    ElMessage.success('新增成功')
  }
  dialog.visible = false
  getList()
}

// 重置表单
function resetForm() {
  form.id = undefined
  form.dictType = ''
  form.dictLabel = ''
  form.dictValue = ''
  form.dictSort = 0
  form.isDefault = 'N'
  form.status = '0'
  form.remark = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  getTypeOptions()
  getList()
})
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}
</style>
<template>
  <div class="page-container">
    <el-button type="primary" size="large" @click="dialogVisible = true">
      访客接待
    </el-button>

    <!-- 访客接待对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="访客接待刷卡"
      :width="dialogWidth"
      :close-on-click-modal="false"
      class="reception-dialog"
      @close="resetDialog"
    >
      <div class="dialog-layout">
        <!-- 左右布局区域 -->
        <div class="top-layout">
          <!-- 左侧：接待人信息 -->
          <div class="left-panel">
            <h3 class="section-title">接待人信息</h3>
            <el-form label-width="80px" label-position="top">
              <el-form-item label="选择产品">
                <el-select
                  v-model="selectedProduct"
                  placeholder="请选择接待产品"
                  style="width: 100%"
                  @change="checkReceptionistPermission"
                >
                  <el-option
                    v-for="product in productList"
                    :key="product"
                    :label="product"
                    :value="product"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="刷卡(工号)">
                <div class="card-input-group">
                  <el-input
                    v-model="receptionistCardCode"
                    placeholder="请刷工卡或输入工号"
                    clearable
                    @keyup.enter="handleReceptionistCard"
                  />
                  <el-button type="primary" @click="handleReceptionistCard">刷卡</el-button>
                </div>
              </el-form-item>

              <!-- 接待人信息展示 -->
              <div v-if="receptionistInfo.name" class="receptionist-info">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="姓名">{{ receptionistInfo.name }}</el-descriptions-item>
                  <el-descriptions-item label="工号">{{ receptionistInfo.employeeId }}</el-descriptions-item>
                  <el-descriptions-item label="部门">{{ receptionistInfo.department }}</el-descriptions-item>
                </el-descriptions>
                <el-alert
                  v-if="permissionError"
                  title="没有该产品的接待权限"
                  type="error"
                  :closable="false"
                  show-icon
                  class="permission-alert"
                />
              </div>
            </el-form>
          </div>

          <!-- 右侧：访客区域 -->
          <div class="right-panel">
            <h3 class="section-title">访客信息登记</h3>
            <div class="visitor-type">
              <span class="type-label">访客类型：</span>
              <el-radio-group v-model="visitorType">
                <el-radio label="internal">外部门员工(有工卡)</el-radio>
                <el-radio label="external">外公司人员(无工卡)</el-radio>
              </el-radio-group>
            </div>

            <!-- 外部门员工刷卡区域 -->
            <div v-if="visitorType === 'internal'" class="internal-card-area">
              <div class="card-input-group">
                <el-input
                  v-model="internalEmployeeCode"
                  placeholder="请刷工卡或输入工号"
                  clearable
                  @keyup.enter="handleInternalCard"
                />
                <el-button type="primary" @click="handleInternalCard">刷卡</el-button>
              </div>
              <div v-if="internalVisitorInfo.name" class="visitor-preview">
                <el-descriptions :column="1" border size="small">
                  <el-descriptions-item label="姓名">{{ internalVisitorInfo.name }}</el-descriptions-item>
                  <el-descriptions-item label="工号">{{ internalVisitorInfo.employeeId }}</el-descriptions-item>
                  <el-descriptions-item label="部门">{{ internalVisitorInfo.department }}</el-descriptions-item>
                </el-descriptions>
              </div>
            </div>

            <!-- 外公司人员手动填写区域 -->
            <div v-if="visitorType === 'external'" class="external-form">
              <el-form label-width="80px" label-position="top">
                <el-form-item label="姓名">
                  <el-input v-model="externalVisitor.name" placeholder="请输入姓名" clearable />
                </el-form-item>
                <el-form-item label="来访公司">
                  <el-input v-model="externalVisitor.company" placeholder="请输入公司名称" clearable />
                </el-form-item>
                <el-form-item label="身份证号">
                  <el-input
                    v-model="externalVisitor.idNumber"
                    placeholder="请输入身份证号(选填)"
                    clearable
                  />
                </el-form-item>
              </el-form>
            </div>

            <el-button
              type="success"
              :disabled="!canConfirmVisitor"
              @click="addVisitorToList"
              class="confirm-visitor-btn"
            >
              确认信息无误，添加访客
            </el-button>
          </div>
        </div>

        <!-- 下侧布局：访客列表 + 确认接待按钮 -->
        <div class="bottom-layout">
          <div class="visitor-list-header">
            <span class="section-title">待接待访客列表</span>
            <span class="list-count" v-if="visitorList.length">共{{ visitorList.length }}人</span>
          </div>
          <div class="visitor-table-wrapper">
            <el-table :data="visitorList" border stripe style="width: 100%">
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="typeLabel" label="访客类型" width="140" />
              <el-table-column prop="identityInfo" label="身份信息" min-width="200" />
              <el-table-column label="操作" width="80" align="center">
                <template #default="{ $index }">
                  <el-button link type="danger" @click="removeVisitor($index)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="confirm-reception-btn-wrapper">
            <el-button type="primary" size="large" @click="submitReception">确认接待</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 对话框宽度（屏幕宽度的90%）
const dialogWidth = '90%'
const dialogVisible = ref(false)

// ---------- Mock 数据 ----------
// 产品列表
const productList = ['产品A', '产品B', '产品C']

// 接待人数据（模拟数据库）
const receptionistDB = [
  { employeeId: 'R1001', name: '张佳怡', department: '接待部', accessibleProducts: ['产品A', '产品B'] },
  { employeeId: 'R1002', name: '李振国', department: '行政部', accessibleProducts: ['产品B', '产品C'] },
  { employeeId: 'R1003', name: '王雅茹', department: '市场部', accessibleProducts: ['产品A'] }
]

// 外部门员工数据（模拟其他部门员工）
const internalStaffDB = [
  { employeeId: 'E2001', name: '赵一航', department: '技术研发部' },
  { employeeId: 'E2002', name: '钱小美', department: '销售部' },
  { employeeId: 'E2003', name: '孙立军', department: '产品部' }
]

// ---------- 左侧接待人状态 ----------
const selectedProduct = ref(productList[0])
const receptionistCardCode = ref('')
const receptionistInfo = ref({ name: '', employeeId: '', department: '' })
const permissionError = ref(false)

// 刷卡：获取接待人信息并检查权限
const handleReceptionistCard = () => {
  const code = receptionistCardCode.value.trim()
  if (!code) {
    ElMessage.warning('请输入或刷卡读取工号')
    return
  }
  const found = receptionistDB.find(item => item.employeeId === code)
  if (!found) {
    ElMessage.error('未找到该接待人工号，请重试')
    receptionistInfo.value = { name: '', employeeId: '', department: '' }
    permissionError.value = false
    return
  }
  receptionistInfo.value = {
    name: found.name,
    employeeId: found.employeeId,
    department: found.department
  }
  checkReceptionistPermission()
}

// 检查当前接待人是否有选中产品的权限
const checkReceptionistPermission = () => {
  if (!receptionistInfo.value.employeeId) {
    permissionError.value = false
    return
  }
  const found = receptionistDB.find(item => item.employeeId === receptionistInfo.value.employeeId)
  if (found && found.accessibleProducts.includes(selectedProduct.value)) {
    permissionError.value = false
  } else {
    permissionError.value = true
  }
}

// 监听产品变化，重新检查权限
watch(selectedProduct, () => {
  if (receptionistInfo.value.employeeId) {
    checkReceptionistPermission()
  }
})

// ---------- 右侧访客状态 ----------
const visitorType = ref('internal') // 'internal' 或 'external'

// 外部门员工刷卡相关
const internalEmployeeCode = ref('')
const internalVisitorInfo = ref({ name: '', employeeId: '', department: '' })

// 外公司人员表单
const externalVisitor = ref({ name: '', company: '', idNumber: '' })

// 访客列表（下方展示）
const visitorList = ref([])

// 右侧“确认信息无误”按钮是否可用
const canConfirmVisitor = computed(() => {
  if (visitorType.value === 'internal') {
    return !!internalVisitorInfo.value.name
  } else {
    return !!(externalVisitor.value.name && externalVisitor.value.company)
  }
})

// 外部门员工刷卡逻辑
const handleInternalCard = () => {
  const code = internalEmployeeCode.value.trim()
  if (!code) {
    ElMessage.warning('请输入或刷卡读取工号')
    return
  }
  const found = internalStaffDB.find(item => item.employeeId === code)
  if (!found) {
    ElMessage.error('未找到该外部门员工信息')
    internalVisitorInfo.value = { name: '', employeeId: '', department: '' }
    return
  }
  internalVisitorInfo.value = {
    name: found.name,
    employeeId: found.employeeId,
    department: found.department
  }
  ElMessage.success(`已读取员工：${found.name}`)
}

// 添加访客至列表
const addVisitorToList = () => {
  if (visitorType.value === 'internal') {
    const visitor = internalVisitorInfo.value
    if (!visitor.name) {
      ElMessage.warning('请先刷卡获取外部门员工信息')
      return
    }
    visitorList.value.push({
      id: Date.now() + Math.random(),
      name: visitor.name,
      typeLabel: '外部门员工',
      identityInfo: `工号：${visitor.employeeId} 部门：${visitor.department}`,
      rawData: { ...visitor, type: 'internal' }
    })
    // 清空内部刷卡区域
    internalEmployeeCode.value = ''
    internalVisitorInfo.value = { name: '', employeeId: '', department: '' }
    ElMessage.success('访客已添加至待接待列表')
  } else {
    const { name, company, idNumber } = externalVisitor.value
    if (!name || !company) {
      ElMessage.warning('请完整填写姓名和来访公司')
      return
    }
    const identityInfo = `公司：${company} ${idNumber ? `身份证：${idNumber}` : ''}`
    visitorList.value.push({
      id: Date.now() + Math.random(),
      name,
      typeLabel: '外公司人员',
      identityInfo,
      rawData: { name, company, idNumber, type: 'external' }
    })
    // 清空外部表单
    externalVisitor.value = { name: '', company: '', idNumber: '' }
    ElMessage.success('访客已添加至待接待列表')
  }
}

// 删除访客
const removeVisitor = (index) => {
  visitorList.value.splice(index, 1)
  ElMessage.info('已移除该访客')
}

// 提交全部接待
const submitReception = () => {
  // 校验接待人是否已刷卡
  if (!receptionistInfo.value.employeeId) {
    ElMessage.error('请先完成接待人刷卡')
    return
  }
  // 校验接待权限
  if (permissionError.value) {
    ElMessage.error('当前接待人没有所选产品的接待权限，无法进行接待')
    return
  }
  if (visitorList.value.length === 0) {
    ElMessage.warning('请至少添加一位访客')
    return
  }

  // 组装提交数据
  const submitData = {
    receptionist: {
      ...receptionistInfo.value,
      product: selectedProduct.value
    },
    visitors: visitorList.value.map(v => v.rawData),
    totalCount: visitorList.value.length
  }
  console.log('提交接待信息：', submitData)
  ElMessageBox.alert(
    `接待人：${submitData.receptionist.name}（${submitData.receptionist.employeeId}）\n产品：${submitData.receptionist.product}\n访客数量：${submitData.totalCount}人\n详情请查看控制台`,
    '提交成功',
    { type: 'success', confirmButtonText: '确定' }
  ).then(() => {
    dialogVisible.value = false
  })
}

// 重置对话框所有状态
const resetDialog = () => {
  // 左侧重置
  selectedProduct.value = productList[0]
  receptionistCardCode.value = ''
  receptionistInfo.value = { name: '', employeeId: '', department: '' }
  permissionError.value = false
  // 右侧重置
  visitorType.value = 'internal'
  internalEmployeeCode.value = ''
  internalVisitorInfo.value = { name: '', employeeId: '', department: '' }
  externalVisitor.value = { name: '', company: '', idNumber: '' }
  // 访客列表清空
  visitorList.value = []
}
</script>

<style scoped>
.page-container {
  padding: 40px;
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 对话框整体样式 */
.reception-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
  max-height: 80vh;
  overflow-y: auto;
}

.dialog-layout {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.top-layout {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.left-panel,
.right-panel {
  flex: 1;
  min-width: 260px;
  background: #ffffff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03), 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.section-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 18px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e6e9f0;
  color: #1f2f3d;
}

.card-input-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.card-input-group .el-input {
  flex: 1;
}

.receptionist-info {
  margin-top: 16px;
}

.permission-alert {
  margin-top: 12px;
}

.visitor-type {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8f9fc;
  padding: 10px 12px;
  border-radius: 8px;
}

.type-label {
  font-weight: 500;
  color: #2c3e50;
}

.internal-card-area,
.external-form {
  margin-top: 12px;
  margin-bottom: 20px;
}

.visitor-preview {
  margin-top: 14px;
}

.confirm-visitor-btn {
  width: 100%;
  margin-top: 8px;
  font-weight: 500;
}

.bottom-layout {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03), 0 2px 6px rgba(0, 0, 0, 0.05);
}

.visitor-list-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eaeef2;
}

.list-count {
  font-size: 13px;
  color: #909399;
}

.visitor-table-wrapper {
  margin-bottom: 20px;
  max-height: 280px;
  overflow-y: auto;
}

.confirm-reception-btn-wrapper {
  display: flex;
  justify-content: flex-end;
}

/* 简洁滚动条 */
.visitor-table-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

.visitor-table-wrapper::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 3px;
}

@media (max-width: 768px) {
  .top-layout {
    flex-direction: column;
  }
  .card-input-group {
    flex-wrap: wrap;
  }
  .confirm-reception-btn-wrapper {
    justify-content: stretch;
  }
  .confirm-reception-btn-wrapper .el-button {
    width: 100%;
  }
}
</style>
<template>
  <div class="schedule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="page-title">
        <div class="logo-icon">排</div>
        <h1>部门排班管理系统 <span class="subtitle">| 拖拽排班 · 智能高效</span></h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" round @click="handleSmartAssign">⚡ 智能一键排班</el-button>
        <el-button type="success" round @click="handleCopySchedule">📋 复制历史排班</el-button>
        <el-button type="danger" round @click="handleClearAll">🗑️ 一键取消排班</el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-panel">
      <div class="filter-item">
        <span class="label">📅 排班日期</span>
        <el-date-picker v-model="scheduleDate" type="date" placeholder="选择日期" :clearable="false" value-format="YYYY-MM-DD" style="width: 100%" />
      </div>
      <div class="filter-item" style="min-width: 240px">
        <span class="label">🏭 生产线体 / 模型</span>
        <el-select v-model="selectedLineIds" multiple collapse-tags placeholder="请选择线体" @change="onFilterChange" style="width: 100%">
          <el-option v-for="line in productionLines" :key="line.id" :label="line.name + ' - ' + line.modelName" :value="line.id" />
        </el-select>
      </div>
      <div class="filter-item" style="min-width: 160px">
        <span class="label">📍 工位组</span>
        <el-select v-model="selectedWorkGroup" placeholder="请选择工位组" clearable @change="onFilterChange" style="width: 100%">
          <el-option v-for="wg in workGroups" :key="wg.id" :label="wg.name" :value="wg.id" />
        </el-select>
      </div>
      <div class="filter-item" style="min-width: 180px">
        <span class="label">🕐 班次类型</span>
        <el-select v-model="selectedShifts" multiple placeholder="请选择班次" @change="onFilterChange" style="width: 100%">
          <el-option v-for="s in shifts" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </div>
      <div class="filter-item" style="min-width: 140px">
        <span class="label">👤 人员类型</span>
        <el-select v-model="employeeTypeFilter" placeholder="全部类型" clearable @change="onFilterChange" style="width: 100%">
          <el-option v-for="t in employeeTypes" :key="t" :label="t" :value="t" />
        </el-select>
      </div>
      <div class="filter-item" style="min-width: 140px">
        <span class="label">🕐 人员班次</span>
        <el-select v-model="employeeShiftFilter" placeholder="全部班次" clearable @change="onFilterChange" style="width: 100%">
          <el-option v-for="s in shifts" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </div>
    </div>

    <!-- 已选线体标签 -->
    <div v-if="selectedLineIds.length > 0" class="selected-lines-tags">
      <el-tag v-for="lid in selectedLineIds" :key="lid" closable @close="removeLine(lid)" size="small">
        {{ getLineName(lid) }}
      </el-tag>
    </div>

    <!-- 统计栏 -->
    <div class="stats-bar">
      <div class="stat-card">
        <div class="stat-icon blue">🏭</div>
        <div class="stat-info">
          <div class="stat-value">{{ selectedLineIds.length }}</div>
          <div class="stat-label">已选线体</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">💼</div>
        <div class="stat-info">
          <div class="stat-value">{{ positions.length }}</div>
          <div class="stat-label">可用岗位</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ filteredEmployees.length }}</div>
          <div class="stat-label">可选员工</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">📋</div>
        <div class="stat-info">
          <div class="stat-value">{{ assignedCount }}</div>
          <div class="stat-label">已排班</div>
        </div>
      </div>
    </div>

    <!-- 提示 -->
    <div class="tip-bar">
      <span>💡 拖拽员工卡片至岗位卡片完成排班；每个岗位仅限一人；点击岗位卡片上的 ✕ 可取消排班。</span>
    </div>

    <!-- 主体：左侧员工列表，右侧岗位列表 -->
    <div class="main-content">
      <!-- 左侧员工列表 -->
      <div class="left-panel">
        <div class="panel">
          <div class="panel-header">
            <div class="panel-title"><span class="dot green"></span>👥 员工列表</div>
            <div style="display:flex;gap:6px;align-items:center">
              <el-tag size="small" type="info">{{ selectedEmployeeIds.length }} 已选中</el-tag>
              <el-button size="small" @click="clearEmployeeSelection" round>取消选中</el-button>
            </div>
          </div>
          <div class="panel-body">
            <div v-if="filteredEmployees.length === 0" class="empty-state">
              <div class="empty-icon">👤</div>
              <div class="empty-text">暂无符合条件的员工</div>
            </div>
            <div class="employee-grid">
              <EmployeeCard
                v-for="emp in filteredEmployees"
                :key="emp.id"
                :employee="emp"
                :is-selected="selectedEmployeeIds.includes(emp.id)"
                :is-assigned="isEmployeeAssigned(emp.id)"
                :assigned-position="getAssignedPositionName(emp.id)"
                @click="toggleEmployeeSelection(emp)"
                @modify="openModifySchedule(emp.id)"
                @dragstart="handleDragStart(emp)"
                @dragend="handleDragEnd"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧岗位列表 -->
      <div class="right-panel">
        <FixedPositionPanel
          :positions="fixedPositions"
          :employees="employees"
          :selected-position-id="selectedPositionId"
          :assigned-map="assignedMap"
          @select="selectPosition"
          @assign="assignSelectedToPosition"
          @remove-employee="removeEmployeeFromPosition"
          @drop-employee="handleDropOnPosition"
          @add-temp="openAddTempPosition"
        />
        <PublicPositionPanel
          :positions="publicPositions"
          :employees="employees"
          :selected-position-id="selectedPositionId"
          :assigned-map="assignedMap"
          @select="selectPosition"
          @assign="assignSelectedToPosition"
          @remove-employee="removeEmployeeFromPosition"
          @drop-employee="handleDropOnPosition"
          @add-public="openAddPublicPosition"
        />
      </div>
    </div>

    <!-- 弹窗：添加临时岗位 -->
    <el-dialog v-model="showAddTempDialog" title="添加临时岗位" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="岗位名称">
          <el-input v-model="newTempPositionName" placeholder="请输入临时岗位名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="技能要求">
          <el-select v-model="newTempPositionSkills" multiple placeholder="选择技能要求" style="width:100%">
            <el-option v-for="sk in allSkills" :key="sk" :label="sk" :value="sk" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属线体">
          <el-select v-model="newTempLineId" placeholder="选择线体" style="width:100%">
            <el-option v-for="line in productionLines" :key="line.id" :label="line.name" :value="line.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddTempDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddTempPosition">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：添加公共岗位 -->
    <el-dialog v-model="showAddPublicDialog" title="添加全能员岗位" width="420px" :close-on-click-modal="false">
      <el-form label-width="80px">
        <el-form-item label="岗位名称">
          <el-input v-model="newPublicPositionName" placeholder="请输入全能员岗位名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="技能要求">
          <el-select v-model="newPublicPositionSkills" multiple placeholder="选择技能要求" style="width:100%">
            <el-option v-for="sk in allSkills" :key="sk" :label="sk" :value="sk" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddPublicDialog = false">取消</el-button>
        <el-button type="success" @click="confirmAddPublicPosition">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：修改排班 -->
    <el-dialog v-model="showModifyDialog" title="修改排班" width="460px" :close-on-click-modal="false">
      <p class="modify-tip">
        为员工 <strong>{{ modifyEmployeeName }}</strong> 选择新的岗位（每个岗位仅限一人）：
      </p>
      <el-select v-model="modifyTargetPositionId" placeholder="请选择新岗位" style="width:100%">
        <el-option
          v-for="pos in positions"
          :key="pos.id"
          :label="pos.name + ' - ' + getShiftName(pos.shiftId)"
          :value="pos.id"
          :disabled="isPositionOccupied(pos.id) && pos.id !== getAssignedPositionId(modifyEmployeeId)"
        />
      </el-select>
      <template #footer>
        <el-button @click="showModifyDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmModifySchedule">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 弹窗：复制历史排班 -->
    <el-dialog v-model="showCopyDialog" title="复制历史排班" width="420px" :close-on-click-modal="false">
      <p class="modify-tip">
        选择要复制排班的日期，将复制到当前日期（<strong>{{ scheduleDate }}</strong>）：
      </p>
      <el-date-picker v-model="copyFromDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" :clearable="false" style="width:100%" />
      <template #footer>
        <el-button @click="showCopyDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCopySchedule">确认复制</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmployeeCard from './components/EmployeeCard.vue'
import FixedPositionPanel from './components/FixedPositionPanel.vue'
import PublicPositionPanel from './components/PublicPositionPanel.vue'

// ---------- 模拟数据 ----------
const scheduleDate = ref(formatDate(new Date()))

const productionLines = [
  { id: 'line1', name: '线体A', modelId: 'model1', modelName: 'X系列-基础型' },
  { id: 'line2', name: '线体B', modelId: 'model2', modelName: 'X系列-增强型' },
  { id: 'line3', name: '线体C', modelId: 'model3', modelName: 'Y系列-标准型' },
  { id: 'line4', name: '线体D', modelId: 'model4', modelName: 'Y系列-旗舰型' },
]

const workGroups = [
  { id: 'wg1', name: '前段工位组' },
  { id: 'wg2', name: '中段工位组' },
  { id: 'wg3', name: '后段工位组' },
  { id: 'wg4', name: '整线工位组' },
]

const shifts = [
  { id: 'shift1', name: '白班' },
  { id: 'shift2', name: '夜班' },
]

const employeeTypes = ['正式工', '临时工', '实习生', '外包人员']
const allSkills = ['焊接', '装配', '质检', '调试', '包装', '物料', '设备操作', '安全管理']

const selectedLineIds = ref([])
const selectedWorkGroup = ref(null)
const selectedShifts = ref([])
const employeeTypeFilter = ref(null)
const employeeShiftFilter = ref(null)

const selectedEmployeeIds = ref([])
const selectedPositionId = ref(null)

const showAddTempDialog = ref(false)
const showAddPublicDialog = ref(false)
const newTempPositionName = ref('')
const newTempPositionSkills = ref([])
const newTempLineId = ref(null)
const newPublicPositionName = ref('')
const newPublicPositionSkills = ref([])

const showModifyDialog = ref(false)
const modifyEmployeeId = ref(null)
const modifyEmployeeName = ref('')
const modifyTargetPositionId = ref(null)

const showCopyDialog = ref(false)
const copyFromDate = ref(null)

let draggingEmployeeId = null
let positionIdCounter = 1000
let scheduleIdCounter = 1000

const employees = [
  { id: 'emp1', name: '张伟', type: '正式工', shiftId: 'shift1', skills: ['焊接', '装配', '质检', '调试', '设备操作'] },
  { id: 'emp2', name: '李娜', type: '正式工', shiftId: 'shift1', skills: ['装配', '调试', '包装', '物料'] },
  { id: 'emp3', name: '王强', type: '正式工', shiftId: 'shift1', skills: ['焊接', '设备操作', '安全管理', '质检'] },
  { id: 'emp4', name: '刘洋', type: '临时工', shiftId: 'shift1', skills: ['装配', '包装'] },
  { id: 'emp5', name: '陈敏', type: '正式工', shiftId: 'shift1', skills: ['质检', '调试', '物料', '装配'] },
  { id: 'emp6', name: '赵磊', type: '正式工', shiftId: 'shift2', skills: ['焊接', '装配', '质检', '调试', '设备操作'] },
  { id: 'emp7', name: '孙丽', type: '实习生', shiftId: 'shift2', skills: ['装配', '包装'] },
  { id: 'emp8', name: '周杰', type: '正式工', shiftId: 'shift2', skills: ['焊接', '设备操作', '安全管理', '调试'] },
  { id: 'emp9', name: '吴芳', type: '临时工', shiftId: 'shift2', skills: ['包装', '物料'] },
  { id: 'emp10', name: '郑浩', type: '正式工', shiftId: 'shift2', skills: ['调试', '质检', '装配', '焊接'] },
  { id: 'emp11', name: '林峰', type: '外包人员', shiftId: 'shift1', skills: ['焊接', '装配', '包装'] },
  { id: 'emp12', name: '黄蓉', type: '正式工', shiftId: 'shift1', skills: ['质检', '调试', '安全管理', '物料'] },
  { id: 'emp13', name: '杨光', type: '实习生', shiftId: 'shift2', skills: ['包装', '物料', '装配'] },
  { id: 'emp14', name: '朱婷', type: '正式工', shiftId: 'shift2', skills: ['焊接', '调试', '设备操作', '质检'] },
  { id: 'emp15', name: '马超', type: '正式工', shiftId: 'shift1', skills: ['装配', '质检', '物料', '包装'] },
]

// 生成每个线体的工位模板（模拟20个工位）
function generateStationTemplates(lineId) {
  const templates = []
  const skillsPool = ['焊接', '装配', '质检', '调试', '包装', '物料', '设备操作']
  for (let i = 1; i <= 20; i++) {
    const skill = skillsPool[(i - 1) % skillsPool.length]
    templates.push({
      stationId: `st${i}`,
      name: `工位${i}-${skill}员`,
      skills: [skill, i % 3 === 0 ? '调试' : '装配'],
      workGroupId: i <= 7 ? 'wg1' : i <= 14 ? 'wg2' : 'wg3'
    })
  }
  return templates
}

const fixedStationTemplates = {}
productionLines.forEach(line => {
  fixedStationTemplates[line.id] = generateStationTemplates(line.id)
})

// 公共岗位模板（初始两个全能员）
const publicPositionTemplates = reactive([
  { id: 'pub_tpl1', name: '全能员1', skills: ['装配', '焊接', '质检', '调试', '包装'] },
  { id: 'pub_tpl2', name: '全能员2', skills: ['装配', '焊接', '质检', '调试', '包装'] },
])

const positions = ref([])

// 排班记录 { [date]: [{id, date, employeeId, positionId}] }
const allSchedules = reactive({})
allSchedules[scheduleDate.value] = []

// 预置前3天数据用于复制
for (let i = 1; i <= 3; i++) {
  const d = new Date()
  d.setDate(d.getDate() - i)
  const dateStr = formatDate(d)
  allSchedules[dateStr] = generateMockSchedules(dateStr)
}

function generateMockSchedules(dateStr) {
  const records = []
  const empIds = ['emp1', 'emp2', 'emp3', 'emp5', 'emp6', 'emp12', 'emp15']
  const posIds = ['pos_line1_st1_shift1', 'pos_line1_st2_shift1', 'pos_line1_st1_shift2', 'pos_line1_st2_shift2', 'pos_pub_shift1_pub_tpl1', 'pos_pub_shift2_pub_tpl1']
  empIds.forEach((empId, idx) => {
    if (idx < posIds.length) {
      records.push({ id: `sch_${dateStr}_${empId}`, date: dateStr, employeeId: empId, positionId: posIds[idx] })
    }
  })
  return records
}

// 计算属性
const filteredEmployees = computed(() => {
  let list = [...employees]
  if (employeeTypeFilter.value) list = list.filter(e => e.type === employeeTypeFilter.value)
  if (employeeShiftFilter.value) list = list.filter(e => e.shiftId === employeeShiftFilter.value)
  return list
})

const assignedMap = computed(() => {
  const map = {}
  const schedules = allSchedules[scheduleDate.value] || []
  schedules.forEach(s => { map[s.positionId] = s.employeeId })
  return map
})

const assignedCount = computed(() => Object.keys(assignedMap.value).length)

const assignedEmployeeIds = computed(() => new Set(Object.values(assignedMap.value)))

const fixedPositions = computed(() => positions.value.filter(p => p.type === 'fixed'))
const publicPositions = computed(() => positions.value.filter(p => p.type === 'public'))

// 工具函数
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function getLineName(lineId) {
  const line = productionLines.find(l => l.id === lineId)
  return line ? `${line.name} - ${line.modelName}` : lineId
}

function removeLine(lineId) {
  selectedLineIds.value = selectedLineIds.value.filter(id => id !== lineId)
  onFilterChange()
}

function onFilterChange() {
  selectedPositionId.value = null
  generatePositions()
  cleanupSchedules()
}

// 生成岗位实例
function generatePositions() {
  const newPositions = []
  const lineIds = selectedLineIds.value
  const shiftIds = selectedShifts.value
  const workGroup = selectedWorkGroup.value

  if (lineIds.length === 0 || shiftIds.length === 0) {
    positions.value = []
    return
  }

  // 固定岗位
  lineIds.forEach(lineId => {
    const templates = fixedStationTemplates[lineId] || []
    templates.forEach(tpl => {
      if (workGroup && tpl.workGroupId !== workGroup) return
      shiftIds.forEach(shiftId => {
        newPositions.push({
          id: `pos_${lineId}_${tpl.stationId}_${shiftId}`,
          name: tpl.name,
          type: 'fixed',
          lineId,
          shiftId,
          skills: tpl.skills,
          isTemp: false,
        })
      })
    })
  })

  // 公共岗位
  publicPositionTemplates.forEach(tpl => {
    shiftIds.forEach(shiftId => {
      newPositions.push({
        id: `pos_pub_${shiftId}_${tpl.id}`,
        name: tpl.name,
        type: 'public',
        shiftId,
        skills: tpl.skills,
        isTemp: false,
      })
    })
  })

  positions.value = newPositions
}

function cleanupSchedules() {
  const currentSchedules = allSchedules[scheduleDate.value] || []
  const validPositionIds = new Set(positions.value.map(p => p.id))
  const validEmployeeIds = new Set(filteredEmployees.value.map(e => e.id))
  allSchedules[scheduleDate.value] = currentSchedules.filter(s => validPositionIds.has(s.positionId) && validEmployeeIds.has(s.employeeId))
}

function getShiftName(shiftId) {
  const shift = shifts.find(s => s.id === shiftId)
  return shift ? shift.name : ''
}

function isEmployeeAssigned(employeeId) {
  return assignedEmployeeIds.value.has(employeeId)
}

function getAssignedPositionName(employeeId) {
  const schedules = allSchedules[scheduleDate.value] || []
  const schedule = schedules.find(s => s.employeeId === employeeId)
  if (!schedule) return ''
  const pos = positions.value.find(p => p.id === schedule.positionId)
  return pos ? pos.name : ''
}

function getAssignedPositionId(employeeId) {
  const schedules = allSchedules[scheduleDate.value] || []
  const schedule = schedules.find(s => s.employeeId === employeeId)
  return schedule ? schedule.positionId : null
}

function isPositionOccupied(positionId) {
  return assignedMap.value[positionId] != null
}

function selectPosition(pos) {
  selectedPositionId.value = selectedPositionId.value === pos.id ? null : pos.id
}

function toggleEmployeeSelection(emp) {
  if (isEmployeeAssigned(emp.id)) {
    ElMessage.warning('该员工已排班，请先取消排班后再操作')
    return
  }
  const idx = selectedEmployeeIds.value.indexOf(emp.id)
  if (idx >= 0) selectedEmployeeIds.value.splice(idx, 1)
  else selectedEmployeeIds.value.push(emp.id)
}

function clearEmployeeSelection() {
  selectedEmployeeIds.value = []
}

function assignSelectedToPosition(pos) {
  if (selectedEmployeeIds.value.length === 0) {
    ElMessage.warning('请先选择要排班的员工')
    return
  }
  if (isPositionOccupied(pos.id)) {
    ElMessage.error('该岗位已有人排班，每个岗位只能排一人')
    return
  }
  const empId = selectedEmployeeIds.value[0]
  if (selectedEmployeeIds.value.length > 1) {
    ElMessage.info('一个岗位只能排一人，默认分配第一个选中的员工')
  }
  assignEmployeeToPosition(empId, pos.id)
  selectedEmployeeIds.value = []
  selectedPositionId.value = null
}

function assignEmployeeToPosition(employeeId, positionId) {
  if (isEmployeeAssigned(employeeId)) {
    ElMessage.warning('该员工已排班')
    return
  }
  if (isPositionOccupied(positionId)) {
    ElMessage.error('该岗位已被占用')
    return
  }
  const schedule = {
    id: `sch_${scheduleDate.value}_${employeeId}_${scheduleIdCounter++}`,
    date: scheduleDate.value,
    employeeId,
    positionId,
  }
  if (!allSchedules[scheduleDate.value]) allSchedules[scheduleDate.value] = []
  allSchedules[scheduleDate.value].push(schedule)
  ElMessage.success('排班成功')
}

function removeEmployeeFromPosition(employeeId, positionId) {
  const schedules = allSchedules[scheduleDate.value] || []
  const idx = schedules.findIndex(s => s.employeeId === employeeId && s.positionId === positionId)
  if (idx >= 0) {
    schedules.splice(idx, 1)
    ElMessage.success('已取消排班')
  }
}

function handleDragStart(emp) {
  draggingEmployeeId = emp.id
}

function handleDragEnd() {
  draggingEmployeeId = null
}

function handleDropOnPosition(positionId) {
  if (!draggingEmployeeId) return
  const empId = draggingEmployeeId
  const emp = employees.find(e => e.id === empId)
  if (!emp) return
  if (isEmployeeAssigned(empId)) {
    ElMessage.warning('该员工已排班，请先取消排班后再拖拽')
    draggingEmployeeId = null
    return
  }
  if (isPositionOccupied(positionId)) {
    ElMessage.error('该岗位已被占用，无法排班')
    draggingEmployeeId = null
    return
  }
  assignEmployeeToPosition(empId, positionId)
  draggingEmployeeId = null
}

function handleSmartAssign() {
  const unassignedEmployees = filteredEmployees.value.filter(e => !isEmployeeAssigned(e.id))
  if (unassignedEmployees.length === 0) {
    ElMessage.info('所有员工已排班')
    return
  }
  const availablePositions = positions.value.filter(p => !isPositionOccupied(p.id))
  if (availablePositions.length === 0) {
    ElMessage.warning('没有空闲岗位')
    return
  }

  allSchedules[scheduleDate.value] = []

  const assignments = []
  const usedEmployees = new Set()
  availablePositions.forEach(pos => {
    let bestMatch = null
    let bestScore = -1
    unassignedEmployees.forEach(emp => {
      if (usedEmployees.has(emp.id)) return
      const score = calculateMatchScore(emp, pos)
      if (score > bestScore) {
        bestScore = score
        bestMatch = emp
      }
    })
    if (bestMatch && bestScore > 0) {
      assignments.push({ empId: bestMatch.id, posId: pos.id })
      usedEmployees.add(bestMatch.id)
    }
  })

  assignments.forEach(a => {
    allSchedules[scheduleDate.value].push({
      id: `sch_${scheduleDate.value}_${a.empId}_${scheduleIdCounter++}`,
      date: scheduleDate.value,
      employeeId: a.empId,
      positionId: a.posId,
    })
  })

  selectedEmployeeIds.value = []
  if (assignments.length > 0) ElMessage.success(`智能排班完成，共分配 ${assignments.length} 人`)
  else ElMessage.info('没有合适的匹配')
}

function calculateMatchScore(emp, pos) {
  let score = 0
  const matchedSkills = emp.skills.filter(sk => pos.skills.includes(sk))
  score += matchedSkills.length * 10
  if (matchedSkills.length === pos.skills.length) score += 20
  if (emp.shiftId === pos.shiftId) score += 15
  else if (pos.shiftId) score -= 10
  if (emp.type === '正式工') score += 2
  return score
}

function handleClearAll() {
  if (assignedCount.value === 0) {
    ElMessage.info('当前没有排班记录')
    return
  }
  ElMessageBox.confirm('确定要取消当前所有排班吗？', '确认操作', {
    confirmButtonText: '确认取消',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    allSchedules[scheduleDate.value] = []
    selectedEmployeeIds.value = []
    ElMessage.success('已取消所有排班')
  }).catch(() => {})
}

function handleCopySchedule() {
  showCopyDialog.value = true
  copyFromDate.value = null
}

function confirmCopySchedule() {
  if (!copyFromDate.value) {
    ElMessage.warning('请选择要复制的日期')
    return
  }
  if (copyFromDate.value === scheduleDate.value) {
    ElMessage.warning('不能复制到相同日期')
    return
  }
  const sourceSchedules = allSchedules[copyFromDate.value] || []
  if (sourceSchedules.length === 0) {
    ElMessage.info('所选日期没有排班记录')
    showCopyDialog.value = false
    return
  }
  const validEmployees = new Set(filteredEmployees.value.map(e => e.id))
  const validPositions = new Set(positions.value.map(p => p.id))
  const occupiedPositions = new Set(Object.keys(assignedMap.value))
  const newSchedules = []
  sourceSchedules.forEach(s => {
    if (validEmployees.has(s.employeeId) && validPositions.has(s.positionId) && !occupiedPositions.has(s.positionId)) {
      newSchedules.push({
        id: `sch_${scheduleDate.value}_${s.employeeId}_${scheduleIdCounter++}`,
        date: scheduleDate.value,
        employeeId: s.employeeId,
        positionId: s.positionId,
      })
      occupiedPositions.add(s.positionId)
    }
  })
  if (newSchedules.length > 0) {
    allSchedules[scheduleDate.value] = newSchedules
    ElMessage.success(`成功复制 ${newSchedules.length} 条排班记录`)
  } else {
    ElMessage.info('没有可复制的排班记录')
  }
  showCopyDialog.value = false
}

function openModifySchedule(employeeId) {
  const emp = employees.find(e => e.id === employeeId)
  if (!emp) return
  modifyEmployeeId.value = employeeId
  modifyEmployeeName.value = emp.name
  modifyTargetPositionId.value = getAssignedPositionId(employeeId)
  showModifyDialog.value = true
}

function confirmModifySchedule() {
  if (!modifyTargetPositionId.value) {
    ElMessage.warning('请选择新岗位')
    return
  }
  const oldPositionId = getAssignedPositionId(modifyEmployeeId.value)
  if (oldPositionId === modifyTargetPositionId.value) {
    ElMessage.info('岗位未变化')
    showModifyDialog.value = false
    return
  }
  if (isPositionOccupied(modifyTargetPositionId.value)) {
    ElMessage.error('该岗位已被占用，请选择其他岗位')
    return
  }
  const schedules = allSchedules[scheduleDate.value] || []
  const schedule = schedules.find(s => s.employeeId === modifyEmployeeId.value)
  if (schedule) {
    schedule.positionId = modifyTargetPositionId.value
    ElMessage.success('排班修改成功')
  }
  showModifyDialog.value = false
}

function openAddTempPosition() {
  newTempPositionName.value = ''
  newTempPositionSkills.value = []
  newTempLineId.value = selectedLineIds.value.length > 0 ? selectedLineIds.value[0] : null
  showAddTempDialog.value = true
}

function confirmAddTempPosition() {
  if (!newTempPositionName.value.trim()) {
    ElMessage.warning('请输入岗位名称')
    return
  }
  if (!newTempLineId.value) {
    ElMessage.warning('请选择线体')
    return
  }
  selectedShifts.value.forEach(shiftId => {
    positions.value.push({
      id: `pos_temp_${Date.now()}_${shiftId}_${positionIdCounter++}`,
      name: newTempPositionName.value.trim(),
      type: 'fixed',
      lineId: newTempLineId.value,
      shiftId,
      skills: newTempPositionSkills.value.length > 0 ? [...newTempPositionSkills.value] : ['装配'],
      isTemp: true,
    })
  })
  showAddTempDialog.value = false
  ElMessage.success('临时岗位添加成功')
}

function openAddPublicPosition() {
  if (publicPositionTemplates.length >= 10) {
    ElMessage.warning('最多只能添加10个公共岗位')
    return
  }
  newPublicPositionName.value = ''
  newPublicPositionSkills.value = []
  showAddPublicDialog.value = true
}

function confirmAddPublicPosition() {
  if (!newPublicPositionName.value.trim()) {
    ElMessage.warning('请输入岗位名称')
    return
  }
  publicPositionTemplates.push({
    id: `pub_tpl_${Date.now()}`,
    name: newPublicPositionName.value.trim(),
    skills: newPublicPositionSkills.value.length > 0 ? [...newPublicPositionSkills.value] : ['装配', '焊接', '质检', '调试', '包装'],
  })
  generatePositions()
  showAddPublicDialog.value = false
  ElMessage.success('公共岗位添加成功')
}

watch(scheduleDate, (newDate) => {
  if (!allSchedules[newDate]) allSchedules[newDate] = []
  selectedEmployeeIds.value = []
  selectedPositionId.value = null
})

onMounted(() => {
  selectedLineIds.value = ['line1']
  selectedShifts.value = ['shift1']
  generatePositions()
})
</script>

<style scoped>
.schedule-container {
  padding: 16px 20px 32px;
  max-width: 1600px;
  margin: 0 auto;
  background: #f6f8fc;
  border-radius: 16px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #4a7cf7, #6c9bff);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(74, 124, 247, 0.35);
}
.page-title h1 {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
  color: #1a2b4a;
  margin: 0;
}
.subtitle {
  font-size: 13px;
  color: #5a6b8c;
  font-weight: 400;
  margin-left: 8px;
}
.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-panel {
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(26, 43, 74, 0.06);
  margin-bottom: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: flex-end;
  border: 1px solid #e8ecf4;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 180px;
}
.filter-item .label {
  font-size: 12px;
  color: #5a6b8c;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}
.selected-lines-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 0;
  margin-bottom: 12px;
}
.stats-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 12px 20px;
  box-shadow: 0 2px 8px rgba(26, 43, 74, 0.06);
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e8ecf4;
  flex: 1;
  min-width: 140px;
  justify-content: center;
  transition: all 0.25s;
}
.stat-card:hover {
  box-shadow: 0 4px 16px rgba(26, 43, 74, 0.1);
  transform: translateY(-2px);
}
.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #fff;
}
.stat-icon.blue { background: linear-gradient(135deg, #4a7cf7, #6c9bff); }
.stat-icon.green { background: linear-gradient(135deg, #34c77b, #5dd99c); }
.stat-icon.orange { background: linear-gradient(135deg, #ffb74d, #ffc97d); }
.stat-icon.red { background: linear-gradient(135deg, #ff6b6b, #ff8e8e); }
.stat-value { font-size: 22px; font-weight: 700; color: #1a2b4a; }
.stat-label { font-size: 12px; color: #5a6b8c; }
.tip-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fff8e6;
  border: 1px solid #ffe4b8;
  border-radius: 6px;
  font-size: 12px;
  color: #b8860b;
  margin-bottom: 12px;
}
.main-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}
.left-panel {
  flex: 0 0 280px;
  min-width: 250px;
}
.right-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(26, 43, 74, 0.06);
  border: 1px solid #e8ecf4;
  overflow: hidden;
}
.panel-header {
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8ecf4;
  background: linear-gradient(180deg, #fafbfe, #fff);
}
.panel-title {
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1a2b4a;
}
.dot { width: 6px; height: 6px; border-radius: 50%; background: #4a7cf7; display: inline-block; }
.dot.orange { background: #ffb74d; }
.panel-body {
  padding: 8px 10px;
  max-height: 500px;
  overflow-y: auto;
}
.employee-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 6px;
}
.empty-state {
  text-align: center;
  padding: 20px;
  color: #b0b8cc;
}
.empty-icon { font-size: 32px; margin-bottom: 8px; opacity: 0.6; }
.empty-text { font-size: 12px; }
.modify-tip {
  margin-bottom: 12px;
  font-size: 13px;
  color: #5a6b8c;
}
@media (max-width: 1200px) {
  .main-content { flex-direction: column; }
  .left-panel, .right-panel { width: 100%; flex: auto; }
}
</style>
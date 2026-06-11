<template>
  <div class="dashboard-container">
    <!-- 左侧部门树（折叠/展开） -->
    <div class="left-panel" :class="{ collapsed: isLeftCollapsed }">
      <div class="dept-header">
        <span>部门架构</span>
        <el-button 
          :icon="isLeftCollapsed ? 'Expand' : 'Fold'" 
          circle 
          size="small"
          class="collapse-btn"
          @click="toggleLeftPanel"
          v-if="!isLeftCollapsed"
        />
      </div>
      <el-tree
        v-show="!isLeftCollapsed"
        :data="deptTree"
        :props="{ label: 'label', children: 'children' }"
        node-key="value"
        default-expand-all
        highlight-current
        :expand-on-click-node="false"
        @current-change="handleDeptChange"
      />
    </div>

    <!-- 悬浮展开按钮（折叠时显示） -->
    <div class="expand-btn-wrapper" v-if="isLeftCollapsed" @click="toggleLeftPanel">
      <el-button type="primary" :icon="Expand" circle size="large" />
    </div>

    <!-- 右侧内容区 -->
    <div class="right-panel" :class="{ expanded: isLeftCollapsed }">
      <div class="top-bar">
        <div class="filter-group">
          <el-date-picker v-model="dateRange" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 160px" />
          <el-select v-model="queryType" placeholder="查询类型" style="width: 160px" @change="handleQueryTypeChange">
            <el-option label="全部数据" value="all" />
            <el-option label="技能刷卡人力" value="skillSwipe" />
            <el-option label="早会点名人力" value="morningMeeting" />
            <el-option label="刷卡签到人力" value="swipeSign" />
          </el-select>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleExport">导出汇总表</el-button>
          <el-button v-if="showDetail" type="success" @click="handleExportDetail">导出当前明细</el-button>
          <el-button type="warning" @click="exportAllDetailsNative">一键导出所有明细</el-button>
        </div>
      </div>

      <!-- 中间表格（完整列） -->
      <div class="middle-table">
        <el-table :data="tableData" row-key="id" :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" border stripe>
          <el-table-column prop="deptName" label="部门名称" min-width="220" fixed="left">
            <template #default="{ row }">
              <span>{{ row.deptName }}<span style="color: #909399; font-size: 12px; margin-left: 8px;">(总出勤: {{ (row.day_actualAttendance || 0) + (row.night_actualAttendance || 0) }})</span></span>
            </template>
          </el-table-column>

          <!-- 白班 -->
          <el-table-column label="白班" align="center">
            <el-table-column prop="day_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'systemManpower', row.day_systemManpower)">{{ row.day_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'actualAttendance', row.day_actualAttendance)">{{ row.day_actualAttendance ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_skillSwipe" label="技能刷卡人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'skillSwipe', row.day_skillSwipe)">{{ row.day_skillSwipe ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_morningMeeting" label="早会点名人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'morningMeeting', row.day_morningMeeting)">{{ row.day_morningMeeting ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_swipeSign" label="刷卡签到人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'swipeSign', row.day_swipeSign)">{{ row.day_swipeSign ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_leaveManpower" label="请假人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'leaveManpower', row.day_leaveManpower)">{{ row.day_leaveManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_receiveSupport" label="接收支援" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'receiveSupport', row.day_receiveSupport)">{{ row.day_receiveSupport ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="day_outSupport" label="外出支援" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'outSupport', row.day_outSupport)">{{ row.day_outSupport ?? '-' }}</span></template></el-table-column>
          </el-table-column>

          <!-- 夜班 -->
          <el-table-column label="夜班" align="center">
            <el-table-column prop="night_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'systemManpower', row.night_systemManpower)">{{ row.night_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'actualAttendance', row.night_actualAttendance)">{{ row.night_actualAttendance ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_skillSwipe" label="技能刷卡人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'skillSwipe', row.night_skillSwipe)">{{ row.night_skillSwipe ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_morningMeeting" label="早会点名人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'morningMeeting', row.night_morningMeeting)">{{ row.night_morningMeeting ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_swipeSign" label="刷卡签到人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'swipeSign', row.night_swipeSign)">{{ row.night_swipeSign ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_leaveManpower" label="请假人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'leaveManpower', row.night_leaveManpower)">{{ row.night_leaveManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_receiveSupport" label="接收支援" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'receiveSupport', row.night_receiveSupport)">{{ row.night_receiveSupport ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_outSupport" label="外出支援" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'outSupport', row.night_outSupport)">{{ row.night_outSupport ?? '-' }}</span></template></el-table-column>
          </el-table-column>

          <!-- 汇总 -->
          <el-table-column label="汇总" align="center">
            <el-table-column prop="total_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'systemManpower', row.total_systemManpower)">{{ row.total_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'actualAttendance', row.total_actualAttendance)">{{ row.total_actualAttendance ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_leaveManpower" label="请假人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'leaveManpower', row.total_leaveManpower)">{{ row.total_leaveManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_receiveSupport" label="接收支援" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'receiveSupport', row.total_receiveSupport)">{{ row.total_receiveSupport ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_outSupport" label="外出支援" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'outSupport', row.total_outSupport)">{{ row.total_outSupport ?? '-' }}</span></template></el-table-column>
          </el-table-column>
        </el-table>
      </div>

      <!-- 明细面板（带分页和加载） -->
      <div class="bottom-detail" v-if="showDetail">
        <div class="detail-header">
          <el-icon><InfoFilled /></el-icon>
          <span class="title-info">{{ detailTitle }}</span>
          <el-button type="text" @click="closeDetail" style="margin-left: auto;">关闭</el-button>
        </div>
        <el-table :data="paginatedDetailData" border stripe size="small" v-loading="detailLoading" element-loading-text="加载明细中...">
          <template v-for="col in detailColumns" :key="col.prop">
            <el-table-column :prop="col.prop" :label="col.label" :width="col.width" />
          </template>
        </el-table>
        <div class="pagination-container" v-if="detailData.length > 0">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="detailData.length"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, InfoFilled, Expand, Fold } from '@element-plus/icons-vue'

// ---------- 部门树数据（示例，实际可从接口获取）----------
const deptTree = ref([
  {
    label: '生产部',
    value: 'PROD',
    level: 3,
    children: [
      { label: '组装一科', value: 'PROD_ASSY1', level: 4, children: [
        { label: '组装线A班', value: 'PROD_ASSY1_A', level: 5, children: [] },
        { label: '组装线B班', value: 'PROD_ASSY1_B', level: 5, children: [] }
      ]},
      { label: '组装二科', value: 'PROD_ASSY2', level: 4, children: [
        { label: '包装线', value: 'PROD_ASSY2_PACK', level: 5, children: [] }
      ]},
      { label: '测试科', value: 'PROD_TEST', level: 4, children: [] }
    ]
  },
  {
    label: '质量部',
    value: 'QA',
    level: 3,
    children: [
      { label: 'IQC科', value: 'QA_IQC', level: 4, children: [] },
      { label: 'OQC科', value: 'QA_OQC', level: 4, children: [] }
    ]
  },
  {
    label: '供应链部',
    value: 'SCM',
    level: 3,
    children: []
  }
])

// 左侧折叠状态
const isLeftCollapsed = ref(false)

// 当前选中部门、日期、查询类型
const selectedDept = ref(null)
const dateRange = ref(new Date().toISOString().slice(0,10))
const queryType = ref('all')

// 原始数据存储
const rawData = reactive({
  empPerPersonMehrList: [],
  manpowerDetailList: [],
  swipeCardSignList: [],
  staffScheduleInfoList: [],
  swipeCardOnboardList: []
})

// 请假/支援列表（模拟数据，实际从接口获取）
const leaveList = ref([
  { userId: 'EMP101', userName: '张明', type: 3, status: '请假', fromOrgCode: 'PROD_ASSY1', fromOrgName: '组装一科', toOrgCode: 'PROD_ASSY1', toOrgName: '组装一科', shiftId: 1 },
  { userId: 'EMP102', userName: '李芳', type: 1, status: '支援', fromOrgCode: 'QA_IQC', fromOrgName: 'IQC科', toOrgCode: 'PROD_ASSY1', toOrgName: '组装一科', shiftId: 1 },
  { userId: 'EMP103', userName: '王磊', type: 0, status: '发布未接收', fromOrgCode: 'PROD_ASSY2', fromOrgName: '组装二科', toOrgCode: 'SCM', toOrgName: '供应链部', shiftId: 2 },
  { userId: 'EMP104', userName: '赵丽', type: 1, status: '支援', fromOrgCode: 'PROD_TEST', fromOrgName: '测试科', toOrgCode: 'QA_IQC', toOrgName: 'IQC科', shiftId: 1 },
  { userId: 'EMP105', userName: '孙强', type: 3, status: '请假', fromOrgCode: 'PROD_ASSY1_A', fromOrgName: '组装线A班', toOrgCode: 'PROD_ASSY1_A', toOrgName: '组装线A班', shiftId: 2 }
])

// 右侧表格数据
const tableData = ref([])

// 明细相关
const showDetail = ref(false)
const detailData = ref([])
const detailTitle = ref('')
const detailColumns = ref([])
const detailLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedDetailData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return detailData.value.slice(start, start + pageSize.value)
})

// 缓存
const metricsCache = new Map()
const getDescendantValuesCache = new Map()

// ---------- 辅助函数 ----------
const getAllDescendantValues = (deptNode) => {
  if (getDescendantValuesCache.has(deptNode.value)) {
    return getDescendantValuesCache.get(deptNode.value)
  }
  let values = [deptNode.value]
  if (deptNode.children && deptNode.children.length) {
    deptNode.children.forEach(child => {
      values = values.concat(getAllDescendantValues(child))
    })
  }
  getDescendantValuesCache.set(deptNode.value, values)
  return values
}

const isEmployeeBelongToDeptSet = (emp, deptValuesSet) => {
  return deptValuesSet.has(emp.l3OrgCode) || deptValuesSet.has(emp.l4OrgCode) || 
         deptValuesSet.has(emp.l5OrgCode) || deptValuesSet.has(emp.organizationCode)
}

// 根据部门节点、班次、类型获取 leave 记录
const getLeaveRecordsByDeptAndShift = (deptNode, shiftId, type) => {
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  return leaveList.value.filter(record => {
    if (record.shiftId !== shiftId) return false
    if (type === 'leave') return record.type === 3 && deptValueSet.has(record.toOrgCode)
    if (type === 'receive') return record.type === 1 && deptValueSet.has(record.toOrgCode)
    if (type === 'out') return record.type === 1 && deptValueSet.has(record.fromOrgCode)
    return false
  })
}

// 模拟生成其他数据（系统人力、早会点名等）
const generateEmployeeList = () => {
  // 与原代码相同，略...
  return []
}
const generateManpowerDetailList = (employees) => { /* 略 */ return [] }
const generateSwipeCardSignList = (employees) => { /* 略 */ return [] }
const generateStaffScheduleInfoList = (employees) => { /* 略 */ return [] }
const generateSwipeCardOnboardList = (staffScheduleList) => { /* 略 */ return [] }

// 模拟接口调用
const fetchData = async (params) => {
  // 实际替换为 axios 请求
  const allEmployees = generateEmployeeList()
  // 根据部门过滤...
  return {
    empPerPersonMehrList: allEmployees,
    manpowerDetailList: generateManpowerDetailList(allEmployees),
    swipeCardSignList: generateSwipeCardSignList(allEmployees),
    staffScheduleInfoList: generateStaffScheduleInfoList(allEmployees),
    swipeCardOnboardList: []
  }
}

const enhanceStaffScheduleWithCardStatus = (scheduleList, onboardList) => {
  // 与原代码相同
}

// 加载数据
const loadData = async () => {
  if (!selectedDept.value) return
  const dept = selectedDept.value
  let params = { shiftDate: dateRange.value }
  if (dept.level === 3) params.l3OrgCode = dept.value
  else if (dept.level === 4) params.l4OrgCode = dept.value
  else if (dept.level === 5) params.l5OrgCode = dept.value
  else params.organizationCode = dept.value
  
  const result = await fetchData(params)
  rawData.empPerPersonMehrList = result.empPerPersonMehrList
  rawData.manpowerDetailList = result.manpowerDetailList
  rawData.swipeCardSignList = result.swipeCardSignList
  rawData.staffScheduleInfoList = result.staffScheduleInfoList
  rawData.swipeCardOnboardList = result.swipeCardOnboardList
  enhanceStaffScheduleWithCardStatus(rawData.staffScheduleInfoList, rawData.swipeCardOnboardList)
}

// 计算部门指标（核心）
const computeDeptMetrics = (deptNode, shiftId) => {
  const cacheKey = `${deptNode.value}_${shiftId}_${queryType.value}`
  if (metricsCache.has(cacheKey)) return metricsCache.get(cacheKey)
  
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  
  // 系统人力
  const systemEmpList = rawData.empPerPersonMehrList.filter(emp => isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId)
  const systemManpower = systemEmpList.length
  
  // 技能刷卡
  const skillSwipedSet = new Set()
  rawData.staffScheduleInfoList.forEach(s => {
    if (s.status === '已刷卡' && isEmployeeBelongToDeptSet(s, deptValueSet) && s.shiftId === shiftId) {
      skillSwipedSet.add(s.employeeNo)
    }
  })
  const skillSwipe = skillSwipedSet.size
  
  // 早会点名（非请假）
  const meetingSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId && emp.WorkFlag !== 3) {
      meetingSet.add(emp.employeeNo)
    }
  })
  const morningMeeting = meetingSet.size
  
  // 刷卡签到
  const signSet = new Set()
  rawData.swipeCardSignList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      signSet.add(emp.employeeNo)
    }
  })
  const swipeSign = signSet.size
  
  // 请假人力（从 leaveList）
  const leaveRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'leave')
  const leaveManpower = leaveRecords.length
  
  // 接收支援人力
  const receiveRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'receive')
  const receiveSupport = receiveRecords.length
  
  // 外出支援人力
  const outRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'out')
  const outSupport = outRecords.length
  
  // 实出勤人力（ESD + 早会点名 + 刷卡签到 + 技能刷卡 + 接收支援中已刷卡的人，排除请假）
  const actualSet = new Set()
  rawData.empPerPersonMehrList.forEach(emp => {
    if (emp.lasttimeEsd && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      actualSet.add(emp.employeeNo)
    }
  })
  meetingSet.forEach(no => actualSet.add(no))
  signSet.forEach(no => actualSet.add(no))
  skillSwipedSet.forEach(no => actualSet.add(no))
  // 接收支援中已刷卡的人加入实出勤
  receiveRecords.forEach(rec => {
    const hasCard = rawData.staffScheduleInfoList.some(s => s.employeeNo === rec.userId && s.shiftId === shiftId && s.status === '已刷卡')
    if (hasCard) actualSet.add(rec.userId)
  })
  // 排除请假人员（从 leaveList 中 type=3 且班次匹配）
  const leaveSet = new Set(leaveRecords.map(r => r.userId))
  const actualAttendance = [...actualSet].filter(no => !leaveSet.has(no)).length
  
  const result = { systemManpower, actualAttendance, skillSwipe, morningMeeting, swipeSign, leaveManpower, receiveSupport, outSupport }
  metricsCache.set(cacheKey, result)
  return result
}

// 构建表格行
const buildTableTree = (deptNode) => {
  if (!deptNode) return null
  const dayMetrics = computeDeptMetrics(deptNode, 1)
  const nightMetrics = computeDeptMetrics(deptNode, 2)
  const row = {
    id: deptNode.value,
    deptName: deptNode.label,
    day_systemManpower: dayMetrics.systemManpower,
    day_actualAttendance: dayMetrics.actualAttendance,
    day_skillSwipe: dayMetrics.skillSwipe,
    day_morningMeeting: dayMetrics.morningMeeting,
    day_swipeSign: dayMetrics.swipeSign,
    day_leaveManpower: dayMetrics.leaveManpower,
    day_receiveSupport: dayMetrics.receiveSupport,
    day_outSupport: dayMetrics.outSupport,
    night_systemManpower: nightMetrics.systemManpower,
    night_actualAttendance: nightMetrics.actualAttendance,
    night_skillSwipe: nightMetrics.skillSwipe,
    night_morningMeeting: nightMetrics.morningMeeting,
    night_swipeSign: nightMetrics.swipeSign,
    night_leaveManpower: nightMetrics.leaveManpower,
    night_receiveSupport: nightMetrics.receiveSupport,
    night_outSupport: nightMetrics.outSupport,
    total_systemManpower: dayMetrics.systemManpower + nightMetrics.systemManpower,
    total_actualAttendance: dayMetrics.actualAttendance + nightMetrics.actualAttendance,
    total_leaveManpower: dayMetrics.leaveManpower + nightMetrics.leaveManpower,
    total_receiveSupport: dayMetrics.receiveSupport + nightMetrics.receiveSupport,
    total_outSupport: dayMetrics.outSupport + nightMetrics.outSupport,
    children: []
  }
  if (deptNode.children && deptNode.children.length) {
    row.children = deptNode.children.map(child => buildTableTree(child)).filter(c => c)
  }
  return row
}

// 刷新表格
const refreshTableData = async () => {
  if (!selectedDept.value) return
  metricsCache.clear()
  getDescendantValuesCache.clear()
  await loadData()
  const treeRoot = buildTableTree(selectedDept.value)
  tableData.value = treeRoot ? [treeRoot] : []
}

// 部门点击
const handleDeptChange = (data) => {
  selectedDept.value = data
  refreshTableData()
  closeDetail()
}

const handleQueryTypeChange = () => refreshTableData()
const handleQuery = () => refreshTableData()

// 导出汇总表（与原代码相同，略）
const handleExport = () => { /* 实现 */ }
const handleExportDetail = () => { /* 实现 */ }

// 获取明细员工（用于明细面板）
const getAllEmployeesInDept = (deptNode, shiftId, type) => {
  const deptValuesSet = new Set(getAllDescendantValues(deptNode))
  let employeeMap = new Map()
  const addEmp = (emp) => {
    if (!employeeMap.has(emp.employeeNo) && emp.shiftId === shiftId && isEmployeeBelongToDeptSet(emp, deptValuesSet)) {
      employeeMap.set(emp.employeeNo, { ...emp })
    }
  }
  if (type === 'systemManpower') {
    rawData.empPerPersonMehrList.forEach(emp => addEmp(emp))
  } else if (type === 'actualAttendance') {
    rawData.empPerPersonMehrList.filter(emp => emp.lasttimeEsd).forEach(emp => addEmp(emp))
    rawData.manpowerDetailList.filter(emp => emp.WorkFlag !== 3).forEach(emp => addEmp(emp))
    rawData.swipeCardSignList.forEach(emp => addEmp(emp))
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
    // 接收支援已刷卡
    const receiveRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'receive')
    receiveRecords.forEach(rec => {
      const hasCard = rawData.staffScheduleInfoList.some(s => s.employeeNo === rec.userId && s.shiftId === shiftId && s.status === '已刷卡')
      if (hasCard && !employeeMap.has(rec.userId)) {
        employeeMap.set(rec.userId, { employeeName: rec.userName, employeeNo: rec.userId, ...rec })
      }
    })
  } else if (type === 'skillSwipe') {
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
  } else if (type === 'morningMeeting') {
    rawData.manpowerDetailList.filter(emp => emp.WorkFlag !== 3).forEach(emp => addEmp(emp))
  } else if (type === 'swipeSign') {
    rawData.swipeCardSignList.forEach(emp => addEmp(emp))
  } else if (type === 'leaveManpower') {
    const leaveRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'leave')
    leaveRecords.forEach(rec => {
      if (!employeeMap.has(rec.userId)) {
        employeeMap.set(rec.userId, { employeeName: rec.userName, employeeNo: rec.userId, ...rec })
      }
    })
  } else if (type === 'receiveSupport') {
    const receiveRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'receive')
    receiveRecords.forEach(rec => {
      if (!employeeMap.has(rec.userId)) {
        employeeMap.set(rec.userId, { employeeName: rec.userName, employeeNo: rec.userId, ...rec })
      }
    })
  } else if (type === 'outSupport') {
    const outRecords = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'out')
    outRecords.forEach(rec => {
      if (!employeeMap.has(rec.userId)) {
        employeeMap.set(rec.userId, { employeeName: rec.userName, employeeNo: rec.userId, ...rec })
      }
    })
  }
  return Array.from(employeeMap.values())
}

/**
 * 生成明细数据（支持班次合并）
 * @param {Object} deptNode - 当前部门节点
 * @param {string} shift - 'day', 'night', 或 'total'
 * @param {string} metricType - 指标类型
 */
const generateDetailData = (deptNode, shift, metricType) => {
  const isTotal = shift === 'total';
  const shiftsToProcess = isTotal ? ['day', 'night'] : [shift];
  let allRows = [];

  // 基础人力指标（需要从员工数据中获取）
  const basicMetrics = ['systemManpower', 'actualAttendance', 'skillSwipe', 'morningMeeting', 'swipeSign'];
  // 支援/请假指标（从 leaveList 获取）
  const supportMetrics = ['leaveManpower', 'receiveSupport', 'outSupport'];

  if (basicMetrics.includes(metricType)) {
    for (const s of shiftsToProcess) {
      const shiftId = s === 'day' ? 1 : 2;
      const employees = getAllEmployeesInDept(deptNode, shiftId, metricType);
      employees.forEach(emp => {
        allRows.push({
          ...emp,
          _shiftText: s === 'day' ? '白班' : '夜班'
        });
      });
    }
  } else if (supportMetrics.includes(metricType)) {
    for (const s of shiftsToProcess) {
      const shiftId = s === 'day' ? 1 : 2;
      let records = [];
      if (metricType === 'leaveManpower') {
        records = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'leave');
      } else if (metricType === 'receiveSupport') {
        records = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'receive');
      } else if (metricType === 'outSupport') {
        records = getLeaveRecordsByDeptAndShift(deptNode, shiftId, 'out');
      }
      records = records.map(rec => ({ ...rec, _shiftText: s === 'day' ? '白班' : '夜班' }));
      allRows.push(...records);
    }
  } else {
    return { columns: [], dataRows: [] };
  }

  // 根据 metricType 构建列定义和数据行
  let columns = [];
  let dataRows = [];

  if (metricType === 'systemManpower') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 }
    ];
    dataRows = allRows.map(row => ({
      _shiftText: row._shiftText,
      l3OrganizationCn: row.l3OrganizationCn,
      l4OrganizationCn: row.l4OrganizationCn,
      l5OrganizationCn: row.l5OrganizationCn,
      organizationNameCn: row.organizationNameCn,
      employeeName: row.employeeName,
      employeeNo: row.employeeNo
    }));
  } 
  else if (metricType === 'actualAttendance') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'esdStatus', label: 'ESD状态', width: 100 }
    ];
    if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
      columns.push({ prop: 'meetingStatus', label: '班会点名', width: 120 });
      columns.push({ prop: 'meetingTime', label: '点名时间', width: 160 });
    }
    if (queryType.value === 'all' || queryType.value === 'swipeSign') {
      columns.push({ prop: 'signStatus', label: '刷卡签到', width: 120 });
      columns.push({ prop: 'signTime', label: '签到时间', width: 160 });
    }
    if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
      columns.push({ prop: 'skillStatus', label: '技能刷卡', width: 120 });
      columns.push({ prop: 'skillTime', label: '刷卡时间', width: 160 });
    }
    if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
      columns.push({ prop: 'submitter', label: '提交人', width: 100 });
    }

    dataRows = allRows.map(row => {
      const shiftId = row._shiftText === '白班' ? 1 : 2;
      const meetingRec = rawData.manpowerDetailList.find(m => m.employeeNo === row.employeeNo && m.shiftId === shiftId);
      const signRec = rawData.swipeCardSignList.find(s => s.employeeNo === row.employeeNo && s.shiftId === shiftId);
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === row.employeeNo && s.shiftId === shiftId && s.status === '已刷卡');
      const esdStatus = row.lasttimeEsd ? '已测' : '未测';
      
      const dataRow = {
        _shiftText: row._shiftText,
        l3OrganizationCn: row.l3OrganizationCn,
        l4OrganizationCn: row.l4OrganizationCn,
        l5OrganizationCn: row.l5OrganizationCn,
        organizationNameCn: row.organizationNameCn,
        employeeName: row.employeeName,
        employeeNo: row.employeeNo,
        esdStatus
      };
      if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
        dataRow.meetingStatus = meetingRec ? '已点名' : '未点名';
        dataRow.meetingTime = meetingRec ? meetingRec.createTime : '';
        dataRow.submitter = meetingRec ? meetingRec.createBy : '';
      }
      if (queryType.value === 'all' || queryType.value === 'swipeSign') {
        dataRow.signStatus = signRec ? '已签到' : '未签到';
        dataRow.signTime = signRec ? signRec.checkTime : '';
      }
      if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
        dataRow.skillStatus = skillRec ? '已刷卡' : '未刷卡';
        dataRow.skillTime = skillRec ? skillRec.loginTime : '';
      }
      return dataRow;
    });
  }
  else if (metricType === 'skillSwipe') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'loginTime', label: '刷卡时间', width: 160 }
    ];
    dataRows = allRows.map(row => ({
      _shiftText: row._shiftText,
      l3OrganizationCn: row.l3OrganizationCn,
      l4OrganizationCn: row.l4OrganizationCn,
      l5OrganizationCn: row.l5OrganizationCn,
      organizationNameCn: row.organizationNameCn,
      employeeName: row.employeeName,
      employeeNo: row.employeeNo,
      loginTime: row.loginTime || ''
    }));
  }
  else if (metricType === 'morningMeeting') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'createTime', label: '点名时间', width: 160 },
      { prop: 'submitter', label: '提交人', width: 100 }
    ];
    dataRows = allRows.map(row => ({
      _shiftText: row._shiftText,
      l3OrganizationCn: row.l3OrganizationCn,
      l4OrganizationCn: row.l4OrganizationCn,
      l5OrganizationCn: row.l5OrganizationCn,
      organizationNameCn: row.organizationNameCn,
      employeeName: row.employeeName,
      employeeNo: row.employeeNo,
      createTime: row.createTime || '',
      submitter: row.createBy || ''
    }));
  }
  else if (metricType === 'swipeSign') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'signTime', label: '签到时间', width: 160 }
    ];
    dataRows = allRows.map(row => ({
      _shiftText: row._shiftText,
      l3OrganizationCn: row.l3OrganizationCn,
      l4OrganizationCn: row.l4OrganizationCn,
      l5OrganizationCn: row.l5OrganizationCn,
      organizationNameCn: row.organizationNameCn,
      employeeName: row.employeeName,
      employeeNo: row.employeeNo,
      signTime: row.signTime || ''
    }));
  }
  else if (metricType === 'leaveManpower') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'userName', label: '姓名', width: 100 },
      { prop: 'userId', label: '工号', width: 120 },
      { prop: 'toOrgName', label: '请假部门', width: 150 },
      { prop: 'status', label: '状态', width: 100 }
    ];
    dataRows = allRows.map(r => ({
      _shiftText: r._shiftText,
      userName: r.userName,
      userId: r.userId,
      toOrgName: r.toOrgName,
      status: r.status
    }));
  }
  else if (metricType === 'receiveSupport') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'userName', label: '姓名', width: 100 },
      { prop: 'userId', label: '工号', width: 120 },
      { prop: 'fromOrgName', label: '来源部门', width: 150 },
      { prop: 'toOrgName', label: '接收部门', width: 150 },
      { prop: 'status', label: '状态', width: 100 },
      { prop: 'isAttended', label: '是否出勤', width: 100 }
    ];
    dataRows = allRows.map(r => ({
      _shiftText: r._shiftText,
      userName: r.userName,
      userId: r.userId,
      fromOrgName: r.fromOrgName,
      toOrgName: r.toOrgName,
      status: r.status,
      isAttended: r.isAttended || '未出勤'
    }));
  }
  else if (metricType === 'outSupport') {
    columns = [
      { prop: '_shiftText', label: '班次', width: 80 },
      { prop: 'userName', label: '姓名', width: 100 },
      { prop: 'userId', label: '工号', width: 120 },
      { prop: 'fromOrgName', label: '派出部门', width: 150 },
      { prop: 'toOrgName', label: '目标部门', width: 150 },
      { prop: 'status', label: '状态', width: 100 }
    ];
    dataRows = allRows.map(r => ({
      _shiftText: r._shiftText,
      userName: r.userName,
      userId: r.userId,
      fromOrgName: r.fromOrgName,
      toOrgName: r.toOrgName,
      status: r.status
    }));
  }

  return { columns, dataRows };
};

// 点击数字查看明细
const handleNumberClick = async (row, shift, metricType, value) => {
  const findDeptNode = (nodes, id) => {
    for (let node of nodes) {
      if (node.value === id) return node
      if (node.children) {
        const found = findDeptNode(node.children, id)
        if (found) return found
      }
    }
    return null
  }
  const deptNode = findDeptNode(deptTree.value, row.id)
  if (!deptNode) return
  
  detailLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 50))
  
  const shiftText = shift === 'day' ? '白班' : (shift === 'night' ? '夜班' : '合计')
  const metricMap = {
    systemManpower: '系统人力',
    actualAttendance: '实出勤人力',
    skillSwipe: '技能刷卡人力',
    morningMeeting: '早会点名人力',
    swipeSign: '刷卡签到人力',
    leaveManpower: '请假人力',
    receiveSupport: '接收支援人力',
    outSupport: '外出支援人力'
  }
  const title = `${row.deptName} (${shiftText}) · ${metricMap[metricType]}: ${value}`
  const { columns, dataRows } = generateDetailData(deptNode, shift, metricType)
  detailColumns.value = columns
  detailData.value = dataRows
  detailTitle.value = title
  showDetail.value = true
  currentPage.value = 1
  detailLoading.value = false
}

const closeDetail = () => {
  showDetail.value = false
  detailData.value = []
  detailColumns.value = []
  detailTitle.value = ''
}

// 导出所有明细（多文件，原生 HTML）
const exportAllDetailsNative = async () => {
  if (!selectedDept.value) {
    ElMessage.warning('请先选择一个部门')
    return
  }
  const deptNode = selectedDept.value
  const categories = [
    { name: '系统人力', metricKey: 'systemManpower', sheetName: '系统人力' },
    { name: '实出勤人力', metricKey: 'actualAttendance', sheetName: '实出勤人力' },
    { name: '技能刷卡人力', metricKey: 'skillSwipe', sheetName: '技能刷卡人力' },
    { name: '刷卡签到人力', metricKey: 'swipeSign', sheetName: '刷卡签到人力' },
    { name: '早会点名人力', metricKey: 'morningMeeting', sheetName: '早会点名人力' },
    { name: '请假人力', metricKey: 'leaveManpower', sheetName: '请假人力' },
    { name: '接收支援人力', metricKey: 'receiveSupport', sheetName: '接收支援人力' },
    { name: '外出支援人力', metricKey: 'outSupport', sheetName: '外出支援人力' }
  ]
  for (const cat of categories) {
    const allDetails = []
    const collectDetails = (node, level = 0) => {
      const shifts = ['day', 'night']
      shifts.forEach(shift => {
        const shiftId = shift === 'day' ? 1 : 2
        const employees = getAllEmployeesInDept(node, shiftId, cat.metricKey)
        employees.forEach(emp => {
          const detail = {
            '部门层级': '-'.repeat(level) + node.label,
            '班次': shift === 'day' ? '白班' : '夜班',
            '姓名': emp.employeeName,
            '工号': emp.employeeNo,
            '部门': emp.organizationNameCn
          }
          if (cat.metricKey === 'receiveSupport') {
            detail['来源部门'] = emp.fromOrgName
            detail['接收部门'] = emp.toOrgName
            detail['状态'] = emp.status
            detail['是否出勤'] = rawData.staffScheduleInfoList.some(s => s.employeeNo === emp.employeeNo && s.shiftId === shiftId && s.status === '已刷卡') ? '已出勤' : '未出勤'
          } else if (cat.metricKey === 'outSupport') {
            detail['派出部门'] = emp.fromOrgName
            detail['目标部门'] = emp.toOrgName
            detail['状态'] = emp.status
          } else if (cat.metricKey === 'leaveManpower') {
            detail['请假部门'] = emp.toOrgName
            detail['状态'] = emp.status
          }
          allDetails.push(detail)
        })
      })
      if (node.children) node.children.forEach(child => collectDetails(child, level + 1))
    }
    collectDetails(deptNode)
    if (allDetails.length === 0) continue
    // 使用原生导出
    const columns = Object.keys(allDetails[0]).map(key => ({ label: key, prop: key }))
    exportToXLS(allDetails, columns, cat.sheetName)
    await new Promise(resolve => setTimeout(resolve, 500))
  }
  ElMessage.success('已开始下载所有分类明细文件')
}

const exportToXLS = (data, columns, fileName) => {
  let html = `<!DOCTYPE html><html><head><meta charset="UTF-8"><title>${fileName}</title><style>th{background:#f2f2f2;font-weight:bold;border:1px solid #ccc;padding:6px;}td{border:1px solid #ccc;padding:4px;}table{border-collapse:collapse;width:100%;}</style></head><body><table><thead><tr>`
  columns.forEach(col => { html += `<th style="font-weight:bold;">${col.label}</th>` })
  html += `</tr></thead><tbody>`
  data.forEach(row => {
    html += `<tr>`
    columns.forEach(col => { html += `<td>${row[col.prop] ?? ''}</td>` })
    html += `</tr>`
  })
  html += `</tbody></table></body></html>`
  const blob = new Blob([html], { type: 'application/vnd.ms-excel' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.href = url
  link.download = `${fileName}_${dateRange.value}.xls`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const toggleLeftPanel = () => { isLeftCollapsed.value = !isLeftCollapsed.value }
const handleSizeChange = (val) => { pageSize.value = val; currentPage.value = 1 }
const handleCurrentChange = (val) => { currentPage.value = val }

onMounted(async () => {
  if (deptTree.value.length) {
    selectedDept.value = deptTree.value[0]
    await refreshTableData()
  }
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.left-panel {
  width: 20%;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: auto;
  padding: 16px 12px;
}

.dept-header {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #303133;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.right-panel {
  width: 80%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #ffffff;
}

.top-bar {
  padding: 16px 20px;
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.middle-table {
  flex: 1;
  padding: 16px 20px;
  overflow: auto;
  background-color: #fff;
}

.clickable-number {
  cursor: pointer;
  color: #409eff;
  font-weight: 500;
  display: inline-block;
  width: 100%;
}
.clickable-number:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.bottom-detail {
  border-top: 2px solid #e4e7ed;
  background-color: #f9fafc;
  padding: 12px 20px 20px 20px;
  max-height: 45%;
  overflow: auto;
  transition: all 0.2s;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #dcdfe6;
}

.detail-header .title-info {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  background: #ecf5ff;
  padding: 4px 12px;
  border-radius: 16px;
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}
</style>
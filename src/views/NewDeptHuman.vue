<template>
  <div class="dashboard-container">
    <!-- 左侧部门树（支持折叠） -->
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
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <el-icon><Folder /></el-icon>
            <span>{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </div>

    <!-- 悬浮展开按钮（仅当左侧折叠时显示） -->
    <div class="expand-btn-wrapper" v-if="isLeftCollapsed" @click="toggleLeftPanel">
      <el-button type="primary" :icon="Expand" circle size="large">展开</el-button>
    </div>

    <!-- 右侧内容区 -->
    <div class="right-panel" :class="{ expanded: isLeftCollapsed }">
      <!-- 筛选栏 -->
      <div class="top-bar">
        <div class="filter-group">
          <el-date-picker
            v-model="dateRange"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 160px"
          />
          <el-select v-model="queryType" placeholder="查询类型" style="width: 160px" @change="handleQueryTypeChange">
            <el-option label="全部数据" value="all" />
            <el-option label="技能刷卡人力" value="skillSwipe" />
            <el-option label="早会点名人力" value="morningMeeting" />
            <el-option label="刷卡签到人力" value="swipeSign" />
          </el-select>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleExport">导出汇总表</el-button>
          <el-button v-if="showDetail" type="success" @click="handleExportDetail">导出明细</el-button>
        </div>
      </div>

      <!-- 中间：出勤数据表格（只保留系统人力、实出勤人力） -->
      <div class="middle-table">
        <el-table
          :data="tableData"
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          border
          stripe
        >
          <el-table-column prop="deptName" label="部门名称" min-width="220" fixed="left">
            <template #default="{ row }">
              <span>
                {{ row.deptName }}
                <span style="color: #909399; font-size: 12px; margin-left: 8px;">
                  (总出勤: {{ (row.day_actualAttendance || 0) + (row.night_actualAttendance || 0) }})
                </span>
              </span>
            </template>
          </el-table-column>

          <!-- 白班列组（只显示系统人力和实出勤人力） -->
          <el-table-column label="白班" align="center">
            <el-table-column prop="day_systemManpower" label="系统人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'systemManpower', row.day_systemManpower)">
                  {{ row.day_systemManpower ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="day_actualAttendance" label="实出勤人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'actualAttendance', row.day_actualAttendance)">
                  {{ row.day_actualAttendance ?? '-' }}
                </span>
              </template>
            </el-table-column>
          </el-table-column>

          <!-- 夜班列组 -->
          <el-table-column label="夜班" align="center">
            <el-table-column prop="night_systemManpower" label="系统人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'systemManpower', row.night_systemManpower)">
                  {{ row.night_systemManpower ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="night_actualAttendance" label="实出勤人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'actualAttendance', row.night_actualAttendance)">
                  {{ row.night_actualAttendance ?? '-' }}
                </span>
              </template>
            </el-table-column>
          </el-table-column>
        </el-table>
      </div>

      <!-- 明细面板（带分页和加载效果） -->
      <div class="bottom-detail" v-if="showDetail">
        <div class="detail-header">
          <el-icon><InfoFilled /></el-icon>
          <span class="title-info">{{ detailTitle }}</span>
          <el-button type="text" @click="closeDetail" style="margin-left: auto;">关闭</el-button>
        </div>
        <el-table 
          :data="paginatedDetailData" 
          border stripe 
          size="small" 
          v-loading="detailLoading" 
          element-loading-text="加载明细中..."
        >
          <template v-for="col in detailColumns" :key="col.prop">
            <el-table-column :prop="col.prop" :label="col.label" :width="col.width" />
          </template>
        </el-table>
        <!-- 分页组件 -->
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
import { Folder, InfoFilled } from '@element-plus/icons-vue'
// import * as XLSX from 'xlsx'

// ---------- 1. 模拟部门树（label, value, level）----------
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

// 左侧面板折叠状态
const isLeftCollapsed = ref(false)

// 当前选中部门
const selectedDept = ref(null)
// 查询日期
const dateRange = ref(new Date().toISOString().slice(0,10))
// 查询类型（新增）
const queryType = ref('all') // all, skillSwipe, morningMeeting, swipeSign

// 原始数据存储（动态加载，根据queryType决定哪些数据源非空）
const rawData = reactive({
  empPerPersonMehrList: [],      // 系统人力原始数据（始终加载）
  manpowerDetailList: [],        // 早会点名原始数据（仅当queryType为all或morningMeeting时加载）
  swipeCardSignList: [],         // 刷卡签到原始数据（仅当queryType为all或swipeSign时加载）
  staffScheduleInfoList: [],     // 岗位排班数据（技能刷卡）（仅当queryType为all或skillSwipe时加载）
  swipeCardOnboardList: []       // 岗位刷卡数据（用于匹配技能刷卡，仅当需要时加载）
})

// 右侧表格数据
const tableData = ref([])

// 明细相关
const showDetail = ref(false)
const detailData = ref([])
const detailTitle = ref('')
const detailColumns = ref([])
const detailLoading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const paginatedDetailData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return detailData.value.slice(start, end)
})

// 缓存部门节点汇总结果
const metricsCache = new Map()
const getDescendantValuesCache = new Map()

// ---------- 2. 模拟数据生成（根据queryType只生成需要的数据源）----------
// 生成员工基础信息（始终需要）
const generateEmployeeList = () => {
  const employees = []
  const namePool = ['张明', '李芳', '王磊', '陈丽', '赵岩', '周强', '吴迪', '郑爽', '孙阳', '林欣', '郭峰', '唐雅']
  const deptMap = {
    'PROD_ASSY1_A': { l3: 'PROD', l4: 'PROD_ASSY1', l5: 'PROD_ASSY1_A', orgCode: 'PROD_ASSY1_A', orgName: '组装线A班' },
    'PROD_ASSY1_B': { l3: 'PROD', l4: 'PROD_ASSY1', l5: 'PROD_ASSY1_B', orgCode: 'PROD_ASSY1_B', orgName: '组装线B班' },
    'PROD_ASSY2_PACK': { l3: 'PROD', l4: 'PROD_ASSY2', l5: 'PROD_ASSY2_PACK', orgCode: 'PROD_ASSY2_PACK', orgName: '包装线' },
    'PROD_TEST': { l3: 'PROD', l4: 'PROD_TEST', l5: null, orgCode: 'PROD_TEST', orgName: '测试科' },
    'QA_IQC': { l3: 'QA', l4: 'QA_IQC', l5: null, orgCode: 'QA_IQC', orgName: 'IQC科' },
    'QA_OQC': { l3: 'QA', l4: 'QA_OQC', l5: null, orgCode: 'QA_OQC', orgName: 'OQC科' },
    'SCM': { l3: 'SCM', l4: null, l5: null, orgCode: 'SCM', orgName: '供应链部' }
  }
  for (let i = 1; i <= 50; i++) {
    const deptKeys = Object.keys(deptMap)
    const deptKey = deptKeys[i % deptKeys.length]
    const dept = deptMap[deptKey]
    employees.push({
      employeeName: namePool[i % namePool.length] + (i + 1),
      employeeNo: `EMP${String(i+100).slice(1)}`,
      l3OrgCode: dept.l3,
      l3OrganizationCn: dept.l3 === 'PROD' ? '生产部' : (dept.l3 === 'QA' ? '质量部' : '供应链部'),
      l4OrgCode: dept.l4,
      l4OrganizationCn: dept.l4 ? (dept.l4 === 'PROD_ASSY1' ? '组装一科' : (dept.l4 === 'PROD_ASSY2' ? '组装二科' : (dept.l4 === 'PROD_TEST' ? '测试科' : (dept.l4 === 'QA_IQC' ? 'IQC科' : 'OQC科')))) : null,
      l5OrgCode: dept.l5,
      l5OrganizationCn: dept.l5 === 'PROD_ASSY1_A' ? '组装线A班' : (dept.l5 === 'PROD_ASSY1_B' ? '组装线B班' : (dept.l5 === 'PROD_ASSY2_PACK' ? '包装线' : null)),
      organizationCode: dept.orgCode,
      organizationNameCn: dept.orgName,
      lasttimeEsd: Math.random() > 0.3 ? new Date().toISOString() : null,
      shiftDate: dateRange.value,
      shiftId: Math.random() > 0.5 ? 1 : 2,
      shiftNo: Math.random() > 0.5 ? 'DAYA' : 'NIGHT'
    })
  }
  return employees
}

// 生成早会点名数据（带点名时间）
const generateManpowerDetailList = (employees) => {
  return employees.filter(() => Math.random() > 0.5).map(emp => ({
    ...emp,
    createBy: ['李主管', '王工', '赵经理', '孙工'][Math.floor(Math.random()*4)],
    createTime: new Date().toISOString(),
    status: '已点名'
  }))
}

// 生成刷卡签到数据（带签到时间）
const generateSwipeCardSignList = (employees) => {
  return employees.filter(() => Math.random() > 0.6).map(emp => ({
    ...emp,
    signTime: new Date().toISOString(),
    checkTime: new Date().toISOString(),
    status: '已签到'
  }))
}

// 生成岗位排班数据（技能刷卡）
const generateStaffScheduleInfoList = (employees) => {
  const list = []
  employees.forEach(emp => {
    const stationCount = Math.floor(Math.random() * 3) + 1
    for (let i = 0; i < stationCount; i++) {
      list.push({
        ...emp,
        stationCode: `STATION_${i}`,
        stationName: `岗位${i+1}`,
        lineCode: `LINE_${i+1}`
      })
    }
  })
  return list
}

// 生成岗位刷卡数据（匹配部分排班）
const generateSwipeCardOnboardList = (staffScheduleList) => {
  return staffScheduleList.filter(() => Math.random() > 0.3).map(record => ({
    ...record,
    loginTime: new Date().toISOString(),
    status: '已刷卡'
  }))
}

// 根据queryType动态加载数据
const fetchData = async (params) => {
  const allEmployees = generateEmployeeList()
  // 根据部门代码过滤员工
  let filteredEmps = allEmployees
  if (params.l3OrgCode) filteredEmps = allEmployees.filter(emp => emp.l3OrgCode === params.l3OrgCode)
  else if (params.l4OrgCode) filteredEmps = allEmployees.filter(emp => emp.l4OrgCode === params.l4OrgCode)
  else if (params.l5OrgCode) filteredEmps = allEmployees.filter(emp => emp.l5OrgCode === params.l5OrgCode)
  else if (params.organizationCode) filteredEmps = allEmployees.filter(emp => emp.organizationCode === params.organizationCode)
  
  const empList = filteredEmps
  const result = { empPerPersonMehrList: empList }
  
  // 根据queryType决定加载哪些附加数据源
  if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
    result.manpowerDetailList = generateManpowerDetailList(empList)
  } else {
    result.manpowerDetailList = []
  }
  
  if (queryType.value === 'all' || queryType.value === 'swipeSign') {
    result.swipeCardSignList = generateSwipeCardSignList(empList)
  } else {
    result.swipeCardSignList = []
  }
  
  if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
    const staffSchedule = generateStaffScheduleInfoList(empList)
    result.staffScheduleInfoList = staffSchedule
    result.swipeCardOnboardList = generateSwipeCardOnboardList(staffSchedule)
    // 执行技能刷卡匹配
    enhanceStaffScheduleWithCardStatus(result.staffScheduleInfoList, result.swipeCardOnboardList)
  } else {
    result.staffScheduleInfoList = []
    result.swipeCardOnboardList = []
  }
  
  return result
}

// 技能刷卡匹配辅助函数
const enhanceStaffScheduleWithCardStatus = (scheduleList, onboardList) => {
  const cardMap = new Map()
  onboardList.forEach(card => {
    const key = `${card.shiftId}|${card.organizationCode}|${card.stationCode}`
    if (!cardMap.has(key) || new Date(card.loginTime) > new Date(cardMap.get(key).loginTime)) {
      cardMap.set(key, card)
    }
  })
  scheduleList.forEach(schedule => {
    const key = `${schedule.shiftId}|${schedule.organizationCode}|${schedule.stationCode}`
    const matchedCard = cardMap.get(key)
    if (matchedCard) {
      schedule.status = '已刷卡'
      schedule.loginTime = matchedCard.loginTime
    } else {
      schedule.status = '未刷卡'
      schedule.loginTime = null
    }
  })
}

// 加载数据入口
const loadData = async () => {
  if (!selectedDept.value) return
  const dept = selectedDept.value
  const date = dateRange.value
  let params = { shiftDate: date }
  if (dept.level === 3) params.l3OrgCode = dept.value
  else if (dept.level === 4) params.l4OrgCode = dept.value
  else if (dept.level === 5) params.l5OrgCode = dept.value
  else params.organizationCode = dept.value
  
  const result = await fetchData(params)
  rawData.empPerPersonMehrList = result.empPerPersonMehrList
  rawData.manpowerDetailList = result.manpowerDetailList || []
  rawData.swipeCardSignList = result.swipeCardSignList || []
  rawData.staffScheduleInfoList = result.staffScheduleInfoList || []
  rawData.swipeCardOnboardList = result.swipeCardOnboardList || []
}

// ---------- 3. 部门工具函数 ----------
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

// 计算部门汇总指标（系统人力、实出勤人力）
const computeDeptMetrics = (deptNode, shiftId) => {
  const cacheKey = `${deptNode.value}_${shiftId}_${queryType.value}`
  if (metricsCache.has(cacheKey)) {
    return metricsCache.get(cacheKey)
  }
  
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  
  // 系统人力：始终根据 empPerPersonMehrList 统计
  const systemEmpList = rawData.empPerPersonMehrList.filter(emp => 
    isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId
  )
  const systemManpower = systemEmpList.length
  
  // 实出勤人力：系统人力中 lasttimeEsd 不为空 + 其他数据源（根据queryType动态）
  const actualSet = new Set()
  // 系统人力中有ESD时间的
  rawData.empPerPersonMehrList.forEach(emp => {
    if (emp.lasttimeEsd && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      actualSet.add(emp.employeeNo)
    }
  })
  
  // 根据查询类型添加对应数据源的员工（去重）
  if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
    rawData.manpowerDetailList.forEach(emp => {
      if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
        actualSet.add(emp.employeeNo)
      }
    })
  }
  if (queryType.value === 'all' || queryType.value === 'swipeSign') {
    rawData.swipeCardSignList.forEach(emp => {
      if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
        actualSet.add(emp.employeeNo)
      }
    })
  }
  if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => {
      if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
        actualSet.add(emp.employeeNo)
      }
    })
  }
  
  const actualAttendance = actualSet.size
  const result = { systemManpower, actualAttendance }
  metricsCache.set(cacheKey, result)
  return result
}

// 构建右侧树形表格
const buildTableTree = (deptNode) => {
  if (!deptNode) return null
  const dayMetrics = computeDeptMetrics(deptNode, 1)
  const nightMetrics = computeDeptMetrics(deptNode, 2)
  const row = {
    id: deptNode.value,
    deptName: deptNode.label,
    day_systemManpower: dayMetrics.systemManpower,
    day_actualAttendance: dayMetrics.actualAttendance,
    night_systemManpower: nightMetrics.systemManpower,
    night_actualAttendance: nightMetrics.actualAttendance,
    children: []
  }
  if (deptNode.children && deptNode.children.length) {
    row.children = deptNode.children.map(child => buildTableTree(child)).filter(c => c !== null)
  }
  return row
}

// 刷新右侧表格
const refreshTableData = async () => {
  if (!selectedDept.value) {
    tableData.value = []
    return
  }
  metricsCache.clear()
  getDescendantValuesCache.clear()
  await loadData()
  const treeRoot = buildTableTree(selectedDept.value)
  if (treeRoot) {
    tableData.value = [treeRoot]
  } else {
    tableData.value = []
  }
}

// 左侧部门点击
const handleDeptChange = (data) => {
  selectedDept.value = data
  refreshTableData()
  closeDetail()
}

// 查询类型改变
const handleQueryTypeChange = () => {
  refreshTableData()
  closeDetail()
}

// 查询按钮
const handleQuery = async () => {
  await refreshTableData()
  ElMessage.success('查询完成')
}

// ---------- 导出功能 ----------
const flattenTree = (rows, result = []) => {
  rows.forEach(row => {
    result.push({
      '部门名称': row.deptName,
      '白班-系统人力': row.day_systemManpower ?? '',
      '白班-实出勤人力': row.day_actualAttendance ?? '',
      '夜班-系统人力': row.night_systemManpower ?? '',
      '夜班-实出勤人力': row.night_actualAttendance ?? ''
    })
    if (row.children && row.children.length) {
      flattenTree(row.children, result)
    }
  })
  return result
}

const handleExport = () => {
  if (!tableData.value.length) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const flatData = flattenTree(tableData.value)
  const ws = XLSX.utils.json_to_sheet(flatData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '出勤汇总')
  XLSX.writeFile(wb, `出勤汇总_${dateRange.value}.xlsx`)
  ElMessage.success('导出成功')
}

const handleExportDetail = () => {
  if (!detailData.value.length) {
    ElMessage.warning('暂无明细数据可导出')
    return
  }
  const exportData = detailData.value.map(row => {
    const obj = {}
    detailColumns.value.forEach(col => {
      obj[col.label] = row[col.prop] ?? ''
    })
    return obj
  })
  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '明细数据')
  XLSX.writeFile(wb, `${detailTitle}.xlsx`)
  ElMessage.success('明细导出成功')
}

// ---------- 明细相关（支持分页和动态列）----------
const getAllEmployeesInDept = (deptNode, shiftId, type) => {
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  let employeeMap = new Map()
  
  const addEmp = (emp) => {
    if (!employeeMap.has(emp.employeeNo) && emp.shiftId === shiftId && isEmployeeBelongToDeptSet(emp, deptValueSet)) {
      employeeMap.set(emp.employeeNo, { ...emp })
    }
  }
  
  if (type === 'systemManpower') {
    rawData.empPerPersonMehrList.forEach(emp => addEmp(emp))
  } 
  else if (type === 'actualAttendance') {
    // 系统人力ESD
    rawData.empPerPersonMehrList.filter(emp => emp.lasttimeEsd).forEach(emp => addEmp(emp))
    // 根据查询类型添加其他数据源
    if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
      rawData.manpowerDetailList.forEach(emp => addEmp(emp))
    }
    if (queryType.value === 'all' || queryType.value === 'swipeSign') {
      rawData.swipeCardSignList.forEach(emp => addEmp(emp))
    }
    if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
      rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
    }
  }
  else if (type === 'skillSwipe') {
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
  }
  else if (type === 'morningMeeting') {
    rawData.manpowerDetailList.forEach(emp => addEmp(emp))
  }
  else if (type === 'swipeSign') {
    rawData.swipeCardSignList.forEach(emp => addEmp(emp))
  }
  
  return Array.from(employeeMap.values())
}

// 生成明细数据（根据查询类型动态显示字段）
const generateDetailData = (deptNode, shift, metricType) => {
  const shiftId = shift === 'day' ? 1 : 2
  const employees = getAllEmployeesInDept(deptNode, shiftId, metricType)
  
  let columns = []
  let dataRows = []
  
  if (metricType === 'systemManpower') {
    columns = [
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 }
    ]
    dataRows = employees.map(emp => ({
      l3OrganizationCn: emp.l3OrganizationCn,
      l4OrganizationCn: emp.l4OrganizationCn,
      l5OrganizationCn: emp.l5OrganizationCn,
      organizationNameCn: emp.organizationNameCn,
      employeeName: emp.employeeName,
      employeeNo: emp.employeeNo
    }))
  } 
  else if (metricType === 'actualAttendance') {
    // 动态列：根据查询类型决定显示哪些字段
    columns = [
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'esdStatus', label: 'ESD状态', width: 100 }
    ]
    // 根据查询类型增加额外列
    if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
      columns.push({ prop: 'meetingStatus', label: '班会点名', width: 120 })
      columns.push({ prop: 'meetingTime', label: '点名时间', width: 160 })
    }
    if (queryType.value === 'all' || queryType.value === 'swipeSign') {
      columns.push({ prop: 'signStatus', label: '刷卡签到', width: 120 })
      columns.push({ prop: 'signTime', label: '签到时间', width: 160 })
    }
    if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
      columns.push({ prop: 'skillStatus', label: '技能刷卡', width: 120 })
      columns.push({ prop: 'skillTime', label: '刷卡时间', width: 160 })
    }
    if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
      columns.push({ prop: 'submitter', label: '提交人', width: 100 })
    }
    
    dataRows = employees.map(emp => {
      const meetingRec = rawData.manpowerDetailList.find(m => m.employeeNo === emp.employeeNo)
      const signRec = rawData.swipeCardSignList.find(s => s.employeeNo === emp.employeeNo)
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === emp.employeeNo && s.status === '已刷卡')
      const esdStatus = emp.lasttimeEsd ? '已测' : '未测'
      
      const row = {
        l3OrganizationCn: emp.l3OrganizationCn,
        l4OrganizationCn: emp.l4OrganizationCn,
        l5OrganizationCn: emp.l5OrganizationCn,
        organizationNameCn: emp.organizationNameCn,
        employeeName: emp.employeeName,
        employeeNo: emp.employeeNo,
        esdStatus
      }
      if (queryType.value === 'all' || queryType.value === 'morningMeeting') {
        row.meetingStatus = meetingRec ? '已点名' : '未点名'
        row.meetingTime = meetingRec ? meetingRec.createTime : ''
        row.submitter = meetingRec ? meetingRec.createBy : ''
      }
      if (queryType.value === 'all' || queryType.value === 'swipeSign') {
        row.signStatus = signRec ? '已签到' : '未签到'
        row.signTime = signRec ? signRec.checkTime : ''
      }
      if (queryType.value === 'all' || queryType.value === 'skillSwipe') {
        row.skillStatus = skillRec ? '已刷卡' : '未刷卡'
        row.skillTime = skillRec ? skillRec.loginTime : ''
      }
      return row
    })
  }
  
  return { columns, dataRows }
}

// 点击数字显示明细（带分页重置）
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
  
  const shiftText = shift === 'day' ? '白班' : '夜班'
  const metricMap = {
    systemManpower: '系统人力',
    actualAttendance: '实出勤人力'
  }
  const title = `${row.deptName} (${shiftText}) · ${metricMap[metricType]}: ${value}`
  
  const { columns, dataRows } = generateDetailData(deptNode, shift, metricType)
  detailColumns.value = columns
  detailData.value = dataRows
  detailTitle.value = title
  showDetail.value = true
  // 重置分页
  currentPage.value = 1
  pageSize.value = 10
  
  detailLoading.value = false
}

const closeDetail = () => {
  showDetail.value = false
  detailData.value = []
  detailColumns.value = []
  detailTitle.value = ''
  detailLoading.value = false
  currentPage.value = 1
}

// 分页回调
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}
const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 左侧面板折叠/展开
const toggleLeftPanel = () => {
  isLeftCollapsed.value = !isLeftCollapsed.value
}

// 初始化
onMounted(async () => {
  if (deptTree.value.length > 0) {
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
}

/* 左侧面板样式 */
.left-panel {
  width: 20%;
  background-color: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  position: relative;
}
.left-panel.collapsed {
  width: 0;
  border-right: none;
}
.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  padding: 16px 12px;
  color: #303133;
  border-left: 3px solid #409eff;
  background-color: #f5f7fa;
  white-space: nowrap;
}
.collapse-btn {
  margin-right: 4px;
}

/* 悬浮展开按钮 */
.expand-btn-wrapper {
  position: fixed;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 100;
  cursor: pointer;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  border-radius: 50%;
  transition: all 0.3s;
}
.expand-btn-wrapper:hover {
  transform: translateY(-50%) scale(1.05);
}

.right-panel {
  width: 80%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
  transition: width 0.3s ease;
}
.right-panel.expanded {
  width: 100%;
}
.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  padding: 16px 12px;
  color: #303133;
  border-left: 3px solid #409eff;
  background-color: #f5f7fa;
}
.collapse-btn {
  margin-right: 8px;
}
.right-panel {
  width: 80%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #fff;
  transition: width 0.3s ease;
}
.right-panel.expanded {
  width: 100%;
}
.top-bar {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
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
}
.clickable-number {
  cursor: pointer;
  color: #409eff;
  font-weight: 500;
}
.clickable-number:hover {
  color: #66b1ff;
  text-decoration: underline;
}
.bottom-detail {
  border-top: 2px solid #e4e7ed;
  background-color: #f9fafc;
  padding: 12px 20px 20px;
  max-height: 45%;
  overflow: auto;
  display: flex;
  flex-direction: column;
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
.pagination-container {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
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
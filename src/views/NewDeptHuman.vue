<template>
  <div class="dashboard-container">
    <!-- 左侧部门树 -->
    <div class="left-panel">
      <div class="dept-header">部门架构</div>
      <el-tree
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

    <!-- 右侧内容区 -->
    <div class="right-panel">
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
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleExport">导出汇总表</el-button>
          <el-button v-if="showDetail" type="success" @click="handleExportDetail">导出明细</el-button>
        </div>
      </div>

      <!-- 中间：出勤数据表格 -->
      <div class="middle-table">
        <el-table
          :data="tableData"
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          border
          stripe
        >
          <!-- 部门名称列 - 增加总出勤显示 -->
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

          <!-- 白班列组 -->
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
            <el-table-column prop="day_skillSwipe" label="技能刷卡人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'skillSwipe', row.day_skillSwipe)">
                  {{ row.day_skillSwipe ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="day_morningMeeting" label="早会点名人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'morningMeeting', row.day_morningMeeting)">
                  {{ row.day_morningMeeting ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="day_swipeSign" label="刷卡签到人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'swipeSign', row.day_swipeSign)">
                  {{ row.day_swipeSign ?? '-' }}
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
            <el-table-column prop="night_skillSwipe" label="技能刷卡人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'skillSwipe', row.night_skillSwipe)">
                  {{ row.night_skillSwipe ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="night_morningMeeting" label="早会点名人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'morningMeeting', row.night_morningMeeting)">
                  {{ row.night_morningMeeting ?? '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="night_swipeSign" label="刷卡签到人力" min-width="110">
              <template #default="{ row }">
                <span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'swipeSign', row.night_swipeSign)">
                  {{ row.night_swipeSign ?? '-' }}
                </span>
              </template>
            </el-table-column>
          </el-table-column>
        </el-table>
      </div>

      <!-- 明细面板（带加载效果） -->
      <div class="bottom-detail" v-if="showDetail">
        <div class="detail-header">
          <el-icon><InfoFilled /></el-icon>
          <span class="title-info">{{ detailTitle }}</span>
          <el-button type="text" @click="closeDetail" style="margin-left: auto;">关闭</el-button>
        </div>
        <el-table :data="detailData" border stripe size="small" v-loading="detailLoading" element-loading-text="加载明细中...">
          <template v-for="col in detailColumns" :key="col.prop">
            <el-table-column :prop="col.prop" :label="col.label" :width="col.width" />
          </template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, InfoFilled } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'

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

// 当前选中部门
const selectedDept = ref(null)
// 查询日期
const dateRange = ref(new Date().toISOString().slice(0,10))

// 原始数据存储
const rawData = reactive({
  empPerPersonMehrList: [],      // 系统人力原始数据
  manpowerDetailList: [],        // 早会点名原始数据
  swipeCardSignList: [],         // 刷卡签到原始数据
  staffScheduleInfoList: [],     // 岗位排班数据（技能刷卡）
  swipeCardOnboardList: []       // 岗位刷卡数据（用于匹配技能刷卡）
})

// 右侧表格数据
const tableData = ref([])

// 明细相关
const showDetail = ref(false)
const detailData = ref([])
const detailTitle = ref('')
const detailColumns = ref([])
const detailLoading = ref(false)   // 明细表格加载状态

// 缓存部门节点汇总结果，避免重复计算
const metricsCache = new Map()  // key: `${deptValue}_${shiftId}`

// ---------- 2. 模拟生成原始数据（含岗位刷卡数据）----------
// 生成员工基础信息（含岗位信息）
const generateEmployeeList = () => {
  const employees = []
  const namePool = ['张明', '李芳', '王磊', '陈丽', '赵岩', '周强', '吴迪', '郑爽', '孙阳', '林欣', '郭峰', '唐雅']
  // 部门映射
  const deptMap = {
    'PROD_ASSY1_A': { l3: 'PROD', l4: 'PROD_ASSY1', l5: 'PROD_ASSY1_A', orgCode: 'PROD_ASSY1_A', orgName: '组装线A班', stationList: ['焊接', '组装', '测试'] },
    'PROD_ASSY1_B': { l3: 'PROD', l4: 'PROD_ASSY1', l5: 'PROD_ASSY1_B', orgCode: 'PROD_ASSY1_B', orgName: '组装线B班', stationList: ['包装', '质检'] },
    'PROD_ASSY2_PACK': { l3: 'PROD', l4: 'PROD_ASSY2', l5: 'PROD_ASSY2_PACK', orgCode: 'PROD_ASSY2_PACK', orgName: '包装线', stationList: ['封箱', '贴标'] },
    'PROD_TEST': { l3: 'PROD', l4: 'PROD_TEST', l5: null, orgCode: 'PROD_TEST', orgName: '测试科', stationList: ['功能测试', '老化测试'] },
    'QA_IQC': { l3: 'QA', l4: 'QA_IQC', l5: null, orgCode: 'QA_IQC', orgName: 'IQC科', stationList: ['来料检验'] },
    'QA_OQC': { l3: 'QA', l4: 'QA_OQC', l5: null, orgCode: 'QA_OQC', orgName: 'OQC科', stationList: ['出货检验'] },
    'SCM': { l3: 'SCM', l4: null, l5: null, orgCode: 'SCM', orgName: '供应链部', stationList: ['计划'] }
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
      shiftNo: Math.random() > 0.5 ? 'DAYA' : 'NIGHT',
      stationCode: dept.stationList ? dept.stationList[Math.floor(Math.random() * dept.stationList.length)] : 'default',
      stationName: dept.stationList ? dept.stationList[Math.floor(Math.random() * dept.stationList.length)] : '默认岗位'
    })
  }
  return employees
}

// 生成早会点名数据
const generateManpowerDetailList = (employees) => {
  return employees.filter(() => Math.random() > 0.5).map(emp => ({
    ...emp,
    createBy: ['李主管', '王工', '赵经理', '孙工'][Math.floor(Math.random()*4)]
  }))
}

// 生成刷卡签到数据
const generateSwipeCardSignList = (employees) => {
  return employees.filter(() => Math.random() > 0.6).map(emp => ({
    ...emp,
    signTime: new Date().toISOString()
  }))
}

// 生成岗位排班数据（每个员工可能有多条，不同岗位）
const generateStaffScheduleInfoList = (employees) => {
  const list = []
  employees.forEach(emp => {
    // 每人1-3个岗位
    const stationCount = Math.floor(Math.random() * 3) + 1
    for (let i = 0; i < stationCount; i++) {
      list.push({
        ...emp,
        stationCode: `${emp.stationCode}_${i}`,
        stationName: `${emp.stationName}_${i}`,
        lineCode: `LINE_${i+1}`
      })
    }
  })
  return list
}

// 生成岗位刷卡数据（部分匹配）
const generateSwipeCardOnboardList = (staffScheduleList) => {
  // 随机选取 70% 的排班记录作为已刷卡
  return staffScheduleList.filter(() => Math.random() > 0.3).map(record => ({
    employeeName: record.employeeName,
    employeeNo: record.employeeNo,
    l3OrgCode: record.l3OrgCode,
    l3OrganizationCn: record.l3OrganizationCn,
    l4OrgCode: record.l4OrgCode,
    l4OrganizationCn: record.l4OrganizationCn,
    l5OrgCode: record.l5OrgCode,
    l5OrganizationCn: record.l5OrganizationCn,
    organizationCode: record.organizationCode,
    organizationNameCn: record.organizationNameCn,
    shiftDate: record.shiftDate,
    shiftId: record.shiftId,
    shiftNo: record.shiftNo,
    stationName: record.stationName,
    stationCode: record.stationCode,
    lineCode: record.lineCode,
    loginTime: new Date().toISOString()
  }))
}

// 模拟调用后端接口（实际替换为axios）
const fetchData = async (params) => {
  // 根据部门代码过滤员工
  const allEmployees = generateEmployeeList()
  let filteredEmps = allEmployees
  if (params.l3OrgCode) filteredEmps = allEmployees.filter(emp => emp.l3OrgCode === params.l3OrgCode)
  else if (params.l4OrgCode) filteredEmps = allEmployees.filter(emp => emp.l4OrgCode === params.l4OrgCode)
  else if (params.l5OrgCode) filteredEmps = allEmployees.filter(emp => emp.l5OrgCode === params.l5OrgCode)
  else if (params.organizationCode) filteredEmps = allEmployees.filter(emp => emp.organizationCode === params.organizationCode)
  
  // 生成各列表
  const empList = filteredEmps
  const manpowerDetail = generateManpowerDetailList(empList)
  const swipeCardSign = generateSwipeCardSignList(empList)
  const staffSchedule = generateStaffScheduleInfoList(empList)
  const swipeCardOnboard = generateSwipeCardOnboardList(staffSchedule)
  
  return {
    empPerPersonMehrList: empList,
    manpowerDetailList: manpowerDetail,
    swipeCardSignList: swipeCardSign,
    staffScheduleInfoList: staffSchedule,
    swipeCardOnboardList: swipeCardOnboard
  }
}

// 根据当前选中部门构建请求参数并加载数据
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
  rawData.manpowerDetailList = result.manpowerDetailList
  rawData.swipeCardSignList = result.swipeCardSignList
  rawData.staffScheduleInfoList = result.staffScheduleInfoList
  rawData.swipeCardOnboardList = result.swipeCardOnboardList
  
  // 执行技能刷卡匹配，增强 staffScheduleInfoList（添加status和loginTime）
  enhanceStaffScheduleWithCardStatus()
}

/**
 * 技能刷卡匹配：以 shiftId + organizationCode + stationCode 为唯一条件，
 * 将 swipeCardOnboardList 中的刷卡记录匹配到 staffScheduleInfoList，
 * 添加 status 和 loginTime 字段。
 */
const enhanceStaffScheduleWithCardStatus = () => {
  // 构建刷卡记录的映射表，key: `${shiftId}|${organizationCode}|${stationCode}`
  const cardMap = new Map()
  rawData.swipeCardOnboardList.forEach(card => {
    const key = `${card.shiftId}|${card.organizationCode}|${card.stationCode}`
    // 如果同一条件有多条，保留最后一条或合并？简单起见保留一条，loginTime取最新
    if (!cardMap.has(key) || new Date(card.loginTime) > new Date(cardMap.get(key).loginTime)) {
      cardMap.set(key, card)
    }
  })
  
  // 遍历排班列表，匹配并添加字段
  rawData.staffScheduleInfoList.forEach(schedule => {
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

// ---------- 3. 部门工具函数 ----------
/**
 * 获取部门及其所有子孙部门的 value 列表
 * 使用递归，结果缓存避免重复计算
 */
const getDescendantValuesCache = new Map()
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

/**
 * 判断员工是否属于指定部门（根据部门层级匹配）
 * 使用部门值集合快速判断
 */
const isEmployeeBelongToDeptSet = (emp, deptValuesSet) => {
  return deptValuesSet.has(emp.l3OrgCode) || deptValuesSet.has(emp.l4OrgCode) || 
         deptValuesSet.has(emp.l5OrgCode) || deptValuesSet.has(emp.organizationCode)
}

/**
 * 计算指定部门（及其子孙）的汇总指标（白班/夜班）
 * 结果会缓存，避免重复计算
 */
const computeDeptMetrics = (deptNode, shiftId) => {
  const cacheKey = `${deptNode.value}_${shiftId}`
  if (metricsCache.has(cacheKey)) {
    return metricsCache.get(cacheKey)
  }
  
  // 获取当前部门及所有子孙部门的 value 集合（用于快速匹配）
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  
  // 1. 系统人力：empPerPersonMehrList 中符合部门和班次的员工数量
  const systemEmpList = rawData.empPerPersonMehrList.filter(emp => 
    isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId
  )
  const systemManpower = systemEmpList.length
  
  // 2. 技能刷卡人力：staffScheduleInfoList 中 status === '已刷卡' 且符合部门班次的员工（按工号去重）
  const skillSwipedSet = new Set()
  rawData.staffScheduleInfoList.forEach(schedule => {
    if (schedule.status === '已刷卡' && isEmployeeBelongToDeptSet(schedule, deptValueSet) && schedule.shiftId === shiftId) {
      skillSwipedSet.add(schedule.employeeNo)
    }
  })
  const skillSwipe = skillSwipedSet.size
  
  // 3. 早会点名人力：manpowerDetailList 去重
  const meetingSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      meetingSet.add(emp.employeeNo)
    }
  })
  const morningMeeting = meetingSet.size
  
  // 4. 刷卡签到人力：swipeCardSignList 去重
  const signSet = new Set()
  rawData.swipeCardSignList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      signSet.add(emp.employeeNo)
    }
  })
  const swipeSign = signSet.size
  
  // 5. 实出勤人力：系统人力中有 lasttimeEsd 的员工 ∪ 早会点名 ∪ 刷卡签到 ∪ 技能刷卡匹配成功（去重）
  const actualSet = new Set()
  // 系统人力中 lasttimeEsd 不为空
  rawData.empPerPersonMehrList.forEach(emp => {
    if (emp.lasttimeEsd && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      actualSet.add(emp.employeeNo)
    }
  })
  // 早会点名
  meetingSet.forEach(no => actualSet.add(no))
  // 刷卡签到
  signSet.forEach(no => actualSet.add(no))
  // 技能刷卡已刷卡
  skillSwipedSet.forEach(no => actualSet.add(no))
  const actualAttendance = actualSet.size
  
  const result = { systemManpower, actualAttendance, skillSwipe, morningMeeting, swipeSign }
  metricsCache.set(cacheKey, result)
  return result
}

// 递归构建右侧树形表格数据（从选中节点开始）
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
    night_systemManpower: nightMetrics.systemManpower,
    night_actualAttendance: nightMetrics.actualAttendance,
    night_skillSwipe: nightMetrics.skillSwipe,
    night_morningMeeting: nightMetrics.morningMeeting,
    night_swipeSign: nightMetrics.swipeSign,
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
  // 清空缓存（数据源变化时需要清空）
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

// 查询按钮
const handleQuery = async () => {
  await refreshTableData()
  ElMessage.success('查询完成')
}

// ---------- 导出功能 ----------
// 导出汇总表（扁平化树）
const flattenTree = (rows, result = []) => {
  rows.forEach(row => {
    result.push({
      '部门名称': row.deptName,
      '白班-系统人力': row.day_systemManpower ?? '',
      '白班-实出勤人力': row.day_actualAttendance ?? '',
      '白班-技能刷卡人力': row.day_skillSwipe ?? '',
      '白班-早会点名人力': row.day_morningMeeting ?? '',
      '白班-刷卡签到人力': row.day_swipeSign ?? '',
      '夜班-系统人力': row.night_systemManpower ?? '',
      '夜班-实出勤人力': row.night_actualAttendance ?? '',
      '夜班-技能刷卡人力': row.night_skillSwipe ?? '',
      '夜班-早会点名人力': row.night_morningMeeting ?? '',
      '夜班-刷卡签到人力': row.night_swipeSign ?? ''
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

// 导出明细表
const handleExportDetail = () => {
  if (!detailData.value.length) {
    ElMessage.warning('暂无明细数据可导出')
    return
  }
  // 构建导出数据：使用当前明细表格的列和行
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

// ---------- 明细相关 ----------
// 获取部门下所有员工（根据部门层级匹配），按指标类型过滤
const getAllEmployeesInDept = (deptNode, shiftId, type) => {
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  let employeeMap = new Map() // key: employeeNo, value: 完整员工对象（合并信息）
  
  const addEmp = (emp, source = '') => {
    if (!employeeMap.has(emp.employeeNo) && emp.shiftId === shiftId && isEmployeeBelongToDeptSet(emp, deptValueSet)) {
      // 深拷贝一份，便于后续添加额外字段
      employeeMap.set(emp.employeeNo, { ...emp })
    }
  }
  
  if (type === 'systemManpower') {
    rawData.empPerPersonMehrList.forEach(emp => addEmp(emp))
  } 
  else if (type === 'actualAttendance') {
    // 系统人力中 lasttimeEsd 不为空
    rawData.empPerPersonMehrList.filter(emp => emp.lasttimeEsd).forEach(emp => addEmp(emp))
    // 早会点名
    rawData.manpowerDetailList.forEach(emp => addEmp(emp))
    // 刷卡签到
    rawData.swipeCardSignList.forEach(emp => addEmp(emp))
    // 技能刷卡已刷卡（仅取 status==='已刷卡' 的员工）
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
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

// 生成明细数据（含列定义）
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
    columns = [
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'esdStatus', label: 'ESD状态', width: 100 },
      { prop: 'meetingRollCall', label: '班会点名', width: 100 },
      { prop: 'skillSwipe', label: '技能刷卡', width: 100 },
      { prop: 'swipeSign', label: '刷卡签到', width: 100 },
      { prop: 'submitter', label: '提交人', width: 100 }
    ]
    dataRows = employees.map(emp => {
      const meetingRec = rawData.manpowerDetailList.find(m => m.employeeNo === emp.employeeNo)
      const signRec = rawData.swipeCardSignList.find(s => s.employeeNo === emp.employeeNo)
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === emp.employeeNo && s.status === '已刷卡')
      const esdStatus = emp.lasttimeEsd ? '已测' : '未测'
      const meetingRollCall = meetingRec ? '已点名' : '未点名'
      const skillSwipe = skillRec ? '已刷卡' : '未刷卡'
      const swipeSign = signRec ? '已签到' : '未签到'
      const submitter = meetingRec ? meetingRec.createBy : ''
      return {
        l3OrganizationCn: emp.l3OrganizationCn,
        l4OrganizationCn: emp.l4OrganizationCn,
        l5OrganizationCn: emp.l5OrganizationCn,
        organizationNameCn: emp.organizationNameCn,
        employeeName: emp.employeeName,
        employeeNo: emp.employeeNo,
        esdStatus,
        meetingRollCall,
        skillSwipe,
        swipeSign,
        submitter
      }
    })
  }
  else if (metricType === 'skillSwipe') {
    columns = [
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'loginTime', label: '刷卡时间', width: 160 }
    ]
    dataRows = employees.map(emp => {
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === emp.employeeNo && s.status === '已刷卡')
      return {
        l3OrganizationCn: emp.l3OrganizationCn,
        l4OrganizationCn: emp.l4OrganizationCn,
        l5OrganizationCn: emp.l5OrganizationCn,
        organizationNameCn: emp.organizationNameCn,
        employeeName: emp.employeeName,
        employeeNo: emp.employeeNo,
        loginTime: skillRec ? skillRec.loginTime : ''
      }
    })
  }
  else if (metricType === 'morningMeeting') {
    columns = [
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'submitter', label: '提交人', width: 100 }
    ]
    dataRows = employees.map(emp => {
      const meetingRec = rawData.manpowerDetailList.find(m => m.employeeNo === emp.employeeNo)
      return {
        l3OrganizationCn: emp.l3OrganizationCn,
        l4OrganizationCn: emp.l4OrganizationCn,
        l5OrganizationCn: emp.l5OrganizationCn,
        organizationNameCn: emp.organizationNameCn,
        employeeName: emp.employeeName,
        employeeNo: emp.employeeNo,
        submitter: meetingRec ? meetingRec.createBy : ''
      }
    })
  }
  else if (metricType === 'swipeSign') {
    columns = [
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'signTime', label: '签到时间', width: 160 }
    ]
    dataRows = employees.map(emp => {
      const signRec = rawData.swipeCardSignList.find(s => s.employeeNo === emp.employeeNo)
      return {
        l3OrganizationCn: emp.l3OrganizationCn,
        l4OrganizationCn: emp.l4OrganizationCn,
        l5OrganizationCn: emp.l5OrganizationCn,
        organizationNameCn: emp.organizationNameCn,
        employeeName: emp.employeeName,
        employeeNo: emp.employeeNo,
        signTime: signRec ? signRec.signTime : ''
      }
    })
  }
  
  return { columns, dataRows }
}

// 点击表格数字显示明细（带加载效果）
const handleNumberClick = async (row, shift, metricType, value) => {
  // 查找当前行对应的原始部门节点
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
  
  // 显示加载效果
  detailLoading.value = true
  // 使用 setTimeout 模拟异步，避免 UI 阻塞（实际生成数据是同步的，但加载状态至少显示一下）
  await new Promise(resolve => setTimeout(resolve, 50))
  
  const shiftText = shift === 'day' ? '白班' : '夜班'
  const metricMap = {
    systemManpower: '系统人力',
    actualAttendance: '实出勤人力',
    skillSwipe: '技能刷卡人力',
    morningMeeting: '早会点名人力',
    swipeSign: '刷卡签到人力'
  }
  const title = `${row.deptName} (${shiftText}) · ${metricMap[metricType]}: ${value}`
  
  const { columns, dataRows } = generateDetailData(deptNode, shift, metricType)
  detailColumns.value = columns
  detailData.value = dataRows
  detailTitle.value = title
  showDetail.value = true
  
  detailLoading.value = false
}

// 关闭明细
const closeDetail = () => {
  showDetail.value = false
  detailData.value = []
  detailColumns.value = []
  detailTitle.value = ''
  detailLoading.value = false
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
/* 样式保持不变，省略... 请参考之前的样式 */
.dashboard-container { display: flex; height: 100vh; width: 100%; overflow: hidden; }
.left-panel { width: 20%; background-color: #f5f7fa; border-right: 1px solid #e4e7ed; overflow: auto; padding: 16px 12px; }
.dept-header { font-size: 16px; font-weight: 600; margin-bottom: 16px; color: #303133; padding-left: 8px; border-left: 3px solid #409eff; }
.custom-tree-node { display: flex; align-items: center; gap: 6px; font-size: 14px; }
.right-panel { width: 80%; display: flex; flex-direction: column; overflow: hidden; background-color: #fff; }
.top-bar { padding: 16px 20px; border-bottom: 1px solid #ebeef5; }
.filter-group { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.middle-table { flex: 1; padding: 16px 20px; overflow: auto; }
.clickable-number { cursor: pointer; color: #409eff; font-weight: 500; }
.clickable-number:hover { color: #66b1ff; text-decoration: underline; }
.bottom-detail { border-top: 2px solid #e4e7ed; background-color: #f9fafc; padding: 12px 20px 20px; max-height: 45%; overflow: auto; }
.detail-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #dcdfe6; }
.detail-header .title-info { font-size: 15px; font-weight: 600; color: #303133; background: #ecf5ff; padding: 4px 12px; border-radius: 16px; }
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-thumb { background-color: #dcdfe6; border-radius: 3px; }
</style>
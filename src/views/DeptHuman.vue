<template>
  <div class="dashboard-container">
    <!-- 左侧部门树（新结构：label/value/level） -->
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
          <el-button @click="handleExport">导出</el-button>
        </div>
      </div>

      <!-- 中间：出勤数据表格（树形结构 + 白夜班横向分组） -->
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

      <!-- 明细面板 -->
      <div class="bottom-detail" v-if="showDetail">
        <div class="detail-header">
          <el-icon><InfoFilled /></el-icon>
          <span class="title-info">{{ detailTitle }}</span>
          <el-button type="text" @click="closeDetail" style="margin-left: auto;">关闭</el-button>
        </div>
        <el-table :data="detailData" border stripe size="small">
          <!-- 动态列根据明细类型变化 -->
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
// import * as XLSX from 'xlsx'

// ---------- 模拟部门树（label, value, level, children）----------
// 生产环境返回格式 [{label:'', value:'', level:3, children:[]}]
const deptTree = ref([
  {
    label: '生产部',
    value: 'PROD',
    level: 3,
    children: [
      { label: '组装一科', value: 'PROD_ASSY1', level: 4, children: [
        { label: '组装线A班', value: 'PROD_ASSY1_A', level: 5, children: [] },
        { label: '组装线B班', value: 'PROD_ASSY1_B', level: 5, children: [] },
        { label: '组装线C班', value: 'PROD_ASSY1_C', level: 5, children: [] }
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

// 当前选中的部门节点（包含label, value, level）
const selectedDept = ref(null)

// 筛选条件
const dateRange = ref(new Date().toISOString().slice(0,10))

// 存储从后端获取的原始数据
const rawData = reactive({
  empPerPersonMehrList: [],      // 系统人力原始数据
  manpowerDetailList: [],        // 早会点名原始数据
  swipeCardSignList: [],         // 刷卡签到原始数据
  staffScheduleInfoList: []      // 技能刷卡原始数据
})

// 右侧表格数据（树形结构，包含白夜班汇总）
const tableData = ref([])

// 明细相关
const showDetail = ref(false)
const detailData = ref([])
const detailTitle = ref('')
const detailColumns = ref([])

// ---------- 1. 模拟生成原始数据 ----------
// 生成员工基础信息（工号、姓名、部门代码层级）
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
  for (let i = 1; i <= 45; i++) {
    const deptKey = Object.keys(deptMap)[i % Object.keys(deptMap).length]
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
      lasttimeEsd: Math.random() > 0.3 ? new Date().toISOString() : null, // 随机ESD时间
      shiftDate: dateRange.value,
      shiftId: Math.random() > 0.5 ? 1 : 2,
      shiftNo: Math.random() > 0.5 ? 'DAYA' : 'NIGHT'
    })
  }
  return employees
}

// 生成早会点名数据（manpowerDetailList）
const generateManpowerDetailList = (employees) => {
  return employees.filter(() => Math.random() > 0.5).map(emp => ({
    ...emp,
    createBy: ['李主管', '王工', '赵经理', '孙工'][Math.floor(Math.random()*4)]
  }))
}

// 生成刷卡签到数据（swipeCardSignList）
const generateSwipeCardSignList = (employees) => {
  return employees.filter(() => Math.random() > 0.6).map(emp => ({
    ...emp,
    signTime: new Date().toISOString()
  }))
}

// 生成技能刷卡数据（staffScheduleInfoList）
const generateStaffScheduleInfoList = (employees) => {
  return employees.filter(() => Math.random() > 0.5).map(emp => ({
    ...emp,
    loginTime: new Date().toISOString()
  }))
}

// 模拟调用后端接口（实际替换为axios）
const fetchData = async (params) => {
  // 根据传入的部门层级参数（l3OrgCode/l4OrgCode等）模拟返回对应部门下的数据
  // 实际上应该由后端根据部门代码过滤，此处我们模拟全量数据再按部门过滤
  const allEmployees = generateEmployeeList()
  const filteredEmps = allEmployees.filter(emp => {
    if (params.l3OrgCode) return emp.l3OrgCode === params.l3OrgCode
    if (params.l4OrgCode) return emp.l4OrgCode === params.l4OrgCode
    if (params.l5OrgCode) return emp.l5OrgCode === params.l5OrgCode
    if (params.organizationCode) return emp.organizationCode === params.organizationCode
    return true
  })
  return {
    empPerPersonMehrList: filteredEmps,
    manpowerDetailList: generateManpowerDetailList(filteredEmps),
    swipeCardSignList: generateSwipeCardSignList(filteredEmps),
    staffScheduleInfoList: generateStaffScheduleInfoList(filteredEmps)
  }
}

// 根据当前选中的部门构建请求参数并发起4个请求
const loadData = async () => {
  if (!selectedDept.value) return
  const dept = selectedDept.value
  const date = dateRange.value
  let params = { shiftDate: date }
  // 根据部门层级构造对应字段
  if (dept.level === 3) params.l3OrgCode = dept.value
  else if (dept.level === 4) params.l4OrgCode = dept.value
  else if (dept.level === 5) params.l5OrgCode = dept.value
  else params.organizationCode = dept.value  // 最小部门

  // 模拟同时请求4个接口（实际使用Promise.all + axios）
  // 这里模拟后端返回的数据结构
  const mockResult = await fetchData(params)
  rawData.empPerPersonMehrList = mockResult.empPerPersonMehrList
  rawData.manpowerDetailList = mockResult.manpowerDetailList
  rawData.swipeCardSignList = mockResult.swipeCardSignList
  rawData.staffScheduleInfoList = mockResult.staffScheduleInfoList
}

// ---------- 2. 数据计算与树形汇总 ----------
// 获取某个部门下所有子孙部门节点的value列表（递归）
const getAllDescendantValues = (deptNode) => {
  let values = [deptNode.value]
  if (deptNode.children && deptNode.children.length) {
    deptNode.children.forEach(child => {
      values = values.concat(getAllDescendantValues(child))
    })
  }
  return values
}

// 根据部门代码匹配规则判断员工是否属于该部门（支持l3/l4/l5/orgCode）
const isEmployeeBelongToDept = (emp, deptValue, deptLevel) => {
  if (deptLevel === 3) return emp.l3OrgCode === deptValue
  if (deptLevel === 4) return emp.l4OrgCode === deptValue
  if (deptLevel === 5) return emp.l5OrgCode === deptValue
  return emp.organizationCode === deptValue
}

// 计算指定部门（及其子孙）的汇总指标（白班/夜班）
const computeDeptMetrics = (deptNode, shiftId) => {
  // 获取当前部门及其所有子孙部门的value列表
  const descendantValues = getAllDescendantValues(deptNode)
  // 根据shiftId过滤班次（1白班2夜班）
  const shift = shiftId === 1 ? 'day' : 'night'
  
  // 1. 系统人力：empPerPersonMehrList中符合部门和班次的员工数量
  const systemEmpList = rawData.empPerPersonMehrList.filter(emp => 
    descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
    descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)
  ).filter(emp => emp.shiftId === shiftId)
  const systemManpower = systemEmpList.length

  // 2. 实出勤人力：四个列表去重（工号）且符合部门和班次
  const actualSet = new Set()
  // empPerPersonMehrList中lasttimeEsd不为空
  rawData.empPerPersonMehrList.filter(emp => emp.lasttimeEsd).forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) actualSet.add(emp.employeeNo)
  })
  rawData.manpowerDetailList.forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) actualSet.add(emp.employeeNo)
  })
  rawData.swipeCardSignList.forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) actualSet.add(emp.employeeNo)
  })
  rawData.staffScheduleInfoList.forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) actualSet.add(emp.employeeNo)
  })
  const actualAttendance = actualSet.size

  // 3. 技能刷卡人力：staffScheduleInfoList去重工号
  const skillSet = new Set()
  rawData.staffScheduleInfoList.forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) skillSet.add(emp.employeeNo)
  })
  const skillSwipe = skillSet.size

  // 4. 早会点名人力：manpowerDetailList去重工号
  const meetingSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) meetingSet.add(emp.employeeNo)
  })
  const morningMeeting = meetingSet.size

  // 5. 刷卡签到人力：swipeCardSignList去重工号
  const signSet = new Set()
  rawData.swipeCardSignList.forEach(emp => {
    if ((descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) || 
         descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) &&
        emp.shiftId === shiftId) signSet.add(emp.employeeNo)
  })
  const swipeSign = signSet.size

  return { systemManpower, actualAttendance, skillSwipe, morningMeeting, swipeSign }
}

// 递归构建右侧树形表格数据（从选中节点开始，包含自身及所有子孙）
const buildTableTree = (deptNode) => {
  if (!deptNode) return null
  
  // 计算白班指标（shiftId=1）和夜班指标（shiftId=2）
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

// 刷新右侧表格（点击部门时调用）
const refreshTableData = async () => {
  if (!selectedDept.value) {
    tableData.value = []
    return
  }
  await loadData()  // 重新拉取原始数据
  const treeRoot = buildTableTree(selectedDept.value)
  if (treeRoot) {
    tableData.value = [treeRoot]
  } else {
    tableData.value = []
  }
}

// 左侧部门点击事件
const handleDeptChange = (data) => {
  selectedDept.value = data
  refreshTableData()
  closeDetail()
}

// 查询按钮（重新查询）
const handleQuery = async () => {
  await refreshTableData()
  ElMessage.success('查询完成')
}

// ---------- 明细相关：根据点击的类型和部门节点生成明细列表 ----------
// 获取部门下所有员工（根据部门层级匹配）
const getAllEmployeesInDept = (deptNode, shiftId, type) => {
  const descendantValues = getAllDescendantValues(deptNode)
  let employeeSet = new Map() // key: employeeNo, value: 完整员工对象

  const addEmp = (emp) => {
    if (!employeeSet.has(emp.employeeNo) && emp.shiftId === shiftId) {
      employeeSet.set(emp.employeeNo, emp)
    }
  }

  if (type === 'systemManpower') {
    rawData.empPerPersonMehrList.forEach(emp => {
      if (descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) ||
          descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)) {
        addEmp(emp)
      }
    })
  } else if (type === 'actualAttendance') {
    // 实出勤：所有四个列表中出现的员工
    rawData.empPerPersonMehrList.filter(emp => emp.lasttimeEsd).forEach(addEmp)
    rawData.manpowerDetailList.forEach(addEmp)
    rawData.swipeCardSignList.forEach(addEmp)
    rawData.staffScheduleInfoList.forEach(addEmp)
  } else if (type === 'skillSwipe') {
    rawData.staffScheduleInfoList.forEach(addEmp)
  } else if (type === 'morningMeeting') {
    rawData.manpowerDetailList.forEach(addEmp)
  } else if (type === 'swipeSign') {
    rawData.swipeCardSignList.forEach(addEmp)
  }
  
  // 再按部门过滤一次
  const filtered = Array.from(employeeSet.values()).filter(emp => 
    descendantValues.includes(emp.l3OrgCode) || descendantValues.includes(emp.l4OrgCode) ||
    descendantValues.includes(emp.l5OrgCode) || descendantValues.includes(emp.organizationCode)
  )
  return filtered
}

// 生成明细数据
const generateDetailData = (deptNode, shift, metricType) => {
  const shiftId = shift === 'day' ? 1 : 2
  const employees = getAllEmployeesInDept(deptNode, shiftId, metricType)
  
  // 根据指标类型定义不同的列
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
    // 构建每个员工的补充信息
    dataRows = employees.map(emp => {
      // 查找早会点名数据
      const meetingRec = rawData.manpowerDetailList.find(m => m.employeeNo === emp.employeeNo)
      // 查找刷卡签到
      const signRec = rawData.swipeCardSignList.find(s => s.employeeNo === emp.employeeNo)
      // 查找技能刷卡
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === emp.employeeNo)
      // ESD状态：lasttimeEsd不为空则为“已测”
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
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === emp.employeeNo)
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

// 点击表格数字显示明细
const handleNumberClick = (row, shift, metricType, value) => {
  // 查找当前行对应的原始部门节点（根据row.id在deptTree中查找）
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
}

// 关闭明细
const closeDetail = () => {
  showDetail.value = false
  detailData.value = []
  detailColumns.value = []
  detailTitle.value = ''
}

// 导出表格（扁平化树）
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
  XLSX.utils.book_append_sheet(wb, ws, '出勤数据')
  XLSX.writeFile(wb, `出勤数据_${dateRange.value}.xlsx`)
  ElMessage.success('导出成功')
}

// 初始化默认选中第一个部门
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
```
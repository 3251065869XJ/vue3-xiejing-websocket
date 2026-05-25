<template>
  <!-- 其余结构不变，在中间表格中增加汇总列 -->
  <el-table ...>
    <!-- 部门名称列 ... -->
    
    <!-- 白班列组（保持不变） -->
    
    <!-- 夜班列组（保持不变） -->
    
    <!-- 新增汇总列组 -->
    <el-table-column label="汇总" align="center">
      <el-table-column prop="total_systemManpower" label="系统人力" min-width="110">
        <template #default="{ row }">
          <span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'total_systemManpower', row.total_systemManpower)">
            {{ row.total_systemManpower ?? '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="total_actualAttendance" label="实出勤人力" min-width="110">
        <template #default="{ row }">
          <span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'total_actualAttendance', row.total_actualAttendance)">
            {{ row.total_actualAttendance ?? '-' }}
          </span>
        </template>
      </el-table-column>
    </el-table-column>
  </el-table>
</template>

<script setup>
// ... 原有导入和基础代码 ...

// 修改 buildTableTree，为每个 row 增加 total_* 字段
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
    // 新增汇总字段
    total_systemManpower: (dayMetrics.systemManpower || 0) + (nightMetrics.systemManpower || 0),
    total_actualAttendance: (dayMetrics.actualAttendance || 0) + (nightMetrics.actualAttendance || 0),
    children: []
  }
  if (deptNode.children && deptNode.children.length) {
    row.children = deptNode.children.map(child => buildTableTree(child)).filter(c => c !== null)
  }
  return row
}

// 修改 handleNumberClick，支持 'total' 班次类型
const handleNumberClick = async (row, shift, metricType, value) => {
  // 查找部门节点
  const findDeptNode = (nodes, id) => { /* 同前 */ }
  const deptNode = findDeptNode(deptTree.value, row.id)
  if (!deptNode) return
  
  detailLoading.value = true
  await new Promise(resolve => setTimeout(resolve, 50))
  
  // 构造标题
  const shiftText = shift === 'day' ? '白班' : (shift === 'night' ? '夜班' : '合计')
  const metricMap = {
    systemManpower: '系统人力',
    actualAttendance: '实出勤人力',
    total_systemManpower: '总系统人力',
    total_actualAttendance: '总实出勤人力'
  }
  const title = `${row.deptName} (${shiftText}) · ${metricMap[metricType]}: ${value}`
  
  // 生成明细数据（需要传递 metricType 和 shift）
  const { columns, dataRows } = generateDetailData(deptNode, shift, metricType)
  detailColumns.value = columns
  detailData.value = dataRows
  detailTitle.value = title
  showDetail.value = true
  currentPage.value = 1
  detailLoading.value = false
}

// 修改 generateDetailData，处理 'total_systemManpower' 和 'total_actualAttendance' 类型
const generateDetailData = (deptNode, shift, metricType) => {
  // 如果 shift 为 'total'，则需要合并白班和夜班的员工
  const isTotal = shift === 'total'
  const shiftsToProcess = isTotal ? ['day', 'night'] : [shift]
  
  let employees = [] // 存储 { shift, emp } 对象
  for (const s of shiftsToProcess) {
    const shiftId = s === 'day' ? 1 : 2
    let metricKey = metricType.includes('system') ? 'systemManpower' : 'actualAttendance'
    // 如果是总类型，实际获取员工时使用对应的基础指标类型
    if (metricType === 'total_systemManpower') metricKey = 'systemManpower'
    if (metricType === 'total_actualAttendance') metricKey = 'actualAttendance'
    
    const empList = getAllEmployeesInDept(deptNode, shiftId, metricKey)
    empList.forEach(emp => {
      employees.push({ shift: s, ...emp })
    })
  }
  
  // 构建列定义（需要增加班次列）
  let columns = []
  let dataRows = []
  
  // 判断基础指标类型
  const isSystem = metricType.includes('system')
  
  if (isSystem) {
    // 系统人力明细
    columns = [
      { prop: 'shift', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 }
    ]
    dataRows = employees.map(item => ({
      shift: item.shift === 'day' ? '白班' : '夜班',
      l3OrganizationCn: item.l3OrganizationCn,
      l4OrganizationCn: item.l4OrganizationCn,
      l5OrganizationCn: item.l5OrganizationCn,
      organizationNameCn: item.organizationNameCn,
      employeeName: item.employeeName,
      employeeNo: item.employeeNo
    }))
  } else {
    // 实出勤人力明细（带状态字段）
    columns = [
      { prop: 'shift', label: '班次', width: 80 },
      { prop: 'l3OrganizationCn', label: '三层部门名称', width: 120 },
      { prop: 'l4OrganizationCn', label: '四层部门名称', width: 120 },
      { prop: 'l5OrganizationCn', label: '五层部门名称', width: 120 },
      { prop: 'organizationNameCn', label: '最小部门名称', width: 130 },
      { prop: 'employeeName', label: '姓名', width: 80 },
      { prop: 'employeeNo', label: '工号', width: 100 },
      { prop: 'esdStatus', label: 'ESD状态', width: 100 }
    ]
    // 根据 queryType 动态添加额外列（与之前相同）
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
    
    dataRows = employees.map(item => {
      // 获取对应的原始数据（需要根据 item 的工号和班次查找）
      const meetingRec = rawData.manpowerDetailList.find(m => m.employeeNo === item.employeeNo && m.shiftId === (item.shift === 'day' ? 1 : 2))
      const signRec = rawData.swipeCardSignList.find(s => s.employeeNo === item.employeeNo && s.shiftId === (item.shift === 'day' ? 1 : 2))
      const skillRec = rawData.staffScheduleInfoList.find(s => s.employeeNo === item.employeeNo && s.shiftId === (item.shift === 'day' ? 1 : 2) && s.status === '已刷卡')
      const esdStatus = item.lasttimeEsd ? '已测' : '未测'
      
      const row = {
        shift: item.shift === 'day' ? '白班' : '夜班',
        l3OrganizationCn: item.l3OrganizationCn,
        l4OrganizationCn: item.l4OrganizationCn,
        l5OrganizationCn: item.l5OrganizationCn,
        organizationNameCn: item.organizationNameCn,
        employeeName: item.employeeName,
        employeeNo: item.employeeNo,
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

// 修改 getAllEmployeesInDept，增加 shiftId 参数，返回指定班次的员工列表
const getAllEmployeesInDept = (deptNode, shiftId, type) => {
  // 原有实现，只需要确保过滤 shiftId 即可
  // 已经存在 shiftId 过滤，无需修改
  // ...
}

// 其他代码保持不变...
</script>
<template>
  <div class="dashboard-container">
    <!-- 左侧部门树（折叠逻辑不变，省略） -->
    <div class="left-panel" :class="{ collapsed: isLeftCollapsed }">
      <!-- ... 省略 ... -->
    </div>
    <div class="expand-btn-wrapper" v-if="isLeftCollapsed" @click="toggleLeftPanel">
      <el-button type="primary" :icon="Expand" circle size="large" />
    </div>

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
          <el-button type="warning" @click="exportAllDetailsNative">导出全部分类明细（多文件）</el-button>
        </div>
      </div>

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
            <!-- 新增请假人力 -->
            <el-table-column prop="day_leaveManpower" label="请假人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'day', 'leaveManpower', row.day_leaveManpower)">{{ row.day_leaveManpower ?? '-' }}</span></template></el-table-column>
          </el-table-column>

          <!-- 夜班 -->
          <el-table-column label="夜班" align="center">
            <el-table-column prop="night_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'systemManpower', row.night_systemManpower)">{{ row.night_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'actualAttendance', row.night_actualAttendance)">{{ row.night_actualAttendance ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_skillSwipe" label="技能刷卡人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'skillSwipe', row.night_skillSwipe)">{{ row.night_skillSwipe ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_morningMeeting" label="早会点名人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'morningMeeting', row.night_morningMeeting)">{{ row.night_morningMeeting ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_swipeSign" label="刷卡签到人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'swipeSign', row.night_swipeSign)">{{ row.night_swipeSign ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_leaveManpower" label="请假人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'leaveManpower', row.night_leaveManpower)">{{ row.night_leaveManpower ?? '-' }}</span></template></el-table-column>
          </el-table-column>

          <!-- 汇总列 -->
          <el-table-column label="汇总" align="center">
            <el-table-column prop="total_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'systemManpower', row.total_systemManpower)">{{ row.total_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'actualAttendance', row.total_actualAttendance)">{{ row.total_actualAttendance ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_leaveManpower" label="请假人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'leaveManpower', row.total_leaveManpower)">{{ row.total_leaveManpower ?? '-' }}</span></template></el-table-column>
          </el-table-column>
        </el-table>
      </div>

      <!-- 明细面板（分页、加载等与原代码一致，内容不变） -->
      <div class="bottom-detail" v-if="showDetail">...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, InfoFilled, Expand, Fold } from '@element-plus/icons-vue'

// ---------- 数据生成（增加 WorkFlag）----------
const generateManpowerDetailList = (employees) => {
  return employees.filter(() => Math.random() > 0.5).map(emp => ({
    ...emp,
    createBy: ['李主管', '王工', '赵经理', '孙工'][Math.floor(Math.random()*4)],
    createTime: new Date().toISOString(),
    status: '已点名',
    WorkFlag: Math.random() > 0.8 ? 3 : (Math.random() > 0.5 ? 1 : 2) // 3为请假
  }))
}

// 其他数据生成函数保持不变（略）

// ---------- 核心计算函数（支持请假人力）----------
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
  
  // 早会点名（全部）
  const allMeetingSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      allMeetingSet.add(emp.employeeNo)
    }
  })
  
  // 请假人力：WorkFlag === 3
  const leaveSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if (emp.WorkFlag === 3 && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      leaveSet.add(emp.employeeNo)
    }
  })
  const leaveManpower = leaveSet.size
  
  // 非请假早会点名
  const meetingSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if (emp.WorkFlag !== 3 && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
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
  
  // 实出勤人力（排除请假）
  const actualSet = new Set()
  rawData.empPerPersonMehrList.forEach(emp => {
    if (emp.lasttimeEsd && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) {
      actualSet.add(emp.employeeNo)
    }
  })
  meetingSet.forEach(no => actualSet.add(no))
  signSet.forEach(no => actualSet.add(no))
  skillSwipedSet.forEach(no => actualSet.add(no))
  const actualAttendance = actualSet.size
  
  const result = { systemManpower, actualAttendance, skillSwipe, morningMeeting, swipeSign, leaveManpower }
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
    night_systemManpower: nightMetrics.systemManpower,
    night_actualAttendance: nightMetrics.actualAttendance,
    night_skillSwipe: nightMetrics.skillSwipe,
    night_morningMeeting: nightMetrics.morningMeeting,
    night_swipeSign: nightMetrics.swipeSign,
    night_leaveManpower: nightMetrics.leaveManpower,
    total_systemManpower: dayMetrics.systemManpower + nightMetrics.systemManpower,
    total_actualAttendance: dayMetrics.actualAttendance + nightMetrics.actualAttendance,
    total_leaveManpower: dayMetrics.leaveManpower + nightMetrics.leaveManpower,
    children: []
  }
  if (deptNode.children?.length) {
    row.children = deptNode.children.map(child => buildTableTree(child)).filter(c => c)
  }
  return row
}

// ---------- 明细获取（支持请假人力）----------
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
  } else if (type === 'actualAttendance') {
    rawData.empPerPersonMehrList.filter(emp => emp.lasttimeEsd).forEach(emp => addEmp(emp))
    rawData.manpowerDetailList.filter(emp => emp.WorkFlag !== 3).forEach(emp => addEmp(emp))
    rawData.swipeCardSignList.forEach(emp => addEmp(emp))
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
  } else if (type === 'skillSwipe') {
    rawData.staffScheduleInfoList.filter(s => s.status === '已刷卡').forEach(emp => addEmp(emp))
  } else if (type === 'morningMeeting') {
    rawData.manpowerDetailList.filter(emp => emp.WorkFlag !== 3).forEach(emp => addEmp(emp))
  } else if (type === 'swipeSign') {
    rawData.swipeCardSignList.forEach(emp => addEmp(emp))
  } else if (type === 'leaveManpower') {
    rawData.manpowerDetailList.filter(emp => emp.WorkFlag === 3).forEach(emp => addEmp(emp))
  }
  return Array.from(employeeMap.values())
}

// 生成明细数据（增加请假分支）
const generateDetailData = (deptNode, shift, metricType) => {
  // ... 原有代码，增加 leaveManpower 分支，已在前面示例中给出，此处省略重复，请参考上文补全 ...
  // 注意：在 handleNumberClick 的 metricMap 中增加 leaveManpower 映射
}

// 导出全部分类明细（包含请假人力）
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
    { name: '请假人力', metricKey: 'leaveManpower', sheetName: '请假人力' }  // 新增
  ]
  // ... 遍历导出，逻辑同前，注意 leaveManpower 的列定义
}

// 其余代码（分页、折叠、导出等）保持不变，只需将 metricMap 补充 leaveManpower 即可
</script>
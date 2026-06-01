<template>
  <div class="dashboard-container">
    <!-- 左侧部门树（折叠逻辑同上，略） -->
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

    <!-- 悬浮展开按钮 -->
    <div class="expand-btn-wrapper" v-if="isLeftCollapsed" @click="toggleLeftPanel">
      <el-button type="primary" :icon="Expand" circle size="large" />
    </div>

    <!-- 右侧内容区 -->
    <div class="right-panel" :class="{ expanded: isLeftCollapsed }">
      <!-- 筛选栏 -->
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
          <el-button type="warning" @click="exportAllDetails">一键导出所有明细</el-button>
        </div>
      </div>

      <!-- 中间表格：完整列 -->
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
          </el-table-column>

          <!-- 夜班 -->
          <el-table-column label="夜班" align="center">
            <el-table-column prop="night_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'systemManpower', row.night_systemManpower)">{{ row.night_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'actualAttendance', row.night_actualAttendance)">{{ row.night_actualAttendance ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_skillSwipe" label="技能刷卡人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'skillSwipe', row.night_skillSwipe)">{{ row.night_skillSwipe ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_morningMeeting" label="早会点名人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'morningMeeting', row.night_morningMeeting)">{{ row.night_morningMeeting ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="night_swipeSign" label="刷卡签到人力" min-width="110"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'night', 'swipeSign', row.night_swipeSign)">{{ row.night_swipeSign ?? '-' }}</span></template></el-table-column>
          </el-table-column>

          <!-- 汇总列 -->
          <el-table-column label="汇总" align="center">
            <el-table-column prop="total_systemManpower" label="系统人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'systemManpower', row.total_systemManpower)">{{ row.total_systemManpower ?? '-' }}</span></template></el-table-column>
            <el-table-column prop="total_actualAttendance" label="实出勤人力" min-width="100"><template #default="{ row }"><span class="clickable-number" @click.stop="handleNumberClick(row, 'total', 'actualAttendance', row.total_actualAttendance)">{{ row.total_actualAttendance ?? '-' }}</span></template></el-table-column>
          </el-table-column>
        </el-table>
      </div>

      <!-- 明细面板（分页、加载等同前，略） -->
      <div class="bottom-detail" v-if="showDetail">...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, InfoFilled, Expand, Fold } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx' // 仍使用 xlsx 库，如需纯原生可自行替换，但多 sheet 导出必须

// ... 原有数据定义和工具函数（部门树、数据生成等）保持不变 ...

// 新增：计算每个部门节点的各项指标（包含技能刷卡、早会点名、刷卡签到）
const computeDeptMetrics = (deptNode, shiftId) => {
  const cacheKey = `${deptNode.value}_${shiftId}_${queryType.value}`
  if (metricsCache.has(cacheKey)) return metricsCache.get(cacheKey)
  
  const descendantValues = getAllDescendantValues(deptNode)
  const deptValueSet = new Set(descendantValues)
  
  // 系统人力
  const systemEmpList = rawData.empPerPersonMehrList.filter(emp => isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId)
  const systemManpower = systemEmpList.length
  
  // 技能刷卡（已匹配刷卡状态）
  const skillSwipedSet = new Set()
  rawData.staffScheduleInfoList.forEach(s => {
    if (s.status === '已刷卡' && isEmployeeBelongToDeptSet(s, deptValueSet) && s.shiftId === shiftId) {
      skillSwipedSet.add(s.employeeNo)
    }
  })
  const skillSwipe = skillSwipedSet.size
  
  // 早会点名
  const meetingSet = new Set()
  rawData.manpowerDetailList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) meetingSet.add(emp.employeeNo)
  })
  const morningMeeting = meetingSet.size
  
  // 刷卡签到
  const signSet = new Set()
  rawData.swipeCardSignList.forEach(emp => {
    if (isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) signSet.add(emp.employeeNo)
  })
  const swipeSign = signSet.size
  
  // 实出勤人力（ESD + 以上三种来源去重）
  const actualSet = new Set()
  rawData.empPerPersonMehrList.forEach(emp => {
    if (emp.lasttimeEsd && isEmployeeBelongToDeptSet(emp, deptValueSet) && emp.shiftId === shiftId) actualSet.add(emp.employeeNo)
  })
  meetingSet.forEach(no => actualSet.add(no))
  signSet.forEach(no => actualSet.add(no))
  skillSwipedSet.forEach(no => actualSet.add(no))
  const actualAttendance = actualSet.size
  
  const result = { systemManpower, actualAttendance, skillSwipe, morningMeeting, swipeSign }
  metricsCache.set(cacheKey, result)
  return result
}

// 构建表格行（包含所有字段）
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
    total_systemManpower: dayMetrics.systemManpower + nightMetrics.systemManpower,
    total_actualAttendance: dayMetrics.actualAttendance + nightMetrics.actualAttendance,
    children: []
  }
  if (deptNode.children?.length) {
    row.children = deptNode.children.map(child => buildTableTree(child)).filter(c => c)
  }
  return row
}

// 导出所有明细（五个sheet）
const exportAllDetails = async () => {
  if (!selectedDept.value) {
    ElMessage.warning('请先选择一个部门')
    return
  }
  // 获取当前选中部门节点（用于明细）
  const deptNode = selectedDept.value
  const workbook = XLSX.utils.book_new()
  
  // 定义需要导出的分类及其对应 sheet 名称和指标类型
  const categories = [
    { name: '系统人力', metricKey: 'systemManpower', sheetName: '系统人力' },
    { name: '实出勤人力', metricKey: 'actualAttendance', sheetName: '实出勤人力' },
    { name: '技能刷卡人力', metricKey: 'skillSwipe', sheetName: '技能刷卡人力' },
    { name: '刷卡签到人力', metricKey: 'swipeSign', sheetName: '刷卡签到人力' },
    { name: '早会点名人力', metricKey: 'morningMeeting', sheetName: '早会点名人力' }
  ]
  
  for (const cat of categories) {
    // 获取所有部门节点的分类明细（递归收集）
    const allDetails = []
    const collectDetails = (node, level = 0) => {
      // 获取该节点的白班和夜班明细
      const shifts = ['day', 'night']
      shifts.forEach(shift => {
        const shiftId = shift === 'day' ? 1 : 2
        const employees = getAllEmployeesInDept(node, shiftId, cat.metricKey)
        employees.forEach(emp => {
          allDetails.push({
            '部门层级': `${'-'.repeat(level)}${node.label}`,
            '班次': shift === 'day' ? '白班' : '夜班',
            '三层部门': emp.l3OrganizationCn,
            '四层部门': emp.l4OrganizationCn || '',
            '五层部门': emp.l5OrganizationCn || '',
            '最小部门': emp.organizationNameCn,
            '姓名': emp.employeeName,
            '工号': emp.employeeNo,
            ...(cat.metricKey === 'systemManpower' ? {} : {
              'ESD状态': emp.lasttimeEsd ? '已测' : '未测',
              '班会点名': rawData.manpowerDetailList.some(m => m.employeeNo === emp.employeeNo && m.shiftId === shiftId) ? '已点名' : '未点名',
              '技能刷卡': rawData.staffScheduleInfoList.some(s => s.employeeNo === emp.employeeNo && s.status === '已刷卡' && s.shiftId === shiftId) ? '已刷卡' : '未刷卡',
              '刷卡签到': rawData.swipeCardSignList.some(s => s.employeeNo === emp.employeeNo && s.shiftId === shiftId) ? '已签到' : '未签到',
              '提交人': rawData.manpowerDetailList.find(m => m.employeeNo === emp.employeeNo && m.shiftId === shiftId)?.createBy || ''
            })
          })
        })
      })
      if (node.children) node.children.forEach(child => collectDetails(child, level + 1))
    }
    collectDetails(deptNode)
    // 转为 sheet
    const ws = XLSX.utils.json_to_sheet(allDetails)
    // 设置标题行加粗（简单处理：通过列宽设置不影响，但加粗需用样式，XLSX 样式复杂，此处不深究）
    XLSX.utils.book_append_sheet(workbook, ws, cat.sheetName)
  }
  
  // 导出文件
  XLSX.writeFile(workbook, `出勤明细全量_${dateRange.value}.xlsx`)
  ElMessage.success('全部分类明细已导出')
}

// 其他函数（getAllEmployeesInDept, handleNumberClick 等）需要适配新的 metricKey 类型，已有实现可复用，只需确保支持 skillSwipe, morningMeeting, swipeSign
// 注意：原有的 getAllEmployeesInDept 已支持这些类型，无需修改

// 初始化等保持不变
</script>
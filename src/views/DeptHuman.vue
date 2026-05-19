<template>
  <div class="dashboard-container">
    <!-- 左侧部门树 -->
    <div class="left-panel">
      <div class="dept-header">部门架构</div>
      <el-tree
        :data="deptTree"
        :props="treeProps"
        node-key="id"
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
          <el-select v-model="shiftType" placeholder="班次" style="width: 120px">
            <el-option label="全部" value="all" />
            <el-option label="白班" value="day" />
            <el-option label="夜班" value="night" />
          </el-select>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleExport">导出</el-button>
        </div>
      </div>

      <!-- 中间：出勤数据表格（树形结构 + 白班/夜班横向分组） -->
      <div class="middle-table">
        <el-table
          :data="tableData"
          row-key="id"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          border
          stripe
          @cell-click="handleCellClick"
        >
          <el-table-column prop="deptName" label="部门名称" min-width="180" fixed="left" />
          
          <!-- 白班列组 -->
          <el-table-column label="白班" align="center">
            <el-table-column prop="day_systemManpower" label="系统人力" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.day_systemManpower !== undefined && row.day_systemManpower !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'day', 'systemManpower', row.day_systemManpower)"
                >
                  {{ row.day_systemManpower }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="day_requiredManpower" label="应出勤" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.day_requiredManpower !== undefined && row.day_requiredManpower !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'day', 'requiredManpower', row.day_requiredManpower)"
                >
                  {{ row.day_requiredManpower }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="day_actualAttendance" label="实出勤" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.day_actualAttendance !== undefined && row.day_actualAttendance !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'day', 'actualAttendance', row.day_actualAttendance)"
                >
                  {{ row.day_actualAttendance }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="day_inventoryManpower" label="盘点人力" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.day_inventoryManpower !== undefined && row.day_inventoryManpower !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'day', 'inventoryManpower', row.day_inventoryManpower)"
                >
                  {{ row.day_inventoryManpower }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="day_receiveCount" label="接收" min-width="90">
              <template #default="{ row }">
                <span 
                  v-if="row.day_receiveCount !== undefined && row.day_receiveCount !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'day', 'receiveCount', row.day_receiveCount)"
                >
                  {{ row.day_receiveCount }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="day_supportCount" label="支援" min-width="90">
              <template #default="{ row }">
                <span 
                  v-if="row.day_supportCount !== undefined && row.day_supportCount !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'day', 'supportCount', row.day_supportCount)"
                >
                  {{ row.day_supportCount }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table-column>

          <!-- 夜班列组 -->
          <el-table-column label="夜班" align="center">
            <el-table-column prop="night_systemManpower" label="系统人力" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.night_systemManpower !== undefined && row.night_systemManpower !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'night', 'systemManpower', row.night_systemManpower)"
                >
                  {{ row.night_systemManpower }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="night_requiredManpower" label="应出勤" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.night_requiredManpower !== undefined && row.night_requiredManpower !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'night', 'requiredManpower', row.night_requiredManpower)"
                >
                  {{ row.night_requiredManpower }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="night_actualAttendance" label="实出勤" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.night_actualAttendance !== undefined && row.night_actualAttendance !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'night', 'actualAttendance', row.night_actualAttendance)"
                >
                  {{ row.night_actualAttendance }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="night_inventoryManpower" label="盘点人力" min-width="100">
              <template #default="{ row }">
                <span 
                  v-if="row.night_inventoryManpower !== undefined && row.night_inventoryManpower !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'night', 'inventoryManpower', row.night_inventoryManpower)"
                >
                  {{ row.night_inventoryManpower }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="night_receiveCount" label="接收" min-width="90">
              <template #default="{ row }">
                <span 
                  v-if="row.night_receiveCount !== undefined && row.night_receiveCount !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'night', 'receiveCount', row.night_receiveCount)"
                >
                  {{ row.night_receiveCount }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="night_supportCount" label="支援" min-width="90">
              <template #default="{ row }">
                <span 
                  v-if="row.night_supportCount !== undefined && row.night_supportCount !== null"
                  class="clickable-number" 
                  @click.stop="handleNumberClick(row, 'night', 'supportCount', row.night_supportCount)"
                >
                  {{ row.night_supportCount }}
                </span>
                <span v-else>-</span>
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
          <el-table-column prop="section" label="工段" width="100" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="employeeId" label="工号" width="120" />
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="meetingRollCall" label="班会点名" width="110" />
          <el-table-column prop="outSupportSection" label="外出支援工段" width="140" />
          <el-table-column prop="isReceive" label="是否接收" width="100" />
          <el-table-column prop="support" label="支援" width="80" />
          <el-table-column prop="esdSubmitter" label="ESD状态提交人" width="140" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, InfoFilled } from '@element-plus/icons-vue'
// import * as XLSX from 'xlsx'

// ---------- 生成深层部门树（1~5层） ----------
const generateDeptTree = (prefix = 'dept', level = 1, maxLevel = 5) => {
  const id = `${prefix}_${level}_${Math.random().toString(36).substr(2, 4)}`
  const label = `部门${prefix}_L${level}`
  const node = { id, label }
  
  if (level < maxLevel) {
    const childCount = Math.floor(Math.random() * 3) + 1 // 1-3个子部门
    node.children = []
    for (let i = 1; i <= childCount; i++) {
      node.children.push(generateDeptTree(`${prefix}_${level}_${i}`, level + 1, maxLevel))
    }
  }
  return node
}

// 构建左侧树：3棵主树，深度随机3~5层
const deptTree = ref([
  generateDeptTree('生产', 1, 4),
  generateDeptTree('质量', 1, 5),
  generateDeptTree('供应链', 1, 3)
])

const treeProps = { children: 'children', label: 'label' }

// 当前选中的部门节点
const selectedDept = ref(null)

// 筛选条件
const dateRange = ref(new Date().toISOString().slice(0,10))
const shiftType = ref('all') // all, day, night

// 右侧表格数据（树形结构，每个节点含白班/夜班指标，父级自动汇总）
const tableData = ref([])

// 明细相关
const showDetail = ref(false)
const detailData = ref([])
const detailTitle = ref('')

// ---------- 模拟叶子节点数据生成（基于部门id、班次、日期）----------
const generateLeafMetrics = (deptId, deptName, shift, date) => {
  // 使用哈希保证同一部门同一天数据稳定
  const hash = (str) => {
    let h = 0
    for (let i = 0; i < str.length; i++) h = ((h << 5) - h) + str.charCodeAt(i)
    return Math.abs(h)
  }
  const seed = hash(`${deptId}_${shift}_${date}`) % 100
  const base = 20 + (seed % 30)
  return {
    systemManpower: base + Math.floor(seed % 15),
    requiredManpower: base + Math.floor((seed + 7) % 12),
    actualAttendance: base + Math.floor((seed + 3) % 10),
    inventoryManpower: base + Math.floor((seed + 11) % 8),
    receiveCount: Math.floor((seed % 6)),
    supportCount: Math.floor((seed % 5))
  }
}

// 递归构建树并自动汇总父节点数值
// 参数: deptNode - 部门节点, date, shiftFilter, 返回包含children和指标的row对象
const buildTreeWithSummary = (deptNode, date, shiftFilter) => {
  if (!deptNode) return null
  
  const deptId = deptNode.id
  const deptName = deptNode.label
  
  // 先递归构建子节点
  let children = []
  if (deptNode.children && deptNode.children.length) {
    children = deptNode.children
      .map(child => buildTreeWithSummary(child, date, shiftFilter))
      .filter(child => child !== null)
  }
  
  // 判断是否为叶子节点（无子节点）
  const isLeaf = children.length === 0
  
  // 初始化当前节点的指标（白班和夜班）
  let dayMetrics = null
  let nightMetrics = null
  
  if (isLeaf) {
    // 叶子节点：根据班次筛选生成原始数据
    if (shiftFilter === 'all' || shiftFilter === 'day') {
      dayMetrics = generateLeafMetrics(deptId, deptName, 'day', date)
    }
    if (shiftFilter === 'all' || shiftFilter === 'night') {
      nightMetrics = generateLeafMetrics(deptId, deptName, 'night', date)
    }
  } else {
    // 非叶子节点：汇总所有子节点的对应班次指标
    // 白班汇总
    if (shiftFilter === 'all' || shiftFilter === 'day') {
      dayMetrics = {
        systemManpower: 0,
        requiredManpower: 0,
        actualAttendance: 0,
        inventoryManpower: 0,
        receiveCount: 0,
        supportCount: 0
      }
      children.forEach(child => {
        if (child.day_systemManpower !== undefined && child.day_systemManpower !== null) {
          dayMetrics.systemManpower += child.day_systemManpower
          dayMetrics.requiredManpower += child.day_requiredManpower
          dayMetrics.actualAttendance += child.day_actualAttendance
          dayMetrics.inventoryManpower += child.day_inventoryManpower
          dayMetrics.receiveCount += child.day_receiveCount
          dayMetrics.supportCount += child.day_supportCount
        }
      })
      // 如果所有子节点都没有白班数据，则设为null
      if (children.every(c => c.day_systemManpower === undefined || c.day_systemManpower === null)) {
        dayMetrics = null
      }
    }
    // 夜班汇总
    if (shiftFilter === 'all' || shiftFilter === 'night') {
      nightMetrics = {
        systemManpower: 0,
        requiredManpower: 0,
        actualAttendance: 0,
        inventoryManpower: 0,
        receiveCount: 0,
        supportCount: 0
      }
      children.forEach(child => {
        if (child.night_systemManpower !== undefined && child.night_systemManpower !== null) {
          nightMetrics.systemManpower += child.night_systemManpower
          nightMetrics.requiredManpower += child.night_requiredManpower
          nightMetrics.actualAttendance += child.night_actualAttendance
          nightMetrics.inventoryManpower += child.night_inventoryManpower
          nightMetrics.receiveCount += child.night_receiveCount
          nightMetrics.supportCount += child.night_supportCount
        }
      })
      if (children.every(c => c.night_systemManpower === undefined || c.night_systemManpower === null)) {
        nightMetrics = null
      }
    }
  }
  
  // 构建当前行对象
  const row = {
    id: deptId,
    deptName: deptName,
    day_systemManpower: dayMetrics?.systemManpower ?? null,
    day_requiredManpower: dayMetrics?.requiredManpower ?? null,
    day_actualAttendance: dayMetrics?.actualAttendance ?? null,
    day_inventoryManpower: dayMetrics?.inventoryManpower ?? null,
    day_receiveCount: dayMetrics?.receiveCount ?? null,
    day_supportCount: dayMetrics?.supportCount ?? null,
    night_systemManpower: nightMetrics?.systemManpower ?? null,
    night_requiredManpower: nightMetrics?.requiredManpower ?? null,
    night_actualAttendance: nightMetrics?.actualAttendance ?? null,
    night_inventoryManpower: nightMetrics?.inventoryManpower ?? null,
    night_receiveCount: nightMetrics?.receiveCount ?? null,
    night_supportCount: nightMetrics?.supportCount ?? null,
    children: children
  }
  
  // 如果当前节点是汇总节点且所有汇总值为0，可视情况保留（可能没有子节点数据，但一般会有）
  return row
}

// 刷新右侧表格（根据选中的部门节点构建树）
const refreshTableData = () => {
  if (!selectedDept.value) {
    tableData.value = []
    return
  }
  const treeRoot = buildTreeWithSummary(selectedDept.value, dateRange.value, shiftType.value)
  if (treeRoot) {
    tableData.value = [treeRoot]
  } else {
    tableData.value = []
  }
}

// 左侧部门树点击
const handleDeptChange = (data) => {
  selectedDept.value = data
  refreshTableData()
  closeDetail()
}

// 查询
const handleQuery = () => {
  refreshTableData()
  ElMessage.success('查询完成')
}

// 导出表格（扁平化）
const flattenTree = (rows, result = []) => {
  rows.forEach(row => {
    const flatRow = {
      '部门名称': row.deptName,
      '白班-系统人力': row.day_systemManpower ?? '',
      '白班-应出勤': row.day_requiredManpower ?? '',
      '白班-实出勤': row.day_actualAttendance ?? '',
      '白班-盘点人力': row.day_inventoryManpower ?? '',
      '白班-接收': row.day_receiveCount ?? '',
      '白班-支援': row.day_supportCount ?? '',
      '夜班-系统人力': row.night_systemManpower ?? '',
      '夜班-应出勤': row.night_requiredManpower ?? '',
      '夜班-实出勤': row.night_actualAttendance ?? '',
      '夜班-盘点人力': row.night_inventoryManpower ?? '',
      '夜班-接收': row.night_receiveCount ?? '',
      '夜班-支援': row.night_supportCount ?? ''
    }
    result.push(flatRow)
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

// ---------- 明细数据模拟（根据部门节点、班次、指标类型生成）----------
// 递归获取部门下所有叶子节点名称（用于明细条数参考）
const collectLeafDeptNames = (row, names = []) => {
  if (!row.children || row.children.length === 0) {
    names.push(row.deptName)
  } else {
    row.children.forEach(child => collectLeafDeptNames(child, names))
  }
  return names
}

const generateDetailDataForNode = (row, shift, metricKey, metricValue) => {
  // 获取该部门下所有叶子部门名称（用于模拟明细数据）
  const leafNames = collectLeafDeptNames(row, [])
  const deptCount = Math.max(1, leafNames.length)
  // 根据部门规模决定明细条目数
  const itemCount = Math.min(10, 3 + deptCount * 2)
  
  const shiftText = shift === 'day' ? '白班' : '夜班'
  const metricMap = {
    systemManpower: '系统人力',
    requiredManpower: '应出勤',
    actualAttendance: '实出勤',
    inventoryManpower: '盘点人力',
    receiveCount: '接收',
    supportCount: '支援'
  }
  const metricName = metricMap[metricKey]
  
  // 模拟人员池
  const namePool = ['张明', '李芳', '王磊', '陈丽', '赵岩', '周强', '吴迪', '郑爽', '孙阳', '林欣']
  const sectionPool = ['组装线', '测试段', '包装段', 'IQC', 'OQC', '仓储科', '物流科']
  const typePool = ['正式工', '派遣工', '临时工', '实习生']
  const rollCallPool = ['已点名', '未点名']
  const receivePool = ['是', '否']
  const supportPool = ['是', '否']
  const esdPool = ['李主管', '王工', '赵经理', '孙工', '周主管', '陈主任']
  
  const details = []
  for (let i = 0; i < itemCount; i++) {
    details.push({
      section: sectionPool[i % sectionPool.length],
      name: namePool[i % namePool.length] + (i + 1),
      employeeId: `100${String(i + 1).padStart(3, '0')}`,
      type: typePool[i % typePool.length],
      meetingRollCall: rollCallPool[i % rollCallPool.length],
      outSupportSection: i % 3 === 0 ? '支援科' : '-',
      isReceive: receivePool[i % receivePool.length],
      support: supportPool[i % supportPool.length],
      esdSubmitter: esdPool[i % esdPool.length]
    })
  }
  return details
}

// 点击数字显示明细
const handleNumberClick = (row, shift, metricKey, value) => {
  const shiftText = shift === 'day' ? '白班' : '夜班'
  const metricMap = {
    systemManpower: '系统人力',
    requiredManpower: '应出勤',
    actualAttendance: '实出勤',
    inventoryManpower: '盘点人力',
    receiveCount: '接收',
    supportCount: '支援'
  }
  const metricName = metricMap[metricKey]
  const title = `${row.deptName} (${shiftText}) · ${metricName}: ${value}`
  
  const details = generateDetailDataForNode(row, shift, metricKey, value)
  detailData.value = details
  detailTitle.value = title
  showDetail.value = true
}

const handleCellClick = () => {}
const closeDetail = () => {
  showDetail.value = false
  detailData.value = []
  detailTitle.value = ''
}

// 初始化选中第一个部门
onMounted(() => {
  if (deptTree.value.length > 0) {
    selectedDept.value = deptTree.value[0]
    refreshTableData()
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
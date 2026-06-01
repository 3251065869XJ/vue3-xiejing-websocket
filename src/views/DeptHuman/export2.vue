<template>
  <!-- 上方工具栏增加一个按钮：导出全部分类明细（多文件） -->
  <div class="filter-group">
    <!-- 原有按钮 ... -->
    <el-button type="warning" @click="exportAllDetailsNative">导出全部分类明细（多文件）</el-button>
  </div>
</template>

<script setup>
// ... 原有代码 ...

/**
 * 原生导出 HTML 表格为 .xls 文件（支持样式加粗）
 * @param {Array} data 明细数据数组，每项是一个对象
 * @param {Array} columns 列定义 [{ prop, label, width }]
 * @param {string} sheetName 表名（用于文件名）
 */
const exportToXLS = (data, columns, sheetName) => {
  // 构建 HTML 表格
  let html = `<!DOCTYPE html>
  <html>
  <head>
    <meta charset="UTF-8">
    <title>${sheetName}</title>
    <style>
      th { background: #f2f2f2; font-weight: bold; text-align: center; border: 1px solid #ccc; padding: 6px; }
      td { border: 1px solid #ccc; padding: 4px; }
      table { border-collapse: collapse; width: 100%; }
    </style>
  </head>
  <body>
    <table>`
  // 表头
  html += `<thead><tr>`
  columns.forEach(col => {
    html += `<th style="font-weight:bold;">${col.label}</th>`
  })
  html += `</tr></thead><tbody>`
  // 数据行
  data.forEach(row => {
    html += `<tr>`
    columns.forEach(col => {
      let value = row[col.prop] !== undefined && row[col.prop] !== null ? row[col.prop] : ''
      html += `<td>${value}</td>`
    })
    html += `</tr>`
  })
  html += `</tbody></table></body></html>`
  
  // 创建 Blob 并下载
  const blob = new Blob([html], { type: 'application/vnd.ms-excel' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.href = url
  link.download = `${sheetName}_${dateRange.value}.xls`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

// 导出全部分类明细（原生多文件）
const exportAllDetailsNative = async () => {
  if (!selectedDept.value) {
    ElMessage.warning('请先选择一个部门')
    return
  }
  const deptNode = selectedDept.value
  
  // 定义分类
  const categories = [
    { name: '系统人力', metricKey: 'systemManpower', sheetName: '系统人力' },
    { name: '实出勤人力', metricKey: 'actualAttendance', sheetName: '实出勤人力' },
    { name: '技能刷卡人力', metricKey: 'skillSwipe', sheetName: '技能刷卡人力' },
    { name: '刷卡签到人力', metricKey: 'swipeSign', sheetName: '刷卡签到人力' },
    { name: '早会点名人力', metricKey: 'morningMeeting', sheetName: '早会点名人力' }
  ]
  
  for (const cat of categories) {
    // 收集该分类下所有层级的明细
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
            '三层部门': emp.l3OrganizationCn,
            '四层部门': emp.l4OrganizationCn || '',
            '五层部门': emp.l5OrganizationCn || '',
            '最小部门': emp.organizationNameCn,
            '姓名': emp.employeeName,
            '工号': emp.employeeNo
          }
          // 根据分类添加额外字段
          if (cat.metricKey !== 'systemManpower') {
            detail['ESD状态'] = emp.lasttimeEsd ? '已测' : '未测'
            detail['班会点名'] = rawData.manpowerDetailList.some(m => m.employeeNo === emp.employeeNo && m.shiftId === shiftId) ? '已点名' : '未点名'
            detail['技能刷卡'] = rawData.staffScheduleInfoList.some(s => s.employeeNo === emp.employeeNo && s.status === '已刷卡' && s.shiftId === shiftId) ? '已刷卡' : '未刷卡'
            detail['刷卡签到'] = rawData.swipeCardSignList.some(s => s.employeeNo === emp.employeeNo && s.shiftId === shiftId) ? '已签到' : '未签到'
            detail['提交人'] = rawData.manpowerDetailList.find(m => m.employeeNo === emp.employeeNo && m.shiftId === shiftId)?.createBy || ''
          }
          allDetails.push(detail)
        })
      })
      if (node.children) node.children.forEach(child => collectDetails(child, level + 1))
    }
    collectDetails(deptNode)
    
    if (allDetails.length === 0) {
      console.warn(`${cat.name} 无数据，跳过导出`)
      continue
    }
    
    // 定义列
    let columns = []
    if (cat.metricKey === 'systemManpower') {
      columns = [
        { prop: '部门层级', label: '部门层级' },
        { prop: '班次', label: '班次' },
        { prop: '三层部门', label: '三层部门名称' },
        { prop: '四层部门', label: '四层部门名称' },
        { prop: '五层部门', label: '五层部门名称' },
        { prop: '最小部门', label: '最小部门名称' },
        { prop: '姓名', label: '姓名' },
        { prop: '工号', label: '工号' }
      ]
    } else {
      columns = [
        { prop: '部门层级', label: '部门层级' },
        { prop: '班次', label: '班次' },
        { prop: '三层部门', label: '三层部门名称' },
        { prop: '四层部门', label: '四层部门名称' },
        { prop: '五层部门', label: '五层部门名称' },
        { prop: '最小部门', label: '最小部门名称' },
        { prop: '姓名', label: '姓名' },
        { prop: '工号', label: '工号' },
        { prop: 'ESD状态', label: 'ESD状态' },
        { prop: '班会点名', label: '班会点名' },
        { prop: '技能刷卡', label: '技能刷卡' },
        { prop: '刷卡签到', label: '刷卡签到' },
        { prop: '提交人', label: '提交人' }
      ]
    }
    
    exportToXLS(allDetails, columns, cat.sheetName)
    // 避免同时弹出过多下载窗口，添加延时
    await new Promise(resolve => setTimeout(resolve, 500))
  }
  ElMessage.success('已开始下载所有分类明细文件')
}

// 其他函数保持不变（getAllEmployeesInDept、computeDeptMetrics 等）
// 注意：需要确保 computeDeptMetrics 中包含 skillSwipe, morningMeeting, swipeSign 字段，之前已实现
</script>
<template>
  <div class="inventory-container">
    <!-- 标题栏 + 操作按钮 -->
    <div class="header-actions">
      <div>
        <h2 class="title">📦 库存水位监控表</h2>
        <div class="tip">
          💧 圆形水位图 = 实际库存 / 计划库存 × 100% &nbsp; | &nbsp; 高亮水位动态填充，百分比实时显示
        </div>
      </div>
      <div class="action-buttons">
        <el-button type="primary" plain @click="randomUpdateSingleRow">
          ✨ 随机更新单行
        </el-button>
        <el-button type="success" @click="randomBatchUpdate">
          🔄 批量刷新数据
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%"
      :header-cell-style="{ background: '#f8fafc', color: '#1e293b', fontWeight: '600' }"
    >
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="planStock" label="📊 计划库存数" width="130" align="center">
        <template #default="{ row }">
          <span style="font-weight: 500;">{{ row.planStock }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="actualStock" label="📈 实际库存数" width="130" align="center">
        <template #default="{ row }">
          <span :style="{ color: row.actualStock > row.planStock ? '#16a34a' : '#475569' }">
            {{ row.actualStock }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="💧 水位图 (圆形动态)" width="120" align="center">
        <template #default="{ row }">
          <WaterLevelCircle :plan-stock="row.planStock" :actual-stock="row.actualStock" />
        </template>
      </el-table-column>
      <el-table-column label="📌 水位状态" width="130" align="center">
        <template #default="{ row }">
          <el-tag :type="getStockStatus(row)" size="small" effect="light" style="border-radius: 20px;">
            {{ getStockText(row) }}
          </el-tag>
          <div style="font-size: 12px; color: #5b6e8c; margin-top: 4px;">
            完成率: {{ getCompletionRate(row) }}%
          </div>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row, $index }">
          <el-button link type="primary" size="small" @click="adjustActualStock(row)">
            调整实际
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 说明信息 -->
    <div class="footer-note">
      <div>
        🧩 <strong>水位图说明：</strong> 圆形区域蓝色填充比例 = (实际库存 / 计划库存) ，百分比动态显示在圆心。
        支持计划库存、实际库存动态传入，水位实时重绘。
      </div>
      <div>
        💡 <strong>动态演示：</strong> 点击上方按钮可随机修改数据，水位图及百分比会立刻响应变化。
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import WaterLevelCircle from '@/views/WaterLevelCircle.vue';

// 模拟数据
const tableData = ref([
  { id: 1, name: '智能办公笔记本', planStock: 120, actualStock: 87 },
  { id: 2, name: '无线静音鼠标', planStock: 200, actualStock: 156 },
  { id: 3, name: '机械键盘 (RGB)', planStock: 80, actualStock: 80 },
  { id: 4, name: 'Type-C 扩展坞', planStock: 50, actualStock: 12 },
  { id: 5, name: '4K 便携显示器', planStock: 30, actualStock: 29 },
  { id: 6, name: '氮化镓充电器', planStock: 150, actualStock: 210 },
  { id: 7, name: '降噪蓝牙耳机', planStock: 90, actualStock: 45 },
  { id: 8, name: '移动固态硬盘 1TB', planStock: 60, actualStock: 0 }
]);

// 辅助函数：完成率
const getCompletionRate = (row) => {
  if (row.planStock <= 0) return 0;
  return Math.min(100, Math.round((row.actualStock / row.planStock) * 100));
};

// 状态标签类型
const getStockStatus = (row) => {
  if (row.planStock <= 0) return 'info';
  const ratio = row.actualStock / row.planStock;
  if (ratio >= 1) return 'success';
  if (ratio >= 0.6) return 'warning';
  return 'danger';
};

// 状态文本
const getStockText = (row) => {
  if (row.planStock <= 0) return '计划无效';
  const ratio = row.actualStock / row.planStock;
  if (ratio >= 1) return '库存充足';
  if (ratio >= 0.6) return '正常水位';
  if (ratio >= 0.2) return '偏低库存';
  return '紧急补货';
};

// 随机更新单行
const randomUpdateSingleRow = () => {
  if (!tableData.value.length) return;
  const randomIndex = Math.floor(Math.random() * tableData.value.length);
  const row = tableData.value[randomIndex];
  const changeType = Math.random() > 0.6 ? 'plan' : 'actual';
  if (changeType === 'plan') {
    let delta = Math.floor(Math.random() * 40) + 10;
    let newPlan = row.planStock + (Math.random() > 0.5 ? delta : -delta);
    if (newPlan < 5) newPlan = 5;
    row.planStock = Math.floor(newPlan);
  } else {
    let delta = Math.floor(Math.random() * 70) - 20;
    let newActual = row.actualStock + delta;
    if (newActual < 0) newActual = 0;
    row.actualStock = Math.floor(newActual);
  }
  // 触发响应式更新
  tableData.value = [...tableData.value];
  ElMessage.success(`已更新商品 “${row.name}” 的库存数据，水位图已动态刷新`);
};

// 批量刷新数据
const randomBatchUpdate = () => {
  tableData.value = tableData.value.map(item => {
    let newPlan = item.planStock + (Math.random() > 0.7 ? Math.floor(Math.random() * 30) - 10 : 0);
    if (newPlan < 5) newPlan = 5;
    let newActual = item.actualStock + (Math.random() > 0.6 ? Math.floor(Math.random() * 50) - 15 : 0);
    if (newActual < 0) newActual = 0;
    return {
      ...item,
      planStock: Math.floor(newPlan),
      actualStock: Math.floor(newActual)
    };
  });
  ElMessage.success('批量数据已刷新，所有水位图动态更新');
};

// 调整实际库存（行内操作）
const adjustActualStock = (row) => {
  let delta = Math.floor(Math.random() * 30) + 5;
  let newActual = row.actualStock + (Math.random() > 0.5 ? delta : -delta);
  if (newActual < 0) newActual = 0;
  row.actualStock = newActual;
  tableData.value = [...tableData.value];
  ElMessage.info(`商品「${row.name}」实际库存已调整`);
};
</script>

<style scoped>
.inventory-container {
  background: white;
  border-radius: 24px;
  padding: 20px 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.title {
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #1e293b;
}
.tip {
  font-size: 14px;
  color: #5e5e6e;
  background: #eef2ff;
  padding: 8px 16px;
  border-radius: 12px;
  display: inline-block;
}
.action-buttons {
  display: flex;
  gap: 12px;
}
.footer-note {
  margin-top: 24px;
  background: #f9f9fc;
  border-radius: 16px;
  padding: 12px 20px;
  font-size: 13px;
  color: #4b5563;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
}
</style>
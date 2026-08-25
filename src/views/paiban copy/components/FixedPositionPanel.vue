<template>
  <div class="panel">
    <div class="panel-header">
      <div class="panel-title"><span class="dot"></span>🔧 线体固定岗位</div>
      <el-button size="small" type="primary" plain round @click="$emit('add-temp')">
        + 添加临时岗位
      </el-button>
    </div>
    <div class="panel-body">
      <div v-if="positions.length === 0" class="empty-state">
        <div class="empty-icon">📦</div>
        <div class="empty-text">请先添加配置</div>
      </div>
      <div class="position-grid">
        <PositionCard
          v-for="pos in positions"
          :key="pos.id"
          :position="pos"
          :assigned-employee="getAssignedEmployee(pos.id)"
          :is-selected="selectedPositionId === pos.id"
          @select="$emit('select', pos)"
          @assign="$emit('assign', pos)"
          @remove-employee="(empId, posId) => $emit('remove-employee', empId, posId)"
          @drop-employee="$emit('drop-employee', pos.id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import PositionCard from './PositionCard.vue'

const props = defineProps({
  positions: { type: Array, required: true },
  employees: { type: Array, required: true },
  selectedPositionId: { type: String, default: null },
  assignedMap: { type: Object, required: true },
})

defineEmits(['select', 'assign', 'remove-employee', 'drop-employee', 'add-temp'])

function getAssignedEmployee(positionId) {
  const empId = props.assignedMap[positionId]
  if (!empId) return null
  return props.employees.find(e => e.id === empId) || null
}
</script>

<style scoped>
.panel {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(26, 43, 74, 0.06);
  border: 1px solid #e8ecf4;
  overflow: hidden;
}
.panel-header {
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8ecf4;
  background: linear-gradient(180deg, #fafbfe, #fff);
}
.panel-title {
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1a2b4a;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4a7cf7;
  display: inline-block;
}
.panel-body {
  padding: 8px 10px;
  max-height: 500px;
  overflow-y: auto;
}
.position-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 8px;
}
.empty-state {
  text-align: center;
  padding: 20px;
  color: #b0b8cc;
}
.empty-icon { font-size: 32px; margin-bottom: 8px; opacity: 0.6; }
.empty-text { font-size: 12px; }
</style>
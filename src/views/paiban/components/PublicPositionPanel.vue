<template>
  <div class="panel">
    <div class="panel-header">
      <div class="panel-title"><span class="dot orange"></span>🌐 公共岗位</div>
      <el-button
        size="small"
        type="success"
        plain
        round
        @click="$emit('add-public')"
        :disabled="positions.length >= 10"
      >
        + 添加全能员 ({{ positions.length }}/10)
      </el-button>
    </div>
    <div class="panel-body">
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

defineEmits(['select', 'assign', 'remove-employee', 'drop-employee', 'add-public'])

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
  display: inline-block;
}
.dot.orange { background: #ffb74d; }
.panel-body {
  padding: 8px 10px;
  max-height: 500px;
  overflow-y: auto;
}
.position-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
  gap: 4px;
}
</style>
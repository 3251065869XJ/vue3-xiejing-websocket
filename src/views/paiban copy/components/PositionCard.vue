<template>
  <div
    class="position-card"
    :class="{ selected: isSelected, occupied: isOccupied }"
    @click="$emit('select', position)"
    @dragover.prevent
    @drop.prevent="$emit('drop-employee', position.id)"
  >
    <div class="card-header">
      <span class="pos-name">{{ position.name }}</span>
      <div class="tags">
        <el-tag v-if="position.isTemp" size="small" class="tag-temp">临时</el-tag>
        <el-tag v-else-if="position.type === 'public'" size="small" class="tag-public">公共</el-tag>
        <el-tag v-else size="small" class="tag-fixed">固定</el-tag>
        <el-tag size="small" type="info" class="shift-tag">{{ shiftName }}</el-tag>
      </div>
    </div>
    <div class="skills-tags">
      <el-tooltip :content="position.skills.join(', ')" placement="top" effect="dark">
        <span class="skill-summary">{{ position.skills.slice(0,3).join('、') }}<template v-if="position.skills.length > 3"> 等{{ position.skills.length }}项</template></span>
      </el-tooltip>
    </div>
    <div class="assigned-employee">
      <template v-if="assignedEmployee">
        <span class="emp-chip">
          <span class="avatar-mini">{{ assignedEmployee.name.charAt(0) }}</span>
          {{ assignedEmployee.name }}
          <span class="remove-btn" @click.stop="$emit('remove-employee', assignedEmployee.id, position.id)">✕</span>
        </span>
      </template>
      <template v-else>
        <span class="empty-slot">空闲</span>
      </template>
    </div>
    <el-button
      v-if="!assignedEmployee"
      class="assign-btn"
      type="primary"
      size="small"
      @click.stop="$emit('assign', position)"
    >分配</el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  position: { type: Object, required: true },
  assignedEmployee: { type: Object, default: null },
  isSelected: { type: Boolean, default: false },
})

defineEmits(['select', 'assign', 'remove-employee', 'drop-employee'])

const isOccupied = computed(() => props.assignedEmployee != null)

const shiftName = computed(() => {
  const map = { shift1: '白班', shift2: '夜班' }
  return map[props.position.shiftId] || ''
})
</script>

<style scoped>
.position-card {
  border: 1px solid #e8ecf4;
  border-radius: 6px;
  padding: 8px 10px;
  background: #fafbfe;
  transition: all 0.15s;
  cursor: pointer;
  position: relative;
  user-select: none;
  font-size: 12px;
  line-height: 1.3;
}
.position-card:hover {
  border-color: #4a7cf7;
  box-shadow: 0 1px 4px rgba(26,43,74,0.08);
}
.position-card.selected {
  border-color: #4a7cf7;
  box-shadow: 0 0 0 2px rgba(74,124,247,0.15);
  background: #fff;
}
.position-card.occupied {
  border-color: #b8e6ce;
  background: #f0faf5;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.pos-name {
  font-weight: 600;
  font-size: 12px;
  color: #1a2b4a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100px;
}
.tags {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
}
.tag-fixed, .tag-public, .tag-temp, .shift-tag {
  font-size: 10px;
  height: 18px;
  line-height: 16px;
  padding: 0 4px;
  border-radius: 3px;
}
.tag-fixed { background: #e8f0fe; border-color: #b8ccff; color: #4a7cf7; }
.tag-public { background: #e6f9f0; border-color: #b8e6ce; color: #28a865; }
.tag-temp { background: #fff4e6; border-color: #ffd9b8; color: #d4882a; }
.shift-tag { background: #f0f2f5; border-color: #dcdfe6; color: #606266; }
.skills-tags { margin-bottom: 4px; }
.skill-summary {
  font-size: 11px;
  color: #5a6b8c;
  cursor: help;
}
.assigned-employee {
  min-height: 20px;
  display: flex;
  align-items: center;
}
.emp-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: #e8f0fe;
  border-radius: 10px;
  padding: 2px 6px 2px 3px;
  font-size: 11px;
  color: #1a2b4a;
  border: 1px solid #c5d8ff;
}
.avatar-mini {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #4a7cf7;
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
}
.remove-btn {
  cursor: pointer;
  color: #ff6b6b;
  font-size: 10px;
}
.empty-slot {
  font-size: 11px;
  color: #b0b8cc;
}
.assign-btn {
  margin-top: 4px;
  padding: 0 6px;
  height: 22px;
  font-size: 11px;
  border-radius: 4px;
  width: 100%;
}
</style>
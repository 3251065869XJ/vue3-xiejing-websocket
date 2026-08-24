<template>
  <div
    class="employee-card"
    :class="{ selected: isSelected, assigned: isAssigned }"
    draggable="true"
    @click="$emit('click')"
    @dragstart="$emit('dragstart', employee)"
    @dragend="$emit('dragend')"
  >
    <div class="card-top">
      <div class="avatar" :class="avatarColor">{{ employee.name.charAt(0) }}</div>
      <div class="emp-info">
        <div class="emp-name">{{ employee.name }}</div>
        <div class="emp-meta">
          <el-tag size="small" :type="employee.type === '正式工' ? 'primary' : 'warning'">{{ employee.type }}</el-tag>
          <el-tag size="small" type="info">{{ shiftName }}</el-tag>
        </div>
      </div>
    </div>
    <div class="skills-container">
      <el-tooltip :content="employee.skills.join(', ')" placement="top" effect="dark">
        <div class="skills-preview">
          <span v-for="sk in displaySkills" :key="sk" class="skill-dot">{{ sk }}</span>
          <span v-if="employee.skills.length > maxVisibleSkills" class="skill-more">+{{ employee.skills.length - maxVisibleSkills }}</span>
        </div>
      </el-tooltip>
    </div>
    <div v-if="isAssigned" class="assigned-tip">
      <span>✓ {{ assignedPosition }}</span>
      <el-button link type="primary" size="small" @click.stop="$emit('modify')">修改</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  employee: { type: Object, required: true },
  isSelected: { type: Boolean, default: false },
  isAssigned: { type: Boolean, default: false },
  assignedPosition: { type: String, default: '' },
})

defineEmits(['click', 'dragstart', 'dragend', 'modify'])

const maxVisibleSkills = 2

const shiftName = computed(() => {
  const map = { shift1: '白班', shift2: '夜班' }
  return map[props.employee.shiftId] || '未知'
})

const displaySkills = computed(() => props.employee.skills.slice(0, maxVisibleSkills))

const avatarColor = computed(() => {
  const colors = ['avatar-color-1', 'avatar-color-2', 'avatar-color-3', 'avatar-color-4', 'avatar-color-5']
  const idx = parseInt(props.employee.id.replace(/\D/g, '')) || 0
  return colors[idx % colors.length]
})
</script>

<style scoped>
.employee-card {
  border: 2px solid #e8ecf4;
  border-radius: 6px;
  padding: 6px 8px;
  background: #fafbfe;
  transition: all 0.2s;
  cursor: grab;
  position: relative;
  user-select: none;
  font-size: 12px;
}
.employee-card:active { cursor: grabbing; }
.employee-card:hover {
  border-color: #b8ccff;
  box-shadow: 0 2px 6px rgba(26,43,74,0.06);
}
.employee-card.selected {
  border-color: #4a7cf7;
  box-shadow: 0 0 0 2px rgba(74,124,247,0.15);
  background: #fff;
}
.employee-card.assigned {
  border-color: #b8e6ce;
  background: #f0faf5;
  opacity: 0.8;
  cursor: default;
}
.card-top {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}
.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 12px;
  color: #fff;
  flex-shrink: 0;
}
.avatar-color-1 { background: linear-gradient(135deg, #4a7cf7, #6c9bff); }
.avatar-color-2 { background: linear-gradient(135deg, #34c77b, #5dd99c); }
.avatar-color-3 { background: linear-gradient(135deg, #ffb74d, #ffc97d); }
.avatar-color-4 { background: linear-gradient(135deg, #ff6b6b, #ff8e8e); }
.avatar-color-5 { background: linear-gradient(135deg, #8b7cf7, #a89bff); }
.emp-info { flex: 1; min-width: 0; }
.emp-name { font-weight: 700; font-size: 12px; color: #1a2b4a; }
.emp-meta { display: flex; gap: 3px; flex-wrap: wrap; margin-top: 2px; }
.emp-meta .el-tag { font-size: 9px; height: 16px; line-height: 14px; padding: 0 3px; border-radius: 3px; }
.skills-container { margin-bottom: 2px; }
.skills-preview { display: flex; flex-wrap: wrap; gap: 2px; }
.skill-dot {
  font-size: 9px;
  padding: 0 4px;
  border-radius: 3px;
  background: #f0f4ff;
  color: #4a7cf7;
  border: 1px solid #d0dcff;
  white-space: nowrap;
}
.skill-more {
  font-size: 9px;
  color: #5a6b8c;
  background: #eef2f8;
  border-radius: 3px;
  padding: 0 4px;
}
.assigned-tip {
  margin-top: 4px;
  font-size: 10px;
  color: #28a865;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
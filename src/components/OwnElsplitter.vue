<template>
  <div style="height: 300px;">
    <!-- 外部触发按钮 -->
    <button @click="toggleLeftPanel">
      {{ leftPanelVisible ? '隐藏左侧面板' : '显示左侧面板' }}
    </button>

    <el-splitter>
      <el-splitter-panel
        v-model:size="leftPanelSize"
        collapsible
        :min="0"
      >
        <div class="demo-panel">1</div>
        <!-- 自定义折叠按钮（可选） -->
        <template #end-collapsible>
          <span>{{ leftPanelVisible ? '◀' : '▶' }}</span>
        </template>
      </el-splitter-panel>

      <el-splitter-panel :min="200">
        <div class="demo-panel">2</div>
      </el-splitter-panel>
    </el-splitter>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// 左侧面板当前尺寸，初始为 30%
const leftPanelSize = ref('30%')

// 是否显示左侧面板
const leftPanelVisible = ref(true)

// 记录隐藏前的尺寸，用于恢复
const cachedSize = ref('30%')

// 切换左侧面板显示/隐藏
const toggleLeftPanel = () => {
  if (leftPanelVisible.value) {
    // 隐藏：缓存当前尺寸，然后设为 0
    cachedSize.value = leftPanelSize.value
    leftPanelSize.value = 0
  } else {
    // 显示：恢复缓存的尺寸
    leftPanelSize.value = cachedSize.value
  }
  leftPanelVisible.value = !leftPanelVisible.value
}
</script>
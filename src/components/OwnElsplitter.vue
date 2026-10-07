<template>
  <div class="page">
    <!-- 顶部工具栏 -->
    <header class="toolbar">
      <div class="brand">
        <el-icon class="brand-icon"><DataAnalysis /></el-icon>
        <span class="brand-text">数据查询平台</span>
      </div>
      <div class="actions">
        <el-button
          type="primary"
          :icon="Search"
          :loading="querying"
          @click="handleQuery"
        >
          查询
        </el-button>
        <el-button :icon="Setting" @click="openConfigDialog">
          个性化配置
        </el-button>
      </div>
    </header>

    <!-- 主体 -->
    <main class="body">
      <el-splitter>
  <el-splitter-panel
    v-model:size="leftPanelSize"
    collapsible
    :min="0"
    @update:size="handleSizeChange"
  >
    <div class="panel">
      <div class="panel-title">筛选条件</div>
      <div class="panel-desc">这里是左侧面板内容</div>
    </div>

    <!-- ⭐ 关键：自定义展开/折叠按钮，拦截点击，接管逻辑 -->
    <template #end-collapsible>
      <el-icon
        class="splitter-toggle"
        @click.stop="toggleLeftPanel"
      >
        <ArrowRight v-if="isLeftHidden" />
        <ArrowLeft v-else />
      </el-icon>
    </template>
  </el-splitter-panel>

  <el-splitter-panel :min="200">
    <div class="panel">
      <div class="panel-title">查询结果</div>
      <div class="panel-desc">这里是右侧结果区域</div>
    </div>
  </el-splitter-panel>
</el-splitter>
    </main>

    <!-- 复用弹窗：查询后询问 / 个性化配置 -->
    <el-dialog
      v-model="dialogVisible"
      width="520px"
      align-center
      :show-close="dialogMode === 'config'"
      :close-on-click-modal="false"
      :close-on-press-escape="dialogMode === 'config'"
      class="preference-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <div class="dialog-icon">
            <el-icon><MagicStick /></el-icon>
          </div>
          <div>
            <div class="dialog-title">
              {{ dialogMode === 'ask' ? '查询完成' : '个性化配置' }}
            </div>
            <div class="dialog-subtitle">
              {{
                dialogMode === 'ask'
                  ? '根据你的使用习惯，调整一下页面显示方式吧'
                  : '随时修改你的偏好，保存后立即生效'
              }}
            </div>
          </div>
        </div>
      </template>

      <div class="option-list">
        <div
          v-for="opt in options"
          :key="opt.key"
          class="option-card"
          :class="{ 'is-active': form[opt.key] }"
          @click="form[opt.key] = !form[opt.key]"
        >
          <div class="option-icon">
            <el-icon><component :is="opt.icon" /></el-icon>
          </div>
          <div class="option-content">
            <div class="option-title">{{ opt.title }}</div>
            <div class="option-desc">{{ opt.desc }}</div>
          </div>
          <el-switch v-model="form[opt.key]" @click.stop />
        </div>
      </div>

      <div v-if="dialogMode === 'ask'" class="dialog-tip">
        <el-icon><InfoFilled /></el-icon>
        <span>
          保存后，下次查询将直接按此配置执行；也可在「个性化配置」中随时更改。
        </span>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogCancel">
            {{ dialogMode === 'ask' ? '暂不设置' : '取消' }}
          </el-button>
          <el-button type="primary" @click="handleDialogConfirm">
            {{ dialogMode === 'ask' ? '保存并应用' : '保存配置' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Search,
  Setting,
  MagicStick,
  InfoFilled,
  DataAnalysis,
  Fold,
  FullScreen
} from '@element-plus/icons-vue'

const STORAGE_KEY = 'query-page-preference-v1'

/* ---------------- 状态 ---------------- */
const querying = ref(false)

const leftPanelSize = ref('30%')
const cachedSize = ref('30%')

function isZero(val) {
  if (val === 0 || val === '0') return true
  if (typeof val === 'string') {
    const n = parseFloat(val)
    return !isNaN(n) && n === 0
  }
  return false
}

// 已保存的偏好
const preference = reactive({
  hideLeftPanel: false,
  fullscreen: false,
  saved: false // 是否已经保存过配置
})

// 弹窗
const dialogVisible = ref(false)
const dialogMode = ref('ask') // 'ask' | 'config'
const form = reactive({
  hideLeftPanel: false,
  fullscreen: false
})

const options = [
  {
    key: 'hideLeftPanel',
    title: '隐藏左侧面板',
    desc: '查询后自动收起左侧筛选区，让结果区域更宽敞',
    icon: Fold
  },
  {
    key: 'fullscreen',
    title: '全屏显示页面',
    desc: '查询后让页面铺满整个屏幕，减少干扰更专注',
    icon: FullScreen
  }
]

/* ---------------- 持久化 ---------------- */
function loadPreference() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const data = JSON.parse(raw)
      preference.hideLeftPanel = !!data.hideLeftPanel
      preference.fullscreen = !!data.fullscreen
      preference.saved = true
    }
  } catch (e) {
    console.warn('读取偏好配置失败', e)
  }
}

function savePreference() {
  try {
    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        hideLeftPanel: preference.hideLeftPanel,
        fullscreen: preference.fullscreen
      })
    )
  } catch (e) {
    console.warn('保存偏好配置失败', e)
  }
}

/* ---------------- 应用配置 ---------------- */
function applyLeftPanel() {
  if (preference.hideLeftPanel) {
    if (leftPanelSize.value !== 0 && leftPanelSize.value !== '0') {
      cachedSize.value = leftPanelSize.value
    }
    leftPanelSize.value = 0
  } else {
    leftPanelSize.value = cachedSize.value || '30%'
  }
}

async function applyFullscreen() {
  try {
    if (preference.fullscreen) {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen()
      }
    } else if (document.fullscreenElement) {
      await document.exitFullscreen()
    }
  } catch (e) {
    console.warn('全屏切换失败（可能被浏览器拦截）', e)
  }
}

function applyPreference() {
  applyLeftPanel()
  applyFullscreen()
}

/* ---------------- 查询逻辑 ---------------- */
// 模拟查询接口，替换为真实请求即可
function mockQuery() {
  return new Promise((resolve) => setTimeout(resolve, 600))
}

async function handleQuery() {
  // 若已保存过配置：在用户手势内先同步触发全屏，再发起查询
  if (preference.saved) {
    applyPreference()
  }

  querying.value = true
  try {
    await mockQuery()
  } finally {
    querying.value = false
  }

  // 首次查询（未保存过配置）→ 弹窗询问
  if (!preference.saved) {
    openAskDialog()
  }
}

/* ---------------- 弹窗逻辑 ---------------- */
function openAskDialog() {
  dialogMode.value = 'ask'
  form.hideLeftPanel = false
  form.fullscreen = false
  dialogVisible.value = true
}

function openConfigDialog() {
  dialogMode.value = 'config'
  form.hideLeftPanel = preference.hideLeftPanel
  form.fullscreen = preference.fullscreen
  dialogVisible.value = true
}

function handleDialogCancel() {
  dialogVisible.value = false
}

function handleDialogConfirm() {
  preference.hideLeftPanel = form.hideLeftPanel
  preference.fullscreen = form.fullscreen
  preference.saved = true
  savePreference()
  applyPreference()

  dialogVisible.value = false
  ElMessage.success(
    dialogMode.value === 'ask' ? '已记住你的偏好' : '配置已更新'
  )
}

// 当前左侧面板是否隐藏
const isLeftHidden = computed(() => isZero(leftPanelSize.value))

// ⭐ 自定义按钮点击：由我们自己控制折叠/展开，绕过组件内部状态
function toggleLeftPanel() {
  if (isLeftHidden.value) {
    // 展开：恢复到隐藏前缓存的尺寸
    leftPanelSize.value = cachedSize.value || '30%'
  } else {
    // 折叠：先缓存当前尺寸，再置零
    cachedSize.value = leftPanelSize.value
    leftPanelSize.value = 0
  }
}

// 监听尺寸变化，同步缓存（用于拖拽改变尺寸的场景）
function handleSizeChange(val) {
  // 只在非零时更新缓存，避免把 0 覆盖进去
  if (!isZero(val)) {
    cachedSize.value = val
  }
}

/* ---------------- 初始化 ---------------- */
onMounted(() => {
  loadPreference()
  // 刷新后只恢复左侧面板状态，不自动全屏（浏览器限制且体验不佳）
  if (preference.saved) {
    applyLeftPanel()
  }
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
}

/* ---------- 顶部栏 ---------- */
.toolbar {
  flex-shrink: 0;
  height: 60px;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  font-size: 22px;
  color: #409eff;
}

.brand-text {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5px;
}

.actions {
  display: flex;
  gap: 12px;
}

/* ---------- 主体 ---------- */
.body {
  flex: 1;
  padding: 16px;
  overflow: hidden;
}

.panel {
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  background: #fff;
  border-radius: 12px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.panel-desc {
  font-size: 13px;
  color: #909399;
}

/* ---------- 弹窗 ---------- */
.dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dialog-icon {
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  background: linear-gradient(135deg, #409eff, #79bbff);
}

.dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dialog-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #fff;
  border: 1.5px solid #ebeef5;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-card:hover {
  border-color: #c6e2ff;
  background: #f5faff;
}

.option-card.is-active {
  border-color: #409eff;
  background: #ecf5ff;
}

.option-icon {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #606266;
  background: #f0f2f5;
  transition: all 0.2s ease;
}

.option-card.is-active .option-icon {
  color: #fff;
  background: #409eff;
}

.option-content {
  flex: 1;
  min-width: 0;
}

.option-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.dialog-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 14px;
  font-size: 12px;
  line-height: 1.6;
  color: #5b7ba6;
  background: #f4f8ff;
  border-radius: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
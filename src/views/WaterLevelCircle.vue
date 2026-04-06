<template>
  <canvas
    ref="canvasRef"
    :width="size"
    :height="size"
    :style="{
      width: `${size}px`,
      height: `${size}px`,
      display: 'block',
      margin: '0 auto',
      borderRadius: '50%',
      boxShadow: '0 6px 14px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.03)'
    }"
  ></canvas>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue';

const props = defineProps({
  planStock: {
    type: Number,
    default: 0,
    required: true
  },
  actualStock: {
    type: Number,
    default: 0,
    required: true
  }
});

const canvasRef = ref(null);
const size = 50;
const radius = 34;
const centerX = size / 2;
const centerY = size / 2;

// 计算百分比 (0~100)
const getPercentage = () => {
  const { planStock, actualStock } = props;
  if (!planStock || planStock <= 0) return 0;
  let percent = (actualStock / planStock) * 100;
  return Math.min(100, Math.max(0, percent));
};

// 获取格式化文本（整数）
const getPercentText = () => {
  return `${Math.round(getPercentage())}%`;
};

// 绘图核心函数
const draw = () => {
  const canvas = canvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  ctx.clearRect(0, 0, size, size);

  // 1. 背景圆：加深的蓝灰渐变
  const bgGradient = ctx.createLinearGradient(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
  bgGradient.addColorStop(0, '#e2e8f0');  // 稍深
  bgGradient.addColorStop(1, '#cbd5e6');  // 更深的蓝灰
  ctx.beginPath();
  ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
  ctx.fillStyle = bgGradient;
  ctx.fill();

  // 2. 水位填充（蓝色系）
  const percentDecimal = getPercentage() / 100;
  const fillHeight = size * percentDecimal;
  const fillStartY = size - fillHeight;

  ctx.save();
  ctx.beginPath();
  ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
  ctx.clip();

  // 水位渐变：亮蓝 → 深蓝
  const waterGradient = ctx.createLinearGradient(0, fillStartY, 0, fillStartY + fillHeight);
  waterGradient.addColorStop(0, '#74c0fc');
  waterGradient.addColorStop(0.4, '#3b82f6');
  waterGradient.addColorStop(1, '#1e40af');
  ctx.fillStyle = waterGradient;
  ctx.fillRect(0, fillStartY, size, fillHeight);

  // 水位顶部高光线
  if (fillHeight > 5 && percentDecimal > 0.05) {
    ctx.beginPath();
    ctx.moveTo(0, fillStartY);
    ctx.lineTo(size, fillStartY);
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)';
    ctx.lineWidth = 1.8;
    ctx.stroke();
  }

  ctx.restore();

  // 3. 圆形边框
  ctx.beginPath();
  ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
  ctx.lineWidth = 1.5;
  ctx.strokeStyle = '#b9cadf';
  ctx.stroke();

  // 4. 百分比文字：添加黑色描边 + 白色填充 + 阴影，确保在任何水位下都清晰
  const text = getPercentText();
  ctx.font = `13px "Segoe UI", "PingFang SC", "Microsoft YaHei", Roboto, "Helvetica Neue", sans-serif`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  
  // 先绘制黑色描边
  ctx.lineWidth = 1.5;
  ctx.strokeStyle = '#1e293b';
  ctx.shadowColor = 'transparent'; // 描边时不使用阴影避免模糊
  ctx.strokeText(text, centerX, centerY);
  
  // 再绘制白色填充，并加上阴影增强立体感
  ctx.fillStyle = '#ffffff';
  ctx.shadowColor = 'rgba(0, 0, 0, 0.5)';
  ctx.shadowBlur = 5;
  ctx.shadowOffsetX = 1;
  ctx.shadowOffsetY = 1;
  ctx.fillText(text, centerX, centerY);
  
  // 重置阴影
  ctx.shadowColor = 'transparent';
};

// 监听 props 变化重新绘制
watch(
  () => [props.planStock, props.actualStock],
  () => {
    nextTick(() => draw());
  },
  { deep: false }
);

onMounted(() => {
  draw();
});
</script>
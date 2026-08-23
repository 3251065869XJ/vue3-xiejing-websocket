<template>
  <el-select
    :model-value="modelValue"
    :placeholder="placeholder"
    :clearable="clearable"
    :multiple="multiple"
    :disabled="disabled"
    @update:model-value="handleChange"
  >
    <el-option
      v-for="item in options"
      :key="item.dictValue"
      :label="item.dictLabel"
      :value="item.dictValue"
    />
  </el-select>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getDictDataByType } from '@/api/dictData'

const props = defineProps({
  modelValue: [String, Number, Array], // 当前值，支持单选、多选
  dictType: {
    type: String,
    required: true // 必须传入字典类型
  },
  multiple: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  clearable: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const options = ref([])

// 加载字典数据
async function loadOptions() {
  if (props.dictType) {
    const res = await getDictDataByType(props.dictType)
    if (res.code === 200) {
      options.value = res.data.map(item => ({
        dictLabel: item.dictLabel,
        dictValue: item.dictValue
      }))
    }
  }
}

// 值变化时触发事件
function handleChange(val) {
  emit('update:modelValue', val)
  emit('change', val)
}

// 监听 dictType 变化，重新加载
watch(() => props.dictType, (newVal) => {
  if (newVal) {
    loadOptions()
  }
})

onMounted(() => {
  loadOptions()
})
</script>
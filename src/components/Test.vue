##### 事务

```
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>4.3.1</version> <!-- 请使用最新版本 -->
</dependency>
```

```
@Service
public class BusinessService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;

    @DSTransactional // 使用此注解管理多数据源事务
    public void doBusiness() {
        // 操作 db1 数据库
        userMapper.insertUser(...);
        
        // 操作 db2 数据库
        orderMapper.insertOrder(...);
        
        // 如果这里抛出 RuntimeException，上述所有操作都将回滚
    }
}
```



##### 头部

```
<template>
  <div class="page-header">
    <el-dropdown
      :disabled="isDropdownDisabled"
      trigger="click"
      @command="handleSelectArea"
    >
      <div class="dropdown-trigger">
        <span>{{ currentAreaName || '请选择区域' }}</span>
        <el-icon class="el-icon--right">
          <arrow-down />
        </el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item
            v-for="area in availableAreaList"
            :key="area.id"
            :command="area.id"
            :class="{ 'is-active': selectedAreaId === area.id }"
          >
            {{ area.name }}
          </el-dropdown-item>
          <el-dropdown-item v-if="availableAreaList.length === 0" disabled>
            暂无区域数据
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useAreaStore } from '@/store/area'

// 获取 store 实例
const userStore = useUserStore()
const areaStore = useAreaStore()

// 用户角色判断（假设 store 中有 isSuperAdmin 字段）
const isSuperAdmin = computed(() => userStore.isSuperAdmin)

// 区域数据
const goodsAreaList = computed(() => areaStore.goodsAreaList || [])
const managerAreaList = computed(() => areaStore.managerAreaList || [])

// 根据角色获取可选区域列表
const availableAreaList = computed(() => {
  if (isSuperAdmin.value) {
    return goodsAreaList.value
  } else {
    return managerAreaList.value
  }
})

// 当前选中的区域 ID
const selectedAreaId = ref(null)

// 下拉框是否禁用（无可选区域时禁用）
const isDropdownDisabled = computed(() => availableAreaList.value.length === 0)

// 当前选中区域的名称（用于显示）
const currentAreaName = computed(() => {
  if (!selectedAreaId.value) return ''
  const area = availableAreaList.value.find(item => item.id === selectedAreaId.value)
  return area ? area.name : ''
})

// 初始化选中区域（根据缓存、角色和区域列表）
const initSelectedArea = () => {
  const availableList = availableAreaList.value
  const cachedAreaId = localStorage.getItem('selected_area_id')

  let finalAreaId = null

  // 如果有缓存，检查缓存中的区域是否在当前可用列表中
  if (cachedAreaId) {
    const existsInAvailable = availableList.some(area => area.id === cachedAreaId)
    if (existsInAvailable) {
      finalAreaId = cachedAreaId
    }
  }

  // 如果缓存无效或不存在，则选中第一个可用区域（如果有）
  if (!finalAreaId && availableList.length > 0) {
    finalAreaId = availableList[0].id
  }

  // 更新选中的区域 ID
  selectedAreaId.value = finalAreaId

  // 将最终确定的区域持久化到缓存（保证缓存与当前显示一致）
  if (finalAreaId && cachedAreaId !== finalAreaId) {
    localStorage.setItem('selected_area_id', finalAreaId)
  } else if (!finalAreaId && cachedAreaId) {
    // 如果最终没有选中任何区域，清除缓存
    localStorage.removeItem('selected_area_id')
  }
}

// 监听可选区域列表的变化（等待数据加载完成后重新初始化）
let initialized = false
const stopWatch = watch(availableAreaList, (newList) => {
  // 当列表有数据或者明确为空（已完成加载）时进行初始化
  if (!initialized) {
    // 判断数据是否已加载完成：goodsAreaList 和 managerAreaList 都有明确值（非 undefined）
    // 可根据实际业务调整加载完成标志，这里简单判断列表不为 null 且已触发过至少一次更新
    if (newList !== undefined) {
      initSelectedArea()
      initialized = true
      stopWatch() // 初始化后停止监听，避免重复触发
    }
  }
}, { immediate: true })

// 处理区域选择
const handleSelectArea = (areaId) => {
  // 保存选中的区域到缓存
  localStorage.setItem('selected_area_id', areaId)
  // 全局刷新页面
  window.location.reload()
}

// 组件挂载时，如果数据已存在则直接初始化（作为 watch 的补充）
onMounted(() => {
  if (!initialized && availableAreaList.value !== undefined) {
    initSelectedArea()
    initialized = true
  }
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.dropdown-trigger {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.dropdown-trigger:hover {
  background-color: #f5f7fa;
}

.dropdown-trigger span {
  margin-right: 8px;
  font-size: 14px;
  color: #303133;
}

.el-dropdown-menu .is-active {
  background-color: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}
</style>
```



###### 附带说明

```
// store 示例（防止重复请求）
const useAreaStore = defineStore('area', {
  state: () => ({
    goodsAreaList: [],
    managerAreaList: [],
    _loaded: false
  }),
  actions: {
    async loadAreas() {
      if (this._loaded) return;
      const res = await api.getAreas();
      this.goodsAreaList = res.goodsAreas;
      this.managerAreaList = res.managerAreas;
      this._loaded = true;
    }
  }
});
```

```
const handleSelectArea = (areaId) => {
  if (selectedAreaId.value === areaId) return; // 没变化，不刷新
  localStorage.setItem('selected_area_id', areaId);
  window.location.reload();
};
```


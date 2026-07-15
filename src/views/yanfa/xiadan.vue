<template>
  <div>
    <!-- 可用库存列表 -->
    <el-table :data="availableStock" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" />
      <el-table-column prop="materialCode" label="物料编码" />
      <el-table-column prop="materialName" label="物料名称" />
      <el-table-column prop="currentQuantity" label="可领数量" />
      <el-table-column label="领用数量">
        <template #default="{ row }">
          <el-input-number v-model="row.willPickQty" :min="0" :max="row.currentQuantity" 
            @change="(val) => updateCart(row, val)" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 购物车预览 -->
    <el-card header="我的购物车" style="margin-top:20px">
      <div v-for="item in cartItems" :key="item.inboundItemId">
        {{ item.materialName }} x {{ item.quantity }}
      </div>
      <el-button type="primary" @click="submitOrder('PICK')">提交领用</el-button>
      <el-button type="warning" @click="submitOrder('BORROW')">提交借料</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { submitOrder } from '@/api/order';

const availableStock = ref([]); // 需从 /api/inventory/available 获取，并附加 willPickQty 字段
const cartItems = reactive([]);

const updateCart = (row, val) => {
  const exist = cartItems.find(i => i.inboundItemId === row.id);
  if (val > 0) {
    if (exist) exist.quantity = val;
    else cartItems.push({ inboundItemId: row.id, quantity: val, ...row });
  } else {
    // 移除
    const idx = cartItems.indexOf(exist);
    if (idx > -1) cartItems.splice(idx, 1);
  }
};

const submitOrder = async (orderType) => {
  if (cartItems.length === 0) return ElMessage.warning('购物车为空');
  await submitOrder({ warehouseId: selectedWarehouse.value, orderType, items: cartItems.map(i => ({ inboundItemId: i.inboundItemId, quantity: i.quantity })) });
  ElMessage.success('下单成功');
  // 清空购物车、刷新库存
};
</script>
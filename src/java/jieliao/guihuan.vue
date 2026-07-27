<template>
  <div>
    <el-form inline>
      <el-form-item label="借料单号">
        <el-input v-model="searchOrderNo" placeholder="输入单号" />
      </el-form-item>
      <el-form-item>
        <el-button @click="loadOrder">查询</el-button>
      </el-form-item>
    </el-form>

    <div v-if="order">
      <h4>借料单 {{ order.orderNo }} (状态: {{ order.status }})</h4>
      <el-table :data="details" border>
        <el-table-column prop="materialCode" label="物料编码" />
        <el-table-column prop="materialName" label="物料名称" />
        <el-table-column prop="actualQty" label="已借数量" />
        <el-table-column prop="returnedQty" label="已归还" />
        <el-table-column label="本次归还">
          <template #default="{ row }">
            <el-input-number v-model="row._returnQty" :min="0" :max="row.actualQty - row.returnedQty" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="doReturn(row)">归还</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const searchOrderNo = ref('');
const order = ref(null);
const details = ref([]);

const loadOrder = async () => {
  const res = await axios.get('/api/borrow/details', { params: { orderNo: searchOrderNo.value } });
  order.value = res.data.order;
  details.value = res.data.details.map(d => ({ ...d, _returnQty: 0 }));
};

const doReturn = async (row) => {
  if (row._returnQty <= 0) return;
  await axios.put('/api/borrow/return', {
    borrowOrderId: order.value.id,
    materialCode: row.materialCode,
    returnQty: row._returnQty
  });
  ElMessage.success('归还成功');
  loadOrder(); // 刷新
};
</script>
<template>
  <div class="inbound-container">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="入库库房" prop="warehouseId">
        <el-select v-model="form.warehouseId" placeholder="请选择库房">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="联络单号" prop="contactNumber">
        <el-input v-model="form.contactNumber" placeholder="唯一联络单号" />
      </el-form-item>
      <el-form-item label="入库日期" prop="inboundDate">
        <el-date-picker v-model="form.inboundDate" type="date" value-format="YYYY-MM-DD" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" />
      </el-form-item>

      <!-- 物料明细表格（支持动态添加行） -->
      <el-table :data="form.items" border>
        <el-table-column type="index" width="50" />
        <el-table-column label="物料编码" prop="materialCode">
          <template #default="{ row }">
            <el-input v-model="row.materialCode" placeholder="必填" />
          </template>
        </el-table-column>
        <el-table-column label="物料名称" prop="materialName">
          <template #default="{ row }">
            <el-input v-model="row.materialName" placeholder="必填" />
          </template>
        </el-table-column>
        <el-table-column label="物料备注" prop="materialRemark">
          <template #default="{ row }">
            <el-input v-model="row.materialRemark" />
          </template>
        </el-table-column>
        <el-table-column label="入库数量" prop="quantity">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" />
          </template>
        </el-table-column>
        <el-table-column label="归还日期" prop="returnDate">
          <template #default="{ row }">
            <el-date-picker v-model="row.returnDate" type="date" value-format="YYYY-MM-DD" />
          </template>
        </el-table-column>
        <el-table-column label="责任人工段长" prop="responsibleForeman">
          <template #default="{ row }">
            <el-input v-model="row.responsibleForeman" />
          </template>
        </el-table-column>
        <el-table-column label="责任研发" prop="responsibleRd">
          <template #default="{ row }">
            <el-input v-model="row.responsibleRd" />
          </template>
        </el-table-column>
        <el-table-column label="货位号" prop="locationCode">
          <template #default="{ row }">
            <el-input v-model="row.locationCode" />
          </template>
        </el-table-column>
        <el-table-column label="带出状态" prop="bringOutStatus">
          <template #default="{ row }">
            <el-select v-model="row.bringOutStatus" placeholder="请选择">
              <el-option label="待带出" value="待带出" />
              <el-option label="不带出" value="不带出" />
              <el-option label="已带出" value="已带出" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ $index }">
            <el-button type="danger" @click="removeItem($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="primary" @click="addItem">添加物料明细</el-button>

      <el-form-item style="margin-top: 20px">
        <el-button type="primary" @click="submitInbound">提交入库</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { createInbound } from '@/api/inbound';

// 库房列表需从后端获取
const warehouses = ref([]); // 自行请求

const formRef = ref(null);
const form = reactive({
  warehouseId: null,
  contactNumber: '',
  inboundDate: '',
  remark: '',
  items: []
});

const rules = {
  warehouseId: [{ required: true, message: '请选择库房' }],
  contactNumber: [{ required: true, message: '请输入联络单号' }],
  inboundDate: [{ required: true, message: '请选择入库日期' }]
};

// 添加一行物料
const addItem = () => {
  form.items.push({
    materialCode: '',
    materialName: '',
    materialRemark: '',
    quantity: 1,
    returnDate: '',
    responsibleForeman: '',
    responsibleRd: '',
    locationCode: '',
    bringOutStatus: '不带出'
  });
};

// 删除一行
const removeItem = (index) => {
  form.items.splice(index, 1);
};

// 提交
const submitInbound = async () => {
  if (!formRef.value) return;
  await formRef.value.validate();
  if (form.items.length === 0) {
    ElMessage.warning('请至少添加一条物料明细');
    return;
  }
  try {
    await createInbound(form);
    ElMessage.success('入库成功');
    // 重置表单...
  } catch (e) {
    ElMessage.error(e.message);
  }
};
const submitInbound = async () => {
  // 校验...
  const payload = {
    contactNumber: form.contactNumber,
    warehouseId: form.warehouseId,
    inboundDate: form.inboundDate,
    remark: form.remark,
    items: form.items.map(item => ({
      materialCode: item.materialCode,
      materialName: item.materialName,
      materialRemark: item.materialRemark,
      quantity: item.quantity,
      returnDate: item.returnDate,
      responsibleForeman: item.responsibleForeman,
      responsibleRd: item.responsibleRd,
      locationCode: item.locationCode,
      bringOutStatus: item.bringOutStatus
    }))
  };
  await createInbound(payload);
  // ...
};
</script>
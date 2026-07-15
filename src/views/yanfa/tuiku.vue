<template>
  <div>
    <el-form :model="form" label-width="140px">
      <el-form-item label="入库联络单号">
        <el-input v-model="form.contactNumber" placeholder="输入联络单号" />
      </el-form-item>
      <el-form-item label="物料编码">
        <el-input v-model="form.materialCode" placeholder="输入物料编码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="queryLinkage">查询联动信息</el-button>
      </el-form-item>
    </el-form>

    <!-- 联动展示信息 -->
    <el-descriptions v-if="linkage" :column="2" border>
      <el-descriptions-item label="库房">{{ linkage.warehouseName }}</el-descriptions-item>
      <el-descriptions-item label="领用人">{{ linkage.borrower }}</el-descriptions-item>
      <el-descriptions-item label="领用数量">{{ linkage.borrowedQuantity }}</el-descriptions-item>
      <el-descriptions-item label="物料名称">{{ linkage.materialName }}</el-descriptions-item>
    </el-descriptions>

    <el-form v-if="linkage" label-width="140px" style="margin-top:20px">
      <el-form-item label="退库数量" required>
        <el-input-number v-model="form.returnQuantity" :min="1" :max="linkage.borrowedQuantity" />
      </el-form-item>
      <el-form-item>
        <el-button type="danger" @click="doReturn">确认退库</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { getLinkage, doReturn } from '@/api/return';

const form = reactive({ contactNumber: '', materialCode: '', returnQuantity: 0 });
const linkage = ref(null);

const queryLinkage = async () => {
  const res = await getLinkage(form.contactNumber, form.materialCode);
  linkage.value = res.data;
};

const doReturnHandle = async () => {
  await doReturn({ ...form });
  ElMessage.success('退库成功');
  linkage.value = null;
};
</script>
<template>
  <div class="container">
    <!-- 顶部左右容器包裹层（占80%高度） -->
    <div class="top-wrap">
      <!-- 左侧容器 -->
      <div 
        class="left-container"
        :class="{ 'left-container--shrink': isRightShow }"
      >
        <div class="left-child-container1">
          <el-form :inline="true" class="card-form">
            <el-form-item label="USB刷卡输入：">
              <el-input
                v-model="leftCardInput"
                placeholder="请使用USB设备刷卡读取数据"
                class="card-input"
                @input="handleLeftCardInput"
                show-overflow-tooltip
              ></el-input>
            </el-form-item>
            <el-form-item>
              <span class="tips-text">提示：刷卡后数据将自动回显并清空输入框</span>
            </el-form-item>
          </el-form>
        </div>
        <div class="left-child-container2">
          <el-table
            :data="displayLeftData"
            border
            stripe
            @row-click="handleRowClick"
            class="info-table"
            highlight-current-row
            fit
          >
            <el-table-column
              type="index"
              label="序号"
              width="60"
              align="center"
            ></el-table-column>
            <el-table-column
              prop="receiveOrderNo"
              label="领料单号"
              align="center"
              class-name="highlight-column"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="receiver"
              label="领料人"
              align="center"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="dept"
              label="领料部门"
              align="center"
              show-overflow-tooltip
              :min-width="100"
            ></el-table-column>
            <el-table-column
              prop="warehouse"
              label="领料库房"
              align="center"
              show-overflow-tooltip
              :min-width="100"
            ></el-table-column>
            <el-table-column
              prop="category"
              label="种类"
              align="center"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="totalCount"
              label="总数"
              align="center"
              width="80"
            ></el-table-column>
            <el-table-column
              prop="updateTime"
              label="最后更新时间"
              align="center"
              :min-width="160"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              label="操作"
              align="center"
              width="120"
            >
              <template #default="scope">
                <el-button
                  link
                  size="small"
                  @click="handleViewDetail(scope.row)"
                >查看详情</el-button>
                <el-button
                  type="danger"
                  size="small"
                  link
                  @click="handleSubmit(scope.row)"
                >提交</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            class="pagination"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="leftTotal"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50]"
            :page-size="pageSize"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </div>

      <!-- 右侧容器（默认隐藏） -->
      <div 
        class="right-container"
        v-show="isRightShow"
        :class="{ 'right-container--show': isRightShow }"
      >
        <div class="right-child-container1">
          <el-form :inline="true" class="card-form">
            <el-form-item label="USB刷卡输入：">
              <el-input
                v-model="rightCardInput"
                placeholder="请使用USB设备刷卡读取数据"
                class="card-input"
                @input="handleRightCardInput"
                show-overflow-tooltip
              ></el-input>
            </el-form-item>
            <el-form-item>
              <span class="tips-text">提示：刷卡后数据将自动回显并清空输入框</span>
            </el-form-item>
          </el-form>
        </div>
        <div class="right-child-container2">
          <!-- 关闭按钮 -->
          <el-button
            link
            class="close-right-btn"
            @click="closeRightContainer"
          >
            <el-icon><Close /></el-icon>
            关闭详情面板
          </el-button>
          
          <el-table
            :data="displayRightData"
            border
            stripe
            class="info-table"
            fit
          >
            <el-table-column
              type="index"
              label="序号"
              width="60"
              align="center"
            ></el-table-column>
            <el-table-column
              prop="goodsType"
              label="货物类型"
              align="center"
              show-overflow-tooltip
            ></el-table-column>
            <el-table-column
              prop="goodsName"
              label="货物名称"
              align="center"
              show-overflow-tooltip
              :min-width="120"
            ></el-table-column>
            <el-table-column
              prop="reserveCount"
              label="预约数量"
              align="center"
              width="80"
            ></el-table-column>
          </el-table>
          <el-pagination
            class="pagination"
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="rightTotal"
            :current-page="rightCurrentPage"
            :page-sizes="[10, 20, 50]"
            :page-size="rightPageSize"
            @size-change="handleRightSizeChange"
            @current-change="handleRightCurrentChange"
          ></el-pagination>
        </div>
      </div>
    </div>

    <!-- 底部容器（占10%高度） -->
    <div class="bottom-container">
      <div class="bottom-title">已提交领料信息：</div>
      <div class="submit-list" v-if="submitList.length > 0">
        <span v-for="(item, index) in submitList" :key="index" class="submit-item">
          {{ item.receiver }} - {{ item.goodsNames }}
          <span v-if="index !== submitList.length - 1" class="separator">|</span>
        </span>
      </div>
      <div class="empty-tips" v-else>暂无已提交的领料信息</div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

export default {
  name: 'ReceiveMaterialManager',
  components: {
    Close
  },
  data() {
    return {
      // 右侧面板显示状态
      isRightShow: false,
      // 左侧刷卡输入框值
      leftCardInput: '',
      // 右侧刷卡输入框值
      rightCardInput: '',
      // 左侧完整数据源（模拟数据库全部数据）
      fullLeftData: [],
      // 左侧当前页显示数据
      displayLeftData: [],
      // 左侧分页参数
      currentPage: 1,
      pageSize: 10,
      leftTotal: 0,
      
      // 右侧完整数据源（当前选中的领料单对应的明细数据）
      fullRightData: [],
      // 右侧当前页显示数据
      displayRightData: [],
      // 右侧分页参数
      rightCurrentPage: 1,
      rightPageSize: 10,
      rightTotal: 0,
      
      // 已提交列表
      submitList: []
    };
  },
  created() {
    // 初始化左侧完整数据
    this.initLeftFullData();
    // 更新左侧分页展示
    this.updateLeftDisplay();
  },
  methods: {
    /**
     * 初始化左侧完整数据（模拟20条领料单）
     */
    initLeftFullData() {
      this.fullLeftData = Array.from({ length: 20 }, (_, index) => ({
        receiveOrderNo: `RK${20260001 + index}`,
        receiver: `张三${index + 1}`,
        dept: `生产部${index % 5 + 1}`,
        warehouse: index % 3 === 0 ? '一号库房' : index % 3 === 1 ? '二号库房' : '三号库房',
        category: index % 4 === 0 ? '电子元件' : index % 4 === 1 ? '五金工具' : index % 4 === 2 ? '办公耗材' : '生产原料',
        totalCount: 10 + index * 2,
        updateTime: `2026-01-0${Math.floor(index / 4) + 1} ${10 + index % 12}:${30 + index % 30}`
      }));
      this.leftTotal = this.fullLeftData.length;
    },

    /**
     * 更新左侧表格分页显示
     */
    updateLeftDisplay() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      this.displayLeftData = this.fullLeftData.slice(start, end);
    },

    /**
     * 根据领料单号生成右侧明细数据（模拟商品明细）
     * @param {String} receiveOrderNo 领料单号
     * @returns {Array} 明细数据数组
     */
    generateRightDataByOrder(receiveOrderNo) {
      // 根据单号后缀生成不同种类的明细，使数据更真实
      const orderNum = parseInt(receiveOrderNo.slice(-4)) || 1;
      const count = 5 + (orderNum % 6); // 每个单明细数量5-10条
      return Array.from({ length: count }, (_, idx) => {
        const typeIdx = (orderNum + idx) % 3;
        let goodsType = '';
        if (typeIdx === 0) goodsType = '电子类';
        else if (typeIdx === 1) goodsType = '五金类';
        else goodsType = '办公类';
        
        return {
          goodsType: goodsType,
          goodsName: `${receiveOrderNo}-商品${idx + 1}`,
          reserveCount: 1 + (idx % 5)
        };
      });
    },

    /**
     * 更新右侧表格分页显示
     */
    updateRightDisplay() {
      const start = (this.rightCurrentPage - 1) * this.rightPageSize;
      const end = start + this.rightPageSize;
      this.displayRightData = this.fullRightData.slice(start, end);
      this.rightTotal = this.fullRightData.length;
    },

    /**
     * 左侧表格行点击事件（显示右侧面板并联动数据）
     * @param {Object} row 点击的行数据
     */
    handleRowClick(row) {
      // 根据当前领料单生成明细数据
      this.fullRightData = this.generateRightDataByOrder(row.receiveOrderNo);
      // 重置右侧分页为第一页
      this.rightCurrentPage = 1;
      this.updateRightDisplay();
      // 显示右侧容器
      this.isRightShow = true;
    },

    /**
     * 关闭右侧面板
     */
    closeRightContainer() {
      this.isRightShow = false;
      // 可选：清空右侧数据释放内存
      this.fullRightData = [];
      this.displayRightData = [];
      this.rightTotal = 0;
    },

    /**
     * 左侧刷卡输入处理
     */
    handleLeftCardInput() {
      if (this.leftCardInput) {
        // 模拟刷卡成功：延迟300ms后清空输入框
        setTimeout(() => {
          this.leftCardInput = '';
          ElMessage.success('左侧刷卡数据读取成功！');
        }, 300);
      }
    },

    /**
     * 右侧刷卡输入处理
     */
    handleRightCardInput() {
      if (this.rightCardInput) {
        setTimeout(() => {
          this.rightCardInput = '';
          ElMessage.success('右侧刷卡数据读取成功！');
        }, 300);
      }
    },

    /**
     * 查看详情（与行点击效果一致）
     * @param {Object} row 行数据
     */
    handleViewDetail(row) {
      this.handleRowClick(row);
    },

    /**
     * 提交领料信息
     * @param {Object} row 行数据
     */
    handleSubmit(row) {
      // 获取当前右侧明细的前三个商品名称（如果不足三个则取全部）
      const goodsNames = this.displayRightData.slice(0, 3).map(item => item.goodsName).join('、');
      this.submitList.push({
        receiver: row.receiver,
        goodsNames: goodsNames || '无商品明细'
      });
      ElMessage.success('领料信息提交成功！');
    },

    /**
     * 左侧分页-每页条数改变
     * @param {Number} val 每页条数
     */
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1;
      this.updateLeftDisplay();
    },

    /**
     * 左侧分页-当前页改变
     * @param {Number} val 当前页
     */
    handleCurrentChange(val) {
      this.currentPage = val;
      this.updateLeftDisplay();
    },

    /**
     * 右侧分页-每页条数改变
     * @param {Number} val 每页条数
     */
    handleRightSizeChange(val) {
      this.rightPageSize = val;
      this.rightCurrentPage = 1;
      this.updateRightDisplay();
    },

    /**
     * 右侧分页-当前页改变
     * @param {Number} val 当前页
     */
    handleRightCurrentChange(val) {
      this.rightCurrentPage = val;
      this.updateRightDisplay();
    }
  }
};
</script>

<style scoped>
/* 全局容器 - 撑满整个屏幕，自适应所有设备 */
.container {
  width: 100vw;
  height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
  padding: clamp(8px, 2vw, 16px);
  min-width: 320px;
}

/* 顶部包裹层 - 占80%高度，弹性布局适配间距 */
.top-wrap {
  height: 80%;
  width: 100%;
  display: flex;
  justify-content: space-between;
  gap: clamp(1%, 2vw, 2%);
  margin-bottom: auto;
  box-sizing: border-box;
  flex-wrap: nowrap;
}

/* 左侧容器 - 默认占满，切换时收缩为48% */
.left-container {
  height: 100%;
  width: 100%;
  background-color: #ffffff;
  border-radius: clamp(6px, 1.5vw, 8px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  padding: clamp(12px, 2vw, 16px);
  box-sizing: border-box;
  transition: width 0.3s ease-in-out;
  min-width: 280px;
}

.left-container--shrink {
  width: 48%;
}

/* 右侧容器 - 默认隐藏，显示时为48% */
.right-container {
  height: 100%;
  width: 0;
  background-color: #ffffff;
  border-radius: clamp(6px, 1.5vw, 8px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  padding: clamp(12px, 2vw, 16px);
  box-sizing: border-box;
  transition: width 0.3s ease-in-out;
  overflow: hidden;
  min-width: 280px;
}

.right-container--show {
  width: 48%;
}

/* 左侧子容器1 - 固定高度80px */
.left-child-container1 {
  height: 80px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: clamp(12px, 1.5vw, 16px);
  box-sizing: border-box;
  flex-shrink: 0;
}

/* 左侧子容器2 - flex:1 占满剩余高度 */
.left-child-container2 {
  flex: 1;
  margin-top: clamp(12px, 1.5vw, 16px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 右侧子容器1 - 固定高度80px */
.right-child-container1 {
  height: 80px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: clamp(12px, 1.5vw, 16px);
  box-sizing: border-box;
  flex-shrink: 0;
}

/* 右侧子容器2 - flex:1 占满剩余高度 */
.right-child-container2 {
  flex: 1;
  margin-top: clamp(12px, 1.5vw, 16px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 底部容器 - 占10%高度 */
.bottom-container {
  height: 10%;
  width: 100%;
  background-color: #ffffff;
  border-radius: clamp(6px, 1.5vw, 8px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
  margin-top: clamp(8px, 1vw, 10px);
  padding: 0 clamp(12px, 2vw, 16px);
  display: flex;
  align-items: center;
  box-sizing: border-box;
  overflow: hidden;
  min-height: 50px;
}

/* 刷卡表单样式 */
.card-form {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: clamp(8px, 1vw, 16px);
}

/* 刷卡输入框 */
.card-input {
  min-width: 200px;
  max-width: 400px;
  flex: 1;
}

/* 提示文本 */
.tips-text {
  color: #606266;
  font-size: clamp(12px, 1vw, 14px);
  white-space: normal;
  flex-shrink: 0;
}

/* 表格样式 */
.info-table {
  flex: 1;
  margin-bottom: clamp(12px, 1.5vw, 16px);
  overflow-x: auto;
  overflow-y: auto;
  font-size: clamp(12px, 1vw, 14px);
}

/* 领料单号高亮列 - 使用 :deep 深度选择器 */
:deep(.highlight-column) {
  background-color: #e6f7ff !important;
  color: #1890ff !important;
  font-weight: 500;
}

/* 分页样式 */
.pagination {
  align-self: flex-end;
  font-size: clamp(12px, 1vw, 14px);
}

/* 右侧关闭按钮 */
.close-right-btn {
  align-self: flex-end;
  margin-bottom: clamp(6px, 1vw, 8px);
  color: #606266;
  font-size: clamp(12px, 1vw, 14px);
  transition: color 0.2s ease;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.close-right-btn:hover {
  color: #f56c6c;
}

/* 底部样式 */
.bottom-title {
  font-weight: 500;
  color: #303133;
  margin-right: clamp(12px, 1.5vw, 16px);
  font-size: clamp(13px, 1vw, 14px);
  white-space: nowrap;
}

.submit-list {
  display: flex;
  flex-wrap: nowrap;
  overflow-x: auto;
  height: 80%;
  align-items: center;
  gap: clamp(8px, 1vw, 16px);
}

.submit-item {
  color: #409eff;
  font-size: clamp(12px, 1vw, 14px);
  white-space: nowrap;
}

.separator {
  margin: 0 clamp(4px, 0.5vw, 8px);
  color: #ebeef5;
}

.empty-tips {
  color: #909399;
  font-size: clamp(12px, 1vw, 14px);
}

/* 滚动条优化 */
::-webkit-scrollbar {
  width: clamp(4px, 1vw, 6px);
  height: clamp(4px, 1vw, 6px);
}

::-webkit-scrollbar-thumb {
  border-radius: clamp(2px, 0.5vw, 3px);
  background-color: #dcdfe6;
}

::-webkit-scrollbar-track {
  background-color: #f5f7fa;
}

/* 媒体查询 - 中等屏幕（平板，≤1200px） */
@media (max-width: 1200px) {
  .left-container--shrink,
  .right-container--show {
    width: 49%;
  }
  
  .card-input {
    max-width: 350px;
  }
}

/* 媒体查询 - 小屏幕（手机，≤768px） */
@media (max-width: 768px) {
  .top-wrap {
    flex-direction: column;
    gap: clamp(8px, 1vw, 10px);
  }
  
  .left-container,
  .left-container--shrink {
    width: 100%;
    height: 49%;
  }
  
  .right-container--show {
    width: 100%;
    height: 49%;
  }
  
  /* 小屏隐藏非核心表格列 */
  :deep(.el-table__body .el-table__cell:has([prop="warehouse"])) {
    display: none !important;
  }
  :deep(.el-table__header .el-table__cell:has([prop="warehouse"])) {
    display: none !important;
  }
  :deep(.el-table__body .el-table__cell:has([prop="updateTime"])) {
    display: none !important;
  }
  :deep(.el-table__header .el-table__cell:has([prop="updateTime"])) {
    display: none !important;
  }
  
  .bottom-container {
    flex-wrap: wrap;
    align-items: flex-start;
    padding-top: clamp(8px, 1vw, 10px);
  }
  
  .bottom-title {
    margin-bottom: clamp(4px, 0.5vw, 6px);
    width: 100%;
  }
}
</style>
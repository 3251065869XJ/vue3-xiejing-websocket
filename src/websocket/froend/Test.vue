<!-- src/components/DataMonitor.vue -->
<template>
  <div class="data-monitor">
    <div class="status">
      连接状态：
      <span :class="isConnected ? 'connected' : 'disconnected'">
        {{ isConnected ? '已连接' : '未连接' }}
      </span>
    </div>

    <div class="user-info">
      <p>当前用户：{{ userAccount }}</p>
      <p>监控区域：{{ areaId }}</p>
    </div>

    <div class="actions">
      <button @click="handleConnect" :disabled="isConnected">连接 WebSocket</button>
      <button @click="handleDisconnect" :disabled="!isConnected">断开连接</button>
    </div>

    <div class="messages" v-if="messageList.length > 0">
      <h3>更新通知记录</h3>
      <div v-for="(msg, index) in messageList" :key="index" class="message-item">
        <p>{{ msg.message }}</p>
        <small>最新更新时间：{{ msg.latestUpdateTime }}</small>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';
import { 
  connectWebSocket, 
  disconnectWebSocket, 
  getConnectionStatus,
  getMessages 
} from '@/services/websocketService';

// 参数配置
const userAccount = ref('工号_001');
const areaId = ref('区域_A');

const isConnected = ref(false);
const messageList = ref([]);

// 连接状态轮询
const statusCheckInterval = setInterval(() => {
  isConnected.value = getConnectionStatus().value;
}, 100);

/**
 * 处理接收到的消息
 */
const handleMessage = (data) => {
  console.log('收到更新通知:', data);
  messageList.value = [data, ...messageList.value.slice(0, 9)]; // 保留最近 10 条
  
  // 这里可以触发页面刷新或弹窗提示
  // 例如: refreshData();
};

/**
 * 连接 WebSocket
 */
const handleConnect = () => {
  connectWebSocket(userAccount.value, areaId.value, handleMessage);
};

/**
 * 断开连接
 */
const handleDisconnect = () => {
  disconnectWebSocket();
};

// 组件卸载时清理
onUnmounted(() => {
  clearInterval(statusCheckInterval);
  disconnectWebSocket();
});
</script>

<style scoped>
.data-monitor {
  padding: 20px;
  font-family: Arial, sans-serif;
}

.status {
  margin-bottom: 15px;
}

.connected {
  color: green;
  font-weight: bold;
}

.disconnected {
  color: red;
  font-weight: bold;
}

.actions {
  margin: 20px 0;
}

.actions button {
  margin-right: 10px;
  padding: 8px 16px;
  cursor: pointer;
}

.message-item {
  background: #f5f5f5;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
}
</style>
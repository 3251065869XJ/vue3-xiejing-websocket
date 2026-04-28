// src/services/websocketService.js
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { ref } from 'vue';

let stompClient = null;
const connected = ref(false);
const messages = ref([]);

// ---------- 重连相关配置 ----------
const MAX_RECONNECT_ATTEMPTS = 3;           // 最大重连次数
const RECONNECT_DELAY = 2000;               // 每次重连间隔（毫秒）
let reconnectAttempts = 0;                  // 当前重连次数
let intentionalClose = false;               // 是否主动断开
let reconnectTimer = null;                  // 重连定时器
// ----------------------------------

/**
 * 建立 WebSocket 连接
 * @param {string} userAccount 工号
 * @param {string} areaId 区域编号
 * @param {function} onMessageCallback 收到消息时的回调
 */
export function connectWebSocket(userAccount, areaId, onMessageCallback) {
    // 清除可能存在的旧重连定时器
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }

    // 如果已经主动断开，不再尝试连接
    if (intentionalClose) return;

    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({},
        () => {
            // ---------- 连接成功 ----------
            connected.value = true;
            reconnectAttempts = 0;               // 重置重连计数
            console.log('WebSocket 连接成功');

            // 订阅私有消息队列
            stompClient.subscribe(`/user/queue/updates`, (message) => {
                const data = JSON.parse(message.body);
                messages.value.push(data);
                if (onMessageCallback) {
                    onMessageCallback(data);
                }
            });

            // 注册用户
            stompClient.send('/app/register', {}, JSON.stringify({
                userAccount: userAccount,
                areaId: areaId
            }));
        },
        (error) => {
            // ---------- 连接失败或异常断开 ----------
            console.error('WebSocket 连接失败', error);
            connected.value = false;

            // 如果是主动断开，不进行重连
            if (intentionalClose) return;

            // 尝试重连
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++;
                console.log(`正在尝试第 ${reconnectAttempts} 次重连...`);
                reconnectTimer = setTimeout(() => {
                    connectWebSocket(userAccount, areaId, onMessageCallback);
                }, RECONNECT_DELAY);
            } else {
                console.log(`已达到最大重连次数 (${MAX_RECONNECT_ATTEMPTS})，停止重连`);
                // 可选：通知用户连接失败
                if (onMessageCallback) {
                    onMessageCallback({
                        type: 'CONNECTION_FAILED',
                        message: 'WebSocket 连接失败，请手动重试'
                    });
                }
            }
        }
    );
}

/**
 * 断开 WebSocket 连接（主动断开）
 */
export function disconnectWebSocket() {
    intentionalClose = true;               // 标记为主动断开
    reconnectAttempts = 0;                 // 重置计数
    if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
    }
    if (stompClient !== null) {
        stompClient.disconnect();
        connected.value = false;
        console.log('WebSocket 连接已主动断开');
    }
}

/**
 * 手动重连（主动调用，重置状态）
 */
export function manualReconnect(userAccount, areaId, onMessageCallback) {
    intentionalClose = false;              // 清除主动断开标记
    reconnectAttempts = 0;                 // 重置计数
    disconnectWebSocket();                 // 先断开旧连接
    connectWebSocket(userAccount, areaId, onMessageCallback);
}

export function getConnectionStatus() {
    return connected;
}

export function getMessages() {
    return messages;
}
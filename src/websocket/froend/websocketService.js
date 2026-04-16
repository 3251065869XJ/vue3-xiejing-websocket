// src/services/websocketService.js
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { ref } from 'vue';

let stompClient = null;
const connected = ref(false);
const messages = ref([]);

/**
 * 建立 WebSocket 连接
 */
export function connectWebSocket(userAccount, areaId, onMessageCallback) {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, 
        () => {
            connected.value = true;
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
            console.error('WebSocket 连接失败', error);
            connected.value = false;
        }
    );
}

/**
 * 断开 WebSocket 连接
 */
export function disconnectWebSocket() {
    if (stompClient !== null) {
        stompClient.disconnect();
        connected.value = false;
        console.log('WebSocket 连接已断开');
    }
}

export function getConnectionStatus() {
    return connected;
}

export function getMessages() {
    return messages;
}
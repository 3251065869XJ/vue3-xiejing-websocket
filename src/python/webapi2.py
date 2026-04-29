import threading
import time
import requests
import os
from flask import Flask, request, jsonify

app = Flask(__name__)

# 模拟数据
data_store = []

@app.route('/api/hello', methods=['GET'])
def hello():
    return jsonify({"message": "Hello from self-calling API"})

@app.route('/api/data', methods=['POST'])
def add_data():
    content = request.get_json()
    data_store.append(content)
    return jsonify({"code": 201, "data": content})

@app.route('/api/data', methods=['GET'])
def get_data():
    return jsonify({"code": 200, "data": data_store})

def run_flask():
    """在子线程中运行 Flask 服务"""
    # 注意：debug=False, use_reloader=False 避免线程冲突
    app.run(host='127.0.0.1', port=5000, debug=False, use_reloader=False)

def self_call_example():
    """等待服务完全启动后，调用自己的 API"""
    time.sleep(2)  # 等待服务启动
    try:
        # 调用 GET 接口
        resp = requests.get("http://127.0.0.1:5000/api/hello")
        print("自己调用 GET /api/hello 结果：", resp.json())

        # 调用 POST 接口
        resp2 = requests.post("http://127.0.0.1:5000/api/data", json={"value": "test"})
        print("自己调用 POST /api/data 结果：", resp2.json())

        # 再次 GET 查看数据
        resp3 = requests.get("http://127.0.0.1:5000/api/data")
        print("自己调用 GET /api/data 结果：", resp3.json())
    except Exception as e:
        print("调用失败：", e)

if __name__ == '__main__':
    # 启动服务线程（daemon=True 使得主线程结束时自动退出）
    server_thread = threading.Thread(target=run_flask, daemon=True)
    server_thread.start()

    # 进行自己调用自己的演示
    self_call_example()

    # 保持主线程存活，让服务继续运行（否则 daemon 线程会退出）
    print("Web API 服务已在后台运行，按 Ctrl+C 退出...")
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("服务已停止")
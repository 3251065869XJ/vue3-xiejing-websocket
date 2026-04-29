# my_api.py
from flask import Flask, request, jsonify
import sys
import os

app = Flask(__name__)

# 简单内存存储
todo_list = []

@app.route('/api/todos', methods=['GET'])
def get_todos():
    return jsonify({"code": 200, "data": todo_list})

@app.route('/api/todos', methods=['POST'])
def add_todo():
    data = request.get_json()
    if not data or 'task' not in data:
        return jsonify({"code": 400, "message": "缺少 task 字段"}), 400
    new_id = len(todo_list) + 1
    new_todo = {"id": new_id, "task": data['task'], "done": False}
    todo_list.append(new_todo)
    return jsonify({"code": 201, "data": new_todo}), 201

@app.route('/api/todos/<int:todo_id>', methods=['PUT'])
def update_todo(todo_id):
    data = request.get_json()
    for todo in todo_list:
        if todo['id'] == todo_id:
            if 'task' in data:
                todo['task'] = data['task']
            if 'done' in data:
                todo['done'] = data['done']
            return jsonify({"code": 200, "data": todo})
    return jsonify({"code": 404, "message": "未找到"}), 404

@app.route('/api/todos/<int:todo_id>', methods=['DELETE'])
def delete_todo(todo_id):
    for i, todo in enumerate(todo_list):
        if todo['id'] == todo_id:
            deleted = todo_list.pop(i)
            return jsonify({"code": 200, "data": deleted})
    return jsonify({"code": 404, "message": "未找到"}), 404

if __name__ == '__main__':
    # 从环境变量读取端口（方便打包后修改），默认 5000
    port = int(os.environ.get('API_PORT', 5000))
    # 启动服务
    # host='0.0.0.0' 允许本机其他程序访问，只本地调用也可用 '127.0.0.1'
    app.run(host='0.0.0.0', port=port, debug=False)


    pyinstaller --onefile --console my_api.py
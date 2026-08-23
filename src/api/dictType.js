import request from '@/utils/request'

// 分页查询字典类型
export function pageDictType(params) {
  return request({
    url: '/dict/type/page',
    method: 'get',
    params
  })
}

// 获取字典类型详情
export function getDictType(id) {
  return request({
    url: `/dict/type/${id}`,
    method: 'get'
  })
}

// 新增字典类型
export function addDictType(data) {
  return request({
    url: '/dict/type',
    method: 'post',
    data
  })
}

// 修改字典类型
export function updateDictType(data) {
  return request({
    url: '/dict/type',
    method: 'put',
    data
  })
}

// 批量删除字典类型（ids 为数组）
export function deleteDictType(ids) {
  return request({
    url: `/dict/type/${ids.join(',')}`,
    method: 'delete'
  })
}
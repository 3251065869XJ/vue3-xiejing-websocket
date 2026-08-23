import request from '@/utils/request'

// 分页查询字典数据
export function pageDictData(params) {
  return request({
    url: '/dict/data/page',
    method: 'get',
    params
  })
}

// 根据类型获取启用字典数据（下拉用）
export function getDictDataByType(dictType) {
  return request({
    url: `/dict/data/type/${dictType}`,
    method: 'get'
  })
}

// 获取字典数据详情
export function getDictData(id) {
  return request({
    url: `/dict/data/${id}`,
    method: 'get'
  })
}

// 新增字典数据
export function addDictData(data) {
  return request({
    url: '/dict/data',
    method: 'post',
    data
  })
}

// 修改字典数据
export function updateDictData(data) {
  return request({
    url: '/dict/data',
    method: 'put',
    data
  })
}

// 批量删除字典数据
export function deleteDictData(ids) {
  return request({
    url: `/dict/data/${ids.join(',')}`,
    method: 'delete'
  })
}
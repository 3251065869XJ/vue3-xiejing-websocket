package com.example.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.material.entity.InboundItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InboundItemMapper extends BaseMapper<InboundItem> {
    // 可以定义复合查询方法，例如按库房、状态、物料编码等
}
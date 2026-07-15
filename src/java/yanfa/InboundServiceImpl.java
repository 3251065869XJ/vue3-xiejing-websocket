package com.example.material.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.material.dto.InboundDTO;
import com.example.material.entity.InboundItem;
import com.example.material.exception.BusinessException;
import com.example.material.mapper.InboundItemMapper;
import com.example.material.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InboundServiceImpl implements InboundService {

    private final InboundItemMapper inboundItemMapper;

    @Override
    @Transactional
    public void createInbound(InboundDTO dto, Long operatorId) {
        // 1. 校验联络单号是否已存在（保证该号码此前未使用过）
        Long count = inboundItemMapper.selectCount(
                new LambdaQueryWrapper<InboundItem>()
                        .eq(InboundItem::getContactNumber, dto.getContactNumber())
        );
        if (count > 0) {
            throw new BusinessException("联络单号[" + dto.getContactNumber() + "]已存在，请勿重复提交");
        }

        // 2. 将公共字段与每条物料明细组合，构建入库明细实体列表
        List<InboundItem> itemList = new ArrayList<>();
        for (InboundDTO.MaterialItem material : dto.getItems()) {
            InboundItem item = new InboundItem();
            // 复制公共字段
            item.setContactNumber(dto.getContactNumber());
            item.setWarehouseId(dto.getWarehouseId());
            item.setInboundDate(dto.getInboundDate());
            item.setRemark(dto.getRemark());
            item.setOperatorId(operatorId);
            // 复制物料明细
            item.setMaterialCode(material.getMaterialCode());
            item.setMaterialName(material.getMaterialName());
            item.setMaterialRemark(material.getMaterialRemark());
            item.setQuantity(material.getQuantity());
            item.setCurrentQuantity(material.getQuantity()); // 初始剩余量=入库量
            item.setReturnDate(material.getReturnDate());
            item.setResponsibleForeman(material.getResponsibleForeman());
            item.setResponsibleRd(material.getResponsibleRd());
            item.setLocationCode(material.getLocationCode());
            item.setBringOutStatus(material.getBringOutStatus() != null ? 
                    material.getBringOutStatus() : "不带出");
            item.setStatus("已在库");
            item.setPickable(1);
            itemList.add(item);
        }

        // 3. 批量插入
        inboundItemMapper.insertBatch(itemList); // MyBatis Plus 批量插入需自定义或使用 saveBatch
    }
}
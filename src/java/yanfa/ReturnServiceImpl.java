@Service
public class ReturnServiceImpl implements ReturnService {

    @Autowired
    private InboundItemMapper inboundItemMapper;
    @Autowired
    private OutboundRecordMapper outboundRecordMapper;
    @Autowired
    private ReturnRecordMapper returnRecordMapper;

    // 根据联络单号和物料编码获取联动信息
    @Override
    public ReturnLinkageVO getLinkageData(String contactNumber, String materialCode) {
        // 查找入库单
        InboundOrder order = inboundOrderMapper.selectOne(
                new LambdaQueryWrapper<InboundOrder>()
                        .eq(InboundOrder::getContactNumber, contactNumber));
        if (order == null) throw new BusinessException("联络单号不存在");

        // 查找该入库单下对应的入库明细（库存批次）
        InboundItem item = inboundItemMapper.selectOne(
                new LambdaQueryWrapper<InboundItem>()
                        .eq(InboundItem::getInboundOrderId, order.getId())
                        .eq(InboundItem::getMaterialCode, materialCode));
        if (item == null) throw new BusinessException("未找到对应物料");

        // 查找该物料最近的一次领用出库记录（领用人等）
        OutboundRecord lastPick = outboundRecordMapper.selectOne(
                new LambdaQueryWrapper<OutboundRecord>()
                        .eq(OutboundRecord::getInboundItemId, item.getId())
                        .eq(OutboundRecord::getOperationType, "PICK")
                        .orderByDesc(OutboundRecord::getCreateTime)
                        .last("limit 1"));
        if (lastPick == null) throw new BusinessException("该物料未被领用，无需退还");

        // 组装联动VO
        ReturnLinkageVO vo = new ReturnLinkageVO();
        vo.setWarehouseName(warehouseMapper.selectById(item.getWarehouseId()).getName());
        vo.setBorrower(userMapper.selectById(lastPick.getOperatorId()).getRealName());
        vo.setBorrowedQuantity(lastPick.getQuantity());
        vo.setMaterialName(item.getMaterialName());
        vo.setOutboundRecordId(lastPick.getId());
        return vo;
    }

    @Override
    @Transactional
    public void doReturn(ReturnInputDTO dto, Long operatorId) {
        // 先获取联动数据以校验
        ReturnLinkageVO linkage = getLinkageData(dto.getContactNumber(), dto.getMaterialCode());
        if (dto.getReturnQuantity() <= 0 || dto.getReturnQuantity() > linkage.getBorrowedQuantity()) {
            throw new BusinessException("退库数量不合法");
        }
        // 更新库存批次：增加当前数量，状态变为已退库，pickable设为0
        InboundItem item = inboundItemMapper.selectOne(
                new LambdaQueryWrapper<InboundItem>()
                        .eq(InboundItem::getMaterialCode, dto.getMaterialCode())
                        .eq(InboundItem::getInboundOrderId, /*从linkage可获取*/ ...));
        item.setCurrentQuantity(item.getCurrentQuantity() + dto.getReturnQuantity());
        item.setStatus("已退库");
        item.setPickable(0);
        inboundItemMapper.updateById(item);

        // 记录退库履历
        OutboundRecord record = new OutboundRecord();
        record.setOperationType("RETURN");
        record.setQuantity(-dto.getReturnQuantity()); // 负数表示入库
        record.setInboundItemId(item.getId());
        // ... 设置其他字段
        outboundRecordMapper.insert(record);

        // 插入退库记录
        ReturnRecord ret = new ReturnRecord();
        BeanUtils.copyProperties(linkage, ret);
        ret.setReturnQuantity(dto.getReturnQuantity());
        ret.setOperatorId(operatorId);
        ret.setReturnDate(LocalDate.now());
        returnRecordMapper.insert(ret);
    }
}
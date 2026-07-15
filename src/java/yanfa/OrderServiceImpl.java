@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMainMapper orderMainMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private InboundItemMapper inboundItemMapper;
    @Autowired
    private OutboundRecordMapper outboundRecordMapper;

    @Override
    @Transactional
    public void submitOrder(OrderSubmitDTO dto, Long applicantId) {
        // 1. 创建订单主表
        OrderMain order = new OrderMain();
        order.setOrderNo("ORD" + System.currentTimeMillis());
        order.setOrderType(dto.getOrderType());
        order.setWarehouseId(dto.getWarehouseId());
        order.setApplicantId(applicantId);
        order.setStatus("SUBMITTED");
        orderMainMapper.insert(order);

        // 2. 处理每条明细，扣减库存、生成出库记录
        for (OrderItemDTO itemDTO : dto.getItems()) {
            InboundItem stock = inboundItemMapper.selectById(itemDTO.getInboundItemId());
            if (stock == null || stock.getCurrentQuantity() < itemDTO.getQuantity()) {
                throw new BusinessException("库存不足");
            }
            if ("PICK".equals(dto.getOrderType()) && stock.getPickable() == 0) {
                throw new BusinessException("该物料已退还，不可领用，只能借料");
            }
            // 更新库存
            stock.setCurrentQuantity(stock.getCurrentQuantity() - itemDTO.getQuantity());
            if (stock.getCurrentQuantity() == 0) {
                stock.setStatus("已出库");
            }
            inboundItemMapper.updateById(stock);

            // 保存订单明细
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setInboundItemId(stock.getId());
            oi.setMaterialCode(stock.getMaterialCode());
            oi.setMaterialName(stock.getMaterialName());
            oi.setQuantity(itemDTO.getQuantity());
            orderItemMapper.insert(oi);

            // 记录出库履历
            OutboundRecord record = new OutboundRecord();
            record.setWarehouseId(dto.getWarehouseId());
            record.setInboundItemId(stock.getId());
            record.setMaterialCode(stock.getMaterialCode());
            record.setMaterialName(stock.getMaterialName());
            record.setQuantity(itemDTO.getQuantity());
            record.setOperationType(dto.getOrderType()); // PICK 或 BORROW
            record.setRelatedOrderId(order.getId());
            record.setOperatorId(applicantId);
            outboundRecordMapper.insert(record);
        }
    }
}
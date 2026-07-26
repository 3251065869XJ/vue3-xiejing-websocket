@Service
@Transactional(rollbackFor = Exception.class)
public class InventoryService {
    @Autowired
    private InventoryMapper inventoryMapper;
    
    /**
     * 研发物料入库（支持一次添加多物料）
     * 逻辑：遍历物料列表，对每一物料检查联络单号+物料编码是否已存在，
     * 若存在则更新（需满足未领用状态），否则新增记录。
     */
    public void inbound(InboundDTO dto) {
        for (InboundItemDTO item : dto.getItems()) {
            // 查询是否存在相同联络单号和物料编码的记录
            Inventory exist = inventoryMapper.selectByContactAndMaterial(
                    dto.getContactNumber(), item.getMaterialCode());
            if (exist != null) {
                // 存在则要求状态为初次入库才可更新入库数量
                if (!"FIRST_IN".equals(exist.getStatus())) {
                    throw new BusinessException("该物料已被领用，不可更新入库数量");
                }
                // 更新入库数量，同时重新计算相关库存
                updateInventoryForInbound(exist, item.getInQty(), item.getBringOutStatus());
                inventoryMapper.updateById(exist);
            } else {
                // 新增库存记录
                Inventory inv = new Inventory();
                inv.setWarehouseId(item.getWarehouseId());
                inv.setContactNumber(dto.getContactNumber());
                inv.setMaterialCode(item.getMaterialCode());
                inv.setMaterialName(item.getMaterialName());
                inv.setMaterialRemark(item.getMaterialRemark());
                inv.setReturnDate(item.getReturnDate());
                inv.setResponsibleForeman(item.getResponsibleForeman());
                inv.setResponsibleRd(item.getResponsibleRd());
                inv.setLocationCode(item.getLocationCode());
                inv.setBringOutStatus(item.getBringOutStatus());
                // 初始化库存数量
                inv.setInQty(item.getInQty());
                inv.setOnHandQty(item.getInQty());
                // 可领用数量：带出状态为1时全量，不带出则0
                inv.setAvailableQty(item.getBringOutStatus() == 1 ? item.getInQty() : 0);
                inv.setOutQty(0);
                inv.setReturnedQty(0);
                if (item.getBringOutStatus() == 1) {
                    inv.setPendingBringOutQty(item.getInQty());
                    inv.setNotBringOutQty(0);
                } else {
                    inv.setPendingBringOutQty(0);
                    inv.setNotBringOutQty(item.getInQty());
                }
                inv.setBroughtOutQty(0);
                inv.setStatus("FIRST_IN");
                inventoryMapper.insert(inv);
            }
        }
    }
    
    /**
     * 更新入库数量（外部调用，需校验状态）
     */
    public void updateInQty(InventoryUpdateDTO dto) {
        Inventory exist = inventoryMapper.selectByContactAndMaterial(
                dto.getContactNumber(), dto.getMaterialCode());
        if (exist == null) {
            throw new BusinessException("库存记录不存在");
        }
        if (!"FIRST_IN".equals(exist.getStatus())) {
            throw new BusinessException("该物料已被领用，不可更新入库数量");
        }
        updateInventoryForInbound(exist, dto.getNewInQty(), exist.getBringOutStatus());
        inventoryMapper.updateById(exist);
    }
    
    /**
     * 入库或更新入库数量时，重新计算库存字段
     */
    private void updateInventoryForInbound(Inventory inv, Integer newInQty, Integer bringOutStatus) {
        inv.setInQty(newInQty);
        inv.setOnHandQty(newInQty); // 入库后全部在库
        if (bringOutStatus == 1) {
            inv.setAvailableQty(newInQty);
            inv.setPendingBringOutQty(newInQty);
            inv.setNotBringOutQty(0);
        } else {
            inv.setAvailableQty(0);
            inv.setPendingBringOutQty(0);
            inv.setNotBringOutQty(newInQty);
        }
        // 其他字段重置（假设无出库退还）
        inv.setOutQty(0);
        inv.setReturnedQty(0);
        inv.setBroughtOutQty(0);
    }
    
    /**
     * 物料退还
     * 根据联络单号+物料编码定位库存，退库数量增加到在库数量，
     * 同时增加已退库数量，且将状态改为已领用（表示发生过领用）
     * 注意：退还后该物料不可再被普通领用，只能借出（业务可扩展冻结字段）
     */
    public void returnMaterial(ReturnDTO dto) {
        Inventory inv = inventoryMapper.selectByContactAndMaterial(
                dto.getContactNumber(), dto.getMaterialCode());
        if (inv == null) {
            throw new BusinessException("库存记录不存在");
        }
        // 更新库存
        inv.setOnHandQty(inv.getOnHandQty() + dto.getReturnQty());
        inv.setReturnedQty(inv.getReturnedQty() + dto.getReturnQty());
        // 退还后不可领用，将可用数量设为0（或冻结标记，此处简化）
        inv.setAvailableQty(0);
        // 状态变更为已领用（如果还不是）
        if ("FIRST_IN".equals(inv.getStatus())) {
            inv.setStatus("TAKEN");
        }
        inventoryMapper.updateById(inv);
    }
}
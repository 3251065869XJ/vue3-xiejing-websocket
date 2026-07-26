// 各Mapper继承BaseMapper即可，特殊查询在Service中通过LambdaQueryWrapper实现
public interface WarehouseMapper extends BaseMapper<Warehouse> {}
public interface InventoryMapper extends BaseMapper<Inventory> {
    // 根据联络单号与物料编码查询（用于唯一约束查询）
    default Inventory selectByContactAndMaterial(String contactNumber, String materialCode) {
        return selectOne(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getContactNumber, contactNumber)
                .eq(Inventory::getMaterialCode, materialCode));
    }
}
public interface OrderMainMapper extends BaseMapper<OrderMain> {}
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
    default List<OrderDetail> selectByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderId));
    }
}
public interface UserWarehouseRoleMapper extends BaseMapper<UserWarehouseRole> {}
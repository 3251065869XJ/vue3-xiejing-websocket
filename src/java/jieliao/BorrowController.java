@RestController
@RequestMapping("/api/borrow")
public class BorrowController {
    @Autowired
    private BorrowOrderService borrowOrderService;
    @Autowired
    private BorrowOrderMapper borrowOrderMapper;
    @Autowired
    private BorrowOrderDetailMapper borrowDetailMapper;

    // 借料申请
    @PostMapping("/submit")
    public R submitBorrow(@RequestParam String applicant, @RequestBody @Valid BorrowOrderSubmitDTO dto) {
        BorrowOrder order = borrowOrderService.submitBorrow(applicant, dto);
        return R.ok(order);
    }

    // 取消借料单
    @PutMapping("/{orderId}/cancel")
    public R cancel(@PathVariable Long orderId) {
        borrowOrderService.cancelBorrow(orderId);
        return R.ok();
    }

    // 拒绝借料单
    @PutMapping("/{orderId}/reject")
    public R reject(@PathVariable Long orderId) {
        borrowOrderService.rejectBorrow(orderId);
        return R.ok();
    }

    // 借料发货
    @PutMapping("/ship")
    public R ship(@RequestBody @Valid BorrowShipmentDTO shipment) {
        borrowOrderService.shipBorrow(shipment);
        return R.ok();
    }

    // 借料归还
    @PutMapping("/return")
    public R returnBorrow(@RequestBody @Valid BorrowReturnDTO dto) {
        borrowOrderService.returnBorrow(dto);
        return R.ok();
    }

    // 查询可借库存（前端选择物料时调用）
    @GetMapping("/available-stock")
    public R availableStock(@RequestParam Long warehouseId, @RequestParam(required = false) String materialCode) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, warehouseId)
                .gt(Inventory::getBorrowableQty, 0)
                .eq(Inventory::getStatus, "TAKEN");
        if (StringUtils.hasText(materialCode)) {
            wrapper.eq(Inventory::getMaterialCode, materialCode);
        }
        return R.ok(inventoryMapper.selectList(wrapper));
    }

    // 借料单明细查询
    @GetMapping("/{orderId}/details")
    public R getDetails(@PathVariable Long orderId) {
        return R.ok(borrowDetailMapper.selectByBorrowOrderId(orderId));
    }
}
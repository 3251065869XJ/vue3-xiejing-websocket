@RestController
@RequestMapping("/api/inbound")
public class InboundController {
    private final InboundService inboundService;

    /**
     * 研发物料入库（支持一次多条物料，联络单号唯一）
     */
    @PostMapping
    public Result createInbound(@RequestBody @Valid InboundDTO dto) {
        Long operatorId = SecurityUtils.getCurrentUserId(); // 获取当前登录库房管理员ID
        inboundService.createInbound(dto, operatorId);
        return Result.success("入库成功");
    }
}

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    public Result submitOrder(@RequestBody @Valid OrderSubmitDTO dto) {
        Long applicantId = SecurityUtils.getCurrentUserId();
        orderService.submitOrder(dto, applicantId);
        return Result.success("领用/借料提交成功");
    }
}

@RestController
@RequestMapping("/api/return")
public class ReturnController {
    @Autowired
    private ReturnService returnService;

    @GetMapping("/linkage")
    public Result getLinkage(@RequestParam String contactNumber,
                             @RequestParam String materialCode) {
        return Result.success(returnService.getLinkageData(contactNumber, materialCode));
    }

    @PostMapping
    public Result doReturn(@RequestBody @Valid ReturnInputDTO dto) {
        Long operatorId = SecurityUtils.getCurrentUserId();
        returnService.doReturn(dto, operatorId);
        return Result.success("退库成功");
    }
}
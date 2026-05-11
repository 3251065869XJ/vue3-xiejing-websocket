import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtil {
    /**
     * 判断字符串是否为数字，并将小数四舍五入保留1位小数；
     * 如果结果是整数，则不显示小数点。
     * 非法输入返回 null。
     */
    public static String formatToOneDecimal(String input) {
        if (input == null) return null;
        String str = input.trim();
        if (str.isEmpty()) return null;

        try {
            BigDecimal bd = new BigDecimal(str);
            // 保留一位小数，四舍五入
            bd = bd.setScale(1, RoundingMode.HALF_UP);
            // stripTrailingZeros 去掉末尾无效零（整数则变为无小数形式）
            return bd.stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return null;   // 不是有效数字
        }
    }
}
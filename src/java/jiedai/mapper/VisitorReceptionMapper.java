// VisitorReceptionMapper.java
package com.example.visitor.mapper;
import com.example.visitor.entity.VisitorReception;
import com.example.visitor.vo.VisitorReceptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface VisitorReceptionMapper {
    void insert(VisitorReception visitorReception);
    List<VisitorReceptionVO> findUncheckedByProduct(@Param("product") String product);
    VisitorReception selectById(@Param("id") Integer id);
    void updateCheckoutStatus(@Param("id") Integer id, @Param("checkoutTime") java.time.LocalDateTime checkoutTime);
}
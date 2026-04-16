import com.example.entity.DataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface DataRecordRepository extends JpaRepository<DataRecord, Long> {

    /**
     * 根据工号和区域编号查询最新更新时间
     */
    @Query("SELECT d.updateTime FROM DataRecord d " +
           "WHERE d.userAccount = :userAccount AND d.areaId = :areaId " +
           "ORDER BY d.updateTime DESC LIMIT 1")
    LocalDateTime findLatestUpdateTime(@Param("userAccount") String userAccount,
                                       @Param("areaId") String areaId);
}


// 使用 LambdaQueryWrapper 查询最新一条
LocalDateTime latestUpdateTime = dataRecordMapper.selectOne(
    Wrappers.lambdaQuery(DataRecord.class)
        .eq(DataRecord::getUserAccount, userAccount)
        .eq(DataRecord::getAreaId, areaId)
        .orderByDesc(DataRecord::getUpdateTime)
        .last("LIMIT 1")
).getUpdateTime();
package com.sober.delay.dao;

import com.sober.delay.entity.ExecuteLogEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author liweigao
 * @date 2018/12/8 上午10:41
 */
@Repository
public interface ExecuteLogDao extends CrudRepository<ExecuteLogEntity, Integer> {

    /**
     * 删除数据
     *
     * @param createTime 创建时间
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "delete from execute_log where create_time < :createTime", nativeQuery = true)
    void deleteExecuteLogEntitiesByCreateTimeBefore(@Param("createTime") Date createTime);

}

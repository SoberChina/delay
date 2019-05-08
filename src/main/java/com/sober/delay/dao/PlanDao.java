package com.sober.delay.dao;

import com.sober.delay.entity.PlanEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author liweigao
 * @date 2018/12/7 下午7:12
 */
@Repository
public interface PlanDao extends CrudRepository<PlanEntity, Integer> {


    /**
     * @param planCode code
     * @return entity
     */
    PlanEntity findByPlanCode(String planCode);

    /**
     * modify state
     *
     * @param state state
     * @param id    id
     */
    @Transactional
    @Modifying
    @Query(value = "update plan  set state=:state where id=:id and state !=1 ", nativeQuery = true)
    void modifyStateById(@Param("state") Integer state, @Param("id") Integer id);


    /**
     * 查找时间范围内的数据
     *
     * @param beginTime begin time
     * @param endTime   end  time
     * @param flag      flag
     * @return collection
     */
    List<PlanEntity> findPlanEntitiesByExecuteTimeBetweenAndFlag(Date beginTime, Date endTime, Integer flag);

    /**
     * 查找小于某个时间
     *
     * @param endTime end time
     * @param flag    flag
     * @return collection
     */
    List<PlanEntity> findPlanEntitiesByExecuteTimeBeforeAndFlag(Date endTime, Integer flag);


    /**
     * modify flag
     *
     * @param collection id collection
     */
    @Transactional
    @Modifying
    @Query(value = "update plan  set flag=1 where id in (:collection) and flag =0", nativeQuery = true)
    void modifyFlagByIds(@Param("collection") Collection<Integer> collection);


    /**
     * 删除执行事件之前的数据
     * 不执行回滚
     *
     * @param executeTime 执行时间
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "delete from plan where execute_time < :executeTime", nativeQuery = true)
    void deletePlanEntitiesByExecuteTimeBefore(@Param("executeTime") Date executeTime);


}

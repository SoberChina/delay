package com.sober.delay.dao;

import com.sober.delay.entity.PlanEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author liweigao
 * @date 2018/12/11 下午2:57
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@Ignore
public class PlanDaTest {


    @Autowired
    private PlanDao planDao;

    @Test
    public void test() throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<PlanEntity> planEntities = planDao.findPlanEntitiesByExecuteTimeBetweenAndFlag(sdf.parse("2018-12-10 " +
                "14:59:40"), sdf.parse("2018-12-12 14:59:54"), 0);
        log.info(String.valueOf(planEntities.size()));
    }
}

package com.sober.delay.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sober.delay.common.Constant;
import com.sober.delay.common.result.Page;
import com.sober.delay.common.result.RestResult;
import com.sober.delay.config.DelayProperties;
import com.sober.delay.config.RabbitMqBeanConfig;
import com.sober.delay.config.RedisEntityConfig;
import com.sober.delay.dao.ExecuteLogDao;
import com.sober.delay.dao.PlanDao;
import com.sober.delay.entity.ExecuteLogEntity;
import com.sober.delay.entity.PlanEntity;
import com.sober.delay.entity.dto.PlanDto;
import com.sober.delay.entity.params.PlanParams;
import com.sober.delay.enums.Error;
import com.sober.delay.enums.*;
import com.sober.delay.exception.BizException;
import com.sober.delay.exception.NotFoundException;
import com.sober.delay.service.PlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author liweigao
 * @date 2018/12/8 下午2:47
 */
@Slf4j
@Service
public class PlanServiceImpl implements PlanService {

    /**
     * 阈值 threshold
     */
    private static final Long THRESHOLD = 60 * 1000L;

    @Autowired
    private RedisEntityConfig redisEntityConfig;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("innerRestTemplate")
    private RestTemplate innerRestTemplate;

    @Autowired
    private RabbitMqBeanConfig rabbitMqBeanConfig;

    @Autowired
    private PlanDao planDao;

    @Autowired
    private ExecuteLogDao executeLogDao;

    @Autowired
    private DelayProperties delayProperties;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PlanDto save(PlanParams planParams) {


        //默认加上容错时间处理
        Long expire =
                planParams.getExecuteTime().getTime() - System.currentTimeMillis() + Constant.FAULT_TOLERANT_REQUEST_TIME;

        //如果在job规定范围内
        boolean flag = (expire < (Constant.ADVANCE_TIME + Constant.TASK_CYCLE));


        //控制分钟级别 去掉后可以按照毫秒级别;
        if (expire < THRESHOLD) {
            expire = null;
        } else {
            expire = formatTimeStamp(expire);
        }

        PlanEntity planEntity = PlanParams.conventEntity(planParams);
        planEntity.setFlag(PlanFlag.NORMAL.getValue());
        //如果时间在job规定范围内 直接推送mq
        if (flag) {
            planEntity.setFlag(PlanFlag.COMPLETE.getValue());
        }

        planEntity.setPlanCode(UUID.randomUUID().toString().replace("-", ""));
        planEntity = planDao.save(planEntity);
        PlanDto planDto = build(planEntity);

        if (flag) {
            pushData(planDto, expire);
        }
        return planDto;
    }

    @Override
    public void deleteByCode(String planCode) {
        PlanEntity planEntity = planDao.findByPlanCode(planCode);
        if (Objects.isNull(planEntity)) {
            throw new NotFoundException(Error.PLAN_IS_NOT_FOUND);
        }
        if (new Date().compareTo(new Date(planEntity.getExecuteTime().getTime())) > 0) {
            return;
        }

        planDao.modifyStateById(PlanStatus.DELETE.getCode(), planEntity.getId());
    }

    @Override
    public PlanDto modifyByCode(PlanParams planParams) {
        //        planDao.save(planParams)

        throw new BizException(Error.FUNCTION_NOT_IMPLEMENTED);
    }

    @Override
    public List<PlanDto> list() {
        Iterable<PlanEntity> planEntities = planDao.findAll();

        if (Objects.isNull(planEntities)) {
            return Lists.newArrayList();
        }
        return build(Lists.newArrayList(planEntities));
    }

    @Override
    public Page<PlanDto> list(Integer page, Integer pageSize) {
        /**
         * jpa pageable page is zero-based page index
         * @see PageRequest#of(int, int)
         * @see org.springframework.data.domain.AbstractPageRequest#AbstractPageRequest(int, int)
         */
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        org.springframework.data.domain.Page<PlanEntity> planEntities = planDao.findAll(pageable);

        return Page.<PlanDto>builder().items(build(planEntities.getContent())).page(page).pageSize(pageSize)
                .total(planEntities.getTotalElements()).first(planEntities.isFirst()).last(planEntities.isLast())
                .empty(planEntities.isEmpty()).totalPages(planEntities.getTotalPages()).build();
    }

    @Override
    public PlanDto findByCode(String planCode) {

        PlanEntity planEntity = planDao.findByPlanCode(planCode);
        if (Objects.isNull(planEntity)) {
            throw new NotFoundException(Error.PLAN_IS_NOT_FOUND);
        }
        return build(planEntity);
    }

    @Override
    public void executePlan(String planCode) {

        PlanEntity planEntity = planDao.findByPlanCode(planCode);

        if (Objects.isNull(planEntity)) {
            throw new NotFoundException(Error.PLAN_IS_NOT_FOUND);
        }
        executePlan(build(planEntity), ExecuteType.MANUAL);
    }

    @Override
    public void executePlan(PlanDto planDto, ExecuteType executeType) {

        Optional<PlanEntity> optional = planDao.findById(planDto.getId());
        //状态不一致消息丢弃
        if (!optional.isPresent() || optional.get().getState().equals(PlanStatus.DELETE.getCode())) {
            return;
        }
        //封装参数
        Map<String, Object> params = Maps.newHashMap();
        final HttpHeaders headers = new HttpHeaders();

        if (!Objects.isNull(planDto.getParams())) {
            params = planDto.getParams();
        }
        if (!Objects.isNull(planDto.getHeaders())) {
            planDto.getHeaders().forEach((k, v) -> {
                headers.add(String.valueOf(k), String.valueOf(v));
            });
        }
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity(params, headers);
        RestResult restResult;
        ResponseEntity<String> stringResponseEntity;
        String request = JSON.toJSONString(params) + JSON.toJSONString(headers);
        if (request.length() > Constant.LOG_LIMIT_LENGTH) {
            request = request.substring(0, Constant.LOG_LIMIT_LENGTH);
        }
        ExecuteLogEntity logEntity = ExecuteLogEntity.builder().planId(planDto.getId())
                .executeType(executeType.getCode()).request(request).build();
        try {
            //如果是 0走eureka调用否则走ip
            if (PlanType.EUREKA.getType().equals(planDto.getPlanType())) {
                stringResponseEntity = innerRestTemplate.exchange(planDto.getCallbackUrl(),
                        HttpMethod.resolve(planDto.getCallbackMethod().toUpperCase()), httpEntity, String.class);
            } else {
                stringResponseEntity = restTemplate.exchange(planDto.getCallbackUrl(),
                        HttpMethod.resolve(planDto.getCallbackMethod().toUpperCase()), httpEntity, String.class);
            }
            log.info("result:{}", JSON.toJSONString(stringResponseEntity));
        } catch (Exception e) {
            log.error(e.getMessage());
            String errorMsg = e.getMessage();
            if (errorMsg.length() > Constant.LOG_LIMIT_LENGTH) {
                errorMsg = errorMsg.substring(0, Constant.LOG_LIMIT_LENGTH);
            }
            logEntity.setResponse(errorMsg);
            saveLog(logEntity);
            throw new BizException(Error.REST_TEMPLATE_IO_EXCEPTION);
        }
        try {
            MediaType mediaType;
            if (!Objects.isNull(stringResponseEntity) && (mediaType =
                    stringResponseEntity.getHeaders().getContentType()) != null) {
                String response = JSON.toJSONString(stringResponseEntity.getBody());
                if (response.length() > Constant.LOG_LIMIT_LENGTH) {
                    response = response.substring(0, Constant.LOG_LIMIT_LENGTH);
                }
                logEntity.setResponse(response);
                //判断mediaType 是否符合格式
                boolean isContains =
                        MediaType.APPLICATION_JSON.includes(mediaType) || MediaType.APPLICATION_JSON_UTF8.includes(mediaType);
                if (isContains) {
                    restResult = JSON.parseObject(stringResponseEntity.getBody(), RestResult.class);
                    if (Objects.isNull(restResult) || !(RestResult.CODE_SUCCESS.equals(restResult.getCode()) || Integer.valueOf(200).equals(restResult.getCode()))) {
                        throw new BizException(Error.PLAN_EXECUTE_ERROR);
                    }
                } else {
                    throw new BizException(Error.MIME_TYPE_NOT_MATCHING);
                }
            } else {
                logEntity.setResponse("error");
                throw new BizException(Error.PLAN_EXECUTE_ERROR);
            }
        } finally {
            saveLog(logEntity);
        }
    }

    @Override
    @Nullable
    public void pushData(PlanDto planDto, @Nullable Long expireTime) {

        String routingKey = String.format(RabbitMqBeanConfig.DELAY_TTL_ROOTING_KEY, expireTime);
        String queueName = String.format(RabbitMqBeanConfig.DELAY_TTL_QUEUE, expireTime);

        //如果不设过期时间 直接异步执行
        if (expireTime == null || expireTime < THRESHOLD) {
            rabbitTemplate.convertAndSend(RabbitMqBeanConfig.DELAY_CONSUMER_DLX_DIRECT_EXCHANGE,
                    RabbitMqBeanConfig.DEFAULT_ROUTING_KEY, JSON.toJSONString(planDto));
            return;
            //如果标识为空 动态创建组件
        }
        if (Objects.isNull(redisEntityConfig.getStringHashOperations().get(Constant.RedisConstant.getFlagKey()
                , String.valueOf(expireTime)))) {
            //注册组件
            rabbitMqBeanConfig.createAssembly(queueName, routingKey, expireTime);
            //加入标识
            redisEntityConfig.getStringHashOperations().put(Constant.RedisConstant.getFlagKey()
                    , String.valueOf(expireTime), "flag");
            redisEntityConfig.getRedisTemplate().expire(Constant.RedisConstant.getFlagKey(),
                    Constant.RedisConstant.getDefaultExpireTime(), TimeUnit.MILLISECONDS);
        }
        rabbitTemplate.convertAndSend(RabbitMqBeanConfig.DELAY_TTL_DIRECT_EXCHANGE, routingKey,
                JSON.toJSONString(planDto));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void autoPush(Date beginTime, Date endTime) throws InterruptedException {

        List<PlanEntity> planEntities;
        switch (delayProperties.getLoadStrategy()) {
            case LATEST:
                planEntities = planDao.findPlanEntitiesByExecuteTimeBetweenAndFlag(beginTime, endTime, 0);
                break;
            case EARLIEST:
                planEntities = planDao.findPlanEntitiesByExecuteTimeBeforeAndFlag(endTime, 0);
                break;
            default:
                planEntities = planDao.findPlanEntitiesByExecuteTimeBetweenAndFlag(beginTime, endTime, 0);
        }

        if (CollectionUtils.isEmpty(planEntities)) {
            return;
        }
        CountDownLatch countDownLatch = new CountDownLatch(planEntities.size());


        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("pushData-pool-%d").build();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(3, 10, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(planEntities.size()), namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());

        planEntities.forEach(planEntity -> {
            threadPoolExecutor.execute(() -> {
                final Long expire = planEntity.getExecuteTime().getTime() - System.currentTimeMillis();
                pushData(build(planEntity), formatTimeStamp(expire));
                countDownLatch.countDown();
            });

        });
        countDownLatch.await();

        planDao.modifyFlagByIds(planEntities.stream().map(PlanEntity::getId).collect(Collectors.toList()));
    }

    @Override
    public void autoDeleteData(Date executeTime) {


        planDao.deletePlanEntitiesByExecuteTimeBefore(executeTime);
        executeLogDao.deleteExecuteLogEntitiesByCreateTimeBefore(executeTime);

    }

    private List<PlanDto> build(List<PlanEntity> planEntities) {

        List<PlanDto> planDtos = Lists.newArrayList();
        if (CollectionUtils.isEmpty(planEntities)) {
            return planDtos;
        }
        planEntities.forEach(planEntity -> {
            planDtos.add(build(planEntity));
        });
        return planDtos;
    }

    private PlanDto build(PlanEntity planEntity) {

        if (Objects.isNull(planEntity)) {
            return PlanDto.builder().build();
        }
        return PlanDto.builder().planType(planEntity.getPlanType()).callbackUrl(planEntity.getCallbackUrl()).createTime(planEntity.getCreateTime())
                .executeTime(planEntity.getExecuteTime()).callbackMethod(planEntity.getCallbackMethod())
                .headers((Map<String, String>) JSON.parse(planEntity.getHeaders())).params((Map<String, Object>) JSON.parse(planEntity.getParams())).planCode(planEntity.getPlanCode())
                .id(planEntity.getId()).planName(planEntity.getPlanName()).retryNum(planEntity.getRetryNum()).state(planEntity.getState())
                .updateTime(planEntity.getUpdateTime()).build();
    }

    private void saveLog(ExecuteLogEntity logEntity) {
        executeLogDao.save(logEntity);
    }

    private Long formatTimeStamp(Long timestamp) {

        return timestamp / THRESHOLD * THRESHOLD;
    }
}

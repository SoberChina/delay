package com.sober.delay.common;

/**
 * @author liweigao
 * @date 2018/12/8 下午4:38
 */
public interface Constant {


    /**
     * 默认请求容错时间
     */
    Long FAULT_TOLERANT_REQUEST_TIME = 100L;
    /**
     * 任务周期 半个小时
     */
    Long TASK_CYCLE = 30 * 60 * 1000L;

    /**
     * 提前30分钟初始化到rabbitmq中
     */
    Long ADVANCE_FLAG = 30 * 60 * 1000L;

    int LOG_LIMIT_LENGTH = 250;

    class RedisConstant {

        /**
         * lock key
         */
        private static final String PUSH_DATA_LOCK_KEY = "delay:pushData";
        /**
         * 标志
         */
        private static final String RABBITMQ_BEAN = "delay:rabbitMqBean";

        private static final long DEFAULT_EXPIRE_TIME = 5 * 60 * 1000L;

        public static String getFlagKey() {
            return RABBITMQ_BEAN;
        }

        public static String getPushDataLockKey() {
            return PUSH_DATA_LOCK_KEY;
        }

        public static long getDefaultExpireTime() {
            return DEFAULT_EXPIRE_TIME;
        }
    }

}

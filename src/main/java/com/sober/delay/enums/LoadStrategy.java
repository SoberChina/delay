package com.sober.delay.enums;

/**
 * 加载数据类型
 *
 * @author liweigao
 * @date 2019/5/8 上午11:05
 */
public enum LoadStrategy {

    /**
     * 加载截止任务时间之前的数据(包涵过期未执行的任务)
     */
    EARLIEST,

    /**
     * 加载最新的任务(过期任务不加载)
     */
    LATEST;

}

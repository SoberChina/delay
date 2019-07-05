package com.sober.delay.common.result;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author liweigao
 * @date 2019/07/04 下午2:19
 */
@Data
@Builder
public class Page<T> {

    /**
     * 是否为空
     */
    private Boolean empty;

    /**
     * 是否为第一页
     */
    private Boolean first;

    /**
     * 是否为最后一页
     */
    private Boolean last;
    /**
     * 页数
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 条数
     */
    private List<T> items;

}

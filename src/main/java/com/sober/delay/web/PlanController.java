package com.sober.delay.web;

import com.sober.delay.common.result.RestResult;
import com.sober.delay.entity.dto.PlanDto;
import com.sober.delay.entity.params.PlanParams;
import com.sober.delay.enums.Error;
import com.sober.delay.exception.BizException;
import com.sober.delay.service.PlanService;
import com.sober.delay.validate.group.Add;
import com.sober.delay.validate.group.Modify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author liweigao
 * @date 2018/12/10 上午10:20
 */
@RestController
@RequestMapping("/delay/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @RequestMapping(value = "", name = "添加计划", method = RequestMethod.POST)
    public ResponseEntity<RestResult<PlanDto>> add(@RequestBody @Validated(value = {Add.class}) PlanParams planParams) {

        if (planParams.getCallbackUrl().indexOf("/delay/plan") > 0) {
            throw new BizException(Error.BAD_REQUEST);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResult.success(planService.save(planParams)));
    }

    @RequestMapping(value = "", name = "修改计划", method = RequestMethod.PATCH)
    public ResponseEntity<RestResult<PlanDto>> modify(@RequestBody @Validated(value = {Modify.class}) PlanParams planParams) {

        return ResponseEntity.status(HttpStatus.CREATED).body(RestResult.success(planService.modifyByCode(planParams)));
    }

    /**
     * 204 no content 设定返回值为了aop能拦截 ,todo
     *
     * @param planCode
     * @return
     */
    @RequestMapping(value = "/{planCode}", name = "删除计划", method = RequestMethod.DELETE)
    public ResponseEntity<RestResult> delete(@PathVariable("planCode") String planCode) {
        planService.deleteByCode(planCode);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(RestResult.success());
    }


    @RequestMapping(value = "/{planCode}", name = "计划详情", method = RequestMethod.GET)
    public ResponseEntity<RestResult<PlanDto>> get(@PathVariable("planCode") String planCode) {

        return ResponseEntity.ok(RestResult.success(planService.findByCode(planCode)));
    }


    @RequestMapping(value = "", name = "计划列表", method = RequestMethod.GET)
    public ResponseEntity<RestResult<PlanDto>> List() {

        return ResponseEntity.ok(RestResult.success(planService.list()));
    }

    @RequestMapping(value = "/{planCode}/execute", name = "执行计划", method = RequestMethod.POST)
    public ResponseEntity<RestResult<PlanDto>> add(@PathVariable("planCode") String planCode) {

        planService.executePlan(planCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(RestResult.success());
    }

}

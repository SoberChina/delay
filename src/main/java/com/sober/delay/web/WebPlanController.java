package com.sober.delay.web;

import com.sober.delay.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author liweigao
 * @date 2019/7/4 下午3:14
 */
@Controller
@RequestMapping("/plan/index")
public class WebPlanController {

    @Autowired
    private PlanService planService;

    @GetMapping(value = "", name = "列表")
    public ModelAndView list(ModelAndView mv,
                             @RequestParam(value = "page", defaultValue = "1", required = false) Integer page,
                             @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

        mv.setViewName("index");
        mv.addObject("planList", planService.list(page, pageSize));
        return mv;
    }

}

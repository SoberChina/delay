package com.sober.delay.reset;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.sober.delay.common.result.RestResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author liweigao
 * @date 2018/12/11 下午7:32
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class RestTemplateTest {

    @Autowired
    private RestTemplate restTemplate;


    @Test
    public void test() {
        Map map = Maps.newHashMap();
        map.put("test", 1);

        MultiValueMap<String, String> valueMap = new HttpHeaders();
        valueMap.add("test", "1");
        HttpEntity httpEntity = new HttpEntity(map, valueMap);
        ResponseEntity<String> responseEntity = restTemplate.exchange("http://www.baidu.com", HttpMethod.GET,
                httpEntity, String.class);
        log.info(JSON.toJSONString(responseEntity));

        ResponseEntity<RestResult> restResultResponseEntity = restTemplate.exchange("http://www.baidu.com", HttpMethod.GET,
                httpEntity, RestResult.class);

        log.info(JSON.toJSONString(restResultResponseEntity));

    }
}

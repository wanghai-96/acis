package com.acis.controller.cxx.preoperativeInfoSystem;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.service.cxx.postoperativeInfo.Impl.posSurgicaRegisterImpl;
import com.acis.service.cxx.preoperativeInfo.Impl.preEemergencyImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: 术前急诊手术管理功能
 * @author: cxx
 * @date: 2020/6/11
 */
@Log4j2
@Api(tags = "术前急诊手术管理功能")
@RestController
@RequestMapping("/acis/preIS/preEemergency")
public class preEemergencyController {
    @Autowired
    posSurgicaRegisterImpl possurgicaregisterimpl;

    @Autowired
    preEemergencyImpl preeemergencyimpl;

    @ApiOperation("术前急诊手术管理功能-controller")
    @GetMapping("/test")
    @ResponseBody
    public R<String> test(@RequestParam("maxSalary") Integer maxSalary) {
        return R.data("sucess");
    }

    @ApiOperation(value = "术前急诊手术管理功能-数据查询与回显", notes = "通过录入患者ID或住院号从HIS系统中提取急诊手术信，可实现快速安排患者进行手术")
    @PostMapping("/showEemergencyInfo")
    @ResponseBody
    public R<Map<String, List<Map<String, String>>>> showEemergencyInfo(@RequestParam String patientInfo) {
        Map<String, List<Map<String, String>>> eemergencyInfo = preeemergencyimpl.showEemergencyInfo(patientInfo);
        return R.data(eemergencyInfo);
    }

    @ApiOperation(value = "术前急诊手术管理功能-数据保存", notes = "保存录入的数据")
    @PostMapping("/saveEemergencyInfo")
    @ResponseBody
    public R<String> saveEemergencyInfo(@RequestBody Map<String, List<Map<String, String>>> EemergencyInfo) {
        Map<String, Object> map = preeemergencyimpl.saveEemergencyInfo(EemergencyInfo);
        int i = Integer.parseInt(String.valueOf(map.get("i")));
        String operationId = String.valueOf(map.get("operationId"));
        if (i==1) {
            return R.data(operationId);
        }
        return R.fail(CommonErrorCode.E100017.getMsg());
    }

}

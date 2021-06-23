package com.acis.controller.cxx.bloodGasAssay;

import com.acis.common.exception.R;
import com.acis.service.cxx.bloodGasAssay.Impl.bloodAgaAssayCoreImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 血气分析
 * @author: cxx
 * @date: 2020/6/11
 */

@Log4j2
@Api(tags = "血气分析")
@RestController
@RequestMapping("/acis/bga")
public class BloodGasAssayController {

    @Autowired
    bloodAgaAssayCoreImpl  bloodagaassaycoreimpl;


    @ApiOperation("血气分析-controller")
    @GetMapping("/test")
    @ResponseBody
    public R<String> test(@RequestParam("maxSalary") Integer maxSalary){
        return R.data("sucess");
    }


    @ApiOperation(value = "血气分析-主接口",notes = "1.实时查看患者的各次血气分析结果。\n" +
            "2.在麻醉单上显示血气分析数据。\n" +
            "3.提供独立的界面，对患者手工录入血气相关4.分析项的数值。\n" +
            "5.支持模板快速录入。")
    @ApiModelProperty(value = "需求体现核心接口")
    @PostMapping("/access")
    @ResponseBody
    public R<String> access() {
        return R.data("sucess");
    }

    @ApiOperation("血气分析-保存")
    @PostMapping("/save")
    @ResponseBody
    public R<String> save(@RequestParam("result") String result) {
        return R.data("sucess");
    }
}

package com.acis.controller.cxx.postoperativeInfoSystem;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.service.cxx.common.Impl.tamplateDataSourseImpl;
import com.acis.service.cxx.postoperativeInfo.Impl.posSurgicaRegisterImpl;
import com.acis.service.cxx.postoperativeInfo.Impl.postoperativeInfoCoreImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description: 术后手术登记功能
 * @author: cxx
 * @date: 2020/6/11
 */

@Log4j2
@Api(tags = "术后手术登记功能")
@RestController
@RequestMapping("/acis/posIS/posSurgicaRegister")
public class posSurgicaRegisterController {

    @Autowired
    postoperativeInfoCoreImpl postoperativeinfocoreimpl;

    @Autowired
    posSurgicaRegisterImpl possurgicaregisterimpl;

    @ApiOperation("术后手术登记功能-controller")
    @GetMapping("/test")
    @ResponseBody
    public R<String> test(@RequestParam("maxSalary") Integer maxSalary) {
        return R.data("sucess");
    }

    @ApiOperation("术后手术登记功能-查询患者手术信息")
    @PostMapping("/queryOperationInfo")
    @ResponseBody
    public R<List<Map<String, String>>> queryPatientInfo(@RequestParam("operationId") String operationId) {
        boolean IsOperationId = StringUtils.isBlank(operationId);
        if (IsOperationId){
            return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
        }
        List<Map<String, String>> surgicaRegister = possurgicaregisterimpl.showSurgicaRegister(operationId);
        if (surgicaRegister.size()==0){
            return  R.fail(CommonErrorCode.E100018.getCode(),CommonErrorCode.E100018.getMsg());
        }
        log.info("surgicaRegister:"+surgicaRegister.toString());
        return R.data(surgicaRegister);
    }


    @ApiOperation("术后手术登记功能-保存")
    @PostMapping("/save")
    @ResponseBody
    public R<String> save(@RequestBody Map<String, List<Map<String, String>>> results,String operationId) {
        if (results.isEmpty()){
            return  R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
        }
        int i = possurgicaregisterimpl.saveSurgicaRegister(results, operationId);
         if (i==0){
            return  R.fail(CommonErrorCode.E100027.getCode(),CommonErrorCode.E100027.getMsg());
        }
        return R.data(CommonErrorCode.E0.getMsg());
    }


}

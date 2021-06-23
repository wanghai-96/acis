package com.acis.controller.cxx.common;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.service.cxx.common.Impl.templateImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Administrator
 * @date: 2020/6/24 11:14
 */
@Log4j2
@Api(tags = "模板展示公用接口")
@RestController
@RequestMapping("/acis/common/template")
public class commonTemplateController {

    @Autowired
    templateImpl templateimpl;

    @ApiOperation(value = "模板展示", notes = "系统文书模板展示通用接口")
    @PostMapping("/templateShowing")
    @ResponseBody
    public R<Map<String, Object>> templateShowing(@RequestParam("templateCode") String templateCode) {
        String templateContent = "";
        log.info("templateCode:" + templateCode);
        //根据手术ID,查询当前手术的所有信息
        if (StringUtils.isBlank(templateCode)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "模板ID为空!");
        }
        templateContent = templateimpl.queryTemplate(templateCode);
        if (templateContent != null) {
            JSONArray jsonArray = JSON.parseArray(templateContent);
//        String s = JSON.toJSONString(jsonArray);
            List<JSONArray> result = new ArrayList<>(2);
            result.add(jsonArray);
            Map<String, Object> resultMap = new HashMap<>(3);
            resultMap.put("list", jsonArray);
            resultMap.put("isIntraoperative", false);

            if (templateCode.equals("50cd8c")) {
                resultMap.put("isIntraoperative", true);
            }
            return R.data(resultMap);

        }
        log.error(CommonErrorCode.E100018.getMsg());
        return R.fail(CommonErrorCode.E100018.getCode(), CommonErrorCode.E100018.getMsg());
    }


    @ApiOperation(value = "查询患者填写模板信息", notes = "查询患者所有填写模板信息")
    @PostMapping("/queryFillInTemplateInfo")
    @ResponseBody
    public R<Map<String, List<Map<String, Object>>>> queryFillInTemplateInfo(@RequestParam("templateCode") String templateCode, String operationId, String patientId) {
//        String templateContent ="";
        //根据手术ID,查询当前手术的所有信息
        if (StringUtils.isBlank(templateCode)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "模板ID为空!");
        }
        Map<String, List<Map<String, Object>>> templateContent = templateimpl.queryTemplateInfo(templateCode, operationId, patientId);

        return R.data(templateContent);
    }

    @ApiOperation(value = "自定义数据存储接口", notes = "存储文书所有自定义数据")
    @PostMapping("/saveCustomData")
    @ResponseBody
    public R<String> saveCustomData(@RequestParam("patientId") String patientId,
                                    @RequestParam("templateCode") String templateCode,
                                    @RequestParam("operationId") String operationId,
                                    @RequestParam("tableName") String tableName,
                                    @RequestBody List<Map<String, String>> customDataList) {
        log.info(customDataList);
        try {
            templateimpl.saveCustomData(patientId, templateCode, operationId, tableName, customDataList);
        } catch (Exception e) {
            e.fillInStackTrace();
            log.error(e);
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }
        return R.data(CommonErrorCode.E0.getMsg());
    }


    @ApiOperation(value = "字典数据展示", notes = "双击实现字典数据下拉框")
    @PostMapping("/showDictionaryData")
    @ResponseBody
    public R<List<String>> showDictionaryData(@RequestParam("tableName") String tableName,
                                              @RequestParam("className") String className,
                                              @RequestParam("showContent") String showContent,
                                              @RequestParam("detailId") String detailId) {
        List<String> dictData = new ArrayList<>(10);
        try {
            dictData = templateimpl.showDictionaryData(tableName, showContent, className, detailId);
        } catch (Exception e) {
            e.fillInStackTrace();
            log.error(CommonErrorCode.E100018.getMsg(), e);
            return R.fail(CommonErrorCode.E100018.getCode(), CommonErrorCode.E100018.getMsg());
        }
        return R.data(dictData);
    }

}

package com.acis.controller.cxx.postoperativeInfoSystem;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.service.cxx.postoperativeInfo.Impl.postoperativeInfoCoreImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description: 术后麻醉评分功能
 * @author: cxx
 * @date: 2020/6/11
 */
@Log4j2
@Api(tags = "术后麻醉评分功能")
@RestController
@RequestMapping("/acis/posIS/posAnesthesiaGrade")
public class posAnesthesiaGradeController {

    @Autowired
    postoperativeInfoCoreImpl postoperativeinfocoreimpl;

    @ApiOperation("术后麻醉评分功能-controller")
    @GetMapping("/test")
    @ResponseBody
    public R<String> test(@RequestParam("maxSalary") Integer maxSalary) {
        return R.data("sucess");
    }

    @ApiOperation(value = "术后麻醉评分功能-评分项展示", notes = "1.提供麻醉复苏（Steward苏醒评分）2.提供疼痛评分。")
    @PostMapping("/access")
    @ResponseBody
    public R<List<Map<String, Object>>> access(@RequestParam("gradingTypeId") String gradingTypeId) {
        List<Map<String, Object>> access = postoperativeinfocoreimpl.access(gradingTypeId);
        if (access == null) {
            return R.fail(CommonErrorCode.E700003.getCode(), CommonErrorCode.E700003.getMsg());
        }
        return R.data(access);
    }

    @ApiOperation(value = "术后麻醉评分功能-回显患者已填评分项", notes = "患者评分项的回显")
    @PostMapping("/echoInfo")
    @ResponseBody
    public R<List<Map<String, String>>> echoInfo(@RequestParam("operationId") String operationId,
                                                 @RequestParam("patientId") String patientId,
                                                 @RequestParam("anesthesiaScoreId") String anesthesiaScoreId) {
        List<Map<String, String>> resultList = postoperativeinfocoreimpl.echoInfo(operationId, patientId, anesthesiaScoreId);
        if (resultList == null) {
            return R.fail(CommonErrorCode.E700003.getCode(), CommonErrorCode.E700003.getMsg());
        }
        return R.data(resultList);
    }


    @ApiOperation(value = "术后麻醉评分功能-评分接口", notes = "计算患者的总评分")
    @PostMapping("/scoreCalculation")
    @ResponseBody
    public R<Map<String, String>> saveInfo(@RequestParam("anesthesiaScoreId") String anesthesiaScoreId,
                                           @RequestParam("operationId") String operationId,
                                           @RequestParam("patientId") String patientId,
                                           @RequestBody List<Map<String, String>> Info) {
        try {
            boolean IsAnesthesiaScoreId = StringUtils.isBlank(anesthesiaScoreId);
            if (IsAnesthesiaScoreId){
                return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
            }
            boolean IsOperationId= StringUtils.isBlank(operationId);
            if (IsOperationId){
                return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
            }
            boolean IsPatientId = StringUtils.isBlank(patientId);
            if (IsPatientId){
                return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
            }
            postoperativeinfocoreimpl.saveInfo(anesthesiaScoreId, operationId, patientId, Info);
            Map<String, String> anesthesiaGradeCore = postoperativeinfocoreimpl.posAnesthesiaGradeCore(operationId, anesthesiaScoreId, patientId,"0");
            return R.data(anesthesiaGradeCore);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return R.fail(CommonErrorCode.E100029.getCode(), CommonErrorCode.E100029.getMsg());
    }


    @ApiOperation(value = "术后麻醉评分功能-患者总评分展示", notes = "患者总评分展示")
    @PostMapping("/showTotalScore")
    @ResponseBody
    public R<Map<String, String>> showTotalScore(@RequestParam("anesthesiaScoreId") String anesthesiaScoreId,
                                    @RequestParam("operationId") String operationId,
                                    @RequestParam("patientId") String patientId) {
        try {
            boolean IsAnesthesiaScoreId = StringUtils.isBlank(anesthesiaScoreId);
            if (IsAnesthesiaScoreId){
                return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
            }
            boolean IsOperationId= StringUtils.isBlank(operationId);
            if (IsOperationId){
                return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
            }
            boolean IsPatientId = StringUtils.isBlank(patientId);
            if (IsPatientId){
                return R.fail(CommonErrorCode.E100022.getCode(),CommonErrorCode.E100022.getMsg());
            }
            Map<String, String> anesthesiaGradeCore = postoperativeinfocoreimpl.posAnesthesiaGradeCore(operationId, anesthesiaScoreId, patientId,"1");

            return R.data(anesthesiaGradeCore);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return R.fail(CommonErrorCode.E100029.getCode(), CommonErrorCode.E100029.getMsg());
    }
}

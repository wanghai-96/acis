package com.acis.controller.lyf.medRecordManage;


import com.acis.common.exception.R;
import com.acis.pojo.lyf.vo.request.BloodGasAnalysisVo;
import com.acis.service.lyf.medRecordManage.BloodGasAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "血气分析")
@RestController
@RequestMapping("/acis/blood")
public class BloodGasAnalysisController {

    @Autowired
    private BloodGasAnalysisService bloodGasAnalysisService;

    @ApiOperation("查询血气分析字典")
    @GetMapping("/getBloodGasAnalysisDict")
    public R getBloodGasAnalysisDict() {
        List<Map<String, String>> bloodGasDict = bloodGasAnalysisService.getBloodGasAnalysisDict();
        return R.data(bloodGasDict);
    }

    @ApiOperation("查询某患者血气分析记录（时间列表）")
    @GetMapping("/getBloodGasAnalysisRecordTime/{operationId}")
    public R getBloodGasAnalysisRecordTime(@PathVariable String operationId) {
        List<Map<String, Object>> timeList = bloodGasAnalysisService.getBloodGasAnalysisRecordTime(operationId);
        return R.data(timeList);
    }

    @ApiOperation("查询某患者某条血气分析详情")
    @GetMapping("/getBloodGasAnalysisRecordDetail/{recordId}")
    public R getBloodGasAnalysisRecordDetail(@PathVariable String recordId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> recordList = bloodGasAnalysisService.getBloodGasAnalysisRecordDetail(recordId);
        if (null != recordList && recordList.size() > 0) {
            String analysisTime = recordList.get(0).get("analysisTime");
            result.put("analysisTime", analysisTime);
            result.put("recordList", recordList);
        }
        return R.data(result);
    }

    @ApiOperation("保存血气分析")
    @PostMapping("/addBloodGasAnalysisRecord")
    public R addBloodGasAnalysisRecord(@RequestBody BloodGasAnalysisVo bloodGasAnalysisVo) {
        String i = bloodGasAnalysisService.addBloodGasAnalysisRecord(bloodGasAnalysisVo);
        if ("0".equals(i)) {
            return R.fail("fail");
        }
        return R.data(i);
    }

    @ApiOperation("编辑血气分析")
    @PutMapping("/updateBloodGasAnalysisRecord")
    public R updateBloodGasAnalysisRecord(@RequestBody BloodGasAnalysisVo bloodGasAnalysisVo) {
        Integer i = bloodGasAnalysisService.updateBloodGasAnalysisRecord(bloodGasAnalysisVo);
        if (0 == i) {
            return R.fail("fail");
        }
        return R.success("success");
    }

    @ApiOperation("删除血气分析")
    @DeleteMapping("/deleteBloodGasAnalysisRecord/{recordId}")
    public R deleteBloodGasAnalysisRecord(@PathVariable String recordId) {
        Integer i = bloodGasAnalysisService.deleteBloodGasAnalysisRecord(recordId);
        if (0 == i) {
            return R.fail("fail");
        }
        return R.success("success");
    }
}

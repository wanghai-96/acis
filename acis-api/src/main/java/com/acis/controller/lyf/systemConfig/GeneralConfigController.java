package com.acis.controller.lyf.systemConfig;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.pojo.AcisIntraoMonitorDataDict;
import com.acis.pojo.AcisSystemConfigBlood;
import com.acis.pojo.AcisSystemConfigNormalDrug;
import com.acis.pojo.AcisSystemLiquidProperty;
import com.acis.pojo.lyf.vo.request.*;
import com.acis.pojo.lyf.vo.response.*;
import com.acis.service.lyf.systemConfig.GeneralConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "一般配置")
@RestController
@RequestMapping("/acis/generalConfig")

public class GeneralConfigController {

    @Autowired
    private GeneralConfigService generalConfigService;

    @ApiOperation("手术申请信息查询")
    @GetMapping("/getSysMenu/{type}")
    public R getSysMenu(@PathVariable String type) {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-常规信息查询")
    @GetMapping("/getNormalConfig/{type}")
    public R getNormalConfig(@PathVariable String type) {
        try {
            List<NormalConfigVo> configList = generalConfigService.getNormalConfig(type);
            return R.data(configList);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "常规信息查询失败!");
        }
    }

    @ApiOperation("一般配置-常规信息编辑")
    @PutMapping("/saveNormalConfig")
    public R saveNormalConfig(@RequestBody List<NormalConfigVo> updateInfos) {
        generalConfigService.saveNormalConfig(updateInfos);
        return R.data(CommonErrorCode.E0);
    }



    @ApiOperation("一般配置-液体属性选项查询")
    @GetMapping("/getLiquidProperty")
    public R<List<LiquidPropertyVo>> getLiquidProperty() {

        List<LiquidPropertyVo> liquidPropertyVos = generalConfigService.getLiquidProperty();
        return R.data(liquidPropertyVos);
    }

    @ApiOperation("一般配置-液体属性选项编辑")
    @PutMapping("/saveLiquidProperty")
    public R saveLiquidProperty(@RequestBody List<LiquidPropertyVo> liquidPropertyList) {
        Integer i = generalConfigService.saveLiquidProperty(liquidPropertyList);
        if (i == 0) {
            return R.fail(CommonErrorCode.E100017.getCode(), "液体属性选项编辑失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    /**
     * 暂不用
     * @return
     */
    @ApiOperation("一般配置-文书正反面打印配置查询")
    @GetMapping("/getWritPrintConfig")
    public R getWritPrintConfig() {

        return R.success(ResultCode.SUCCESS);
    }

    /**
     * 暂不用
     * @return
     */
    @ApiOperation("一般配置-文书正反面打印配置保存")
    @PutMapping("/saveWritPrintConfig")
    public R saveWritPrintConfig() {

        return R.success(ResultCode.SUCCESS);
    }

    /**
     * 暂不用
     * @return
     */
    @ApiOperation("一般配置-文书正反面打印配置删除")
    @DeleteMapping("/deleteWritPrintConfig")
    public R deleteWritPrintConfig() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-用药显示格式")
    @GetMapping("/getDrugShowType/{type}")
    public R<List<SystemConfigNormalDrugVo>> getDrugShowType(@PathVariable Integer type) {
        List<SystemConfigNormalDrugVo> configNormalDrugs = generalConfigService.getDrugShowType(type);
        return R.data(configNormalDrugs);
    }

    @ApiOperation("一般配置-血气分析选项查询")
    @GetMapping("/getBloodInfo")
    public R<List<ConfigBloodVo>> getBloodInfo(@RequestParam(value = "name",required = false)String name) {

        List<ConfigBloodVo> configBloodVos = generalConfigService.getBloodInfo(name);
        return R.data(configBloodVos);
    }

    @ApiOperation("一般配置-修改血气分析选项")
    @PutMapping("/updateBloodState")
    public R updateBloodState(@RequestBody List<ConfigBloodVo> bloodAnalyzeList) {
        Integer i = generalConfigService.updateBloodState(bloodAnalyzeList);
        if (i == 0) {
            return R.fail(CommonErrorCode.E100017.getCode(), "修改血气分析选项失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-查询体征报警选项")
    @GetMapping("/getAlarmConfig")
    public R<List<AlarmConfigVo>> getAlarmConfig() {

        List<AlarmConfigVo> alarmConfigList = generalConfigService.getAlarmConfig();
        return R.data(alarmConfigList);
    }

    @ApiOperation("一般配置-新增体征报警配置")
    @PostMapping("/addAlarmConfig")
    public R addAlarmConfig(@RequestBody List<AlarmConfigVo> alarmConfigList) {

        Integer i = generalConfigService.addAlarmConfig(alarmConfigList);
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-修改体征报警配置")
    @PutMapping("/updateAlarmConfig")
    public R updateAlarmConfig(@RequestBody List<AlarmConfigVo> alarmConfigList) {

        Integer i = generalConfigService.updateAlarmConfig(alarmConfigList);
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-删除体征报警配置")
    @DeleteMapping("/deleteAlarmConfig")
    public R deleteAlarmConfig(@RequestBody List<String> alarmCode) {

        if (null != alarmCode && alarmCode.size() > 0) {
            for (String s : alarmCode) {
                Integer i = generalConfigService.deleteAlarmConfig(s);
            }
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-查询文书打印及上传配置")
    @GetMapping("/getWritUploadConfig")
    public R getWritUploadConfig() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-修改文书打印及上传配置")
    @GetMapping("/updateWritUploadConfig")
    public R updateWritUploadConfig(@RequestBody List<WritUploadConfigVo> writUploadConfigList) {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-查询选择上传文书")
    @GetMapping("/getWritToUpload")
    public R getWritToUpload(@RequestBody List<WritUploadVo> writUploadVoList) {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-选择上传文书")
    @PutMapping("/chooseWritToUpload")
    public R chooseWritToUpload(@RequestBody List<WritUploadVo> writUploadVoList) {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("一般配置-监护仪列表查询")
    @GetMapping("/getMonitorList")
    public R getMonitorList(@RequestParam("type") String type) {
        List<AcisInstrumentVo> instrumentVos = generalConfigService.selectByInstrument(type);
        return R.data(instrumentVos);
    }

    @ApiOperation("获取系统配置信息")
    @GetMapping("/getSystemConfigInfo")
    public R getSystemConfigInfo() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("查询体征字典")
    @GetMapping("/getSignList")
    public R<List<MonitorDataDictVo>> getSignList(@RequestParam(value = "name", required = false) String name) {
        List<MonitorDataDictVo> monitorDataDictVos = generalConfigService.getSignList(name);
        return R.data(monitorDataDictVos);
    }

    @ApiOperation("获取his体征列表")
    @GetMapping("/getHisSignList")
    public R<List<HisSignVo>> getHisSignList() {
        List<HisSignVo> hisSignList = generalConfigService.getHisSignList();
        return R.data(hisSignList);
    }
}

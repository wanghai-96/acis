package com.acis.controller.intraoperative;

import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.dao.intraoperative.DictModeMapper;
import com.acis.pojo.intraoperative.dto.dict.AcisIntraoPharmacyDictDTO;
import com.acis.pojo.intraoperative.vo.dict.*;
import com.acis.service.intraoperative.DictModeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description  获取字典功能
 */
@Api(tags = "获取字典功能")
@RestController
@CrossOrigin
@Log4j2
@RequestMapping("/acis/intraoperative/dict")
public class DictModeController {

    @Autowired
    private DictModeService dictModeService;

    @ApiOperation("获得术中事件字典信息")
    @GetMapping("/getAcisIntraoEventDict")
    public R<List<AcisIntraoEventDictVO>> getAcisIntraoEventDict(){
        List<AcisIntraoEventDictVO> list = dictModeService.getAcisIntraoEventDictLsit();
        return R.data(list, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中体征字典信息")
    @GetMapping("/getAnesthesiaMonitorDict")
    public R<List<AcisAnesthesiaMonitorDictVO>> getAnesthesiaMonitorDict(@ApiParam("查询条件") @RequestParam(value = "condition",required = false)String condition){
        List<AcisAnesthesiaMonitorDictVO> list = dictModeService.getAcisIntraoMonitorDataDict(condition);
        return R.data(list, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得个性化体征模板下拉菜单")
    @GetMapping("/getAnesthesiaMonitorDataPersonTempleteList")
    public R<List<AcisMonitorPersonalListVO>> getAnesthesiaMonitorDataPersonTempleteList(){
        List<AcisMonitorPersonalListVO> list = dictModeService.getAcisMonitorPersonList();
        return R.data(list, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得个性化体征监测数据模板详情")
    @GetMapping("/getAnesthesiaMonitorPersonalDetails")
    public R<List<AcisMonitorPersonalListDetailVO>> getAnesthesiaMonitorPersonalDetails(@ApiParam("模板code") @RequestParam(value = "templeteCode",required = true) Integer templeteCode){
        List<AcisMonitorPersonalListDetailVO> list = dictModeService.getAcisMonitorPersonListDetails(templeteCode);
        return R.data(list, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("添加个性化体征监测数据模板详情")
    @PostMapping("/addAnesthesiaMonitorPersonalDetail")
    public R<Integer> addAnesthesiaMonitorPersonalDetail(@ApiParam("模板code") @RequestParam(value = "templeteCode",required = true) Integer templeteCode,
                                                         @ApiParam("监测项目code") @RequestParam(value = "itemCode",required = true) String itemCode,
                                                        @ApiParam("监测项目名称") @RequestParam(value = "itemName",required = true) String itemName){
        String templeteName = null;
        try {
            switch (templeteCode){
                case 1 : templeteName="体征模板1";
                break;
                case 2 : templeteName="体征模板2";
                break;
                case 3 : templeteName="体征模板3";
                break;
                case 4 : templeteName="监测模板";
                break;
                default:
                    //错误码警示
                    throw new ACISException(CommonErrorCode.E900004);
            }
        } catch (ACISException e) {
            e.printStackTrace();
        }
        Integer result = dictModeService.insertAcisMonitorPersonalListDetail(itemCode,itemName, templeteCode,templeteName);
        return R.data(result, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("删除个性化体征监测数据模板详情")
    @PostMapping("/delAnesthesiaMonitorPersonalDetail")
    public R<Integer> delAnesthesiaMonitorPersonalDetail(@ApiParam("模板code") @RequestParam(value = "templeteCode",required = true) Integer templeteCode,
                                                @ApiParam("监测项目code") @RequestParam(value = "itemCode",required = true)String itemCode){
        Integer result = dictModeService.delCountFromAcisMonitorPersonalListDetail(itemCode, templeteCode);
        return R.data(result, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中麻药用药下拉菜单")
    @GetMapping("/getAcisIntraoPharmacyMedicineDictList")
    public R<List<AcisIntraoPharmacyDictVO>> getAcisIntraoPharmacyMedicineDictList(@RequestParam(value = "eventName",required = false)String eventName){
        List<AcisIntraoPharmacyDictVO> list = null;
        try {
            list = dictModeService.getAcisIntraoPharmacyMedicineDictList(eventName);
            if(null==list){
                //非空报错判断
                throw new ACISException(CommonErrorCode.E100018);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(list, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中输血输液下拉菜单")
    @GetMapping("/getAcisIntraoPharmacyBloodDictList")
    public R<List<AcisIntraoPharmacyDictVO>> getAcisIntraoPharmacyBloodDictList(@RequestParam(value = "eventName",required = false)String eventName){
        List<AcisIntraoPharmacyDictVO> list = null;
        try {
            list = dictModeService.getAcisIntraoPharmacyBloodDictList(eventName);
            if(null==list){
                //非空报错判断
                throw new ACISException(CommonErrorCode.E100018);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(list, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得心电图字典信息")
    @GetMapping("/getHeartBeatPicDict")
    public R<List<AcisHeartBeatPicVO>> getHeartBeatPicDict(){
        R<List<AcisHeartBeatPicVO>> result;
        try {
            result = dictModeService.getHeartBeatPicDict();
            if(null==result){
                return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(CommonErrorCode.E600006.getMsg());
            throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return result;
    }

    @ApiOperation("获得手术体位字典信息")
    @GetMapping("/getPositionDict")
    public R getPositionDict(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术前诊断字典信息")
    @GetMapping("/getDiagnoseBeforeOperationDict")
    public R getDiagnoseBeforeOperationDict(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得科室字典信息")
    @GetMapping("/getDeptDict")
    public R getDeptDict(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得手术器材字典信息")
    @GetMapping("/getEquipmentDict")
    public R getEquipmentDict(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得医院医护字典信息")
    @GetMapping("/getHisUserDict")
    public R getHisUserDict(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }
}

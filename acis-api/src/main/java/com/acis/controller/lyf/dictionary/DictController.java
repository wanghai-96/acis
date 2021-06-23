package com.acis.controller.lyf.dictionary;

import com.acis.common.constants.CacheKeyEnum;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.common.util.redis.RedisUtil;
import com.acis.dao.lyf.AcisSystemConfigNormalMapper;
import com.acis.pojo.lyf.vo.request.*;
import com.acis.pojo.lyf.vo.response.*;
import com.acis.service.lyf.dictionary.DictService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author STEVEN LEE
 * @date 2020/6/4
 */
@Api(tags = "字典")
@RestController
@RequestMapping("/acis/dict")
public class DictController {

    protected final static Logger logger = LoggerFactory.getLogger(DictController.class);

    @Autowired
    private DictService dictService;
    @Autowired
    private AcisSystemConfigNormalMapper configNormalMapper;
    @Autowired
    private RedisUtil redisUtil;

    @ApiOperation("体征字典查询")
    @GetMapping("/getSignDict")
    public R getSignDict() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("常用术语列表查询")
    @GetMapping("/getCommonDict")
    public R<List<AcisDictCommonVo>> getCommonDict() {

        List<AcisDictCommonVo> dictCommonList = dictService.getCommonDict();

        return R.data(dictCommonList);
    }

    @ApiOperation("常用术语详情查询")
    @GetMapping("/getCommonDictdetails/{itemCode}")
    public R<List<AcisDictCommonDetailVo>> getCommonDictdetails(@PathVariable String itemCode) {

        List<AcisDictCommonDetailVo> commonDetailVoList = dictService.getCommonDictdetails(itemCode);

        return R.data(commonDetailVoList);
    }

    @ApiOperation("编辑常用术语详情")
    @PutMapping("/updateCommonDictdetails")
    public R updateCommonDictdetails(@RequestBody @Validated List<CommonDetailDictVo> detailDictList) {

        Integer i = dictService.updateCommonDictdetails(detailDictList);
        if (i == 0) {
            logger.error("编辑常用术语详情失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑常用术语详情失败");
        }

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("新增常用术语详情")
    @PostMapping("/addCommonDictdetails")
    public R addCommonDictdetails(@RequestBody @Validated CommonDetailDictVo detailDict) {

        Integer i = dictService.addCommonDictdetails(detailDict);
        if (i == 0) {
            logger.error("新增常用术语详情失败！");
            return R.fail(CommonErrorCode.E100019.getCode(), "新增常用术语详情失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除常用术语详情")
    @DeleteMapping("/deleteCommonDictdetails/{detailId}/{itemCode}")
    public R deleteCommonDictdetails(@PathVariable String detailId, @PathVariable String itemCode) {

        Integer i = dictService.deleteCommonDictdetails(detailId, itemCode);
        if (i == 0) {
            logger.error("删除常用术语详情失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "删除常用术语详情失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("麻醉事件列表查询")
    @GetMapping("/getAnesEventDict")
    public R<List<AnesEventVo>> getAnesEventDict() {

        try {
            List<AnesEventVo> anesEventVoList = dictService.getAnesEventDict();
            return R.data(anesEventVoList);
        } catch (Exception e) {
            logger.error("麻醉事件列表查询失败");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "麻醉事件列表查询失败");
        }

    }

    @ApiOperation("麻醉事件详情查询")
    @GetMapping("/getAnesEventDetails/{eventCode}")
    public R<List<DictAnesEventDetailVO1>> getAnesEventDetails(@PathVariable String eventCode) {

        try {
            List<DictAnesEventDetailVO1> eventDetailList = dictService.getAnesEventDetails(eventCode);
            return R.data(eventDetailList);
        } catch (Exception e) {
            logger.error("麻醉事件详情查询失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "麻醉事件详情查询失败");
        }
    }

    @ApiOperation("编辑麻醉事件详情")
    @PutMapping("/updateAnesEventDetails")
    public R updateAnesEventDetails(@RequestBody List<AnesEventDetailVo1> anesEventDetailList) {

        Integer i = dictService.updateAnesEventDetails(anesEventDetailList);
        if (i == 0) {
            logger.error("编辑麻醉事件详情失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑麻醉事件详情失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("新增麻醉事件详情")
    @PostMapping("/addAnesEventDetails")
    public R addAnesEventDetails(@RequestBody AnesEventDetailVo1 anesEventDetail) {
        Integer i = dictService.addAnesEventDetails(anesEventDetail);
        if (i == 0) {
            logger.error("新增麻醉事件详情失败！");
            return R.fail(CommonErrorCode.E100019.getCode(), "新增麻醉事件详情失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除麻醉事件详情")
    @DeleteMapping("/deleteAnesEventDetails/{detailCode}/{eventCode}")
    public R deleteAnesEventDetails(@PathVariable String detailCode, @PathVariable String eventCode) {

        Integer i = dictService.deleteAnesEventDetails(detailCode, eventCode);
        if (i == 0) {
            logger.error("删除麻醉事件详情失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除麻醉事件详情失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    //@ApiOperation("收费分类下拉框列表查询")
    @GetMapping("/getChargeTypeList")
    public R getChargeTypeList() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("单位下拉框列表查询")
    @GetMapping("/getUnitList/{itemCode}")
    public R<List<Map<String, String>>> getUnitList(@PathVariable String itemCode) {
        List<Map<String, String>> itemList = dictService.getItemList(itemCode);
        return R.data(itemList);
    }

    @ApiOperation("浓度单位下拉框列表查询")
    @GetMapping("/getConUnitList")
    public R<List<Map<String, String>>> getConUnitList() {
        List<Map<String, String>> itemList = dictService.getItemList("D002");
        return R.data(itemList);
    }

    @ApiOperation("速度单位下拉框列表查询")
    @GetMapping("/getSpeedUnitList")
    public R<List<Map<String, String>>> getSpeedUnitList() {
        List<Map<String, String>> itemList = dictService.getItemList("D003");
        return R.data(itemList);
    }

    @ApiOperation("剂量单位下拉框列表查询")
    @GetMapping("/getDosageUnitList")
    public R<List<Map<String, String>>> getDosageUnitList() {
        List<Map<String, String>> itemList = dictService.getItemList("D001");
        return R.data(itemList);
    }

    @ApiOperation("用药途径下拉框列表查询")
    @GetMapping("/getDrugUseTypeList")
    public R<List<Map<String, String>>> getDrugUseTypeList() {
        List<Map<String, String>> itemList = dictService.getItemList("D004");
        return R.data(itemList);
    }

    //@ApiOperation("药品属性下拉框列表查询")
    @GetMapping("/getDrugPropertyList")
    public R getDrugPropertyList() {

        return R.success(ResultCode.SUCCESS);
    }

    //@ApiOperation("医嘱分类下拉框列表查询")
    @GetMapping("/getAdviceTypeList")
    public R getAdviceTypeList() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("麻醉方法字典查询")
    @GetMapping("/getAnesMethodList")
    public R<List<AnesMethodVo>> getAnesMethodList() {

        List<AnesMethodVo> anesMethodList = dictService.getAnesMethodList();
        return R.data(anesMethodList);
    }

    @ApiOperation("麻醉方法字典查询 分页")
    @GetMapping("/getAnesMethodDict")
    public R getAnesMethodDict(@RequestParam(value = "start") Integer start,
                               @RequestParam(value = "size") Integer size,
                               @RequestParam(value = "content", required = false) String content) {

        PageInfo<AnesMethodVo> pageInfo = dictService.getAnesMethodDict(size, start, content);
        return R.data(null == pageInfo ? new ArrayList<>() : pageInfo);
    }
    @ApiOperation("编辑麻醉方法字典")
    @PutMapping("/updateAnesMethodList")
    public R updateAnesMethodList(@RequestBody List<AnesMethodVo> anesMethod) {

        Integer i = dictService.updateAnesMethodList(anesMethod);
        if (i == 0) {
            logger.error("编辑麻醉方法字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑麻醉方法字典失败！");
        }
        redisUtil.del(CacheKeyEnum.ANESMETHOD.getName());
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("新增麻醉方法字典")
    @PostMapping("/addAnesMethodList")
    public R addAnesMethodList(@RequestBody AnesMethodVo anesMethod) {

        Integer i = dictService.addAnesMethodList(anesMethod);
        if (i == 0) {
            logger.error("添加麻醉方法字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "添加麻醉方法字典失败！");
        }
        redisUtil.del(CacheKeyEnum.ANESMETHOD.getName());
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除麻醉方法字典")
    @DeleteMapping("/deleteAnesMethodList/{anesCode}")
    public R deleteAnesMethodList(@PathVariable String anesCode) {
        Integer i = dictService.deleteAnesMethodList(anesCode);
        if (i == 0) {
            logger.error("删除麻醉方法字典失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除麻醉方法字典失败！");
        }
        redisUtil.del(CacheKeyEnum.ANESMETHOD.getName());
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("采集仪器字典查询")
    @GetMapping("/getInstrumentList")
    public R<List<AcisDictInstrumentVo>> getInstrumentList() {
        List<AcisDictInstrumentVo> dictInstrumentList = dictService.getInstrumentList();
        return R.data(dictInstrumentList);
    }

    @ApiOperation("编辑采集仪器字典")
    @PutMapping("/updateInstrumentList")
    public R updateInstrumentList(@RequestBody List<AcisDictInstrumentVo> instrumentDict) {

        Integer i = dictService.updateInstrumentList(instrumentDict);
        if (i == 0) {
            logger.error("编辑采集仪器字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑采集仪器字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("新增采集仪器字典")
    @PostMapping("/addInstrumentList")
    public R addInstrumentList(@RequestBody AcisDictInstrumentVo instrumentDict) {

        Integer i = dictService.addInstrumentList(instrumentDict);
        if (i == 0) {
            logger.error("新增采集仪器字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "新增采集仪器字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除采集仪器字典")
    @DeleteMapping("/deleteInstrumentList/{instrumentCode}")
    public R deleteInstrumentList(@PathVariable String instrumentCode) {

        Integer i = dictService.deleteInstrumentList(instrumentCode);
        if (i == 0) {
            logger.error("删除采集仪器字典失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除采集仪器字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("手术间字典查询")
    @GetMapping("/getOpeRoomList")
    public R<Map<String, Object>> getOpeRoomList() {
        //查询手术间字典
        List<AcisDictRoomVo> roomList = dictService.getOpeRoomList();
        //查询手术室代码
        String code = "N003";
        String roomCode = configNormalMapper.getRoomCode(code);
        Map<String, Object> map = new HashMap<>();
        map.put("roomList", roomList);
        map.put("roomCode", roomCode);
        return R.data(map);
    }

    @ApiOperation("新增手术间字典")
    @PostMapping("/addOpeRoomList")
    public R addOpeRoomList(@RequestBody AcisDictRoomVo roomDict) {

        Integer i = dictService.addOpeRoomList(roomDict);
        if (i == 0) {
            logger.error("新增手术间字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "新增手术间字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("编辑手术间字典")
    @PutMapping("/updateOpeRoomList")
    public R updateOpeRoomList(@RequestBody List<AcisDictRoomVo> roomDict) {

        Integer i = dictService.updateOpeRoomList(roomDict);
        if (i == 0) {
            logger.error("编辑采集仪器字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑采集仪器字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除手术间字典")
    @DeleteMapping("/deleteOpeRoomList/{roomNo}")
    public R deleteOpeRoomList(@PathVariable String roomNo) {

        Integer i = dictService.deleteOpeRoomList(roomNo);
        if (i == 0) {
            logger.error("删除手术间字典失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除手术间字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("医生护士字典查询")
    @GetMapping("/getDoctorNurseList1")
    public R<List<DictHisUserVo>> getDoctorNurseList1(@RequestParam("userJob") String userJob,
                                                      @RequestParam(value = "code", required = false) String code) {
        List<DictHisUserVo> userList = dictService.getDoctorNurseList(userJob, code);
        return R.data(userList);
    }

    @ApiOperation("医生护士字典查询")
    @GetMapping("/getDoctorNurseList/{userJob}")
    public R<List<DictHisUserVo>> getDoctorNurseList(@PathVariable String userJob) {
        List<DictHisUserVo> userList = dictService.getDoctorNurseList(userJob, "");
        return R.data(userList);
    }

    @ApiOperation("医生护士字典查询分页")
    @GetMapping("/getDoctorNurseListPaging")
    public R<PageInfo<DictHisUserVo>> getDoctorNurseListPaging(@RequestParam("userJob") String userJob,
                                                               @RequestParam("start") Integer start,
                                                               @RequestParam("size") Integer size,
                                                               @RequestParam(value = "content", required = false) String content) {

        PageInfo<DictHisUserVo> userList = dictService.getDoctorNurseListPaging(userJob, size, start, content);
        return R.data(userList);
    }

    @ApiOperation("新增医生护士字典")
    @PostMapping("/addDoctorNurseList")
    public R addDoctorNurseList(@RequestBody DictHisUserVo hisUserDict) {

        Integer i = dictService.addDoctorNurseList(hisUserDict);
        if (i == 0) {
            logger.error("新增医生护士字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "新增医生护士字典失败！");
        }
        redisUtil.del(CacheKeyEnum.ANESDOC.getName());
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("编辑医生护士字典")
    @PutMapping("/updateDoctorNurseList")
    public R updateDoctorNurseList(@RequestBody List<DictHisUserVo> hisUserDict) {

        Integer i = dictService.updateDoctorNurseList(hisUserDict);
        if (i == 0) {
            logger.error("编辑医生护士字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑医生护士字典失败！");
        }
        redisUtil.del(CacheKeyEnum.ANESDOC.getName());
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除医生护士字典")
    @DeleteMapping("/deleteDoctorNurseList/{userId}")
    public R deleteDoctorNurseList(@PathVariable String userId) {

        Integer i = dictService.deleteDoctorNurseList(userId);
        if (i == 0) {
            logger.error("删除医生护士字典失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除医生护士字典失败！");
        }
        redisUtil.del(CacheKeyEnum.ANESDOC.getName());
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("科室列表查询")
    @GetMapping("/getDeptList")
    public R<List<AcisDictDeptVo>> getDeptList(@RequestParam(value = "name", required = false) String name) {
        List<AcisDictDeptVo> deptList = dictService.getDeptList1(name);
        return R.data(deptList);
    }

    @ApiOperation("科室列表查询")
    @GetMapping("/getDeptList1")
    public R<PageInfo<AcisDictDeptVo>> getDeptList1(@RequestParam(value = "name", required = false) String name,
                                                    @RequestParam("start") Integer start,
                                                    @RequestParam("size") Integer size) {
        PageInfo<AcisDictDeptVo> deptList = dictService.getDeptList(name, start, size);
        return R.data(deptList);
    }

    @ApiOperation("诊断字典查询")
    @GetMapping("/getDiagnoseList")
    public R<List<DictDiagnoseVo>> getDiagnoseList() {

        List<DictDiagnoseVo> diagnoseList = dictService.getDiagnoseList();
        return R.data(diagnoseList);
    }

    @ApiOperation("新增诊断字典")
    @PostMapping("/addDiagnoseList")
    public R addDiagnoseList(@RequestBody DictDiagnoseVo diagnoseDict) {

        Integer i = dictService.addDiagnoseList(diagnoseDict);
        if (i == 0) {
            logger.error("新增诊断字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "新增诊断字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("编辑诊断字典")
    @PutMapping("/updateDiagnoseList")
    public R updateDiagnoseList(@RequestBody List<DictDiagnoseVo> diagnoseDict) {

        Integer i = dictService.updateDiagnoseList(diagnoseDict);
        if (i == 0) {
            logger.error("编辑诊断字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑诊断字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除诊断字典")
    @DeleteMapping("/deleteDiagnoseList/{diagCode}")
    public R deleteDiagnoseList(@PathVariable String diagCode) {

        Integer i = dictService.deleteDiagnoseList(diagCode);
        if (i == 0) {
            logger.error("删除诊断字典失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除诊断字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("手术字典查询")
    @GetMapping("/getOperationList")
    public R getOperationList(@RequestParam("start") Integer start,
                              @RequestParam("size") Integer size,
                              @RequestParam(value = "content", required = false) String content) {

        PageInfo<DictOperationVo> operationList = dictService.getOperationList(start, size, content);
        return R.data(operationList);
    }

    @ApiOperation("新增手术字典")
    @PostMapping("/addOperationList")
    public R addOperationList(@RequestBody DictOperationVo operationDict) {

        Integer i = dictService.addOperationList(operationDict);
        if (i == 0) {
            logger.error("新增手术字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "新增手术字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("编辑手术字典")
    @PutMapping("/updateOperationList")
    public R updateOperationList(@RequestBody List<DictOperationVo> operationDict) {

        Integer i = dictService.updateOperationList(operationDict);
        if (i == 0) {
            logger.error("编辑手术字典失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑手术字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("删除手术字典")
    @DeleteMapping("/deleteOperationList/{opeCode}")
    public R deleteOperationList(@PathVariable String opeCode) {

        Integer i = dictService.deleteOperationList(opeCode);
        if (i == 0) {
            logger.error("删除手术字典失败！");
            return R.fail(CommonErrorCode.E900005.getCode(), "删除手术字典失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("麻醉常用量查询")
    @GetMapping("/getAnesUsualDose/{eventCode}")
    public R<List<DictAnesEventDetailVO2>> getAnesUsualDose(@PathVariable String eventCode) {

        try {
            List<DictAnesEventDetailVO2> eventDetailList = dictService.getAnesEventDetails1(eventCode);
            return R.data(eventDetailList);
        } catch (Exception e) {
            logger.error("麻醉常用量查询失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "麻醉常用量查询失败");
        }
    }

    @ApiOperation("编辑麻醉常用量")
    @PutMapping("/updateAnesUsualDose")
    public R updateAnesUsualDose(@RequestBody List<AnesEventDetailVo2> anesEventDetailList) {

        Integer i = dictService.updateAnesUsualDose(anesEventDetailList);
        if (i == 0) {
            logger.error("编辑麻醉常用量失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑麻醉常用量失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("新增麻醉常用量")
    @PostMapping("/addAnesUsualDose")
    public R addAnesUsualDose(@RequestBody AnesEventDetailVo2 anesEventDetail) {
        Integer i = dictService.addAnesUsualDose(anesEventDetail);
        if (i == 0) {
            logger.error("新增麻醉常用量失败！");
            return R.fail(CommonErrorCode.E100019.getCode(), "新增麻醉常用量失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("手术状态字典查询")
    @GetMapping("/getOpeStateDict")
    public R<List<OpeConditionVo>> getOpeStateDict() {
        List<OpeConditionVo> opeConditionVos = dictService.getOpeStateDict();
        return R.data(opeConditionVos);
    }

    @ApiOperation("麻醉医生字典模糊查询  分页")
    @GetMapping("/getAnesDocList")
    public R getAnesDocList(@RequestParam(value = "content",required = false)String content,
                            @RequestParam(value = "startPage")Integer startPage,
                            @RequestParam(value = "pageSize")Integer pageSize) {

        PageInfo<Map<String, Object>> pageInfo = dictService.getAnesDocList(content, startPage, pageSize);

        return R.data(null == pageInfo ? new ArrayList<>() : pageInfo);
    }

}

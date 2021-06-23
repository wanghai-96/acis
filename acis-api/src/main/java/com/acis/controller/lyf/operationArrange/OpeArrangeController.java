package com.acis.controller.lyf.operationArrange;


import com.acis.common.constants.CacheKeyEnum;
import com.acis.common.constants.OpeArrangeEnum;
import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.common.util.dateUtils.DateUtil;
import com.acis.common.util.redis.RedisUtil;
import com.acis.common.util.strUtils.SysIntegrationUtil;
import com.acis.pojo.*;
import com.acis.pojo.lyf.dto.MedMainDoctorDto;
import com.acis.pojo.lyf.dto.MedMainRoomDto;
import com.acis.pojo.lyf.dto.SendOrderDTO;
import com.acis.pojo.lyf.vo.request.*;
import com.acis.pojo.lyf.vo.response.*;
import com.acis.service.lyf.dictionary.DictService;
import com.acis.service.lyf.operationArrange.OpeArrangeLogService;
import com.acis.service.lyf.systemConfig.OpeArrangeService;
import com.acis.service.userpermissions.security.SecurityUserHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author STEVEN LEE
 * @date 2020/6/5 11:58
 */
@Api(tags = "手术排班")
@RestController
@RequestMapping("/acis/operation")
@CrossOrigin
public class OpeArrangeController {
    protected final static Logger logger = LoggerFactory.getLogger(OpeArrangeController.class);
    @Autowired
    private OpeArrangeService opeArrangeService;
    @Autowired
    private OpeArrangeLogService opeArrangeLogService;
    @Autowired
    private DictService dictService;
    @Autowired
    private RedisUtil redisUtil;


    @ApiOperation("手术申请信息查询")
    @GetMapping("/getOpeApplyInfo/{opeScheduleTime}/{floor}")
    public R<List<OpeApplyInfoVo>> getOpeApplyInfo(@PathVariable String opeScheduleTime, @PathVariable String floor) {

        if ("0".equals(floor)) {
            floor = "";
        }
        List<OpeApplyInfoVo> applyInfoList = opeArrangeService.getOpeApplyInfo(opeScheduleTime, floor);

        return R.data(applyInfoList);
    }

    /**
     * 手术申请信息同步
     *
     * @param start
     * @param end
     * @param deptCode
     * @return
     */
    @ApiOperation("手术申请信息同步")
    @GetMapping("/syncOpeApplyInfo")
    public R syncOpeApplyInfo(@RequestParam("start") Integer start,
                              @RequestParam("end") Integer end,
                              @RequestParam(value = "deptCode", required = false) String deptCode) {

        if (null == deptCode || "".equals(deptCode)) {
            deptCode = "-1";
        }
        Boolean isSuccess = SysIntegrationUtil.syncOpeApplyInfo(start, end, deptCode);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "手术申请信息同步失败");
        }

        return R.success(ResultCode.SUCCESS);
    }

    //@ApiOperation("患者基本信息查询")
    @GetMapping("/getPatMasterIndex")
    public R getPatMasterIndex(@RequestParam("start") Integer start, @RequestParam("end") Integer end) {


        return R.success(ResultCode.SUCCESS);
    }

    /**
     * 患者基本信息同步
     *
     * @param start
     * @param end
     * @return
     */
    @ApiOperation("患者基本信息同步")
    @GetMapping("/syncPatMasterIndex")
    public R syncPatMasterIndex(@RequestParam("start") Integer start, @RequestParam("end") Integer end) {
        Boolean isSuccess = SysIntegrationUtil.syncPatMasterIndex(start, end);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "患者基本信息同步失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("麻醉医生列表查询")
    @GetMapping("/getAnesDocList/{date}")
    public R<List<MedMainDoctorVo>> getAnesDocList(@PathVariable String date) {
        List<MedMainDoctorDto> medMainDoctorDTOS = opeArrangeService.getMainDoctorListByDate(date);
        List<MedMainDoctorVo> medMainDoctorVOS = new ArrayList<>();
        for (MedMainDoctorDto medMainDoctorDTO : medMainDoctorDTOS) {
            MedMainDoctorVo medMainDoctorVO = new MedMainDoctorVo();
            //医生id编号
            medMainDoctorVO.setUserId(medMainDoctorDTO.getUserId());
            //医生职位
            medMainDoctorVO.setUserJob(medMainDoctorDTO.getUserJob());
            //最大手术台次
            medMainDoctorVO.setMaxOperoomCount(medMainDoctorDTO.getMaxOperoomCount());
            //默认手术间
            medMainDoctorVO.setDefaultOperoomNo(medMainDoctorDTO.getDefaultOperoomNo());
            //医生姓名
            medMainDoctorVO.setUserName(medMainDoctorDTO.getUserName());
            //默认手术间
            medMainDoctorVO.setDefaultOperoomNo(medMainDoctorDTO.getDefaultOperoomNo());
            //序号
            medMainDoctorVO.setOrderNumber(medMainDoctorDTO.getOrderNumber());
            //返回结果
            medMainDoctorVO.setResult(medMainDoctorDTO.getResult());
            //医生被安排台次
            medMainDoctorVO.setCount(medMainDoctorDTO.getCount());
            medMainDoctorVOS.add(medMainDoctorVO);
        }
        return R.data(medMainDoctorVOS);
    }

    @ApiOperation("护士列表查询")
    @GetMapping("/getOpeNurseList/{date}")
    public R<List<MedMainDoctorVo>> getOpeNurseList(@PathVariable String date) {
        List<MedMainDoctorDto> medMainDoctorDTOS = opeArrangeService.getMainNurseListByDate(date);
        List<MedMainDoctorVo> medMainDoctorVOS = new ArrayList<>();
        for (MedMainDoctorDto medMainDoctorDTO : medMainDoctorDTOS) {
            MedMainDoctorVo medMainDoctorVO = new MedMainDoctorVo();
            //医生id编号
            medMainDoctorVO.setUserId(medMainDoctorDTO.getUserId());
            //医生职位
            medMainDoctorVO.setUserJob(medMainDoctorDTO.getUserJob());
            //最大手术台次
            medMainDoctorVO.setMaxOperoomCount(medMainDoctorDTO.getMaxOperoomCount());
            //默认手术间
            medMainDoctorVO.setDefaultOperoomNo(medMainDoctorDTO.getDefaultOperoomNo());
            //医生姓名
            medMainDoctorVO.setUserName(medMainDoctorDTO.getUserName());
            //默认手术间
            medMainDoctorVO.setDefaultOperoomNo(medMainDoctorDTO.getDefaultOperoomNo());
            //序号
            medMainDoctorVO.setOrderNumber(medMainDoctorDTO.getOrderNumber());
            //返回结果
            medMainDoctorVO.setResult(medMainDoctorDTO.getResult());
            //医生被安排台次
            medMainDoctorVO.setCount(medMainDoctorDTO.getCount());
            medMainDoctorVOS.add(medMainDoctorVO);
        }
        return R.data(medMainDoctorVOS);
    }

    @ApiOperation("正在排班手术信息查询")
    @GetMapping("/getOpeArrangingList/{opeRoom}/{opeScheduleTime}")
    public R<List<OpeArrangingVo>> getOpeArrangingList(@PathVariable String opeRoom, @PathVariable String opeScheduleTime) {

        List<OpeArrangingVo> opeArrangingList = opeArrangeService.getOpeArrangingList(opeRoom, opeScheduleTime);
        return R.data(opeArrangingList);
    }

    @ApiOperation("切换信息查询")
    @GetMapping("/getOpeInfoInArrange/{opeScheduleTime}")
    public R<List<OpeInfoInArrageVo>> getOpeInfoInArrange(@PathVariable String opeScheduleTime) {
        try {
            List<OpeInfoInArrageVo> opeInfoInArrageVos = opeArrangeService.getOpeInfoInArrange(opeScheduleTime);
            return R.data(opeInfoInArrageVos);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "切换信息查询失败");
        }

    }

    @ApiOperation("某一条手术排班详细信息保存")
    @PutMapping("/updateOpeArrangeInfo")
    public R updateOpeArrangeInfo(@RequestBody OpeArrangeInfoVo opeArrangeInfo) {

        if (null == opeArrangeInfo.getOperationId() || "".equals(opeArrangeInfo.getOperationId())) {
            logger.error("手术id不能为空！");
        }
        int i = opeArrangeService.updateOpeArrangeInfo(opeArrangeInfo);

        if (i == 0) {
            logger.error("信息保存失败!");
            return R.fail("信息保存失败，请重试");
        }

        return R.success(ResultCode.SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("安排手术间并生成日志")
    @PutMapping("/arrangeOpeRoom/{opeRoom}/{operationId}/{opeScheduleTime}")
    public R arrangeOpeRoom(@PathVariable String opeRoom, @PathVariable String operationId, @PathVariable String opeScheduleTime) {

        //在排班之前判断该手术间是否达到最大手术台数
        Integer maxOpeNum = dictService.selectMaxOpeNum(opeRoom);
        Integer opeNum = opeArrangeService.selectOpeNum(opeRoom, opeScheduleTime);
        if (opeNum >= maxOpeNum) {
            return R.fail("该手术间已达最大手术台数！");
        }

        //判断是否有该手术申请
        AcisOpeApplyInfo opeApplyInfo = opeArrangeService.selectPtIdByOpeId(operationId);
        if (null == opeApplyInfo || "".equals(opeApplyInfo)) {
            return R.fail("没有该手术申请！");
        }

        //查询日志，将分配给该手术间的麻醉医生和护士信息获取出来
        List<AcisOpeLogInfo> logInfoList = opeArrangeLogService.getLogByRoomAndDate(opeRoom, opeScheduleTime);

        AcisOpeScheduleInfo opeScheduleInfo = new AcisOpeScheduleInfo();
        //添加患者id
        opeScheduleInfo.setPatientId(opeApplyInfo.getPatientId());
        //手术间
        opeScheduleInfo.setOpeRoom(opeRoom);
        //获取当前手术间楼层
        String floor = opeRoom.substring(0, 1);
        //手术id
        opeScheduleInfo.setOperationId(operationId);
        //台次
        opeScheduleInfo.setSequence(String.valueOf(opeNum + 1));

        //保存手术安排信息
        try {
            Integer i = opeArrangeService.insertOpeArrangeInfo(opeScheduleInfo, logInfoList);
            //修改手术申请信息楼层
            opeArrangeService.updateFloor(floor, operationId);
            //将手术申请信息的状态设为已排班
            Integer state = 1;
            Integer j = opeArrangeService.updateStateInApply(operationId, state);
        } catch (Exception e) {
            logger.error(CommonErrorCode.E100019.getMsg());
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }

        try {
            //生成并添加日志
            AcisOpeLogInfo logInfo = new AcisOpeLogInfo();
            //添加手术安排操作人员
            //logInfo.setCreator(SecurityUserHelper.getCurrentUser().getUserId());
            //添加操作日期
            Date time = new Date();
            logInfo.setExecuteTime(time);
            //添加日志内容
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = sdf.format(time);
            String message = format + " 手术医师：" + opeApplyInfo.getSurgeon() + ",申请科室：" + opeApplyInfo.getDeptCode() + ",患者姓名：" + opeApplyInfo.getVisitId() + "，手术名称：" + opeApplyInfo.getOpeNameBefore() + "，分配到" + opeRoom + "号手术间";
            logInfo.setMessage(message);
            //添加手术日期
            logInfo.setOpeScheduleTime(opeApplyInfo.getOpeScheduleTime());
            //添加手术间
            logInfo.setOpeRoom(opeRoom);
            //添加患者id
            logInfo.setPatientId(opeApplyInfo.getPatientId());
            //添加手术id
            logInfo.setOperationId(operationId);
            //添加日志状态
            logInfo.setStatus(false);
            //添加日志类型
            logInfo.setType(0);
            //添加更新字段
            logInfo.setUpdateColumn("");
            //添加旧值
            logInfo.setOldValue("");
            //添加新值
            logInfo.setNewValue("");
            Integer i1 = opeArrangeLogService.insertLog(logInfo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonErrorCode.E100019.getMsg());
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("安排主麻并生成日志")
    @PutMapping("/arrangeFirstAnesDoc/{opeRoom}/{userId}/{result}/{opeScheduleTime}")
    public R arrangeFirstAnesDoc(@PathVariable String opeRoom, @PathVariable String userId, @PathVariable String result, @PathVariable String opeScheduleTime) {

        try {
            //将已安排手术间并未提交的手术安排麻醉医生
            Integer i = opeArrangeService.arrangeAnesDoc(opeRoom, userId, opeScheduleTime);

            //将之前的医生安排日志记录状态改为1
            String column = OpeArrangeEnum.ANESDOC.getName();
            Integer i1 = opeArrangeLogService.updateLogState(opeRoom, opeScheduleTime, column);

            //添加日志
            AcisOpeLogInfo logInfo = new AcisOpeLogInfo();
            //添加手术安排操作人员
            //logInfo.setCreator(SecurityUserHelper.getCurrentUser().getUserId());
            //添加操作日期
            Date time = new Date();
            logInfo.setExecuteTime(time);
            //添加日志内容
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = sdf.format(time);
            String message = format + " 主麻医师：" + result + " 分配到" + opeRoom + "号手术间";
            logInfo.setMessage(message);
            //添加手术日期
            logInfo.setOpeScheduleTime(DateUtil.parse(opeScheduleTime, "yyyy-MM-dd"));
            //添加手术间
            logInfo.setOpeRoom(opeRoom);
            //添加患者id
            logInfo.setPatientId("");
            //添加手术id
            logInfo.setOperationId("");
            //添加日志状态
            logInfo.setStatus(false);
            //添加日志类型
            logInfo.setType(1);
            //添加更新字段
            logInfo.setUpdateColumn(column);
            //添加旧值
            logInfo.setOldValue("");
            //添加新值
            logInfo.setNewValue(userId);

            Integer i2 = opeArrangeLogService.insertLog(logInfo);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(CommonErrorCode.E100019.getMsg());
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("安排副麻并生成日志")
    @PutMapping("/arrangeOtherAnesDoc/{opeRoom}/{userId}/{result}/{opeScheduleTime}")
    public R arrangeOtherAnesDoc(@PathVariable String opeRoom, @PathVariable String userId, @PathVariable String result, @PathVariable String opeScheduleTime) {

        try {
            //判断该医生是否已分配为主麻
            List<String> anesDoc = opeArrangeService.selectAnesDoc(opeRoom, opeScheduleTime);
            if (null != anesDoc && anesDoc.size() != 0) {
                for (String s : anesDoc) {
                    if (userId.equals(s)) {
                        return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
                    }
                }
            }

            //判断该医生是否已分配为第一副麻
            List<String> firstAnesDoc = opeArrangeService.selectFirstAnesDoc(opeRoom, opeScheduleTime);
            boolean f = true;
            for (String s1 : firstAnesDoc) {
                if (userId.equals(s1)) {
                    return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
                }
                if (null != s1 && !s1.equals("")) {
                    f = false;
                    break;
                }
            }

            if (f) {
                try {
                    //安排第一副麻
                    Integer i1 = opeArrangeService.insertFirstAnesDoc(userId, opeRoom, opeScheduleTime);
                    //生成日志
                    String column = OpeArrangeEnum.FIRSTANESDOC.getName();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = sdf.format(new Date());
                    String message = format + " 副麻医师：" + result + " 分配到" + opeRoom + "号手术间";
                    Integer type = 1;
                    Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(CommonErrorCode.E100019.getMsg());
                    return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                }
                return R.success(ResultCode.SUCCESS);
            } else {
                //判断该医生是否已分配为第二副麻
                List<String> secAnesDoc = opeArrangeService.selectSecAnesDoc(opeRoom, opeScheduleTime);
                boolean s = true;
                for (String s2 : secAnesDoc) {
                    if (userId.equals(s2)) {
                        return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
                    }
                    if (null != s2 && !s2.equals("")) {
                        s = false;
                        break;
                    }
                }
                if (s) {
                    try {
                        //安排第二副麻
                        Integer i1 = opeArrangeService.insertSecAnesDoc(userId, opeRoom, opeScheduleTime);
                        //生成日志
                        String column = OpeArrangeEnum.SECANESDOC.getName();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format = sdf.format(new Date());
                        String message = format + " 副麻医师：" + result + " 分配到" + opeRoom + "号手术间";
                        Integer type = 1;
                        Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error(CommonErrorCode.E100019.getMsg());
                        return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                    }
                    return R.success(ResultCode.SUCCESS);
                } else {
                    try {
                        //安排第三副麻
                        Integer i1 = opeArrangeService.insertThirdAnesDoc(userId, opeRoom, opeScheduleTime);
                        //生成日志
                        String column = OpeArrangeEnum.THIRDANESDOC.getName();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String format = sdf.format(new Date());
                        String message = format + " 副麻医师：" + result + " 分配到" + opeRoom + "号手术间";
                        Integer type = 1;
                        Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error(CommonErrorCode.E100019.getMsg());
                        return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                    }
                    return R.success(ResultCode.SUCCESS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "安排副麻并生成日志失败！");
        }
    }

    private Integer saveLog(String opeRoom, String opeScheduleTime, String column, String message, String userId, Integer type) {
        //将之前的医生安排日志记录状态改为1
        Integer i1 = opeArrangeLogService.updateLogState(opeRoom, opeScheduleTime, column);
        //添加日志
        AcisOpeLogInfo logInfo = new AcisOpeLogInfo();
        //添加手术安排操作人员
        //logInfo.setCreator(SecurityUserHelper.getCurrentUser().getUserId());
        //添加操作日期
        Date time = new Date();
        logInfo.setExecuteTime(time);
        //添加日志内容
        logInfo.setMessage(message);
        //添加手术日期
        logInfo.setOpeScheduleTime(DateUtil.parse(opeScheduleTime, "yyyy-MM-dd"));
        //添加手术间
        logInfo.setOpeRoom(opeRoom);
        //添加患者id
        logInfo.setPatientId(null);
        //添加手术id
        logInfo.setOperationId(null);
        //添加日志状态
        logInfo.setStatus(false);
        //添加日志类型
        logInfo.setType(type);
        //添加更新字段
        logInfo.setUpdateColumn(column);
        //添加旧值
        logInfo.setOldValue(null);
        //添加新值
        logInfo.setNewValue(userId);

        Integer i2 = opeArrangeLogService.insertLog(logInfo);

        return i2;
    }

    @ApiOperation("安排洗手护士并生成日志")
    @PutMapping("/arrangeOpeNurse/{opeRoom}/{userId}/{result}/{opeScheduleTime}")
    public R arrangeOpeNurse(@PathVariable String opeRoom, @PathVariable String userId, @PathVariable String opeScheduleTime, @PathVariable String result) {

        //判断该护士是否已分配为第一洗手护士
        List<String> first = opeArrangeService.selectFirstOpeNurse(opeRoom, opeScheduleTime);

        boolean f = true;
        for (String s1 : first) {
            if (userId.equals(s1)) {
                return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
            }
            if (null != s1 && !"".equals(s1)) {
                f = false;
                break;
            }
        }
        if (f) {
            try {
                //安排第一洗手护士
                Integer i1 = opeArrangeService.insertFirstOpeNurse(userId, opeRoom, opeScheduleTime);
                //生成日志
                String column = OpeArrangeEnum.FIRSTOPENURSE.getName();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(new Date());
                String message = format + " 洗手护士：" + result + " 分配到" + opeRoom + "号手术间";
                Integer type = 1;
                Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(CommonErrorCode.E100019.getMsg());
                return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
            }
            return R.success(ResultCode.SUCCESS);
        } else {
            //判断该护士是否已分配为第二洗手护士
            List<String> sec = opeArrangeService.selectSecOpeNurse(opeRoom, opeScheduleTime);
            boolean g = true;
            for (String s1 : first) {
                if (userId.equals(s1)) {
                    return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
                }
                if (null != s1 && !s1.equals("")) {
                    g = false;
                    break;
                }
            }
            if (g) {
                try {
                    //安排第二洗手护士
                    Integer i1 = opeArrangeService.insertSecOpeNurse(userId, opeRoom, opeScheduleTime);
                    //生成日志
                    String column = OpeArrangeEnum.SECOPENURSE.getName();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = sdf.format(new Date());
                    String message = format + " 洗手护士：" + result + " 分配到" + opeRoom + "号手术间";
                    Integer type = 1;
                    Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(CommonErrorCode.E100019.getMsg());
                    return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                }
                return R.success(ResultCode.SUCCESS);
            } else {
                try {
                    //安排第二洗手护士
                    Integer i1 = opeArrangeService.insertThirdOpeNurse(userId, opeRoom, opeScheduleTime);
                    //生成日志
                    String column = OpeArrangeEnum.THIRDOPENURSE.getName();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = sdf.format(new Date());
                    String message = format + " 洗手护士：" + result + " 分配到" + opeRoom + "号手术间";
                    Integer type = 1;
                    Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(CommonErrorCode.E100019.getMsg());
                    return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                }
                return R.success(ResultCode.SUCCESS);
            }
        }
    }

    @ApiOperation("安排巡回护士并生成日志")
    @PutMapping("/arrangeSupplyNurse/{opeRoom}/{userId}/{result}/{opeScheduleTime}")
    public R arrangeSupplyNurse(@PathVariable String opeRoom, @PathVariable String userId, @PathVariable String result, @PathVariable String opeScheduleTime) {
        //判断该护士是否已分配为第一巡回护士
        List<String> first = opeArrangeService.selectFirstSupplyNurse(opeRoom, opeScheduleTime);

        boolean f = true;
        for (String s1 : first) {
            if (userId.equals(s1)) {
                return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
            }
            if (null != s1 && !"".equals(s1)) {
                f = false;
                break;
            }
        }
        if (f) {
            try {
                //安排第一巡回护士
                Integer i1 = opeArrangeService.insertFirstSupplyNurse(userId, opeRoom, opeScheduleTime);
                //生成日志
                String column = OpeArrangeEnum.FIRSTOPENURSE.getName();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = sdf.format(new Date());
                String message = format + " 巡回护士：" + result + " 分配到" + opeRoom + "号手术间";
                Integer type = 1;
                Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(CommonErrorCode.E100019.getMsg());
                return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
            }
            return R.success(ResultCode.SUCCESS);
        } else {
            //判断该护士是否已分配为第二巡回护士
            List<String> sec = opeArrangeService.selectSecSupplyNurse(opeRoom, opeScheduleTime);
            boolean g = true;
            for (String s1 : first) {
                if (userId.equals(s1)) {
                    return R.fail(CommonErrorCode.E700002.getCode(), CommonErrorCode.E700002.getMsg());
                }
                if (null != s1 && !s1.equals("")) {
                    g = false;
                    break;
                }
            }
            if (g) {
                try {
                    //安排第二巡回护士
                    Integer i1 = opeArrangeService.insertSecSupplyNurse(userId, opeRoom, opeScheduleTime);
                    //生成日志
                    String column = OpeArrangeEnum.SECOPENURSE.getName();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = sdf.format(new Date());
                    String message = format + " 巡回护士：" + result + " 分配到" + opeRoom + "号手术间";
                    Integer type = 1;
                    Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(CommonErrorCode.E100019.getMsg());
                    return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                }
                return R.success(ResultCode.SUCCESS);
            } else {
                try {
                    //安排第二巡回护士
                    Integer i1 = opeArrangeService.insertThirdSupplyNurse(userId, opeRoom, opeScheduleTime);
                    //生成日志
                    String column = OpeArrangeEnum.THIRDOPENURSE.getName();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = sdf.format(new Date());
                    String message = format + " 巡回护士：" + result + " 分配到" + opeRoom + "号手术间";
                    Integer type = 1;
                    Integer i2 = saveLog(opeRoom, opeScheduleTime, column, message, userId, type);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(CommonErrorCode.E100019.getMsg());
                    return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
                }
                return R.success(ResultCode.SUCCESS);
            }
        }
    }

    @ApiOperation("房态图信息查询")
    @GetMapping("/getMainRoomList/{opeScheduleTime}")
    public R<List<MedMainRoomDto>> getMainRoomList(@PathVariable String opeScheduleTime) {
        if (null == opeScheduleTime || "".equals(opeScheduleTime)) {
            logger.error("手术时间不能为空！");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术时间不能为空");
        }
        List<MedMainRoomDto> roomInfoVOList = opeArrangeService.getRoomInfoList(opeScheduleTime);
        if (null == roomInfoVOList || roomInfoVOList.size() == 0) {
            logger.error("房态图信息查询失败");
            return R.fail(CommonErrorCode.E100001.getCode(), "房态图信息查询失败");
        }
        return R.data(roomInfoVOList);
    }

    @ApiOperation("手术提交")
    @PutMapping("/submitRoomSchedule/{opeScheduleTime}")
    public R submitRoomSchedule(@PathVariable String opeScheduleTime) {

        if (null == opeScheduleTime || "".equals(opeScheduleTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "手术时间不能为空");
        }
        try {
            //修改手术状态为2
            int i = opeArrangeService.submitRoomSchedule(opeScheduleTime);
            //将安排手术间生成的日志作废
            opeArrangeLogService.updateLogStateByScheduleTime(opeScheduleTime);
        } catch (Exception e) {
            logger.error("手术提交失败");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术提交失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("通过手术间号获取手术间配置信息")
    @GetMapping("/getRoomDetailByRoomNo/{roomNo}")
    public R<RoomDetailInfoVo> getRoomDetailByRoomNo(@PathVariable String roomNo) {
        RoomDetailInfoVo roomDetailInfo = opeArrangeService.getRoomDetailByRoomNo(roomNo);
        return R.data(roomDetailInfo);
    }

    @ApiOperation("通过手术间号修改手术间配置信息")
    @PostMapping("/editRoomDetailByRoomNo")
    public R editRoomDetailByRoomNo(@RequestParam("roomNo") String roomNo, @RequestParam("maxNo") String maxNo, @RequestParam("deptCode") String deptCode) {

        int i = opeArrangeService.editRoomDetailByRoomNo(roomNo, maxNo, deptCode);
        if (i == 0) {
            logger.error("手术间信息配置失败！");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术间信息配置失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("清空手术间或者医生护士安排并生成日志")
    @PostMapping("/clearArrange/{type}")
    public R clearArrange(@RequestBody List<AcisOpeLogInfoVo> logList,
                          @PathVariable Integer type) {
        try {
            logger.info("清空手术间或者医生护士安排并生成日志");
            List<Integer> sysnoList = new ArrayList<>();
            if (OpeArrangeEnum.ALL.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.ANESDOC.getName() + "&" + OpeArrangeEnum.FIRSTANESDOC.getName() + "&" + OpeArrangeEnum.SECANESDOC.getName() + "&" + OpeArrangeEnum.THIRDANESDOC.getName() + "&" + OpeArrangeEnum.FIRSTOPENURSE.getName() + "&" +
                            OpeArrangeEnum.SECOPENURSE.getName() + "&" + OpeArrangeEnum.THIRDOPENURSE.getName() + "&" + OpeArrangeEnum.FIRSTSUPPLYNURSE.getName() + "&" + OpeArrangeEnum.SECSUPPLYNURSE.getName() + "&" + OpeArrangeEnum.THIRDSUPPLYNURSE.getName() + "&" +
                            OpeArrangeEnum.SEQUENCE.getName() + "&" + OpeArrangeEnum.ROOM.getName();
                    //修改字段旧值
                    String oldValue = scheduleInfo.getAnesDoc() + "&" + scheduleInfo.getFirstAnesDoc() + "&" + scheduleInfo.getSecAnesDoc() + "&" + scheduleInfo.getThirdAnesDoc() + "&" + scheduleInfo.getFirstOpeNurse() + "&" + scheduleInfo.getSecOpeNurse() + "&" + scheduleInfo.getThirdOpeNurse() + "&" +
                            scheduleInfo.getFirstSupplyNurse() + "&" + scheduleInfo.getSecSupplyNurse() + "&" + scheduleInfo.getThirdSupplyNurse() + "&" + scheduleInfo.getSequence() + "&" + scheduleInfo.getOpeRoom();

                    //清空所有安排
                    Integer result = opeArrangeService.clearArrange(logInfo.getOperationId());
                    //更改手术申请信息状态
                    Integer state = 0;
                    Integer j = opeArrangeService.updateStateInApply(logInfo.getOperationId(), state);
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已从手术间" + scheduleInfo.getOpeRoom() + "清除";
                        Integer type1 = 3;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.ANESDOC.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.ANESDOC.getName();
                    //旧值
                    String oldValue = scheduleInfo.getAnesDoc();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearAnesDocArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除主麻医生的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.FIRSTANESDOC.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.FIRSTANESDOC.getName();
                    //旧值
                    String oldValue = scheduleInfo.getFirstAnesDoc();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearFirstAnesDocArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除副麻1医生的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.SECANESDOC.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.SECANESDOC.getName();
                    //旧值
                    String oldValue = scheduleInfo.getSecAnesDoc();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearSecAnesDocArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除副麻2医生的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.THIRDANESDOC.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.THIRDANESDOC.getName();
                    //旧值
                    String oldValue = scheduleInfo.getThirdAnesDoc();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearThirdAnesDocArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除副麻3医生的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.FIRSTOPENURSE.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.FIRSTOPENURSE.getName();
                    //旧值
                    String oldValue = scheduleInfo.getFirstOpeNurse();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearFirstOpeNurseArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除洗手护士1的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.SECOPENURSE.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.SECOPENURSE.getName();
                    //旧值
                    String oldValue = scheduleInfo.getSecOpeNurse();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearSecOpeNurseArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除洗手护士2的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.THIRDOPENURSE.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.THIRDOPENURSE.getName();
                    //旧值
                    String oldValue = scheduleInfo.getThirdOpeNurse();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearThirdOpeNurseArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除洗手护士3的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.FIRSTSUPPLYNURSE.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.FIRSTSUPPLYNURSE.getName();
                    //旧值
                    String oldValue = scheduleInfo.getFirstSupplyNurse();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearFirstSupplyNurseArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除巡回护士1的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.SECSUPPLYNURSE.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.SECSUPPLYNURSE.getName();
                    //旧值
                    String oldValue = scheduleInfo.getSecSupplyNurse();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearSecSupplyNurseArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除巡回护士2的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            } else if (OpeArrangeEnum.THIRDSUPPLYNURSE.getType().equals(type)) {
                for (AcisOpeLogInfoVo logInfo : logList) {
                    AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpeInfo(logInfo.getOperationId());
                    //修改字段
                    String updateColumn = OpeArrangeEnum.THIRDSUPPLYNURSE.getName();
                    //旧值
                    String oldValue = scheduleInfo.getThirdSupplyNurse();
                    //清空主麻的安排
                    Integer result = opeArrangeService.clearThirdSupplyNurseArrange(logInfo.getOperationId());
                    if (result != 0) {
                        //生成日志
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(scheduleInfo.getCreateTime());
                        String message = time + "病人姓名：" + scheduleInfo.getPatientId() + "，手术名称：" + scheduleInfo.getOpeNameAfter() + " 已清除巡回护士3的安排";
                        Integer type1 = 4;
                        Integer i = saveLog(logInfo, updateColumn, oldValue, message, type1);
                    }
                }
            }
            return R.success(ResultCode.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ACISException(CommonErrorCode.E100017.getCode(), "清空数据失败！");
        }
    }

    private Integer saveLog(AcisOpeLogInfoVo logInfo, String updateColumn, String oldValue, String message, Integer type) {

        //更新字段
        logInfo.setUpdateColumn(updateColumn);
        //旧值
        logInfo.setOldValue(oldValue);
        //日志内容
        logInfo.setMessage(message);
        //日志类型
        logInfo.setType(type);
        //操作人
        //String userId = SecurityUserHelper.getCurrentUser().getUserId();
        //logInfo.setCreator(userId);
        //生成时间
        logInfo.setExecuteTime(new Date());
        //日志状态
        logInfo.setStatus(false);

        AcisOpeLogInfo acisOpeLogInfo = new AcisOpeLogInfo();
        BeanUtils.copyProperties(logInfo, acisOpeLogInfo);
        acisOpeLogInfo.setOpeScheduleTime(DateUtil.parse(logInfo.getOpeScheduleTime(), "yyyy-MM-dd HH:mm:ss"));

        Integer integer = opeArrangeLogService.insertLog(acisOpeLogInfo);

        return integer;
    }

    @ApiOperation("撤销清空操作")
    @Transactional
    @PutMapping("/deleteLogBySysno/{id}")
    public R deleteLogBySysno(@PathVariable Integer id) {
        //通过id获取日志信息
        AcisOpeLogInfo opeLogInfo = opeArrangeLogService.getLogById(id);
        if (null == opeLogInfo) {
            return R.fail("无该日志！");
        }
        if (opeLogInfo.getStatus()) {
            return R.success("该操作已撤销！");
        }

        if (opeLogInfo.getType() == 0) {
            logger.info("撤销手术间安排");
            //删除手术间安排
            opeArrangeService.clearArrange(opeLogInfo.getOperationId());
            //将手术申请状态转为0
            Integer state = 0;
            opeArrangeService.updateStateInApply(opeLogInfo.getOperationId(), state);
            //修改日志状态
            opeArrangeLogService.updateLogStatusBySysno(id);
        } else if (opeLogInfo.getType() == 1 || opeLogInfo.getType() == 2) {
            logger.info("撤销麻醉医生护士安排");
            opeArrangeService.clearDocNurseArrange(opeLogInfo.getUpdateColumn(), opeLogInfo.getOpeScheduleTime(), opeLogInfo.getOpeRoom());
            opeArrangeLogService.updateLogStatusBySysno(id);
        } else if (opeLogInfo.getType() == 3) {
            try {
                //将手术信息保存到排班表中
                Integer j = opeArrangeService.insertOpeInfo(opeLogInfo.getOperationId(), opeLogInfo.getPatientId());
                //将旧值回填到相应字段
                int i = opeArrangeLogService.deleteLogBySysno(opeLogInfo);
                //将手术申请信息状态改为1
                Integer state = 1;
                Integer m = opeArrangeService.updateStateInApply(opeLogInfo.getOperationId(), state);
                if (i != 0) {
                    //将日志状态改为1
                    opeArrangeLogService.updateLogStatusBySysno(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("手术安排信息还原失败！");
                throw new ACISException(CommonErrorCode.E100017);
            }
        } else {
            try {
                //还原手术申请相应字段的数据
                int i = opeArrangeService.updateByLog(opeLogInfo);
                if (i != 0) {
                    //将日志状态改为1
                    opeArrangeLogService.updateLogStatusBySysno(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("手术安排信息还原失败！");
                throw new ACISException(CommonErrorCode.E100017);
            }

        }

        return R.success(ResultCode.SUCCESS);
    }

    /**
     * @param operationId
     * @return
     */
    @ApiOperation("取消某台手术安排")
    @PutMapping("/deleteRoom/{operationId}")
    public R deleteRoom(@PathVariable String operationId) {

        try {
            //删除该手术的安排信息
            Integer i = opeArrangeService.deleteOpeScheduleInfo(operationId);
            //将手术申请信息状态重新设为未安排
            Integer state = 0;
            Integer j = opeArrangeService.updateStateInApply(operationId, state);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "取消某台手术安排失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("查询日志信息")
    @GetMapping("/selectLogMessage/{opeScheduleTime}")
    public R<List<AcisOpeLogInfo>> selectLogMessage(@PathVariable String opeScheduleTime) {
        if (null == opeScheduleTime || "".equals(opeScheduleTime)) {
            logger.error("手术时间不能为空！");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术时间不能为空");
        }
        List<AcisOpeLogInfo> logInfoList = opeArrangeLogService.selectLogMessage(opeScheduleTime);
        return R.data(logInfoList);
    }

    @ApiOperation("数据交换")
    @PutMapping("/changeDataFromTwoRooms")
    public R changeRoom(@RequestBody RoomChangeVo roomChangeVO) {

        if (null == roomChangeVO.getOldRoom() || "".equals(roomChangeVO.getOldRoom())) {
            return R.fail(CommonErrorCode.E100001.getCode(), "手术间号不能为空");
        }
        if (null == roomChangeVO.getOperatingRoomNo() || "".equals(roomChangeVO.getOperatingRoomNo())) {
            return R.fail(CommonErrorCode.E100001.getCode(), "手术间号不能为空");
        }
        Boolean b = false;
        if ("1".equals(roomChangeVO.getAnesDoc()) && "1".equals(roomChangeVO.getFirstAnesDoc()) && "1".equals(roomChangeVO.getSecAnesDoc()) && "1".equals(roomChangeVO.getThirdAnesDoc()) && "1".equals(roomChangeVO.getFirstOpeNurse()) && "1".equals(roomChangeVO.getSecOpeNurse()) && "1".equals(roomChangeVO.getThirdOpeNurse()) && "1".equals(roomChangeVO.getFirstSupplyNurse()) && "1".equals(roomChangeVO.getSecSupplyNurse()) && "1".equals(roomChangeVO.getThirdSupplyNurse())) {
            b = true;
        }
        try {
            if (b) {
                //查询手术间号为oldRoom的手术id
                List<AcisOpeScheduleInfo> oldRoomList = opeArrangeService.selectRoom(roomChangeVO.getOldRoom(), roomChangeVO.getDate());
                //查询手术间号为newRoom的手术id
                List<AcisOpeScheduleInfo> newRoomList = opeArrangeService.selectRoom(roomChangeVO.getOperatingRoomNo(), roomChangeVO.getDate());

                //交换手术间
                for (AcisOpeScheduleInfo opeScheduleInfo : oldRoomList) {
                    opeScheduleInfo.setOpeRoom(roomChangeVO.getOperatingRoomNo());
                    opeArrangeService.updateRoom(opeScheduleInfo);
                }

                for (AcisOpeScheduleInfo opeScheduleInfo : newRoomList) {
                    opeScheduleInfo.setOpeRoom(roomChangeVO.getOldRoom());
                    opeArrangeService.updateRoom(opeScheduleInfo);
                }

                //查询手术间号为oldRoom的操作日志
                List<AcisOpeLogInfo> oldLogInfo = opeArrangeLogService.selectLog(roomChangeVO.getOldRoom(), roomChangeVO.getDate());
                //查询手术间号为newRoom的操作日志
                List<AcisOpeLogInfo> newLogInfo = opeArrangeLogService.selectLog(roomChangeVO.getOperatingRoomNo(), roomChangeVO.getDate());

                //将日志手术间互换
                for (AcisOpeLogInfo opeLogInfo : oldLogInfo) {
                    opeLogInfo.setOpeRoom(roomChangeVO.getOperatingRoomNo());
                    opeArrangeLogService.updateLog(opeLogInfo);
                }

                for (AcisOpeLogInfo opeLogInfo : newLogInfo) {
                    opeLogInfo.setOpeRoom(roomChangeVO.getOldRoom());
                    opeArrangeLogService.updateLog(opeLogInfo);
                }


            } else {
                if ("1".equals(roomChangeVO.getAnesDoc())) {
                    String updateColumn = OpeArrangeEnum.ANESDOC.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getFirstAnesDoc())) {
                    String updateColumn = OpeArrangeEnum.FIRSTANESDOC.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getSecAnesDoc())) {
                    String updateColumn = OpeArrangeEnum.SECANESDOC.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getThirdAnesDoc())) {
                    String updateColumn = OpeArrangeEnum.THIRDANESDOC.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getFirstOpeNurse())) {
                    String updateColumn = OpeArrangeEnum.FIRSTOPENURSE.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getSecOpeNurse())) {
                    String updateColumn = OpeArrangeEnum.SECOPENURSE.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getThirdOpeNurse())) {
                    String updateColumn = OpeArrangeEnum.THIRDOPENURSE.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getFirstSupplyNurse())) {
                    String updateColumn = OpeArrangeEnum.FIRSTSUPPLYNURSE.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getSecSupplyNurse())) {
                    String updateColumn = OpeArrangeEnum.SECSUPPLYNURSE.getName();
                    change(updateColumn, roomChangeVO);
                }
                if ("1".equals(roomChangeVO.getThirdSupplyNurse())) {
                    String updateColumn = OpeArrangeEnum.THIRDSUPPLYNURSE.getName();
                    change(updateColumn, roomChangeVO);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("更换手术间失败！");
            return R.fail(CommonErrorCode.E100017.getCode(), "更换手术间失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    private void change(String updateColumn, RoomChangeVo roomChangeVO) {
        //查询两个手术间日志更改字段信息
        AcisOpeLogInfo logInfo1 = opeArrangeLogService.selectLogByRoomAndColumn(roomChangeVO.getOldRoom(), updateColumn, roomChangeVO.getDate());
        AcisOpeLogInfo logInfo2 = opeArrangeLogService.selectLogByRoomAndColumn(roomChangeVO.getOperatingRoomNo(), updateColumn, roomChangeVO.getDate());

        //查询手术间号为oldRoom的手术id
        List<AcisOpeScheduleInfo> oldRoomList = opeArrangeService.selectRoom(roomChangeVO.getOldRoom(), roomChangeVO.getDate());

        //查询手术间号为newRoom的手术id
        List<AcisOpeScheduleInfo> newRoomList = opeArrangeService.selectRoom(roomChangeVO.getOperatingRoomNo(), roomChangeVO.getDate());

        //更换两个手术间手术信息中对应字段的值
        if (null != logInfo1) {
            //如果有日志则改为相应的值
            if (null != newRoomList && newRoomList.size() != 0) {
                for (AcisOpeScheduleInfo info : newRoomList) {
                    opeArrangeService.changeValueToOther(roomChangeVO.getOperatingRoomNo(), updateColumn, logInfo1.getNewValue(), info.getOperationId());
                }
            }
            opeArrangeLogService.changeLogToOtherByColumn(roomChangeVO.getOperatingRoomNo(), logInfo1.getId(), updateColumn);
        } else {
            //如果没有日志则清空
            if (null != newRoomList && newRoomList.size() != 0) {
                for (AcisOpeScheduleInfo info : newRoomList) {
                    opeArrangeService.changeValueToOther(roomChangeVO.getOperatingRoomNo(), updateColumn, "", info.getOperationId());
                }
            }
        }

        if (null != logInfo2) {
            //如果有日志则改为相应的值
            if (null != oldRoomList && oldRoomList.size() != 0) {
                for (AcisOpeScheduleInfo info : oldRoomList) {
                    opeArrangeService.changeValueToOther(roomChangeVO.getOldRoom(), updateColumn, logInfo2.getNewValue(), info.getOperationId());
                }
            }
            opeArrangeLogService.changeLogToOtherByColumn(roomChangeVO.getOldRoom(), logInfo2.getId(), updateColumn);
        } else {
            //如果没有日志则清空
            if (null != oldRoomList && oldRoomList.size() != 0) {
                for (AcisOpeScheduleInfo info : oldRoomList) {
                    opeArrangeService.changeValueToOther(roomChangeVO.getOldRoom(), updateColumn, "", info.getOperationId());
                }
            }

        }


    }

    @ApiOperation("更换手术间")
    @PutMapping("/changeRoom")
    public R changeDataFromTwoRooms(@RequestBody DataChangeVo dataChangeVO) {

        try {
            //从日志中获取新手术间医生护士安排
            List<AcisOpeLogInfo> logInfoList = opeArrangeLogService.selectLogByRoom(dataChangeVO.getOperatingRoomNo(), dataChangeVO.getDate());

            //查询该手术的手术信息
            AcisOpeScheduleInfo scheduleInfo = opeArrangeService.selectOpe(dataChangeVO.getOperationId());
            if (2 == scheduleInfo.getState()) {
                return R.fail(CommonErrorCode.E100003.getCode(), "该手术申请已提交，无法更换手术间！");
            }

            //将新手术间的医生护士安排放进这台手术
            scheduleInfo.setOpeRoom(dataChangeVO.getOperatingRoomNo());

            if ("0".equals(dataChangeVO.getAnesDoc())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.ANESDOC.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setAnesDoc(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getFirstAnesDoc())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.FIRSTANESDOC.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setFirstAnesDoc(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getSecAnesDoc())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.SECANESDOC.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setSecAnesDoc(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getThirdAnesDoc())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.THIRDANESDOC.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setThirdAnesDoc(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getFirstOpeNurse())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.FIRSTOPENURSE.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setFirstOpeNurse(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getSecOpeNurse())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.SECOPENURSE.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setSecOpeNurse(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getThirdOpeNurse())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.THIRDOPENURSE.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setThirdOpeNurse(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getFirstSupplyNurse())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.FIRSTSUPPLYNURSE.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setFirstSupplyNurse(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getSecSupplyNurse())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.SECSUPPLYNURSE.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setSecSupplyNurse(logInfo.getNewValue());
                    }
                }
            }
            if ("0".equals(dataChangeVO.getThirdSupplyNurse())) {
                for (AcisOpeLogInfo logInfo : logInfoList) {
                    if (OpeArrangeEnum.THIRDSUPPLYNURSE.getName().equals(logInfo.getUpdateColumn())) {
                        scheduleInfo.setThirdSupplyNurse(logInfo.getNewValue());
                    }
                }
            }
            //保存手术信息
            OpeArrangeInfoVo opeArrangeInfoVo = new OpeArrangeInfoVo();
            BeanUtils.copyProperties(scheduleInfo, opeArrangeInfoVo);
            int i = opeArrangeService.updateOpeArrangeInfo(opeArrangeInfoVo);
            if (i == 0) {
                logger.error("更换手术间失败！");
                return R.fail(CommonErrorCode.E100017.getCode(), "更换手术间失败");
            }
            return R.success(ResultCode.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "更换手术间失败！");
        }
    }

    @ApiOperation("麻醉方法列表查询")
    @GetMapping("/getAnesMethodList")
    public R getAnesMethodList(@RequestParam(value = "name", required = false) String name) {
        if (null == name || "".equals(name)) {
            //缓存中查找
            Object anesMethodList = redisUtil.get(CacheKeyEnum.ANESMETHOD.getName());
            //若缓存中没有去数据库查询并保存到缓存中
            if (null == anesMethodList || "".equals(anesMethodList)) {
                List<AcisDictAnesMethod> anesMethods = dictService.selectAnesMethodList(name);
                redisUtil.set(CacheKeyEnum.ANESMETHOD.getName(), anesMethods, 86400);
                return R.data(anesMethods);
            } else {
                return R.data(anesMethodList);
            }
        } else {
            List<AcisDictAnesMethod> anesMethods = dictService.selectAnesMethodList(name);
            return R.data(anesMethods);
        }
    }

    @ApiOperation("麻醉医生列表查询（手术排班详细信息页面）")
    @GetMapping("/getAnesDocList1")
    public R getAnesDocList1(@RequestParam(value = "name", required = false) String name) {
        String userJob = "1";
        //如果没有查询条件走缓存
        if (null == name || "".equals(name)) {
            //从缓存中查找
            Object anesDocList = redisUtil.get(CacheKeyEnum.ANESDOC.getName());
            //若缓存中没有去数据库查询并保存到缓存中
            if (null == anesDocList || "".equals(anesDocList)) {
                List<AcisDictHisUser> anesDocs = dictService.selectAnesDocs(name, userJob);
                redisUtil.set(CacheKeyEnum.ANESDOC.getName(), anesDocs, 86400);
                return R.data(anesDocs);
            } else {
                return R.data(anesDocList);
            }
        } else {
            //有查询条件走数据库
            List<AcisDictHisUser> anesDocs = dictService.selectAnesDocs(name, userJob);
            return R.data(anesDocs);
        }
    }

    @ApiOperation("护士列表查询（手术排班详细信息页面）")
    @GetMapping("/getNurseList1")
    public R getNurseList1(@RequestParam(value = "name", required = false) String name) {
        String userJob = "2";
        //如果没有查询条件走缓存
        if (null == name || "".equals(name)) {
            //从缓存中查找
            Object anesDocList = redisUtil.get(CacheKeyEnum.NURSE.getName());
            //若缓存中没有去数据库查询并保存到缓存中
            if (null == anesDocList || "".equals(anesDocList)) {
                List<AcisDictHisUser> anesDocs = dictService.selectAnesDocs(name, userJob);
                redisUtil.set(CacheKeyEnum.NURSE.getName(), anesDocs, 86400);
                return R.data(anesDocs);
            } else {
                return R.data(anesDocList);
            }
        } else {
            //有查询条件走数据库
            List<AcisDictHisUser> anesDocs = dictService.selectAnesDocs(name, userJob);
            return R.data(anesDocs);
        }
    }

    @ApiOperation("手术提交前预览")
    @GetMapping("/getOpeInfo/{opeScheduleTime}")
    public R<List<OpeInfoVo>> getOpeInfo(@PathVariable String opeScheduleTime) {
        //时间不能为空
        if (null == opeScheduleTime || "".equals(opeScheduleTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), CommonErrorCode.E100001.getMsg());
        }
        //查询手术信息
        List<OpeInfoVo> opeInfoVoList = opeArrangeService.getOpeInfo(opeScheduleTime);
        return R.data(opeInfoVoList);
    }

    @ApiOperation("手术排班表查询")
    @GetMapping("/getOpeScheduleForm/{opeScheduleTime}")
    public R<List<OpeInfoVo1>> getOpeScheduleForm(@PathVariable String opeScheduleTime) {

        //手术时间不能为空
        if (null == opeScheduleTime || "".equals(opeScheduleTime)) {
            logger.error("手术时间不能为空！");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术时间不能为空");
        }
        //查询手术信息
        List<OpeInfoVo1> opeInfoList = opeArrangeService.getOpeScheduleForm(opeScheduleTime);
        return R.data(opeInfoList);
    }

    @ApiOperation("手术通知单手术患者信息列表查询")
    @GetMapping("/getPtOpeInfoList")
    public R<List<PtOpeInfoVo>> getPtOpeInfoList(@RequestParam("opeScheduleTime") String opeScheduleTime, @RequestParam(value = "operationId", required = false) String operationId) {

        //手术时间不能为空
        if (null == opeScheduleTime || "".equals(opeScheduleTime)) {
            logger.error("手术时间不能为空！");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术时间不能为空");
        }

        List<PtOpeInfoVo> ptOpeInfoVos = opeArrangeService.getPtOpeInfoList(opeScheduleTime, operationId);

        return R.data(ptOpeInfoVos);
    }


    @ApiOperation("手术排班表导出Excel")
    @GetMapping("/opeScheduleFormToExcel")
    public R opeScheduleFormToExcel() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("手术通知单查询")
    @GetMapping("/getOpeInformForm/{operationId}")
    public R<OpeFormInfoVo> getOpeInformForm(@PathVariable String operationId) {
        //手术时间不能为空
        if (null == operationId || "".equals(operationId)) {
            logger.error("手术id不能为空！");
            return R.fail(CommonErrorCode.E100001.getCode(), "手术id不能为空");
        }

        //根据手术id查询手术通知单信息
        OpeFormInfoVo opeFormInfo = opeArrangeService.getOpeInformForm(operationId);
        if (null == opeFormInfo) {
            logger.error("手术通知单信息查询失败！");
            return R.fail(CommonErrorCode.E100002.getCode(), "手术通知单信息查询失败");
        }

        return R.data(opeFormInfo);
    }

    @ApiOperation("手术通知单转Excel")
    @GetMapping("/opeInformFormToExcel")
    public R opeInformFormToExcel() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("医生配置信息修改")
    @PutMapping("/updateDocConfigInfo")
    public R updateDocConfigInfo(@RequestBody @Validated MedMainDoctorVo doctorVo) {

        try {
            Integer i = opeArrangeService.updateDocConfigInfo(doctorVo);
            if (i != 1) {
                logger.error("医生配置信息修改失败！");
                return R.fail(CommonErrorCode.E100017.getCode(), "医生配置信息修改失败!");
            }
        } catch (Exception e) {
            logger.error("医生配置信息修改失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "医生配置信息修改失败!");
        }


        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("手术间列表查询")
    @GetMapping("/getRoomList")
    public R<List<String>> getRoomList() {

        List<String> roomList = opeArrangeService.getRoomList();

        return R.data(roomList);
    }

    @ApiOperation("术前访视麻醉方法提取")
    @GetMapping("/getAnesMethod/{operationId}")
    public R<Map<String, String>> getAnesMethod(@PathVariable String operationId) {

        Map<String, String> anesMethod = opeArrangeService.getAnesMethod(operationId);

        return R.data(anesMethod);
    }

    @ApiOperation("手术排台信息查询")
    @GetMapping("/getOpeArrangeTable")
    public R getOpeArrangeTable(@RequestParam(value = "date") String date, @RequestParam(value = "surgeon", required = false) String surgeon) {

        try {
            List<OpeArrangeTableVo> opeArrangeTableVos = opeArrangeService.getOpeArrangeTable(date, surgeon);
            return R.data(opeArrangeTableVos);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "手术排台信息查询失败！");
        }

    }

    @ApiOperation("手术排班表中修改手术间和手术台次")
    @PutMapping("/updateRoomAndSequence")
    public R updateRoomAndSequence(@RequestBody List<RoomSequence> roomSequence) {
        try {
            if (null != roomSequence) {
                for (RoomSequence sequence : roomSequence) {
                    opeArrangeService.updateRoomAndSequence(sequence);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return R.success("修改成功");
    }

    @ApiOperation("批量修改手术医护人员")
    @PutMapping("/updateDocAndNurse")
    public R updateDocAndNurse(@RequestBody DocNurseVo docNurse) {

        try {
            if (null == docNurse.getOperationIds()) {
                return R.fail("手术id不能为空");
            }
            opeArrangeService.updateDocAndNurse(docNurse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return R.success("修改成功");
    }

    @ApiOperation("单个手术间手术申请信息提交")
    @PutMapping("/submitOpeApplyOneRoom/{date}/{room}")
    public R submitOpeApplyOneRoom(@PathVariable String date, @PathVariable String room) {
        try {
            if (null == date || "".equals(date) || null == room || "".equals(room)) {
                return R.fail("日期和手术间号不能为空");
            }
            //将手术状态改为2
            opeArrangeService.submitOpeApplyOneRoom(date, room);
            //将手术安排生成的日志作废
            opeArrangeLogService.updateLogStateByScheduleTimeAndRoom(date, room);
        } catch (Exception e) {
            logger.error("手术提交失败");
            e.printStackTrace();
        }

        return R.success("提交成功");
    }


    @ApiOperation("临床护理退单后将排班状态设置为未提交")
    @PostMapping("/updateOpeApplyState/{seqNo}")
    public R updateOpeApplyState(@PathVariable String seqNo) {
        try {
            logger.info("临床护理退单后将排班状态设置为未提交");
            //将排班状态设置为未排班
            opeArrangeService.updateOpeApplyState(seqNo);
            //去除手术申请的安排情况
            opeArrangeService.deleteOpeScheduleInfo1(seqNo);
            //将手术排班生成的日志作废
            opeArrangeLogService.updateLogStatusByOperationId(seqNo);
            return R.success("成功");
        } catch (Exception e) {
            logger.error(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            e.printStackTrace();
            return R.fail("退单失败");
        }
    }

    @ApiOperation("同步手术信息到临床护理")
    @GetMapping("/syncOpeInfoToIncs/{startTime}/{endTime}")
    public R syncOpeInfoToIncs(@PathVariable String startTime, @PathVariable String endTime) {
        try {
            logger.info("同步手术信息到临床护理");
            List<SendOrderDTO> opeInfos = opeArrangeService.syncOpeInfoToIncs(startTime, endTime);
            return R.data(opeInfos);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return R.fail("同步手术信息到临床护理失败");
        }
    }
}

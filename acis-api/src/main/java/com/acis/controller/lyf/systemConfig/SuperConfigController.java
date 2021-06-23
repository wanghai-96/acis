package com.acis.controller.lyf.systemConfig;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.acis.common.constants.CacheKeyEnum;
import com.acis.common.constants.Constant;
import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.common.util.redis.RedisUtil;
import com.acis.common.util.strUtils.StringUtil;
import com.acis.pojo.*;
import com.acis.pojo.lyf.ContainerPo;
import com.acis.pojo.lyf.dto.PatientBasicInfoDto;
import com.acis.pojo.lyf.vo.request.*;
import com.acis.pojo.lyf.vo.response.*;
import com.acis.pojo.viewdocking.ViewStopParam;
import com.acis.service.intraoperative.InfoService;
import com.acis.service.lyf.SignDataCollect.MonitorDataService;
import com.acis.service.lyf.common.CommonService;
import com.acis.service.lyf.systemConfig.OpeArrangeService;
import com.acis.service.lyf.systemConfig.SuperConfigService;
import com.acis.service.userpermissions.security.SecurityUserHelper;
import com.acis.service.userpermissions.utils.SystemUserPermsUtil;
import com.acis.service.viewdocking.ViewDockingService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;


@Api(tags = "超级配置")
@RestController
@RequestMapping("/acis/superConfig")
@Log4j2
@CrossOrigin
public class SuperConfigController {

    protected final static Logger logger = LoggerFactory.getLogger(SuperConfigController.class);

    @Autowired
    private SuperConfigService superConfigService;

    @Autowired
    private OpeArrangeService opeArrangeService;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MonitorDataService monitorDataService;

    @Autowired
    private InfoService infoService;

    @Autowired
    private ViewDockingService viewDockingService;

    @ApiOperation("超级配置-查询手术状态信息")
    @GetMapping("/getOpeConConfig")
    public R<Map<String, List<AcisSystemConfigOpeCondition>>> getOpeConConfig() {

        Map<String, List<AcisSystemConfigOpeCondition>> systemConfigOpeCondition = superConfigService.selectAll();

        return R.data(systemConfigOpeCondition);
    }

    @ApiOperation("超级配置-编辑手术状态信息")
    @PutMapping("/updateOpeConConfig")
    public R updateOpeConConfig(@RequestBody List<OpeConditionVo> opeConditionList) {

        try {
            //将启用的手术状态的状态设置为true
            if (null != opeConditionList && opeConditionList.size() > 0) {
                for (OpeConditionVo conditionVo : opeConditionList) {
                    conditionVo.setState(true);
                }
            }
            //获取所有手术状态信息
            List<OpeConditionVo> all = superConfigService.getAllCondition();
            //获取两个集合差集（未使用的手术状态）
            List<OpeConditionVo> notUse = all.stream()
                    .filter(item -> !opeConditionList.stream().map(OpeConditionVo::getConCode)
                            .collect(Collectors.toList()).contains(item.getConCode()))
                    .collect(Collectors.toList());
            if (notUse.size() > 0) {
                for (OpeConditionVo conditionVo : notUse) {
                    //将未使用的手术状态的排序和状态设为0
                    conditionVo.setSort(0);
                    conditionVo.setState(false);
                }
            }
            //保存更改后手术状态信息
            if (opeConditionList != null) {
                opeConditionList.addAll(notUse);
            }
            Integer i = superConfigService.updateOpeConConfig(opeConditionList);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("编辑手术状态信息失败");
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑手术状态信息失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("超级配置-查询导航菜单配置信息")
    @GetMapping("/getOpeModuleConfig/{procedureState}")
    public R getOpeModuleConfig(@PathVariable Integer procedureState) {

        try {
            //获取容器中的信息
//            Object o = redisUtil.get(CacheKeyEnum.MODULEMENU.getName() + "_" + procedureState);
            Object o = null;
            if (null == o || "".equals(o)) {
                //默认 ,某患者,麻醉单,复苏单
                Map<String, List<AcisSystemModulePer>> opeModule = superConfigService.getOpeModule(procedureState);
                //将信息添加到容器中
//                redisUtil.set(CacheKeyEnum.MODULEMENU.getName() + "_" + procedureState, opeModule, 86400);
                return R.data(opeModule);
            }
            return R.data(o);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "主界面导航菜单栏获取失败");
        }
    }

    @ApiOperation("超级配置-编辑导航菜单配置信息")
    @PutMapping("/updateOpeModuleConfig")
    public R updateOpeModuleConfig(@RequestBody List<OpeModuleConfigVo> opeModuleConfigList) {

        try {
            Integer procedureState = opeModuleConfigList.get(0).getProcedureState();
            //获取所有导航菜单信息
            Integer i = 1;
            List<OpeModuleConfigVo> all = superConfigService.getAllOpeModule(procedureState, i);
            //获取两个集合的差集
            List<OpeModuleConfigVo> notUse = all.stream()
                    .filter(item -> !opeModuleConfigList.stream().map(OpeModuleConfigVo::getId)
                            .collect(Collectors.toList()).contains(item.getId()))
                    .collect(Collectors.toList());
            if (notUse.size() > 0) {
                for (OpeModuleConfigVo configVo : notUse) {
                    //将未使用的导航菜单的模块编号、模块名称和排序设置为“”
                    configVo.setModuleCode("");
                    configVo.setModuleName("");
                    configVo.setSort(0);
                }
            }
            //保存更改后的导航菜单信息
            opeModuleConfigList.addAll(notUse);
            Integer j = superConfigService.updateOpeModuleConfig(opeModuleConfigList);
            //清除redis中缓存
//            redisUtil.del(CacheKeyEnum.MODULEMENU.getName() + "_" + procedureState);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑导航菜单配置信息失败！");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("超级配置-查询文书菜单配置信息")
    @GetMapping("/getWritConfig/{procedureState}")
    public R getWritConfig(@PathVariable Integer procedureState) {

        try {
            //获取容器中的信息
//            Object o = redisUtil.get(CacheKeyEnum.WRITMENU.getName() + "_" + procedureState);
            Object o = null;
            if (null == o || "".equals(o)) {
                Map<String, List<WritInfoVo>> writMenu = superConfigService.getWritMenu(procedureState);
                //将信息添加到容器中
//                redisUtil.set(CacheKeyEnum.WRITMENU.getName() + "_" + procedureState, writMenu, 86400);
                return R.data(writMenu);
            }
            return R.data(o);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "查询文书菜单配置信息失败!");
        }
    }

    @ApiOperation("超级配置-编辑文书菜单配置信息")
    @PutMapping("/updateWritConfig/{procedureState}")
    public R updateWritConfig(@RequestBody List<OpeModuleConfigVo> opeModuleConfigList, @PathVariable Integer procedureState) {

        try {
            //获取已配置的文书菜单
            Integer i = 2;
            List<OpeModuleConfigVo> writConfigList = superConfigService.getAllOpeModule(procedureState, i);

            //获取配置中没有的文书
            List<OpeModuleConfigVo> notInConfig = opeModuleConfigList.stream()
                    .filter(item -> !writConfigList.stream().map(e -> e.getPerCode())
                            .collect(Collectors.toList()).contains(item.getPerCode()))
                    .collect(Collectors.toList());

            //获取修改集合中没有的文书
            List<OpeModuleConfigVo> notInUpdate = writConfigList.stream()
                    .filter(item -> !opeModuleConfigList.stream().map(e -> e.getPerCode())
                            .collect(Collectors.toList()).contains(item.getPerCode()))
                    .collect(Collectors.toList());

            //获取两个集合的交集
            List<OpeModuleConfigVo> both = opeModuleConfigList.stream()
                    .filter(item -> writConfigList.stream().map(e -> e.getPerCode())
                            .collect(Collectors.toList()).contains(item.getPerCode()))
                    .collect(Collectors.toList());

            //对配置中没有的文书进行insert
            Integer i1 = superConfigService.insertWritConfig(notInConfig, procedureState);

            //对修改集合中没有的文书sort设置为0
            Integer i2 = superConfigService.deleteWritConfig(notInUpdate);

            //对交集进行update
            Integer i3 = superConfigService.updateWritConfig(both);

//            redisUtil.del(CacheKeyEnum.WRITMENU.getName() + "_" + procedureState);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑文书菜单配置信息失败!");
        }
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("主界面导航菜单栏获取")
    @GetMapping("/getOpeModule/{procedureState}")
    public R<Object> getOpeModule(@PathVariable Integer procedureState) {
        try {
            //登录状态下,当前用户id
            String userId = SecurityUserHelper.getCurrentUser().getUserId();
            //获取容器中的信息
//            Object o = redisUtil.get(CacheKeyEnum.MODULEMENU.getName() + "_" + procedureState);
            Object o = null;
            if (null == o || "".equals(o)) {
                //默认 ,某患者,麻醉单,复苏单
                Map<String, List<AcisSystemModulePer>> opeModule = superConfigService.getOpeModule(procedureState);
                //将信息添加到容器中
//                redisUtil.set(CacheKeyEnum.MODULEMENU.getName() + "_" + procedureState, opeModule, 86400);
                return R.data(SystemUserPermsUtil.checkMenuAndParamsClass(userId, opeModule));
            } else {
                JSONObject object = SystemUserPermsUtil.checkMenuAndParamsJson(o.toString(), userId);
                return R.data(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "主界面导航菜单栏获取失败");
        }
    }

    @ApiOperation("主界面文书菜单栏获取")
    @GetMapping("/getWritMenu/{procedureState}")
    public R getWritMenu(@PathVariable Integer procedureState) {
        try {
            //登录状态下,当前用户id
            String userId = SecurityUserHelper.getCurrentUser().getUserId();
            //获取容器中的信息
//            Object o = redisUtil.get(CacheKeyEnum.WRITMENU.getName() + "_" + procedureState);
            Object o = null;
            if (null == o || "".equals(o)) {
                Map<String, List<WritInfoVo>> writMenu = superConfigService.getWritMenu(procedureState);
                //将信息添加到容器中
//                redisUtil.set(CacheKeyEnum.WRITMENU.getName() + "_" + procedureState, writMenu, 86400);
                return R.data(SystemUserPermsUtil.checkWritMenuAndParamsClass(userId, writMenu));
            } else {
                JSONObject object = SystemUserPermsUtil.checkWritMenuAndParamsJson(o.toString(), userId);
                return R.data(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.fillInStackTrace());
        }
        return null;
    }

    @ApiOperation("主界面手术状态栏获取")
    @GetMapping("/getOpeConMenu/{operationId}")
    public R getOpeConMenu(@PathVariable String operationId) {
        try {
            //获取ip地址
            String ipAddress = Inet4Address.getLocalHost().getHostAddress();
            //获取是否pacu管理程序和是否诱导室管理程序的状态
            List<Map<String, String>> map = superConfigService.getConfigByIp(ipAddress);
            List<OpeConMenuVo> opeConMenuVoList = new ArrayList<>();
            if (null != map && map.size() > 0) {
                String s1 = "";
                String s2 = "";
                for (Map<String, String> stringMap : map) {
                    //诱导室状态
                    if ("N027".equals(stringMap.get("normal_code"))) {
                        s1 = stringMap.get("normal_value");
                    }
                    //PACU状态
                    if ("N032".equals(stringMap.get("normal_code"))) {
                        s2 = stringMap.get("normal_value");
                    }
                }

                if ("1".equals(s2)) {
                    //如果pacu管理程序为启动，则只显示入复苏室出复苏室
                    opeConMenuVoList = superConfigService.getPacuConMenu(operationId);
                } else if ("1".equals(s1)) {
                    //如果诱导室管理程序为启动，则只显示入诱导室出诱导室
                    opeConMenuVoList = superConfigService.getInduceConMenu(operationId);
                } else {
                    //否则显示正常状态
                    opeConMenuVoList = superConfigService.getOpeConMenu(operationId);
                }
            } else {
                //否则显示正常状态
                opeConMenuVoList = superConfigService.getOpeConMenu(operationId);
                Iterator<OpeConMenuVo> iterator = opeConMenuVoList.iterator();
                while (iterator.hasNext()) {
                    String s = iterator.next().getConName();
                    if ("准备手术".equals(s)) {
                        iterator.remove();//使用迭代器的删除方法删除
                    }
                }
            }
            return R.data(opeConMenuVoList);
        } catch (Exception e) {
            logger.error("手术状态信息获取失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "手术状态信息获取失败");
        }
    }

    @ApiOperation("主界面手术菜单获取")
    @GetMapping("/getOpeSimpleInfoMenu")
    public R<PageInfo<OpeSimpleInfoVo>> getOpeSimpleInfoMenu(@RequestParam(value = "opeState") Integer opeState,
                                                             @RequestParam(value = "roomNo", required = false) String roomNo,
                                                             @RequestParam(value = "patientId", required = false) String patientId,
                                                             @RequestParam(value = "name", required = false) String name,
                                                             @RequestParam(value = "date") String date,
                                                             @RequestParam(value = "anesMethod", required = false) String anesMethod,
                                                             @RequestParam(value = "deadLine", required = false) String deadLine,
                                                             @RequestParam(value = "anesDoc", required = false) String anesDoc,
                                                             @RequestParam(value = "state", required = false) String state,
                                                             @RequestParam(value = "dept", required = false) String dept,
                                                             @RequestParam(value = "opeName", required = false) String opeName,
                                                             @RequestParam(value = "start") Integer start,
                                                             @RequestParam(value = "pageSize") Integer pageSize) {
        if (null == opeState) {
            logger.error("手术状态不能为空！");
            throw new ACISException(CommonErrorCode.E100001, "手术状态不能为空");
        }
        try {

            //同步代码块,进行手术主信息同步
            synchronized (this) {
                //同步手术信息 2020-12-30   演示注释掉主进程
                //viewDockingService.syncAcisOpeScheduleInfo(date);
            }
            //获取ip地址
            String ipAddress = Inet4Address.getLocalHost().getHostAddress();
            //获取是否pacu管理程序和是否诱导室管理程序的状态
            List<Map<String, String>> map = superConfigService.getConfigByIp(ipAddress);
            PageInfo<OpeSimpleInfoVo> opeSimpleInfoList = new PageInfo<>();
            if (null != map && map.size() > 0) {
                String s1 = "";
                String s2 = "";
                for (Map<String, String> stringMap : map) {
                    //诱导室状态
                    if ("N027".equals(stringMap.get("normal_code"))) {
                        s1 = stringMap.get("normal_value");
                    }
                    //PACU状态
                    if ("N032".equals(stringMap.get("normal_code"))) {
                        s2 = stringMap.get("normal_value");
                    }
                }
                if ("1".equals(s2)) {
                    //如果pacu管理程序为启动，则只显示入复苏室出复苏室状态的手术信息
                    opeSimpleInfoList = opeArrangeService.getPacuSimpleInfoMenu(opeState, roomNo, patientId, name, date, anesMethod, deadLine, anesDoc, state, dept, opeName, start, pageSize);
                } else if ("1".equals(s1)) {
                    //如果诱导室管理程序为启动，则只显示入诱导室出诱导室状态的手术信息
                    opeSimpleInfoList = opeArrangeService.getInduceSimpleInfoMenu(opeState, roomNo, patientId, name, date, anesMethod, deadLine, anesDoc, state, dept, opeName, start, pageSize);
                } else {
                    //否则显示所有状态的手术信息
                    opeSimpleInfoList = opeArrangeService.getOpeSimpleInfoMenu(opeState, roomNo, patientId, name, date, anesMethod, deadLine, anesDoc, state, dept, opeName, start, pageSize);
                }
            } else {
                //否则显示所有状态的手术信息
                opeSimpleInfoList = opeArrangeService.getOpeSimpleInfoMenu(opeState, roomNo, patientId, name, date, anesMethod, deadLine, anesDoc, state, dept, opeName, start, pageSize);
            }
            return R.data(opeSimpleInfoList);
        } catch (Exception e) {
            logger.error("手术状态信息获取失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "手术状态信息获取失败");
        }
    }

    @ApiOperation("主界面患者详情获取")
    @GetMapping("/getPtOpeDetailInfo/{operationId}")
    public R<Map<String, Map<String, Object>>> getPtOpeDetailInfo(@PathVariable String operationId) {

        try {
            if (null == operationId || "".equals(operationId)) {
                logger.error("手术id不能为空！");
                throw new ACISException(CommonErrorCode.E100001, "手术id不能为空");
            }

            Map<String, Map<String, Object>> resultMap = opeArrangeService.getPtOpeDetailInfo(operationId);

            logger.info("患者详情： " + resultMap.toString());

            return R.data(resultMap);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "主界面患者详情获取");
        }

    }

    @ApiOperation("主界面默认手术间及手术间列表获取")
    @GetMapping("/getDefaultRoom")
    public R<Map<String, Object>> getDefaultRoom() {

        try {
            //获取IP地址
            String hostAddress = Inet4Address.getLocalHost().getHostAddress();
            //获取默认手术间
            String room = superConfigService.getDefaultRoom(hostAddress);
            //获取手术间列表
            List<String> roomList = superConfigService.getRoomList();
            Map<String, Object> map = new HashMap<>(2);
            map.put("defaultRoom", room == null ? "" : room);
            map.put("roomList", roomList);
            logger.info("默认手术间： " + room);
            return R.data(map);
        } catch (Exception e) {
            logger.error("主界面默认手术间获取失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100019.getCode(), "主界面默认手术间获取失败");
        }
    }

    @ApiOperation("模块列表查询")
    @GetMapping("/getModuleList")
    public R<List<AcisSystemModule>> getModuleList() {

        List<AcisSystemModule> moduleList = superConfigService.getModuleList();
        return R.data(moduleList);
    }

    @ApiOperation("各手术状态时间保存")
    @PostMapping("/addTimePoint")
    public R addTimePoint(@RequestParam("operationId") String operationId,
                          @RequestParam("conCode") String conCode,
                          @RequestParam("timePoint") String timePoint) {
        if (null == timePoint || "".equals(timePoint) || "null".equals(timePoint)) {
            return R.fail("timePoint不能为空");
        }
        try {
            //获取手术回退次数
            String operBackCount = opeArrangeService.getOperBackCount(operationId);
            //2021-3-17 拼接当前时间  前端时间修改变成  14:00 样式 需要拼接当前年月日 和 秒值
//            String yearMonthDay = DateUtil.format(new Date(),"yyyy-MM-dd");
//            timePoint = yearMonthDay+" "+timePoint+":00";
            //判断该手术状态是否已存在，若存在则修改时间
            String operation = opeArrangeService.checkTimePointIsExist(operationId, conCode,operBackCount);
            if (null != operation && !"".equals(operation)) {
                //编辑手术状态时间点
                opeArrangeService.updateTimePoint(operationId, conCode, timePoint,operBackCount);
                //编辑事件中手术时间点
                String detailCode = "0";
                String eventCode = "E001";
                if ("6".equals(conCode)) {
                    detailCode = "1";
                } else if ("7".equals(conCode)) {
                    detailCode = "48";
                } else if ("8".equals(conCode)) {
                    detailCode = "45";
                } else if ("9".equals(conCode)) {
                    detailCode = "47";
                } else if ("10".equals(conCode)) {
                    detailCode = "49";
                } else if ("11".equals(conCode)) {
                    detailCode = "53";
                } else if ("13".equals(conCode)) {
                    detailCode = "51";
                } else if ("14".equals(conCode)) {
                    detailCode = "52";
                }
                opeArrangeService.updateEventTimePoint(operationId, eventCode, detailCode, timePoint);
            } else {
                //保存手术状态时间点
                Integer i = opeArrangeService.addTimePoint(operationId, conCode, timePoint,operBackCount);
                //更改手术状态
                Integer j = opeArrangeService.updateOpeState(operationId, conCode);
                //若状态为14出复苏室时，需将相应的复苏室床位改为闲置
                if ("14".equals(conCode)) {
                    System.out.println("关闭复苏室床位字典");
                    opeArrangeService.updatePacuBedState(operationId);
                    //将复苏室床位使用记录设置为关闭
                    System.out.println("关闭复苏室床位");
                    monitorDataService.updateBedUseState(operationId);
                }
                //若状态为麻醉结束10或者出复苏室14，则记录仪器关闭时间
                if ("10".equals(conCode) || "14".equals(conCode)) {
                    monitorDataService.updateMonitorCloseTime(operationId);
                    //这一步要结束掉手术的同步任务 执行结束手术监护仪同步
                    //2021 1.20 注释打开    此位置做演示和注解关闭
//                ViewStopParam viewStopParam = new ViewStopParam();
//                viewStopParam.setOperationId(operationId);
//                log.info("执行结束手术监护仪同步时间: {}", DateUtil.calendar(System.currentTimeMillis()).getTime());
//                //远程调用执行关闭服务
//                String result = HttpRequest.post(Constant.ANES_DATA_SERVER)
//                        .body(JSONUtil.parseObj(viewStopParam).toString())
//                        .execute().body();
//                log.info(JSONUtil.parseObj(result));
                    //若有用药没有停止  则将停止所有用药
                    infoService.stopAllDrugInUse(operationId);
                }
                //若手术状态为入手术室6，需记录事件
                if ("6".equals(conCode)) {
                    monitorDataService.addOpeStartEvent(operationId, timePoint);
                }
                //若手术状态为手术开始8，需记录事件
                if ("8".equals(conCode)) {
                    monitorDataService.addOpeStartEvent1(operationId, timePoint);
                }
                //若手术状态为手术手术结束9，需记录事件
                if ("9".equals(conCode)) {
                    monitorDataService.addOpeStartEvent2(operationId, timePoint);
                }
                //若手术状态为麻醉开始7，需记录事件
                if ("7".equals(conCode)) {
                    monitorDataService.addOpeStartEvent3(operationId, timePoint);
                }
                //若手术状态为麻醉结束10，需记录事件
                if ("10".equals(conCode)) {
                    monitorDataService.addOpeStartEvent4(operationId, timePoint);
                }
                //2020-10-13 NEON XIE 修改状态,当状态为11时,出手术室,计算对应术中的入量
                //本人声明,如果这里改废了,这个锅依然不是我背
                if ("11".equals(conCode)) {
                    //这里去计算该手术的总入量和总出量
                    R<Integer> result = infoService.setCalculateAmount(operationId);
                    if (null == result) {
                        return R.fail(CommonErrorCode.E100019.getCode(), "设置手术入量失败");
                    }
                }

                //2021-2-25 入复苏室 和 出 复苏室
                if ("11".equals(conCode)) {
                    System.out.println("出手术室");
                    monitorDataService.addOpeStartEvent7(operationId, timePoint);
                }
                if ("13".equals(conCode)) {
                    System.out.println("入复苏室");
                    monitorDataService.addOpeStartEvent5(operationId, timePoint);
                }
                if ("14".equals(conCode)) {
                    System.out.println("出复苏室");
                    monitorDataService.addOpeStartEvent6(operationId, timePoint);
                }
            }
            return R.data(conCode);
        } catch (Exception e) {
            logger.error("手术状态时间保存失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100019.getCode(), "手术状态时间保存失败");
        }
    }

    @ApiOperation("保存患者出手术室后去向")
    @PostMapping("/addDirectionAfterOpeRoom")
    public R addDirectionAfterOpeRoom(@RequestBody DirectionVo directionVo) {
        Integer i = infoService.addDirectionAfterOpeRoom(directionVo);
        if (0 == i) {
            return R.fail("fail");
        }
        return R.success("success");
    }

    @ApiOperation("系统配置常规项查询")
    @GetMapping("/getNormalConfig/{type}")
    public R getNormalConfig(@PathVariable String type) {

        try {
            List<SystemConfigNormalVo> configNormalVos = superConfigService.getNormalConfig(type);
            return R.data(configNormalVos);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "系统配置常规项配置信息获取失败！");
        }
    }

    @ApiOperation("编辑系统配置常规项")
    @PutMapping("/updateNormalConfig")
    public R updateNormalConfig(@RequestBody List<SystemConfigNormalVo> configNormalList) {
        try {
            Integer i = superConfigService.updateNormalConfig(configNormalList);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "编辑系统配置常规项失败！");
        }

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("实时状态查询")
    @GetMapping("/getRealState")
    public R<List<RealStateInfoVo>> getRealState(@RequestParam("day") String day, @RequestParam(value = "room", required = false) String room) {

        try {
            logger.info("实时状态查询");
            List<RealStateInfoVo> realStateInfoList = opeArrangeService.getRealState(day, room);
            return R.data(realStateInfoList);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "实时状态查询失败！");
        }

    }

    @ApiOperation("手术概览查询")
    @GetMapping("/getOpeView")
    public R<List<OpeViewInfoVo>> getOpeView() {
        try {
            List<OpeViewInfoVo> opeViewInfoVos = opeArrangeService.getOpeView();
            return R.data(opeViewInfoVos);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "手术概览查询失败!");
        }

    }

    @ApiOperation("分组菜单查询")
    @GetMapping("/getModule")
    public R getModule() {

        List<ModuleVo> moduleVos = superConfigService.getModule();

        return R.data(moduleVos);
    }

    @ApiOperation("文件上传配置查询")
    @GetMapping("/getFileUpload")
    public R getFileUpload() {
        AcisFileUploadPrintVo fileUpload = superConfigService.getFileUpload();
        if (fileUpload == null) {
            return R.fail(CommonErrorCode.E100018.getCode(), CommonErrorCode.E100018.getMsg());
        }
        return R.data(fileUpload);
    }

    @ApiOperation("文件上传配置更新")
    @PostMapping("/saveFileUpload")
    public R saveFileUpload(@RequestBody AcisFileUploadPrintVo fileuploadprintvo) {
        int i = superConfigService.saveFileUpload(fileuploadprintvo);
        if (i == 0) {
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }
        return R.data(CommonErrorCode.E0.getMsg());
    }

    @ApiOperation("文件上传文书回显")
    @GetMapping("/getUploadTemplateInfo")
    public R getAllUploadTemplateInfo() {
        List<AcisTemplateInfoVo> allUploadTemplateInfo = superConfigService.getAllUploadTemplateInfo();
        return R.data(allUploadTemplateInfo);
    }

    @ApiOperation("文件上传文书保存")
    @PostMapping("/saveUploadTemplateInfo")
    public R getTemplateInfo(@RequestBody List<AcisTemplateInfoVo> templateInfoVo) {
        int i = superConfigService.saveAllUploadTemplateInfo(templateInfoVo);
        return R.data(i);
    }


    @ApiOperation("获取配置项具体信息返回前端")
    @GetMapping("/getConfigInfo")
    public R getConfigInfo(@RequestParam("normalCode") String normalCode) {
        boolean IsNormalCode = StringUtils.isBlank(normalCode);
        if (IsNormalCode) {
            return R.fail(CommonErrorCode.E100001.getMsg());
        }
        String configInfo = superConfigService.selectConfigInfo(normalCode);
        if (configInfo.equals("")) {
            return R.fail(CommonErrorCode.E100018.getCode(), CommonErrorCode.E100018.getMsg());
        }
        return R.data(configInfo);
    }

    @ApiOperation("查询体征项配置信息")
    @GetMapping("/getVitalSignDict")
    public R getVitalSignDict() {
        List<Map<String, Object>> result = superConfigService.getVitalSignDict();
        return R.data(result);
    }

    @ApiOperation("编辑体征配置信息")
    @PutMapping("/updateVitalSignDict")
    public R updateVitalSignDict(@RequestBody VitalSignVo vitalSignDictVos) {
        superConfigService.updateVitalSignDict(vitalSignDictVos);
        return R.success("success");
    }

    @ApiOperation("体征字典查询")
    @GetMapping("/getVitalSignDictAll")
    public R getVitalSignDictAll() {
        List<Map<String, Object>> vitalSignDict = superConfigService.getVitalSignDictAll();
        return R.data(vitalSignDict);
    }

    @ApiOperation("通过ip查询手术间号")
    @GetMapping("/getRoomByIp")
    public R getRoomByIp(HttpServletRequest request) {
        try {
            //String header = request.getHeader("x-forwarded-for");
            String remoteAddr = request.getRemoteAddr();
            //System.out.println("hostAddress:::"+remoteAddr);
            String room = superConfigService.getRoomByIp(remoteAddr);
            return R.data(room);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("fail");
        }
    }
}

package com.acis.controller.lyf.SignDataCollect;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.common.util.dateUtils.DateUtil;
import com.acis.pojo.AcisDictInstrument;
import com.acis.pojo.AcisIntraoMonitorDataHistory;
import com.acis.pojo.AcisIntraoMonitorDataListen;
import com.acis.pojo.lyf.PushMessage;
import com.acis.pojo.lyf.Sign;
import com.acis.pojo.lyf.dto.MonitorEventData;
import com.acis.pojo.lyf.dto.MonitorIndividuationDto;
import com.acis.pojo.lyf.dto.MonitorInfoDto;
import com.acis.pojo.lyf.vo.request.MonitorInfoVo;
import com.acis.pojo.lyf.vo.response.SignInfoVo;
import com.acis.pojo.viewdocking.ViewChangeParam;
import com.acis.service.lyf.SignDataCollect.MonitorDataService;
import com.acis.service.lyf.SignDataCollect.SocketIoService;
import com.acis.service.lyf.systemConfig.OpeArrangeService;
import com.acis.service.lyf.systemConfig.SuperConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.acis.common.util.strUtils.SysIntegrationUtil.httpGet;

/**
 * @author STEVEN LEE
 * @date 2020/7/2 14:33
 */
@Api(tags = "体征数据采集")
@RestController
@RequestMapping("/acis/signDataCollect")
@CrossOrigin
public class SignDataCollectController {
    protected final static Logger logger = LoggerFactory.getLogger(SignDataCollectController.class);

    private static Map<String, String> TIMEPOINT = new ConcurrentHashMap<>();
    private static Map<String, Integer> FIVETIMEPOINT = new ConcurrentHashMap<>();
    private static Map<String, Integer> FIFTEENTIMEPOINT = new ConcurrentHashMap<>();
    private static Map<String, Boolean> booleanMap = new ConcurrentHashMap<>();

    @Autowired
    private SocketIoService socketIoService;

    @Autowired
    private SuperConfigService superConfigService;

    @Autowired
    private MonitorDataService monitorDataService;

    @Autowired
    private OpeArrangeService opeArrangeService;

    /**
     * 推送体征数据
     */
    @PostMapping("/signEvent")
    public void signEvent(@RequestParam("ip") String ip) {
        //String operationId = "b0f9d8bda9244397a44cb8ff278937d9";

        //通过仪器ip获取手术id
        //String ip = "192.168.1.177";
        String instrumentType = "0";
        String operationId = opeArrangeService.getOperationIdByIp(ip, instrumentType);

        String patientId = "10000188893";
        String visitId = "1";
        String operId = "1";


        Boolean b = true;
        //获取体征模板
        List<MonitorIndividuationDto> dtos = superConfigService.getDtos(1);
        //模拟1000次收到数据
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String format = df.format(new Date());
        String timePoint = format + ":00";
        String time = format + ":00";
        for (int j = 10; j < 1000; j++) {
            //获得当前手术对应的时间间隔的时长
            Integer duration = opeArrangeService.getDuration(operationId);
            logger.info("时间间隔： " + duration);
            logger.info("==============================");
            logger.info("time" + j + " = " + time);
            logger.info("timePoint" + j + " = " + timePoint);
            logger.info("==============================");
            //模拟一分钟一发送
            if (!time.equals(timePoint)) {
                timePoint = time;
                b = true;
            }
            List<AcisIntraoMonitorDataHistory> historyList = new ArrayList<>();
            String data = "";
            try {
                data = monitorDataService.getData(j, patientId, visitId, operId);
                Thread.sleep(3333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String[] split = data.split("=");
            String[] signData = split[1].split(",");

            String format1 = df.format(new Date());
            time = format1 + ":00";

            //获取分钟数，判断时间是否为5或者1的倍数
            String[] s = time.split(" ");
            String[] split1 = s[1].split(":");
            int i2 = Integer.valueOf(split1[1]) % duration;
            int i3 = Integer.valueOf(split1[1]) % 5;

            //体征模板列表长度
            int dtosSize = dtos.size();
            //体征数据长度
            int signDataSize = signData.length;
            //获取两者之间小的长度
            int theShort = dtosSize;

            if (dtosSize >= signDataSize) {
                theShort = signDataSize;
            }

            for (int i1 = 0; i1 < theShort; i1++) {
                MonitorIndividuationDto dto = dtos.get(i1);
                AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
                dataHistory.setCreateTime(new Date());
                dataHistory.setItemCode(dto.getItemCode());
                dataHistory.setItemName(dto.getItemName());
                dataHistory.setItemUnit(dto.getItemUnit());
                dataHistory.setTimePoint(DateUtil.parse(time, "yyyy-MM-dd HH:mm:ss"));
                dataHistory.setItemValue(signData[i1]);
                if (b) {
                    dataHistory.setState(1);
                } else {
                    dataHistory.setState(0);
                }
                dataHistory.setOperationId(operationId);
                dataHistory.setPatientId(patientId);
                historyList.add(dataHistory);
            }
            PushMessage pushMessage = new PushMessage();
            List<Sign> signs = new ArrayList<>();
            for (AcisIntraoMonitorDataHistory dataHistory : historyList) {
                dataHistory.setIsDelete(true);
                dataHistory.setIsDisplay(1);
                dataHistory.setIsChange(0);
                //保存到历史数据表
                monitorDataService.insertIntoHistory(dataHistory);
                if (b) {
                    //保存到间隔为1分钟文书显示数据表
                    monitorDataService.insertIntoData1(dataHistory);
                    if (i2 == 0 && i3 == 0) {
                        //保存到间隔为5分钟文书显示数据表
                        monitorDataService.insertIntoData(dataHistory);
                    }
                    //封装到前端显示的类中
                    Sign sign = new Sign();
                    sign.setItemCode(dataHistory.getItemCode());
                    sign.setItemValue(dataHistory.getItemValue());
                    sign.setState(1);
                    sign.setTimePoint(DateUtil.format(dataHistory.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                    signs.add(sign);
                }
            }
            pushMessage.setContent(signs);
            //System.out.println(signs);
            logger.info(signs.toString());
            pushMessage.setLoginUserNum(operationId);

            if (b) {
                //间隔为1分钟
                if (duration == 1) {
                    //发送到页面显示
                    socketIoService.pushMessageToUser(pushMessage);
                    List<Sign> content = pushMessage.getContent();
                    //更改体征数据状态
                    for (Sign sign : content) {
                        AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
                        dataHistory.setOperationId(operationId);
                        dataHistory.setItemCode(sign.getItemCode());
                        dataHistory.setState(2);
                        dataHistory.setTimePoint(DateUtil.parse(sign.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                        monitorDataService.updateState2(dataHistory);
                    }
                    //间隔为5分钟
                } else if (duration == 5 && i2 == 0) {
                    //发送到页面显示
                    socketIoService.pushMessageToUser(pushMessage);
                    List<Sign> content = pushMessage.getContent();
                    //更改体征数据状态
                    for (Sign sign : content) {
                        AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
                        dataHistory.setOperationId(operationId);
                        dataHistory.setItemCode(sign.getItemCode());
                        dataHistory.setState(2);
                        dataHistory.setTimePoint(DateUtil.parse(sign.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                        monitorDataService.updateState(dataHistory);
                    }
                }

            }
            b = false;
        }
    }

    /**
     * 推送监测数据
     */
    @PostMapping("/monitorEvent")
    public void monitorEvent(@RequestParam("ip") String ip) {
        //String operationId = "b0f9d8bda9244397a44cb8ff278937d9";
        //通过仪器ip获取手术id
        //String ip = "192.168.1.177";
        String instrumentType = "0";
        String operationId = opeArrangeService.getOperationIdByIp(ip, instrumentType);
        String patientId = "10000188893";
        String visitId = "1";
        String operId = "1";

        //模拟1000次收到数据
        Boolean b = true;
        String timePoint = "";
        List<Sign> signList = new ArrayList<>();
        for (int i = 10; i < 1000; i++) {
            //获取监测数据
            MonitorEventData monitorEventData = monitorDataService.getMonitorEventData(patientId, visitId, operId, i);

            String format = DateUtil.format(monitorEventData.getTimePoint(), "yyyy-MM-dd HH:mm:ss");
            logger.info("==============================");
            logger.info("time" + i + " = " + format);
            logger.info("timePoint" + i + " = " + timePoint);
            logger.info("==============================");
            if (!format.equals(timePoint)) {
                timePoint = format;
                b = true;
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //封装到发送到前端显示的类
            Sign sign = new Sign();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String time = df.format(new Date());
            sign.setTimePoint(time + ":00");
            sign.setItemCode(monitorEventData.getItemCode());
            sign.setItemValue(monitorEventData.getItemValue());
            sign.setState(1);
            signList.add(sign);
            logger.info("集合长度：" + signList.size());

            //保存到数据库
            AcisIntraoMonitorDataListen monitorData = new AcisIntraoMonitorDataListen();
            //监测数据编号
            monitorData.setItemCode(monitorEventData.getItemCode());
            //监测数据名称
            monitorData.setItemName(monitorEventData.getItemName());
            //监测数据值
            monitorData.setItemValue(monitorEventData.getItemValue());
            //单位
            monitorData.setItemUnit(monitorEventData.getItemUnit());
            //数据生成时间点
            monitorData.setTimePoint(DateUtil.parse(time + ":00", "yyyy-MM-dd HH:mm:ss"));
            //手术id
            monitorData.setOperationId(operationId);
            //创建时间
            monitorData.setCreateTime(new Date());
            monitorData.setIsDelete(true);
            monitorData.setIsDisplay(1);
            monitorData.setIsChange(0);
            //将监测数据状态设置为1已接收
            monitorData.setState(1);
            monitorDataService.insertIntoMonitorListen(monitorData);

            if (b) {
                //传递给前端显示
                PushMessage pushMessage = new PushMessage();
                pushMessage.setLoginUserNum(operationId);
                pushMessage.setContent(signList);
                socketIoService.pushMessageToUser1(pushMessage);
                //将监测数据状态设置为2已发送
                Integer state = 2;
                logger.info(signList.toString());
                for (Sign sign1 : signList) {
                    monitorDataService.updateState4(sign1.getItemCode(), sign1.getTimePoint(), operationId, state);
                }
                //清空signList集合
                if (null != signList && signList.size() != 0) {
                    signList.clear();
                }
            }
            b = false;
        }
    }

    @ApiOperation("采集仪器信息查询")
    @GetMapping("/getMonitorInfo/{roomNo}")
    public R<Map<String, List<MonitorInfoDto>>> getMonitorInfo(@PathVariable String roomNo) {

        try {
            logger.info("采集仪器信息查询");
            Map<String, List<MonitorInfoDto>> map = new HashMap<>();
            //查询所有监护仪设备信息
            Integer i1 = 0;
            List<MonitorInfoDto> monitorInfoList = monitorDataService.getMonitorInfo(i1,roomNo);
            if (null != monitorInfoList && monitorInfoList.size() != 0) {
                //对每台设备设定状态值
                for (MonitorInfoDto infoDto : monitorInfoList) {
                    if (roomNo.equals(infoDto.getRoomNo()) && infoDto.getState() == 0) {
                        infoDto.setState(2);
                    }
                    infoDto.setItemType(i1);
                }
            }
            //查询所有麻醉机设备信息
            Integer i2 = 1;
            List<MonitorInfoDto> monitorInfoList1 = monitorDataService.getMonitorInfo(i2,roomNo);
            if (null != monitorInfoList1 && monitorInfoList1.size() != 0) {
                //对每台设备设定状态值
                for (MonitorInfoDto infoDto : monitorInfoList1) {
                    if (roomNo.equals(infoDto.getRoomNo()) && infoDto.getState() == 0) {
                        infoDto.setState(2);
                    }
                    infoDto.setItemType(i2);
                }
            }
            map.put("monitor", monitorInfoList);
            map.put("anesMachine", monitorInfoList1);
            return R.data(map);
        } catch (Exception e) {
            logger.error("采集仪器信息查询失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "采集仪器信息查询失败");
        }
    }

    @ApiOperation("采集仪器记录保存")
    @PostMapping("/addMonitorInfo")
    public R addMonitorInfo(@RequestBody List<MonitorInfoVo> monitorInfoVo) {

        try {
            logger.info("采集仪器记录保存");
            if (null != monitorInfoVo && monitorInfoVo.size() > 0) {
                for (MonitorInfoVo infoVo : monitorInfoVo) {
                    //手术室时绑定仪器
                    if (null != infoVo.getInstrumentCode() && !"".equals(infoVo.getInstrumentCode())) {
                        //添加仪器使用记录
                        Integer i = monitorDataService.addMonitorInfo(infoVo);
                        //仪器字典中更改绑定手术间号
                        //Integer j = monitorDataService.updateRoomNo(infoVo.getInstrumentCode(), infoVo.getRoomNo());
                    } else {
                        //复苏室时绑定仪器
                        //查询该床位是否有仪器绑定
                        AcisDictInstrument dictInstrument = monitorDataService.getInstrumentInfoByBedId(infoVo.getRoomNo());
                        //如果有的话则记录仪器使用记录
                        if (null != dictInstrument) {
                            BeanUtils.copyProperties(dictInstrument, infoVo);
                            //添加采集频率
                            infoVo.setCollectNumber(String.valueOf(dictInstrument.getCollectNumber()));
                            //添加仪器类型
                            infoVo.setItemType(Integer.parseInt(dictInstrument.getItemType()));
                            //添加仪器使用记录
                            Integer i = monitorDataService.addMonitorInfo(infoVo);
                            //仪器字典中更改绑定手术间号
                            //Integer j = monitorDataService.updateRoomNo(infoVo.getInstrumentCode(), infoVo.getRoomNo());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("采集仪器记录保存失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100019.getCode(), "采集仪器记录保存失败");
        }
        return R.success(ResultCode.SUCCESS);
    }

    //@ApiOperation("采集仪器关闭时间记录")
    //@PutMapping("/updateMonitorCloseTime")
    public R updateMonitorCloseTime(@RequestParam("operationId") String operationId, @RequestParam("roomNo") String roomNo) {

        try {
            logger.info("采集仪器关闭时间记录");
            //FIXME:  设置仪器类型 ：0-监护仪 1-麻醉机 2-呼吸机
            Integer i = monitorDataService.updateMonitorCloseTime(operationId);
        } catch (Exception e) {
            logger.error("采集仪器关闭时间记录失败！");
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100017.getCode(), "采集仪器关闭时间记录失败");
        }

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("切换普通模式和抢救模式")
    @PutMapping("/changeOpeMode/{opeMode}/{operationId}")
    public R changeOpeMode(@PathVariable Boolean opeMode, @PathVariable String operationId) {

        Integer i = opeArrangeService.updateOpeMode(opeMode, operationId);
        if (i == 0) {
            return R.fail(CommonErrorCode.E100017.getCode(), "切换模式失败！");
        }
        //这里调用远程发送切换模式
        ViewChangeParam viewStartParam = new ViewChangeParam();
        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("主界面右侧边栏体征列表查询")
    @GetMapping("/getSignList")
    public R<List<SignInfoVo>> getSignList() {
        List<SignInfoVo> signInfoVos = superConfigService.getSignList();
        return R.data(signInfoVos);
    }

    /**
     * 通过rabbitmq接收体征数据
     *
     * @param hello
     */
    //@RabbitListener(queues = "hello.sign")
    public void sendSign(String hello) {
        logger.info(hello);
        //通过仪器ip获取手术id
        String[] split = hello.split("=");
        String ip = split[2];
        String instrumentType = "0";
        String operationId = opeArrangeService.getOperationIdByIp(ip, instrumentType);

        if (null == operationId) {
            logger.info("患者已出手术室");
            //throw new ACISException(CommonErrorCode.E300008.getCode(), "患者已出手术室");
        } else {
            //logger.info("b1" + booleanMap.get(ip));
            booleanMap.put(ip, false);
            //Boolean b = true;
            //获取体征模板
            List<MonitorIndividuationDto> dtos = superConfigService.getDtos(3);
            //模拟1000次收到数据
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String format = df.format(new Date());
            if (null == TIMEPOINT.get(ip)) {
                TIMEPOINT.put(ip, format + ":00");
            }

            //获取数据发送时间间隔
            Integer duration = opeArrangeService.getDuration(operationId);
            List<AcisIntraoMonitorDataHistory> historyList = new ArrayList<>();

            //对数据进行解析
            String[] signData = split[1].split(",");

            String format1 = df.format(DateUtil.parseTime(split[0]));
            String time = format1 + ":00";

            //获取分钟数，判断时间是否为5或者1的倍数
            String[] s = time.split(" ");
            String[] split1 = s[1].split(":");
            System.out.println("duration:" + duration);
            int i2 = Integer.valueOf(split1[1]) % duration;
            logger.info("i2 ======= " + i2);
            int i3 = Integer.valueOf(split1[1]) % 5;
            logger.info("i3 ======= " + i3);

            logger.info("时间间隔： " + duration);
            logger.info("==============================");
            logger.info("time" + " = " + time);
            logger.info("timePoint" + " = " + TIMEPOINT.get(ip));
            logger.info("==============================");
            //模拟一分钟一发送
            if (!time.equals(TIMEPOINT.get(ip))) {
                TIMEPOINT.put(ip, time);
                booleanMap.put(ip, true);
            }
            //logger.info("b2" + booleanMap.get(ip));

            //体征模板列表长度
            int dtosSize = dtos.size();
            //体征数据长度
            int signDataSize = signData.length;
            //获取两者之间小的长度
            int theShort = dtosSize;

            if (dtosSize >= signDataSize) {
                theShort = signDataSize;
            }

            for (int i1 = 0; i1 < theShort; i1++) {
                MonitorIndividuationDto dto = dtos.get(i1);
                AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
                dataHistory.setCreateTime(new Date());
                dataHistory.setItemCode(dto.getItemCode());
                dataHistory.setItemName(dto.getItemName());
                dataHistory.setItemUnit(dto.getItemUnit());
                dataHistory.setTimePoint(DateUtil.parse(time, "yyyy-MM-dd HH:mm:ss"));
                dataHistory.setItemValue(signData[i1]);
                if (booleanMap.get(ip)) {
                    dataHistory.setState(1);
                } else {
                    dataHistory.setState(0);
                }
                dataHistory.setOperationId(operationId);
                historyList.add(dataHistory);
            }
            //用于封装显示在文书中的体征数据
            PushMessage pushMessage = new PushMessage();
            //用于封装显示在文书中的体征数据
            PushMessage pushMessageInRight = new PushMessage();
            //用于保存显示在文书中的体征数据
            List<Sign> signs = new ArrayList<>();
            //用于保存显示在主界面右侧栏的体征数据
            List<Sign> signInRight = new ArrayList<>();
            for (AcisIntraoMonitorDataHistory dataHistory : historyList) {
                dataHistory.setIsDelete(true);
                dataHistory.setIsDisplay(1);
                dataHistory.setIsChange(0);
                String hostAddress = null;
                try {
                    hostAddress = Inet4Address.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                dataHistory.setIp(hostAddress);
                //保存到历史数据表
                monitorDataService.insertIntoHistory(dataHistory);
                //将实时体征数据发送到主界面右侧栏
                Sign signss = new Sign();
                signss.setItemCode(dataHistory.getItemCode());
                signss.setItemValue(dataHistory.getItemValue());
                signss.setState(1);
                signss.setTimePoint(DateUtil.format(dataHistory.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                signInRight.add(signss);
                //logger.info("b3保存到数据表" + booleanMap.get(ip));
                if (booleanMap.get(ip)) {
                    //保存到间隔为1分钟文书显示数据表
                    logger.info("保存到间隔为1分钟文书显示数据表：" + dataHistory);
                    monitorDataService.insertIntoData1(dataHistory);
                    if (i2 == 0 && i3 == 0) {
                        //保存到间隔为5分钟文书显示数据表
                        logger.info("保存到间隔为5分钟文书显示数据表：" + dataHistory);
                        monitorDataService.insertIntoData(dataHistory);
                    }
                    //封装到前端显示的类中
                    Sign sign = new Sign();
                    sign.setItemCode(dataHistory.getItemCode());
                    sign.setItemValue(dataHistory.getItemValue());
                    sign.setState(1);
                    sign.setTimePoint(DateUtil.format(dataHistory.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                    signs.add(sign);
                }
            }
            pushMessageInRight.setContent(signInRight);
            logger.info("实时体征数据：" + signInRight);
            pushMessageInRight.setLoginUserNum(operationId);

            //发送实时数据到前端
            socketIoService.pushMessageToUser2(pushMessageInRight);

            pushMessage.setContent(signs);
            logger.info("文书体征数据：" + signs);
            pushMessage.setLoginUserNum(operationId);

            //logger.info("b4" + booleanMap.get(ip));
            if (booleanMap.get(ip)) {
                //间隔为1分钟
                if (duration == 1) {
                    //发送到页面显示
                    socketIoService.pushMessageToUser(pushMessage);
                    List<Sign> content = pushMessage.getContent();
                    //更改体征数据状态
                    for (Sign sign : content) {
                        AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
                        dataHistory.setOperationId(operationId);
                        dataHistory.setItemCode(sign.getItemCode());
                        dataHistory.setState(2);
                        dataHistory.setTimePoint(DateUtil.parse(sign.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                        monitorDataService.updateState2(dataHistory);
                    }
                    //间隔为5分钟
                } else if (duration == 5 && i2 == 0) {
                    //发送到页面显示
                    socketIoService.pushMessageToUser(pushMessage);
                    List<Sign> content = pushMessage.getContent();
                    //更改体征数据状态
                    for (Sign sign : content) {
                        AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
                        dataHistory.setOperationId(operationId);
                        dataHistory.setItemCode(sign.getItemCode());
                        dataHistory.setState(2);
                        dataHistory.setTimePoint(DateUtil.parse(sign.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                        monitorDataService.updateState(dataHistory);
                    }
                }
            }
            booleanMap.put(ip, false);
        }

        //logger.info("b5" + booleanMap.get(ip));
    }

    /**
     * rabbitMq接收监测数据
     *
     * @param hello
     */
    //@RabbitListener(queues = "neo.monitor")
    public void sendMonitor(String hello) {

        logger.info(hello);
        //通过仪器ip获取手术id
        String[] split = hello.split("=");
        String ip = split[2];
        String instrumentType = "0";
        String operationId = opeArrangeService.getOperationIdByIp(ip, instrumentType);

        //logger.info("b1" + booleanMap.get(ip));
        booleanMap.put(ip + "monitor", false);
        //Boolean b = true;
        //获取体征模板
        List<MonitorIndividuationDto> dtos = superConfigService.getDtos(4);
        //模拟1000次收到数据
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String format = df.format(new Date());
        if (null == TIMEPOINT.get(ip + "monitor")) {
            TIMEPOINT.put(ip + "monitor", format + ":00");
        }

        //获取数据发送时间间隔
        Integer duration = 15;
        List<AcisIntraoMonitorDataHistory> historyList = new ArrayList<>();

        //对数据进行解析
        String[] signData = split[1].split(",");

        String format1 = df.format(DateUtil.parseTime(split[0]));
        String time = format1 + ":00";

        //获取分钟数,split1[1]为分钟数
        String[] s = time.split(" ");
        String[] split1 = s[1].split(":");
        logger.info("当前的分钟数为：" + Integer.valueOf(split1[1]));

        logger.info("时间间隔： " + duration);
        logger.info("==============================");
        logger.info("time" + " = " + time);
        logger.info("timePoint" + " = " + TIMEPOINT.get(ip + "monitor"));
        logger.info("==============================");
        //模拟一分钟一发送
        if (!time.equals(TIMEPOINT.get(ip + "monitor"))) {
            TIMEPOINT.put(ip + "monitor", time);
            booleanMap.put(ip + "monitor", true);
        }
        //logger.info("b2" + booleanMap.get(ip));

        //体征模板列表长度
        int dtosSize = dtos.size();
        //体征数据长度
        int signDataSize = signData.length;
        //获取两者之间小的长度
        int theShort = dtosSize;

        if (dtosSize >= signDataSize) {
            theShort = signDataSize;
        }

        //用于封装显示在文书中的监测数据
        PushMessage pushMessage = new PushMessage();
        //用于保存显示在文书中的监测数据
        List<Sign> signs = new ArrayList<>();


        //将数据和体征进行匹配
        for (int i1 = 0; i1 < theShort; i1++) {
            MonitorIndividuationDto dto = dtos.get(i1);
            AcisIntraoMonitorDataHistory dataHistory = new AcisIntraoMonitorDataHistory();
            dataHistory.setCreateTime(new Date());
            dataHistory.setItemCode(dto.getItemCode());
            dataHistory.setItemName(dto.getItemName());
            dataHistory.setItemUnit(dto.getItemUnit());
            dataHistory.setTimePoint(DateUtil.parse(time, "yyyy-MM-dd HH:mm:ss"));
            dataHistory.setItemValue(signData[i1]);
            if (booleanMap.get(ip + "monitor")) {
                dataHistory.setState(1);
            } else {
                dataHistory.setState(0);
            }
            dataHistory.setOperationId(operationId);
            historyList.add(dataHistory);
        }

        logger.info("FIVETIMEPOINT:" + FIVETIMEPOINT.get(ip + "monitor"));
        logger.info("FIFTEENTIMEPOINT:" + FIFTEENTIMEPOINT.get(ip + "monitor"));
        if (booleanMap.get(ip + "monitor") && (null == FIVETIMEPOINT.get(ip + "monitor") || Integer.valueOf(split1[1]).equals(FIVETIMEPOINT.get(ip + "monitor")))) {
            //如果是新的一分钟的第一组数据且时间是+5的倍数，则保存到数据库
            for (AcisIntraoMonitorDataHistory dataHistory : historyList) {
                dataHistory.setIsDelete(true);
                dataHistory.setIsDisplay(1);
                dataHistory.setIsChange(0);
                AcisIntraoMonitorDataListen listen = new AcisIntraoMonitorDataListen();
                BeanUtils.copyProperties(dataHistory, listen);
                monitorDataService.insertIntoMonitorListen(listen);
                //封装到前端显示的类中
                Sign sign = new Sign();
                sign.setItemCode(dataHistory.getItemCode());
                sign.setItemValue(dataHistory.getItemValue());
                sign.setState(1);
                sign.setTimePoint(DateUtil.format(dataHistory.getTimePoint(), "yyyy-MM-dd HH:mm:ss"));
                signs.add(sign);
            }
            logger.info("保存数据库：" + signs);

            //更新STARTTIMEPOINT中的时间
            if (Integer.valueOf(split1[1]) + 5 < 60) {
                FIVETIMEPOINT.put(ip + "monitor", Integer.valueOf(split1[1]) + 5);
            } else {
                FIVETIMEPOINT.put(ip + "monitor", Integer.valueOf(split1[1]) + 5 - 60);
            }
            //如果是新的一分钟的第一组数据且时间是+15的倍数，则发送到前端显示
            if (null == FIFTEENTIMEPOINT.get(ip + "monitor") || Integer.valueOf(split1[1]).equals(FIFTEENTIMEPOINT.get(ip + "monitor"))) {
                pushMessage.setContent(signs);
                logger.info("文书监测数据：" + signs);
                pushMessage.setLoginUserNum(operationId);
                socketIoService.pushMessageToUser1(pushMessage);
                //更改数据库中数据的状态为2
                Integer state = 2;
                for (Sign sign : signs) {
                    monitorDataService.updateState4(sign.getItemCode(), sign.getTimePoint(), operationId, state);
                }
                //更改FIFTEENTIMEPOINT中的时间
                if (Integer.valueOf(split1[1]) + 15 < 60) {
                    FIFTEENTIMEPOINT.put(ip + "monitor", Integer.valueOf(split1[1]) + 15);
                } else {
                    FIFTEENTIMEPOINT.put(ip + "monitor", Integer.valueOf(split1[1]) + 15 - 60);
                }
                logger.info("发送到前端：" + signs);
            }
        }
        booleanMap.put(ip + "monitor", false);
        //logger.info("b5" + booleanMap.get(ip));
    }

    @ApiOperation("模拟his手术取消")
    @PostMapping("/cancelOperation")
    public void cancelOperation(@RequestParam("operationId") String operationId) {

        //取消手术
        opeArrangeService.updateOpeState(operationId, "18");

        //发送通知
        String room = opeArrangeService.getRoomByOperationId(operationId);
        PushMessage pushMessage = new PushMessage();
        pushMessage.setLoginUserNum("2_" + room);
        List<Sign> list = new ArrayList<>();
        Sign sign = new Sign();
        sign.setItemValue(operationId);
        sign.setItemCode("1");
        list.add(sign);
        pushMessage.setContent(list);
        socketIoService.pushMessageToUser3(pushMessage);

    }

    @ApiOperation("模拟手术增加")
    @PostMapping("/addOperation")
    public void addOperation(@RequestBody List<Map<String, String>> operationInfos) {
        //添加手术

        //通过手术间进行手术分类
        Set<String> roomSet = new HashSet<>();
        if (null != operationInfos && operationInfos.size() > 0) {
            for (Map<String, String> info : operationInfos) {
                roomSet.add(info.get("room"));
            }

            List<String> roomList = new ArrayList<>(roomSet);
            for (String room : roomList) {
                List<Map<String, String>> operationList = new ArrayList<>();
                for (Map<String, String> info : operationInfos) {
                    String thisRoom = info.get("room");
                    if (thisRoom.equals(room)) {
                        operationList.add(info);
                    }
                }
                //发送通知
                PushMessage pushMessage = new PushMessage();
                pushMessage.setLoginUserNum("2_" + room);
                List<Sign> list = new ArrayList<>();
                Sign sign = new Sign();
                sign.setOperationList(operationList);
                sign.setItemCode("2");
                list.add(sign);
                pushMessage.setContent(list);
                socketIoService.pushMessageToUser3(pushMessage);
            }
        }
    }

    @ApiOperation("模拟手术信息编辑")
    @PostMapping("/editOperation")
    public void editOperation(@RequestBody List<Map<String, String>> operationInfos) {
        //编辑手术

        //
    }

    @ApiOperation("开启定时同步线程任务(福建医科大学第一附属医院)")
    @GetMapping("/startSyncMonitorDataFJ/{operationId}")
    public R startSyncMonitorDataFJ(@PathVariable String operationId) {
        String url = "http://127.0.0.1:8092/dataDockFZ/startSyncMonitorDataFJ/" + operationId;
        httpGet(url);
        return R.success("success");
    }


    @ApiOperation("结束同步体征数据调用,并关闭向前端推送数据的线程")
    @GetMapping("/endMonitorDataDocking/{operationId}")
    public R endMonitorDataDocking(@PathVariable String operationId) {
        String url = "http://127.0.0.1:8092/dataDockFZ/endMonitorDataDocking/" + operationId;
        httpGet(url);
        return R.success("success");
    }
}

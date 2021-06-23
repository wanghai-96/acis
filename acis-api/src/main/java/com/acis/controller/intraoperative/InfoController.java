package com.acis.controller.intraoperative;

import com.acis.common.constants.AcisFtpDict;
import com.acis.common.constants.WebServiceConstant;
import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.common.util.FtpUploadUtil.FtpUtil;
import com.acis.common.util.ObjectUtil.ObjectUtils;
import com.acis.common.util.WebServiceUtil.WebserviceUtils;
import com.acis.common.util.XMLutil.XmlUtils;
import com.acis.dao.intraoperative.InfoMapper;
import com.acis.pojo.intraoperative.dto.AcisPatientWritData;
import com.acis.pojo.intraoperative.dto.RegistrationOfSurgicalInformation;
import com.acis.pojo.intraoperative.dto.fz.SendAnesthesiaApply;
import com.acis.pojo.intraoperative.dto.fz.SendAnesthesiaProcessRecord;
import com.acis.pojo.intraoperative.vo.dict.AcisStorageIntraoEventDictVO;
import com.acis.pojo.intraoperative.vo.info.*;
import com.acis.pojo.lyf.vo.request.BatchAddIntraoMonitorDataVo;
import com.acis.pojo.lyf.vo.request.EventInfoVo;
import com.acis.pojo.lyf.vo.request.SignValueVo;
import com.acis.pojo.lyf.vo.request.SignatureVo;
import com.acis.service.intraoperative.InfoService;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.fastjson.JSON;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import io.swagger.annotations.*;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.acis.common.util.strUtils.SysIntegrationUtil.httpGet;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 术中综合信息功能
 */
@Log4j2
@Api(tags = "术中综合信息功能")
@RestController
@RequestMapping("/acis/intraoperative/info")
@CrossOrigin
public class InfoController {

    @Autowired
    private InfoService infoService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private InfoMapper infoMapper;

    /**
     * @author Neon Xie
     * @Date 2021/2/27
     * @description FTP 上传麻醉/复苏记录单
     */
    @ResponseBody
    @ApiOperation("测试接口")
    @PostMapping("/ftpUploadAnesTest/{operationId}/{condition}")
    public R ftpUploadAnesTest(@ApiParam("手术id") @PathVariable String operationId, @ApiParam("情况: 0麻醉记录单 1复苏记录单") @PathVariable String condition) {
        StringBuffer xml;
        String name = null;
        String operationSubFlag = null;
        try {

        } catch (Exception e) {

            e.printStackTrace();
            //上传文件失败返回错误信息
            log.info(CommonErrorCode.E100008.getMsg());
            return R.fail(CommonErrorCode.E100008.getCode(), CommonErrorCode.E100008.getMsg());
        }
        SendAnesthesiaApply sendAnesthesiaApply = infoMapper.getSendAnesthesiaApply(operationId);
        SendAnesthesiaApply sendAnesthesiaApply1 = infoMapper.getSendAnesthesiaApplyWrit(operationId);
        ObjectUtils.combineSydwCore(sendAnesthesiaApply, sendAnesthesiaApply1);
        XStream xStream = new XStream(new DomDriver(null, new XmlFriendlyNameCoder("_-", "_")));
        xStream.processAnnotations(SendAnesthesiaApply.class);
        String xmlDtoStr = xStream.toXML(sendAnesthesiaApply).trim().replaceAll("\\s*|\t|\r|\n", "");
        //拼接xml参数
        xml = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnesthesiaApply").append(WebServiceConstant.HEAD_MIDDLE).append(xmlDtoStr).append(WebServiceConstant.HEAD_AFTER);
        System.out.println(xmlDtoStr);
        return R.data(WebserviceUtils.postWithoutSoap(WebServiceConstant.URL, xml));
    }


    /**
     * @author Neon Xie
     * @Date 2021/3/24
     * @description 手术状态回退功能  用于PACU回退
     */
    @ApiOperation("手术状态回退功能")
    @GetMapping("/backOperationState/{operationId}")
    public R<String> backOperationState(@ApiParam("手术id") @PathVariable String operationId){
        //第一步先 同步方法确保先执行完成  关闭体征读取和获取进程
        synchronized (this){
            String url = "http://127.0.0.1:8092/dataDockFZ/endMonitorDataDocking/" + operationId;
            //执行关闭线程的方法
            httpGet(url);
        }
        //清空 PACU床位
        try {
            infoService.backOperationState(operationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data("success");
    }



    /**
     * @author Neon Xie
     * @Date 2021/3/4
     * @description 获取打印/保存 麻醉/复苏记录单的  应该打印的页数
     */
    @ApiOperation("获取打印/保存 麻醉/复苏记录单的  应该打印的页数")
    @GetMapping("/getsThePageThatCurrentlyNeedsToBePrinted/{operationId}/{condition}")
    public R<String> getsThePageThatCurrentlyNeedsToBePrinted(@ApiParam("手术id") @PathVariable String operationId, @ApiParam("情况 0 麻醉 1复苏") @PathVariable String condition) {
        String index = null;
        try {
            //通过先计算生成 70 页范围内的分页值  在循环中 去和  分页的开始时间和结束时间去匹配  最新的事件时间节点是否在某个范围内  若在某个范围内  则说明该范围的当前循环次数
            //为当前的应该打印的次数的索引 index 值
            index = infoService.getsThePageThatCurrentlyNeedsToBePrinted(operationId, condition);
            if (null == index) {
                return R.data("0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(index);
    }

    /**
     * @author Neon Xie
     * @Date 2021/3/1
     * @description 获得当前手术绑定的监护仪ip
     */
    @ApiOperation("获得当前手术绑定的监护仪ip")
    @GetMapping("/getOperationBindEquipmentIp/{operationId}")
    public R<String> getOperationBindEquipmentIp(@ApiParam("手术id") @PathVariable String operationId) {
        String equipmentIP;
        try {
            equipmentIP = infoService.getOperationBindEquipmentIp(operationId);
            if (null == equipmentIP) {
                //这里是因为没有查询到手术绑定的监护仪信息
                return R.fail(CommonErrorCode.E100034.getCode(), CommonErrorCode.E100034.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e);
            return R.fail(CommonErrorCode.E100034.getCode(), CommonErrorCode.E100034.getMsg());
        }
        return R.data(equipmentIP);
    }

    /**
     * @author Neon Xie
     * @Date 2021/3/1
     * @description 初始化用户登录图片签名
     */
    @ApiOperation("初始化用户登录图片签名")
    @PostMapping("/getApplicationKey/{operationId}")
    public R getApplicationKey(@ApiParam("手术id") @PathVariable String operationId) {
        Integer result;
        try {
            result = infoService.initOperationUserKey(operationId);
            if (null == result) {
                return R.fail(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e);
            return R.fail(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
        }

        return R.data(result);
    }

    /**
     * @author Neon Xie
     * @Date 2021/2/27
     * @description 是否可以上传麻醉/复苏记录单
     */
    @ResponseBody
    @ApiOperation("是否可以上传麻醉/复苏记录单")
    @GetMapping("/acisUploadWritWright/{operationId}/{condition}")
    public R<Integer> acisUploadWritWright(@ApiParam("手术id") @PathVariable String operationId, @ApiParam("情况: 0麻醉记录单 1复苏记录单") @PathVariable String condition) {
        boolean result;
        try {
            result = infoService.getOperationUploadWrit(operationId, condition);
            if (!result) {
                //不能上传
                return R.data(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(1);
    }

    /**
     * @author Neon Xie
     * @Date 2021/2/27
     * @description FTP 上传麻醉/复苏记录单
     */
    @ResponseBody
    @ApiOperation("FTP 上传麻醉/复苏记录单")
    @PostMapping("/ftpUploadAnesPDF/{operationId}/{condition}")
    public R<Boolean> ftpUploadAnesPDF(@ApiParam("手术id") @PathVariable String operationId, @ApiParam("情况: 0麻醉记录单 1复苏记录单") @PathVariable String condition, HttpServletRequest request) {
        boolean result;
        String name;
        String operationSubFlag;
        // >>>> PDF文件上传参数
        String getSendPdfMsgInfoxml;
        // >>>>  麻醉记录单内容
        String getSendAnesthesiaApply;
        String getSendAnesthesiaSignData;
        // >>>>  复苏记录单内容
        String getSendAnabiosisApply;
        String getAnabiosisSignData;
        String getAnabiosisProcessRecord;
        try {
            if ("0".equals(condition)) {
                name = "麻醉.pdf";
                operationSubFlag = "1";
            } else if ("1".equals(condition)) {
                name = "复苏.pdf";
                operationSubFlag = "30";
            } else {
                //上传文件condition不符合格式
                log.info(CommonErrorCode.E100007.getMsg());
                return R.fail(CommonErrorCode.E100007.getCode(), CommonErrorCode.E100007.getMsg());
            }
            String fileName = operationId + "-" + name;
            log.info("UPLOAD_FTP_FILE_NAME:" + fileName);
            //获得上传文件的文件流
            InputStream in = request.getInputStream();
            //获得格式化日期作为当日文件路径父级 : 例如 20210227
            String presentData = "/" + new SimpleDateFormat("yyyyMMdd").format(new Date());
            //执行文件上传 : 传入 HOST:地址 PORT:端口 USER_NAME:账户名 PASSWORD:密码 BASE_PATH:根路径 FILR_PATH:文件路径+生成  FILENAME:文件名  IN:文件流
            FtpUtil.uploadFile(AcisFtpDict.HOST, Integer.parseInt(AcisFtpDict.PORT), AcisFtpDict.USER_NAME, AcisFtpDict.PASSWORD,
                    AcisFtpDict.BASE_PATH, presentData + "//" + operationId, fileName, in);
            //文件路径
            String filePath = AcisFtpDict.BASE_PATH + presentData;
            //获得动态bean转化的xml文件
            //回传PDF文件相关信息到集成平台
            getSendPdfMsgInfoxml = infoService.getSendPdfMsgInfo(operationId, condition, filePath, operationSubFlag);
            if (null == getSendPdfMsgInfoxml) {
                System.out.println("错误输出");
                return R.fail(CommonErrorCode.E100033.getCode(), CommonErrorCode.E100033.getMsg());
            } else {
                //执行 -> SendPdfMsgInfo 回传PDF文件相关信息
                String pdfXmlParam = XmlUtils.trimStr(getSendPdfMsgInfoxml);
                StringBuffer SendPdfMsgInfo = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendPdfMsgInfo").append(WebServiceConstant.HEAD_MIDDLE).append(pdfXmlParam).append(WebServiceConstant.HEAD_AFTER);
                WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendPdfMsgInfo);
                System.out.println("输出PDF文件参数>>>>>>> \n" + SendPdfMsgInfo);
            }
            if ("0".equals(condition)) {
                //麻醉记录单基本信息内容
                getSendAnesthesiaApply = infoService.getSendAnesthesiaApply(operationId);
                //手麻系统调用集成平台服务向集成平台发送麻醉过程病人体征记录
                getSendAnesthesiaSignData = infoService.getSendAnesthesiaSignData(operationId);
                if (null == getSendAnesthesiaApply || null == getSendAnesthesiaSignData) {
                    System.out.println("错误输出");
                    return R.fail(CommonErrorCode.E100033.getCode(), CommonErrorCode.E100033.getMsg());
                } else {
                    //这里拼接xml 使用POST方法进行 WebService 调用
                    //执行  - > SendAnesthesiaApply -> 麻醉记录单基本信息内容
                    String applyXmlParam = XmlUtils.trimStr(getSendAnesthesiaApply);
                    StringBuffer SendAnesthesiaApply = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnesthesiaApply").append(WebServiceConstant.HEAD_MIDDLE).append(applyXmlParam).append(WebServiceConstant.HEAD_AFTER);
                    WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendAnesthesiaApply);
                    System.out.println("输出麻醉记录单内容>>>>>>> \n" + SendAnesthesiaApply);
                    //执行 -> SendAnesthesiaSignData -> 麻醉过程体征数据
                    String signXmlParam = XmlUtils.trimStr(getSendAnesthesiaSignData);
                    StringBuffer SendAnesthesiaSignData = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnesthesiaSignData").append(WebServiceConstant.HEAD_MIDDLE).append(signXmlParam).append(WebServiceConstant.HEAD_AFTER);
                    WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendAnesthesiaSignData);
                    System.out.println("输出麻醉记录体征内容>>>>>>> \n" + SendAnesthesiaSignData);
                }
            } else {
                getSendAnabiosisApply = infoService.getAnabiosisApply(operationId);
                getAnabiosisProcessRecord = infoService.getSendAnabiosisProcessRecord(operationId);
                getAnabiosisSignData = infoService.getSendAnabiosisSignData(operationId);
                if (null == getSendAnabiosisApply || null == getAnabiosisSignData || null == getAnabiosisProcessRecord) {
                    System.out.println("错误输出");
                    return R.fail(CommonErrorCode.E100033.getCode(), CommonErrorCode.E100033.getMsg());
                } else {
                    //这里拼接xml 使用POST方法进行 WebService 调用
                    //执行  - > SendAnabiosisApply -> 复苏记录单基本信息内容
                    String applyXmlParam = XmlUtils.trimStr(getSendAnabiosisApply);
                    StringBuffer SendAnabiosisApply = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnabiosisApply").append(WebServiceConstant.HEAD_MIDDLE).append(applyXmlParam).append(WebServiceConstant.HEAD_AFTER);
                    WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendAnabiosisApply);
                    System.out.println("输出复苏记录单基本内容>>>>>>> \n" + SendAnabiosisApply);
                    //执行  - > SendAnabiosisProcessRecord -> 复苏过程记录信息
                    String progressAnabiosisParam = XmlUtils.trimStr(getAnabiosisProcessRecord);
                    StringBuffer SendAnabiosisProcessRecord = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnabiosisProcessRecord").append(WebServiceConstant.HEAD_MIDDLE).append(progressAnabiosisParam).append(WebServiceConstant.HEAD_AFTER);
                    WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendAnabiosisProcessRecord);
                    System.out.println("输出复苏过程记录信息>>>>>>> \n" + SendAnabiosisProcessRecord);
                    //执行 - > SendAnabiosisSignData -> 复苏过程体征数据
                    String signAnabiosisParam = XmlUtils.trimStr(getAnabiosisSignData);
                    StringBuffer SendAnabiosisSignData = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnabiosisSignData").append(WebServiceConstant.HEAD_MIDDLE).append(signAnabiosisParam).append(WebServiceConstant.HEAD_AFTER);
                    WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendAnabiosisSignData);
                    System.out.println("输出复苏过程体征信息>>>>>>> \n" + SendAnabiosisSignData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //上传文件失败返回错误信息
            log.info(CommonErrorCode.E100008.getMsg());
            return R.fail(CommonErrorCode.E100008.getCode(), CommonErrorCode.E100008.getMsg());
        }
        return R.data(true);
    }

    /**
     * @author Neon Xie
     * @Date 2021/2/27
     * @description 术后液体数据更新/插入
     */
    @ApiOperation("术后液体数据更新/插入")
    @PostMapping("/updatePostoperativeFluidRegistration")
    public R updatePostoperativeFluidRegistration(@ApiParam("术后液体保存参数") @RequestBody List<AcisPatientWritData> list) {
        Integer result = null;
        try {
            result = infoService.updatePostoperativeFluidRegistration(list);
            if (null == result) {
                return R.fail(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(result);
    }


    @ApiOperation("术后液体数据回显")
    @GetMapping("/getPostoperativeFluidRegistration/{operationId}")
    public R getPostoperativeFluidRegistration(@ApiParam("手术id") @PathVariable String operationId) {
        List<HashMap<String, String>> result = null;
        try {
            result = infoService.getPostoperativeFluidRegistration(operationId);
            if (null == result) {
                return R.data(new ArrayList<>(), CommonErrorCode.E100018.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(result);
    }


    @ApiOperation("手术信息修改")
    @PostMapping("/updateOperationInfo")
    public R updateOperationInfo(@ApiParam("手术信息修改参数") @RequestBody RegistrationOfSurgicalInformation param) {
        Integer reuslt;
        try {
            reuslt = infoService.updateOperationInfo(param);
            if (null == reuslt) {
                return R.fail(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            //未查询到消息
            log.error(CommonErrorCode.E100017.getMsg());
            return R.fail(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
        }
        return R.data(reuslt);
    }

    @ApiOperation("手术信息回显")
    @GetMapping("/getOperationInformation/{operationId}")
    public R getOperationInformation(@ApiParam("手术id") @PathVariable String operationId) {
        HashMap<String, String> result = null;
        try {
            result = infoService.getOperationInformation(operationId);
            if (null == result) {
                return R.data(new HashMap<>(), CommonErrorCode.E100018.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(result);
    }

    @ApiOperation("急诊手术登记")
    @PostMapping("/emergencyRegister")
    public R emergencyRegister(@ApiParam("急诊手术登记参数") @RequestBody RegistrationOfSurgicalInformation param) {
        Integer reuslt = null;
        try {
            reuslt = infoService.operationEmergencyRegist(param);
        } catch (Exception e) {
            e.printStackTrace();
            //未查询到消息
            log.error(CommonErrorCode.E100021.getMsg());
            return R.fail(CommonErrorCode.E100021.getCode(), CommonErrorCode.E100021.getMsg());
        }

        return R.data(reuslt);
    }


    @ApiOperation("创建术中患者体征分页信息")
    @PostMapping("/getIntraoMonitorDataByTimeInterval")
    public R<AcisIntraoWritPageVO> getIntraoMonitorDataByTimeInterval(@ApiParam("创建术中患者体征分页入参") @RequestBody @Validated AcisGetMonitorPageInfoParamVO param) {

        //这里需要通过传入的时间间隔封装分页信息
        AcisIntraoWritPageVO acisIntraoWritPageVO = null;
        try {
            if (null == param.getIntervalTime()) {
                //体征数据间隔时间判断
                return R.fail(CommonErrorCode.E500032.getCode(), "未获取到体征信息间隔时间");
            } else {
                if (param.getIntervalTime() <= 0) {
                    //体征数据间隔参数合理性判断
                    return R.fail(CommonErrorCode.E500033.getCode(), "请输入合理的体征数据间隔时间");
                }
            }
            if (null == param.getOperationId() || StringUtils.isBlank(param.getOperationId())) {
                //手术id参数判断
                return R.fail(CommonErrorCode.E500028.getCode(), "未获取到手术id");
            }
            if (null == param.getPageIndex()) {
                //当前页码判断
                return R.fail(CommonErrorCode.E500029.getCode(), "未获取到当前页码");
            } else {
                if (param.getPageIndex() >= 70) {
                    //验证传入的页数是否超过分页的最大值
                    return R.fail(CommonErrorCode.E600007.getCode(), "传入的当前页超出最大值");
                }
            }
            if (null == param.getPageTimeInterval()) {
                //当前页码判断
                return R.fail(CommonErrorCode.E500030.getCode(), "未获取到分页时间跨度");
            } else {
                if (param.getPageTimeInterval() <= 0) {
                    //时间跨度合理性判断
                    return R.fail(CommonErrorCode.E500031.getCode(), "请输入合理的时间跨度");
                }
            }
            //生成分页信息
            acisIntraoWritPageVO = infoService.buildIntraoDataTimeIntervalPage(param.getOperationId(), param.getPageTimeInterval(), param.getIntervalTime(), param.getPageIndex(), param.getOperState());
        } catch (Exception e) {
            //未查询到消息
            log.error(CommonErrorCode.E100021.getMsg());
            return R.fail(CommonErrorCode.E100021.getCode(), CommonErrorCode.E100021.getMsg());
        }
        return R.data(acisIntraoWritPageVO);
    }

    @ApiOperation("获取术中患者体征信息")
    @PostMapping("/getIntraoMonitorData")
    public R<List<AcisIntraoMonitorDataAllVO>> getIntraoMonitorData(@ApiParam("获取术中患者体征信息参数") @RequestBody @Validated AcisGetMonitorDataParamVO param) {
        List<AcisIntraoMonitorDataAllVO> list = null;
        try {
            switch (param.getDataMode()) {
                case 1:
                    list = infoService.getAcisIntraoMonitorEmergencyData(param.getOperationId(), param.getStartTime(), param.getEndTime(), param.getModeCode());
                    return R.data(list);
                case 5:
                    list = infoService.getAcisIntraoMonitorData(param.getOperationId(), param.getStartTime(), param.getEndTime(), param.getModeCode());
                    break;
                default:
            }
        } catch (Exception e) {
            //未查询到消息
            log.error(CommonErrorCode.E600006.getMsg());
            return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("批量添加患者体征数据")
    @PostMapping("/batchAddIntraoMonitorData")
    public R batchAddIntraoMonitorData(@RequestBody BatchAddIntraoMonitorDataVo batchAddIntraoMonitorDataVo) {
        String startTime = batchAddIntraoMonitorDataVo.getStartTime();
        String endTime = batchAddIntraoMonitorDataVo.getEndTime();
        List<SignValueVo> signList = batchAddIntraoMonitorDataVo.getSignList();
        if (null == startTime || "".equals(startTime)) {
            return R.fail("开始时间不能为空");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail("结束时间不能为空");
        }
        if (null == signList || signList.size() == 0) {
            return R.fail("请选择体征信息");
        }
        infoService.batchAddIntraoMonitorData(batchAddIntraoMonitorDataVo);
        return R.success("成功");
    }


    @ApiOperation("获取术中事件信息")
    @PostMapping("/getAcisIntraoEventInfo")
    public R<Map<String, Object>> getAcisIntraoEventInfo(@ApiParam("获取事件参数") @RequestBody AcisGetEventsParamVO params) {
        Map<String, Object> result = new HashMap<>();
        List<AcisGetAcisIntraoEventVO> list = null;
        try {
            if (StringUtils.isBlank(params.getOperationId())) {
                log.error(CommonErrorCode.E500028.getMsg());
                return R.fail(CommonErrorCode.E500028.getCode(), "未获取到手术id");
            }
//            if (StringUtils.isBlank(params.getStartTime()) || StringUtils.isBlank(params.getEndTime())) {
//                //传入开始时间参数为空
//                log.error(CommonErrorCode.E500034.getMsg());
//                return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
//            }
            result = infoService.getAcisGetAcisIntraoEvent(params.getOperationId(), params.getLength(), params.getLine(), params.getPage(), params.getStartTime(), params.getEndTime(), params.getType());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(CommonErrorCode.E600006.getMsg());
            return R.fail(CommonErrorCode.E600006.getCode(), "获取信息失败");
        }
        return R.data(result);
    }


    @ApiOperation("获取术中患者监测数据")
    @PostMapping("/getAcisIntraoMonitorListenData")
    public R<List<AcisIntraoListenDataVO>> getAcisIntraoMonitorListenData(@ApiParam("获取监测数据入参") @RequestBody AcisGetListenDataParamVO params) {
        List<AcisIntraoListenDataVO> list = null;
        try {
            if (null == params) {
                //传入开始时间参数为空
                log.error(CommonErrorCode.E500034.getMsg());
                return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
            } else {
                if (StringUtils.isBlank(params.getOperationId())) {
                    //未获取到手术id
                    log.error(CommonErrorCode.E500028.getMsg());
                    return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
                }
                if (StringUtils.isBlank(params.getStartTime()) || StringUtils.isBlank(params.getEndTime())) {
                    //传入开始时间参数为空
                    log.error(CommonErrorCode.E500034.getMsg());
                    return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
                }
                list = infoService.getAcisIntraoListenData(params.getOperationId(), params.getStartTime(), params.getEndTime());
            }
        } catch (Exception e) {
            //数据获取失败
            log.info(CommonErrorCode.E600006.getMsg());
            throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }

    @PostMapping("/updateAcisIntraoListenDataInfo")
    @ApiOperation(value = "修改术中体征监测数据记录", httpMethod = "POST", notes = "修改术中体征监测数据记录")
    public R<String> updateAcisIntraoListenDataInfo(@ApiParam("修改体征监测的参数") @RequestBody AcisIntraoUpdateListenDataVO data) {
        String result = null;
        try {
            if (null == data) {
                //未键入查询参数
                log.info(CommonErrorCode.E100022.getMsg());
                return R.fail(CommonErrorCode.E100022.getCode(), CommonErrorCode.E100022.getMsg());
            } else {
                for (AcisIntraoListenDataVO acisIntraoListenDataVO : data.getList()) {
                    if (null == acisIntraoListenDataVO.getItemCode() || acisIntraoListenDataVO.getItemCode().equals("")) {
                        //未获取到体征项目参数
                        log.info(CommonErrorCode.E500035.getMsg());
                        return R.fail(CommonErrorCode.E500035.getCode(), CommonErrorCode.E500035.getMsg());
                    }
                    if (null == acisIntraoListenDataVO.getItemName() || acisIntraoListenDataVO.getItemName().equals("")) {
                        //未获取到体征名称
                        log.info(CommonErrorCode.E500036.getMsg());
                        return R.fail(CommonErrorCode.E500036.getCode(), CommonErrorCode.E500036.getMsg());
                    }
                    for (AcisIntraoListenDataItemValueVO acisIntraoListenDataItemValueVO : acisIntraoListenDataVO.getList()) {
                        if (null == acisIntraoListenDataItemValueVO.getItemValue() || acisIntraoListenDataItemValueVO.getItemValue().equals("")) {
                            //传入体征值为空
                            log.info(CommonErrorCode.E500037.getMsg());
                            return R.fail(CommonErrorCode.E500037.getCode(), CommonErrorCode.E500037.getMsg());
                        }
                        if (null == acisIntraoListenDataItemValueVO.getTimePoint() || acisIntraoListenDataItemValueVO.getTimePoint().equals("")) {
                            //传入体征时间节点为空
                            log.info(CommonErrorCode.E500038.getMsg());
                            return R.fail(CommonErrorCode.E500038.getCode(), CommonErrorCode.E500038.getMsg());
                        }
                    }
                }
            }
            //执行数据存储
            result = infoService.updateIntraoListenData(data.getOperationId(), data.getList());
        } catch (Exception e) {
            //数据修改失败
            log.info(CommonErrorCode.E100017.getMsg());
            throw new ACISException(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
        }
        return R.data(result);
    }

    @PostMapping("/editPatientMonitorDataExt")
    @ApiOperation(value = "修改术中患者体征数据", httpMethod = "POST", notes = "修改术中患者体征数据")
    public R<String> editPatientMonitorDataExt(@ApiParam("修改术中体征的参数") @RequestBody AcisIntraoUpdateMonitorDataVO data) {
        String result = null;
        try {
            if (null == data) {
                //未键入查询参数
                log.info(CommonErrorCode.E100022.getMsg());
                return R.fail(CommonErrorCode.E100022.getCode(), CommonErrorCode.E100022.getMsg());
            } else {
                if (null == data.getOperationId() || data.getOperationId().equals("")) {
                    //未获取到手术id
                    log.info(CommonErrorCode.E500028.getMsg());
                    return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
                }
                if (null == data.getDataMode() || !(data.getDataMode().equals("1") || data.getDataMode().equals("5"))) {
                    //体征模式参数设置
                    log.info(CommonErrorCode.E500039.getMsg() + "输入值" + data.getDataMode());
                    return R.fail(CommonErrorCode.E500039.getCode(), CommonErrorCode.E500039.getMsg());
                }
                for (AcisIntraoUpdateMonitorDataListVO acisIntraoUpdateMonitorDataListVO : data.getList()) {
                    if (null == acisIntraoUpdateMonitorDataListVO.getItemCode() || acisIntraoUpdateMonitorDataListVO.getItemCode().equals("")) {
                        //未获取到体征项目参数
                        log.info(CommonErrorCode.E500035.getMsg());
                        return R.fail(CommonErrorCode.E500035.getCode(), CommonErrorCode.E500035.getMsg());
                    }
                    if (null == acisIntraoUpdateMonitorDataListVO.getItemName() || acisIntraoUpdateMonitorDataListVO.getItemName().equals("")) {
                        //未获取到体征名称
                        log.info(CommonErrorCode.E500036.getMsg());
                        return R.fail(CommonErrorCode.E500036.getCode(), CommonErrorCode.E500036.getMsg());
                    }
                    for (AcisIntraoMonitorDataItemValueVO acisIntraoMonitorDataItemValueVO : acisIntraoUpdateMonitorDataListVO.getList()) {
                        if (null == acisIntraoMonitorDataItemValueVO.getItemValue() || acisIntraoMonitorDataItemValueVO.getItemValue().equals("")) {
                            //传入体征值为空
                            log.info(CommonErrorCode.E500037.getMsg());
                            return R.fail(CommonErrorCode.E500037.getCode(), CommonErrorCode.E500037.getMsg());
                        }
                        if (null == acisIntraoMonitorDataItemValueVO.getTimePoint() || acisIntraoMonitorDataItemValueVO.getTimePoint().equals("")) {
                            //传入体征时间节点为空
                            log.info(CommonErrorCode.E500038.getMsg());
                            return R.fail(CommonErrorCode.E500038.getCode(), CommonErrorCode.E500038.getMsg());
                        }
                    }
                }
            }
            //执行数据存储
            log.info(data);
            result = infoService.updateIntraoMonitorData(data.getOperationId(), data.getList(), data.getDataMode());
        } catch (Exception e) {
            //数据修改失败
            e.printStackTrace();
            log.info(CommonErrorCode.E100017.getMsg());
            throw new ACISException(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
        }
        return R.data(result);
    }

    @ApiOperation("获得术中登记数据记录")
    @PostMapping("/getAcisIntraoEventRegistList")
    public R<List<AcisIntraoEventRegistVO>> getAcisIntraoEventRegistList(@ApiParam("手术id") @RequestParam(required = true) String operationId,
                                                                         @RequestParam Integer type) {
        List<AcisIntraoEventRegistVO> list = null;
        try {
            if (StringUtils.isBlank(operationId)) {
                //未获取到手术id
                log.info(CommonErrorCode.E100020.getMsg());
                return R.fail(CommonErrorCode.E100020.getCode(), CommonErrorCode.E100020.getMsg());
            }
            list = infoService.getAllIntraoEventByOperId(operationId, type);
            if (null == list || list.size() == 0) {
                //未获取到信息
                log.info(CommonErrorCode.E600006.getMsg());
                return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(list);
    }


    @ApiOperation("添加/修改/删除术中登记数据记录")
    @PostMapping("/storageAcisIntraoEventRegistList")
    public R storageAcisIntraoEventRegistList(@ApiParam("术中登记数据") @RequestBody AcisStorageEventRegistVO param) {
        Integer result = null;
        try {
            if (null == param.getList()) {
                //传入参数为空
                log.info(CommonErrorCode.E100022.getMsg());
                return R.fail(CommonErrorCode.E100022.getCode(), CommonErrorCode.E100022.getMsg());
            } else {
                for (AcisStorageIntraoEventRegistVO acisStorageIntraoEventRegistVO : param.getList()) {
                    if (!StringUtils.isBlank(acisStorageIntraoEventRegistVO.getEventEndTime())) {
                        if (null == acisStorageIntraoEventRegistVO.getHoldingTime() || StringUtils.isBlank(acisStorageIntraoEventRegistVO.getHoldingTime())) {
                            //未填写持续时间
                            log.info(CommonErrorCode.E100028.getMsg());
                            return R.fail(CommonErrorCode.E100028.getCode(), CommonErrorCode.E100028.getMsg());
                        }
                    }
                    if (null == acisStorageIntraoEventRegistVO.getEventEndTime() || StringUtils.isBlank(acisStorageIntraoEventRegistVO.getEventEndTime())) {
                        acisStorageIntraoEventRegistVO.setEventEndTime(null);
                    }
                    if (null == acisStorageIntraoEventRegistVO.getIsHolding() || StringUtils.isBlank(acisStorageIntraoEventRegistVO.getIsHolding())) {
                        acisStorageIntraoEventRegistVO.setIsHolding("0");
                    }
                    String eventId = acisStorageIntraoEventRegistVO.getEventId();
                    String detailId = acisStorageIntraoEventRegistVO.getDetailId();
                    String operationId = acisStorageIntraoEventRegistVO.getOperationId();
                    if (param.getMode() == 0) {
                        //执行添加
                        if (eventId.equals("E001") || eventId.equals("E010") || eventId.equals("E012")) {
                            //判断该事件是否可以重复添加
                            Integer i = infoService.getEventIsSingle(eventId, detailId);
                            //判断该事件是否已经添加
                            Integer j = infoService.checkEventIsExist(eventId, detailId, operationId);
                            if (0 == j) {
                                //添加到表acis_intrao_event
                                result = infoService.storageIntraoEvent(acisStorageIntraoEventRegistVO);
                            } else {
                                if (0 == i) {
                                    //添加到表acis_intrao_event
                                    result = infoService.storageIntraoEvent(acisStorageIntraoEventRegistVO);
                                }
                            }
                        } else {
                            //若该药已经在使用且还没有停止 则禁止该药的添加
                            Integer i = infoService.checkIsUseAndNotStop(operationId, eventId, detailId);
                            if (0 != i) {
                                return R.fail(CommonErrorCode.E100001.getCode(), "该事件正在使用");
                            }
                            //添加到acis_intrao_pharmacy_event
                            result = infoService.storageIntraoMedAndBloodEvent(acisStorageIntraoEventRegistVO);
                        }
                        if (null == result) {
                            //数据添加失败
                            log.info(CommonErrorCode.E100027.getMsg());
                            return R.fail(CommonErrorCode.E100027.getCode(), CommonErrorCode.E100027.getMsg());
                        }
                    }
                    if (param.getMode() == 1) {
                        //执行修改
                        if (eventId.equals("E001") || eventId.equals("E010") || eventId.equals("E012")) {
                            //修改到表acis_intrao_event
                            result = infoService.unablentraoEvent(acisStorageIntraoEventRegistVO.getId());
                            result = infoService.updateIntraoEvent(acisStorageIntraoEventRegistVO);
                            //修改到acis_operation_data_arrange
                            String detailId1 = acisStorageIntraoEventRegistVO.getDetailId();
                            String eventStartTime = acisStorageIntraoEventRegistVO.getEventStartTime();
                            if (eventId.equals("E001")) {
                                String conCode = "0";
                                if ("1".equals(detailId1)) {
                                    conCode = "6";
                                } else if ("48".equals(detailId1)) {
                                    conCode = "7";
                                } else if ("45".equals(detailId1)) {
                                    conCode = "8";
                                } else if ("47".equals(detailId1)) {
                                    conCode = "9";
                                } else if ("49".equals(detailId1)) {
                                    conCode = "10";
                                } else if ("53".equals(detailId1)) {
                                    conCode = "11";
                                } else if ("51".equals(detailId1)) {
                                    conCode = "13";
                                } else if ("52".equals(detailId1)) {
                                    conCode = "14";
                                }
                                infoService.updateOpeTimePoint(operationId, conCode, eventStartTime);
                            }
                        } else {
                            //修改到acis_intrao_pharmacy_event
                            result = infoService.unableIntraoMedAndBloodEvent(acisStorageIntraoEventRegistVO.getId());
                            result = infoService.updateIntraoMedAndBloodEvent(acisStorageIntraoEventRegistVO);
                        }
                        if (null == result) {
                            //数据修改失败
                            log.info(CommonErrorCode.E100017.getMsg());
                            return R.fail(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
                        }
                    }
                    if (param.getMode() == 2) {
                        //执行删除
                        if (eventId.equals("E001") || eventId.equals("E010") || eventId.equals("E012")) {
                            //删除到表acis_intrao_event
                            result = infoService.deleteIntraoEvent(acisStorageIntraoEventRegistVO.getId());
                        } else {
                            //删除到acis_intrao_pharmacy_event
                            result = infoService.deleteIntraoMedAndBloodEvent(acisStorageIntraoEventRegistVO.getId());
                        }
                        if (null == result) {
                            //数据删除失败
                            log.info(CommonErrorCode.E900005.getMsg());
                            return R.fail(CommonErrorCode.E900005.getCode(), CommonErrorCode.E900005.getMsg());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(result);
    }

    @ApiOperation("获取术中输液输血麻药用药事件记录")
    @PostMapping("/getAcisIntraoBloodOrMedicineDataInfo")
    public R<List<AcisIntraoMedicineAndBloodListVO>> getAcisIntraoBloodOrMedicineDataInfo(@ApiParam("获取事件参数") @RequestBody AcisGetMediAndBloodParamVO params) {
        //获取术中输液输血事件记录
        List<AcisIntraoMedicineAndBloodListVO> list = null;
        try {
            if (StringUtils.isBlank(params.getOperationId())) {
                //未获取到手术id
                log.error(CommonErrorCode.E500028.getMsg());
                return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
            }
            if (StringUtils.isBlank(params.getStartTime()) || StringUtils.isBlank(params.getEndTime())) {
                //传入开始时间参数为空
                log.error(CommonErrorCode.E500034.getMsg());
                return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
            }
            if (params.getMode() < 1 || params.getMode() > 2) {
                //传入开始时间参数为空
                log.error(CommonErrorCode.E500040.getMsg());
                return R.fail(CommonErrorCode.E500040.getCode(), CommonErrorCode.E500040.getMsg());
            } else {
                if (params.getMode() == 1) {
                    //查询麻药用药
                    list = infoService.getcisIntraoMedicineList(params.getOperationId(), params.getStartTime(), params.getEndTime());
                    //判断查询结果
                    if (null == list || list.size() == 0) {
                        //未查询到消息
                        return R.data(CommonErrorCode.E600006.getCode(), list, CommonErrorCode.E600006.getMsg());
                    }
                }
                if (params.getMode() == 2) {
                    //查询输血输液
                    list = infoService.getcisIntraoBloodList(params.getOperationId(), params.getStartTime(), params.getEndTime());
                    //判断查询结果
                    if (null == list || list.size() == 0) {
                        //未查询到消息
                        return R.data(CommonErrorCode.E600006.getCode(), list, CommonErrorCode.E600006.getMsg());
                    }
                }
            }
        } catch (Exception e) {
            //查询信息失败
            log.info(CommonErrorCode.E600006.getMsg());
            throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("获取术中麻药用药事件记录")
    @PostMapping("/getAcisIntraoPharmacyDataInfo")
    public R<List<AcisIntraoMedicineAndBloodListVO>> getAcisIntraopharmacyDataInfo(@ApiParam("获取事件参数") @RequestBody AcisGetEventsParamVO params) {
        //获取术中麻药用药事件记录
        List<AcisIntraoMedicineAndBloodListVO> list = null;
        try {
            if (StringUtils.isBlank(params.getOperationId())) {
                //未获取到手术id
                log.error(CommonErrorCode.E500028.getMsg());
                return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
            }
            if (StringUtils.isBlank(params.getStartTime()) || StringUtils.isBlank(params.getEndTime())) {
                //传入开始时间参数为空
                log.error(CommonErrorCode.E500034.getMsg());
                return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
            }
            list = infoService.getcisIntraoMedicineList(params.getOperationId(), params.getStartTime(), params.getEndTime());
            //判断查询结果是否为空
            if (list.size() == 0) {
                //未查询到消息
                return R.data(200, list, CommonErrorCode.E600006.getMsg());
            }
        } catch (Exception e) {
            //查询信息失败
            e.printStackTrace();
            log.info(CommonErrorCode.E600006.getMsg());
            //throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("停止术中持续用药")
    @PostMapping("/stopPharmacyUse")
    public R stopPharmacyUse(@RequestParam("id") Integer id) {
        //查询该药是否是持续用药  该药是否已停止
        Map<String, Object> map = infoService.getIsHoldingAndEndTime(id);
        String isHolding = String.valueOf(map.get("is_holding"));
        String eventEndTime = String.valueOf(map.get("event_end_time"));
        if ("0".equals(isHolding)) {
            return R.fail("该药是非持续用药");
        }
        if (null != eventEndTime && !"".equals(eventEndTime) && !"null".equals(eventEndTime)) {
            return R.fail("该药已停止使用");
        }
        Integer i = infoService.stopPharmacyUse(id);
        return R.success("停止成功");
    }

    @ApiOperation("获取术中输液输血事件记录")
    @PostMapping("/getAcisIntraoBloodDataInfo")
    public R<List<AcisIntraoMedicineAndBloodListVO>> getAcisIntraoBloodDataInfo(@ApiParam("获取事件参数") @RequestBody AcisGetEventsParamVO params) {
        //获取术中输液输血事件记录
        List<AcisIntraoMedicineAndBloodListVO> list = null;
        try {
            if (StringUtils.isBlank(params.getOperationId())) {
                //未获取到手术id
                log.error(CommonErrorCode.E500028.getMsg());
                return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
            }
            if (StringUtils.isBlank(params.getStartTime()) || StringUtils.isBlank(params.getEndTime())) {
                //传入开始时间参数为空
                log.error(CommonErrorCode.E500034.getMsg());
                return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
            }
            list = infoService.getcisIntraoBloodList(params.getOperationId(), params.getStartTime(), params.getEndTime());
            //判断查询结果
            if (list.size() == 0) {
                //未查询到消息
                return R.data(200, list, CommonErrorCode.E600006.getMsg());
            }
        } catch (Exception e) {
            //查询信息失败
            log.info(CommonErrorCode.E600006.getMsg());
            throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("获得术中麻醉事件登记记录列表(通过事件id显示针对该事件的所有事件)")
    @GetMapping("/getOperationRegisteredInfoByEventId")
    public R<List<AcisIntraoPharmacyEventDetailsVO>> getOperationRegisteredInfoByEventId(@RequestParam(value = "operationId", required = true) String operationId,
                                                                                         @RequestParam(value = "eventId", required = true) String eventId) {
        List<AcisIntraoPharmacyEventDetailsVO> list = null;
        try {
            if (StringUtils.isBlank(operationId)) {
                //未获取到手术id
                log.info(CommonErrorCode.E500028.getMsg());
                return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
            }
            list = infoService.getIntraoPharmcyEventDetailListByEventId(operationId, eventId);
        } catch (Exception e) {
            log.info(CommonErrorCode.E100018.getMsg());
            throw new ACISException(CommonErrorCode.E100018.getCode(), CommonErrorCode.E100018.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("获得术中麻醉事件登记记录列表(该手术的所有大事件)")
    @GetMapping("/getOperationRegisteredInfo")
    public R<List<AcisIntraoPharmacyEventDetailsVO>> getOperationRegisteredInfo(@RequestParam(value = "operationId", required = true) String operationId) {
        List<AcisIntraoPharmacyEventDetailsVO> list = null;
        try {
            if (StringUtils.isBlank(operationId)) {
                //未获取到手术id
                log.info(CommonErrorCode.E500028.getMsg());
                throw new ACISException(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
            }
            list = infoService.getIntraoPharmcyEventDetailList(operationId);
            //获取数据非空判断
            if (null == list || list.size() == 0) {
                log.info(CommonErrorCode.E500028.getMsg());
                return R.data(CommonErrorCode.E500028.getCode(), list, CommonErrorCode.E500028.getMsg());
            }
        } catch (Exception e) {
            log.info(CommonErrorCode.E500028.getMsg());
            throw new ACISException(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("登记术中麻药用药输血输液事件数据记录")
    @PostMapping("/storageAnesthesiaMedicineBloodEvents")
    public R<Integer> storageAnesthesiaMedicineBloodEvents(@ApiParam("术中麻醉事件数据记录列表") @RequestBody List<AcisIntraoPharmacyEventVO> list) {
        Integer result = null;
        try {
            for (AcisIntraoPharmacyEventVO acisIntraoPharmacyEventVO : list) {
                boolean isOperationId = StringUtils.isBlank(acisIntraoPharmacyEventVO.getOperationId());
                boolean isEventId = StringUtils.isBlank(acisIntraoPharmacyEventVO.getEventId());
                if (isOperationId) {
                    return R.fail(CommonErrorCode.E100001.getCode(), "请填写手术ID！");
                }
                if (isEventId) {
                    return R.fail(CommonErrorCode.E100001.getCode(), "请填写事件ID！");
                }
                //若该药已经在使用且还没有停止 则禁止该药的添加
                Integer i = infoService.checkIsUseAndNotStop(acisIntraoPharmacyEventVO.getOperationId(), acisIntraoPharmacyEventVO.getEventId(), acisIntraoPharmacyEventVO.getDetailId());
                if (0 != i) {
                    return R.fail(CommonErrorCode.E100001.getCode(), "该事件正在使用");
                }
                //执行术中事件数据添加
                result = infoService.insertAcistraoPharmacyEvent(acisIntraoPharmacyEventVO);
                if (result == 0) {
                    //插入数据失败
                    log.error(CommonErrorCode.E100019.getMsg());
                    return R.data(CommonErrorCode.E100019.getCode(), result, CommonErrorCode.E100019.getMsg());
                } else {
                    //这里去执行  麻醉过程的麻药用药输血输液 记录的上传   上传使用WebService 接口
                    SendAnesthesiaProcessRecord sendAnesthesiaProcessRecord = infoService.getSendAnesthesiaProcessRecord(acisIntraoPharmacyEventVO.getOperationId());
                    if (null == sendAnesthesiaProcessRecord) {
                        return R.fail(CommonErrorCode.E100018.getCode(), CommonErrorCode.E100018.getMsg());
                    } else {
                        //补充元素属性
                        sendAnesthesiaProcessRecord.setEventNo(null == acisIntraoPharmacyEventVO.getEventId() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getEventId()) ? "" : acisIntraoPharmacyEventVO.getEventId());
                        sendAnesthesiaProcessRecord.setItemClass(null == acisIntraoPharmacyEventVO.getEventId() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getEventId()) ? "" : acisIntraoPharmacyEventVO.getEventId());
                        sendAnesthesiaProcessRecord.setItemCode(null == acisIntraoPharmacyEventVO.getDetailId() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getDetailId()) ? "" : acisIntraoPharmacyEventVO.getDetailId());
                        sendAnesthesiaProcessRecord.setItemName(null == acisIntraoPharmacyEventVO.getEventName() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getEventName()) ? "" : acisIntraoPharmacyEventVO.getEventName());
                        sendAnesthesiaProcessRecord.setDosage(null == acisIntraoPharmacyEventVO.getDosage() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getDosage()) ? "" : acisIntraoPharmacyEventVO.getDosage());
                        sendAnesthesiaProcessRecord.setDosageUnit(null == acisIntraoPharmacyEventVO.getDosageUnit() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getDosageUnit()) ? "" : acisIntraoPharmacyEventVO.getDosageUnit());
                        sendAnesthesiaProcessRecord.setPerformSpeed(null == acisIntraoPharmacyEventVO.getSpeed() ? "" : String.valueOf(acisIntraoPharmacyEventVO.getSpeed()));
                        sendAnesthesiaProcessRecord.setSpeedUnit(null == acisIntraoPharmacyEventVO.getSpeedUnit() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getSpeedUnit()) ? "" : acisIntraoPharmacyEventVO.getSpeedUnit());
                        sendAnesthesiaProcessRecord.setConcentration(null == acisIntraoPharmacyEventVO.getConcentration() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getConcentration()) ? "" : acisIntraoPharmacyEventVO.getConcentration());
                        sendAnesthesiaProcessRecord.setConcentrationUnit(null == acisIntraoPharmacyEventVO.getConcentrationUnit() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getConcentrationUnit()) ? "" : acisIntraoPharmacyEventVO.getConcentrationUnit());
                        sendAnesthesiaProcessRecord.setStartTime(acisIntraoPharmacyEventVO.getEventStartTime());
                        sendAnesthesiaProcessRecord.setEndTime(null == acisIntraoPharmacyEventVO.getEventEndTime() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getEventEndTime()) ? "" : acisIntraoPharmacyEventVO.getEventEndTime());
                        sendAnesthesiaProcessRecord.setUsage(null == acisIntraoPharmacyEventVO.getApproach() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getApproach()) ? "" : acisIntraoPharmacyEventVO.getApproach()); //用药方法
                        sendAnesthesiaProcessRecord.setEventAttr(null == acisIntraoPharmacyEventVO.getEventType() || StringUtils.isBlank(acisIntraoPharmacyEventVO.getEventType()) ? "" : acisIntraoPharmacyEventVO.getEventType()); //事件属性
                        XStream xStream = new XStream(new DomDriver(null, new XmlFriendlyNameCoder("_-", "_")));
                        xStream.processAnnotations(SendAnesthesiaProcessRecord.class);
                        String xmlDtoStr = xStream.toXML(sendAnesthesiaProcessRecord);
                        //这里调用  WebService的麻药用药输血输液事件上传
                        //执行 -> SendAnesthesiaProcessRecord -> 麻醉过程记录信息
                        String dataXmlParam = XmlUtils.trimStr(xmlDtoStr);//.trim().replaceAll("\\s*|\t|\r|\n","");
                        StringBuffer SendAnesthesiaSignData = new StringBuffer().append(WebServiceConstant.HEAD_BEFORE).append("SendAnesthesiaProcessRecord").append(WebServiceConstant.HEAD_MIDDLE).append(dataXmlParam).append(WebServiceConstant.HEAD_AFTER);
                        //WebserviceUtils.postWithoutSoap(WebServiceConstant.URL,SendAnesthesiaSignData);
                        System.out.println(SendAnesthesiaSignData);
                    }
                }
            }
        } catch (Exception e) {
            log.error(CommonErrorCode.E900002.getMsg());
            //添加信息失败异常捕获
            return R.fail(CommonErrorCode.E900002.getCode(), CommonErrorCode.E900002.getMsg());
        }
        return R.data(result);
    }

    @ApiOperation("登记术中事件数据记录")
    @PostMapping("/addAcisIntraoEvents")
    public R<Integer> addAcisIntraoEvents(@ApiParam("术中事件记录列表") @RequestBody List<AcisStorageIntraoEventDictVO> list) {
        Integer result = null;
        try {
            for (AcisStorageIntraoEventDictVO acisStorageIntraoEventDictVO : list) {
                if (StringUtils.isBlank(acisStorageIntraoEventDictVO.getOperationId())) {
                    //请填写手术ID
                    log.info(CommonErrorCode.E500028.getMsg());
                    return R.fail(CommonErrorCode.E500028.getCode(), CommonErrorCode.E500028.getMsg());
                }
                if (null == acisStorageIntraoEventDictVO.getEventStartTime()) {
                    //请填写事件开始时间
                    log.info(CommonErrorCode.E500034.getMsg());
                    return R.fail(CommonErrorCode.E500034.getCode(), CommonErrorCode.E500034.getMsg());
                }
                if (acisStorageIntraoEventDictVO.getEventEndTime().equals("")) {
                    acisStorageIntraoEventDictVO.setEventEndTime(null);
                }
            }
            result = infoService.storageAcisIntraoEvent(list);
        } catch (Exception e) {
            //数据保存失败
            log.info(CommonErrorCode.E100019.getMsg());
            throw new ACISException(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }
        return R.data(result);
    }

    @ApiOperation("删除术中麻醉事件记录")
    @PostMapping("/deleteAnesthesiaEvents")
    public R<Integer> deleteAnesthesiaEvents(@ApiParam("需要删除的事件列表") @RequestBody List<AcisIntraoDeleteEvents> list) {
        Integer result = null;
        try {
            for (AcisIntraoDeleteEvents acisIntraoDeleteEvents : list) {
                //判断传输参数非空
                boolean isOperationId = StringUtils.isBlank(acisIntraoDeleteEvents.getOperationId());
                boolean isDetailID = StringUtils.isBlank(acisIntraoDeleteEvents.getDetailId());
                boolean isEventId = StringUtils.isBlank(acisIntraoDeleteEvents.getEventId());
                if (isOperationId) {
                    //判断手术id是否填写
                    return R.fail(CommonErrorCode.E100001.getCode(), CommonErrorCode.E100001.getMsg());
                }
                if (isDetailID) {
                    //判断二级事件是否填写
                    return R.fail(CommonErrorCode.E100001.getCode(), CommonErrorCode.E100001.getMsg());
                }
                if (isEventId) {
                    //判断事件id是否填写
                    return R.fail(CommonErrorCode.E100001.getCode(), CommonErrorCode.E100001.getMsg());
                }
                result = infoService.deleteAcistraoPharmacyEvents(list);
            }
        } catch (Exception e) {
            //删除数据失败错误信息
            log.error(CommonErrorCode.E900005.getMsg());
            //添加信息失败异常捕获
            return R.fail(CommonErrorCode.E900005.getCode(), CommonErrorCode.E900005.getMsg());
        }
        return R.data(result);
    }

    @ApiOperation("术中麻醉手术交接")
    @PostMapping("/operationExchange")
    public R<Integer> operationExchange(@ApiParam("术中交班信息入参") @RequestBody List<AcisIntraoOperaExchangeVO> list) {
        //术中麻醉手术交接
        Integer result;
        //获得手术id
        String operationId = list.get(0).getOperationId();
        if (StringUtils.isBlank(operationId)) {
            //判断手术id是否为空
            return R.fail(CommonErrorCode.E100020.getCode(), CommonErrorCode.E100020.getMsg());
        }
        try {
            //执行交接班信息
            result = infoService.addAcisIntraoOperationExchange(list, operationId);
        } catch (Exception e) {
            //错误信息捕获
            log.error(CommonErrorCode.E100019.getMsg());
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }
        return R.data(result);
    }

    @ApiOperation("获得手术交班信息")
    @PostMapping("/getOperationExchangeInfo")
    public R<AcisGetOperExChangeInfoVO> getOperationExchangeInfo(@ApiParam("手术id") @RequestBody AcisOperationIdInfoVO operationId) {
        Integer result;
        AcisGetOperExChangeInfoVO acisGetOperExChangeInfoVO;
        try {
            //获得手术id
            if (StringUtils.isBlank(operationId.getOperationId())) {
                //判断手术id是否为空
                return R.fail(CommonErrorCode.E100020.getCode(), CommonErrorCode.E100020.getMsg());
            }
            acisGetOperExChangeInfoVO = infoService.getOperExChangeInfo(operationId.getOperationId());
            if (null == acisGetOperExChangeInfoVO) {
                return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
            }
        } catch (Exception e) {
            //错误信息捕获
            log.error(CommonErrorCode.E600006.getMsg());
            return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(acisGetOperExChangeInfoVO);
    }

    @ApiOperation("用于批量添加和修改术中体征/监测 数据")
    @PostMapping("/editAcisIntraoMonitorAndListenData")
    public R editAcisIntraoMonitorAndListenData(@ApiParam("术中登记修改/添加数据入参") @RequestBody AcisAddAndDelMonitorAndListenParamsVO params) {
        R result = null;
        try {
            String operationId = params.getOperationId();
            List<AcisAddAndDelMonitorAndListenDetailVO> list = params.getList();
            Integer mode = params.getMode();
            if (1 != mode && 2 != mode) {
                //传入参数提示
                log.error(CommonErrorCode.E500040.getMsg());
                return R.fail(CommonErrorCode.E500040.getCode(), CommonErrorCode.E500040.getMsg());
            } else {
                //模式 1体征 2监测
                result = infoService.editAcisIntraoMonitorAndListenData(operationId, mode, list);
            }
        } catch (Exception e) {
            //数据修改失败
            e.printStackTrace();
            log.error(CommonErrorCode.E100017.getMsg());
            throw new ACISException(CommonErrorCode.E100017.getCode(), CommonErrorCode.E100017.getMsg());
        }
        return result;
    }


    @ApiOperation("综合测试接口")
    @GetMapping("/medicineCalculate")
    public R medicineCalculate() {
        //用于权限系统项目菜单项
        String uuid = UUID.randomUUID().toString().substring(0, 7);
        String url = request.getRequestURI();
        System.out.println(url);
        //用户权限系统用户角色id项
        String userRoleId = UUID.randomUUID().toString().substring(0, 8);
        //System.out.println(MenuPermsEnum.valueOf(""));
        return R.data(uuid, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("查询签名")
    @GetMapping("/getSignature")
    public R getSignature(@RequestBody SignatureVo signatureVo) {
        String signature = infoService.getSignature(signatureVo);
        return R.data(signature);
    }

    @ApiOperation("套用模版")
    @PostMapping("/useTemplateDuringOperation/{operationId}")
    @Transactional(rollbackFor = Exception.class)
    public R useTemplateDuringOperation(@RequestBody List<EventInfoVo> eventInfoVos, @PathVariable String operationId) {
        if (null == eventInfoVos || eventInfoVos.size() == 0) {
            return R.fail("不能为空");
        }
        for (EventInfoVo eventInfoVo : eventInfoVos) {
            String id = eventInfoVo.getId();
            //获取事件详情
            Map<String, Object> map = infoService.getEventInfoDetail(Integer.parseInt(id));
            map.put("operationId", operationId);
            //获取事件的code  判断事件是否可以重复添加
            String eventId = String.valueOf(map.get("event_id"));
            String detailId = String.valueOf(map.get("detail_id"));
            String isSingle = String.valueOf(map.get("is_single"));
            String drawIcon = String.valueOf(map.get("draw_icon"));
            if ("0".equals(isSingle)) {
                //若不可以重复添加 则判断该手术是否已有该事件
                Integer i = infoService.checkEventIsExist(eventId, detailId, operationId);
                if (0 == i) {
                    //若没有该事件  则添加该事件
                    infoService.addEventInfo1(eventInfoVo, operationId, drawIcon);
                }
            } else if ("1".equals(isSingle)) {
                //若可以重复添加  则直接添加
                infoService.addEventInfo1(eventInfoVo, operationId, drawIcon);
            }
        }
        return R.success("");
    }

    /**
     * @author Neon Xie
     * @Date 2021/3/24
     * @description 个性化体征模板显示
     */
    @ApiOperation("个性化体征模板显示")
    @GetMapping("/getIntraoMonitorindividuation")
    public R< List<HashMap<String, String>> > getIntraoMonitorindividuation(@ApiParam("体征模板编号") @RequestParam String templeteCode ){
        //根据 体征模板id,查询所有体征信息
        if(StringUtils.isBlank(templeteCode)){
            return R.fail(CommonErrorCode.E100001.getCode(), "模板ID为空!");
        }
        log.info("个性体征模板编号为:->"+ templeteCode);
        return R.data(infoService.getIntraoMonitorData(templeteCode)) ;
    }

    /**
     * @author Neon Xie
     * @Date 2021/3/24
     * @description 更新个性化体征模板显示
     */
    @ApiOperation("更新个性化体征显示")
    @PostMapping("updateIntraoMonitorIndividuation")
    @Transactional(rollbackFor = Exception.class)
    public R updateIntraoMonitorData(@ApiParam("更新的体征信息数据") @RequestBody String intraoMonitorDatas){
        Integer result = null;

        try {
            List<Map<String,String>> dataList =  (List<Map<String,String>>)JSON.parse(intraoMonitorDatas);
            result = infoService.updateIntraoMonitorIndividuation(dataList);
            if (null == result) {
                return R.fail(CommonErrorCode.E900007.getCode(),"更新数据失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(result);
    }
}

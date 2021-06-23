package com.acis.controller.lyf.sysIntegration;

import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.util.dateUtils.DateUtil;
import com.acis.common.util.strUtils.StringUtil;
import com.acis.common.util.strUtils.SysIntegrationUtil;
import com.acis.pojo.AcisSiTestInfo;
import com.acis.pojo.lyf.AcisSiEmrInfo1;
import com.acis.pojo.lyf.dto.ExamInfoDto;
import com.acis.pojo.lyf.dto.OrderInfoDto;
import com.acis.pojo.lyf.vo.response.*;
import com.acis.pojo.viewdocking.ViewPatInspectionApply;
import com.acis.pojo.viewdocking.ViewPatInspectionReport;
import com.acis.service.lyf.sysIntegration.EmrService;
import com.acis.service.lyf.sysIntegration.ExamService;
import com.acis.service.lyf.sysIntegration.OrderService;
import com.acis.service.lyf.sysIntegration.TestService;
import com.acis.service.lyf.systemConfig.OpeArrangeService;
import com.acis.service.viewdocking.ViewDockingService;
import com.alibaba.druid.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "系统集成")
@RestController
@RequestMapping("/acis/sysIntegration")
@CrossOrigin
public class SysIntegrationController {

    @Autowired
    private OpeArrangeService opeArrangeService;

    @Autowired
    private EmrService emrService;

    @Autowired
    private ExamService examService;

    @Autowired
    private TestService testService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ViewDockingService viewDockingService;

    /**
     * @author Neon Xie
     * @Date 2021/1/4
     * @description 获得冬雷医院检验申请单信息列表
     */
    @ApiOperation("获得冬雷医院检验申请单信息列表")
    @PostMapping("/getBDGInspectionApply")
    public R<List<ViewPatInspectionApply>> getBDGInspectionApply(@RequestParam("patientId") String patientId,
                                   @RequestParam("startTime") String startTime,
                                   @RequestParam("endTime") String endTime){
        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        R<List<ViewPatInspectionApply>> result = null;
        try {
            result = viewDockingService.getBDGPatInspectionApply(startTime, endTime,patientId);
            if(null==result){
                return R.data(new ArrayList<>(),CommonErrorCode.E100018.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @author Neon Xie
     * @Date 2021/1/4
     * @description 获取检验报告单信息列表
     */
    @ApiOperation("获取检验报告单信息列表")
    @PostMapping("/getBEGInspectionReport")
    public R getBEGInspectionReport(@ApiParam("申请单号") @RequestParam("inspectionNo") String inspectionNo){
        R<List<ViewPatInspectionReport>> result = null;
        try {
            result = viewDockingService.getBDGPatInspectionReport(inspectionNo);
            if(null==result){
                return R.data(new ArrayList<>(),CommonErrorCode.E100018.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 检验信息查询
     *
     * @param patientId
     * @return
     */
    @ApiOperation("检验信息查询")
    @GetMapping("/getTestInfo")
    public R<List<AcisSiTestInfo>> getTestInfo(@RequestParam("patientId") String patientId,
                                               @RequestParam("startTime") String startTime,
                                               @RequestParam("endTime") String endTime) {
        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        //查询本地中某患者一个月内的检验信息
        List<AcisSiTestInfo> testInfoInAcis = testService.getTestInfo(patientId, startTime, endTime);

        return R.data(testInfoInAcis);
    }

    @ApiOperation("检验信息同步")
    @GetMapping("/syncTestInfo")
    public R syncTestInfo(@RequestParam("patientId") String patientId,
                          @RequestParam("startTime") String startTime,
                          @RequestParam("endTime") String endTime) {

        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        Boolean isSuccess = SysIntegrationUtil.syncTestExamEmr("1", patientId, startTime, endTime);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "检验信息同步失败！");
        }
        return getTestInfo(patientId, startTime, endTime);
    }

    /**
     * 检查信息查询
     * @param patientId
     * @return
     */
    @ApiOperation("检查信息查询")
    @GetMapping("/getExamInfo")
    public R<List<ExamInfoDto>> getExamInfo(@RequestParam("patientId") String patientId,
                                            @RequestParam("startTime") String startTime,
                                            @RequestParam("endTime") String endTime) {

        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        //本地查询患者一个月内的检查信息
        List<ExamInfoDto> examInfoInAcis = examService.getExamInfo(patientId, startTime, endTime);
        return R.data(examInfoInAcis);
    }

    @ApiOperation("检查信息同步")
    @GetMapping("/syncExamInfo")
    public R syncExamInfo(@RequestParam("patientId") String patientId,
                          @RequestParam("startTime") String startTime,
                          @RequestParam("endTime") String endTime) {

        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        Boolean isSuccess = SysIntegrationUtil.syncTestExamEmr("2", patientId, startTime, endTime);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "检查信息同步失败！");
        }
        return getExamInfo(patientId, startTime, endTime);
    }

    /**
     * 放在主系统中
     * @param patientId
     * @return
     */
    @ApiOperation("医嘱信息查询")
    @GetMapping("/getOrderInfo")
    public R getOrderInfo(@RequestParam("patientId") String patientId, @RequestParam("dateStr") String dateStr) {
        if (StringUtil.isEmpty(dateStr)){
            throw new ACISException(CommonErrorCode.E500000);
        }
        if (StringUtils.isEmpty(patientId)){
            throw new ACISException(CommonErrorCode.E500001);
        }
        Date date = DateUtil.parseDate(dateStr);
        if (date == null){
            throw new ACISException(CommonErrorCode.E500003);
        }
        //查询指定患者的医嘱信息
        List<OrderInfoDto> medOdOrderInfos = orderService.selectOrderInfoList(date, patientId);
        if (medOdOrderInfos == null || medOdOrderInfos.size() ==0){
            return R.data(new ArrayList<>());
        }
        List<OrderInfoVO> result = new ArrayList<>(medOdOrderInfos.size());
        OrderInfoVO orderInfoVO = null;
        for (OrderInfoDto infoDto : medOdOrderInfos) {
            orderInfoVO = new OrderInfoVO();
            BeanUtils.copyProperties(infoDto, orderInfoVO);
            result.add(orderInfoVO);
        }
        if (result.size() > 0){
            Comparator<OrderInfoVO> comparing = Comparator.comparing(OrderInfoVO::getOrderId);
            Collections.sort(result, comparing);
        }
        return R.data(result);
    }

    @ApiOperation("医嘱信息同步")
    @GetMapping("/syncOrderInfo")
    public R syncOrderInfo(@RequestParam("patientId") String patientId, @RequestParam("dateStr") String dateStr) {
        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        Boolean isSuccess = SysIntegrationUtil.syncTestExamEmr("4", patientId, dateStr, null);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "医嘱信息同步失败！");
        }
        return getOrderInfo(patientId, dateStr);
    }

    /**
     * 放在主系统中
     * @param patientId
     * @return
     */
    @ApiOperation("病历信息查询")
    @GetMapping("/getEmrInfo")
    public R<List<AcisSiEmrInfo1>> getEmrInfo(@RequestParam("patientId") String patientId,
                                              @RequestParam("startTime") String startTime,
                                              @RequestParam("endTime") String endTime) {
        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        //从本地获取患者前一个月的病历病程
        List<AcisSiEmrInfo1> emrInfoInAcis = emrService.getMedHistory(patientId, startTime, endTime);
        return R.data(emrInfoInAcis);
    }

    @ApiOperation("病历信息同步")
    @GetMapping("/syncEmrInfo")
    public R<List<AcisSiEmrInfo1>> syncEmrInfo(@RequestParam("patientId") String patientId,
                                               @RequestParam("startTime") String startTime,
                                               @RequestParam("endTime") String endTime) {
        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }

        Boolean isSuccess = SysIntegrationUtil.syncTestExamEmr("3", patientId, startTime, endTime);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "病历信息同步失败！");
        }
        return getEmrInfo(patientId, startTime, endTime);
    }

    @ApiOperation("检验信息曲线图查询")
    @GetMapping("/getTestInfoLine")
    public R getTestInfoLine(@RequestParam("itemCode") String itemCode,
                             @RequestParam("patientId") String patientId,
                             @RequestParam("startTime") String startTime,
                             @RequestParam("endTime") String endTime) {
        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == startTime || "".equals(startTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        if (null == endTime || "".equals(endTime)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }

        List<TestInfoVo> testInfoList = testService.getTestInfoLine(itemCode, patientId, startTime, endTime);

        return R.data(testInfoList);
    }

    @ApiOperation("体征信息同步并显示")
    @GetMapping("/getSignInfo/{patientId}/{operationId}")
    public R<List<HisSignInfoVo>> getSignInfo(@PathVariable String patientId, @PathVariable String operationId) {

        if (null == patientId || "".equals(patientId)) {
            return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
        }
        //同步his体征数据
        Boolean isSuccess = SysIntegrationUtil.syncSignInfo(patientId, operationId);
        if (!isSuccess) {
            return R.fail(CommonErrorCode.E100013.getCode(), "体征信息同步失败!");
        }
        //查询his体征数据
        List<HisSignInfoVo> signInfoList = testService.getSignInfoList(operationId);
        //麻醉方法提取
        Map<String, String> anesMethod = opeArrangeService.getAnesMethod(operationId);
        HisSignInfoVo hisSignInfoVo = new HisSignInfoVo();
        hisSignInfoVo.setTagName("anesMethod");
        hisSignInfoVo.setSignValue(anesMethod.get("detail_name"));
        hisSignInfoVo.setSignCode(anesMethod.get("anes_method"));
        hisSignInfoVo.setPatientId(patientId);
        hisSignInfoVo.setOperationId(operationId);
        hisSignInfoVo.setSignUnit("");
        signInfoList.add(hisSignInfoVo);
        return R.data(signInfoList);
    }

    @ApiOperation("术前访视检验数据查询")
    @GetMapping("/getTestInfo/{patientId}")
    public R<List<TestItemVo>> getTestInfo(@PathVariable String patientId) {
        try {
            if (null == patientId || "".equals(patientId)) {
                return R.fail(CommonErrorCode.E100001.getCode(), "参数不能为空！");
            }
            //获取检验数据
            List<TestItemVo> testItemVos = testService.getTestItem(patientId);
            //查询术前访视单检验数据列表
            String type = "2";
            List<HisSignVo> testItemList = testService.getTestItemList(type);

            if (null != testItemVos && testItemVos.size() > 0) {
                for (TestItemVo itemVo : testItemVos) {
                    if (null != testItemList && testItemList.size() > 0) {
                        for (HisSignVo signVo : testItemList) {
                            if (itemVo.getItemCode().equals(signVo.getCode())) {
                                itemVo.setTagName(signVo.getTagName());
                                break;
                            }
                        }
                    }
                }
            }
            return R.data(testItemVos);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CommonErrorCode.E100002.getCode(), "术前访视检验数据查询失败");
        }

    }
}

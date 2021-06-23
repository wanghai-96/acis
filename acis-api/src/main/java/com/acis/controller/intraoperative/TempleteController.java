package com.acis.controller.intraoperative;

import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.dao.intraoperative.InfoMapper;
import com.acis.pojo.intraoperative.dto.info.AcisDictAnesEventDetailDTO;
import com.acis.pojo.intraoperative.vo.info.AcisAddAcisIntraoEventTempleteVO;
import com.acis.pojo.intraoperative.vo.templete.*;
import com.acis.service.intraoperative.InfoService;
import com.acis.service.intraoperative.TempleteService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 术中文书模板功能
 */
@Api(tags = "术中文书模板功能")
@RestController
@CrossOrigin
@Log4j2
@RequestMapping("/acis/intraoperative/templete")
public class TempleteController {

    @Autowired
    private InfoService infoService;

    @Autowired
    private TempleteService templeteService;

    @ApiOperation("获得术中登记右侧的全部事件列表和code")
    @GetMapping("/getAcisIntraoEventCodeList")
    public R<List<AcisIntraoEventCodeListVO>> getAcisIntraoEventCodeList() {
        List<AcisIntraoEventCodeListVO> list;
        try {
            list = templeteService.getAcisIntraoEventCodeList();
            if (null == list || list.size() == 0) {
                return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(CommonErrorCode.E600006.getMsg());
            throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }

    @ApiOperation("根据事件code获得事件详情列表")
    @PostMapping("/getAcisIntraoEventDetailByCodeList")
    public R<List<AcisIntraoAnesEventDetailVO>> getAcisIntraoEventDetailByCodeList(@ApiParam("事件码") @RequestParam(required = true) String eventId,
                                                                                   @RequestParam(required = false,value = "eventName")String eventName) {
        List<AcisIntraoAnesEventDetailVO> list = null;
        try {
            if (StringUtils.isBlank(eventId)) {
                //传入参数为空
                log.error(CommonErrorCode.E100022.getMsg());
            } else {
                list = templeteService.getAcisAnesEventDetail(eventId, eventName);
                if (null == list || list.size() == 0) {
                    return R.fail(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(CommonErrorCode.E600006.getMsg());
            throw new ACISException(CommonErrorCode.E600006.getCode(), CommonErrorCode.E600006.getMsg());
        }
        return R.data(list);
    }



    @ApiOperation("添加术中登记体征/监测 数据模板项目")
    @PostMapping("/addAcisIntraoEventTemplete")
    public R addAcisIntraoEventTemplete(@ApiParam("项目修改参数") @RequestBody AcisAddAcisIntraoEventTempleteVO param) {
        R result = null;
        try {
            if (1 != param.getMode() && 2 != param.getMode()) {
                log.info(CommonErrorCode.E500040.getMsg());
                return R.fail(CommonErrorCode.E500040.getCode(), CommonErrorCode.E500040.getMsg());
            } else {
                if (1 == param.getMode()) {
                    //执行添加体征模板
                    result = templeteService.addIntraoEventTempletMonitorData(param.getItemCode(), param.getItemName());
                } else {
                    //执行添加监测模板
                    result = templeteService.addIntraoEventTempleteListenData(param.getItemCode(), param.getItemName());
                }
            }
        } catch (Exception e) {
            //添加失败
            e.printStackTrace();
            log.info(CommonErrorCode.E900002.getMsg());
            throw new ACISException(CommonErrorCode.E900002.getCode(), CommonErrorCode.E900002.getMsg());
        }
        return result;
    }

    @ApiOperation("删除术中登记体征/监测 数据模板项目")
    @PostMapping("/deleteAcisIntraoEventTemplete")
    public R deleteAcisIntraoEventTemplete(@ApiParam("项目修改参数") @RequestBody AcisAddAcisIntraoEventTempleteVO param) {
        R result = null;
        try {
            if (1 != param.getMode() && 2 != param.getMode()) {
                log.info(CommonErrorCode.E500040.getMsg());
                return R.fail(CommonErrorCode.E500040.getCode(), CommonErrorCode.E500040.getMsg());
            } else {
                if (1 == param.getMode()) {
                    //执行删除体征模板
                    result = templeteService.deleteIntraoEventTempletMonitorData(param.getItemCode());
                } else {
                    //执行删除监测模板
                    result = templeteService.deleteIntraoEventTempleteListenData(param.getItemCode());
                }
            }
        } catch (Exception e) {
            //添加失败
            e.printStackTrace();
            log.info(CommonErrorCode.E900005.getMsg());
            throw new ACISException(CommonErrorCode.E900005.getCode(), CommonErrorCode.E900005.getMsg());
        }
        return result;
    }

    @ApiOperation("修改模板名称(重命名)")
    @PostMapping("/changeAcisIntraoTempleteName")
    public R changeAcisIntraoTempleteName(@ApiParam("id") @RequestParam(value = "id", required = true) String id,
                                          @ApiParam("名称") @RequestParam(value = "name", required = true) String name) {
        R result;
        try {
            if (StringUtils.isBlank(id)) {
                //参数不能为空
                log.info(CommonErrorCode.E100001.getMsg());
                return R.fail(CommonErrorCode.E100001.getCode(), CommonErrorCode.E100001.getMsg());
            }
            if (StringUtils.isBlank(name)) {
                //参数不能为空
                log.info(CommonErrorCode.E100001.getMsg());
                return R.fail(CommonErrorCode.E100001.getCode(), CommonErrorCode.E100001.getMsg());
            }
            result = templeteService.changeAcisIntraoTempleteName(id, name);
        } catch (Exception e) {
            //重命名更新失败
            e.printStackTrace();
            log.info(CommonErrorCode.E900007.getMsg());
            throw new ACISException(CommonErrorCode.E900007.getCode(), CommonErrorCode.E900007.getMsg());
        }
        return result;
    }

    @ApiOperation("单击某个大事件显示对应事件左侧列表和详细信息")
    @GetMapping("/getAnesthesuaBigEventDetails")
    public R<PageInfo<AcisDictAnesEventDetailVO>> getAnesthesuaBigEventDetails(@ApiParam("大事件id") @RequestParam("eventCode") String eventCode,
                                                                               @ApiParam("当前页数") @RequestParam("pageIndex") Integer pageIndex,
                                                                               @ApiParam("页数大小") @RequestParam("pageSize") Integer pageSize,
                                                                               @ApiParam("模糊查询条件") @RequestParam(value = "inputCode", required = false) String inputCode) {
        PageInfo<AcisDictAnesEventDetailVO> pageInfo = templeteService.getAcisDictAnesBigEventDetails(eventCode, pageIndex, pageSize, inputCode);
        return R.data(pageInfo);
    }

    /**
     * 术中麻醉事件模板添加
     *
     * @param param
     * @return
     */
    @ApiOperation("术中麻醉事件模板添加")
    @PostMapping("/addAnesthesiaEventTemplete")
    public R addAnesthesiaEventTemplete(@ApiParam("术中麻醉事件数据记录列表") @RequestBody AcisStroageIntraoTempleteVO param) {
        //添加术中模板
        R result = null;
        try {
            if (null == param) {
                //参数不能为空
                log.info(CommonErrorCode.E100022.getMsg());
                throw new ACISException(CommonErrorCode.E100022.getCode(), CommonErrorCode.E100022.getMsg());
            } else {
                result = templeteService.addAnesthesiaEventTemplete(param.getOperationId(), param.getTempleteName(), param.getTempleteParentName(), param.getHavingWay(), param.getList());
            }
        } catch (Exception e) {
            //保存失败
            e.printStackTrace();
            log.info(CommonErrorCode.E100019.getMsg());
            throw new ACISException(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100019.getMsg());
        }
        return result;
    }

    @ApiOperation("术中麻醉事件模板删除通过模板id")
    @PostMapping("/deleteAnesthesiaEventTemplete")
    public R deleteAnesthesiaEventTemplete(@ApiParam("模板id") @RequestParam("modeId") Integer modeId) {
        R result = null;
        try {
            //这里是逻辑删除,当业务需要的时候依旧可以重启相关值
            result = templeteService.deleteAcisIntraoModeById(modeId);
        } catch (Exception e) {
            //删除数据失败
            e.printStackTrace();
            log.error(CommonErrorCode.E600003.getMsg());
            throw new ACISException(CommonErrorCode.E600003.getCode(), CommonErrorCode.E600003.getMsg());
        }
        return result;
    }

    @ApiOperation("通过模板id和事件id编辑模板中对应的事件")
    @PostMapping("/editAnesIntraoTempleteDetail")
    public R editAnesIntraoTempleteDetail(@ApiParam("编辑模板详情") @RequestBody AcisStorageTempleteEventVO param) {
        R result;
        try {
            //模式参数 1执行删除 2执行更新
            log.info("进入方法");
            Integer mode = param.getMode();
            if (1 != mode && 2 != mode) {
                //参数值过滤
                log.info("执行删除");
                log.info(CommonErrorCode.E500040.getMsg());
                return R.fail(CommonErrorCode.E500040.getCode(), CommonErrorCode.E500040.getMsg());
            } else {
                log.info("执行更新");
                result = templeteService.editIntraoTempleteDetail(mode, param.getList());
            }
        } catch (Exception e) {
            //数据更新失败
            e.printStackTrace();
            log.info(CommonErrorCode.E900007.getMsg());
            throw new ACISException(CommonErrorCode.E900007.getCode(), CommonErrorCode.E900007.getMsg());
        }
        return result;
    }

    @ApiOperation("通过模板id和事件id删除模板中对应的事件(已测试)")
    @PostMapping("/deleteAnesthesiaEventTempleteDetail")
    public R<Integer> deleteAnesthesiaEventTempleteDetail(@ApiParam("模板id") @RequestParam("modeId") Integer modeId,
                                                          @ApiParam("事件id") @RequestParam("eventId") String eventId,
                                                          @ApiParam("二级事件id") @RequestParam("detailId") String detailId) {
        //逻辑删除
        Integer result = templeteService.deleteAcisIntraoModeDetailByIdEventId(modeId, eventId, detailId);
        return R.data(result, ResultCode.SUCCESS.getMessage());
    }


    /**
     * 通过模板id获取术中麻醉事件模板详情
     *
     * @param templeteId
     * @return
     */
    @ApiOperation("通过模板id获取术中麻醉事件模板详情")
    @PostMapping("/getAnesthesiaEventTemplete")
    public R<List<AcisIntraoModeDetailVO>> getAnesthesiaEventTemplete(@ApiParam("模板id") @RequestParam(required = true) Integer templeteId) {
        //获取事件模板列表,用于右侧的菜单显示对应的模板详情
        List<AcisIntraoModeDetailVO> acisIntraoModeDetailVOList = null;
        try {
            acisIntraoModeDetailVOList = templeteService.getAcisIntraoModeDetailVOListById(templeteId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(acisIntraoModeDetailVOList, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获取术中麻醉事件模板左侧列表(已测试)")
    @GetMapping("/getAnesthesiaEventTempleteList")
    public R<List<AcisIntraoModeHavingWayVO>> getAnesthesiaEventTempleteList() {
        //获取事件模板列表,用于左侧的菜单显示
        List<AcisIntraoModeHavingWayVO> acisIntraoModeHavingWayVOList = templeteService.getIntraoTempleteModeList();
        System.out.println(acisIntraoModeHavingWayVOList);
        return R.data(acisIntraoModeHavingWayVOList, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("术中麻醉清点模板添加")
    @PostMapping("/addAnesthesiaCheckTemplete")
    public R addAnesthesiaCheckTemplete(@ApiParam("患者id") String patientId) {
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获取术中麻醉清点模板")
    @GetMapping("/getAnesthesiaCheckTemplete")
    public R getAnesthesiaCheckTemplete(@ApiParam("患者id") String patientId) {
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("右键麻醉事件模板数据获取")
    @GetMapping("/getRightAnesthesiaEventTempleteData")
    public R getRightAnesthesiaEventTempleteData(@ApiParam("患者id") String patientId) {
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中事件字典模板信息")
    @GetMapping("/getAnesthesiaEventDictTemplete")
    public R getAnesthesiaEventDictTemplete() {
        //aldjkfakdfjla234sdsdvsdvsdvsdvee
        String operationId = "aldjkfakdfjla234sdsdvsdvsdvsdvee";
        System.out.println("");
        return R.data(infoService.setCalculateAmount(operationId));
    }
}

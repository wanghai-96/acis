package com.acis.controller.cxx.pacuSystem;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.pojo.AcisDictRoom;
import com.acis.service.cxx.pacu.Impl.pacuBedManagementImpl;
import com.acis.service.cxx.pacu.Impl.pacuCoreImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: PACU床位管理功能
 * @author: cxx
 * @date: 2020/6/11
 */
@Log4j2
@Api(tags = "PACU床位管理功能")
@RestController
@RequestMapping("/acis/pacuS/pacuBedManagement")
public class pacuBedManagementController {

    @Autowired
    pacuCoreImpl pacucoreimpl;

    @Autowired
    pacuBedManagementImpl pacubmi;


    @ApiOperation("PACU床位管理功能-controller")
    @GetMapping("/test")
    @ResponseBody
    public R<String> test(@RequestParam("maxSalary") Integer maxSalary) {
        return R.data("sucess");
    }

    @ApiOperation(value = "PACU床位管理功能-主接口", notes = "能通过图形化界面显示现有复苏室所有床位的占用情况，并显示复苏中的患者基本信息。")
    @PostMapping("/access")
    @ResponseBody
    public R<List<Map<String, Object>>> access() {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        return R.data(resultList);
    }


    @ApiOperation("PACU床位管理功能-床位查询")
    @PostMapping("/bedsQuery")
    @ResponseBody
    public R<List<Map<String, String>>> bedsQuery(@RequestParam("roomNumber") String roomNumber) {
        List<Map<String, String>> queryUsable = pacubmi.bedsQueryUsable(roomNumber);
        return R.data(queryUsable);
    }

    @ApiOperation("PACU床位管理功能-床位选择")
    @PostMapping("/bedsChoose")
    @ResponseBody
    public R<String> bedsChoose(@RequestParam("patientId") String patientId,
                                @RequestParam("roomNumber") String roomNumber,
                                @RequestParam("bedNumber") String bedNumber,
                                @RequestParam("operationId") String operationId) {
        int chooseBedsInfo = pacubmi.patientChooseBeds(patientId, roomNumber, bedNumber, operationId);
        return R.data(CommonErrorCode.E0.getMsg());
    }

    @ApiOperation("PACU床位管理功能-床位取消")
    @PostMapping("/bedsCancel")
    @ResponseBody
    public R<String> bedsCancel(@RequestParam("patientId") String patientId,
                                @RequestParam("roomNnumber") String roomNnumber,
                                @RequestParam("bedNumber") String bedNumber,
                                @RequestParam("operationId") String operationId) {
        int state = pacubmi.bedsCancelInfo(patientId, bedNumber, operationId, roomNnumber);
        if (state == 1) {
            return R.data(CommonErrorCode.E0.getMsg());
        }
        return R.fail(CommonErrorCode.E100017.getCode(),CommonErrorCode.E100017.getMsg());

    }

    @ApiOperation("PACU床位管理功能-床位展示")
    @PostMapping("/bedsInfoShow")
    @ResponseBody
    public R<List<Map<String, String>>> bedsInfoShow() {
        List<Map<String, String>> maps = pacubmi.showBedsInfo();
        return R.data(maps);
    }

}

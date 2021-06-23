package com.acis.controller.cxx.operationCancelledSystem;

import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.pojo.AcisOpeScheduleInfo;
import com.acis.service.cxx.operationCancelled.operationCancelledCore;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: 手术取消管理
 * @author: cxx
 * @date: 2020/6/11
 */
@Log4j2
@Api(tags = "手术取消管理")
@RestController
@RequestMapping("/acis/OperCS/operationCancelled")
public class operationCancelledController {

    @Autowired
    operationCancelledCore operationcancelledcore;

    @ApiOperation("手术取消管理-controller")
    @GetMapping("/test")
    @ResponseBody
    public R<String> test() {
        return R.data("sucess");
    }


    @ApiOperation("手术取消管理-取消按钮")
    @PostMapping("/cancelOperation")
    @ResponseBody
    public R<String> cancelOperation(@RequestParam("operationId") String operationId, @RequestParam("cancelReason") String cancelReason,
                                     @RequestParam("deleteUser") String deleteUser) throws ACISException {
        boolean result=false;
        try {
            boolean isOperationId = StringUtils.isBlank(operationId);
            boolean isCancelReason = StringUtils.isBlank(cancelReason);
            if (isCancelReason) {
                return R.fail(CommonErrorCode.E100001.getCode(),"手术取消原因为空！");
            }
            if (isOperationId) {
                return R.fail(CommonErrorCode.E100001.getCode(),"请填写手术ID！");

            }
            result = operationcancelledcore.cancelledOperationCore(operationId, cancelReason, deleteUser);
        } catch (Exception e) {
            log.error(e.fillInStackTrace());
        }
        if (!result){
            return R.fail(CommonErrorCode.E100001.getCode(),"数据修改失败！");
        }
        return R.data("手术取消成功");
    }

    @ApiOperation("手术取消管理-保存")
    @PostMapping("/save")
    @ResponseBody
    public R<String> save(@RequestParam("result") String result) {
        return R.data("sucess");
    }
}

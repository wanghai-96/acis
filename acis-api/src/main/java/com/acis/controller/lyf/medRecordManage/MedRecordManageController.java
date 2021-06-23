package com.acis.controller.lyf.medRecordManage;

import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author STEVEN LEE
 * @date 2020/6/15 15:36
 */
@Api(tags = "病案管理子系统")
@RestController
@RequestMapping("/acis/medRecord")
public class MedRecordManageController {

    @ApiOperation("文书集中打印")
    @GetMapping("/printWritTogether")
    public R getOpeConConfig(@RequestParam("operationId")String operationId, @RequestParam("writList")List<String> writList) {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("查询是否自动归档")
    @GetMapping("/getIsAutoAchiving")
    public R getIsAutoAchiving() {

        return R.success(ResultCode.SUCCESS);
    }

    @ApiOperation("查询未归档的手术")
    @GetMapping("/getNotAchivingOpeInfo")
    public R getNotAchivingOpeInfo() {

        return R.success(ResultCode.SUCCESS);
    }

    /**
     * 归档可分为打印自动归档和病案提交两张情况
     * @param opeList
     * @return
     */
    @ApiOperation("归档(病案提交)")
    @PutMapping("/achiveWrit")
    public R achiveWrit(@RequestParam("opeList") List<String> opeList) {

        //若需要检查文书完整性则进行检查

        return R.success(ResultCode.SUCCESS);
    }
}

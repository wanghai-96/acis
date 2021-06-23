package com.acis.controller.intraoperative;

import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.pojo.intraoperative.count.condition.AcisSearchOpeInfoParam;
import com.acis.service.intraoperative.jdbcdao.BuildSqlUtil;
import com.acis.service.intraoperative.jdbcdao.CountDao;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 麻醉信息统计功能
 */
@Api(tags = "麻醉信息统计功能")
@RestController
@CrossOrigin
@RequestMapping("/acis/intraoperative/count")
public class CountController {

    @ApiOperation("临床手查询")
    @GetMapping("/searchOperation")
    @ResponseBody
    public R searchOperation(@ApiParam("临床手术查询参数") @RequestBody @Validated AcisSearchOpeInfoParam param){
        List<Map<String,String>> list = null;
        try {
            list = CountDao.getOperationInfo(BuildSqlUtil.getOperationInfoSql(param));
            System.out.println(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.data(list);
    }

    @ApiOperation("取消的手术查询")
    @GetMapping("/searchCanceledOperation")
    public R searchCanceledOperation(@ApiParam("患者id") String patientId){
        String text = "内容";
        String sql = "select * from aics_operation_all_information";
        List<Map<String,String>> list = CountDao.getOperationInfo(sql);

        //List<String> list1 = BuildSqlUtil.getTableParams(BuildSqlUtil.VIEW_TABLE);
        return R.data("");
    }

    @ApiOperation("恢复室病人统计")
    @GetMapping("/countRecoveryRoomPatient")
    public R countRecoveryRoomPatient(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("麻醉方法统计")
    @GetMapping("/countAnesthesiaMethod")
    public R countAnesthesiaMethod(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("Asa分级统计")
    @GetMapping("/countAsaLevel")
    public R countAsaLevel(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("输血统计")
    @GetMapping("/countTransfusion")
    public R countTransfusion(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("科室工作量统计")
    @GetMapping("/countDepartmentWorkload")
    public R countDepartmentWorkload(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("麻醉医生工作量统计")
    @GetMapping("/countAnesthesiaDoctorWorkLoad")
    public R countAnesthesiaDoctorWorkLoad(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("手术医生工作量统计")
    @GetMapping("/countSurgeonWorkLoad")
    public R countSurgeonWorkLoad(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("护士工作量统计")
    @GetMapping("/countNurseWorkLoad")
    public R countNurseWorkLoad(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("动脉穿刺统计")
    @GetMapping("/countArteriopuncture")
    public R countArteriopuncture(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("质控数据统计")
    @GetMapping("/countQualityControl")
    public R countQualityControl(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("术后随访统计")
    @GetMapping("/countAfterOperationVisit")
    public R countAfterOperationVisit(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("麻醉质控17项指标统计")
    @GetMapping("/countQualitySeventeenIndex")
    public R countQualitySeventeenIndex(@ApiParam("患者id") String patientId){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }
}

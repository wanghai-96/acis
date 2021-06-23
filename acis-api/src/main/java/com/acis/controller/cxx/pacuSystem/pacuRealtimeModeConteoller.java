package com.acis.controller.cxx.pacuSystem;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.pojo.cxx.patientInfoVo;
import com.acis.pojo.cxx.realtimeModeInfoVo;
import com.acis.pojo.cxx.realtimeModeResultVo;
import com.acis.service.cxx.pacu.Impl.pacuCoreImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @description:
 * @author: Administrator
 * @date: 2020/8/18 9:29
 */
@Log4j2
@Api(tags = "PACU实时状态功能")
@RestController
@RequestMapping("/acis/pacuS/pacuRealtimeMode")
public class pacuRealtimeModeConteoller {

    @Autowired
    pacuCoreImpl pacuCore;

    @ApiOperation("PACU实时状态-query")
    @GetMapping("/query")
    @ResponseBody
    public R queryRealtimeMode(@RequestParam("dateTime")String dateTime){
        String pattern = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
        boolean isMatch = Pattern.matches(pattern, dateTime);
        log.info(isMatch);
        if (!isMatch){
            return  R.fail(CommonErrorCode.E100001.getCode(),CommonErrorCode.E100001.getMsg());
        }
        List<realtimeModeResultVo> modeVos = pacuCore.pacuRealtimeMode(dateTime);

        return R.data(modeVos);
    }


}

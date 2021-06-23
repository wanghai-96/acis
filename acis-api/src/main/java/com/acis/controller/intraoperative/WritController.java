package com.acis.controller.intraoperative;

import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.pojo.intraoperative.dto.utilclass.CheckTimeClass;
import com.acis.service.intraoperative.util.CheckTimeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 术中文书信息功能
 */
@Api(tags = "术中文书信息功能")
@RestController
@CrossOrigin
@RequestMapping("/acis/intraoperative/writ")
public class WritController {

    @ApiOperation("存储术中麻醉记录文书数据")
    @PostMapping("/storageAnesthesiaRecordWrit")
    public R storageAnesthesiaRecordWrit(String startTime,String endTime){
        String text = "ilreufhkasjdnkaaksdjfhaksjdhaiuhas";
        HashMap<Boolean,String> checkTimeClass = CheckTimeUtil.checkAddEventTime(text,startTime,endTime);
        System.out.println(checkTimeClass);
        if(checkTimeClass.isEmpty()){
            return R.success("成功");
        }else{
            return R.fail(checkTimeClass.get(false));
        }
    }

    @ApiOperation("获得术中麻醉记录文书数据")
    @GetMapping("/getAnesthesiaRecordWrit")
    public R getAnesthesiaRecordWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("存储术中麻醉手术转出数据")
    @PostMapping("/storageAnesthesiaChangeOutWrit")
    public R storageAnesthesiaChangeOutWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中麻醉手术转出数据")
    @GetMapping("/getAnesthesiaChangeOutWrit")
    public R getAnesthesiaChangeOutWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("存储术中麻醉护理文书数据")
    @PostMapping("/storageAnesthesiaProtectWrit")
    public R storageAnesthesiaProtectWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中麻醉护理文书数据")
    @GetMapping("/getAnesthesiaProtectWrit")
    public R getAnesthesiaProtectWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("存储术中麻醉清点文书数据")
    @PostMapping("/storageAnesthesiaCheckWrit")
    public R storageAnesthesiaCheckWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中麻醉清点文书数据")
    @GetMapping("/getAnesthesiaCheckWrit")
    public R getAnesthesiaCheckWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("存储术中麻醉质控文书数据")
    @PostMapping("/storageAnesthesiaQualityWrit")
    public R storageAnesthesiaQualityWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }

    @ApiOperation("获得术中麻醉质控文书数据")
    @GetMapping("/getAnesthesiaQualityWrit")
    public R getAnesthesiaQualityWrit(){
        String text = "内容";
        return R.data(text, ResultCode.SUCCESS.getMessage());
    }
}

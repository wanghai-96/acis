package com.acis.controller.lyf.writ;

import com.acis.common.exception.R;
import com.acis.pojo.lyf.vo.response.DrugConsumablesVo;
import com.acis.service.intraoperative.InfoService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "文书信息查询")
@RestController
@RequestMapping("/acis/writ")
@Log4j2
@CrossOrigin
public class WritInfoController {

    @Autowired
    private InfoService infoService;


    @ApiOperation("获取药品耗材清单")
    @GetMapping("/getDrugConsumablesList")
    public R getDrugConsumablesList(@RequestParam("start")Integer start,
                                    @RequestParam("size")Integer size,
                                    @RequestParam("operationId")String operationId) {
        Map<String, Object> list = infoService.getDrugConsumablesList(size, start, operationId);
        return R.data(list);
    }
}

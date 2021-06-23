package com.acis.controller.cxx.common;

import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.service.cxx.common.Impl.tamplateDataSourseImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: Administrator
 * @date: 2020/7/1 13:57
 */
@Log4j2
@Api(tags = "模板展示公用接口")
@RestController
@RequestMapping("/acis/common/templateDataSource")
public class commonTemplateDataSource {

    @Autowired
    tamplateDataSourseImpl tamplatedatasourseimpl;


    @ApiOperation(value = "展示字典数据源", notes = "模板配置字典数据源统一展示接口")
    @GetMapping("/showTemplateDataSource")
    @ResponseBody
    public R<List<Map<String, Object>>> showTemplateDataSource() {
        List<Map<String, Object>> templateBindingDataSource = tamplatedatasourseimpl.getTemplateBindingDataSource();
        if (templateBindingDataSource.size()==0){
            log.error(CommonErrorCode.E100024.getMsg());
            return R.fail(CommonErrorCode.E100024.getCode(),CommonErrorCode.E100024.getMsg());
        }
        return R.data(templateBindingDataSource);
    }
}

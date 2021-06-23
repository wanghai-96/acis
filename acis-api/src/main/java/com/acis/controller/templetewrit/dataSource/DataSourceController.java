package com.acis.controller.templetewrit.dataSource;


import com.acis.common.exception.R;

import com.acis.common.exception.ResultCode;
import com.acis.pojo.templetewrit.dataSource.dto.AcisDataSourceDto;
import com.acis.service.templetewrit.dataSource.DataSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 文书数据源配置接口
 */
@Log4j2
@RestController
@CrossOrigin
@Api(value = "文书数据源配置接口")
@RequestMapping("acis/templetewrit/dataSource")
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    @GetMapping("/getTempleteDataSource")
    @ApiOperation("查询每张文书绑定的数据源跟字段")
    public R getTempleteDataSource(){
        List<AcisDataSourceDto> dataSourceList= null;
        try {
            dataSourceList = dataSourceService.queryDataSource();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dataSourceList!=null){

            return R.data(dataSourceList);
        }
       return R.fail("数据库中没有你想获得的数据");
    }


    @PostMapping("/addDataSource")
    @ApiOperation(value ="新增文书源头数据",httpMethod = "POST", notes = "新增加每张文书可以自定义数据源跟字段")
    public R addDataSource(@ApiParam(name="tableName",value="表名称",required=true) String tableName,
                           @ApiParam( name="className",value="表对应字段名称",required=true) String className){
        return R.success(ResultCode.SUCCESS);
    }
}

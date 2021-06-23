package com.acis.controller.templetewrit.dataSource;

import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.pojo.templetewrit.dataSource.dto.AcisDataSourceDictDto;
import com.acis.pojo.templetewrit.dataSource.dto.AcisDictDropListDto;
import com.acis.service.templetewrit.dataSource.DataSourceDictService;
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
@Api("文书数据源字典配置类")
@RequestMapping("acis/templetewrit/dataSourceDict")
public class DataSourceDictController {

    @Autowired
    private DataSourceDictService dataSourceDictService;

    @GetMapping("/queryDataSourceDict")
    @ApiOperation(value ="查询文书源头字典数据",httpMethod = "GET", notes = "查询每张文书绑定的字典数据源跟字段")
    public R queryDataSourceDict(){
        List<AcisDataSourceDictDto> dataSourceDictList=dataSourceDictService.queryDataSourceDict();
        if (dataSourceDictList!=null){
            return R.data(dataSourceDictList!=null?dataSourceDictList:new ArrayList<>());
        }
        return R.fail("数据库中没有你想获得的数据");
    }

    @PostMapping("/addDictDataSource")
    @ApiOperation(value ="新增文书字典源头数据",httpMethod = "POST", notes = "新增加每张字典文书可以自定义数据源跟字段")
    public R addDictDataSource(@ApiParam(name="dictTableName",value="表名称",required=true) String dictTableName,
                                      @ApiParam( name="dictClassName",value="表对应字段名称",required=true) String dictClassName){
        return R.success(ResultCode.SUCCESS);
    }


    /*该方法暂时不用*/
    @GetMapping("/queryDictDropList/{dictTableName}/{dictClassName}")
    @ApiOperation(value ="查询文书模板下拉框内容",httpMethod = "GET", notes = "查询文书模板下拉框内容")
    public R queryDictDropList(@ApiParam(name="dictTableName",value="字典表名称",required=true)@PathVariable String dictTableName,
                               @ApiParam( name="dictClassName",value="字典表对应字段名称",required=true) @PathVariable String dictClassName){
        List<AcisDictDropListDto> dictDropList=dataSourceDictService.queryDictDropList(dictTableName,dictClassName);
        if (dictDropList!=null){
            return R.data(dictDropList);
        }
        return R.fail("数据库中没有你想获得的数据");
    }
}

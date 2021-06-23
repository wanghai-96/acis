package com.acis.controller.templetewrit.template;


import com.acis.common.exception.ACISException;
import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.common.exception.ResultCode;
import com.acis.pojo.cxx.TemplateTableInfoVo;
import com.acis.pojo.templetewrit.dataSource.dto.AcisWritTemplateDto;
import com.acis.pojo.templetewrit.dataSource.vo.AcisWritTemplateVo;
import com.acis.service.templetewrit.template.TemplateService;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 文书数据源配置接口
 */
@Log4j2
@RestController
@RequestMapping("acis/templatewrit/templete")
@CrossOrigin
@Api("文书数据源配置接口")
public class TemplateController {

    @Autowired
    private TemplateService templateService;

    @ApiOperation("新增模板功能")
    @PostMapping("/acisTempleteAddTemplate")
    public R<Integer> acisTempleteAddTemplate(@ApiParam("模板json数据") @RequestBody String templateJson) {
        Integer result;
        try {
            //判断传入json数据是否为空
            if (StringUtils.isBlank(templateJson)) {
                //非空报出异常
                return R.fail(CommonErrorCode.E700000.getCode(), CommonErrorCode.E700000.getMsg());
            }
            result = templateService.addTemplate(templateJson);
        } catch (Exception e) {
            //数据结果保存失败
            log.error(CommonErrorCode.E100019.getMsg());
            return R.fail(CommonErrorCode.E100019.getCode(), CommonErrorCode.E100018.getMsg());
        }
        return R.data(result);
    }


    @PostMapping("/acisTempleteUpdateTemplate")
    @ApiOperation("模板修改功能")
    public R acisTempleteUpdateTemplate(
            @ApiParam(name = "templateJson", value = "模板json数据", required = true) @RequestBody String templateJson,
            @ApiParam(name = "templateCode", value = "模板code", required = true) String templateCode) {
        try {
            if (StringUtils.isBlank(templateJson)) {
                throw new ACISException(CommonErrorCode.E700000);
            }
            if (StringUtil.isEmpty(templateJson)) {
                throw new ACISException(CommonErrorCode.E700001);
            }
            Boolean update = templateService.updateTemplate(templateJson, templateCode);
            if (update) {
                return R.success(ResultCode.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.fail("服务器异常");

    }


    @GetMapping("/queryTemplateById")
    @ApiOperation(value = "查询模板", httpMethod = "GET", notes = "根据模板id查询单个模板")
    public R queryTemplateById(@ApiParam(name = "templateid", value = "模板id", required = true) String templateCode) {
        if (StringUtil.isEmpty(templateCode.toString())) {
            throw new ACISException(CommonErrorCode.E700001);
        }
        AcisWritTemplateDto templateDto = templateService.queryTemplateById(templateCode);
        AcisWritTemplateVo templateVo = new AcisWritTemplateVo();
        String result = templateDto.getTemplateData();
        JSONArray arr = JSONArray.parseArray(result);
        BeanUtils.copyProperties(templateDto, templateVo);
        templateVo.setTemplateData(arr);
        return R.data(templateVo != null ? templateVo : new AcisWritTemplateVo());
    }

    @GetMapping("/queryTemplateList")
    @ApiOperation(value = "查询所有模板信息", httpMethod = "GET", notes = "获取所有模板信息")
    public R queryTemplateList() {
        return R.success(ResultCode.SUCCESS);
    }


    @ApiOperation("根据模板id删除模板")
    @DeleteMapping("/deleteTemplateById/{templateCode}")
    public R deleteTemplateById(@PathVariable String templateCode) {

        return R.success(ResultCode.SUCCESS);
    }


    @GetMapping("/queryPictureList")
    @ApiOperation(value = "获取logo图片", httpMethod = "GET", notes = "获取数据库中所有医院logo")
    public R queryPictureList() {
        return R.success(ResultCode.SUCCESS);
    }

    /**
     * @author cxx
     * @date 2020/07/13
     * @description 以下内容为新增内容
     */

    @PostMapping("/queryTemplateListTest")
    @ApiOperation(value = "返回前端文书列表", notes = "返回前端文书返回列表")
    public R queryTemplateListTest() {
        List<Map<String, Object>> byTemplateClode = templateService.selectByTemplateClode();
        return R.data(byTemplateClode);
    }

    @PostMapping("/saveTemplateTableInfo")
    @ApiOperation(value = "保存文书模板的表信息", notes = "保存文书模板的表信息")
    public R saveTemplateTableInfo(@RequestBody Map<String, String> tableInfo, @RequestParam("templateCode") String templateCode) {
        int i = templateService.updateTableInfo(tableInfo, templateCode);
        return R.data(CommonErrorCode.E0.getMsg());
    }

}

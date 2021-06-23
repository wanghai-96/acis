package com.acis.controller.userpermissions;


import com.acis.pojo.userpermissions.dto.AcisSystemMenuDto;
import com.acis.pojo.userpermissions.vo.AcisSystemMenuButtonsVO;
import com.acis.pojo.userpermissions.vo.AcisSystemMenuVo;
import com.acis.pojo.userpermissions.vo.AcisSystemMenusVO;
import com.acis.service.userpermissions.service.SysMenuService;
import com.acis.service.userpermissions.sreturn.PermissionCommonErrorCode;
import com.acis.service.userpermissions.sreturn.S;
import com.acis.service.userpermissions.sreturn.UPMSException;
import com.acis.service.userpermissions.utils.CommonErrorCode;
import com.acis.service.userpermissions.utils.ResultPO;
import com.acis.service.userpermissions.utils.StringUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 菜单处理类
 */
@Log4j2
@RestController
@CrossOrigin
@Api("菜单处理类")
@RequestMapping("acis/userpermissions/systemmenu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @PostMapping(value = "/getMenuListByRole")
    @ApiOperation(value="根据角色查询对应的左侧系统菜单列表",httpMethod="POST",notes="登录后根据角色查询对应的左侧系统菜单列表")
    public S getMenuListByRole(@ApiParam(name="userRoleId",value="菜单对象",required=true) String userRoleId,Integer procedureState) {

        return S.data("success");
    }

    @GetMapping("/getAllMenuList")
    @ApiOperation("显示菜单详情和操作权限")
    public S<List<AcisSystemMenusVO>> getAllMenuList(){
        List<AcisSystemMenusVO> list = null;
        try {
            list = sysMenuService.getAcisSysteMenuDetail();
            if(null==list){
                return S.fail(PermissionCommonErrorCode.E100026.getCode(),PermissionCommonErrorCode.E100026.getMsg());
            }
        } catch (Exception e) {
            //更新角色信息失败
            log.info(PermissionCommonErrorCode.E100030.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100030.getCode(),PermissionCommonErrorCode.E100030.getMsg());
        }
        return S.data(list);
    }

    @PostMapping("/addMenu")
    @ApiOperation(value ="新增",httpMethod = "POST", notes = "新增菜单")
    public ResultPO addMenu(@ApiParam(name="menuVo",value="菜单对象",required=true)@RequestBody AcisSystemMenuVo menuVo)throws Exception {

        String result = " ";
        boolean menu = StringUtil.isEmptyIfStr(menuVo);
        if (menu) {
            return ResultPO.failure(CommonErrorCode.E400000.getMsg());
        }
        boolean systemId = StringUtils.isBlank(menuVo.getSystemId());
        if (systemId) {
            return ResultPO.failure(CommonErrorCode.E200000.getMsg());
        }
        boolean MenuType = StringUtils.isBlank(menuVo.getMenuType());
        if (MenuType) {
            return ResultPO.failure(CommonErrorCode.E400001.getMsg());
        }
        boolean menuPerms = StringUtils.isBlank(menuVo.getMenuPerms());
        if (menuPerms) {
            return ResultPO.failure(CommonErrorCode.E400002.getMsg());
        }
        if (menuVo.getStatus() != 0 && menuVo.getStatus() != 1) {
            //throw new UPMSException(CommonErrorCode.E400003);
        }
        boolean logogram = StringUtils.isBlank(menuVo.getLogogram());
        if (logogram) {
            return ResultPO.failure(CommonErrorCode.E400004.getMsg());
        }
        boolean code = StringUtils.isBlank(menuVo.getMenuCode());
        if (code) {
            return ResultPO.failure(CommonErrorCode.E400005.getMsg());
        }
        if (menuVo.getParentId() == null) {
            //throw new UPMSException(CommonErrorCode.E400006);
        }

        if (menuVo.getMenuHidden() != 0 && menuVo.getMenuHidden() != 1) {
            //throw new UPMSException(CommonErrorCode.E400007);
        }
        boolean menuTitle = StringUtils.isBlank(menuVo.getTitle());
        if (menuTitle) {
            return ResultPO.failure(CommonErrorCode.E400008.getMsg());
        }
        result = sysMenuService.addMenu(menuVo);
        if (StringUtils.isBlank(result)) {
            return ResultPO.failure(CommonErrorCode.E400009.getMsg());
        }
        return ResultPO.success(result);
    }


    @DeleteMapping("/delMenuById")
    @ApiOperation(value ="根据id删除对应的菜单信息",httpMethod = "DELETE", notes = "删除")
    public ResultPO delMenuById(@ApiParam(name="menuId",value="菜单id",required=true) String menuId)throws Exception {
        boolean id = StringUtils.isBlank(menuId);
        if (id) {
            return ResultPO.failure(CommonErrorCode.E400010.getMsg());
        }
        Boolean delete = sysMenuService.deleteMenuById(menuId);
        if (delete){
            return ResultPO.success();
        }
        //throw new UPMSException(CommonErrorCode.E100000, "该菜单不存在");
        return ResultPO.success();
    }


    @PostMapping("/updateMenuById")
    @ApiOperation(value ="根据菜单id修改对应的菜单信息",httpMethod = "POST", notes = "修改")
    public ResultPO updateMenuById(@ApiParam(name="SystemMenuVo",value="菜单对象",required=true) @RequestBody AcisSystemMenuVo systemMenuVo)throws Exception {
        boolean menuId = StringUtils.isBlank(systemMenuVo.getMenuId());
        if (menuId) {
            return ResultPO.failure(CommonErrorCode.E400010.getMsg());
        }
        Boolean update = sysMenuService.updateMenuById(systemMenuVo);
        if (update){
            return ResultPO.success();
        }
        //throw new UPMSException(CommonErrorCode.E100000, "修改菜单未成功");
        return ResultPO.success();
    }



}








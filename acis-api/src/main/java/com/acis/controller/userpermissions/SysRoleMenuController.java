package com.acis.controller.userpermissions;


import com.acis.pojo.userpermissions.vo.AcisRoleMenuVo;
import com.acis.service.userpermissions.service.SysRoleMenuService;
import com.acis.service.userpermissions.sreturn.PermissionCommonErrorCode;
import com.acis.service.userpermissions.sreturn.S;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 角色权限处理类
 */
@Log4j2
@RestController
@CrossOrigin
@Api(value = "角色权限处理类")
@RequestMapping("acis/userpermissions/roleMenu")
public class SysRoleMenuController {

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @PostMapping("/add")
    @ApiOperation("新增角色菜单")
    public S<String> saveRoleMenu(@ApiParam("角色对应的菜单列表实体")AcisRoleMenuVo roleMenuVO) {

        if (StringUtils.isBlank(roleMenuVO.getRoleId()) || null == roleMenuVO.getMenuId()) {
            //判断角色id是否填写,角色菜单是否添加
            return S.fail(PermissionCommonErrorCode.E300005.getCode(), PermissionCommonErrorCode.E300005.getMsg());
        }
        //初始化结果
        String result = "";
        try {
            result = sysRoleMenuService.systemAddRoleMenu(roleMenuVO);
        } catch (Exception e) {
            //新增角色结果失败
            log.error(PermissionCommonErrorCode.E200005.getMsg());
            return S.fail(PermissionCommonErrorCode.E200005.getCode(), PermissionCommonErrorCode.E200005.getMsg());
        }
        return S.data(result);
    }
}

package com.acis.controller.userpermissions;


import com.acis.service.userpermissions.service.SysUserRoleService;
import com.acis.service.userpermissions.sreturn.PermissionCommonErrorCode;
import com.acis.service.userpermissions.sreturn.S;
import com.acis.service.userpermissions.sreturn.UPMSException;
import com.acis.service.userpermissions.utils.ResultPO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.acis.service.userpermissions.service.impl.Iterator.applyPermission;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 用户角色处理类
 */
@Log4j2
@RestController
@CrossOrigin
@Api("用户角色处理类")
@RequestMapping("acis/userpermissions/userRole")
public class SysUserRoleController {

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @PostMapping(value = "/userAddRole")
    @ApiOperation("用户添加角色功能")
    public S<Integer> userAddRole(@ApiParam("用户id") @RequestParam(value = "userId",required = true)String userId,
                         @ApiParam("角色id") @RequestParam(value = "roleId",required = true)String roleId){
        Integer result = null;
        try {
            if(StringUtils.isBlank(userId)){
                //用户id判断
                log.info(PermissionCommonErrorCode.E200011.getMsg());
                return S.fail(PermissionCommonErrorCode.E200011.getCode(),PermissionCommonErrorCode.E200011.getMsg());
            }
            if(StringUtils.isBlank(roleId)){
                //角色id判断
                log.info(PermissionCommonErrorCode.E300005.getMsg());
                return S.fail(PermissionCommonErrorCode.E300005.getCode(),PermissionCommonErrorCode.E300005.getMsg());
            }
            result = sysUserRoleService.addUserRole(userId, roleId);
        } catch (Exception e) {
            //更新角色信息失败
            log.info(PermissionCommonErrorCode.E100030.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100030.getCode(),PermissionCommonErrorCode.E100030.getMsg());
        }
        return S.data(result);
    }


    @PostMapping(value = "/updateUserRole")
    @ApiOperation(value="修改用户角色",httpMethod="POST",notes="修改用户角色")
    public ResultPO updateUserRole(){

        String url="/updateUserRole";
        boolean b=applyPermission(url);
        return ResultPO.success();
    }






}

package com.acis.controller.userpermissions;

import com.acis.pojo.userpermissions.vo.*;
import com.acis.service.userpermissions.security.SecurityUserHelper;
import com.acis.service.userpermissions.service.*;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 用户处理类
 */
@Log4j2
@RestController
@CrossOrigin
@Api(tags = "用户权限综合处理类")
@RequestMapping("/acis/userpermissions/synthesize")
public class UserPermissionController {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;


    /**
     * 解锁锁屏密码
     * @return
     */
    @ApiOperation("解锁锁屏密码")
    @PostMapping("/unlockAcisSystem")
    public S unlockAcisSystem(@ApiParam("用户密码")@RequestParam(value = "password",required = true)String password,
                              @ApiParam("用户账号")@RequestParam(value = "loginName",required = true)String loginName){
        S data = null;
        try {
            if(StringUtils.isBlank(password)||StringUtils.isBlank(loginName)){
                return S.fail(PermissionCommonErrorCode.E200004.getCode(), PermissionCommonErrorCode.E200004.getMsg());
            }else{
                data =  sysUserService.unlockSystem(loginName,password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 新增用户信息
     * @param acisAddSystemUserVo
     * @return
     */
    @ApiOperation("新增用户信息")
    @PostMapping("/sysAddUser")
    public S sysAddUser(@ApiParam("用户信息参数") @RequestBody @Validated AcisAddSystemUserVo acisAddSystemUserVo) {
        S result = null;
        try {
            //执行添加操作
            result = sysUserService.addUser(acisAddSystemUserVo);
        } catch (Exception e) {
            //数据结果保存失败
            e.fillInStackTrace();
            log.error(PermissionCommonErrorCode.E100019.getMsg());
            return S.fail(PermissionCommonErrorCode.E100019.getCode(), PermissionCommonErrorCode.E100019.getMsg());
        }
        return result;
    }

    /**
     * @author Neon Xie
     * @Date 2021/3/22
     * @description 系统用户用于修改用户密码   
     */
    //# 主管护师 主任医师 副主任医师 主治医师 护师  住院医师 护士
    @ApiOperation("修改用户密码")
    @PostMapping("/sysUpdateUserPwd")
    public S sysUpdateUserPwd(@ApiParam("修改用户参数")@RequestBody @Validated AcisChangePwdParamVO params){
        S updatePwd = null;
        try{
            if (StringUtils.isBlank(params.getLoginName())) {
                //用户账号非空判断
                log.error(PermissionCommonErrorCode.E200001.getMsg());
                return S.fail(PermissionCommonErrorCode.E200001.getCode(), PermissionCommonErrorCode.E200001.getMsg());
            }
            if (StringUtils.isBlank(params.getOldPwd())||StringUtils.isBlank(params.getNewPwd())) {
                //前后输入的密码都不能为空
                log.error(PermissionCommonErrorCode.E200002.getMsg());
                return S.fail(PermissionCommonErrorCode.E200002.getCode(), PermissionCommonErrorCode.E200002.getMsg());
            }
            updatePwd = sysUserService.updatePwd(params.getLoginName(), params.getOldPwd(), params.getNewPwd());
        }catch (Exception e){
            e.fillInStackTrace();
            log.error(PermissionCommonErrorCode.E100023.getMsg());
        }
        return updatePwd;
    }

    /**
     * 修改用户个人信息功能
     * @param param
     * @return
     */
    @ApiOperation("修改用户个人信息功能")
    @PostMapping("/sysUpdateUser")
    public S sysUpdateUser(@ApiParam("修改用户信息参数") @RequestBody AcisSystemUpdateUserVO param) {
        S result = null;
        try {
            if(null==param){
                return S.fail(PermissionCommonErrorCode.E100027.getCode(), PermissionCommonErrorCode.E100027.getMsg());
            }else{
                //执行用户信息更新
                result = sysUserService.updateUser(param);
            }
        } catch (Exception e) {
            //更新信息失败
            e.printStackTrace();
            log.error(PermissionCommonErrorCode.E700005.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E700005.getCode(),PermissionCommonErrorCode.E700005.getMsg());
        }
        return result;
    }

    /**
     * 用户状态禁用,启用功能
     * @param userId
     * @param status
     * @return
     */
    @PostMapping("/SystUserStatusEnable")
    @ApiOperation("用户状态禁用,启用功能(删除用户)")
    public S SystUserStatusEnable(@ApiParam("用户id")@RequestParam(value = "userId",required = true) String userId,
                                                        @ApiParam("用户启用状态:0不启用1启用")@RequestParam(value = "status",required = true)Integer status){
        S result = null;
        try {
            if (StringUtils.isBlank(userId)) {
                return S.fail(PermissionCommonErrorCode.E200011.getCode(),PermissionCommonErrorCode.E200011.getMsg());
            }
            if(!(0<=status&&status<=1)){
                return S.fail(PermissionCommonErrorCode.E200012.getCode(),PermissionCommonErrorCode.E200012.getMsg());
            }
            result = sysUserService.updateUserStatus(userId, status);
        } catch (Exception e) {
            //修改用户状态失败
            e.fillInStackTrace();
            log.error(PermissionCommonErrorCode.E100024.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100024.getCode(),PermissionCommonErrorCode.E100024.getMsg());
        }
        return S.data(result);
    }

    /**
     * 用户登录显示用户信息
     * @param
     * @return
     */
    @GetMapping("/SysDisplayUserInfo")
    @ApiOperation("用户登录显示用户信息")
    public S<AcisSystemUserInfoVO> SysDisplayUserInfo(){
        AcisSystemUserInfoVO info;
        try {
            String userId = SecurityUserHelper.getCurrentUser().getUserId();
            if (StringUtils.isBlank(userId)) {
                log.info(PermissionCommonErrorCode.E200011.getMsg());
                return S.fail(PermissionCommonErrorCode.E200011.getCode(),PermissionCommonErrorCode.E200011.getMsg());
            }
            info = sysUserService.getUserInfo(userId);
            if(null == info){
                log.info(PermissionCommonErrorCode.E100025.getMsg());
                return S.fail(PermissionCommonErrorCode.E100025.getCode(),PermissionCommonErrorCode.E100025.getMsg());
            }
        } catch (Exception e) {
            //查询用户信息失败
            e.fillInStackTrace();
            log.error(PermissionCommonErrorCode.E100025.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100025.getCode(),PermissionCommonErrorCode.E100025.getMsg());
        }
        return S.data(info);
    }


    /**
     * 显示所有用户信息
     * @param pageIndex
     * @param pageSize
     * @return
     */
    @PostMapping("/SysAllUsersInfo")
    @ApiOperation("显示所有用户信息")
    public S<List<AcisSystemUserInfoVO>> SysAllUsersInfo(@ApiParam("当前页")@RequestParam(required = true) Integer pageIndex,
                                                   @ApiParam("页数大小")@RequestParam(required = true)Integer pageSize){
        List<AcisSystemUserInfoVO> list = null;
        try {

        } catch (Exception e) {
            //查询用户信息失败
            e.fillInStackTrace();
            log.error(PermissionCommonErrorCode.E100025.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100025.getCode(),PermissionCommonErrorCode.E100025.getMsg());
        }
        return S.data(list);
    }
}

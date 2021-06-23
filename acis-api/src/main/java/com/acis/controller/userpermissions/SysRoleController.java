package com.acis.controller.userpermissions;

import com.acis.pojo.userpermissions.dto.AcisSystemMenuDto;
import com.acis.pojo.userpermissions.dto.AcisSystemRoleDto;
import com.acis.pojo.userpermissions.vo.*;
import com.acis.service.userpermissions.service.SysRoleService;
import com.acis.service.userpermissions.sreturn.PermissionCommonErrorCode;
import com.acis.service.userpermissions.sreturn.S;
import com.acis.service.userpermissions.utils.ResultPO;
import com.acis.service.userpermissions.utils.StringUtil;
import com.acis.service.userpermissions.sreturn.UPMSException;
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
 * @description 角色处理类
 */
@Log4j2
@RestController
@CrossOrigin
@Api("角色处理类")
@RequestMapping("acis/userpermissions/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/getAllRoleList")
    @ApiOperation(value ="查询所有的角色信息",httpMethod = "GET", notes = "查询所有的角色信息，返回角色集合")
    public S<PageInfo<SystemRoleVO>> getRoleList(@RequestParam(value = "start",required = true)String start,
                         @RequestParam(value = "pageSize",required = true)String pageSize){
        PageInfo<SystemRoleVO> roleList = null;
        try {
            int result = 0;
            if(Integer.parseInt(start)<0||Integer.parseInt(pageSize)<0){
                //传入分页参数不合理
                log.info(PermissionCommonErrorCode.E200013.getMsg());
                return S.fail(PermissionCommonErrorCode.E200013.getCode(),PermissionCommonErrorCode.E200013.getMsg());
            }
            roleList = sysRoleService.getRoleList(start,pageSize);
            if(roleList.getList().size()==0){
                //判断是否获取到数据
                return S.data(PermissionCommonErrorCode.E100026.getCode(),roleList,PermissionCommonErrorCode.E100026.getMsg());
            }
        } catch (Exception e) {
            //进入报错信息
            log.info(PermissionCommonErrorCode.E100019.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100019.getCode(),PermissionCommonErrorCode.E100019.getMsg());
        }
        return S.data(roleList);
    }


    @PostMapping("/addRoleInfo")
    @ApiOperation(value ="新增角色信息",httpMethod = "POST", notes = "新增用户角色")
    public S<Integer> addRole(@ApiParam("角色对象入参信息")@RequestBody SystemAddRoleVO role){
        Integer result = null;
        try {
            if(null!=role){
                result = sysRoleService.addRole(role);
            }else{
                //传入对象为空报错
                log.info(PermissionCommonErrorCode.E100027.getMsg());
                return S.fail(PermissionCommonErrorCode.E100027.getCode(),PermissionCommonErrorCode.E100027.getMsg());
            }
            if(result==0){
                //插入角色信息报错提示
                log.info(PermissionCommonErrorCode.E100028.getMsg());
                return S.fail(PermissionCommonErrorCode.E100028.getCode(),PermissionCommonErrorCode.E100028.getMsg());
            }
        } catch (Exception e) {
            log.info(PermissionCommonErrorCode.E100028.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100028.getCode(),PermissionCommonErrorCode.E100028.getMsg());
        }
        return S.data(result);
    }

    @PostMapping("/deleteRoleById")
    @ApiOperation(value ="删除角色信息",httpMethod = "DELETE", notes = "根据用户id删除角色信息")
    public S<Integer> deleteRoleById(@ApiParam(name="roleid",value="用户",required=true)@RequestParam(required = true) String  roleId){
        Integer result = null;
        try {
            if (StringUtils.isBlank(roleId)) {
                //传入角色id不能为空
                log.info(PermissionCommonErrorCode.E300005.getMsg());
                return S.fail(PermissionCommonErrorCode.E300005.getCode(),PermissionCommonErrorCode.E300005.getMsg());
            }
            result = sysRoleService.deleteRoleById(roleId);
            if (result==0){
                //删除角色信息失败
                log.info(PermissionCommonErrorCode.E100029.getMsg());
                return S.data(PermissionCommonErrorCode.E100029.getCode(),result,PermissionCommonErrorCode.E100029.getMsg());
            }
        } catch (Exception e) {
            log.info(PermissionCommonErrorCode.E100029.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100029.getCode(),PermissionCommonErrorCode.E100029.getMsg());
        }
        return S.data(result);
    }


    @PostMapping("/updateRoleById")
    @ApiOperation(value ="修改角色信息",httpMethod = "POST", notes = "修改角色信息")
    public S<Integer> updateRoleById(@ApiParam(name="SystemRoleVo",value="角色",required=true) @RequestBody SystemUpdateRoleVO role){
        Integer result = null;
        try {
            if(null!=role){
                result = sysRoleService.updateRoleById(role);
            }else{
                //传入对象为空报错
                log.info(PermissionCommonErrorCode.E100027.getMsg());
                return S.fail(PermissionCommonErrorCode.E100027.getCode(),PermissionCommonErrorCode.E100027.getMsg());
            }
            if(result==0){
                //更新角色信息报错提示
                log.info(PermissionCommonErrorCode.E100030.getMsg());
                return S.fail(PermissionCommonErrorCode.E100030.getCode(),PermissionCommonErrorCode.E100030.getMsg());
            }
        } catch (Exception e) {
            //更新失败提示
            log.info(PermissionCommonErrorCode.E100030.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100030.getCode(),PermissionCommonErrorCode.E100030.getMsg());
        }
        return S.data(result);
    }


    @PostMapping(value = "/queryRoleMenuByRoleId")
    @ApiOperation(value="根据角色ID查询对应的菜单名称",httpMethod="GET",notes="根据角色ID查询对应的菜单名称")
    public S<List<AcisSystemMenusVO>> getRoleInfo(@ApiParam("角色ID")@RequestParam(required = true) String roleId) {
        List<AcisSystemMenusVO> list = null;
        try {
            if (StringUtil.isEmpty(roleId)){
                log.info(PermissionCommonErrorCode.E100027.getMsg());
                return S.fail(PermissionCommonErrorCode.E100027.getCode(),PermissionCommonErrorCode.E100027.getMsg());
            }
            list = sysRoleService.getAcisSysteMenuDetail(roleId);
            if (list.size()==0){
                return S.data(PermissionCommonErrorCode.E100027.getCode(),list,PermissionCommonErrorCode.E100027.getMsg());
            }
        } catch (Exception e) {
            //查询角色菜单信息失败
            log.info(PermissionCommonErrorCode.E100026.getMsg());
            throw new UPMSException(PermissionCommonErrorCode.E100026.getCode(),PermissionCommonErrorCode.E100026.getMsg());
        }
        return S.data(list);
    }
}

package com.acis.controller.userpermissions;


import com.acis.common.exception.CommonErrorCode;
import com.acis.common.exception.R;
import com.acis.pojo.userpermissions.dto.AcisSystemUserDto;
import com.acis.pojo.userpermissions.dto.AcisUserInfoDto;
import com.acis.pojo.userpermissions.vo.AcisAddSystemUserVo;
import com.acis.pojo.userpermissions.vo.AcisChangePwdParamVO;
import com.acis.pojo.userpermissions.vo.AcisSystemUserVo;
import com.acis.service.userpermissions.filter.JWTLoginFilter;
import com.acis.service.userpermissions.security.JwtHelper;
import com.acis.service.userpermissions.security.JwtUser;
import com.acis.service.userpermissions.security.SecurityUserHelper;
import com.acis.service.userpermissions.service.SysUserService;
import com.acis.service.userpermissions.sreturn.PermissionCommonErrorCode;
import com.acis.service.userpermissions.sreturn.S;
import com.acis.service.userpermissions.utils.ResultPO;
import com.acis.service.userpermissions.utils.StringUtil;
import com.acis.service.userpermissions.sreturn.UPMSException;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.models.auth.In;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neon Xie
 * @date 2020/07/09
 * @description 用户处理类
 */
@Log4j2
@RestController
@CrossOrigin
@Api(tags = "用户处理类")
@RequestMapping("acis/userpermissions/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;


    @GetMapping("/info")
    @ApiOperation(value = "当前用户的菜单详情功能", httpMethod = "GET", notes = "获取当前用户的用户权限")
    public ResultPO userInfo() throws Exception {
        if (StringUtil.isEmpty(SecurityUserHelper.getCurrentUser().getUserId())) {
            //throw new UPMSException(CommonErrorCode.E100001, "系统超时，请重新登录");
        }
        AcisSystemUserDto user = sysUserService.selectUserById(SecurityUserHelper.getCurrentUser().getUserId());
        if (StringUtil.isEmpty(user.getUserRoleId())) {
            //throw new UPMSException(CommonErrorCode.E100001, "当前用户未分配角色");
        }
        List<AcisUserInfoDto> userInfo = sysUserService.queryUserInfo(user.getUserRoleId());
//       for (int i=0;i<=userInfo.size();i++) {
//           UserInfoDto u = userInfo.get(i);
//           List a = null;
//           if (u.getMenuType() == 0) {
//               a = new ArrayList<>();
//               a.add(u);
//           } else {
//               List b = new ArrayList<>();
//               b.add(u);
//           }
//       }
        return ResultPO.success(userInfo != null ? userInfo : new ArrayList<>());
    }


    @GetMapping("/getUserList")
    @ApiOperation(value = "获取当前数据库中所有用户信息", httpMethod = "GET", notes = "获取当前数据库中所有用户信息")
    public ResultPO getUserList(
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "userDeptId", required = false) String userDeptId,
            @RequestParam(value = "userDesc", required = false) String userDesc,
            @RequestParam(value = "systemId", required = false) String systemId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "start") Integer start,
            @RequestParam(value = "pageSize") Integer pageSize) throws Exception {
        if (StringUtil.isEmpty(SecurityUserHelper.getCurrentUser().getUserId())) {
            return ResultPO.failure("当前用户登录超时。请重新登录");
        }
        PageInfo<AcisSystemUserVo> userList = sysUserService.getUserList(userName, userDeptId, userDesc, systemId, status, start, pageSize);
        return ResultPO.success(userList != null ? userList : new ArrayList<>());
    }
}

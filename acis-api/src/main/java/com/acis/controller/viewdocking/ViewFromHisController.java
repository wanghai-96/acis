package com.acis.controller.viewdocking;


import com.acis.common.exception.R;
import com.acis.dao.viewdocking.ViewDockingMapper;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author Neon Xie
 * @Date 2020/12/28
 * @description 调用院方视图信息 控制器 , 我写的代码 ,你要知道是什么 , 不要知道为什么
 */
@Log4j2
@RestController
@CrossOrigin
@Api("院方视图信息 控制器")
@RequestMapping("acis/viewDocking/ViewFromHisController")
public class ViewFromHisController {

    @Autowired
    private ViewDockingMapper viewDockingMapper;


    @PostMapping("/testForView")
    public R testForView(){
        return R.data(viewDockingMapper.getPatOperation());
    }

    @GetMapping("/testForSqlServer")
    public R testForSqlServer(){
        return R.data(viewDockingMapper.testSqlserver());
    }
    
}

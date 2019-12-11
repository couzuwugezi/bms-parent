package com.bms.project.controller;

import com.bms.project.common.AccountUtil;
import com.bms.project.common.NetUtil;
import com.bms.project.common.model.LoginFormVo;
import com.bms.project.generator.entity.BmsLoginLogMp;
import com.bms.project.generator.service.IBmsLoginLogMpService;
import com.bms.project.model.Constant;
import com.bms.project.model.ResponseData;
import com.bms.project.service.IndexService;
import com.bms.project.shiro.ShiroProperty;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.code.kaptcha.Producer;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author liqiang
 * @date 2019/10/26 22:25
 */
@Controller
public class IndexController {

    @Autowired
    private ShiroProperty shiroProperty;

    @Autowired
    private IndexService indexService;

    @Autowired
    private IBmsLoginLogMpService iBmsLoginLogMpService;

    @Autowired
    private Producer producer;

    @RequestMapping("/captcha*")
    public void getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        String capText = producer.createText();
        BufferedImage bi = producer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        HttpSession session = request.getSession();
        //存入Session
        session.setAttribute(Constant.KAPTCHA_SESSION_KEY, capText);
        ImageIO.write(bi, "JPEG", out);
        try {
            out.flush();
        } finally {
            out.close();
        }
    }

    @PostMapping("/check")
    @ResponseBody
    public ResponseData check(@RequestBody LoginFormVo loginFormVo, HttpServletRequest httpServletRequest) {
        ResponseData login = indexService.login(loginFormVo);
        String failReason = "";
        if (!login.isSuccess()) {
            failReason = login.getRtnMsg();
        }
        BmsLoginLogMp loginLogMp = new BmsLoginLogMp().setFailReason(failReason).setOperateType("1").setLoginIp(NetUtil.getRealIpAddress(httpServletRequest)).setOperateTime(LocalDateTime.now()).setLoginName(loginFormVo.getUsername());
        iBmsLoginLogMpService.save(loginLogMp);
        return login;
    }

    @RequestMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        iBmsLoginLogMpService.save(new BmsLoginLogMp()
                .setOperateTime(LocalDateTime.now())
                .setOperateType("2")
                .setFailReason("")
                .setLoginName(AccountUtil.getCurrentAccount().getLoginName())
                .setLoginIp(NetUtil.getRealIpAddress(request)));
        SecurityUtils.getSubject().logout();
        WebUtils.issueRedirect(request, response, "/login.html");
    }

    @GetMapping("/loadMainDiv")
    public String loadMainDiv(@RequestParam String page) {
        return page;
    }

    @RequestMapping(value = {"/login", "/login.html"})
    public String toLogin(Model model) {
        model.addAttribute("needCaptcha", shiroProperty.isIfNeedCaptcha());
        return "login";
    }

    /**
     * 加载菜单
     *
     * @return
     */
    @PostMapping("/loadMenus")
    @ResponseBody
    @RequiresAuthentication
    public ResponseData loadMenus() {
        return indexService.loadMenus();
    }

    @PostMapping("/loadLoginLog")
    @ResponseBody
    @RequiresAuthentication
    public ResponseData loadLoginLog() {
        return ResponseData.success(iBmsLoginLogMpService.list(new QueryWrapper<BmsLoginLogMp>().orderByDesc("operate_time")));
    }
}

package com.example.demo.config.interceptor;

import com.example.demo.annotion.LoginRequired;
import com.example.demo.util.DemoUtil;
import com.example.demo.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            if (loginRequired != null && hostHolder.getUser() == null) {
                String XRequestedWith = request.getHeader("X-requested-with");
                if (XRequestedWith == null) {
                    //X-Requested-With: XMLHttpRequest
                    //同步请求
                    response.sendRedirect(request.getContextPath() + "/login");
                } else {
                    //Ajax 请求
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(DemoUtil.getJSONString(1, "您还没有登录！"));
                }
                return false;
            }
        }
        return true;
    }
}

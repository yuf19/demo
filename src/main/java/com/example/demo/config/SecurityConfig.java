package com.example.demo.config;

import com.example.demo.util.DemoUtil;
import com.example.demo.util.IDenoConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements IDenoConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resource/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        //授权


    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_ADMIN
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/delete",
                "/data",
                "/actuator/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
        //禁用csrf
        /*.and().csrf().disable()*/;

        //权限不够时的处理
        http.exceptionHandling()
                //没登录时
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String XRequestedWith = request.getHeader("X-requested-with");
                        if (XRequestedWith == null) {
                            //同步请求
                            response.sendRedirect(request.getContextPath() + "/login");
                        } else {
                            //Ajax 请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(DemoUtil.getJSONString(403, "您还没有登录！"));
                        }
                    }
                })
                //登陆了但是权限不够
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String XRequestedWith = request.getHeader("X-requested-with");
                        if (XRequestedWith == null) {
                            //同步请求
                            response.sendRedirect(request.getContextPath() + "/denied");
                        } else {
                            //Ajax 请求
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(DemoUtil.getJSONString(403, "您没有此权限！"));
                        }
                    }
                });

        //Security底层默认会拦截/logout请求，进行退出处理
        //覆盖它默认的逻辑，才能执行我们自己的退出代码
        http.logout().logoutUrl("/securityLogout");
    }
}

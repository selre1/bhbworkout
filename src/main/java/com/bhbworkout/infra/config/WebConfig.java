package com.bhbworkout.infra.config;

import com.bhbworkout.modules.notification.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final NotificationInterceptor notificationInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //registry.addInterceptor(notificationInterceptor); 모든 요청에 적용 됌

        /*
        * static 리소스 요청에 적용시키고 싶지 않음
        * */
        List<String> staticResourcesPath = Arrays.stream(StaticResourceLocation.values()).flatMap(StaticResourceLocation::getPatterns).collect(Collectors.toList());
        staticResourcesPath.add("/node_modules/**");


        registry.addInterceptor(notificationInterceptor)
                .excludePathPatterns(staticResourcesPath);
    }
}

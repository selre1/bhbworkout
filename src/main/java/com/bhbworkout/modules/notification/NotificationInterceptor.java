package com.bhbworkout.modules.notification;

import com.bhbworkout.modules.account.Account;
import com.bhbworkout.modules.account.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;


    /*
    * 모든 핸들러(모든 상황)마다 읽지 않은 알림이 있으면 표시하기 위함
    * postHandle는 뷰렌더링하기 전에 인터셉터함
    *
    * */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        /*
        * 인증정보가 있는 응답에만 적용
        * 리다이렉션은 적용x ( 컨트롤러 2번 타는거 중복 방지)
        * anonimous x
        * */
        if(modelAndView != null && !isRdirectView(modelAndView) && authentication != null && authentication.getPrincipal() instanceof UserAccount){
            Account account = ((UserAccount) authentication.getPrincipal()).getAccount();
            long count = notificationRepository.countByAccountAndChecked(account,false);
            modelAndView.addObject("hasNotification", count > 0);
        }
    }

    private boolean isRdirectView(ModelAndView modelAndView) {
        return modelAndView.getViewName().startsWith("redirect:") || modelAndView.getView() instanceof RedirectView;
    }
}

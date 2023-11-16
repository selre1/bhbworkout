package com.bhbworkout.account;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지 되어야함
@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있도록 함
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account") //이 어노테이션 참조하고 있는 객체가 anonymous면 null 아니면 account
public @interface CurrentUser {
}

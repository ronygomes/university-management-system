package me.ronygomes.ums.api.config.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@PreAuthorize("hasRole('ADMIN')")
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminAccess {
}

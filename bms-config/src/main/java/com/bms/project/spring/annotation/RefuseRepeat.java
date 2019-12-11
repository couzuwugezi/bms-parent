package com.bms.project.spring.annotation;

import java.lang.annotation.*;

/**
 * @author liqiang
 * @date 2019/11/7 13:16
 * 拒绝重复提交
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RefuseRepeat {
}

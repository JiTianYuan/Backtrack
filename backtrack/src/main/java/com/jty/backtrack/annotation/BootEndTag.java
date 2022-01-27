package com.jty.backtrack.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jty
 * @date 2022/1/16
 *
 * 启动结束的标记方法，该方法结束后认作是启动行为的结束
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface BootEndTag {
}

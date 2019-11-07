package io.spring.annotation;

import java.lang.annotation.*;

/**
 * @ClassName RequestParam
 * @author Jason
 * @Date  2019-11-04 23:18
 * @version 1.0  
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
	String value() default "";
}

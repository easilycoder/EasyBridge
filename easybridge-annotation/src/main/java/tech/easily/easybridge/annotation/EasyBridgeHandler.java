package tech.easily.easybridge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface EasyBridgeHandler {

    // TODO: 2018/4/4 maybe we should collect the info about the constructor,for building all the instance with different constructor

    String name() default "";
}

package com.github.genju83.simplepreference;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Victor on 10/3/17.
 */

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SimplePreference {
    String value();
}

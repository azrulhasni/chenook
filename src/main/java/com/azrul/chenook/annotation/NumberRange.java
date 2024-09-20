/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author azrul
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberRange {
    long min() default -1;
    long max() default -1;
    String[] message() default {};
}

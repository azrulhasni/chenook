/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.annotation;

/**
 *
 * @author azrul
 */
public @interface Matcher {
    String regexp();
    String[] message() default {};
}

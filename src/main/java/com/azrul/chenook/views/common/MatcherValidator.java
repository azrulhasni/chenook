/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.vaadin.flow.data.validator.RegexpValidator;

/**
 *
 * @author azrul
 */
public class MatcherValidator extends RegexpValidator{
    public MatcherValidator(String regexp, String message){
        super(regexp, message);
    }
}

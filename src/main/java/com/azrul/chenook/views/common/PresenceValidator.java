/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author azrul
 */
public class PresenceValidator<T> extends AbstractValidator<T> {


    public PresenceValidator(String message) {
        super(message);
    }

    @Override
    public ValidationResult apply(T t, ValueContext vc) {
        if (t == null) {
            return ValidationResult.error(this.getMessage(t));
        } else {
            if (t instanceof String s) {
                if (StringUtils.isBlank(s)) {
                    return ValidationResult.error(this.getMessage(t));
                } else {
                    return ValidationResult.ok();
                }
            }
            return ValidationResult.ok();
        }
        
    }
}

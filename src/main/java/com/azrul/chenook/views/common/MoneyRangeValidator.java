/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import javax.money.MonetaryAmount;

/**
 *
 * @author azrul
 */
public class MoneyRangeValidator extends AbstractValidator<MonetaryAmount> {
    private Long min;
    private Long max;
    
    public MoneyRangeValidator(String message, Long min, Long max){
        super(message);
        this.min=min;
        this.max=max;
    }
    
    @Override
    public ValidationResult apply(MonetaryAmount t, ValueContext vc) {
        if (t==null){
            return ValidationResult.ok();
        }
        
        if (t.getNumber().longValue()<min){
            return ValidationResult.error(this.getMessage(t));
        }
        if (t.getNumber().longValue()>max){
            return ValidationResult.error(this.getMessage(t));
        }
        return ValidationResult.ok();
    }
    
}

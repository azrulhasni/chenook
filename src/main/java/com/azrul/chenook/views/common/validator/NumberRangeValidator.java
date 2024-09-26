/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.validator;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RangeValidator;
import java.math.BigDecimal;
import javax.money.MonetaryAmount;

/**
 *
 * @author azrul
 */
public class NumberRangeValidator extends RangeValidator<Number> {
    public NumberRangeValidator(String message, Long min, Long max){
        super(
                message,
                (Number n1,Number n2)->Double.compare(n1.doubleValue(), n2.doubleValue()),
                min,
                max
        );
    }
    
//    @Override
//    public ValidationResult apply(Number t, ValueContext vc) {
//        if (t==null){
//            return ValidationResult.ok();
//        }
//        
//        if (t.longValue()<min){
//            return ValidationResult.error(this.getMessage(t));
//        }
//        if (t.longValue()>max){
//            return ValidationResult.error(this.getMessage(t));
//        }
//        return ValidationResult.ok();
//    }
    
}

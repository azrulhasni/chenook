/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.validator;

import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.AbstractValidator;
import com.vaadin.flow.data.validator.RangeValidator;
import java.util.Comparator;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

/**
 *
 * @author azrul
 */
public class MoneyRangeValidator extends RangeValidator<MonetaryAmount> {
    
    public MoneyRangeValidator(String message, String currencyCode, Long min, Long max){
        super(
                message, 
                (MonetaryAmount m1, MonetaryAmount m2) -> m1.compareTo(m2),
                Monetary.getDefaultAmountFactory().setCurrency(currencyCode).setNumber(min).create(),
                Monetary.getDefaultAmountFactory().setCurrency(currencyCode).setNumber(min).create()
        );
    }
   
    
//    @Override
//    public ValidationResult apply(MonetaryAmount t, ValueContext vc) {
//        if (t==null){
//            return ValidationResult.ok();
//        }
//        
//        if (t.getNumber().longValue()<min){
//            return ValidationResult.error(this.getMessage(t));
//        }
//        if (t.getNumber().longValue()>max){
//            return ValidationResult.error(this.getMessage(t));
//        }
//        return ValidationResult.ok();
//    }
    
}

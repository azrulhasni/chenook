/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import org.vaadin.addons.MoneyField;

/**
 *
 * @author azrul
 */
public class WorkflowAwareMoneyField<T> extends MoneyField {


    public WorkflowAwareMoneyField() {
    }

//    String fieldName, String currencyCode , T workItem
//
//    
//        ){
//        //T workItem = binder.getBean();
//        Optional<WorkField> owf = WorkflowUtils.getAnnotation(WorkField.class, workItem, fieldName);
//        owf.ifPresent(wf -> {
//            this.setLabel(fieldName);
//            if (wf.prefix().length > 0) {
//                this.setCurrency(Currency.getInstance(currencyCode));
//            }
//        });
//
//        Optional<NotNullValue> onb = WorkflowUtils.getAnnotation(NotNullValue.class, workItem, fieldName);
//        onb.ifPresent(nb -> {
//            this.setRequiredIndicatorVisible(true);
//            if (nb.message().length > 0) {
//                this.setErrorMessage(nb.message()[0]);
//            } else {
//                owf.ifPresentOrElse(wf -> {
//                    this.setErrorMessage(wf.displayName() + " cannot be empty");
//                }, () -> {
//                    this.setErrorMessage(fieldName + " cannot be empty");
//                });
//            }
//        });
//        Optional<MinValue> omin = WorkflowUtils.getAnnotation(MinValue.class, workItem, fieldName);
//        omin.ifPresent(m -> {
//            if (m.value() > 0) {
//                this.setMin((Long) m.value());
//                this.setMinMessage(m.message()[0]);
//            }
//        });
//
//        Optional<MaxValue> omax = WorkflowUtils.getAnnotation(MaxValue.class, workItem, fieldName);
//        omax.ifPresent(m -> {
//            if (m.value() > 0) {
//                this.setMax((Long) m.value());
//                this.setMaxMessage(m.message()[0]);
//            }
//        });
//
//    }

//    @Override
//    public void setValue(MonetaryAmount v) {
//        if (v != null) {
//            if (getMin() != null && getMin().compareTo(v.getNumber().longValue()) < 0) {
//                this.setErrorMessage(getMinMessage());
//            }
//            if (getMax() != null && getMax().compareTo(v.getNumber().longValue()) > 0) {
//                this.setErrorMessage(getMaxMessage());
//            }
//        }
//        super.setValue(v);
//    }

//    public static <T>WorkflowAwareMoneyField create(String fieldName,String currencyCode, Binder<T> binder){
//        T workItem = binder.getBean();
//        var field = new WorkflowAwareMoneyField(fieldName,currencyCode,workItem);
//        binder.bind(field, fieldName);
//        return field;
//    }
    public static <T> WorkflowAwareMoneyField create(String fieldName, String currencyCode, Binder<T> binder) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareMoneyField();
        field.setId(fieldName);
        field.setCurrency(Currency.getInstance(currencyCode));

        List<Validator> validators = new ArrayList<>();
        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(), 
                fieldName);
        
        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap, 
                field
        );
        
        validators.addAll(
                WorkflowUtils.applyNotNull(
                    annoFieldDisplayMap, 
                    field, 
                    workfieldMap, 
                    fieldName
                )
        );
        
        validators.addAll(
                WorkflowUtils.applyMoneyRange(
                    annoFieldDisplayMap, 
                    workfieldMap, 
                    fieldName,
                    currencyCode
                )
        );

        var bindingBuilder = binder.forField(field);
        // bindingBuilder.withNullRepresentation(new MonetaryAmount());
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

  
}
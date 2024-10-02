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

     private WorkflowAwareGroup group;
    
    public WorkflowAwareMoneyField(WorkflowAwareGroup group){
        this.group=group;
    }
    
    public void applyGroup(){
        if (group!=null){
            this.setReadOnly(!group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }
    
    public static <T> WorkflowAwareMoneyField create(String fieldName, String currencyCode, Binder<T> binder) {
        return create(fieldName, currencyCode, binder, null);
    }

    public static <T> WorkflowAwareMoneyField create(String fieldName, String currencyCode, Binder<T> binder, WorkflowAwareGroup group) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareMoneyField(group);
        field.setId(fieldName);
        field.setCurrency(Currency.getInstance(currencyCode));
        field.applyGroup();

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
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

  
}

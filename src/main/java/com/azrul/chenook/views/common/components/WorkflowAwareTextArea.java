/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author azrul
 */
public class WorkflowAwareTextArea<T> extends TextArea {

    public WorkflowAwareTextArea(){}

    public static <T> WorkflowAwareTextArea create(String fieldName, Binder<T> binder) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareTextArea();
        field.setId(fieldName);
        List<Validator> validators = new ArrayList<>();

         var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(), 
                fieldName
        );
        
        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap, 
                field
        );
        
        validators.addAll(
                WorkflowUtils.applyNotBlank(
                        annoFieldDisplayMap, 
                        field, 
                        workfieldMap, 
                        fieldName
                )
        );
        
        validators.addAll(
                WorkflowUtils.applyMatcher(
                        annoFieldDisplayMap
                )
        );
        
        var bindingBuilder = binder.forField(field);
         bindingBuilder.withNullRepresentation("");
        for (var validator:validators){
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }
}
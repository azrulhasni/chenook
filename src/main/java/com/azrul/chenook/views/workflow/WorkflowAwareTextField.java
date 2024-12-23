/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.converter.Converter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author azrul
 */
public class WorkflowAwareTextField<T> extends TextField {

    private WorkflowAwareGroup group;
    
    public WorkflowAwareTextField(WorkflowAwareGroup group){
        this.group=group;
    }
    
    public void applyGroup(){
        if (group!=null){
            this.setReadOnly(!group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }
    
  
    public static <T> WorkflowAwareTextField create(String fieldName, Boolean readOnly, Binder<T> binder, Converter converter, WorkflowAwareGroup group) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareTextField(group);
        List<Validator> validators = new ArrayList<>();
        field.setId(fieldName);
        field.applyGroup();

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
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        
        if (readOnly) {
            if (converter != null) {
                bindingBuilder.withConverter(converter).bindReadOnly(fieldName);
            } else {
                bindingBuilder.bindReadOnly(fieldName);
            }
        } else {
            if (converter != null) {
                bindingBuilder.withConverter(converter).bind(fieldName);
            } else {
                bindingBuilder.bind(fieldName);
            }
        }
        field.setId(fieldName);
        return field;
    }

    public static <T> WorkflowAwareTextField create(String fieldName, Boolean readOnly, Binder<T> binder, WorkflowAwareGroup group) {
        return create(fieldName, readOnly, binder, null, group);
    }
    
    public static <T> WorkflowAwareTextField create(String fieldName, Boolean readOnly, Binder<T> binder) {
        return create(fieldName, readOnly, binder, null, null);
    }
    
    

}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;

import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author azrul
 */
public class WorkflowAwareNumberField<T> extends NumberField {
    private WorkflowAwareGroup group;
    
    private WorkflowAwareNumberField(WorkflowAwareGroup group) {
        this.group = group;
    }
    
  
    
    public void applyGroup(){
        if (this.group!=null){
            this.setReadOnly(!group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }
    
    public static <T> WorkflowAwareNumberField create(String fieldName, Binder<T> binder){
        return create(fieldName, binder, null);
    }

    public static <T> WorkflowAwareNumberField create(String fieldName, Binder<T> binder, WorkflowAwareGroup group) {
   
            T workItem = binder.getBean();
            var field = new WorkflowAwareNumberField(group);
            field.setId(fieldName);
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
                    WorkflowUtils.applyNumberRange(
                            annoFieldDisplayMap,
                            workfieldMap,
                            fieldName
                    )
            );
            
            var bindingBuilder = binder.forField(field);
            
            bindingBuilder.withNullRepresentation(0.0);
            for (var validator : validators) {
                bindingBuilder.withValidator(validator);
            }
           
            bindingBuilder.bind(fieldName);
            return field;
       
    }

    
}
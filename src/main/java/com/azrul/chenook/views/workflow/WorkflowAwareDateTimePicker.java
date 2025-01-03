/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.workflow;


import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author azrul
 */
public class WorkflowAwareDateTimePicker<T> extends DateTimePicker{
    private WorkflowAwareGroup group;
    
    public WorkflowAwareDateTimePicker(WorkflowAwareGroup group){
        this.group=group;
    }
    
    public WorkflowAwareDateTimePicker(){
        this.group=null;
    }
    
    public void applyGroup(){
        if (this.group!=null){
            this.setReadOnly(!group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }
    
     public static <T> WorkflowAwareDateTimePicker create(String fieldName, Binder<T> binder) {
        return create(fieldName, binder, null);
     }
    
    public static <T> WorkflowAwareDateTimePicker create(String fieldName, Binder<T> binder, WorkflowAwareGroup group) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareDateTimePicker(group);
        field.applyGroup();
        field.setId(fieldName);

        List<Validator> validators = new ArrayList<>();
       var annoFieldDisplayMap = WorkflowUtils.getAnnotations(workItem.getClass(), fieldName);
     
        Map<String,Object> workfieldMap = WorkflowUtils.applyWorkField(annoFieldDisplayMap,field);
        
        validators.addAll(WorkflowUtils.applyNotNull(annoFieldDisplayMap, field, workfieldMap, fieldName));

        WorkflowUtils.applyDateTimeFormat(annoFieldDisplayMap, field);
        
        var bindingBuilder = binder.forField(field);
        
        for (var validator:validators){
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

}

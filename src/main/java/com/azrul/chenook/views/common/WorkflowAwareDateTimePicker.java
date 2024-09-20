/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;


import com.azrul.chenook.annotation.DateTimeFormat;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author azrul
 */
public class WorkflowAwareDateTimePicker<T> extends DateTimePicker{
    public WorkflowAwareDateTimePicker(){}
    
    public static <T> WorkflowAwareDateTimePicker create(String fieldName, Binder<T> binder) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareDateTimePicker();

        List<Validator> validators = new ArrayList<>();
       Map<Class<? extends Annotation>,Map<String,Object>> annoFieldDisplayMap = WorkflowUtils.getAnnotations(workItem.getClass(), fieldName);
     
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

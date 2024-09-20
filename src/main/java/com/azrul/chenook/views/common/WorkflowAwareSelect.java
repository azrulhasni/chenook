/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;


import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author azrul
 */
public class WorkflowAwareSelect<T> extends Select {
    public WorkflowAwareSelect(){}
    
    
    public static <T>WorkflowAwareSelect create(String fieldName,Binder<T> binder){
        T workItem = binder.getBean();
        var field = new WorkflowAwareSelect();
         List<Validator> validators = new ArrayList<>();
       var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(), 
                fieldName);
        
        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap,
                field);
        
        validators.addAll(
                WorkflowUtils.applyNotNull(
                        annoFieldDisplayMap, 
                        field, 
                        workfieldMap, 
                        fieldName));
        
        var bindingBuilder = binder.forField(field);
        //bindingBuilder.withNullRepresentation("");
        for (var validator:validators){
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }
}

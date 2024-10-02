/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 *
 * @author azrul
 */
public class WorkflowAwareComboBox<T, C> extends ComboBox<C> {
    private WorkflowAwareGroup group;
    
    private WorkflowAwareComboBox(WorkflowAwareGroup group) {
        this.group = group;
    }
 
    
    public void applyGroup(){
        if (this.group!=null){
            this.setReadOnly(!group.calculateEnable());
            this.setVisible(group.calculateVisible());
        }
    }
    
    public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, Set<C> data, WorkflowAwareGroup group){
        return create(fieldName, binder, field->field.setItems(data), group);
    }
    
     public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, BackEndDataProvider<String, Void> dp, WorkflowAwareGroup group){
        return create(fieldName, binder, field->field.setItems(dp), group);
    }
     
     public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, Set<C> data){
        return create(fieldName, binder, field->field.setItems(data), null);
    }
    
     public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, BackEndDataProvider<String, Void> dp){
        return create(fieldName, binder, field->field.setItems(dp),null);
    }
    
   

    public static <T, C> WorkflowAwareComboBox create(
            String fieldName, Binder<T> binder, Consumer<WorkflowAwareComboBox> dataSetter, WorkflowAwareGroup group) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareComboBox(group);
        field.setId(fieldName);
        field.applyGroup();

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
       
        dataSetter.accept(field);
        var bindingBuilder = binder.forField(field);

        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }

}

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

    private WorkflowAwareComboBox() {
    }
    
    public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, Set<C> data){
        return create(fieldName, binder, field->field.setItems(data));
    }
    
     public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, BackEndDataProvider<String, Void> dp){
        return create(fieldName, binder, field->field.setItems(dp));
    }
    
   

    public static <T, C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, Consumer<WorkflowAwareComboBox> dataSetter) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareComboBox();
        field.setId(fieldName);

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
//        Optional<WorkField> owf = WorkflowUtils.getAnnotation(WorkField.class,workItem, fieldName);
//        owf.ifPresent(wf->{
//            this.setLabel(fieldName);
//        });
//        Optional<NotNullValue> onb = WorkflowUtils.getAnnotation(NotNullValue.class,workItem, fieldName);
//        onb.ifPresent(nb->{
//            this.setRequiredIndicatorVisible(true);
//           if (nb.message().length>0) {
//                this.setErrorMessage(nb.message()[0]);
//            } else {
//                owf.ifPresentOrElse(wf -> {
//                    this.setErrorMessage(wf.displayName() + " cannot be empty");
//                }, () -> {
//                    this.setErrorMessage(fieldName + " cannot be empty");
//                });
//            }
//        });
//    }
//    
//    public static <T>WorkflowAwareComboBox create(String fieldName,Binder<T> binder){
//        T workItem = binder.getBean();
//        var field = new WorkflowAwareComboBox(fieldName,workItem);
//        binder.bind(field, fieldName);
//        return field;
//    }
}

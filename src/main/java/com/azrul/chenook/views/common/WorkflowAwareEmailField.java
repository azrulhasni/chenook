/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;

import com.azrul.chenook.annotation.NotBlankValue;
import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author azrul
 */
public class WorkflowAwareEmailField<T> extends EmailField{
    public WorkflowAwareEmailField(){}
    
     public static <T> WorkflowAwareEmailField create(String fieldName, Binder<T> binder) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareEmailField();

        List<Validator> validators = new ArrayList<>();
        var annotations = WorkflowUtils.getAnnotations(workItem, fieldName);
        if (annotations.containsKey(WorkField.class)) {
            WorkField wf = (WorkField) annotations.get(WorkField.class);
            field.setLabel(wf.displayName());
        }
        if (annotations.containsKey(NotNullValue.class)) {
            NotBlankValue nb = (NotBlankValue) annotations.get(NotBlankValue.class);
            field.setRequiredIndicatorVisible(true);

            if (nb.message().length > 0) {
                validators.add(new PresenceValidator(nb.message()[0]));
            } else {
                if (annotations.containsKey(WorkField.class)) {
                    WorkField wf = (WorkField) annotations.get(WorkField.class);
                    validators.add(new PresenceValidator("Field " + wf.displayName() + " is empty"));
                } else {
                    validators.add(new PresenceValidator("Field " + fieldName + " is empty"));
                }
            }
        }
        var bindingBuilder = binder.forField(field);
         bindingBuilder.withNullRepresentation("");
        for (var validator:validators){
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);
        return field;
    }
    
//    String fieldName,  T workItem){
//        Optional<WorkField> owf = WorkflowUtils.getAnnotation(WorkField.class,workItem, fieldName);
//        owf.ifPresent(wf->{
//            this.setLabel(fieldName);
//        });
//        Optional<NotBlankValue> onb = WorkflowUtils.getAnnotation(NotBlankValue.class,workItem, fieldName);
//        onb.ifPresent(nb->{
//            this.setRequiredIndicatorVisible(true);
//            if (nb.message().length>0) {
//                this.setErrorMessage(nb.message()[0]);
//            } else {
//                owf.ifPresentOrElse(wf -> {
//                    this.setErrorMessage(wf.displayName() + " cannot be empty");
//                }, () -> {
//                    this.setErrorMessage(fieldName + " cannot be empty");
//                });
//            }
//        });
//       
//    }
//    
//    public static <T>WorkflowAwareEmailField create(String fieldName,Binder<T> binder){
//        T workItem = binder.getBean();
//        var field = new WorkflowAwareEmailField(fieldName,workItem);
//        binder.bind(field, fieldName);
//        return field;
//    }
}
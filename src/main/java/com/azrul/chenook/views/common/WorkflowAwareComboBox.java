/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common;


import com.azrul.chenook.annotation.NotNullValue;
import com.azrul.chenook.annotation.WorkField;
import com.azrul.chenook.utils.WorkflowUtils;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 *
 * @author azrul
 */
public class WorkflowAwareComboBox<T,C> extends ComboBox<C>{
    private WorkflowAwareComboBox(){}
    
     public static <T,C> WorkflowAwareComboBox create(String fieldName, Binder<T> binder, Set<C> data) {
        T workItem = binder.getBean();
        var field = new WorkflowAwareComboBox();

        List<Validator> validators = new ArrayList<>();
       var annotations = WorkflowUtils.getAnnotations(workItem, fieldName);
        if (annotations.containsKey(WorkField.class)) {
            WorkField wf = (WorkField) annotations.get(WorkField.class);
            field.setLabel(wf.displayName());
            if (wf.prefix().length > 0) {
                field.setPrefixComponent(new NativeLabel(wf.prefix()[0]));
            }
        }
        if (annotations.containsKey(NotNullValue.class)) {
            NotNullValue nb = (NotNullValue) annotations.get(NotNullValue.class);
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
        field.setItems(data);
        var bindingBuilder = binder.forField(field);
       
        for (var validator:validators){
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

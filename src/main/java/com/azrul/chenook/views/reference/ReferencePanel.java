package com.azrul.chenook.views.reference;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.azrul.chenook.annotation.SingleValue;
import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.BadgeUtils;
import com.azrul.chenook.service.ReferenceService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.workflow.GridMemento;
import com.azrul.chenook.views.workflow.WorkflowAwareGridBuilder;
import com.azrul.chenook.views.workflow.WorkflowAwareGroup;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class ReferencePanel<T extends WorkItem, R extends Reference, RS extends ReferenceService<R>>
        extends CustomField<Set<R>> {
    private int maxSelection = 0;

    private ReferenceService<R> refService;
    private BadgeUtils badgeUtils;
    private WorkflowAwareGroup<T> group;
    private Button btnSelectDialog;
    private Button btnDelete;
    private ListBox<R> refList =new ListBox<>();;

    @SuppressWarnings("unchecked")
    private ReferencePanel(
            final String fieldName,
            final Binder<T> binder,
            final WorkflowAwareGroup<T> group,
            final RS refService,
            final BadgeUtils badgeUtils) {
        this.refService = refService;
        this.group = group;
        this.badgeUtils = badgeUtils;
       
        refList.setHeight("auto");
        refList.setMinHeight("var(--lumo-size-s)");
        refList.getStyle().set("border", "1px solid lightgray");
        refList.getStyle().set("border-radius", "10px");
        refList.getStyle().set("padding-left", "5px");

        Class<? extends WorkItem> workClass = binder.getBean().getClass();

        try {
            Field workField = WorkflowUtils.getField(workClass, fieldName);
            if (workField.isAnnotationPresent(SingleValue.class)) {
                maxSelection = 1;
            } else {
                maxSelection = 0;
            }

            Class<R> referenceClass = (Class<R>) ((ParameterizedType) workField.getGenericType())
                    .getActualTypeArguments()[0];


            if (workField.get(binder.getBean())!=null){
                refList.setItems((Set<R>)workField.get(binder.getBean()));
            }else{
                refList.setItems(Set.of()); 
                workField.set(binder.getBean(), Set.of());
            }

            btnDelete = new Button("Delete", e -> {
                try {
                    Set<R> currentValues = new HashSet<>((Set<R>)workField.get(binder.getBean()));
                    currentValues.remove(refList.getValue());
                    workField.set(binder.getBean(), currentValues);
                    refList.setItems((Set<R>)workField.get(binder.getBean()));
                } catch (IllegalArgumentException | IllegalAccessException e1) {
                    Logger.getLogger(ReferencePanel.class.getName()).log(Level.SEVERE, null, e1);
                }
            });
            btnDelete.setId("btnDelete-"+fieldName);

            btnSelectDialog = new Button("Select", e -> {
                Dialog dialog = buildDialog(fieldName,referenceClass, (selections) -> {
                    try {
                        workField.set(binder.getBean(), selections);
                        refList.setItems(selections);
                    } catch (IllegalArgumentException | IllegalAccessException e1) {
                        // TODO Auto-generated catch block
                        Logger.getLogger(ReferencePanel.class.getName()).log(Level.SEVERE, null, e1);
                    }
                });
                dialog.open();
            });
            btnSelectDialog.setId("btnSelectDialog-"+fieldName);

            HorizontalLayout refPanel = new HorizontalLayout();
            refPanel.setWidthFull();
            refPanel.setAlignItems(FlexComponent.Alignment.START);
            refPanel.add(refList,btnSelectDialog,btnDelete);
            btnSelectDialog.getStyle().set("margin-left", "auto");
            refList.setWidthFull();

            this.add(refPanel);
        } catch (SecurityException
                | IllegalArgumentException
                | IllegalAccessException e) {
                    Logger.getLogger(ReferencePanel.class.getName()).log(Level.SEVERE, null, e);
        } 
    }

    public void applyGroup() {
        if (group != null) {
            this.setReadOnly(!group.calculateEnable());
            this.btnDelete.setEnabled(group.calculateEnable());
            this.refList.setReadOnly(!group.calculateEnable());
            this.btnSelectDialog.setEnabled(group.calculateEnable());
            this.setVisible(group.calculateVisible());
            this.btnSelectDialog.setVisible(group.calculateVisible());
            this.btnDelete.setVisible(group.calculateVisible());
            this.refList.setVisible(group.calculateVisible());
        }
    }



    private Dialog buildDialog(
            String fieldName,
            Class<R> referenceClass,
            Consumer<Set<R>> postSelection) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select reference(s)");
       
        GridMemento<R> memento = WorkflowAwareGridBuilder.<R>build(
                fieldName,
                referenceClass, 
                m->refService.countActiveReferenceData(referenceClass,m.getSearchPanel()), 
                m->refService.getActiveReferenceData(referenceClass, m.getSearchPanel(), m.getPageNav()),
                Optional.of(()->createGrid(referenceClass))
        );
     
        dialog.add(memento.getPanel());
        Button btnClose = new Button("Close", e -> dialog.close());
        Button btnSelect = new Button("Select", e -> {
            Set<R> selectedItems = memento.getGrid().getSelectedItems();
            postSelection.accept(selectedItems);
            dialog.close();
        });
        btnSelect.setId("btnSelect-"+fieldName);
        dialog.getFooter().add(btnSelect, btnClose);
        return dialog;
    }

    private Grid<R> createGrid(Class<R> referenceClass) {
        Grid<R> grid = new Grid<>(referenceClass,false);
        var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
        for (var fieldEntry : fieldMap.entrySet()) {
            if (!StringUtils.equals(fieldEntry.getKey(),"status")){
                grid.addColumn(fieldEntry.getKey())
                        .setHeader(fieldEntry.getValue());
                grid.setAllRowsVisible(true);
            }
        }

        grid.addComponentColumn(r->{
            Span badge = badgeUtils.createRefStatusBadge(r.getStatus());
            return badge;
        }).setHeader("Status");
        
        //grid.setDataProvider(dataProvider);
        if (maxSelection == 1) {
            grid.setSelectionMode(SelectionMode.SINGLE);
            if (!refList.isEmpty()) {
                grid.asSingleSelect().setValue(refList.getValue());
            }

        } else {
            grid.setSelectionMode(SelectionMode.MULTI);
            if (!refList.isEmpty()) {
                grid.asMultiSelect().select(refList.getListDataView().getItems().toList());
            }
        }
        return grid;
    }

    public static <T extends WorkItem, R extends Reference, RS extends ReferenceService<R>> ReferencePanel<T, R, RS> create(
            final String fieldName,
            final Binder<T> binder,
            final WorkflowAwareGroup<T> group,
            final RS refService,
            final BadgeUtils badgeUtils) {

        T workItem = binder.getBean();
        var field = new ReferencePanel<T, R, RS>(fieldName, binder, group, refService, badgeUtils);
        List<Validator> validators = new ArrayList<>();
        field.setId(fieldName);
        field.applyGroup();

        var annoFieldDisplayMap = WorkflowUtils.getAnnotations(
                workItem.getClass(),
                fieldName);

        var workfieldMap = WorkflowUtils.applyWorkField(
                annoFieldDisplayMap,
                field);

        validators.addAll(
                WorkflowUtils.applyNotEmpty(
                        annoFieldDisplayMap,
                        field,
                        workfieldMap,
                        fieldName));

        var bindingBuilder = binder.forField(field);
        bindingBuilder.withNullRepresentation(Set.of());
        for (var validator : validators) {
            bindingBuilder.withValidator(validator);
        }
        bindingBuilder.bind(fieldName);

        return field;
    }

    @Override
    protected Set<R> generateModelValue() {
        return refList.getListDataView().getItems().collect(Collectors.toSet());
    }

    @Override
    public Set<R> getValue() {
        return generateModelValue();
    }   

    @Override
    protected void setPresentationValue(Set<R> arg0) {
       refList.setItems(arg0);
    }

    @Override
    public void setValue(Set<R> arg0) {
        setPresentationValue(arg0);
    }   

}
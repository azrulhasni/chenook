package com.azrul.chenook.views.reference;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.azrul.chenook.domain.Reference;
//import com.azrul.chenook.domain.ReferenceMap;
import com.azrul.chenook.domain.WorkItem;
import com.azrul.chenook.service.ReferenceService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.azrul.chenook.views.workflow.WorkflowAwareGroup;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.provider.DataProvider;

public class ReferencePanel<T extends WorkItem, R extends Reference, RS extends ReferenceService<R>>
        extends CustomField<Set<R>> {
    private final int COUNT_PER_PAGE = 3;
    private int maxSelection = 0;

    private ReferenceService<R> refService;
    private WorkflowAwareGroup group;
    private Button btnSelectDialog;
    private Set<R> currentReferences;

    @SuppressWarnings("unchecked")
    private ReferencePanel(
            final String fieldName,
            final Binder<T> binder,
            final WorkflowAwareGroup group,
            final RS refService) {
        this.refService = refService;
        this.group = group;
        VirtualList<R> refList = new VirtualList<>();
        refList.setHeight("auto");
        refList.getStyle().set("border","1px solid lightgray");
        refList.getStyle().set("border-radius", "10px"); 
        refList.getStyle().set("padding=leeft", "5px");

        Class<? extends WorkItem> workClass = binder.getBean().getClass();
        
        try {
            Field workField = WorkflowUtils.getField(workClass, fieldName);

            this.currentReferences = (Set<R>) workField.get(binder.getBean());
           
            Class<R> referenceClass = (Class<R>) ((ParameterizedType) workField.getGenericType())
                    .getActualTypeArguments()[0];
            if (currentReferences!=null && !currentReferences.isEmpty()){
                refList.setItems(currentReferences);
            }

            // textField.setReadOnly(true);
            btnSelectDialog = new Button("Select", e -> {
                Dialog dialog = buildDialog(referenceClass, (selections) -> {
                    /*
                     * this.currentReferenceMap = refService.getMap(parentId, referenceClass);
                     * R ref = currentReferenceMap.getReferences().iterator().next();
                     * textField.setValue(ref.toString());
                     */
                    this.currentReferences.clear();
                    this.currentReferences.addAll(selections);
                    refList.setItems(currentReferences);
                });
                dialog.open();
            });

            HorizontalLayout refPanel = new HorizontalLayout();
            refPanel.setWidthFull();
            refPanel.setAlignItems(FlexComponent.Alignment.CENTER);
            refPanel.add(refList, btnSelectDialog);
            btnSelectDialog.getStyle().set("margin-left", "auto");
            refList.setWidthFull();

            this.add(refPanel);
        } catch ( SecurityException 
        | IllegalArgumentException 
        | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void applyGroup() {
        if (group != null) {
            this.setReadOnly(!group.calculateEnable());
            this.btnSelectDialog.setEnabled(group.calculateEnable());
            this.setVisible(group.calculateVisible());
            this.btnSelectDialog.setVisible(group.calculateVisible());
        }
    }

    // private void init() {

    // this.getStyle().set("border", "1px solid lightgrey");
    // this.getStyle().set("border-radius", "10px");
    // }

    private Dialog buildDialog(
            Class<R> referenceClass,
            Consumer<Set<R>> postSelection) {
        Dialog dialog = new Dialog();
        SearchPanel searchPanel = new SearchPanel();
        Integer count = refService.countAllReferenceData(referenceClass, searchPanel);
        PageNav nav = new PageNav();
        DataProvider<R, Void> dataProvider = refService.getAllReferenceData(referenceClass, searchPanel, nav);
        Grid<R> grid = new Grid<>(referenceClass, false);
        nav.init(grid, count, COUNT_PER_PAGE);
        grid.setItems(dataProvider);
        Map<String, String> sortableFields = WorkflowUtils.getSortableFields(referenceClass);

        var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
        for (var fieldEntry : fieldMap.entrySet()) {
            grid.addColumn(fieldEntry.getKey())
                    .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                    .setHeader(fieldEntry.getValue());
            grid.setAllRowsVisible(true);
        }

        /*
         * ReferenceMap<R> refMap = refService.getMap(parentId, referenceClass);
         * for (int i = 0; i < grid.getDataCommunicator().getItemCount(); i++) {
         * R item = grid.getDataCommunicator().getItem(i);
         * if (refMap.getReferences().contains(item)) {
         * grid.getSelectionModel().select(item);
         * }
         * }
         */
        for (R ref : currentReferences) {
            grid.getSelectionModel().select(ref);
        }

        grid.setDataProvider(dataProvider);
        if (maxSelection == 1) {
            grid.setSelectionMode(SelectionMode.SINGLE);
        } else {
            grid.setSelectionMode(SelectionMode.MULTI);
        }
        // grid.setHeight("calc("+COUNT_PER_PAGE+" * var(--lumo-size-m))");

        searchPanel.searchRunner(s -> {
            Integer count2 = refService.countAllReferenceData(referenceClass, searchPanel);
            nav.refresh(count2);
            dataProvider.refreshAll();
        });

        dialog.add(searchPanel, nav, grid);
        Button btnClose = new Button("Close", e -> dialog.close());
        Button btnSelect = new Button("Select", e -> {
            Set<R> selectedItems = grid.getSelectedItems();
            postSelection.accept(selectedItems);
            dialog.close();
        });
        dialog.getFooter().add( btnSelect,btnClose);
        return dialog;
    }

    public static <T extends WorkItem, R extends Reference, RS extends ReferenceService<R>> ReferencePanel<T, R, RS> create(
            final String fieldName,
            final Binder<T> binder,
            final WorkflowAwareGroup group,
            final RS refService) {

        T workItem = binder.getBean();
        var field = new ReferencePanel<T, R, RS>(fieldName, binder, group, refService);
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
        return this.currentReferences;
    }

    @Override
    protected void setPresentationValue(Set<R> arg0) {
        this.currentReferences.clear();
        this.currentReferences.addAll(arg0);
    }

    /*
     * public ReferenceMap<R> getCurrentReferenceMap() {
     * return currentReferenceMap;
     * }
     * 
     * public void setCurrentReferenceMap(ReferenceMap<R> currentReferenceMap) {
     * this.currentReferenceMap = currentReferenceMap;
     * }
     */
}
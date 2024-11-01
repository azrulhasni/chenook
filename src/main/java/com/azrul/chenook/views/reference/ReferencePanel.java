package com.azrul.chenook.views.reference;

import java.util.Map;
import java.util.Set;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import com.azrul.chenook.domain.Reference;
import com.azrul.chenook.domain.ReferenceMap;
import com.azrul.chenook.service.ReferenceService;
import com.azrul.chenook.utils.WorkflowUtils;
import com.azrul.chenook.views.common.components.PageNav;
import com.azrul.chenook.views.common.components.SearchPanel;
import com.azrul.chenook.views.workflow.WorkflowAwareGroup;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.dom.Style.AlignItems;

public class ReferencePanel<R extends Reference, RS extends ReferenceService<R>> extends CustomField<Set<R>>{
    private final int COUNT_PER_PAGE = 3;
    private int maxSelection = 0;
    private String context;
    private String label;
    private Class<R> referenceClass;
    private Long parentId;

    private ReferenceService<R> refService;
    private WorkflowAwareGroup group;
    private Button btnSelectDialog;
    private ReferenceMap<R> currentReferenceMap;

  

    private ReferencePanel(
        final RS refService,
        final WorkflowAwareGroup group,
        final Class<R> referenceClass, 
        final Long parentId, 
        final OidcUser oidcUser, 
        final Integer maxSelection,
        final String label, 
        final String context
    ) {
        this.refService = refService;
        this.group = group;
        setMaxSelection(maxSelection);
        this.context = context;
        this.label = label;
        this.referenceClass = referenceClass;   
        this.parentId = parentId;
        this.setReadOnly(true);
        
        this.setLabel(label);
        if (maxSelection > 1) {
            Integer count = refService.countReferenceData(referenceClass, parentId);
            PageNav nav = new PageNav();

            DataProvider<R, Void> dataProvider = refService.getReferenceData(referenceClass, nav, parentId);
            Grid<R> grid = new Grid<>(referenceClass, false);
            grid.setItems(dataProvider);
            grid.setAllRowsVisible(true);

            Map<String, String> sortableFields = WorkflowUtils.getSortableFields(referenceClass);

            var fieldMap = WorkflowUtils.getFieldNameDisplayNameMap(referenceClass);
            for (var fieldEntry : fieldMap.entrySet()) {
                grid.addColumn(fieldEntry.getKey())
                        .setSortable(sortableFields.containsKey(fieldEntry.getKey()))
                        .setHeader(fieldEntry.getValue());
            }

            nav.init(grid, count, COUNT_PER_PAGE);
            grid.setHeight("calc(" + (COUNT_PER_PAGE + 1) + " * var(--lumo-size-l))");

            grid.setDataProvider(dataProvider);

            btnSelectDialog = new Button("Select", e -> {
                Dialog dialog = buildDialog(referenceClass, parentId, ()->{
                    Integer countSelected = refService.countReferenceData(referenceClass, parentId);
                    nav.refresh(countSelected);
                    grid.getDataProvider().refreshAll();
                    grid.setAllRowsVisible(true);
                });
                dialog.open();
            });

            this.add(nav);
            this.add(btnSelectDialog);
            this.add(grid);
        }else{
            ReferenceMap<R> refMap = refService.getMap(parentId, referenceClass);
            TextField textField = new TextField();
            
            textField.setReadOnly(true);
            btnSelectDialog = new Button("Select", e -> {
                Dialog dialog = buildDialog(referenceClass, parentId, ()->{
                    ReferenceMap<R> refMap2 = refService.getMap(parentId, referenceClass);
                    R ref = refMap2.getReferences().iterator().next();
                    textField.setValue(ref.toString());
                });
                dialog.open();
            });
            
            if (refMap.getReferences().size()>0){
                R ref = refMap.getReferences().iterator().next();
                textField.setValue(ref.toString());
            }
            HorizontalLayout refPanel = new HorizontalLayout();
            refPanel.setWidthFull();
            refPanel.setAlignItems(FlexComponent.Alignment.CENTER);
            refPanel.add(textField,btnSelectDialog);
            btnSelectDialog.getStyle().set("margin-left", "auto");  
            textField.setWidthFull();

            this.add(refPanel);
        }
    }

    public void applyGroup(){
        if (group!=null){
            this.setReadOnly(!group.calculateEnable());
            this.btnSelectDialog.setEnabled(group.calculateEnable());
            this.setVisible(group.calculateVisible());
            this.btnSelectDialog.setVisible(group.calculateVisible());
        }
    }

   // private void init() {
        
        //this.getStyle().set("border", "1px solid lightgrey");
        //this.getStyle().set("border-radius", "10px");
    //}

    public void setMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    @Override
    public String getLabel() {
        return label;
    }

    private Dialog buildDialog(
            Class<R> referenceClass,
            Long parentId,
            Runnable refresh) {
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

        ReferenceMap<R> refMap = refService.getMap(parentId, referenceClass);
        for (int i = 0; i < grid.getDataCommunicator().getItemCount(); i++) {
            R item = grid.getDataCommunicator().getItem(i);
            if (refMap.getReferences().contains(item)) {
                grid.getSelectionModel().select(item);
            }
        }

        

        grid.setDataProvider(dataProvider);
        if (maxSelection == 1) {
            grid.setSelectionMode(SelectionMode.SINGLE);
        } else {
            grid.setSelectionMode(SelectionMode.MULTI);
        }
        // grid.setHeight("calc("+COUNT_PER_PAGE+" * var(--lumo-size-m))");

        searchPanel.searchRunner(s ->{
            Integer count2 = refService.countAllReferenceData(referenceClass, searchPanel);
            nav.refresh(count2);
            dataProvider.refreshAll();
        });

        dialog.add(searchPanel, nav, grid);
        Button btnClose = new Button("Close", e -> dialog.close());
        Button btnSelect = new Button("Select", e -> {
            Set<R> selectedItems = grid.getSelectedItems();
            refService.saveMap(parentId, referenceClass, selectedItems, context);
            
            refresh.run();
            dialog.close();
        });
        dialog.getFooter().add(btnClose, btnSelect);
        return dialog;
    }

    public static <R extends Reference, RS extends ReferenceService<R>> ReferencePanel<R, RS> create(
            final Class<R> referenceClass,
            final RS refService,
            final WorkflowAwareGroup group,
            final Long parentId,
            final OidcUser oidcUser,
            final Integer maxSelection,
            final String label,
            final String context) {

        var refPanel = new ReferencePanel<R, RS>(
            refService,
            group,
            referenceClass, 
            parentId, 
            oidcUser, 
            maxSelection, 
            label, 
            context);
        refPanel.applyGroup();
        return refPanel;
    }

    @Override
    protected Set<R> generateModelValue() {
            return currentReferenceMap.getReferences();
    }

    @Override
    protected void setPresentationValue(Set<R> arg0) {
        
    }

    public ReferenceMap<R> getCurrentReferenceMap() {
        return currentReferenceMap;
    }

    public void setCurrentReferenceMap(ReferenceMap<R> currentReferenceMap) {
        this.currentReferenceMap = currentReferenceMap;
    }
}
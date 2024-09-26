/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.theme.lumo.LumoIcon;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author azrul
 */
public class PageNav extends HorizontalLayout {

    private Button firstPage;
    private Button finalPage;
    private Button nextPage;
    private Button previousPage;
    private Button sortBy;
    private NativeLabel currentPage;
    
    private Integer countPerPage;
    private Integer page = 1;
    private Integer totalPageCount;
    private Integer totalDataCount;
    private String sortField;
    private Boolean asc;
    private Grid grid;
   // private DataProvider dataProvider;
    private Map<String,String> sortableFields;

    public PageNav(){
    }
    
    public void init(
            Grid grid,
            Integer totalDataCount, 
            Integer countPerPage, 
            String sortField, 
            Map<String,String> sortableFields,
            Boolean asc) {
        //this.dataProvider = dataProvider;
        this.countPerPage = countPerPage;
        this.setPage((Integer) 1);
        this.totalDataCount=totalDataCount;
        this.grid = grid;
        this.sortableFields = sortableFields;
        this.sortField = sortField; 
        this.asc=asc;
        this.setPadding(true);
        this.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        
       
        String uniqueDisc = "PAGE_NAV";
        //this.setSpacing(false);
        firstPage = new Button();
        firstPage.setIcon(VaadinIcon.FAST_BACKWARD.create());
        firstPage.setId("btnFirstPage-"+uniqueDisc);
        //firstPage.addThemeVariants(ButtonVariant.LUMO_SMALL);
        finalPage = new Button();
        finalPage.setIcon(VaadinIcon.FAST_FORWARD.create());
        finalPage.setId("btnFinalStage-"+uniqueDisc);
        //finalPage.addThemeVariants(ButtonVariant.LUMO_SMALL);
        nextPage = new Button();
        nextPage.setIcon(VaadinIcon.CARET_RIGHT.create());
        nextPage.setId("btnNextPage-"+uniqueDisc);
        //nextPage.addThemeVariants(ButtonVariant.LUMO_SMALL);
        previousPage = new Button();
        previousPage.setIcon(VaadinIcon.CARET_LEFT.create());
        previousPage.setId("btnLastPage-"+uniqueDisc);
        //previousPage.addThemeVariants(ButtonVariant.LUMO_SMALL);
        currentPage = new NativeLabel();
        sortBy = new Button();
        sortBy.setIcon(LumoIcon.ORDERED_LIST.create());
        //sortBy.addThemeVariants(ButtonVariant.LUMO_SMALL);
        ContextMenu sortMenu = new ContextMenu();
        
        sortMenu.setTarget(sortBy);
        sortMenu.setOpenOnClick(true);
       
        
        List<TextField> sortLabels = new ArrayList<>(); 
        for (var sortItem:sortableFields.entrySet()){
            TextField label = new TextField();
            label.setValue(sortItem.getValue());
            label.setReadOnly(true);
            sortLabels.add(label);
            MenuItem mi = sortMenu.addItem(label,e->{
                this.sortField = sortItem.getKey();
                //reset
                for (var l:sortLabels){
                    l.setSuffixComponent(new Div());
                }
                //depending on asc or desc - change icon
                if (this.asc){
                    this.asc=false;
                    label.setSuffixComponent(VaadinIcon.ANGLE_DOWN.create());
                }else{
                    this.asc=true;
                    label.setSuffixComponent(VaadinIcon.ANGLE_UP.create());
                }
                grid.getDataProvider().refreshAll();//this will use sortField and asc
            });
            
        }
        
        if (totalDataCount==null || totalDataCount<=0){
            return;
        }
        
       
        //currentPage.getStyle().set("font-size","12px");
        currentPage.getStyle().set("line-height", "2");
        add(firstPage);
        add(previousPage);
        add(currentPage);
        currentPage.setText("0");
        add(nextPage);
        add(finalPage);
        add(sortBy);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
//        getThemeList().removeAll(List.of("spacing-xs", "spacing-s", "spacing", "spacing-l", "spacing-xl"));
//        getThemeList().add("spacing-xs");
        
        
        
        this.totalPageCount = (int)Math.ceil((double)totalDataCount/this.getCountPerPage());
        calculateEnable();
        this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
        
        this.getFirstPage().addClickListener(e -> {
            this.setPage((Integer) 1);
            calculateEnable();
            this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
            this.grid.getDataProvider().refreshAll();
        });
        this.getFinalPage().addClickListener(e -> {
            if (this.page < this.totalPageCount) {
                this.setPage(this.totalPageCount);
                calculateEnable();
                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
                this.grid.getDataProvider().refreshAll();
            }
        });
        this.getNextPage().addClickListener(e -> {
            if (this.page < this.totalPageCount) {
                this.page++;
                calculateEnable();
                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
                this.grid.getDataProvider().refreshAll();
            }
        });
        this.getPreviousPage().addClickListener(e -> {
            if (this.page > 1) {
                this.page--;
                calculateEnable();
                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
                this.grid.getDataProvider().refreshAll();
            }
        });
        
    }
    
    public Integer getPage(){
        return page;
    }
    
    public Integer getTotalPageCount(){
        return totalPageCount;
    }
    
    public Integer getMaxCountPerPage(){
        return getCountPerPage();
    }
    
    public Integer getDataCountPerPage(){
        if (getTotalDataCount()<getCountPerPage()){ 
                            return getTotalDataCount(); 
                        } else {
                            if (page.equals(totalPageCount)){
                                return getTotalDataCount()-((totalPageCount-1)*getCountPerPage());
                            }else{
                                return getCountPerPage();
                            }
                        }
    }
    
    
    
    
    public void refresh(Integer totalDataCount) {
        
        this.totalDataCount=totalDataCount;
        this.totalPageCount = (int)Math.ceil((double)totalDataCount/getCountPerPage());
        if (this.page>this.totalPageCount){
            if (this.totalPageCount>0){
                this.setPage(this.totalPageCount);
            }else{
                this.setPage(1);
            }
        }
        calculateEnable();
        this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
        if (totalDataCount==0){
            this.setVisible(false);
        }
    }

//    public void initPageNav(Integer totalDataCount, DataProvider dataProvider, Integer countPerPage) {
//        this.dp = dataProvider;
//        this.countPerPage = countPerPage;
//        this.setPage((Integer) 1);
//        this.totalDataCount=totalDataCount;
//        this.totalPageCount = (int)Math.ceil((double)totalDataCount/this.getCountPerPage());
//        calculateEnable();
//        this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
//        
//        this.getFirstPage().addClickListener(e -> {
//            this.setPage((Integer) 1);
//            calculateEnable();
//            this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
//            dp.refreshAll();
//        });
//        this.getFinalPage().addClickListener(e -> {
//            if (this.page < this.totalPageCount) {
//                this.setPage(this.totalPageCount);
//                calculateEnable();
//                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
//                dp.refreshAll();
//            }
//        });
//        this.getNextPage().addClickListener(e -> {
//            if (this.page < this.totalPageCount) {
//                this.page++;
//                calculateEnable();
//                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
//                dp.refreshAll();
//            }
//        });
//        this.getPreviousPage().addClickListener(e -> {
//            if (this.page > 1) {
//                this.page--;
//                calculateEnable();
//                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
//                dp.refreshAll();
//            }
//        });
//    }

    private void calculateEnable() {
        if (page == 1) {
            this.getFinalPage().setEnabled(true);
            this.getFirstPage().setEnabled(false);
            this.getNextPage().setEnabled(true);
            this.getPreviousPage().setEnabled(false);
        } else if (page == totalPageCount) {
            this.getFinalPage().setEnabled(false);
            this.getFirstPage().setEnabled(true);
            this.getNextPage().setEnabled(false);
            this.getPreviousPage().setEnabled(true);
        } else {
            this.getFinalPage().setEnabled(true);
            this.getFirstPage().setEnabled(true);
            this.getNextPage().setEnabled(true);
            this.getPreviousPage().setEnabled(true);
        }
    }

    /**
     * @return the firstPage
     */
    public Button getFirstPage() {
        return firstPage;
    }

    /**
     * @return the finalPage
     */
    public Button getFinalPage() {
        return finalPage;
    }

    /**
     * @return the nextPage
     */
    public Button getNextPage() {
        return nextPage;
    }

    /**
     * @return the previousPage
     */
    public Button getPreviousPage() {
        return previousPage;
    }

    /**
     * @param page the page to set
     */
    private void setPage(Integer page) {
        this.page = page;
        
    }
    
    public void jumpToPage(Integer page) {
        setPage(page);
         calculateEnable();
                this.currentPage.setText(Integer.toString(page) + "/" + Integer.toString(totalPageCount));
                grid.getDataProvider().refreshAll();
        
    }

    /**
     * @return the sortField
     */
    public String getSortField() {
        return sortField;
    }

    /**
     * @param sortField the sortField to set
     */
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    /**
     * @return the asc
     */
    public Boolean getAsc() {
        return asc;
    }

    /**
     * @param asc the asc to set
     */
    public void setAsc(Boolean asc) {
        this.asc = asc;
    }

    /**
     * @return the countPerPage
     */
    public Integer getCountPerPage() {
        return countPerPage;
    }

    /**
     * @return the totalDataCount
     */
    public Integer getTotalDataCount() {
        return totalDataCount;
    }

 

}

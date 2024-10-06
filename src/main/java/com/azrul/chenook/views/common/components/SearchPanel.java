/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.azrul.chenook.views.workflow.SearchTermProvider;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;
import java.util.function.Consumer;

/**
 *
 * @author azrul
 */
public class SearchPanel extends HorizontalLayout implements SearchTermProvider,HasValue<AbstractField.ComponentValueChangeEvent<TextField,String>,String> {
    private final TextField searchField;
    private final Button searchBtn;
    
    public SearchPanel(){
        searchField = new TextField();
        this.add(searchField);
        searchBtn = new Button();
        searchBtn.setText("");
        searchBtn.setIcon(VaadinIcon.SEARCH.create());
        this.add(searchBtn);
    }
    
    @Override
    public String getValue(){
        return searchField.getValue();
    }
    
    @Override
    public void setValue(String value){
        searchField.setValue(value);
    }
    
    public void searchRunner(Consumer<String> runSearch){
        String searchTerms = searchField.getValue();
        
        searchBtn.addClickListener(e->runSearch.accept(searchTerms));
        searchField.addKeyPressListener(e->{
            if (e.getKey().equals(Key.ENTER)){
                runSearch.accept(searchTerms);
            }
        });
    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<TextField, String>> vl) {
        return searchField.addValueChangeListener(vl);
    }

    @Override
    public void setReadOnly(boolean bln) {
        searchBtn.setEnabled(!bln);
        searchField.setReadOnly(bln);
    }

    @Override
    public boolean isReadOnly() {
        return searchField.isReadOnly();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean bln) {
        searchField.setRequiredIndicatorVisible(bln);
     }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return searchField.isRequiredIndicatorVisible();
    }

    @Override
    public String getSearchTerm() {
        return getValue();
   }
}

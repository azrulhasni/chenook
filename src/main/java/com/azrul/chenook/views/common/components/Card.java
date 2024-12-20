/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.common.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 *
 * @author azrul
 */
public class Card extends Composite<Div> implements HasComponents, HasSize {
        Div div = new Div();
        VerticalLayout content = new VerticalLayout();
        H5 title = new H5();
        String titleText = "";
        Component titleComponent = null;
        int MAX_TITLE_LENGTH = 30;
        

        public Card(String titleText, Component titleComponent) {
            this.titleText = titleText;
            this.titleComponent=titleComponent;
        }
        
         public Card(String titleText) {
            this.titleText = titleText;
        }

        @Override
        public Div initContent() {
            if (titleText!=null){
                title.setText(
                         titleText.length()>MAX_TITLE_LENGTH
                        ?titleText.substring(0,MAX_TITLE_LENGTH)+"..."
                        :titleText);
            }
            Div status = new Div();
            if (titleComponent!=null){
                
                status.add(titleComponent);
                status.addClassNames(LumoUtility.Background.CONTRAST_5,
                    LumoUtility.TextColor.PRIMARY, LumoUtility.Padding.SMALL,
                    LumoUtility.Border.BOTTOM,
                    LumoUtility.BorderColor.CONTRAST_10);
            }
            title.addClassNames(LumoUtility.Background.CONTRAST_5,
                    LumoUtility.TextColor.PRIMARY, LumoUtility.Padding.SMALL,
                    //LumoUtility.Border.BOTTOM,
                    LumoUtility.BorderColor.CONTRAST_10);
            div.addClassNames(LumoUtility.Display.FLEX,
                    LumoUtility.FlexDirection.COLUMN, LumoUtility.Border.ALL,
                    LumoUtility.BorderColor.CONTRAST_10,
                    LumoUtility.BorderRadius.SMALL,
                    LumoUtility.BoxShadow.SMALL);
//            content.addClassNames(LumoUtility.Flex.GROW,
//                    LumoUtility.Padding.SMALL);
            content.setSpacing(false);
            content.setPadding(true);
            div.add(title,status, content);
            return div;
        }

        @Override
        public void add(Component... components) {
            content.add(components);
        }

        @Override
        public void remove(Component... components) {
            content.remove(components);
        }
    
    
}

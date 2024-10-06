/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.azrul.chenook.views.users;

import com.azrul.chenook.domain.BizUser;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 *
 * @author azrul
 */
public class UserProfileDialog extends Dialog{
    public UserProfileDialog(BizUser user){
         UserProfile up = new UserProfile(user);
                this.setWidth("50%");
                this.setHeaderTitle("User profile");
                //up.getStyle().set("width", "fit-content");
                this.add(up);
                Button btnCloseProfile = new Button("Close");
                btnCloseProfile.addClickListener(e -> this.close());
                this.getFooter().add(btnCloseProfile);
                
    }
}

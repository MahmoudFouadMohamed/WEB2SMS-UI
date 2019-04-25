package com.edafa.web2sms.ui.locale;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "userBean")
@ViewScoped
public class UserManagementTempBean {
    private boolean showUserManagementTabs=true;

    public boolean isShowUserManagementTabs() {
        return showUserManagementTabs;
    }

    public void setShowUserManagementTabs(boolean showUserManagementTabs) {
        this.showUserManagementTabs = showUserManagementTabs;
    }
    
    
}

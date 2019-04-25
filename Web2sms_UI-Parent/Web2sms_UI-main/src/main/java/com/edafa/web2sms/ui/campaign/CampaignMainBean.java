package com.edafa.web2sms.ui.campaign;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;

@ViewScoped
@ManagedBean(name = "campaignMainBean")
public class CampaignMainBean {

	private boolean activeEditFlag = true;
	private boolean createFlag;
	private boolean historyFlag;

	// Setters and getters------------------------------

	public boolean isActiveEditFlag() {
		return activeEditFlag;
	}
	public void setActiveEditFlag(boolean activeEditFlag) {
		this.activeEditFlag = activeEditFlag;
	}
	public boolean isCreateFlag() {
		return createFlag;
	}
	public void setCreateFlag(boolean createFlag) {
		this.createFlag = createFlag;
	}
	public boolean isHistoryFlag() {
		return historyFlag;
	}
	public void setHistoryFlag(boolean historyFlag) {
		this.historyFlag = historyFlag;
	}

	// Methods---------------------------------------------

	public void createListener(AjaxBehaviorEvent event) {
		activeEditFlag = false;
		createFlag = true;
		historyFlag = false;
	}

	public void historyListner(AjaxBehaviorEvent event) {
		activeEditFlag = false;
		createFlag = false;
		historyFlag = true;
	}

}

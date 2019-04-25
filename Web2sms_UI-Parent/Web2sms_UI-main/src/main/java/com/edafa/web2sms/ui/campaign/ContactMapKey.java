package com.edafa.web2sms.ui.campaign;

public class ContactMapKey {

	private String listId;
	private String msisdn;

	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getMsisdn() {
		return msisdn;
	}
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public ContactMapKey() {

	}

	public ContactMapKey(String listId, String msisdn) {
		this.listId = listId;
		this.msisdn = msisdn;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ContactMapKey)) {
			return false;
		}
		ContactMapKey other = (ContactMapKey) object;
		if ((this.listId.equals(other.listId)) && this.msisdn.equals(other.msisdn)) {
			return true;
		}
		return false;
	}

}

package com.edafa.web2sms.ui.locale;

import com.edafa.web2sms.service.acc_manag.model.AccountFullInfo;
import com.edafa.web2sms.service.model.User;

public class UserAccount extends User {

	private AccountFullInfo account;

	public AccountFullInfo getAccount() {
		return account;
	}

	public void setAccount(AccountFullInfo account) {
		this.account = account;
	}

}

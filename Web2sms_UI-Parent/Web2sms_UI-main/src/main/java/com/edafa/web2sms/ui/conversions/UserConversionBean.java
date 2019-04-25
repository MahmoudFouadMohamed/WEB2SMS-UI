package com.edafa.web2sms.ui.conversions;

import javax.ejb.Stateless;

import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.model.User;

@Stateless
public class UserConversionBean {
	
	public AccManagUser getAccManagUser(User user) {
		
		AccManagUser accUser = new AccManagUser();
		accUser.setAccountId(user.getAccountId());
		accUser.setUsername(user.getUsername());
		accUser.setPhoneNumber(user.getPhoneNumber());
		accUser.setPhoneNumber(user.getEmail());
		if (user.getUserActions() != null && !user.getUserActions().isEmpty())
			accUser.getUserActions().addAll(user.getUserActions());
		return accUser;
		
	}
	
	public User geUserModel(AccManagUser user) {
		
		User userModel = new User();
		userModel.setAccountId(user.getAccountId());
		userModel.setUsername(user.getUsername());
		userModel.setPhoneNumber(user.getPhoneNumber());
		userModel.setPhoneNumber(user.getEmail());
		if (user.getUserActions() != null && !user.getUserActions().isEmpty())
			userModel.getUserActions().addAll(user.getUserActions());
		return userModel;
		
	}
	
}

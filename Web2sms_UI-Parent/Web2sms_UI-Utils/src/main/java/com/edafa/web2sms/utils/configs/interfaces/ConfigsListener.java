package com.edafa.web2sms.utils.configs.interfaces;

import javax.ejb.Local;

import com.edafa.web2sms.utils.configs.enums.ModulesEnum;

@Local
public interface ConfigsListener {
	void configurationRefreshed(ModulesEnum module);

	void configurationRefreshed();
}

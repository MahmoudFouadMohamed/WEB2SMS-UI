package com.edafa.web2sms.utils.configs.interfaces;

import java.util.List;

import javax.ejb.Local;

import com.edafa.web2sms.utils.configs.enums.ModulesEnum;
import com.edafa.web2sms.utils.configs.exception.FailedToReadConfigsException;
import com.edafa.web2sms.utils.configs.exception.FailedToSaveConfigsException;
import com.edafa.web2sms.utils.configs.exception.InvalidConfigsException;
import com.edafa.web2sms.utils.configs.model.ModuleConfigs;

@Local
public interface ConfigsManagerBeanLocal {

	void registerConfigsListener(ModulesEnum module, ConfigsListener listener);

	void registerConfigsListener(ConfigsListener listener);

	boolean isConfigurationLoaded(ModulesEnum module);

	void refreshModuleConfigs(String module) throws FailedToReadConfigsException, InvalidConfigsException;

	void refreshModuleConfigs(ModulesEnum module) throws FailedToReadConfigsException, InvalidConfigsException;

	void refreshAllModuleConfigs() throws FailedToReadConfigsException, InvalidConfigsException;

	public ModuleConfigs readModuleConfigs(String module) throws FailedToReadConfigsException;

	public ModuleConfigs readModuleConfigs(ModulesEnum module) throws FailedToReadConfigsException;

	public List<ModuleConfigs> readConfigs() throws FailedToReadConfigsException;

	void saveConfigs(ModuleConfigs moduleConfigs) throws FailedToSaveConfigsException, InvalidConfigsException;

	void refreshModuleConfigs(ModuleConfigs moduleConfigs) throws InvalidConfigsException;

}

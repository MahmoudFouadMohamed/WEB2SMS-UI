package com.edafa.web2sms.utils.configs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.ws.BindingProvider;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.edafa.service.config.model.ConfigResultSet;
import com.edafa.web2sms.service.config.ConfigsManagementService;
import com.edafa.web2sms.service.config.ConfigsManagementService_Service;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.ModulesEnum;
import com.edafa.web2sms.utils.configs.exception.FailedToReadConfigsException;
import com.edafa.web2sms.utils.configs.exception.FailedToSaveConfigsException;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsSource;
import com.edafa.web2sms.utils.configs.model.Config;

/**
 * Session Bean implementation class DefaultConfigsSource
 */
@Stateless
@LocalBean
public class DefaultConfigsSource implements ConfigsSource {
	
	@Resource(name = "java:app/env/baseconfigdir")
	String baseConfigDir;
	
	private Ini iniConfigs;
	
	public DefaultConfigsSource() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<Config> readConfigs(ModulesEnum module) throws FailedToReadConfigsException {
		try {
			ensureOpenIni();
		} catch (InvalidFileFormatException e) {
			throw new FailedToReadConfigsException(e);
		} catch (IOException e) {
			throw new FailedToReadConfigsException(e);
		}
		
		List<Config> configs;
		Map<String, String> configsMap = iniConfigs.get(module.getName());
		if (configsMap != null) {
			configs = getConfigsModel(configsMap, module);
		} else {
			configs = new ArrayList<>();
		}
		return configs;
	}
	
	@Override
	public void saveConfigs(ModulesEnum module, List<Config> configs) throws FailedToSaveConfigsException {
		try {
			ensureOpenIni();
		} catch (InvalidFileFormatException e) {
			throw new FailedToSaveConfigsException(e);
		} catch (IOException e) {
			throw new FailedToSaveConfigsException(e);
		}
		
		for (Config config : configs) {
			iniConfigs.put(module.getName(), config.getKey(), config.getValue());
		}
		try {
			iniConfigs.store();
		} catch (IOException e) {
			throw new FailedToSaveConfigsException(e);
		}
	}
	
	private void ensureOpenIni() throws InvalidFileFormatException, IOException {
		if (iniConfigs == null)
			iniConfigs = new Ini(new File(baseConfigDir + "configs.ini"));
	}
	
	public List<Config> getConfigsModel(Map<String, String> configsMap, ModulesEnum module) {
		List<Config> configs = new ArrayList<Config>();
		for (Iterator<Entry<String, String>> it = configsMap.entrySet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			configs.add(getConfigModel(entry.getKey(), entry.getValue(), module));
		}
		return configs;
	}
	
	public Config getConfigModel(String key, String value, ModulesEnum module) {
		Config config = new Config();
		config.setId(0);
		config.setKey(key);
		config.setValue(value);
		config.setModule(module.getName());
		
		try {
			Configs enumConfig = Configs.getConfig(ModulesEnum.valueOf(module.getName()), key);
			if (enumConfig != null) {
				config.setConfigType(enumConfig.getType());
				config.setRunningConfig(enumConfig.getValue().toString());
			}
		} catch (Exception e) {
		}
		return config;
	}
	
	@Override
	public List<Config> readConfigsRemotly(ModulesEnum module) throws FailedToReadConfigsException {
		List<Config> configs = null;
		
		try {
			ConfigsManagementService_Service configsManagementService = new ConfigsManagementService_Service();
			String configsManagementURL = (String) Configs.UIConfigsServiceURL.getValue();
			ConfigsManagementService configsManagementServicePorts = configsManagementService.getConfigsManagementServicePort();
			int requestTimeout = (int) Configs.CONFIG_WS_REQUEST_TIMEOUT.getValue();
			int connectTimeout = (int) Configs.CONFIG_WS_CONNECT_TIMEOUT.getValue();
			((BindingProvider) configsManagementServicePorts).getRequestContext().put("com.sun.xml.ws.request.timeout", requestTimeout);
			((BindingProvider) configsManagementServicePorts).getRequestContext().put("com.sun.xml.ws.connect.timeout", connectTimeout);
			((BindingProvider) configsManagementServicePorts).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, configsManagementURL);
			
			ConfigResultSet result = configsManagementServicePorts.readConfigsModule(module.getName());
			ResponseStatus status = result.getStatus();
			switch (status) {
				case SUCCESS:
					configs = getConfigsModel(result.getConfig());
					break;
				
				default:
					break;
			}
		} catch (Exception ex) {
			throw new FailedToReadConfigsException();
		}
		return configs;
	}
	
	private List<Config> getConfigsModel(List<com.edafa.ws.utils.configs.model.Config> configurations) {
		List<Config> mappedConfigs = new ArrayList<Config>();
		for (com.edafa.ws.utils.configs.model.Config conf : configurations) {
			mappedConfigs.add(getConfigModel(conf));
		}
		return mappedConfigs;
	}
	
	private Config getConfigModel(com.edafa.ws.utils.configs.model.Config conf) {
		Config config = new Config();
		config.setId(conf.getId());
		config.setKey(conf.getKey());
		config.setValue(conf.getValue());
		config.setDescription(conf.getDescription());
		config.setModule(conf.getModule());
		
		try {
			Configs enumConfig = Configs.getConfig(ModulesEnum.valueOf(conf.getModule()), conf.getKey());
			if (enumConfig != null) {
				config.setConfigType(enumConfig.getType());
				config.setRunningConfig(enumConfig.getValue().toString());
			}
		} catch (Exception e) {
		}
		return config;
	}
	
	
	private List<com.edafa.ws.utils.configs.model.Config> getConfigsFromModel(List<Config> configurations) {
		List<com.edafa.ws.utils.configs.model.Config> mappedConfigs = new ArrayList<com.edafa.ws.utils.configs.model.Config>();
		for (Config conf : configurations) {
			mappedConfigs.add(getConfigFromModel(conf));
		}
		return mappedConfigs;
	}
	
	private com.edafa.ws.utils.configs.model.Config getConfigFromModel(Config conf) {
		com.edafa.ws.utils.configs.model.Config config = new com.edafa.ws.utils.configs.model.Config();
		config.setId(conf.getId());
		config.setKey(conf.getKey());
		config.setValue(conf.getValue());
		config.setDescription(conf.getDescription());
		config.setModule(conf.getModule());
		
		try {
			Configs enumConfig = Configs.getConfig(ModulesEnum.valueOf(conf.getModule()), conf.getKey());
			if (enumConfig != null) {
//				config.setConfigType(enumConfig.getType());
				config.setRunningConfig(enumConfig.getValue().toString());
			}
		} catch (Exception e) {
		}
		return config;
	}
	
	@Override
	public void saveConfigsRemotly(ModulesEnum module, List<Config> configs) throws FailedToSaveConfigsException {
		
		try {
			ConfigsManagementService_Service configsManagementService = new ConfigsManagementService_Service();
			String configsManagementURL = (String) Configs.UIConfigsServiceURL.getValue();
			ConfigsManagementService configsManagementServicePorts = configsManagementService.getConfigsManagementServicePort();
			int requestTimeout = (int) Configs.CONFIG_WS_REQUEST_TIMEOUT.getValue();
			int connectTimeout = (int) Configs.CONFIG_WS_CONNECT_TIMEOUT.getValue();
			((BindingProvider) configsManagementServicePorts).getRequestContext().put("com.sun.xml.ws.request.timeout", requestTimeout);
			((BindingProvider) configsManagementServicePorts).getRequestContext().put("com.sun.xml.ws.connect.timeout", connectTimeout);
			((BindingProvider) configsManagementServicePorts).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, configsManagementURL);
			List<com.edafa.ws.utils.configs.model.Config> configs_ws = getConfigsFromModel(configs);
			
			ResultStatus result = configsManagementServicePorts.saveModuleConfigs(module.getName(), configs_ws);
			
		} catch (Exception ex) {
			throw new FailedToSaveConfigsException(ex);
		}
	}
	
}

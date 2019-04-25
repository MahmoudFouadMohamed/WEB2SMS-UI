package com.edafa.web2sms.utils.configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.utils.configs.enums.AppSettings;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.configs.enums.ModulesEnum;
import com.edafa.web2sms.utils.configs.exception.FailedToReadConfigsException;
import com.edafa.web2sms.utils.configs.exception.FailedToSaveConfigsException;
import com.edafa.web2sms.utils.configs.exception.InvalidConfigsException;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsListener;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsManagerBeanLocal;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsSource;
import com.edafa.web2sms.utils.configs.model.Config;
import com.edafa.web2sms.utils.configs.model.ModuleConfigs;

/**
 * Session Bean implementation class ConfigsManagerBean
 */
@Singleton
@LocalBean
@Startup
public class ConfigsManagerBean implements ConfigsManagerBeanLocal {
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
	java.util.logging.Logger logger = java.util.logging.Logger.getAnonymousLogger();

	@Resource(name = "java:app/env/basedir")
	String baseDir;

	@EJB
	ConfigsSource defaultConfigsSource;

	private Map<ModulesEnum, List<ConfigsListener>> configsListenersMap;
	private List<ConfigsListener> configsListenersList;
	private Map<ModulesEnum, Boolean> configuratoinLoadedFlags;

	// @EJB
	// ConfigDaoLocal configDao;

	public ConfigsManagerBean() {
		configuratoinLoadedFlags = new HashMap<ModulesEnum, Boolean>();
		configsListenersMap = new HashMap<ModulesEnum, List<ConfigsListener>>();
		configsListenersList = new ArrayList<ConfigsListener>();
		logger.setLevel(Level.ALL);
	}

	@PostConstruct
	void init() {
		AppSettings.BaseDir.setEnvEntryValue(baseDir);
		appLogger.info("Will load UIConfigs configurations");
		logger.info("Will load UIConfigs configurations");
		try {
			ModuleConfigs moduleConfigs = readModuleConfigs(ModulesEnum.UIConfigs);
			applyConfigs(moduleConfigs);
			setConfiguratoinLoaded(ModulesEnum.UIConfigs, true);
		} catch (FailedToReadConfigsException e) {
			appLogger.error("Cannot retrieve UIConfigs configuration", e);
			logger.log(Level.SEVERE, "Cannot UIConfigs configuration: ", e);
			configuratoinLoadedFlags.put(ModulesEnum.UIConfigs, false);
		} catch (InvalidConfigsException e) {
			appLogger.error("Invalid UIConfigs configs: " + e.getMessage());
			logger.log(Level.SEVERE, "Invalid UIConfigs configs: " + e.getMessage());
			setConfiguratoinLoaded(ModulesEnum.UIConfigs, false);
		}
		if(isConfigurationLoaded(ModulesEnum.UIConfigs)){
		appLogger.info("Will load modules configurations");
		logger.info("Will load modules configurations");
		for (Iterator<ModulesEnum> it = Configs.getModules().iterator(); it.hasNext();) {
			ModulesEnum module = it.next();
			appLogger.info("Will load " + module.getName() + " configurations");

			logger.info("Will load " + module.getName() + " configurations");
			try {
				ModuleConfigs moduleConfigs = readModuleConfigs(module);
				applyConfigs(moduleConfigs);
				setConfiguratoinLoaded(module, true);
			} catch (FailedToReadConfigsException e) {
				appLogger.error("Cannot retrieve configuration", e);
				logger.log(Level.SEVERE, "Cannot retrieve configuration: ", e);
				configuratoinLoadedFlags.put(module, false);
			} catch (InvalidConfigsException e) {
				appLogger.error("Invalid configs: " + e.getMessage());
				logger.log(Level.SEVERE, "Invalid configs: " + e.getMessage());
				setConfiguratoinLoaded(module, false);
			}
		}
		} else {
			for (Iterator<ModulesEnum> it = Configs.getModules().iterator(); it.hasNext();) {
				ModulesEnum module = it.next();
				setConfiguratoinLoaded(module, false);
			}
			appLogger.error("UIConfigs isn't loaded, Cannot retrieve other modules configuration");
			logger.log(Level.SEVERE, "Cannot retrieve configuration: ");
		}
 }

	@Override
	@Lock(LockType.WRITE)
	public void registerConfigsListener(ModulesEnum module, ConfigsListener listener) {
		appLogger.info("Registering new ConfigsListener " + listener + " for module " + module.name());
		logger.info("Registering new ConfigsListener " + listener + " for module " + module.name());
		List<ConfigsListener> configListeners = configsListenersMap.get(module);
		if (configListeners == null) {
			configsListenersMap.put(module, new ArrayList<ConfigsListener>());
		}
		configsListenersMap.get(module).add(listener);
	}

	@Override
	@Lock(LockType.WRITE)
	public void registerConfigsListener(ConfigsListener listener) {
		appLogger.info("Registering new ConfigsListener " + listener + " for all modules ");
		logger.info("Registering new ConfigsListener " + listener + " for all modules ");
		configsListenersList.add(listener);
	}

	@Override
	public boolean isConfigurationLoaded(ModulesEnum module) {
		Boolean result1 = configuratoinLoadedFlags.get(ModulesEnum.UIConfigs);
		Boolean result2 = configuratoinLoadedFlags.get(module);
		return result1 != null && result2 != null ? (result1 && result2) : false;
	}

	private void setConfiguratoinLoaded(ModulesEnum module, boolean configuratoinLoaded) {
		this.configuratoinLoadedFlags.put(module, configuratoinLoaded);
	}

	@Override
	public ModuleConfigs readModuleConfigs(String module) throws FailedToReadConfigsException {
		ModulesEnum moduleEnum;
		try {
			moduleEnum = ModulesEnum.valueOf(module);
		} catch (Exception e) {
			throw new FailedToReadConfigsException("No such module");
		}
		return readModuleConfigs(moduleEnum);
	}

	@Override
	public ModuleConfigs readModuleConfigs(ModulesEnum module) throws FailedToReadConfigsException {
		appLogger.info("Reading configs for module " + module);
		if (module == null) {
			throw new FailedToReadConfigsException("No such application module");
		}

		try {
			module = ModulesEnum.valueOf(module.name());
		} catch (Exception e) {
			throw new FailedToReadConfigsException("No such application module");
		}
		appLogger.info("Reading configuratoin for module [" + module.getName() + "]");
		ModuleConfigs moduleConfigs = new ModuleConfigs();
		moduleConfigs.setModule(module.name());
		moduleConfigs.setConfigsApplied(module.isConfigsApplied());
		try {
			if (module.equals(ModulesEnum.UIConfigs)) {
				List<Config> configs = defaultConfigsSource.readConfigs(module);
				moduleConfigs.setConfigList(configs);
			}else {
				List<Config> configs = defaultConfigsSource.readConfigsRemotly(module);
				moduleConfigs.setConfigList(configs);
			}
		} catch (Exception e) {
			appLogger.error("Failed to read configuration for moudle: " + module.name(), e);
			throw new FailedToReadConfigsException(e);
		}
		return moduleConfigs;
	}

	@Override
	public List<ModuleConfigs> readConfigs() throws FailedToReadConfigsException {
		List<ModuleConfigs> modulesConfigs = new ArrayList<>();
		try {
			appLogger.info("Reading configuratoin for all modules");
			for (ModulesEnum module : ModulesEnum.values()) {
				modulesConfigs.add(readModuleConfigs(module));
			}
		} catch (Exception e) {
			appLogger.error("Failed to read configuration ", e);
			throw new FailedToReadConfigsException(e);
		}

		return modulesConfigs;
	}

	public void validateModuleConfigs(ModuleConfigs moduleConfigs) throws InvalidConfigsException {
		// if (!moduleConfigs.isValid()) {
		// appLogger.error("Invalid configs for module: " + moduleConfigs);
		// throw new InvalidConfigsException("Invalid request");
		// }
		ModulesEnum module = ModulesEnum.valueOf(moduleConfigs.getModule());
		List<Config> configs = moduleConfigs.getConfigList();
		List<Config> invalidConfigs = new ArrayList<Config>();
		Collection<Configs> moduleEnumConfigs = Configs.getModuleConfigs(module);
		Map<String, Config> configsMap = new HashMap<>();

		for (Config conf : configs) {
			configsMap.put(conf.getKey(), conf);
		}

		// Validate Configuration
		for (Iterator<Configs> iterator = moduleEnumConfigs.iterator(); iterator.hasNext();) {
			Configs configEnum = iterator.next();
			Config conf = configsMap.get(configEnum.getProperty());
			// Check if the configuration exist in the DB
			if (conf == null) {
				if (!configEnum.isHasDefault()) {
					appLogger.error("No default value for property: " + configEnum.getProperty());
					logger.log(Level.SEVERE, "No default value for property: " + configEnum.getProperty());
					invalidConfigs.add(conf);
				}

			} else if (!configEnum.isValidValue(conf.getValue())) {
				invalidConfigs.add(conf);
				appLogger.error("Invalid value for property: " + configEnum.getProperty());
				logger.log(Level.SEVERE, "Invalid value for property: " + configEnum.getProperty());
				break;
			}
		}

		if (!invalidConfigs.isEmpty()) {
			throw new InvalidConfigsException(module.name(), invalidConfigs);
		}

	}

	@Override
	public void saveConfigs(ModuleConfigs moduleConfigs) throws FailedToSaveConfigsException, InvalidConfigsException {
		if (!moduleConfigs.isValid()) {
			appLogger.error("Invalid request to save configs: " + moduleConfigs);
			throw new FailedToSaveConfigsException("Invalid request");
		}
		appLogger.info("Saving configs for module " + moduleConfigs.getModule());
		appLogger.info("Saving configs for module " + moduleConfigs.getModule());
		appLogger.info("Validating the configs for");

		validateModuleConfigs(moduleConfigs);

		ModulesEnum module = ModulesEnum.valueOf(moduleConfigs.getModule());
		List<Config> configs = moduleConfigs.getConfigList();
		appLogger.debug("Set ConfigsApplied=" + false + " for module " + module.name());
		module.setConfigsApplied(false);
		try {
			if (module.equals(ModulesEnum.UIConfigs)) {
				appLogger.info("Persisting new configs");
				logger.info("Persisting new configs");
				defaultConfigsSource.saveConfigs(module, configs);
			} else {
				appLogger.info("Persisting new configs remotly");
				logger.info("Persisting new configs remotly");
				defaultConfigsSource.saveConfigsRemotly(module, configs);
			}
			appLogger.info("Configs saved successfully");
			logger.info("Configs saved successfully");
		} catch (Exception e) {
			appLogger.error("Failed to save configuration", e);
			throw e;
		}
	}

	// @Override
	public void applyConfigs(ModuleConfigs moduleConfigs) throws InvalidConfigsException {
		ModulesEnum module = ModulesEnum.valueOf(moduleConfigs.getModule());

		List<Config> configs = moduleConfigs.getConfigList();

		validateModuleConfigs(moduleConfigs);

		// Update the configuration
		for (Iterator<Config> iterator = configs.iterator(); iterator.hasNext();) {
			Config conf = iterator.next();
			Configs config = Configs.getConfig(module, conf.getKey());

			if (config == null) {
				continue;
			}
			try {
				switch (config.getType()) {
				case BOOLEAN:
					config.setValue(Boolean.parseBoolean(conf.getValue()));
					break;
				case INTEGER:
					config.setValue(Integer.parseInt(conf.getValue()));
					break;
				default:
					config.setValue(conf.getValue());
				}
				appLogger.info("Applied Configuration: Property(" + config.getProperty() + "), Value("
						+ config.getValue() + ")");
				logger.info("Applied Configuration: Property(" + config.getProperty() + "), Value(" + config.getValue()
						+ ")");
			} catch (Exception e) {
				appLogger.error("Invalid value for property: " + config.getProperty()
						+ ", Configuration will not be updated");
				logger.log(Level.SEVERE, "Invalid value for property: " + config.getProperty()
						+ ", Configuration will not be updated");
				setConfiguratoinLoaded(module, false);
			}
		}

	}

	@Override
	public void refreshAllModuleConfigs() throws FailedToReadConfigsException, InvalidConfigsException {
		logger.info("Refresh all modules configs");
		appLogger.info("Refresh all modules configs");
		for (Iterator<ModulesEnum> it = Configs.getModules().iterator(); it.hasNext();) {
			ModulesEnum module = it.next();

			refreshModuleConfigs(module);
		}
		notifyConfigsListenres();
	}

	@Override
	public void refreshModuleConfigs(String module) throws FailedToReadConfigsException, InvalidConfigsException {
		ModulesEnum moduleEnum;
		try {
			moduleEnum = ModulesEnum.valueOf(module);
		} catch (Exception e) {
			throw new FailedToReadConfigsException("No such module");
		}

		refreshModuleConfigs(moduleEnum);
	}

	@Override
	public void refreshModuleConfigs(ModulesEnum module) throws FailedToReadConfigsException, InvalidConfigsException {
		try {
			appLogger.info("Reading configuratoin for module [" + module.name() + "]");
			ModuleConfigs moduleConfigs = readModuleConfigs(module);
			applyConfigs(moduleConfigs);
			module.setConfigsApplied(true);
			notifyConfigsListenres(module);
		} catch (FailedToReadConfigsException e) {
			appLogger.error("Cannot read configuration", e);
			logger.log(Level.SEVERE, "Cannot read configuration ", e);
			setConfiguratoinLoaded(module, false);
			throw e;
		}
		notifyConfigsListenres();
	}

	@Override
	public void refreshModuleConfigs(ModuleConfigs moduleConfigs) throws InvalidConfigsException {
		ModulesEnum moduleEnum = ModulesEnum.valueOf(moduleConfigs.getModule());
		applyConfigs(moduleConfigs);
		notifyConfigsListenres(moduleEnum);
		notifyConfigsListenres();
	}

	private void notifyConfigsListenres(ModulesEnum module) {
		List<ConfigsListener> configListeners = this.configsListenersMap.get(module);
		if (configListeners != null) {
			for (ConfigsListener configListener : configListeners) {
				try {
					configListener.configurationRefreshed(module);
				} catch (Exception e) {
					appLogger.warn("Failure occured while notifying the configs listner (" + configListener
							+ ") with new configs in module (" + module.getName() + ")", e);
				}
			}
		}
	}

	private void notifyConfigsListenres() {
		for (ConfigsListener configListener : configsListenersList) {
			try {
				configListener.configurationRefreshed();
			} catch (Exception e) {
				appLogger.warn("Failure occured while notifying the configs listner (" + configListener
						+ ") with new configs ", e);
			}
		}
	}

}
package com.edafa.web2sms.utils.configs.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.edafa.web2sms.utils.configs.exception.ConfigValueNotSetException;
import com.edafa.web2sms.utils.configs.exception.InvalidConfigValue;

public enum Configs {
	
	UIConfigsServiceURL(ModulesEnum.UIConfigs, "UIConfigsServiceURL", ConfigType.STRING),
	CONFIG_WS_REQUEST_TIMEOUT(ModulesEnum.UIConfigs, "CONFIG_WS_REQUEST_TIMEOUT", 15000,ConfigType.INTEGER),
	CONFIG_WS_CONNECT_TIMEOUT(ModulesEnum.UIConfigs, "CONFIG_WS_CONNECT_TIMEOUT", 1000,ConfigType.INTEGER),
	LOG_LEVEL_UTILS(ModulesEnum.UIUtils, "LOG_LEVEL", "debug", ConfigType.LOG_LEVEL), 
	LOG_LAYOUT_INFO(ModulesEnum.UIUtils, "LOG_LAYOUT_INFO",
			"%d{yyyyMMdd HH:mm:ss.SSS z} | %p | %.30t | %C{1}-%M-%L | %m%n", ConfigType.STRING), 
	LOG_LAYOUT_DEBUG(ModulesEnum.UIUtils, "LOG_LAYOUT_DEBUG",
			"%d{yyyyMMdd HH:mm:ss.SSS z} | %p | %.30t | %C{1}-%M-%L | %m%n", ConfigType.STRING), LOG_TIMESTAMP_FORMAT(ModulesEnum.UIUtils,
			"LOG_TIMESTAMP_FORMAT", "yyyyMMdd HH:mm:ss.SSS z", ConfigType.STRING), LOG_TIME_FORMAT(ModulesEnum.UIUtils, "LOG_TIME_FORMAT",
			"HH:mm:ss.SSS z", ConfigType.STRING), LOG_DATE_FORMAT(ModulesEnum.UIUtils, "LOG_DATE_FORMAT", "yyyyMMdd", ConfigType.STRING), WS_REQUEST_TIMEOUT(
			ModulesEnum.UIUtils, "WS_REQUEST_TIMEOUT", 15000, ConfigType.INTEGER), WS_CONNECT_TIMEOUT(ModulesEnum.UIUtils, "WS_CONNECT_TIMEOUT",
			1000, ConfigType.INTEGER),

	// UI configuration
	REFRESH_PERIOD(ModulesEnum.UIManagement, "REFRESH_PERIOD", 10, ConfigType.INTEGER), UPLOAD_NUM_RECORDS(ModulesEnum.UIManagement,
			"UPLOAD_NUM_RECORDS", 10, ConfigType.INTEGER), CSV_DELEMITER(ModulesEnum.UIManagement, "CSV_DELEMITER", ",", ConfigType.STRING),
	DEFAULT_VIRTUAL_LIST_NAME(ModulesEnum.UIManagement, "DEFAULT_VIRTUAL_LIST_NAME", "Individuals", ConfigType.STRING),
	// Logging
	LOG_DIR_NAME(ModulesEnum.UIManagement, "LOG_DIR_NAME", "logs/", ConfigType.STRING), LOG_LEVEL_UI(ModulesEnum.UIManagement, "LOG_LEVEL", "debug",
			ConfigType.LOG_LEVEL),

	// URLs
	LIST_WEBSERVICE_URL(ModulesEnum.UIManagement, "LIST_WEBSERVICE_URL", ConfigType.STRING), CAMPAIGN_WEBSERVICE_URL(ModulesEnum.UIManagement,
			"CAMPAIGN_WEBSERVICE_URL", ConfigType.STRING), TEMPALTE_WEBSERVICE_URL(ModulesEnum.UIManagement, "TEMPALTE_WEBSERVICE_URL",
			ConfigType.STRING), ACCOUNT_WEBSERVICE_URL(ModulesEnum.UIManagement, "ACCOUNT_WEBSERVICE_URL", ConfigType.STRING), UPLOAD_FILE_SERVER_LINK(
			ModulesEnum.UIManagement, "UPLOAD_FILE_SERVER_LINK", ConfigType.STRING), DOWNLOAD_LIST_SERVER_LINK(ModulesEnum.UIManagement,
			"DOWNLOAD_LIST_SERVER_LINK", ConfigType.STRING),
			DOWNLOAD_REPORT_SERVER_LINK(ModulesEnum.UIManagement,
					"DOWNLOAD_REPORT_SERVER_LINK", ConfigType.STRING),
			REPORT_WEBSERVICE_URL(ModulesEnum.UIManagement, "REPORT_WEBSERVICE_URL",
			ConfigType.STRING), SERVICE_PROVISIONING_WEBSERVICE_URL(ModulesEnum.UIManagement, "SERVICE_PROVISIONING_WEBSERVICE_URL",
			ConfigType.STRING), UPLOAD_FILE_SIZE(ModulesEnum.UIManagement, "UPLOAD_FILE_SIZE", 500, ConfigType.INTEGER),
                        USER_MANAGEMENT_WEBSERVICE_URL(ModulesEnum.UIManagement, "USER_MANAGEMENT_WEBSERVICE_URL", ConfigType.STRING),
                        GROUP_MANAGEMENT_WEBSERVICE_URL(ModulesEnum.UIManagement, "GROUP_MANAGEMENT_WEBSERVICE_URL", ConfigType.STRING),
                        USER_LOGIN_WEBSERVICE_URL(ModulesEnum.UIManagement, "USER_LOGIN_WEBSERVICE_URL", ConfigType.STRING),
	// Number validation
	MSISDN_CC(ModulesEnum.UIUtils, "MSISDN_CC", "20", ConfigType.STRING), NDC_REGEX(ModulesEnum.UIUtils, "NDC_REGEX", "(10|11|12|15)", ConfigType.STRING), MSISDN_SN_LEN(
			ModulesEnum.UIUtils, "MISDN_SN_LEN", 8, ConfigType.INTEGER), MSISDN_NDC_LEN(ModulesEnum.UIUtils, "MISDN_SN_LEN", 2, ConfigType.INTEGER), MSISDN_NATIONAL_KEY(
			ModulesEnum.UIUtils, "MSISDN_NATIONAL_KEY", "0", ConfigType.STRING), SHORT_CODE_SENDER_LENGTH(ModulesEnum.UIUtils,
			"SHORT_CODE_SENDER_LENGTH", 7, ConfigType.INTEGER), ALPHANUM_SENDER_LENGTH(ModulesEnum.UIUtils, "ALPHANUM_SENDER_LENGTH", 11,
			ConfigType.INTEGER),

	// Reports dates
	REPORTS_PERIOD(ModulesEnum.UIUtils, "PERIOD_NUM", 90, ConfigType.INTEGER),

	REGESTERED_DELIVERY(ModulesEnum.UIUtils, "REGESTERED_DELIVERY", true, ConfigType.BOOLEAN), 
	USER_ATTRIBUTE(ModulesEnum.UIManagement,"USER_ATTRIBUTE", "REMOTE_USER",ConfigType.STRING), 
	LANGUAGE_ATTRIBUTE(ModulesEnum.UIManagement, "LANGUAGE_ATTRIBUTE","lang", ConfigType.STRING), 

	DIRECT_LOGIN_REDIRECT_URL(ModulesEnum.UIManagement, "DIRECT_LOGIN_REDIRECT_URL", ConfigType.STRING),
	USER_ACTIONS(ModulesEnum.UIManagement, "USER_ACTIONS" , "userActions", ConfigType.STRING),
	SMS_TEXT_LENGTH(ModulesEnum.UIManagement, "SMS_TEXT_LENGTH", 300, ConfigType.INTEGER), 
	TEMP_NAME_LENGTH(ModulesEnum.UIManagement, "TEMP_NAME_LENGTH", 40, ConfigType.INTEGER),
	// Alarms
        WS_CLIENT_CONNECT_TIMEOUT(ModulesEnum.UIUtils, "WS_CLIENT_CONNECT_TIMEOUT", 1000,ConfigType.INTEGER),
	WS_CLIENT_REQUEST_TIMEOUT(ModulesEnum.UIUtils, "WS_CLIENT_REQUEST_TIMEOUT", 5000,ConfigType.INTEGER),
        ERRORS_RAISING(ModulesEnum.UIUtils, "ERRORS_RAISING", false, ConfigType.BOOLEAN),
        ALARMING_IDENTIFIER(ModulesEnum.UIUtils, "ALARMING_IDENTIFIER", ConfigType.STRING),
        ERRORS_RAISING_SERVICE_URI(ModulesEnum.UIUtils, "ERRORS_RAISING_SERVICE_URI", ConfigType.STRING),
        ERRORS_CONFIG_SERVICE_URI(ModulesEnum.UIUtils, "ERRORS_CONFIG_SERVICE_URI", ConfigType.STRING),

	//UserManagement
        ADMINS_GROUP_NAME(ModulesEnum.UIManagement, "ADMINS_GROUP_NAME","Admins", ConfigType.STRING),

//    TOKEN_KEY(ModulesEnum.UIManagement,"TOKEN_KEY", "request-token", ConfigType.STRING),
	ACTIVE_PASSWORD(ModulesEnum.UIManagement,"ACTIVE_PASSWORD", "active-pass-flag", ConfigType.STRING),
	OTP_KEY(ModulesEnum.UIManagement,"OTP_KEY", "otp-key", ConfigType.STRING),
	CAMPAIGNS_URL(ModulesEnum.UIManagement, "CAMPAIGNS_URL","https://localhost:7002/web2sms/campaign/Campaigns.xhtml", ConfigType.STRING),
        REDIRECT_RELATIVE_URL(ModulesEnum.UIManagement, "REDIRECT_RELATIVE_URL","/login/login.xhtml", ConfigType.STRING),
    
        INACTIVE_PASS_REDIRECT_TO_CHANGE_PASS(ModulesEnum.UIManagement, "INACTIVE_PASS_REDIRECT_TO_CHANGE_PASS",true, ConfigType.BOOLEAN),
        LOGIN_ID(ModulesEnum.UIManagement,"LOGIN_ID", "LOGIN_ID",ConfigType.STRING),
        ENABLE_DIRECT_LOGIN(ModulesEnum.UIManagement, "ENABLE_DIRECT_LOGIN",true, ConfigType.BOOLEAN),
        LOGIN_REDIRECT_URL(ModulesEnum.UIManagement, "LOGIN_REDIRECT_URL", ConfigType.STRING)
        ;
	
	private static Map<ModulesEnum, Map<String, Configs>> modulesConfigs;

	public static Collection<Configs> getModuleConfigs(ModulesEnum module) {
		if (modulesConfigs == null) {
			modulesConfigs = new HashMap<ModulesEnum, Map<String, Configs>>();
		}
		Map<String, Configs> moduleConfigs = modulesConfigs.get(module);
		if (moduleConfigs == null)
			moduleConfigs = new HashMap<String, Configs>();
		modulesConfigs.put(module, moduleConfigs);
		return moduleConfigs.values();
	}

	public static Map<String, Configs> getModuleConfigsMap(ModulesEnum module) {
		if (modulesConfigs == null) {
			modulesConfigs = new HashMap<ModulesEnum, Map<String, Configs>>();
		}
		Map<String, Configs> moduleConfigs = modulesConfigs.get(module);
		if (moduleConfigs == null)
			moduleConfigs = new HashMap<String, Configs>();
		modulesConfigs.put(module, moduleConfigs);
		return moduleConfigs;
	}

	public static Set<ModulesEnum> getModules() {
		return modulesConfigs.keySet();
	}

	public static Configs getConfig(ModulesEnum module, String property) {
		return getModuleConfigsMap(module).get(property);
	}

	private final String property;
	private final boolean hasDefault;
	private Object value;
	private Object oldValue;
	private ConfigType type;
	private ModulesEnum module;

	Configs(ModulesEnum module, String property, ConfigType type) {
		this.property = property;
		this.hasDefault = false;
		this.type = type;
		this.module = module;
		Configs.getModuleConfigsMap(module).put(property, this);
	}

	Configs(ModulesEnum module, String property, Object value, ConfigType type) throws InvalidConfigValue {
		this.property = property;
		this.hasDefault = true;
		this.type = type;
		setValue(value);
		Configs.getModuleConfigsMap(module).put(property, this);
	}

	public boolean isHasDefault() {
		return hasDefault;
	}

	public void setValue(Object value) throws InvalidConfigValue {
		if (!type.validateType(value))
			throw new InvalidConfigValue(module, property, value, type);
		this.oldValue = this.value;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public Object getValue() throws ConfigValueNotSetException {
		if (value == null)
			throw new ConfigValueNotSetException(module, property);
		else if (type.equals(ConfigType.LIST))
			return Arrays.asList(((String) value).split(type.getAcceptedValues()[0]));

		return value;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public ConfigType getType() {
		return type;
	}

	public boolean isValidValue(Object value) {
		return type.validateType(value);
	}

	public ModulesEnum getModule() {
		return module;
	}

	public void setModule(ModulesEnum module) {
		this.module = module;
	}

}

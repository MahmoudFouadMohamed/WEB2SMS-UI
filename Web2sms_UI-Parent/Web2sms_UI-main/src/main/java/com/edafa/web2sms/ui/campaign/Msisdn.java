package com.edafa.web2sms.ui.campaign;

import com.edafa.web2sms.utils.configs.enums.Configs;

public class Msisdn {
	public static final String INTERNATIONAL_KEY = "00";
	public static final String INTERNATIONAL_PLUS = "\\+";
	public static final String EGT_REG = "20";
	public static final String EGT_EXT = INTERNATIONAL_KEY + EGT_REG;
	public static final String EGT_PLS = INTERNATIONAL_PLUS + EGT_REG;
	public static final String NATIONAL_REG = "0";

	public static int EGT_EXT_LEN = (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue() + EGT_EXT.length();
	public static int EGT_PLS_LEN = (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue() + EGT_PLS.length();
	public static int EGT_REG_LEN = (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue() + EGT_REG.length();
	public static int NATIONAL_REG_LEN = (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue()
			+ NATIONAL_REG.length();
	public static int NATIONAL_SRT_LEN = (Integer) Configs.MSISDN_NDC_LEN.getValue() + (Integer) Configs.MSISDN_SN_LEN.getValue();

}

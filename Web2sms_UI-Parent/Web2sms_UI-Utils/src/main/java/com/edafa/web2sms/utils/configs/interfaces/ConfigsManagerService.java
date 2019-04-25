package com.edafa.web2sms.utils.configs.interfaces;

import java.util.List;

import javax.ejb.Local;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import com.edafa.web2sms.utils.configs.exception.FailedToReadConfigsException;
import com.edafa.web2sms.utils.configs.exception.FailedToSaveConfigsException;
import com.edafa.web2sms.utils.configs.exception.InvalidConfigsException;
import com.edafa.web2sms.utils.configs.model.ModuleConfigs;
//import com.sun.xml.internal.ws.developer.SchemaValidation;

@WebService(name = "UIConfigsManagerService", portName = "UIConfigsManagerServicePort", targetNamespace = "http://www.edafa.com/ws/utils/configs")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
//@SchemaValidation
@Local
public interface ConfigsManagerService {

	@WebMethod(operationName = "getModules")
	@WebResult(name = "Modules", partName = "modules")
	public List<String> getModules();

	@WebMethod(operationName = "readConfigs")
	@WebResult(name = "ModuleConfigs", partName = "moduleConfigs")
	public List<ModuleConfigs> readConfigs() throws FailedToReadConfigsException;

	@WebMethod(operationName = "readModuleConfigs")
	@WebResult(name = "ModuleConfigs", partName = "moduleConfigs")
	public ModuleConfigs readModuleConfigs(@WebParam(name = "Module", partName = "module") String module)
			throws FailedToReadConfigsException;

	@WebMethod(operationName = "refreshModuleConfigs")
	public void refreshModuleConfigs(@WebParam(name = "Module", partName = "module") String module)
			throws FailedToReadConfigsException, InvalidConfigsException;

	@WebMethod(operationName = "refreshAllModuleConfigs")
	public void refreshAllModuleConfigs() throws FailedToReadConfigsException, InvalidConfigsException;

	@WebMethod(operationName = "saveConfigs")
	public void saveConfigs(@WebParam(name = "ModuleConfigs", partName = "moduleConfigs") ModuleConfigs moduleConfigs)
			throws FailedToSaveConfigsException, InvalidConfigsException;

}

package com.edafa.web2sms.ui.list;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;

import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.ui.filters.DirectLoginFilter;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

public class MySheetContentHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;
    
    
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	User user = new User();

	FacesContext facesContext = FacesContext.getCurrentInstance();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);

	List<String> getRowList;
	List<List<String>> myNewList = new ArrayList<List<String>>();
	List<String> columnsId = new ArrayList<String>();
	int count;
	int columnCount = 0;

	int maxRecord = (Integer) Configs.UPLOAD_NUM_RECORDS.getValue();

	@Override
	public void cell(String cellReference, String formattedValue, XSSFComment paramXSSFComment){//String cellReference, String formattedValue) {

		getRowList.add(formattedValue);
		
		/**
		 * count object refers to rowIndex
		 * count == 0 means 'first row (header)'
		 */
		if (count == 0) {
			columnsId.add(String.valueOf(columnCount++));
		}

	}

	@Override
	public void endRow(int paramInt) {
		if (myNewList.size() < maxRecord) {
			myNewList.add(getRowList);
		} else {
			throw new TerminateSaxParserException("Done reading rows_num=" + maxRecord + " to view to the user");
		}
	}

	@Override
	public void headerFooter(String arg0, boolean arg1, String arg2) {

	}

	@Override
	public void startRow(int rowNum) {
		getRowList = new ArrayList<String>();
		if (rowNum == 0) {
			count = 0;
		} else {
			count++;
		}
	}

	public List<List<String>> getRecordsList() {
		try {
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());

			userLogger.info( "Returning records list of uploaded file to user");

			return myNewList;
		} catch (Exception e) {
			userLogger.error( "Error while returning records list of uploaded file to user", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return null;
		}
	}

	public List<String> getColumnsNumList() {
		try {
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());

			userLogger.info("Returning columns list of files to user");
			return columnsId;
		} catch (Exception e) {
			userLogger.error("Error while returning columns list of files to user", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return null;
		}
	}

	public String userLogInfo(String id, String userName) {
		StringBuilder sb = new StringBuilder();
		sb.append("User");
		sb.append("(");
		sb.append(id);
		sb.append(",");
		sb.append(userName);
		sb.append("): ");
		return sb.toString();
	}
}

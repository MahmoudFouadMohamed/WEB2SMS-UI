package com.edafa.web2sms.ui.list;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.edafa.csv.batchfile.CSVBatchFile;
import com.edafa.csv.record.CSVRecord;
import com.edafa.csv.record.Field;
import com.edafa.web2sms.encyrptionutil.Interfaces.EncyrptionUtilInterface;
import com.edafa.web2sms.service.enums.ListType;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.list.model.ContactListInfoResultSet;
import com.edafa.web2sms.service.list.model.ContactListResultSet;
import com.edafa.web2sms.service.list.model.ContactResultSet;
import com.edafa.web2sms.service.list.model.FileResult;
import com.edafa.web2sms.service.lists.ListsManegementService;
import com.edafa.web2sms.service.model.Contact;
import com.edafa.web2sms.service.model.ContactList;
import com.edafa.web2sms.service.model.ContactListInfo;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.file.DownloadedFileInfo;
import com.edafa.web2sms.utils.file.FileUploadClient;
import com.edafa.web2sms.utils.sms.SMSUtils;

import ezvcard.VCard;
import ezvcard.io.text.VCardReader;
import files.list.service.web2sms.edafa.com.FileDetails;

@ViewScoped
@ManagedBean(name = "viewListBean")
public class ViewListBean {
	private Map<Integer, Boolean> checked;
	private List<ContactListInfo> listofLists;
	ListsManegementService listsManegementServicePort;
	private String listNameSearch;

	private String subListName;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
	private List<Contact> contactsList;
	private String listIdForSubList;

	private int totalRows;
	private int firstRow;
	private int rowsPerPage;
	private int pageRange;
	private Integer[] pages;
	private int currentPage;
	private int totalPages;

	private int totalRowsNum;
	/*
	 * List is used to hold selected contacts, MSISDN is used as a KEY and a
	 * boolean value (Selected/Not-Selected) is used as a value
	 */
	private Map<String, Boolean> checkedSubList;
	private String firstName;
	private String msisdn;
	private List<Contact> newcontactList;
	private String searchMsisdn;

	private boolean viewContactPageFlag = false;
	private boolean contactFlag;
	private boolean chooseMessFlag;

	FacesContext facesContext = FacesContext.getCurrentInstance();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);

	private boolean exportButton;
	private boolean errorButton;
	private Integer listCheckedId;
	List<ContactListInfo> checkedListsNew = new ArrayList<ContactListInfo>();
	private boolean viewButtonsFlag = true;
	User user = new User();
	InputStream input;

	@EJB
	WSClients portObj;
        
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;
	
	@EJB
	EncyrptionUtilInterface encyrptionUtil;

	private UploadedFile uploadedFile;
	private String fileName;
	private File file;
	private Map<Integer, String> mapOfColumn;
	private List<Entry<Integer, String>> listOfColumn;
	private String listName;

	@Resource(name = "java:app/env/basedirfiles")
	String proBaseDir;

	private List<Contact> contactList = new ArrayList<Contact>();
	private String firstName2;
	private String MSISDN;
	private List<Contact> listContact;
	private List<ContactListInfo> userList;
	private String choosenList;
	private Map<String, Boolean> checked2;

	private int totalRows2;
	private int firstRow2;
	private int rowsPerPage2;
	private int pageRange2;
	private Integer[] pages2;
	private int currentPage2;
	private int totalPages2;

	private int listId;
	private String choosenListName;
	private int totalRowsNum2;
	private String choosenUploadFileType;
	private String selectedMapValue;

	private List<Map<Integer, String>> recordsList;
	private ArrayList<String> files = new ArrayList<String>();
	private List<List<String>> myNewList;
	private String nameMap = "-1";
	private String msisdnMap = "-1";
	private List<String> columnsId;
	private boolean showInvalidFile = false;
	private String fileUploadName;
	private boolean uploadTableFalg;
	String fileType;
	DownloadedFileInfo result;

	private int totalRows3;
	private int firstRow3;
	private int rowsPerPage3;
	private int pageRange3;
	private Integer[] pages3;
	private int currentPage3;
	private int totalPages3;

	private boolean tableFlag = true;
	private boolean importFromListFlag;
	private boolean importFromFileFlag;
	private String selectedType = new String();

	private String changedListName;
	private Contact contactEditObj;
	private String contactFirstName;
	private String contactMsisdn;

	private String changedListId;
	private List<Contact> archiveList;
	private boolean searchMode;

	/**
	 * Intra List added vars..
	 */
	private boolean editFlag = true;
	private boolean deleteContactFromList = false;
	private boolean intraSubListChoosen = false;
	private boolean intraListChoosen = false;
	private boolean editContactFlag = false;
	private Contact intraList;
	private Integer IntraListId;
	private List<Contact> intraContactsList;
	private Map<Integer, Boolean> intraContactsSelected;
	// flag for view internal lists button to paginate in internal lists or in
	// normal lists.
	private boolean viewInternalListsFlag = false;
	private boolean intraPagiantionFlag;

	/**
	 * Customized List added vars..
	 */
	private String value1Map = "-1";
	private String value2Map = "-1";
	private String value3Map = "-1";
	private String value4Map = "-1";
	private String value5Map = "-1";
	private Boolean customizedlist = null;
	private boolean addContactflag = false;

	/**
	 * map for drop down list of table for uploaded file values in it changes
	 * according to file type second value is get from appropriate property file
	 * according to language
	 * */
	private Map<String, String> valuesMap;

	/**
	 * map for file extensions
	 */
	private boolean createCustomizeList;
	String value1;
	String value2;
	String value3;
	String value4;
	String value5;

	// XSS validation parameters
	boolean validFile = true;
	// pagingation variables for intra list for add in subintra list
	private int totalRowsIntra;
	private int firstRowIntra;
	private int rowsPerPageIntra;
	private int pageRangeIntra;
	private Integer[] pagesIntra;
	private int currentPageIntra;
	private int totalPagesIntra;

	// private boolean intraListFlag= false;

	// Setters and getters-----------------------------------------

	public List<Contact> getContactsList() {
		return contactsList;
	}

	public boolean isCreateCustomizeList() {
		return createCustomizeList;
	}

	public void setCreateCustomizeList(boolean createCustomizeList) {
		this.createCustomizeList = createCustomizeList;
	}

	public boolean isIntraPagiantionFlag() {
		return intraPagiantionFlag;
	}

	public void setIntraPagiantionFlag(boolean intraPagiantionFlag) {
		this.intraPagiantionFlag = intraPagiantionFlag;
	}

	public Map<String, String> getValuesMap() {
		return valuesMap;
	}

	public void setValuesMap(Map<String, String> valuesMap) {
		this.valuesMap = valuesMap;
	}

	public boolean isAddContactflag() {
		return addContactflag;
	}

	public void setAddContactflag(boolean addContactflag) {
		this.addContactflag = addContactflag;
	}

	public boolean isCustomizedlist() {
		return customizedlist;
	}

	public void setCustomizedlist(boolean customizedlist) {
		this.customizedlist = customizedlist;
	}

	public String getValue1Map() {
		return value1Map;
	}

	public void setValue1Map(String value1Map) {
		this.value1Map = value1Map;
	}

	public String getValue2Map() {
		return value2Map;
	}

	public void setValue2Map(String value2Map) {
		this.value2Map = value2Map;
	}

	public String getValue3Map() {
		return value3Map;
	}

	public void setValue3Map(String value3Map) {
		this.value3Map = value3Map;
	}

	public String getValue4Map() {
		return value4Map;
	}

	public void setValue4Map(String value4Map) {
		this.value4Map = value4Map;
	}

	public String getValue5Map() {
		return value5Map;
	}

	public void setValue5Map(String value5Map) {
		this.value5Map = value5Map;
	}

	public Map<Integer, Boolean> getIntraContactsSelected() {
		return intraContactsSelected;
	}

	public void setIntraContactsSelected(Map<Integer, Boolean> intraContactsSelected) {
		this.intraContactsSelected = intraContactsSelected;
	}

	public List<Contact> getIntraContactsList() {
		return intraContactsList;
	}

	public void setIntraContactsList(List<Contact> intraContactsList) {
		this.intraContactsList = intraContactsList;
	}

	public Integer getIntraListId() {
		return IntraListId;
	}

	public void setIntraListId(Integer intraListId) {
		IntraListId = intraListId;
	}

	public Contact getIntraList() {
		return intraList;
	}

	public void setIntraList(Contact intraList) {
		this.intraList = intraList;
	}

	public boolean isIntraSubListChoosen() {
		return intraSubListChoosen;
	}

	public void setIntraSubListChoosen(boolean intraSubListChoosen) {
		this.intraSubListChoosen = intraSubListChoosen;
	}

	public boolean isDeleteContactFromList() {
		return deleteContactFromList;
	}

	public void setDeleteContactFromList(boolean deleteContactFromList) {
		this.deleteContactFromList = deleteContactFromList;
	}

	public boolean isEditContactFlag() {
		return editContactFlag;
	}

	public void setEditContactFlag(boolean editContactFlag) {
		this.editContactFlag = editContactFlag;
	}

	public boolean isIntraListChoosen() {
		return intraListChoosen;
	}

	public void setIntraListChoosen(boolean intraListChoosen) {
		this.intraListChoosen = intraListChoosen;
	}

	public boolean isEditFlag() {
		return editFlag;
	}

	public void setEditFlag(boolean editFlag) {
		this.editFlag = editFlag;
	}

	public Contact getContactEditObj() {
		return contactEditObj;
	}

	public void setContactEditObj(Contact contactEditObj) {
		this.contactEditObj = contactEditObj;
	}

	public String getContactFirstName() {
		return contactFirstName;
	}

	public void setContactFirstName(String contactFirstName) {
		this.contactFirstName = contactFirstName;
	}

	public String getContactMsisdn() {
		return contactMsisdn;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	public String getValue3() {
		return value3;
	}

	public void setValue3(String value3) {
		this.value3 = value3;
	}

	public String getValue4() {
		return value4;
	}

	public void setValue4(String value4) {
		this.value4 = value4;
	}

	public String getValue5() {
		return value5;
	}

	public void setValue5(String value5) {
		this.value5 = value5;
	}

	public void setContactMsisdn(String contactMsisdn) {
		this.contactMsisdn = contactMsisdn;
	}

	public String getChangedListName() {
		return changedListName;
	}

	public void setChangedListName(String changedListName) {
		this.changedListName = changedListName;
	}

	public String getSelectedType() {
		return selectedType;
	}

	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}

	public boolean isImportFromListFlag() {
		return importFromListFlag;
	}

	public void setImportFromListFlag(boolean importFromListFlag) {
		this.importFromListFlag = importFromListFlag;
	}

	public boolean isImportFromFileFlag() {
		return importFromFileFlag;
	}

	public void setImportFromFileFlag(boolean importFromFileFlag) {
		this.importFromFileFlag = importFromFileFlag;
	}

	public boolean isTableFlag() {
		return tableFlag;
	}

	public void setTableFlag(boolean tableFlag) {
		this.tableFlag = tableFlag;
	}

	public boolean isViewButtonsFlag() {
		return viewButtonsFlag;
	}

	public void setViewButtonsFlag(boolean viewButtonsFlag) {
		this.viewButtonsFlag = viewButtonsFlag;
	}

	public Integer getListCheckedId() {
		return listCheckedId;
	}

	public void setListCheckedId(Integer listCheckedId) {
		this.listCheckedId = listCheckedId;
	}

	public int getTotalRows3() {
		return totalRows3;
	}

	public boolean isSearchMode() {
		return searchMode;
	}

	public void setSearchMode(boolean searchMode) {
		this.searchMode = searchMode;
	}

	public void setTotalRows3(int totalRows3) {
		this.totalRows3 = totalRows3;
	}

	public int getFirstRow3() {
		return firstRow3;
	}

	public void setFirstRow3(int firstRow3) {
		this.firstRow3 = firstRow3;
	}

	public int getRowsPerPage3() {
		return rowsPerPage3;
	}

	public void setRowsPerPage3(int rowsPerPage3) {
		this.rowsPerPage3 = rowsPerPage3;
	}

	public int getPageRange3() {
		return pageRange3;
	}

	public void setPageRange3(int pageRange3) {
		this.pageRange3 = pageRange3;
	}

	public Integer[] getPages3() {
		return pages3;
	}

	public void setPages3(Integer[] pages3) {
		this.pages3 = pages3;
	}

	public int getCurrentPage3() {
		return currentPage3;
	}

	public void setCurrentPage3(int currentPage3) {
		this.currentPage3 = currentPage3;
	}

	public int getTotalPages3() {
		return totalPages3;
	}

	public void setTotalPages3(int totalPages3) {
		this.totalPages3 = totalPages3;
	}

	public boolean isErrorButton() {
		return errorButton;
	}

	public void setErrorButton(boolean errorButton) {
		this.errorButton = errorButton;
	}

	public boolean isExportButton() {
		return exportButton;
	}

	public void setExportButton(boolean exportButton) {
		this.exportButton = exportButton;
	}

	public boolean isChooseMessFlag() {
		return chooseMessFlag;
	}

	public void setChooseMessFlag(boolean chooseMessFlag) {
		this.chooseMessFlag = chooseMessFlag;
	}

	public boolean isContactFlag() {
		return contactFlag;
	}

	public void setContactFlag(boolean contactFlag) {
		this.contactFlag = contactFlag;
	}

	public boolean isViewContactPageFlag() {
		return viewContactPageFlag;
	}

	public void setViewContactPageFlag(boolean viewContactPageFlag) {
		this.viewContactPageFlag = viewContactPageFlag;
	}

	public String getSearchMsisdn() {
		return searchMsisdn;
	}

	public void setSearchMsisdn(String searchMsisdn) {
		this.searchMsisdn = searchMsisdn;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public void setContactsList(List<Contact> contactsList) {
		this.contactsList = contactsList;
	}

	public String getSubListName() {
		return subListName;
	}

	public void setSubListName(String subListName) {
		this.subListName = subListName;
	}

	public String getListNameSearch() {
		return listNameSearch;
	}

	public void setListNameSearch(String listNameSearch) {
		this.listNameSearch = listNameSearch;
	}

	public Map<Integer, Boolean> getChecked() {
		return checked;
	}

	public void setChecked(Map<Integer, Boolean> checked) {
		this.checked = checked;
	}

	public Map<String, Boolean> getCheckedSubList() {
		return checkedSubList;
	}

	public void setCheckedSubList(Map<String, Boolean> checkedSubList) {
		this.checkedSubList = checkedSubList;
	}

	public List<ContactListInfo> getListofLists() {
		return listofLists;
	}

	public void setListofLists(List<ContactListInfo> listofLists) {
		this.listofLists = listofLists;
	}

	public String getSelectedMapValue() {
		return selectedMapValue;
	}

	public boolean isUploadTableFalg() {
		return uploadTableFalg;
	}

	public void setUploadTableFalg(boolean uploadTableFalg) {
		this.uploadTableFalg = uploadTableFalg;
	}

	public String getFileUploadName() {
		return fileUploadName;
	}

	public void setFileUploadName(String fileUploadName) {
		this.fileUploadName = fileUploadName;
	}

	public boolean isShowInvalidFile() {
		return showInvalidFile;
	}

	public void setShowInvalidFile(boolean showInvalidFile) {
		this.showInvalidFile = showInvalidFile;
	}

	public List<String> getColumnsId() {
		return columnsId;
	}

	public void setColumnsId(List<String> columnsId) {
		this.columnsId = columnsId;
	}

	public String getNameMap() {
		return nameMap;
	}

	public void setNameMap(String nameMap) {
		this.nameMap = nameMap;
	}

	public String getMsisdnMap() {
		return msisdnMap;
	}

	public void setMsisdnMap(String msisdnMap) {
		this.msisdnMap = msisdnMap;
	}

	public ArrayList<String> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}

	public List<Map<Integer, String>> getRecordsList() {
		return recordsList;
	}

	public void setRecordsList(List<Map<Integer, String>> recordsList) {
		this.recordsList = recordsList;
	}

	public void setSelectedMapValue(String selectedMapValue) {
		this.selectedMapValue = selectedMapValue;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public String getChoosenUploadFileType() {
		return choosenUploadFileType;
	}

	public void setChoosenUploadFileType(String choosenUploadFileType) {
		this.choosenUploadFileType = choosenUploadFileType;
	}

	public List<Contact> getListContact() {
		return listContact;
	}

	public void setListContact(List<Contact> listContact) {
		this.listContact = listContact;
	}

	public String getChoosenListName() {
		return choosenListName;
	}

	public List<List<String>> getMyNewList() {
		return myNewList;
	}

	public void setMyNewList(List<List<String>> myNewList) {
		this.myNewList = myNewList;
	}

	public void setChoosenListName(String choosenListName) {
		this.choosenListName = choosenListName;
	}

	public Map<String, Boolean> getChecked2() {
		return checked2;
	}

	public void setChecked2(Map<String, Boolean> checked2) {
		this.checked2 = checked2;
	}

	public String getChoosenList() {
		return choosenList;
	}

	public void setChoosenList(String choosenList) {
		this.choosenList = choosenList;
	}

	public List<ContactListInfo> getUserList() {
		return userList;
	}

	public void setUserList(List<ContactListInfo> userList) {
		this.userList = userList;
	}

	public List<Contact> getContactList() {
		return contactList;
	}

	public void setContactList(List<Contact> contactList) {
		this.contactList = contactList;
	}

	public String getFirstName2() {
		return firstName2;
	}

	public void setFirstName2(String firstName2) {
		this.firstName2 = firstName2;
	}

	public String getMSISDN() {
		return MSISDN;
	}

	public void setMSISDN(String mSISDN) {
		MSISDN = mSISDN;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<Entry<Integer, String>> getListOfColumn() {
		return listOfColumn;
	}

	public void setListOfColumn(List<Entry<Integer, String>> listOfColumn) {
		this.listOfColumn = listOfColumn;
	}

	public Map<Integer, String> getMapOfColumn() {
		return mapOfColumn;
	}

	public void setMapOfColumn(Map<Integer, String> mapOfColumn) {
		this.mapOfColumn = mapOfColumn;
	}

	// Constructor----------------------------------------------

	public ViewListBean() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			 ThreadContext.push(trxId);
			userInfo.setUser(user);
			listofLists = new ArrayList<ContactListInfo>();
			valuesMap = new LinkedHashMap<String, String>();
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());

			rowsPerPage = 10; // Default rows per page (max amount of rows to be
			// displayed at once).
			pageRange = 10;

			userList = new ArrayList<ContactListInfo>();
			listContact = new ArrayList<Contact>();
			checked2 = new HashMap<String, Boolean>();
			archiveList = new ArrayList<Contact>();
			intraContactsList = new ArrayList<Contact>();
			intraContactsSelected = new HashMap<Integer, Boolean>();

			rowsPerPageIntra = 10;
			rowsPerPage2 = 10; // Default rows per page (max amount of rows to
								// be
			// displayed at once).
			pageRange2 = 10;

			rowsPerPage3 = 10; // Default rows per page (max amount of rows to
								// be
			// displayed at once).
			pageRange3 = 10;

			// userLogger.debug(userLogInfo(user.getAccountId(),
			// user.getUsername())
			// +
			// "get customizeList flag from session if found, or initialize with false.");

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			customizedlist = (Boolean) session.getAttribute("customizedlist");
			if (customizedlist == null)
				customizedlist = false;

			// userLogger.info(userLogInfo(user.getAccountId(),
			// user.getUsername())
			// + "constructing customizeList flag with: " + customizedlist);

		} catch (Exception e) {
			userLogger.error("Error while intializing user", e);
			appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.pop();
		}
	}

	@PostConstruct
	public void populateList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			listsManegementServicePort = portObj.getListsManegementServicePort();
			userLogger.debug( "Getting paginated lists from=" + firstRow3 + " to=" + rowsPerPage3);
			List<ListType> typeList = new ArrayList<ListType>();
			if (viewInternalListsFlag) {
				typeList.add(ListType.INTRA_LIST);
				typeList.add(ListType.INTRA_SUB_LIST);
			} else {
				typeList.add(ListType.NORMAL_LIST);
				typeList.add(ListType.VIRTUAL_LIST);
				typeList.add(ListType.CUSTOMIZED_LIST);
			}
			ContactListInfoResultSet result = listsManegementServicePort.getContactListsInfoPagination(userInfo,
					firstRow3, rowsPerPage3, typeList);
			ResponseStatus state = result.getStatus();
			userLogger.info( "listsManegementServicePort response is:(" + state + ").");

			switch (state) {

			case SUCCESS:
				listofLists = result.getContactListInfoResultSet();
				userLogger.info( "list of contact lists is populating successfully, rows_count=" + listofLists.size());
				break;
			case FAIL:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                .getLocalizedLabel("loading_failed")), null));
				userLogger.error( "Fail while getting list of contact lists");
				break;
			case INELIGIBLE_ACCOUNT:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                .getLocalizedLabel("ineligible_view_lists")), null));
				userLogger.warn( "Ineligible user, not allowed to display lists of contacts");
                            break;
			default:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                .getLocalizedLabel("loading_failed")), null));
				userLogger.error( "Unknown response status=" + result.getStatus() + " , while getting list of contact lists");
				break;
			}
			populateIntraListToImport();

			totalRows3 = listsManegementServicePort.countContactListsInfo(userInfo, typeList);

			userLogger.info( " total list rows_count=" + totalPages3);

			// Set currentPage, totalPages and pages.
			currentPage3 = (totalRows3 / rowsPerPage3) - ((totalRows3 - firstRow3) / rowsPerPage3) + 1;
			totalPages3 = (totalRows3 / rowsPerPage3) + ((totalRows3 % rowsPerPage3 != 0) ? 1 : 0);
			int pagesLength = Math.min(pageRange3, totalPages3);
			pages3 = new Integer[pagesLength];

			// firstPage must be greater than 0 and lesser than
			// totalPages-pageLength.
			int firstPage = Math.min(Math.max(0, currentPage3 - (pageRange3 / 2)), totalPages3 - pagesLength);

			// Create pages (page numbers for page links).
			for (int i = 0; i < pagesLength; i++) {
				pages3[i] = ++firstPage;
				if (pagesLength > 10) {
					pagesLength = 10;
				}
			}

			if (listofLists == null || listofLists.size() == 0) {
				tableFlag = false;
			}

			checked = new HashMap<Integer, Boolean>();
			checkedSubList = new HashMap<String, Boolean>();

			if (listofLists == null || listofLists.isEmpty()) {
				viewButtonsFlag = false;
				userLogger
						.info( "List of contact lists is empty");
			}
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			session.removeAttribute("listName");
		} catch (Exception e) {
                        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			userLogger.error("Error while getting paginated lists of contacts from=" + firstRow3 + " to=" + rowsPerPage3, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public String logTrxId(String trxId) {
		StringBuilder sb = new StringBuilder();
		sb.append("UserTrx");
		sb.append("(");
		sb.append(trxId);
		sb.append("): ");
		return sb.toString();
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

	// Paging actions
	// -----------------------------------------------------------------------------

	public void pageFirst() {
		page(0);
	}

	public void pageNext() {
		page(firstRow + rowsPerPage);
	}

	public void pagePrevious() {
		page(firstRow - rowsPerPage);
	}

	public void pageLast() {
		page(totalRows - ((totalRows % rowsPerPage != 0) ? totalRows % rowsPerPage : rowsPerPage));
	}

	private void page(int firstRow) {
		this.firstRow = firstRow;
		populateSubList(); // Load requested page.
	}

	public void page(ActionEvent event) {
		page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
	}

	public void pageFirst2() {
		page2(0);
	}

	public void pageNext2() {
		page2(firstRow2 + rowsPerPage2);
	}

	public void pageNextIntra() {
		pageIntra(firstRowIntra + rowsPerPageIntra);
	}

	public void pageIntra(int firstRowIntra) {
		this.firstRowIntra = firstRowIntra;
		populateIntraListToImport();// Load requested page.
	}

	public void pagePrevious2() {
		page2(firstRow2 - rowsPerPage2);
	}

	public void pageLast2() {
		page2(totalRows2 - ((totalRows2 % rowsPerPage2 != 0) ? totalRows2 % rowsPerPage2 : rowsPerPage2));
	}

	private void page2(int firstRow2) {
		this.firstRow2 = firstRow2;
		populateList2(); // Load requested page.
	}

	public void page2(ActionEvent event) {
		page2(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage2);
	}

	public void pageFirst3() {
		page3(0);
	}

	public void pageNext3() {
		page3(firstRow3 + rowsPerPage3);
	}

	public void pagePrevious3() {
		page3(firstRow3 - rowsPerPage3);
	}

	public void pageLast3() {
		page3(totalRows3 - ((totalRows3 % rowsPerPage3 != 0) ? totalRows3 % rowsPerPage3 : rowsPerPage3));
	}

	private void page3(int firstRow3) {

		this.firstRow3 = firstRow3;
		if (viewInternalListsFlag) {
			viewAllIntraLists();
			intraPagiantionFlag = true;
		} else
			populateList(); // Load requested page.
	}

	public void page3(ActionEvent event) {
		page3(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage3);
	}

	// Action methods ----------------------------------------------------

	public void getUpdateListId(String listId) {
		for (int i = 0; i < listofLists.size(); i++) {
			if (listId.equals(String.valueOf(listofLists.get(i).getListId()))) {
				subListName = listofLists.get(i).getListName();
				if (listofLists.get(i).getListType().equals(ListType.INTRA_LIST)) {
					editFlag = false;
					deleteContactFromList = false;
					intraSubListChoosen = false;
					editContactFlag = false;
					customizedlist = false;
					addContactflag = false;
					// addNewContactToList = false;
					userLogger.info( "list with id: (" + i
							+ ") is Intra List. ");
					userLogger.info( "editflag is: " + editFlag);

				} else if (listofLists.get(i).getListType().equals(ListType.INTRA_SUB_LIST)) {
					editFlag = true;
					editContactFlag = false;
					deleteContactFromList = true;
					intraSubListChoosen = true;
					customizedlist = false;
					addContactflag = false;
					userLogger.info( "list with id: (" + i
							+ ") is sub Intra List. ");
					userLogger.info( "editflag is: " + editFlag);
					userLogger.info( "editing contact flag is: "
							+ editContactFlag);

				} else if (listofLists.get(i).getListType().equals(ListType.CUSTOMIZED_LIST)) {
					editFlag = true;
					deleteContactFromList = true;
					intraSubListChoosen = false;
					editContactFlag = true;
					addContactflag = true;
					customizedlist = true;
					userLogger.info( "list with id: (" + i
							+ ") is customized List. ");

					userLogger.info( "set customize list flag into session for add new contact list page");
					FacesContext facesContext = FacesContext.getCurrentInstance();
					HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
					session.setAttribute("customizedlist", customizedlist);

				} else {
					editFlag = true;
					deleteContactFromList = true;
					editContactFlag = true;
					intraSubListChoosen = false;
					addContactflag = true;
					customizedlist = false;
					userLogger.info( "editflag is: " + editFlag);
				}

				totalRowsNum = listofLists.get(i).getContactsCount();
				userLogger.info( "List choosen for getting it's contacts list_id=" + listId + " and name = " + subListName
						+ " is (" + listofLists.get(i).getListType() + ") , total_rows_count=" + totalRowsNum);
			}

		}
		listIdForSubList = listId;
		newcontactList = new ArrayList<Contact>();
		changedListName = subListName;
		changedListId = listId;
		contactsList = new ArrayList<Contact>();
		firstRow = 0;
		archiveList.clear();
		populateSubList();
	}

	public void populateSubList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			userInfo.setUser(user);

			userLogger.debug( "Getting contacts for list_id=" + listIdForSubList + " from=" + firstRow + " get count="
					+ rowsPerPage);

			ContactResultSet result = listsManegementServicePort.getContactListWithPagination(userInfo,
					Integer.valueOf(listIdForSubList), firstRow, rowsPerPage);

			ResponseStatus response = result.getStatus();
			userLogger.info("listsManegementServicePort response is:(" + response + ").");

			switch (response) {
			case SUCCESS:
				if (searchMode == true) {
					contactsList.clear();
					for (Contact contact : archiveList) {
						contactsList.add(contact);
					}
					searchMode = false;
				} else {
					for (Contact contact : result.getContacts()) {
						contactsList.add(contact);

						archiveList.add(contact);

						userLogger.info( " list of contacts count="
								+ result.getContacts().size() + " for list_id=" + listIdForSubList);
					}
				}

				userLogger.info("list of contacts of id=" + listIdForSubList + " is populated successfully");
				break;
			case FAIL:
				contactsList.clear();
				userLogger.error( "Error while populating list of contacts of id=" + listIdForSubList);
				break;
			case INELIGIBLE_ACCOUNT:
				contactsList.clear();
				userLogger.warn("ineligible account, not allowed to display contacts list_id=" + listIdForSubList);
				break;
			default:
				contactsList.clear();
				userLogger.error("undefined return status=" + response + " while getting contacts of list_id="
						+ listIdForSubList);
				break;
			}

			totalRows = totalRowsNum;

			// Set currentPage, totalPages and pages.
			currentPage = (totalRows / rowsPerPage) - ((totalRows - firstRow) / rowsPerPage) + 1;
			totalPages = (totalRows / rowsPerPage) + ((totalRows % rowsPerPage != 0) ? 1 : 0);
			int pagesLength = Math.min(pageRange, totalPages);
			pages = new Integer[pagesLength];

			// firstPage must be greater than 0 and lesser than
			// totalPages-pageLength.
			int firstPage = Math.min(Math.max(0, currentPage - (pageRange / 2)), totalPages - pagesLength);

			// Create pages (page numbers for page links).
			for (int i = 0; i < pagesLength; i++) {
				pages[i] = ++firstPage;
				if (pagesLength > 10) {
					pagesLength = 10;
				}
			}

			if (contactsList == null || totalRows == 0) {
				userLogger.info( "No sub contacts for list_name="
						+ subListName + " ,list_id=" + listIdForSubList + " found in the database");
			}

		} catch (Exception e) {
			contactsList.clear();
			userLogger.error( "Error while loading contacts for list_name=" + subListName + " ,list_id=" + listIdForSubList, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	public void searchForList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			if (!listNameSearch.trim().isEmpty()) {
				tableFlag = false;

				userLogger.debug( "Searching for list_name="
						+ listNameSearch);
				List<ListType> listType = new ArrayList<ListType>();
				if (viewInternalListsFlag) {
					// intra and subIntra
					listType.add(ListType.INTRA_LIST);
					listType.add(ListType.INTRA_SUB_LIST);

				} else {
					// normal, virtual and customized
					listType.add(ListType.CUSTOMIZED_LIST);
					listType.add(ListType.NORMAL_LIST);
					listType.add(ListType.VIRTUAL_LIST);

				}
				ContactListResultSet result = listsManegementServicePort
						.searchLists(userInfo, listNameSearch, listType);
				ResponseStatus state = result.getStatus();

				userLogger.info( "listsManegementServicePort response is:(" + state + ").");
				switch (state) {
				case SUCCESS:
					listofLists = result.getContactListInfoSet();
					userLogger.info( " searching for list_name="
							+ listNameSearch + " returned with result successfully with count="
							+ result.getContactListInfoSet().size());
					break;
				case FAIL:
					userLogger.error(  "Error while searching for "
							+ listNameSearch);
					break;
				case LIST_NOT_FOUND:
					userLogger.warn(  "List " + listNameSearch
							+ " is not found");
					break;
				case INELIGIBLE_ACCOUNT:
					userLogger.warn( "ineligible user, not allowed to display lists");
				default:
					userLogger.error(  "undefined result" + state
							+ " for searching list");
					break;
				}
			} else {
				tableFlag = true;
				populateList();
			}
		} catch (Exception e) {
			tableFlag = false;
			userLogger.error( "Error while searching for list ", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void deleteList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String deletedListId = "( ";
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			List<String> listStatus = new ArrayList<String>();
			List<String> listOfSucessList = new ArrayList<String>();
			List<String> listOffail = new ArrayList<String>();
			ResultStatus status = new ResultStatus();
			List<ContactListInfo> checkedList = new ArrayList<ContactListInfo>();

			for (Map.Entry<Integer, Boolean> e : checked.entrySet()) {
				Integer key = e.getKey();
				Boolean value = e.getValue();
				if (value.equals(true)) {
					for (int i = 0; i < listofLists.size(); i++) {
						if (listofLists.get(i).getListId().equals(key)) {
							checkedList.add(listofLists.get(i));
						}
					}
				}
			}
			boolean intraListToDelete = false;

			Iterator<ContactListInfo> checkedListIter = checkedList.iterator();
			while (checkedListIter.hasNext()) {
				ContactListInfo contactListInfo = checkedListIter.next();
				if (contactListInfo.getListType().equals(ListType.INTRA_LIST)) {
					intraListToDelete = true;
					checkedListIter.remove();
				}
			}

			if (checkedList.size() != 0) {

				for (int i = 0; i < checkedList.size(); i++) {

					userLogger.debug(  "Deleting list_id="
							+ checkedList.get(i).getListId());

					deletedListId = deletedListId + checkedList.get(i).getListId() + " - ";

					status = listsManegementServicePort.deleteContactList(userInfo, checkedList.get(i));

					ResponseStatus resp = status.getStatus();

					userLogger.info( "listsManegementServicePort response is:(" + resp + ").");

					switch (resp) {
					case SUCCESS:
						if (intraListToDelete) {
							listStatus.add(String.valueOf("intra list"));
							userLogger.info( "intraListToDelete flag value is : (" + intraListToDelete + ").");
						}
						listOfSucessList.add(checkedList.get(i).getListName());
						listofLists.remove(checkedList.get(i));
						userLogger.info( "list_id="
								+ checkedList.get(i).getListId() + "deleted successfully");
						break;
					case FAIL:
						if (intraListToDelete) {
							listStatus.add(String.valueOf("intraListToDelete"));
							userLogger.info( "intraListToDelete flag value is : (" + intraListToDelete + ").");
						}
						listOffail.add(checkedList.get(i).getListName());
						userLogger.error( "error while deleting list_id=" + checkedList.get(i).getListId());
						break;
					case INELIGIBLE_ACCOUNT:
						if (intraListToDelete) {
							listStatus.add(String.valueOf("intraListToDelete"));
							userLogger.info( "intraListToDelete flag value is : (" + intraListToDelete + ").");
						}
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("ineligible_delete_list")), null));
						userLogger.warn( "ineligible user, can not delete list/lists");
						break;
					case LOCKED_LIST:
						if (intraListToDelete) {
							listStatus.add(String.valueOf("intraListToDelete"));
							userLogger.info( "intraListToDelete flag value is : (" + intraListToDelete + ").");
						}
						listStatus.add(checkedList.get(i).getListName());
						userLogger.warn( "list_id="
								+ checkedList.get(i).getListId() + " is assotiated with an active campaign");
						break;
					case LIST_NOT_FOUND:
						if (intraListToDelete) {
							listStatus.add(String.valueOf("intraListToDelete"));
							userLogger.info( "intraListToDelete flag value is : (" + intraListToDelete + ").");
						}
						userLogger.warn(  "list_id="
								+ checkedList.get(i).getListId() + " not found");
						break;
					default:
						if (intraListToDelete) {
							listStatus.add(String.valueOf("intraListToDelete"));
							userLogger.info( "intraListToDelete flag value is : (" + intraListToDelete + ").");
						}
						userLogger.error(  "undefined result" + resp
								+ " while deleting list_id=" + checkedList.get(i).getListId());
						break;
					}// end switch
				}// end for

				if ((listStatus.size() + listOfSucessList.size()) > 0) {
					if (listOfSucessList.size() == 0) {
						String listOfListsId = "";
						for (int j = 0; j < listStatus.size(); j++) {
							listOfListsId = listOfListsId + " - " + listStatus.get(j);
						}

						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_can_not_be_deleted")) + " " + listOfListsId, null));
					} else if (listStatus.size() == 0) {
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
										.getLocalizedLabel("deleted_successfully")), null));
					} else {
						String listOfListsId = "";
						for (int j = 0; j < listStatus.size(); j++) {
							listOfListsId = listOfListsId + " - " + listStatus.get(j);
						}
						String listOfsucc = "";
						for (int j = 0; j < listOfSucessList.size(); j++) {
							listOfsucc = listOfsucc + " - " + listOfSucessList.get(j);
						}
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_can_not_be_deleted"))
										+ " "
										+ listOfListsId
										+ " but "
										+ listOfsucc
										+ " "
										+ String.format(Util.getLocalizedLabel("deleted_successfully")), null));
					}// end else
				}// end if

				if (listOffail.size() > 0) {
					String listOfFail = "";
					for (int j = 0; j < listOffail.size(); j++) {
						listOfFail = listOfFail + " " + listOffail.get(j);
					}
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("list_deleted_failure")) + " " + listOfFail, null));
				}
			} else {

				if (intraListToDelete) {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("can't_remove_intra_list")), null));
				} else {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("choose_list_to_be_deleted")), null));
				}
			}
		} catch (Exception e) {
			userLogger.error( "Error while deleting list " + deletedListId + " )", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	public void getAllListInfo() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info( "Get all lists");
			List<ListType> typeList = new ArrayList<ListType>();
			typeList.add(ListType.INTRA_LIST);
			typeList.add(ListType.INTRA_SUB_LIST);
			typeList.add(ListType.NORMAL_LIST);
			typeList.add(ListType.VIRTUAL_LIST);
			typeList.add(ListType.CUSTOMIZED_LIST);
			ContactListInfoResultSet resultSet = listsManegementServicePort.getContactListsInfo(userInfo, typeList);
			ResponseStatus resStatus = resultSet.getStatus();

			userLogger.info("listsManegementServicePort response is:(" + resStatus + ").");

			switch (resStatus) {
			case SUCCESS:
				userList = resultSet.getContactListInfoResultSet();
				userLogger.info( "list of contact lists is populating successfully with count=" + userList.size());
				break;
			case FAIL:
				userLogger.error( "list of contact lists is not populated");
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.warn( " is not allowed to display lists of contact lists");
				break;
			default:
				userLogger.error("Unknown response status " + resultSet.getStatus()
						+ " , trying to populate list of contact lists");
				break;
			}
		} catch (Exception e) {
			userLogger.error( "Error while getting all lists info for copy from anther list menu", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	// update list--------------------------------------------------------

	public static boolean isAlphnum(String sender) {
		return (!sender.matches("[0-9]+"));
	}

	private boolean checkRequiredField() {
		if (firstName.trim().isEmpty() && msisdn.trim().isEmpty()) {

			return true;
		} else if (msisdn.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("msisdn_required")), null));
			userLogger.info( "MSISDN field is empty");
			return false;
		}
		return true;
	}

	public void saveContact() {
		Contact contact = new Contact();
		if (checkRequiredField() && validateCustomizeValues()) {
			if (firstName.trim().isEmpty() && msisdn.trim().isEmpty() && newcontactList.size() > 0) {
				// more than one contact
			} else if (SMSUtils.validateLocalAddress(msisdn) || SMSUtils.validateInternationalAddress(msisdn)) {
				contact.setFirstName(firstName);
				contact.setMsisdn(msisdn);
				contact.setValue1(value1);
				contact.setValue2(value2);
				contact.setValue3(value3);
				contact.setValue4(value4);
				contact.setValue5(value5);
				firstName = "";
				msisdn = "";
				value1 = "";
				value2 = "";
				value3 = "";
				value4 = "";
				value5 = "";

				// userLogger.info(userLogInfo(user.getAccountId(),
				// user.getUsername()) + "contact : " + contact);
				newcontactList.add(contact);

			} // end if mobile number validation
			else {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("msisdn_invalid")), null));
				userLogger.info( "entered invalid msisdn ");
			}
		}// end big if checkValidation
	}// end of function

	private boolean validateCustomizeValues() {
		if (customizedlist) {
			if (value1.trim().isEmpty() && value2.trim().isEmpty() && value3.trim().isEmpty()
					&& value4.trim().isEmpty() && value5.trim().isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("values_required")), null));
				userLogger.error( "customize list without values ");
				return false;
			} else { // at least one value is not empty
				return true;
			}
		} else {
			userLogger.info( "not customized list ");
			return true;
		} // not customized no need to validate values
	}

	public void addContact() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info("Adding contact to existing list_id=" + listIdForSubList);

			if (checkRequiredField() && validateCustomizeValues()) {
				if (firstName.trim().isEmpty() && msisdn.trim().isEmpty() && newcontactList.size() > 0) {
					saveContact();
					ContactList contactObj = new ContactList();
					ContactListInfo info = new ContactListInfo();
					info.setListId(Integer.valueOf(listIdForSubList));
					info.setListName(subListName);
					if (customizedlist) {
						info.setListType(ListType.CUSTOMIZED_LIST);
						// userLogger.info("set type to customized List");
					} else if (listofLists.get(Integer.valueOf(listIdForSubList)).getListType()
							.equals(ListType.VIRTUAL_LIST)) {
						info.setListType(ListType.VIRTUAL_LIST);
					}

					else
						info.setListType(ListType.NORMAL_LIST);

					info.setContactsCount(newcontactList.size());
					contactObj.setListInfo(info);

					String newMsisdn = "( ";

					for (int i = 0; i < newcontactList.size(); i++) {
						contactObj.getListContacts().add(newcontactList.get(i));
						newMsisdn = newMsisdn + newcontactList.get(i).getMsisdn() + "-";
					}

					userLogger.debug( "Adding new contact/contacts "
							+ newMsisdn + " )" + " to list_id=" + listIdForSubList);

					ContactResultSet result = listsManegementServicePort.expandContactList(userInfo, contactObj);
					userLogger.info( "listsManegementServicePort response is:(" + result + ").");
					ResponseStatus resp = result.getStatus();
					switch (resp) {
					case SUCCESS:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
										.getLocalizedLabel("list_edit_successfully")), null));
						userLogger.info( "contact is added to list_id="
								+ listIdForSubList + "successfully");
						newcontactList.clear();
						populateList();
						break;
					case FULLY_UPDATED:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
										.getLocalizedLabel("list_edit_successfully")), null));
						userLogger.info( "contact is added to list_id="
								+ listIdForSubList + "successfully");
						newcontactList.clear();
						populateList();
						break;
					case FAIL:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_edit_failure")), null));
						userLogger.error( "error while adding contact to list_id=" + listIdForSubList);
						newcontactList.clear();
						break;
					case INELIGIBLE_ACCOUNT:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("ineligible_add_contact")), null));
						userLogger.warn( "can't add new contact to list_id=" + listIdForSubList);
						newcontactList.clear();
						break;
					case PARTIAL_UPDATE:
						String duplicatListIds = "";
						for (int i = 0; i < result.getContacts().size(); i++) {
							duplicatListIds = duplicatListIds + "/";
						}
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("contact_already_exist")) + duplicatListIds, null));
						userLogger.info( "contact(s) already exists in list_id=" + listIdForSubList);
						populateList();
						break;
					case DUPLICATE_CONTACT:
						String duplicatContacts = "";
						for (int i = 0; i < result.getContacts().size(); i++) {
							duplicatListIds = duplicatContacts + "/";
						}
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("contact_already_exist")) + duplicatContacts, null));
						userLogger.warn( "contact(s) already exists in list_id=" + listIdForSubList);
						break;
					case LOCKED_LIST:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("locked_list")), null));
						userLogger.warn( "list_id=" + listIdForSubList
								+ " can not be edited");
						newcontactList.clear();
						break;
					case LIST_NOT_FOUND:
						userLogger.warn(  "list_id=" + listIdForSubList);
						newcontactList.clear();
						break;
					default:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_edit_failure")), null));
						userLogger.error(  "undefined result status"
								+ resp + " while adding contact to list_id=" + listIdForSubList);
						newcontactList.clear();
						break;

					}
				} else {
					// userLogger.debug("msisdn: " + msisdn);
					// userLogger
					// .debug("SMSUtils.validateLocalAddress(msisdn) || SMSUtils.validateInternationalAddress(msisdn) "
					// + SMSUtils.validateLocalAddress(msisdn)
					// + SMSUtils.validateInternationalAddress(msisdn));
					if (SMSUtils.validateLocalAddress(msisdn) || SMSUtils.validateInternationalAddress(msisdn)) {
						saveContact();
						ContactList contactObj = new ContactList();
						ContactListInfo info = new ContactListInfo();
						info.setListId(Integer.valueOf(listIdForSubList));
						info.setListName(subListName);

						if (customizedlist) {
							// userLogger.info("cutomized list.");
							info.setListType(ListType.CUSTOMIZED_LIST);
						} else
							info.setListType(ListType.NORMAL_LIST);
						contactObj.setListInfo(info);

						String newMsisdn = "( ";
						for (int i = 0; i < newcontactList.size(); i++) {
							contactObj.getListContacts().add(newcontactList.get(i));
							newMsisdn = newMsisdn + newcontactList.get(i).getMsisdn() + "-";
						}

						userLogger.debug(  "Adding new contact/contacts "
								+ newMsisdn + " )" + " to list_id=" + listIdForSubList);

						ContactResultSet result = listsManegementServicePort.expandContactList(userInfo, contactObj);

						ResponseStatus resp = result.getStatus();
						userLogger.info("listsManegementServicePort response is:(" + resp + ").");
						switch (resp) {
						case SUCCESS:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
											.getLocalizedLabel("list_edit_successfully")), null));
							userLogger.info("contact is added to list_id=" + listIdForSubList + " successfully");
							newcontactList.clear();
							populateList();
							break;
						case FULLY_UPDATED:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
											.getLocalizedLabel("list_edit_successfully")), null));
							userLogger.info("contact is added to list_id=" + listIdForSubList + " successfully");
							newcontactList.clear();
							populateList();
							break;
						case FAIL:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("list_edit_failure")), null));
							userLogger.error( "error while adding contact to list_id=" + listIdForSubList);
							newcontactList.clear();
							break;
						case INELIGIBLE_ACCOUNT:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("ineligible_add_contact")), null));
							userLogger.warn( "can't add new contact to list_id=" + listIdForSubList);
							newcontactList.clear();
							break;
						case PARTIAL_UPDATE:
							String duplicatListIds = "";
							for (int i = 0; i < result.getContacts().size(); i++) {
								duplicatListIds = duplicatListIds + "/";
							}
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("contact_already_exist")) + duplicatListIds, null));
							userLogger.info( "contact/contacts already exists in list_id=" + listIdForSubList);
							populateList();
							break;
						case DUPLICATE_CONTACT:
							String duplicatContacts = "";
							for (int i = 0; i < result.getContacts().size(); i++) {
								duplicatListIds = duplicatContacts + "/";
							}
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("contact_already_exist")) + duplicatContacts, null));
							userLogger.warn( "contact/contacts already exists for list_id=" + listIdForSubList);
							break;
						case LOCKED_LIST:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("locked_list")), null));
							userLogger.warn( "list_id="
									+ listIdForSubList + " can not be edited");
							newcontactList.clear();
							break;
						case LIST_NOT_FOUND:
							userLogger.warn(  "list_id="
									+ listIdForSubList + " not found");
							newcontactList.clear();
							break;
						default:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("list_edit_failure")), null));
							userLogger.error( "error while adding contact to list_id=" + listIdForSubList);
							newcontactList.clear();
							break;

						}
					} else {
						userLogger.error(  "Invalid msisdn "+ " for list with id=" + listIdForSubList);

						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("msisdn_invalid")), null));
					}
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_edit_failure")), null));
			userLogger.info( "error while adding contact to list " + listIdForSubList);
			userLogger.error("Error while adding new contact to list_id=" + listIdForSubList, e);
			newcontactList.clear();
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void deleteContactsFromList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info( " deleteing contacts from list" + listIdForSubList);

			ContactList deletedContactList = new ContactList();
			ContactListInfo info = new ContactListInfo();
			info.setListId(Integer.valueOf(listIdForSubList));
			info.setListName(subListName);
			deletedContactList.setListInfo(info);

			String deletedContactsMsisdn = "";
			for (Map.Entry<String, Boolean> e : checkedSubList.entrySet()) {
				String key = e.getKey();
				Boolean value = e.getValue();
				if (value.equals(true)) {
					for (int i = 0; i < contactsList.size(); i++) {
						if (contactsList.get(i).getMsisdn().equals(key)) {
							deletedContactList.getListContacts().add(contactsList.get(i));
							deletedContactsMsisdn = "(" + deletedContactsMsisdn + "/" + contactsList.get(i).getMsisdn();
						}
					}
				}
			}

			userLogger.debug( "deleted contacts from list_id=" + listIdForSubList + " countacts " + encyrptionUtil.encrypt(deletedContactsMsisdn)
					+ " )");

			ResultStatus status = listsManegementServicePort.deleteSubContactList(userInfo, deletedContactList);
			ResponseStatus response = status.getStatus();
			userLogger.info("listsManegementServicePort response is:(" + response + ").");

			switch (response) {
			case SUCCESS:
				for (int i = 0; i < deletedContactList.getListContacts().size(); i++) {
					contactsList.remove(deletedContactList.getListContacts().get(i));
				}

				populateList();

				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
								.getLocalizedLabel("contact_delete_successfully")), null));
				userLogger.info("contact/contacts is deleted from list " + listIdForSubList + " successfully");
				break;
			case INELIGIBLE_ACCOUNT:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("ineligible_delete_contact")), null));
				userLogger.warn( "user " + user.getAccountId() + " can't delete contact/contacts from list "
						+ listIdForSubList);
				break;
			case LOCKED_LIST:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("locked_list")), null));
				userLogger.warn( "list " + listIdForSubList + " can not be edited");
				break;
			case LIST_NOT_FOUND:
				userLogger.warn( "list " + listIdForSubList + " not found");
				break;
			case FAIL:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_edit_failure")), null));
				userLogger.error( "error while delteing contact/contacts from list " + listIdForSubList);
				break;
			default:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_edit_failure")), null));
				userLogger.error( "undefined status" + response + " while delteing contact/contacts from list "
						+ listIdForSubList);
				break;
			}

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_edit_failure")), null));

			userLogger.error("error while delteing contact/contacts from list " + listIdForSubList, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void searchForMsisdn() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		
		userInfo.setUser(user);
		try {
	        ThreadContext.push(trxId);
			userLogger.info("Searching for msisdn in contact list_id=" + listIdForSubList);
			searchMode = true;

			if (searchMsisdn != null) {
				if (!searchMsisdn.trim().isEmpty()) {
					userLogger.debug( "Searching for msisdn in contact list_id=" + listIdForSubList);
					ContactResultSet result = listsManegementServicePort.searchContacts(userInfo, searchMsisdn,
							Integer.valueOf(listIdForSubList));
					ResponseStatus state = result.getStatus();
					userLogger.info( "listsManegementServicePort response is:(" + state + ").");
					switch (state) {
					case SUCCESS:
						contactsList = result.getContacts();
						userLogger.info( "searching for "
								+ searchMsisdn + " in list_id=" + listIdForSubList + " found successfully count="
								+ contactsList.size());
						break;
					case CONTACTS_NOT_FOUND:
						userLogger.warn(  "searching for "
								+ searchMsisdn + " in list_id=" + listIdForSubList + " is unfound");
						break;
					case INVALID_REQUEST:
						userLogger.warn( "invalid request while searching for msisdn in list with id"
								+ listIdForSubList);
						break;
					case INELIGIBLE_ACCOUNT:
						userLogger.warn(" is not allowed to search for msisdn in list with id="
								+ listIdForSubList);
					case FAIL:
						userLogger.error(  "Fail while searching for "
								+ searchMsisdn + " in list_id=" + listIdForSubList);
						break;
					default:
						userLogger.error( "undefined result=" + state
								+ " while searching for msisdn in list=" + listIdForSubList);
						break;
					}
				} else {
					populateSubList();
				}
			} else {
				populateSubList();
			}
		} catch (Exception e) {
			userLogger.error( "Error while searching for msisdn in list=" + listIdForSubList, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	// Export list----------------------------------------------------

	public void checkIfUserChecked() {
		try {
			checkedListsNew.clear();
			for (Map.Entry<Integer, Boolean> e : checked.entrySet()) {
				Integer key = e.getKey();
				Boolean value = e.getValue();
				if (value.equals(true)) {
					for (int i = 0; i < listofLists.size(); i++) {
						if (listofLists.get(i).getListId().equals(key)) {
							checkedListsNew.add(listofLists.get(i));
						}
					}
				}
			}
			if (checkedListsNew.size() == 0) {
				errorButton = true;
				exportButton = false;
			} else if (checkedListsNew.size() > 1) {
				errorButton = true;
				exportButton = false;
			} else {
				exportButton = true;
				errorButton = false;
			}
		} catch (Exception e) {
			userLogger.error( "error while checking if user checked any lists to export");
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public void showErroMessage() {
		if (checkedListsNew.size() == 0) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_one_list_for_export")), null));
			userLogger.info( "didn't choose any list to export");
		} else if (checkedListsNew.size() > 1) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_one_list_for_export")), null));
			userLogger.info(userLogInfo(user.getAccountId(), user.getUsername())
					+ "chose many lists to export instead of one");
		}
		checkedListsNew.clear();
	}

	public void readFileFromServer() {
		try {
			userLogger.info( "set list_id="
					+ checkedListsNew.get(0).getListId() + " in session for exporting");
			UserTrxInfo userInfo = new UserTrxInfo();
			userInfo.setTrxId(TrxId.getTrxId());
			userInfo.setUser(user);

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			session.setAttribute("listId", checkedListsNew.get(0).getListId());
			session.setAttribute("UserInfo", userInfo);

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							String.format(Util.getLocalizedLabel("export_error")), null));
			userLogger.error( "Error while setiing list_id="
					+ checkedListsNew.get(0).getListId() + " in session for exporting list", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	// Upload file to create list methods
	// ------------------------------------------------------------------------------------

	public void getSelectedFileType(ValueChangeEvent e) {
		fileType = e.getNewValue().toString();
		if (fileType.equals("none")) {

			userLogger.info( "didn't choose file type to upload from");
		} else {
			userLogger.info( "uploaded file with type="
					+ fileType);
		}
	}

	public void submit() {
		UserTrxInfo userInfo = new UserTrxInfo();
	try{
		String trxId = TrxId.getTrxId();
		ThreadContext.push(trxId);

		userInfo.setTrxId(trxId);
		userInfo.setUser(user);
	//	String trxId = logTrxId(userInfo.getTrxId());
		if (uploadedFile != null) {
			userLogger.info( "File is uploaded");
			// Prepare filename prefix and suffix for an unique filename in
			// upload
			// folder.
			String prefix = FilenameUtils.getBaseName(uploadedFile.getName());
			String suffix = FilenameUtils.getExtension(uploadedFile.getName());
			int max = (Integer) Configs.UPLOAD_FILE_SIZE.getValue(); // 500MB =
																		// 500 *
																		// 1024
																		// *
																		// 1024

			userLogger
					.info( "uploading file maximum size=" + max);

			if (uploadedFile.getSize() > (max * 1024 * 1024)) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("file_too_large")), null));
				userLogger.info("uploaded file is too larg "
						+ uploadedFile.getSize());
			} else {
				// Prepare file and outputstream.
				file = null;
				OutputStream output = null;

				try {
					// Create file with unique name in upload folder and write
					// to
					// it.
					file = File.createTempFile(prefix + "File" + "_", "." + suffix, new File(proBaseDir));
					output = new FileOutputStream(file);
					InputStream inputStream = uploadedFile.getInputStream();
					IOUtils.copy(inputStream, output);
					fileName = file.getName();

					userLogger.info( "uploded file with name "
							+ fileName + " to create list");

					readFile(trxId);
					FacesContext facesContext = FacesContext.getCurrentInstance();
					HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
					session.setAttribute("UploadTrxId", trxId);
					List<String> filesNames = (List<String>) session.getAttribute("filesNames");
					if (filesNames == null || filesNames.isEmpty()) {
						filesNames = new ArrayList<String>();
					}
					filesNames.add(fileName);
					session.setAttribute("filesNames", filesNames);
					for (String string : filesNames) {
						userLogger.trace( "user uploaded files names : " + string);
					}
				} catch (IOException e) {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("upload_right_file")), null));
					userLogger.error("Error while uploading file ", e);
                                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
				} finally {
					userLogger.info( "closing output stream for file uploading");
					IOUtils.closeQuietly(output);
				}
			}
		} else {
			userLogger.info( " didn't upload file");
			}
		} finally {
			ThreadContext.pop();
		}
	
	}

	private void readFile(String trxId) {
		mapOfColumn = new HashMap<Integer, String>();
		columnsId = new ArrayList<String>();
		myNewList = new ArrayList<List<String>>();
		if (fileType.equals("xls") || fileType.equals("xlsx")) {
			userLogger.info(" choose an excel file type to upload");
			readExcelSheet(trxId);
		} else if (fileType.equals("csv")) {
			userLogger
					.info( " choose an csv file type to upload");
			readCSVFile(trxId);

		} else if (fileType.equals("vcf")) {
			userLogger
					.info( " choose an vcf file type to upload");
			readVCFFile(trxId);
		} else {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_valid_file_type")), null));
			userLogger.info( "chose undefined file type to upload");
		}
	}

	private void readCSVFile(String trxId) {
		try {
			validFile = true;
			userLogger.info( "Reading upload file as csv file name=" + fileName);

			recordsList = new ArrayList<Map<Integer, String>>();
			valuesMap = new LinkedHashMap<String, String>();
			valuesMap.put("none", String.format(Util.getLocalizedLabel("none")));
			valuesMap.put("name", String.format(Util.getLocalizedLabel("name")));
			valuesMap.put("mobile", String.format(Util.getLocalizedLabel("mobile")));

			userLogger.info("Reading upload file as csv file name=" + fileName + " using csv_delemiter="
					+ ((String) Configs.CSV_DELEMITER.getValue()) + ". Values Map  : " + valuesMap.entrySet());

			// reading unicode utf-8 and asci ONLY
			CSVBatchFile csvFile = new CSVBatchFile(file, ((String) Configs.CSV_DELEMITER.getValue()), "",
					StandardOpenOption.READ);
			CSVRecord headerRecord = csvFile.readCSVRecord();
			List<String> getRowList;

			for (int i = 0; i < headerRecord.getNumberOfFields(); i++) {
				columnsId.add(String.valueOf(i));
			}

			/**
			 * iteration to read all file and validate it against XSS
			 */
			CSVRecord records;
			List<List<String>> newList = new ArrayList<List<String>>();
			records = csvFile.readCSVRecord();

			while (records != null) {
				getRowList = new ArrayList<String>();
				for (Field field : records.getFields()) {
					getRowList.add(field.getValue());
				}
				newList.add(getRowList);
				records = csvFile.readCSVRecord();
			}

			// validate uploaded file (only first ten rows) against XSS
			z: for (int i = 0; i < newList.size(); i++) {
				for (int j = 0; j < newList.get(i).size(); j++)
					if (!validateXSS(newList.get(i).get(j))) {
						validFile = false;
						userLogger.info(trxId + userLogInfo(user.getAccountId(), user.getUsername())
								+ "Uploaded XLS/XLSX file has xss!");
						break z;
					}
			}

			/**
			 * normal iteration to read first num of records to view for user as
			 * sample
			 */
			userLogger.info( "Number of rows to be viewed as sample while uplading list from an csv filename " + fileName
					+ " count=" + ((Integer) Configs.UPLOAD_NUM_RECORDS.getValue()));
			if (validFile) {
				csvFile = new CSVBatchFile(file, ((String) Configs.CSV_DELEMITER.getValue()), "",
						StandardOpenOption.READ);
				headerRecord = csvFile.readCSVRecord();
				getRowList = new ArrayList<String>();
				CSVRecord record;
				int count;
				for (record = csvFile.readCSVRecord(), count = ((Integer) Configs.UPLOAD_NUM_RECORDS.getValue()); record != null
						&& count > 0; record = csvFile.readCSVRecord(), count--) {

					getRowList = new ArrayList<String>();
					for (Field field : record.getFields()) {
						getRowList.add(field.getValue());
					}
					myNewList.add(getRowList);
				}

				if (columnsId.size() > 0) {
					userLogger.info( "Uploaded file name "
							+ fileName + " as excel type is not empty, it contains columns_count=" + columnsId.size());
					uploadTableFalg = true;
				} else {
					userLogger.info( "Uploaded file name "
							+ fileName + " as csv type is empty");
				}

				csvFile.close();
				userLogger.info( "Done reading csv file name="
						+ fileName);
			} else {
				uploadTableFalg = false;
				userLogger.info( "invalid csv file/ csv file contains XSS");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_file_content")), null));
			}
		} catch (Exception e) {
			userLogger.error( "Error while reading csv file name=" + fileName, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	private void readExcelSheet(String trxId) {
		try {
			validFile = true;
			userLogger.info("Reading upload file as excel file name " + fileName);

			recordsList = new ArrayList<Map<Integer, String>>();
			valuesMap = new LinkedHashMap<String, String>();
			valuesMap.put("none", String.format(Util.getLocalizedLabel("none")));
			valuesMap.put("name", String.format(Util.getLocalizedLabel("name")));
			valuesMap.put("mobile", String.format(Util.getLocalizedLabel("mobile")));
			valuesMap.put("Value1", String.format(Util.getLocalizedLabel("value1")));
			valuesMap.put("Value2", String.format(Util.getLocalizedLabel("value2")));
			valuesMap.put("Value3", String.format(Util.getLocalizedLabel("value3")));
			valuesMap.put("Value4", String.format(Util.getLocalizedLabel("value4")));
			valuesMap.put("Value5", String.format(Util.getLocalizedLabel("value5")));

			// userLogger.info(userLogInfo(user.getAccountId(),
			// user.getUsername()) + "Values Map : "
			// + valuesMap.entrySet());
			Iterator<Row> rowIterator = null;
			List<String> getRowList;
			FileInputStream myFile = new FileInputStream(file);

			if (fileType.equals("xlsx")) {
				userLogger.info( "Reading upload file name "
						+ fileName + " as excel file with xlsx extention");

				OPCPackage pkg = OPCPackage.open(myFile);
				XSSFReader reader = new XSSFReader(pkg);

				MySheetContentHandler sheetHandler = new MySheetContentHandler();

				StylesTable styles = reader.getStylesTable();
				ReadOnlySharedStringsTable sharedStrings = new ReadOnlySharedStringsTable(pkg);
				ContentHandler handler = new XSSFSheetXMLHandler(styles, sharedStrings, sheetHandler, true);

				/**
				 * sheet handler to read all sheet not only first 10 rows to
				 * validate against XSS and decide view and send to backEnd or
				 * not.
				 */
				class MySheetContentHandlerForAll implements XSSFSheetXMLHandler.SheetContentsHandler {
					List<String> getRowList;
					List<List<String>> myNewList = new ArrayList<List<String>>();
					List<String> columnsId = new ArrayList<String>();
					int count;
					int columnCount = 0;

					@Override
					public void cell(String cellReference, String formattedValue, XSSFComment paramXSSFComment) {// String
																													// cellReference,
																													// String
																													// formattedValue)
																													// {
						getRowList.add(formattedValue);
						if (count == 0) {
							columnsId.add(String.valueOf(columnCount++));
						}
					}

					@Override
					public void endRow(int paramInt) {
						myNewList.add(getRowList);
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
						return myNewList;
					}

					public List<String> getColumnsNumList() {
						return columnsId;
					}

					@Override
					public void headerFooter(String paramString1, boolean paramBoolean, String paramString2) {
					}
				}

				MySheetContentHandlerForAll sheetHandlerforAll = new MySheetContentHandlerForAll();

				ContentHandler handlerForAllFile = new XSSFSheetXMLHandler(styles, sharedStrings, sheetHandlerforAll,
						true);

				XMLReader parser = XMLReaderFactory.createXMLReader();
				parser.setContentHandler(handlerForAllFile);

				userLogger.info("Parsing uploaded file fileName=" + fileName);
				try {
					parser.parse(new InputSource(reader.getSheetsData().next()));
				} catch (TerminateSaxParserException e) {
					userLogger.error("Exception while parsing xlsx file fileName=" + fileName+". "+e.getMessage());
					appLogger.error( "Exception while parsing xlsx file fileName=" + fileName, e);
                    appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.FAILED_OPERATION, "Terminate Sax Parser");
				}

				userLogger.info("Done parsing uploaded file fileName=" + fileName);

				List<List<String>> newList = sheetHandlerforAll.getRecordsList();

				userLogger.info( " uploaded file has : ("
						+ newList.size() + ") row.");

				// validate uploaded XLSX file against XSS
				z: for (int i = 0; i < newList.size(); i++) {
					for (int j = 0; j < newList.get(i).size(); j++)
						if (!validateXSS(newList.get(i).get(j))) {
							validFile = false;
							userLogger.info( "Uploaded XLSX file has xss!");
							break z;
						}
				}

				if (validFile) {
					uploadTableFalg = true;

					parser = XMLReaderFactory.createXMLReader();
					parser.setContentHandler(handler);

					userLogger.info("Parsing uploaded file fileName=" + fileName);
					try {
						parser.parse(new InputSource(reader.getSheetsData().next()));
					} catch (TerminateSaxParserException e) {
						userLogger.error("Exception while parsing xlsx file fileName=" + fileName+". "+ e.getMessage());
						appLogger.error( "Exception while parsing xlsx file fileName=" + fileName, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.FAILED_OPERATION, "Terminate Sax Parser");
					}

					userLogger.info("Done parsing uploaded file fileName=" + fileName);

					userLogger.info( "Number of rows to be viewed as sample while uploading list from an excel filename="
							+ fileName + " count=" + ((Integer) Configs.UPLOAD_NUM_RECORDS.getValue()));
					myNewList = sheetHandler.getRecordsList();
					columnsId = sheetHandler.getColumnsNumList();
					pkg.close();
				}

				else {
					uploadTableFalg = false;
					userLogger.info( "invalid file ");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_file_content")), null));

				}
			} else if (fileType.equals("xls")) {
				userLogger.info( "Reading upload file name "
						+ fileName + " as excel file with xls extention");

				// Get the workbook instance for XLS file
				HSSFWorkbook workbook = new HSSFWorkbook(myFile);

				// Get first sheet from the workbook
				HSSFSheet sheet = workbook.getSheetAt(0);

				// Get iterator to all the rows in current sheet
				rowIterator = sheet.iterator();
				// iterate to read all sheet and validate against xss
				List<List<String>> newList = new ArrayList<List<String>>();
				while (rowIterator.hasNext()) {
					getRowList = new ArrayList<String>();
					Row row = rowIterator.next();
					int num = row.getPhysicalNumberOfCells();
					for (int i = 0; i < num; i++) {
						if (row.getCell(i) != null) {
							if (row.getCell(i).getCellType() == Cell.CELL_TYPE_NUMERIC) {
								getRowList.add(String.valueOf(Math.round(row.getCell(i).getNumericCellValue())));
							} else {
								getRowList.add(row.getCell(i).getStringCellValue());

							}
						}
					}
					newList.add(getRowList);
				}
				// TODO validate uploaded file against XSS
				z: for (int i = 0; i < newList.size(); i++) {
					for (int j = 0; j < newList.get(i).size(); j++) {
						if (!validateXSS(newList.get(i).get(j))) {
							validFile = false;
							userLogger.info( "Uploaded XLS/XLSX file has xss!");
							break z;
						}
					}
				}
				if (validFile) {
					rowIterator = sheet.iterator();
					while (rowIterator.hasNext()) {
						Row row = rowIterator.next();
						if (row.getRowNum() == 0) {
							getRowList = new ArrayList<String>();
							int num = row.getPhysicalNumberOfCells();

							for (int i = 0; i < num; i++) {
								columnsId.add(String.valueOf(i));
								if (row.getCell(i) != null) {
									if (row.getCell(i).getCellType() == Cell.CELL_TYPE_NUMERIC) {
										getRowList
												.add(String.valueOf(Math.round(row.getCell(i).getNumericCellValue())));
									} else {
										getRowList.add(row.getCell(i).getStringCellValue());

									}
								}
							}

							myNewList.add(getRowList);

							userLogger
									.info( "Number of rows to be viewed as sample while uplading list from an excel filename "
											+ fileName + " count=" + ((Integer) Configs.UPLOAD_NUM_RECORDS.getValue()));

						} else if ((row.getRowNum() < (Integer) Configs.UPLOAD_NUM_RECORDS.getValue())) {
							getRowList = new ArrayList<String>();
							for (int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
								if (row.getCell(i) != null) {
									if (row.getCell(i).getCellType() == Cell.CELL_TYPE_NUMERIC) {
										getRowList
												.add(String.valueOf(Math.round(row.getCell(i).getNumericCellValue())));
									} else {
										getRowList.add(row.getCell(i).getStringCellValue());

									}
								}
							}

							myNewList.add(getRowList);

						}
					}

					if (columnsId.size() > 0) {
						userLogger.info( "Uploaded fileName="
								+ fileName + " as excel type is not empty, it contains columns_count="
								+ columnsId.size());
						uploadTableFalg = true;
					} else {
						userLogger.info( "Uploaded fileName="
								+ fileName + " as excel type is empty");
					}

					myFile.close();

					userLogger.info( "Done reading excel sheet with name=" + fileName);
				} else {
					uploadTableFalg = false;
					userLogger.info( "invalid file ");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_file_content")), null));

				}

			}
		} catch (FileNotFoundException e) {
			userLogger.error( "Error while reading excel sheet with name=" + fileName + " file not found", e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_valid_file_type")), null));
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.INVALID_OPERATION, "File Not Found");
		} catch (IOException e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_valid_file_type")), null));
			userLogger.error( "Error while reading excel sheet with name=" + fileName + " IO exception", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.IO_ERROR, "IO failure");
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_valid_file_type")), null));
			userLogger.error("Error while reading excel sheet with name=" + fileName, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public void getListNameToFile() {
		try {
			userLogger.info( "Set list_name=" + listName
					+ " in session to be used in creating list from uploaded file");

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			session.setAttribute("listName", listName);
		} catch (Exception e) {
			userLogger.error( "Error while setting list_name="
					+ listName + "in session to be used in creating list from uploaded file ", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public void createListFromUpload(boolean customizeList) {
		String trxId = null;
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			 trxId = (String) session.getAttribute("UploadTrxId");
			if (trxId == null) {
				trxId = TrxId.getTrxId();
//				userInfo.setTrxId(trxId);
//				userInfo.setUser(user);
//				trxId = logTrxId(userInfo.getTrxId());
			}
//			else {
////				userInfo.setTrxId(trxId);
////				userInfo.setUser(user);
//			}
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info(  "Creating list by uploading file");
			userLogger.info( "Create customize List flag: "
					+ customizeList);
			createCustomizeList = customizeList;
			String listNameNew = (String) session.getAttribute("listName");

			// session.removeAttribute("listName");
			if (listNameNew == null || listNameNew.trim().isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_name_required")), null));
				userLogger.info( "list name field is empty");
				fileUploadName = "";
			} else if (fileType.equals("none")) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("select_file_type")), null));
				userLogger.info("User didn't choose file type to upload");
				fileUploadName = "";
			} else if (file == null) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("choose_file_to_upload")), null));
				userLogger.info("User didn't choose file to upload");
				fileUploadName = "";
			} else if (msisdnMap.trim().isEmpty() || msisdnMap.equals("undefined")) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("no_mapping")), null));
				userLogger.info("User didn't choose name or msisdn columns");

			}

			else if (createCustomizeList && value1Map.equals("-1") && value2Map.equals("-1") && value3Map.equals("-1")
					&& value4Map.equals("-1") && value5Map.equals("-1")) {

				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_customize")), null));
				userLogger.info( "User didn't choose any values");

			} else {
				FileUploadClient fileUpload = new FileUploadClient();
				String token;
				try {
					InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
					token = fileUpload.upload((String) Configs.UPLOAD_FILE_SERVER_LINK.getValue(), inputStream);
					if (token != null) {
						FileDetails fileDetails = new FileDetails();
						fileDetails.setFileToken(token);
						fileDetails.setFileType(choosenUploadFileType.toUpperCase());
						if (nameMap != null && !nameMap.equals("")) {
							fileDetails.setFnameColNumber(Integer.valueOf(nameMap));
						}
						fileDetails.setMsisdnColNumber(Integer.valueOf(msisdnMap));
						fileDetails.setLnameColNumber(-1);
						fileDetails.setListName(listNameNew);
						if (value1Map != null && !value1Map.equals(""))
							fileDetails.setValue1ColNumber(Integer.valueOf(value1Map));
						else
							fileDetails.setValue1ColNumber(-1);
						if (value2Map != null && !value2Map.equals(""))
							fileDetails.setValue2ColNumber(Integer.valueOf(value2Map));
						else
							fileDetails.setValue2ColNumber(-1);

						if (value3Map != null && !value3Map.equals(""))
							fileDetails.setValue3ColNumber(Integer.valueOf(value3Map));
						else
							fileDetails.setValue3ColNumber(-1);

						if (value4Map != null && !value4Map.equals(""))
							fileDetails.setValue4ColNumber(Integer.valueOf(value4Map));
						else
							fileDetails.setValue4ColNumber(-1);

						if (value5Map != null && !value5Map.equals(""))
							fileDetails.setValue5ColNumber(Integer.valueOf(value5Map));
						else
							fileDetails.setValue5ColNumber(-1);
						userLogger.info( "File Details: " + fileDetails);
						// userLogger.debug(logTrxId(userInfo.getTrxId()) +
						// " name map value: "+ nameMap+ "fileDetails : "
						// + "1: " + fileDetails.getValue1ColNumber() + " 2: "
						// +fileDetails.getValue2ColNumber()
						//
						// + " 3: " +fileDetails.getValue3ColNumber() + " 4: "
						// +fileDetails.getValue4ColNumber()
						// + " 5: " +fileDetails.getValue5ColNumber()
						// +" first: " + fileDetails.getFnameColNumber()
						// + " last: " +fileDetails.getLnameColNumber() +
						// " number: " + fileDetails.getMsisdnColNumber());

						userLogger.debug("creating list from uploading file [file_token=" + token + ",file_type="
								+ choosenUploadFileType + ",name_map=" + nameMap + ",msisdn=" + msisdnMap
								+ ",list_name=" + listNameNew + "]");

						FileResult result = listsManegementServicePort.createNewListFromFile(userInfo, fileDetails);
						com.edafa.web2sms.service.enums.ResponseStatus response = result.getStatus();
						userLogger.info("listsManegementServicePort response is:(" + response + ").");
						switch (response) {
						case SUCCESS:
							int invalidContacts = result.getFileResult().getInvalidContacts();
							if (invalidContacts == 0) {
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
												.getLocalizedLabel("list_created_successfully")), null));
							} else {
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_INFO, invalidContacts + " "
												+ String.format(Util.getLocalizedLabel("invalid_records")), null));
							}
							userLogger.info("List created successfully from file");

							listNameNew = "";
							listName = "";
							session.removeAttribute("listName");
							session.removeAttribute("uploadTrxId");
							userLogger.info( "listname and bean will be removed");
							FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("viewListBean");
							break;
						case FAIL:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("list_upload_failure")), null));
							userLogger.error( "error while creating list from file, status FAIL");
							listContact = null;
							break;
						case INELIGIBLE_ACCOUNT:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("ineligible_create_list")), null));
							userLogger.warn( " can't upload list from file");
							break;
						case DUPLICATE_LIST_NAME:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("invalid_list_name")), null));
							userLogger.warn("list name " + listNameNew
									+ "is already exists");
							break;
						case INVALED_FILE:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("invalid_file")), null));
							userLogger.warn( " trying to upload file "
									+ file.getName() + " which contains invalid records");
							break;
						default:
							userLogger.error(trxId
									+  " undefined status="
									+ response + " while creating list from uploaded file");
							break;
						}
					}
				} catch (IOException e) {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("list_created_failure")), null));
					userLogger.error("Error while creating list from uploaded file with name=" + fileName
									+ " with IO exception", e);
                                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.IO_ERROR, "IO failure");
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_created_failure")), null));
			userLogger.error( "Error while creating list from uploaded file with name=" + fileName, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		} 
		finally{
            ThreadContext.pop();
        }
	}

	// Copy from other list methods
	// -------------------------------------------------------------

	public void getChoosenListId(ValueChangeEvent e) {
		userLogger.info( "Choose list_id=" + listId
				+ " to choose contacts from");
		try {
			if (e.getNewValue() != null) {
				String list = e.getNewValue().toString();
				choosenList = list;
				listId = Integer.valueOf(list);
				for (int i = 0; i < userList.size(); i++) {
					if (listId == userList.get(i).getListId()) {
						{
							if (userList.get(i).getListType().equals(ListType.INTRA_LIST)
									|| userList.get(i).getListType().equals(ListType.INTRA_SUB_LIST)) {
								intraListChoosen = true;
								userLogger.info( " Intra List is choosen to import from it.");
							} else if (userList.get(i).getListType().equals(ListType.CUSTOMIZED_LIST)) {
								customizedlist = true;
//								userLogger.info( " ");
							}
						}
						choosenListName = userList.get(i).getListName();
						totalRowsNum2 = userList.get(i).getContactsCount();
					}
				}
				pageNext2();
			} else {
				listId = 0;
				pageNext2();
			}
		} catch (Exception ex) {
			userLogger.error( "Error while populating contacts list of list_id=" + listId + " to copy contacts from it", ex);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	private void populateList2() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info("populating list with id " + listId);

			userLogger.debug( "populating contact list with id=" + listId + " get rows from=0 to=" + firstRow2);

			ContactResultSet result = listsManegementServicePort.getContactListWithPagination(userInfo, listId, 0,
					firstRow2);

			ResponseStatus response = result.getStatus();
			userLogger.info( "listsManegementServicePort response is:(" + response + ").");

			switch (response) {
			case SUCCESS:
				listContact = result.getContacts();
				userLogger.info( "List to choose contacts from is populated successfully with count=" + listContact.size());
				break;
			case FAIL:
				listContact = null;
				userLogger.error( "Error while populating list to choose contacts from");
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.warn("is not allowed to display contact list");
				break;
			default:
				userLogger.error( "Undefined response status=" + response + " while populating list to choose contacts from");
				break;
			}

			totalRows2 = totalRowsNum2;

			userLogger.info( "All user lists count=" + totalRows2);

			// Set currentPage, totalPages and pages.
			currentPage2 = (totalRows2 / rowsPerPage2) - ((totalRows2 - firstRow2) / rowsPerPage2) + 1;
			totalPages2 = (totalRows2 / rowsPerPage2) + ((totalRows2 % rowsPerPage2 != 0) ? 1 : 0);
			int pagesLength = Math.min(pageRange2, totalPages2);
			pages2 = new Integer[pagesLength];

			// firstPage must be greater than 0 and lesser than
			// totalPages-pageLength.
			int firstPage = Math.min(Math.max(0, currentPage2 - (pageRange2 / 2)), totalPages2 - pagesLength);

			// Create pages (page numbers for page links).
			for (int i = 0; i < pagesLength; i++) {
				pages2[i] = ++firstPage;
				if (pagesLength > 10) {
					pagesLength = 10;
				}
			}

			if (contactList == null || totalRows2 == 0) {
				userLogger.info( "No contacts for list" + listId
						+ " found in the database");
			}

		} catch (Exception e) {
			userLogger.error("Error while loading contacts for list " + listId, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
		finally{
            ThreadContext.pop();
        }
	}

	public void copyContacts() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			userLogger.info("Copying contact to new list");
			if (listName.trim().isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_name_required")), null));
				userLogger.info( "list name field is empty");
			} else {
				ContactList copyList = new ContactList();
				ContactListInfo info = new ContactListInfo();
				info.setListName(listName);

				if (intraListChoosen)
					info.setListType(ListType.INTRA_SUB_LIST);
				else if (customizedlist)
					info.setListType(ListType.CUSTOMIZED_LIST);
				else
					info.setListType(ListType.NORMAL_LIST);

				userLogger.info( "selected contacts are from (" + info.getListType().value() + ") list.");

				copyList.setListInfo(info);
				Contact selectedContact;

				for (Map.Entry<String, Boolean> e : checked2.entrySet()) {
					String key = e.getKey();
					Boolean value = e.getValue();
					if (value.equals(true)) {
						selectedContact = new Contact();
						selectedContact.setMsisdn(key);

						for (Contact contact : listContact) {
							if (contact.getMsisdn().equals(key)) {
								selectedContact.setFirstName(contact.getFirstName());
								selectedContact.setValue1(contact.getValue1());
								selectedContact.setValue2(contact.getValue2());
								selectedContact.setValue3(contact.getValue3());
								selectedContact.setValue4(contact.getValue4());
								selectedContact.setValue5(contact.getValue5());
							}
						}
						copyList.getListContacts().add(selectedContact);
					}
				}

				if (copyList.getListContacts().size() > 0) {

					userLogger.debug("creating new list with name="
							+ copyList.getListInfo().getListName() + " with contacts count="
							+ copyList.getListContacts().size() + "from intraListFlag : (" + intraListChoosen + "). ");

					ResultStatus result = listsManegementServicePort.createNewList(userInfo, copyList);
					ResponseStatus response = result.getStatus();
					userLogger.info( "listsManegementServicePort response is:(" + response + ").");
					switch (response) {
					case SUCCESS:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
										.getLocalizedLabel("list_created_successfully")), null));
						userLogger
								.info( "list is created successfully");
						break;
					case FAIL:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_created_failure")), null));
						userLogger.error( "error while creating list");
						break;
					case DUPLICATE_LIST_NAME:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("invalid_list_name")), null));
						userLogger.warn( "list name is already exists");
						break;
					case INELIGIBLE_ACCOUNT:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("ineligible_create_list")), null));
						userLogger.warn(" not eligible to create blank list");
						break;

					case CONTACTS_NOT_FOUND:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("No_Valid_Contacts")), null));
						userLogger.warn("No vaild contacts found in this list.");
						break;
					default:
						userLogger.error( "Undefined response status=" + response
								+ " while creating new list ");
						break;
					}// end switch
				}// end if
				else {
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("choose_contact_to_copy")), null));
					userLogger.info("didn't choose any contact to copy to new list");
				}
			}// if end
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_created_failure")), null));
			userLogger.error( "Error while creating list", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void copyAllContacts() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info( "Copying all contact to new list");

			if (listName.trim().isEmpty()) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_name_required")), null));
				userLogger.info( "list name field is empty");
			} else {
				ContactListInfo info = new ContactListInfo();
				info.setListName(listName);
				if (intraListChoosen)
					info.setListType(ListType.INTRA_SUB_LIST);
				else if (customizedlist)
					info.setListType(ListType.CUSTOMIZED_LIST);
				else
					info.setListType(ListType.NORMAL_LIST);
				userLogger.debug( "copy all contacts from list id=" + listId + " to create new list with name="
						+ info.getListName() + "with intraListflag : (" + intraListChoosen + ").");

				ResultStatus result = listsManegementServicePort.copyToNewList(userInfo, info, listId);

				ResponseStatus response = result.getStatus();

				userLogger.info( "listsManegementServicePort response is:(" + response + ").");
				switch (response) {
				case SUCCESS:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
									.getLocalizedLabel("list_created_successfully")), null));
					userLogger.info("list is created successfully with all contacts from list=" + listId);
					break;
				case FAIL:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("list_created_failure")), null));
					userLogger.error( "error while creating list from all the contacts of list=" + listId);
					break;
				case DUPLICATE_LIST_NAME:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_list_name")), null));
					userLogger.warn( "list name already exists while creating list from all the contacts of list=" + listId);
					break;
				case INELIGIBLE_ACCOUNT:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_create_list")), null));
					userLogger.warn("Not eligible for creating new list containing all contacts from list=" + listId);
					break;
				default:
					userLogger.error("Undefined response status="
							+ response + " while creating list from all the contacts of list=" + listId);
					break;
				}// end switch

			}// if end
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_created_failure")), null));
			userLogger.error("Error while creating list", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	// Create blank list methods
	// ----------------------------------------------------------------

	private boolean checkRequiredField2() {
		if (firstName2.trim().isEmpty() && MSISDN.trim().isEmpty()) {
			return true;
		} else if (listName.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_name_required")), null));
			userLogger.info( "list name field is empty");
			return false;

		} else if (MSISDN.trim().isEmpty()) {
			// FacesContext.getCurrentInstance().addMessage(null,
			// new FacesMessage(FacesMessage.SEVERITY_ERROR,
			// String.format(Util.getLocalizedLabel("msisdn_required")), null));
			userLogger.info( "MSISDN field is empty");
			return false;
		}
		return true;
	}

	public void saveContactList() {
		try {
			Contact contact = new Contact();
			if (checkRequiredField2()) {
				if (firstName2.trim().isEmpty() && MSISDN.trim().isEmpty() && contactList.size() > 0) {

				} else if (SMSUtils.validateLocalAddress(MSISDN) || SMSUtils.validateInternationalAddress(MSISDN)) {
					contact.setFirstName(firstName2);
					contact.setMsisdn(MSISDN);
					contactList.add(contact);
					firstName2 = "";
					MSISDN = "";
					userLogger.info( "entered " + "valid msisdn="
							+ encyrptionUtil.encrypt(contact.getMsisdn()) + " and first name=" + contact.getFirstName());
				} // end if
				else {
					userLogger.info( "entered "
							+ "invalid msisdn");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("msisdn_invalid")), null));
				}
			}// end big if
		} catch (Exception e) {
			userLogger.error("Error while saving contacts for creating blank list", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public String createBlankContactList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			if (checkRequiredField2()) {
				if (firstName2.trim().isEmpty() && MSISDN.trim().isEmpty() && contactList.size() > 0) {
					saveContactList();
					ContactList list = new ContactList();
					ContactListInfo info = new ContactListInfo();
					info.setListName(listName);

					for (int i = 0; i < contactList.size(); i++) {
						list.getListContacts().add(contactList.get(i));
					}
					if (intraListChoosen)
						info.setListType(ListType.INTRA_SUB_LIST);
					else if (customizedlist)
						info.setListType(ListType.CUSTOMIZED_LIST);
					else
						info.setListType(ListType.NORMAL_LIST);

					list.setListInfo(info);
					userLogger.debug( "creating list with list name="
							+ list.getListInfo().getListName() + " with contacts count="
							+ list.getListContacts().size());

					ResultStatus result = listsManegementServicePort.createNewList(userInfo, list);

					ResponseStatus resp = result.getStatus();

					userLogger.info("listsManegementServicePort response is:(" + resp + ").");
					switch (resp) {
					case SUCCESS:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
										.getLocalizedLabel("list_created_successfully")), null));
						userLogger.info( "list with name="
								+ list.getListInfo().getListName() + " is created successfully");
						contactList.clear();
						populateList();
						break;
					case DUPLICATE_LIST_NAME:
						// ////
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("invalid_list_name")), null));
						userLogger.warn( "list was't created as list name" + list.getListInfo().getListName()
								+ " is already exists");

						break;
					case FAIL:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_created_failure")), null));
						userLogger.error( "error while creating list with name=" + list.getListInfo().getListName());
						contactList.clear();
						break;
					case INELIGIBLE_ACCOUNT:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("ineligible_create_list")), null));
						userLogger.warn(" not ilgible so can't create blank list named=" + list.getListInfo().getListName());
						contactList.clear();
						break;
					default:
						userLogger.error( "undefined status=" + resp
								+ " while creating blank list named=" + list.getListInfo().getListName());
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("list_created_failure")), null));
						break;

					}
				} else {
					if (SMSUtils.validateLocalAddress(MSISDN) || SMSUtils.validateInternationalAddress(MSISDN)) {
						saveContactList();
						ContactList list = new ContactList();
						ContactListInfo info = new ContactListInfo();
						info.setListName(listName);

						for (int i = 0; i < contactList.size(); i++) {
							list.getListContacts().add(contactList.get(i));
						}
						if (intraListChoosen)
							info.setListType(ListType.INTRA_SUB_LIST);
						else if (customizedlist)
							info.setListType(ListType.CUSTOMIZED_LIST);
						else
							info.setListType(ListType.NORMAL_LIST);

						list.setListInfo(info);

						userLogger.debug( "creating list with list name=" + list.getListInfo().getListName()
								+ " with contacts count=" + list.getListContacts().size());

						ResultStatus result = listsManegementServicePort.createNewList(userInfo, list);

						ResponseStatus resp = result.getStatus();
						userLogger.info("listsManegementServicePort response is:(" + resp + ").");
						switch (resp) {
						case SUCCESS:
							userLogger.info( "list with name="
									+ list.getListInfo().getListName() + " is created successfully");

							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
											.getLocalizedLabel("list_created_successfully")), null));

							contactList.clear();
							populateList();
							break;
						case DUPLICATE_LIST_NAME:
							userLogger.warn( "list was't created as list name" + list.getListInfo().getListName()
									+ " is already exists");

							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("invalid_list_name")), null));

							break;
						case FAIL:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("list_created_failure")), null));
							userLogger.error( "error while creating list with name=" + list.getListInfo().getListName());
							contactList.clear();
							break;
						case INELIGIBLE_ACCOUNT:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("ineligible_create_list")), null));
							userLogger.warn( "can't create blank list named=" + list.getListInfo().getListName());
							contactList.clear();
							break;
						default:
							userLogger.error( "undefined status=" + resp
									+ " creates blank list named=" + list.getListInfo().getListName());
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("list_created_failure")), null));
							break;

						}
					} else {
						userLogger.info(" entered invalid msisdn");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("msisdn_invalid")), null));
					}
				}
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_created_failure")), null));
			userLogger.error( "error while creating blank list");
			contactList.clear();
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		return null;
	}

	public void getSelectedTypeOfCreation() {
		if (selectedType.equals("anotherList")) {
			getAllListInfo();
			importFromListFlag = true;
			importFromFileFlag = false;
			createCustomizeList = false;
		} else if (selectedType.equals("fromfile")) {
			importFromListFlag = false;
			importFromFileFlag = true;
			createCustomizeList = false;
		} else if (selectedType.equals("customizeList")) {
			importFromListFlag = false;
			importFromFileFlag = false;
			createCustomizeList = true;

		} else {
			importFromListFlag = false;
			importFromFileFlag = false;
			createCustomizeList = false;

		}
	}

	// Edit list name and contacts-------------------------------------

	public void changeListName() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		try {
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			if (changedListName != null && !changedListName.trim().isEmpty()) {

				userLogger.info( " editting list name of list id=" + changedListId + " to list name=" + changedListName);

				ResultStatus result = listsManegementServicePort.editContactListName(userInfo,
						Integer.valueOf(changedListId), changedListName);
				ResponseStatus status = result.getStatus();
				userLogger.info("listsManegementServicePort response is:(" + status + ").");

				switch (status) {
				case SUCCESS:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
									.getLocalizedLabel("change_list_name_successfully")), null));
					populateList();
					subListName = changedListName;

					userLogger.info(" changed list name with id "
							+ changedListId + " to " + changedListName + " with status SUCCESS");
					break;
				case INELIGIBLE_ACCOUNT:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_edit_list_name")), null));

					userLogger.warn("can't change list name with id "
							+ changedListId + " to " + changedListName + " with status INELIGIBLE_ACCOUNT");

					break;
				case INVALID_REQUEST:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("change_list_name_failure")), null));

					userLogger.warn("Invalid request can't change list name with id " + changedListId + " to "
							+ changedListName + " with status INVALID_REQUEST");
					break;
				case LIST_NOT_FOUND:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("change_list_name_failure")), null));

					userLogger.warn( "can't change list name with id "
							+ changedListId + " to " + changedListName + " with status LIST_NOT_FOUND");
					break;
				case LOCKED_LIST:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("locked_list")), null));

					userLogger.warn("can't change list name with id "
							+ changedListId + " to " + changedListName + " with status LOCKED_LIST");
					break;
				case DUPLICATE_LIST_NAME:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("list_name_duplicated")), null));
					userLogger.warn( "can't change list name with id "
							+ changedListId + " to " + changedListName + " with status DUPLICATE_LIST_NAME");

					break;
				default:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("change_list_name_failure")), null));
					userLogger.error( "undefined reponse status="
							+ status + " while changing list name with id " + changedListId + " to " + changedListName);

					break;
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_name_required")), null));
				userLogger.info( "List name can not be empty while changing it for list_id=" + changedListId);

			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("change_list_name_failure")), null));
			userLogger.error( "Error while changing list name with id=" + changedListId, e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public String populateEditContactObj() {
		contactFirstName = contactEditObj.getFirstName();
		contactMsisdn = contactEditObj.getMsisdn();
		value1 = contactEditObj.getValue1();
		value2 = contactEditObj.getValue2();
		value3 = contactEditObj.getValue3();
		value4 = contactEditObj.getValue4();
		value5 = contactEditObj.getValue5();
		return "";
	}

	public void changeContact() {
		UserTrxInfo userInfo = new UserTrxInfo();
		Contact updatedContact = new Contact();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		try {
	        ThreadContext.push(trxId);
			userInfo.setUser(user);
			if (contactMsisdn != null && !contactMsisdn.trim().isEmpty()) {
				if (SMSUtils.validateLocalAddress(contactMsisdn)
						|| SMSUtils.validateInternationalAddress(contactMsisdn)) {
					if (isCustomizedlist() && (value1 == null || value1.trim().isEmpty())
							&& (value2 == null || value2.trim().isEmpty())
							&& (value3 == null || value3.trim().isEmpty())
							&& (value4 == null || value4.trim().isEmpty())
							&& (value5 == null || value5.trim().isEmpty())) {
						userLogger.info( "did not entered values"
								+ " while changing contact in list_id=" + changedListId
								+ "however that list is customized list");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("invalid_customize_contact")), null));
					} else {
						updatedContact.setFirstName(contactFirstName);
						updatedContact.setMsisdn(contactMsisdn);
						if (isCustomizedlist()) {
							updatedContact.setValue1(value1);
							updatedContact.setValue2(value2);
							updatedContact.setValue3(value3);
							updatedContact.setValue4(value4);
							updatedContact.setValue5(value5);
						}
						value1 = "";
						value2 = "";
						value3 = "";
						value4 = "";
						value5 = "";
						userLogger.info( "Editing contact [msisdn="
								+ encyrptionUtil.encrypt(updatedContact.getMsisdn()) + ",first_name=" + updatedContact.getFirstName()
								+ "] in list id=" + changedListId + " to new msisdn=" + encyrptionUtil.encrypt(contactEditObj.getMsisdn()));

						ResultStatus result = listsManegementServicePort.editContact(userInfo, updatedContact,
								contactEditObj.getMsisdn(), Integer.valueOf(changedListId));
						ResponseStatus status = result.getStatus();
						userLogger.info( "listsManegementServicePort response is:(" + status + ").");
						switch (status) {
						case SUCCESS:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
											.getLocalizedLabel("change_contact_success")), null));

							for (int i = 0; i < contactsList.size(); i++) {
								if (contactsList.get(i).getMsisdn().equals(contactEditObj.getMsisdn())) {
									contactsList.get(i).setFirstName(updatedContact.getFirstName());
									contactsList.get(i).setLastName(updatedContact.getLastName());
									contactsList.get(i).setMsisdn(updatedContact.getMsisdn());
									contactsList.get(i).setValue1(updatedContact.getValue1());
									contactsList.get(i).setValue2(updatedContact.getValue2());
									contactsList.get(i).setValue3(updatedContact.getValue3());
									contactsList.get(i).setValue4(updatedContact.getValue4());
									contactsList.get(i).setValue5(updatedContact.getValue5());
									break;
								}
							}

							userLogger.info( "changed old contact [msisdn=" + encyrptionUtil.encrypt(updatedContact.getMsisdn()) + ",first_name="
									+ updatedContact.getFirstName() + "] to [msisdn=" + encyrptionUtil.encrypt(contactEditObj.getMsisdn())
									+ ",first_name=" + updatedContact.getFirstName() + "] in list with id "
									+ changedListId + " with status SUCCESS");
							break;
						case INELIGIBLE_ACCOUNT:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("ineligible_edit_contact")), null));

							userLogger.warn("can't edit contact with msisdn " + encyrptionUtil.encrypt(contactEditObj.getMsisdn())
									+ " in list with id " + changedListId + " with status INELIGIBLE_ACCOUNT");

							break;
						case INVALID_REQUEST:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("change_contact_failure")), null));

							userLogger.warn( "can't edit contact with msisdn " + encyrptionUtil.encrypt(contactEditObj.getMsisdn())
									+ " in list with id " + changedListId + " with status INVALID_REQUEST");
							break;
						case CONTACTS_NOT_FOUND:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("change_contact_failure")), null));

							userLogger.warn( "can't edit contact with msisdn " + encyrptionUtil.encrypt(contactEditObj.getMsisdn())
									+ " in list with id " + changedListId + " with status CONTACTS_NOT_FOUND");
							break;
						default:
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("change_contact_failure")), null));
							userLogger.error( "can't edit contact with msisdn " +encyrptionUtil.encrypt( contactEditObj.getMsisdn())
									+ " in list with id " + changedListId + " with status " + status);

							break;
						}
					}
				} else {
					userLogger.info( "entered invalid msisdn while changing contact in list_id=" + changedListId);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("msisdn_invalid")), null));
				}
			} else {
				userLogger.info( "Did not enter msisdn="
						+encyrptionUtil.encrypt(contactMsisdn) + " while changing contact in list_id=" + changedListId);
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("msisdn_required")), null));
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("change_contact_failure")), null));
			userLogger.error( "Error while editing contact with msisdn=" + encyrptionUtil.encrypt(updatedContact.getMsisdn()) + " in list id="
					+ changedListId, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

		}finally{
            ThreadContext.pop();
        }

	}

	// For refresh button
	public String getIntraLists() {
		// To call back end get Internal Lists.
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		userInfo.setUser(user);
		try {
	        ThreadContext.push(trxId);
			userLogger.info("getting intra sender list.");
			ResultStatus result = listsManegementServicePort.handleIntraList(userInfo);
			ResponseStatus status = result.getStatus();
			userLogger.info("fetching intra sender list for account id: (" + user.getAccountId()
					+ " ) is finished with status: (" + status + ").");
			switch (status) {
			case SUCCESS:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
								.getLocalizedLabel("List_is_refreshed_successfully")), null));
				populateList();

				userLogger.info( "handle intra lists finished with SUCCESS");
				break;
			case INVALID_REQUEST:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("Invalid_request")), null));

				userLogger.warn("hnadle intra sender lists finished with status INVALID_REQUEST");
				break;

			case FAIL:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("Invalid_request")), null));

				userLogger.warn( "hnadle intra sender lists finished with status FAIL");
				break;

			case LIST_NOT_FOUND:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("Invalid_request")), null));

				userLogger.warn( "hnadle intra sender lists finished with status LIST_NOT_FOUND");
				break;

			default:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("Invalid_request")), null));
				userLogger.error( "undefined reponse status=" + status + " while getting intra sender lists");

				break;
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("Invalid_request")), null));
			userLogger.error( "unhandled exception while getting intra lists."+e.getMessage());
            appLogger.error( "unhandled exception while getting intra lists.",e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

		return "";
	}

	public void populateIntraListToImport() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		userInfo.setUser(user);
		try {
	        ThreadContext.push(trxId);
			userLogger.info( "getting intra list contacts. ");
			List<ListType> typeList = new ArrayList<ListType>();
			typeList.add(ListType.INTRA_LIST);
			ContactListInfoResultSet intraList = listsManegementServicePort.getContactListsInfo(userInfo, typeList);

			ResponseStatus response = intraList.getStatus();
			userLogger.info( "listsManegementServicePort response is:(" + response + ").");

			switch (response) {
			case SUCCESS:
				IntraListId = intraList.getContactListInfoResultSet().get(0).getListId();

				userLogger.info("list of contacts of id=" + IntraListId + " is get successfully");

				userLogger.debug( "Getting contacts for IntraListId=" + IntraListId + " from=" + firstRowIntra + " get count="
						+ rowsPerPageIntra);

				ContactResultSet intraListresult = listsManegementServicePort.getContactListWithPagination(userInfo,
						IntraListId, firstRowIntra, rowsPerPageIntra);

				ResponseStatus intraContactResponse = intraListresult.getStatus();
				userLogger.info("listsManegementServicePort response is:(" + response + ").");

				switch (intraContactResponse) {
				case SUCCESS:

					for (Contact contact : intraListresult.getContacts()) {
						intraContactsList.add(contact);
						archiveList.add(contact);
					}
					userLogger.info(  "contacts of intra list count="
							+ intraListresult.getContacts().size() + " for list_id=" + IntraListId);

					userLogger.info( "list of contacts of id="
							+ IntraListId + " is populated successfully");
					break;
				case FAIL:
					intraContactsList.clear();
					userLogger.error( "Error while populating contacts of intralist of id=" + IntraListId);
					break;
				case INELIGIBLE_ACCOUNT:
					intraContactsList.clear();
					userLogger.warn( "ineligible account, not allowed to display intralist contacts list_id=" + IntraListId);
					break;
				default:
					intraContactsList.clear();
					userLogger.error(  "undefined return status="
							+ response + " while getting intralist contacts of list_id=" + IntraListId);
					break;

				}
				break;
			case FAIL:
				userLogger.error( "Error while getting contacts of intralist ");
				break;
			default:
				userLogger.error( "Unknown Error while get contacts of intralist ");
				break;

			}
			if (intraContactsList == null ) {
				userLogger.info( "No sub contacts for list_name="
						+ subListName + " ,list_id=" + IntraListId + " found in the database");
			}
		} catch (Exception e) {
			intraContactsList.clear();
			userLogger.error( "Error while loading contacts for list_name=" + subListName + " ,list_id=" + IntraListId, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public String addSubIntraContact() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		userInfo.setUser(user);

		List<Contact> subIntraCont = new ArrayList<Contact>();
		for (Map.Entry<Integer, Boolean> e : intraContactsSelected.entrySet()) {
			String key = String.valueOf(e.getKey());
			Boolean value = e.getValue();
			if (value.equals(true)) {
				for (int i = 0; i < intraContactsList.size(); i++) {
					if (intraContactsList.get(i).getMsisdn().equals(key)) {
						subIntraCont.add(intraContactsList.get(i));
					}
				}
			}
		}

		userLogger.debug( "Adding new intra contact/contacts to list_id=" + IntraListId);
		try {
	        ThreadContext.push(trxId);
			ContactList contactObj = new ContactList();
			ContactListInfo info = new ContactListInfo();
			info.setListId(Integer.valueOf(listIdForSubList));
			info.setListName(subListName);
			info.setListType(ListType.INTRA_SUB_LIST);
			contactObj.setListInfo(info);

			for (int i = 0; i < subIntraCont.size(); i++) {
				contactObj.getListContacts().add(subIntraCont.get(i));
			}

			ContactResultSet result = listsManegementServicePort.expandContactList(userInfo, contactObj);

			ResponseStatus resp = result.getStatus();
			userLogger.info( "listsManegementServicePort response is:(" + resp + ").");
			switch (resp) {
			case SUCCESS:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
								.getLocalizedLabel("list_edit_successfully")), null));
				userLogger.info( "contact is added to list_id=" + listIdForSubList + "successfully");
				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();
				intraContactsSelected = new HashMap<Integer, Boolean>();
//				populateIntraListToImport();
				getUpdateListId(listIdForSubList);
				populateList();
				break;
			case FULLY_UPDATED:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
								.getLocalizedLabel("list_edit_successfully")), null));
				userLogger.info( "contact is added to list_id=" + listIdForSubList + "successfully");
				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();
				intraContactsSelected = new HashMap<Integer, Boolean>();
				populateIntraListToImport();
				getUpdateListId(listIdForSubList);
				populateList();
				break;
			case FAIL:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_edit_failure")), null));
				userLogger.error( "error while adding contact to list_id=" + listIdForSubList);
				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();
				intraContactsSelected = new HashMap<Integer, Boolean>();
				populateIntraListToImport();
				getUpdateListId(listIdForSubList);

				break;
			case INELIGIBLE_ACCOUNT:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("ineligible_add_contact")), null));
				userLogger.warn( "can't add new contact to list_id=" + listIdForSubList);
				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();
				intraContactsSelected = new HashMap<Integer, Boolean>();
				populateIntraListToImport();
				getUpdateListId(listIdForSubList);
				break;
			case PARTIAL_UPDATE:
				String duplicatListIds = "";
				for (int i = 0; i < result.getContacts().size(); i++) {
					duplicatListIds = duplicatListIds + "/";
				}
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("contact_already_exist")) + duplicatListIds, null));
				userLogger.info( "contact(s) already exists in list_id=" + listIdForSubList);
				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();
				intraContactsSelected = new HashMap<Integer, Boolean>();
				populateIntraListToImport();
				getUpdateListId(listIdForSubList);
				populateList();
				break;
			case DUPLICATE_CONTACT:
				String duplicatContacts = "";
				for (int i = 0; i < result.getContacts().size(); i++) {
					duplicatListIds = duplicatContacts + "/";
				}
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("contact_already_exist")) + duplicatContacts, null));
				userLogger.warn( "contact(s) already exists in list_id=" + listIdForSubList);
				intraContactsList = new ArrayList<Contact>();
				populateIntraListToImport();
				intraContactsSelected = new HashMap<Integer, Boolean>();

				break;
			case LOCKED_LIST:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("locked_list")), null));
				userLogger.warn("list_id=" + listIdForSubList + " can not be edited");
				intraContactsList = new ArrayList<Contact>();
				populateIntraListToImport();
				intraContactsSelected = new HashMap<Integer, Boolean>();

				subIntraCont = new ArrayList<Contact>();
				getUpdateListId(listIdForSubList);
				break;
			case LIST_NOT_FOUND:
				userLogger.warn( "list_id=" + listIdForSubList);

				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();

				intraContactsSelected = new HashMap<Integer, Boolean>();
				populateIntraListToImport();
				getUpdateListId(listIdForSubList);
				break;
			default:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("list_edit_failure")), null));
				userLogger.error("undefined result status" + resp + " while adding contact to list_id=" + listIdForSubList);
				intraContactsList = new ArrayList<Contact>();
				subIntraCont = new ArrayList<Contact>();
				intraContactsSelected = new HashMap<Integer, Boolean>();
				populateIntraListToImport();
				getUpdateListId(listIdForSubList);
				break;

			}

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_edit_failure")), null));
			userLogger.error( "unhandled exception while adding contact to list_id=" + listIdForSubList);

			intraContactsList = new ArrayList<Contact>();
			subIntraCont = new ArrayList<Contact>();
			intraContactsSelected = new HashMap<Integer, Boolean>();
			populateIntraListToImport();
			getUpdateListId(listIdForSubList);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		return "";

	}

	private void readVCFFile(String trxId) {
		try {
			validFile = true;
			userLogger.info("Reading upload file as VCF file name=" + fileName);

			recordsList = new ArrayList<Map<Integer, String>>();
			valuesMap = new LinkedHashMap<String, String>();
			valuesMap.put("none", String.format(Util.getLocalizedLabel("none")));
			valuesMap.put("name", String.format(Util.getLocalizedLabel("name")));
			valuesMap.put("mobile", String.format(Util.getLocalizedLabel("mobile")));

			userLogger.info( "Number of rows to be viewed as sample while uplading list from an csv filename " + fileName
					+ " count=" + ((Integer) Configs.UPLOAD_NUM_RECORDS.getValue()) + ". Values Map : "
					+ valuesMap.entrySet());

			VCardReader reader = new VCardReader(file);
			VCard vcard = null;

			/**
			 * loop to read all file and validate if contains XSS script or
			 * continue to show/save it. // if not break reading file and
			 */
			List<List<String>> myFileList = new ArrayList<List<String>>();

			while ((vcard = reader.readNext()) != null) {
				List<String> record = new ArrayList<String>();
				if (vcard.getFormattedName() != null) {
					String name = vcard.getFormattedName().getValue();
					if (validateXSS(name)) {
						record.add(name);
						validFile = true;
					} else {
						validFile = false;
						break;
					}
				}
				if (vcard.getTelephoneNumbers() != null && !vcard.getTelephoneNumbers().isEmpty()) {
					String telephone = vcard.getTelephoneNumbers().get(0).getText();
					if (validateXSS(telephone)) {
						record.add(telephone);
						validFile = true;
					} else {
						validFile = false;
						break;
					}

					myFileList.add(record);
				}

			}
			/**
			 * complete reading the the file if valid as old way
			 */
			userLogger.info( " Valid file Flag: " + validFile);
			reader = new VCardReader(file);
			vcard = null;
			if (validFile) {
				for (int i = 0; i < (Integer) Configs.UPLOAD_NUM_RECORDS.getValue(); i++) {
					if ((vcard = reader.readNext()) != null) {
						List<String> record = new ArrayList<String>();
						if (vcard.getFormattedName() != null) {
							String name = vcard.getFormattedName().getValue();
							record.add(name);
						}
						if (vcard.getTelephoneNumbers() != null && !vcard.getTelephoneNumbers().isEmpty()) {
							String telephone = vcard.getTelephoneNumbers().get(0).getText();
							record.add(telephone);
							myNewList.add(record);
						}
					}

				}
				userLogger.info( "myNewList size"
						+ myNewList.size());

				reader.close();
				columnsId.add("0");
				columnsId.add("1");
				if (columnsId.size() > 0) {
					userLogger.info( "Uploaded file name "
							+ fileName + " as excel type is not empty, it contains columns_count=" + columnsId.size());
					uploadTableFalg = true;
				} else {
					userLogger.info("Uploaded file name "
							+ fileName + " as VCF type is empty");
				}

				userLogger.info( "Done reading vcf file name="
						+ fileName);
			} else {

				uploadTableFalg = false;
				userLogger.info( "invalid csv file ");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_file_content")), null));
			}

		} catch (Exception e) {
			userLogger.error( "Error while reading vcf file name=" + fileName, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public void viewAllIntraLists() {
		viewInternalListsFlag = true;
		intraPagiantionFlag = true;
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
			listofLists = new ArrayList<ContactListInfo>();

			listsManegementServicePort = portObj.getListsManegementServicePort();
			if (!intraPagiantionFlag)
				firstRow3 = 0;
			userLogger.debug( "Getting paginated intra/sub intra lists from=" + firstRow3 + " to=" + rowsPerPage3);
			List<ListType> typeList = new ArrayList<ListType>();
			typeList.add(ListType.INTRA_LIST);
			typeList.add(ListType.INTRA_SUB_LIST);

			ContactListInfoResultSet result = listsManegementServicePort.getContactListsInfoPagination(userInfo,
					firstRow3, rowsPerPage3, typeList);
			ResponseStatus state = result.getStatus();
			userLogger.info( "listsManegementServicePort response for getting intra/sub intra is:(" + state + ").");

			switch (state) {

			case SUCCESS:
				listofLists = result.getContactListInfoResultSet();
				// populateIntraListToImport();

				userLogger.info( "list of contact lists is populating successfully, rows_count=" + listofLists.size());
				break;
			case FAIL:
				userLogger.error( "Fail while getting list of contact lists");
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.warn( "Ineligible user, not allowed to display lists of contacts");
			default:
				userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername())
						+ "Unknown response status=" + result.getStatus() + " , while getting list of contact lists");
				break;
			}

			totalRows3 = listsManegementServicePort.countContactListsInfo(userInfo, typeList);

			userLogger.info( " total list rows_count=" + totalPages3);

			// Set currentPage, totalPages and pages.
			currentPage3 = (totalRows3 / rowsPerPage3) - ((totalRows3 - firstRow3) / rowsPerPage3) + 1;
			totalPages3 = (totalRows3 / rowsPerPage3) + ((totalRows3 % rowsPerPage3 != 0) ? 1 : 0);
			int pagesLength = Math.min(pageRange3, totalPages3);
			pages3 = new Integer[pagesLength];

			// firstPage must be greater than 0 and lesser than
			// totalPages-pageLength.
			int firstPage = Math.min(Math.max(0, currentPage3 - (pageRange3 / 2)), totalPages3 - pagesLength);

			// Create pages (page numbers for page links).
			for (int i = 0; i < pagesLength; i++) {
				pages3[i] = ++firstPage;
				if (pagesLength > 10) {
					pagesLength = 10;
				}
			}

			if (listofLists == null || listofLists.size() == 0) {
				tableFlag = false;
			}

			checked = new HashMap<Integer, Boolean>();
			checkedSubList = new HashMap<String, Boolean>();
			// Should not happened, always will be there intra list.
			if (listofLists == null || listofLists.isEmpty()) {
				viewButtonsFlag = false;
				userLogger.info( " intra/sub intra List of contact lists is empty");
			}

		} catch (Exception e) {
			userLogger.error( "Error while getting paginated intra/sub intra lists of contacts from=" + firstRow3 + " to="
					+ rowsPerPage3, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	// if file doesn't contain xss return true, otherwise return false
	private boolean validateXSS(String origValue) {
		String value = origValue;
		if (value != null) {

			// Avoid null characters
			value = value.replaceAll("", "");

			// Avoid anything between script tags
			Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid anything in a src='...' type of expression
			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE
					| Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Remove any lonesome </script> tag
			scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Remove any lonesome <script ...> tag
			scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
					| Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid eval(...) expressions
			scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
					| Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid expression(...) expressions
			scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
					| Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid javascript:... expressions
			scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid vbscript:... expressions
			scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
			value = scriptPattern.matcher(value).replaceAll("");

			// Avoid onload= expressions
			scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
					| Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");

			// remove <
			scriptPattern = Pattern.compile("<*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
			value = scriptPattern.matcher(value).replaceAll("");
		}
		return origValue.equals(value);

	}
}

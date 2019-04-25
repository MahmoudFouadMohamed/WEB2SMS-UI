package com.edafa.web2sms.ui.campaign;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UICommand;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.service.campaign.model.CampaignResult;
import com.edafa.web2sms.encyrptionutil.Interfaces.EncyrptionUtilInterface;
import com.edafa.web2sms.prov.enums.ProvResponseStatus;
import com.edafa.web2sms.prov.model.ProvResultStatus;
import com.edafa.web2sms.service.acc_manag.account.AccountManegementService;
import com.edafa.web2sms.service.acc_manag.account.model.AccountResultFullInfo;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.campaign.CampaignManagementService;
import com.edafa.web2sms.service.enums.CampaignType;
import com.edafa.web2sms.service.enums.ListType;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.list.model.ContactListInfoResultSet;
import com.edafa.web2sms.service.list.model.ContactListResultSet;
import com.edafa.web2sms.service.list.model.ContactResultSet;
import com.edafa.web2sms.service.lists.ListsManegementService;
import com.edafa.web2sms.service.model.Contact;
import com.edafa.web2sms.service.model.ContactListInfo;
import com.edafa.web2sms.service.model.ProvTrxInfo;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.SubmittedCampaign;
import com.edafa.web2sms.service.model.Template;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.model.enums.CampaignValidationStatus;
import com.edafa.web2sms.service.model.enums.Language;
import com.edafa.web2sms.service.model.enums.ScheduleFrequency;
import com.edafa.web2sms.service.prov.ServiceProvisioning;
import com.edafa.web2sms.service.template.TemplateManegementService;
import com.edafa.web2sms.service.template.TemplatesResultSet;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.sms.SMSUtils;
import com.edafa.web2sms.utils.sms.SenderType;
import com.edafa.web2sms.utils.sms.exception.InvalidSMSSender;

@ManagedBean(name = "campaignBean")
@ViewScoped
public class CampaignBean {
	private String campaignName;
	private boolean scheduleFlag;
	private boolean startDateAndTimeFlag = false;
	private String text;
	private String languageSelected = "ENGLISH";
	private String startDateAndTime;
	private String stopTime = "";
	private String senderName;
	private boolean templateFlag;
	private boolean frequencyFlag;
	private boolean onceFlag = true;
	private String choosenFrequency;
	private boolean templateDiv;
	private String choosenTemplate;
	private String endDate;
	private List<ContactListInfo> listInfo;
	CampaignManagementService campaignServicePort;
	ListsManegementService listsManegementServicePort;
	TemplateManegementService templateManegmentServicePort;
	ServiceProvisioning serviceProvisioningPort;
	AccountManegementService accountManegmentServicePort;
	private Map<Integer, Boolean> checked;
	private List<ContactListInfo> selectedList;
	private String testList;
	private Date finalStartDate;
	private Date finalStopTime;
	private int numOfSelectedLists = 0;
	private List<Template> templateList;
	private String listNameSearch;
	private boolean endDateFlag;
	private boolean stopTimeFlag;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
	private String testStartdate;
	private String changeSenderName;

	FacesContext facesContext = FacesContext.getCurrentInstance();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
	User user = new User();
	AccManagUser accManagUser=new AccManagUser();
	private Date finalEndDate;
	private boolean saveButtonFlag;
	private boolean goTohistory;

	private String firstName;
	private String msisdn;
	private List<Contact> individualContactList;
	private Map<String, Boolean> checked2;
	private boolean contactAddedSuccess;
	private List<Contact> selectedContact;
	private String testMsisdn;

	private boolean senderNameSucces;

	private int totalRows;
	private int firstRow;
	private int rowsPerPage;

	private int totalRows2;
	private int firstRow2;
	private int rowsPerPage2;

	@EJB
	WSClients portObj;
	
	@EJB
	EncyrptionUtilInterface encyrptionUtil;
        
        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;

	private String listId;
	private List<Contact> contactList;
	private Map<String, Boolean> checked3;

	private HtmlDataTable htmlDataTable;

	private boolean searchMode;
	private List<ContactListInfo> archiveList;
	private boolean listCheckedFlag = true;
	private List<Contact> newIndividualList;
	private List<String> sendersList;
	private String choosenSender;

	private String newSenderName;
	private String editedSenderName;

	private boolean intraCheckFlag;
	private boolean intraPanelFlag;

	private boolean templateChecked;

	/*
	 * intra campaign parameters
	 */

	private boolean senderNameNotSelected = true;
	private boolean intraSenderNameChoosen = false;
	private boolean contactsSelected = false;
	/*
	 * customized campaign parameters
	 */
	private boolean customizeCampFlag = false;

	private boolean individualContacts = false;

	private List<String> intraSendersList = new ArrayList<String>();
//	String loginTrxId;
	// Setters and getters--------------------------------------------

	public List<ContactListInfo> getListInfo() {
		return listInfo;
	}

	public boolean isIndividualContacts() {
		return individualContacts;
	}

	public void setIndividualContacts(boolean individualContacts) {
		this.individualContacts = individualContacts;
	}

	public boolean isCustomizeCampFlag() {
		return customizeCampFlag;
	}

	public void setCustomizeCampFlag(boolean customizeCampFlag) {
		this.customizeCampFlag = customizeCampFlag;
	}

	public boolean isContactsSelected() {
		return contactsSelected;
	}

	public void setContactsSelected(boolean contactsSelected) {
		this.contactsSelected = contactsSelected;
	}

	public boolean isSenderNameNotSelected() {
		return senderNameNotSelected;
	}

	public void setSenderNameNotSelected(boolean senderNameNotSelected) {
		this.senderNameNotSelected = senderNameNotSelected;
	}

	public boolean isTemplateChecked() {
		return templateChecked;
	}

	public void setTemplateChecked(boolean templateChecked) {
		this.templateChecked = templateChecked;
	}

	public boolean isIntraPanelFlag() {
		return intraPanelFlag;
	}

	public void setIntraPanelFlag(boolean intraPanelFlag) {
		this.intraPanelFlag = intraPanelFlag;
	}

	public String getEditedSenderName() {
		return editedSenderName;
	}

	public void setEditedSenderName(String editedSenderName) {
		this.editedSenderName = editedSenderName;
	}

	public String getNewSenderName() {
		return newSenderName;
	}

	public void setNewSenderName(String newSenderName) {
		this.newSenderName = newSenderName;
	}

	public boolean isIntraCheckFlag() {
		return intraCheckFlag;
	}

	public void setIntraCheckFlag(boolean intraCheckFlag) {
		this.intraCheckFlag = intraCheckFlag;
	}

	public List<Contact> getNewIndividualList() {
		return newIndividualList;
	}

	public void setNewIndividualList(List<Contact> newIndividualList) {
		this.newIndividualList = newIndividualList;
	}

	public String getChoosenSender() {
		return choosenSender;
	}

	public void setChoosenSender(String choosenSender) {
		this.choosenSender = choosenSender;
	}

	public boolean isListCheckedFlag() {
		return listCheckedFlag;
	}

	public void setListCheckedFlag(boolean listCheckedFlag) {
		this.listCheckedFlag = listCheckedFlag;
	}

	public boolean isSearchMode() {
		return searchMode;
	}

	public void setSearchMode(boolean searchMode) {
		this.searchMode = searchMode;
	}

	public HtmlDataTable getHtmlDataTable() {
		return htmlDataTable;
	}

	public void setHtmlDataTable(HtmlDataTable htmlDataTable) {
		this.htmlDataTable = htmlDataTable;
	}

	public Map<String, Boolean> getChecked3() {
		return checked3;
	}

	public void setChecked3(Map<String, Boolean> checked3) {
		this.checked3 = checked3;
	}

	public List<Contact> getContactList() {
		return contactList;
	}

	public void setContactList(List<Contact> contactList) {
		this.contactList = contactList;
	}

	public boolean isSenderNameSucces() {
		return senderNameSucces;
	}

	public void setSenderNameSucces(boolean senderNameSucces) {
		this.senderNameSucces = senderNameSucces;
	}

	public String getTestMsisdn() {
		return testMsisdn;
	}

	public void setTestMsisdn(String testMsisdn) {
		this.testMsisdn = testMsisdn;
	}

	public Map<String, Boolean> getChecked2() {
		return checked2;
	}

	public List<Contact> getSelectedContact() {
		return selectedContact;
	}

	public void setSelectedContact(List<Contact> selectedContact) {
		this.selectedContact = selectedContact;
	}

	public boolean isContactAddedSuccess() {
		return contactAddedSuccess;
	}

	public void setContactAddedSuccess(boolean contactAddedSuccess) {
		this.contactAddedSuccess = contactAddedSuccess;
	}

	public void setChecked2(Map<String, Boolean> checked2) {
		this.checked2 = checked2;
	}

	public String getFirstName() {
		return firstName;
	}

	public List<Contact> getIndividualContactList() {
		return individualContactList;
	}

	public void setIndividualContactList(List<Contact> individualContactList) {
		this.individualContactList = individualContactList;
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

	public boolean isGoTohistory() {
		return goTohistory;
	}

	public void setGoTohistory(boolean goTohistory) {
		this.goTohistory = goTohistory;
	}

	public boolean isSaveButtonFlag() {
		return saveButtonFlag;
	}

	public void setSaveButtonFlag(boolean saveButtonFlag) {
		this.saveButtonFlag = saveButtonFlag;
	}

	public String getChangeSenderName() {
		return changeSenderName;
	}

	public void setChangeSenderName(String changeSenderName) {
		this.changeSenderName = changeSenderName;
	}

	public Boolean getReadOnly() {
		boolean test = FacesContext.getCurrentInstance().getRenderResponse();
		return test;
		// return readOnly;
	}

	public String getTestStartdate() {
		return testStartdate;
	}

	public boolean isStopTimeFlag() {
		return stopTimeFlag;
	}

	public void setStopTimeFlag(boolean stopTimeFlag) {
		this.stopTimeFlag = stopTimeFlag;
	}

	public void setTestStartdate(String testStartdate) {
		this.testStartdate = testStartdate;
	}

	public boolean isEndDateFlag() {
		return endDateFlag;
	}

	public Date getFinalEndDate() {
		return finalEndDate;
	}

	public void setFinalEndDate(Date finalEndDate) {
		this.finalEndDate = finalEndDate;
	}

	public void setEndDateFlag(boolean endDateFlag) {
		this.endDateFlag = endDateFlag;
	}

	public String getListNameSearch() {
		return listNameSearch;
	}

	public void setListNameSearch(String listNameSearch) {
		this.listNameSearch = listNameSearch;
	}

	public List<Template> getTemplateList() {
		return templateList;
	}

	public void setTemplateList(List<Template> templateList) {
		this.templateList = templateList;

	}

	public int getNumOfSelectedLists() {
		return numOfSelectedLists;
	}

	public void setNumOfSelectedLists(int numOfSelectedLists) {
		this.numOfSelectedLists = numOfSelectedLists;
	}

	public void setListInfo(List<ContactListInfo> listInfo) {
		this.listInfo = listInfo;
	}

	public List<ContactListInfo> getSelectedList() {
		return selectedList;
	}

	public void setSelectedList(List<ContactListInfo> selectedList) {
		this.selectedList = selectedList;
	}

	public Date getFinalStartDate() {
		return finalStartDate;
	}

	public void setFinalStartDate(Date finalStartDate) {
		this.finalStartDate = finalStartDate;
	}

	public Date getFinalStopTime() {
		return finalStopTime;
	}

	public List<String> getSendersList() {
		return sendersList;
	}

	public void setSendersList(List<String> sendersList) {
		this.sendersList = sendersList;
	}

	public void setFinalStopTime(Date finalStopTime) {
		this.finalStopTime = finalStopTime;
	}

	public String getTestList() {
		return testList;
	}

	public void setTestList(String testList) {
		this.testList = testList;
	}

	public Map<Integer, Boolean> getChecked() {
		return checked;
	}

	public void setChecked(Map<Integer, Boolean> checked) {
		this.checked = checked;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public String getChoosenTemplate() {
		return choosenTemplate;
	}

	public void setChoosenTemplate(String choosenTemplate) {
		this.choosenTemplate = choosenTemplate;
	}

	public boolean isTemplateDiv() {
		return templateDiv;
	}

	public void setTemplateDiv(boolean templateDiv) {
		this.templateDiv = templateDiv;
	}

	public String getChoosenFrequency() {
		return choosenFrequency;
	}

	public void setChoosenFrequency(String choosenFrequency) {
		this.choosenFrequency = choosenFrequency;
	}

	public boolean isOnceFlag() {
		return onceFlag;
	}

	public void setOnceFlag(boolean onceFlag) {
		this.onceFlag = onceFlag;
	}

	public boolean isFrequencyFlag() {
		return frequencyFlag;
	}

	public void setFrequencyFlag(boolean frequencyFlag) {
		this.frequencyFlag = frequencyFlag;
	}

	public boolean isTemplateFlag() {
		return templateFlag;
	}

	public void setTemplateFlag(boolean templateFlag) {
		this.templateFlag = templateFlag;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getStartDateAndTime() {
		return startDateAndTime;
	}

	public void setStartDateAndTime(String startDateAndTime) {
		this.startDateAndTime = startDateAndTime;
	}

	public String getStopTime() {
		return stopTime;
	}

	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getLanguageSelected() {
		return languageSelected;
	}

	public void setLanguageSelected(String languageSelected) {
		this.languageSelected = languageSelected;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isStartDateAndTimeFlag() {
		return startDateAndTimeFlag;
	}

	public void setStartDateAndTimeFlag(boolean startDateAndTimeFlag) {
		this.startDateAndTimeFlag = startDateAndTimeFlag;
	}

	public boolean isScheduleFlag() {
		return scheduleFlag;
	}

	public void setScheduleFlag(boolean scheduleFlag) {
		this.scheduleFlag = scheduleFlag;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public boolean isIntraSenderNameChoosen() {
		return intraSenderNameChoosen;
	}

	public void setIntraSenderNameChoosen(boolean intraSenderNameChoosen) {
		this.intraSenderNameChoosen = intraSenderNameChoosen;
	}

	// Constructor------------------------------------------------------------

	public CampaignBean() {
		 UserTrxInfo userInfo = new UserTrxInfo();
	        String trxId = TrxId.getTrxId();
	        try {
	            ThreadContext.push(trxId);
	            userInfo.setTrxId(trxId);
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
//			String logID = (String)Configs.LOGIN_ID.getValue();
//        	loginTrxId=  (String) request.getSession().getAttribute(logID);
			accManagUser.setAccountId(userAcc.getAccount().getAccountId());
			accManagUser.setUsername(userAcc.getUsername());
			rowsPerPage = 7; // Default rows per page (max amount of rows to be
			// displayed at once).
			rowsPerPage2 = 6;

			listInfo = new ArrayList<ContactListInfo>();
			individualContactList = new ArrayList<Contact>();
		} catch (Exception e) {
			userLogger.error("Exception while constructing campaign bean", e);
			appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.pop();
		}
	}

	@PostConstruct
	private void polpulateList() {
		AccountUserTrxInfo userInfo = new AccountUserTrxInfo();
		String trxId = TrxId.getTrxId();
		
		userInfo.setTrxId(trxId);
		userInfo.setUser(accManagUser);
		try {
	        ThreadContext.push(trxId);
			try {
				sendersList = new ArrayList<String>();
				sendersList.addAll(userAcc.getAccount().getSender());

				/*
				 * calling find account full info to return account with all
				 * intra senders (system and his own) to show in sender name
				 * panel
				 */
				userLogger.info("Getting account by company name to get all senders. ");

				accountManegmentServicePort = portObj.getAccountServicePort();
				AccountResultFullInfo accResult = accountManegmentServicePort.findAccountByCompanyNameFullInfo(
						userInfo, userAcc.getAccount().getCompanyName());
				com.edafa.web2sms.service.acc_manag.enums.ResponseStatus status = accResult.getStatus();

				userLogger.info("Getting account by company name finished by status: (" + status + ").");

				switch (status) {
				case SUCCESS:
					intraSendersList = accResult.getAccount().getIntraSender();
					if (intraSendersList != null)
						for (int i = 0; i < intraSendersList.size(); i++) {
							if (!sendersList.contains(intraSendersList.get(i) + " (Intra)")) {
								sendersList.add(intraSendersList.get(i) + " (Intra)");
								// if
								// (!userAcc.getAccount().getIntraSender().contains(intraSenders.get(i)))
								// userAcc.getAccount().getIntraSender().add(intraSenders.get(i));
							}
						}

					break;

				case FAIL:
					userLogger.error( "Getting account by company name finished by status: (" + status + ").");

					break;
				case ACCT_NOT_EXIST:
					userLogger.error("Getting account by company name finished by status: (" + status + ").");
					break;
				default:
					userLogger.error("Getting account by company name finished by undefined status: (" + status + ").");
					break;
				}

				// userLogger.info(userLogInfo(user.getAccountId(),
				// user.getUsername()) + "getting intra senders");
				// for (int i = 0; i <
				// userAcc.getAccount().getIntraSender().size(); i++) {f
				// {
				// String intraSender =
				// userAcc.getAccount().getIntraSender().get(i) + " (Intra)";
				// sendersList.add(intraSender);
				// }
				//
				// }

				if (sendersList.size() == 1) {
					senderNameNotSelected = false;
					individualContacts = true;
					senderName = sendersList.get(0);
					choosenSender = senderName;
					if (sendersList.get(0).contains("(Intra)")) {
						intraSenderNameChoosen = true;
						individualContacts = false;
					}

					userLogger.info(" This user has one sende "
							+ senderName);
					userLogger.info("senderNameNotSelected: "
							+ senderNameNotSelected + " individualContacts: " + individualContacts
							+ " intraSenderNameChoosen: " + intraSenderNameChoosen + " individualContacts:"
							+ individualContacts);

				}
				if (sendersList.size() > 1) {
					String sendersStrings = "";
					for (String s : sendersList) {
						sendersStrings = sendersStrings + s + " , ";
					}
					userLogger.info("User has many senders=["
							+ sendersStrings + "]");
				} else {
					senderName = sendersList.get(0);
					userLogger.info("User sender=" + senderName);
				}
				tempChecked();
			} catch (Exception e) {
				userLogger.error("Error while setting sender", e);
                appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			}

			serviceProvisioningPort = portObj.getServiceProvisioningPort();

			campaignServicePort = portObj.getCampaignServicePort();

			selectedList = new ArrayList<ContactListInfo>();
			selectedContact = new ArrayList<Contact>();

			checked = new HashMap<Integer, Boolean>();
			checked2 = new HashMap<String, Boolean>();
			checked3 = new HashMap<String, Boolean>();
			individualContactList = new ArrayList<Contact>();
			newIndividualList = new ArrayList<Contact>();
			archiveList = new ArrayList<ContactListInfo>();

		} catch (Exception e) {
			userLogger.error("Error while setting service ports", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void checkSenderName(AjaxBehaviorEvent e) {
		if (senderNameNotSelected)
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("choose_sender_name")), null));
	}

	public void callPopulateListOfCont() {
		listInfo = new ArrayList<ContactListInfo>();
		individualContactList = new ArrayList<Contact>();
		if (listInfo == null || listInfo.isEmpty()) {
			populateListsOfContacts();
		}

	}

	public void populateListsOfContacts() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
			listsManegementServicePort = portObj.getListsManegementServicePort();
			List<ListType> typesList = new ArrayList<ListType>();
			if (intraSenderNameChoosen) {
				typesList.add(ListType.INTRA_LIST);
				typesList.add(ListType.INTRA_SUB_LIST);
				userLogger.debug("list types: intra and sub intra.");
			} else if (customizeCampFlag) {
				typesList.add(ListType.CUSTOMIZED_LIST);
				userLogger.debug( "getting customized lists only.");
			} else {
				typesList.add(ListType.VIRTUAL_LIST);
				typesList.add(ListType.NORMAL_LIST);
				typesList.add(ListType.INTRA_LIST);
				typesList.add(ListType.INTRA_SUB_LIST);
				typesList.add(ListType.CUSTOMIZED_LIST);
				userLogger.debug("normal sender name, getting all lists.");
			}

			userLogger.debug("Getting all lists of contacts from=" + firstRow + " getting rows_count=" + rowsPerPage);
			ContactListInfoResultSet result = listsManegementServicePort.getContactListsInfo(userInfo, typesList);

			ResponseStatus state = result.getStatus();

			switch (state) {
			case SUCCESS:
				if (searchMode == true) {
					listInfo.clear();
					for (ContactListInfo contactListInfo : archiveList) {
						listInfo.add(contactListInfo);
					}
					searchMode = false;
				} else {
					listInfo = new ArrayList<ContactListInfo>();
					archiveList = new ArrayList<ContactListInfo>();
					for (ContactListInfo contactListInfo : result.getContactListInfoResultSet()) {
						if (contactListInfo.getListType().equals(ListType.VIRTUAL_LIST)) {
							ContactResultSet contact = listsManegementServicePort.getContactList(userInfo,
									contactListInfo.getListId());
							ResponseStatus status = contact.getStatus();
							userLogger.info("listsManegementServicePort response is:(" + status + ").");
							switch (status) {
							case SUCCESS:
								individualContactList.addAll(contact.getContacts());
								userLogger.info("individual list contacts is set successfully with rows_count="
										+ individualContactList.size());
								break;
							case FAIL:
								userLogger.info("Error while getting list of individual contacts");
								break;
							case INELIGIBLE_ACCOUNT:
								userLogger.info("INELIGIBLE_ACCOUNT, not allowed to get individual contacts");
								break;
							default:
								userLogger.info("Undefined response status=" + status + " while getting individual contacts");
								break;
							}
						} else {

							listInfo.add(contactListInfo);
							archiveList.add(contactListInfo);
							userLogger.trace("Getting list_id="
									+ contactListInfo.getListId() + " ,list_name=" + contactListInfo.getListName()
									+ " and added to list info and archive list");
						}
					}

				}

				break;
			case FAIL:
				listInfo = null;
				userLogger.info("Error while populating list of contat lists");
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.info("INELIGIBLE_ACCOUNT, not allowed to display list of contacts");
			default:
				userLogger.info( "Unkown response status=" + state + " for populating list of contact lists");
				break;
			}

		} catch (Exception e) {
			userLogger.error( "Error while populating list", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		//
		// System.out.println(listInfo.size());
		// for (int i = 0; i < listInfo.size(); i++)
		// System.out.println(listInfo.get(i).getListName());

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

	// Validate date -----------------------------------------------------------

	// stop logging

	/**
	 * Validate date
	 * 
	 * @return boolean, true is valid ,false is not valid
	 * @param string
	 *            date
	 */
	private boolean validateDate() {
		userLogger.debug( "Validating date for edited campaign [campaign_name=" + campaignName + ",start_date="
				+ startDateAndTime + ",end_date=" + endDate + ",stop_date=" + stopTime + ",sender_name=" + senderName
				+ ",schedule_flag=" + scheduleFlag + ",frequency=" + choosenFrequency + ",text=" + encyrptionUtil.encrypt(text) + "]");
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		try{
	        ThreadContext.push(trxId);
		
		if (scheduleFlag == false) {
			userLogger.debug( "campaign_name=" + campaignName
					+ " is starting as soon as possible");
			if (frequencyFlag == true) {
				// check end date
				userLogger.debug( "campaign_name=" + campaignName
						+ " has a frequency=" + choosenFrequency);

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
				String tempEndDate = endDate;
				Calendar c = Calendar.getInstance();

				try {
					finalEndDate = formatter.parse(tempEndDate);

					if (tempEndDate == null || tempEndDate.trim().isEmpty()) {
						userLogger.debug( "campaign_name="
								+ campaignName + " has no end date");

						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("end_date_required")), null));
						return false;
					} else {
						c.clear(Calendar.HOUR);
						c.clear(Calendar.MINUTE);
						c.clear(Calendar.SECOND);
						c.clear(Calendar.MILLISECOND);
						if (finalEndDate.before(c.getTime())) {
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("end_date_invalid")), null));
							userLogger.info( "campaign_name="
									+ campaignName + " end date is before current date" + " ,end_date=" + finalEndDate
									+ " current_date=" + c.getTime());

							return false;
						}
						return true;
					}
				} catch (Exception e) {
					userLogger.error( "campaign_name="
							+ campaignName + " ,error while pasring end_date=" + tempEndDate, e);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_date")), null));
                                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
					return false;
				}
			} else {
				// now all
				userLogger.debug( "campaign_name=" + campaignName
						+ " is starting as soon as possible with stop_date=null and  end_date=null");

				startDateAndTime = null;
				stopTime = null;
				endDate = null;
				userLogger.info( " creating as soon as posible campaign");
				return true;
			}
		} else {
			if (frequencyFlag == true) {
				// scheduled with frequency
				userLogger.debug( "campaign_name=" + campaignName
						+ " is scheduled with frequency");

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
				SimpleDateFormat endDateformatter = new SimpleDateFormat("dd/MM/yyyy");
				String tempStartDate = startDateAndTime;
				String tempStopTime = stopTime;
				String tempEndDate = endDate;
				Calendar c = Calendar.getInstance();
				Calendar ct = Calendar.getInstance();
				Calendar cd = Calendar.getInstance();
				Calendar cdd = Calendar.getInstance();

				try {
					finalStartDate = formatter.parse(tempStartDate);
					finalEndDate = endDateformatter.parse(tempEndDate);

					if (tempStopTime == null || tempStopTime.trim().isEmpty()) {
						userLogger.debug( "campaign_name="
								+ campaignName + " stop time is set to null");
						finalStopTime = null;
					} else {
						finalStopTime = timeFormatter.parse(tempStopTime);
						ct.setTime(finalStopTime);

						userLogger.debug( "campaign_name="
								+ campaignName + " stop_time=" + finalStopTime);
					}

					c.clear(Calendar.SECOND);
					c.clear(Calendar.MILLISECOND);

					ct.set(2014, 01, 01);
					cd.setTime(finalStartDate);
					cd.set(2014, 01, 01);

					if (!finalStartDate.equals(c.getTime())) {
						if (finalStartDate.before(c.getTime())) {
							userLogger.debug( "campaign_name="
									+ campaignName + " start date don't equal current date" + " ,start_date="
									+ finalStartDate + " current_date=" + c.getTime());

							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("start_date_invalid")), null));

							return false;
						} else if (finalStopTime != null) {
							if (ct.before(cd)) {
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("stop_time_invalid")), null));
								userLogger.info(" trying to submit campaign with end date before now");
								return false;
							}
						}
					}

					if (finalStopTime != null) {
						userLogger.info( "campaign_name="
								+ campaignName + " stop time is not null, stop_time=" + finalStopTime);
						if (ct.before(cd)) {
							userLogger.info( "campaign_name="
									+ campaignName + " stop time is after start time" + " ,stop_time=" + finalStopTime
									+ " start_date=" + finalStartDate);
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("stop_time_invalid")), null));

							return false;
						}
					}

					if (tempEndDate == null || tempEndDate.trim().isEmpty()) {
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("end_date_required")), null));

						userLogger.info( "campaign_name="
								+ campaignName + " no end date");

						return false;
					} else {
						cdd.clear(Calendar.HOUR);
						cdd.clear(Calendar.MINUTE);
						cdd.clear(Calendar.SECOND);
						cdd.clear(Calendar.MILLISECOND);
						if (finalEndDate.before(c.getTime())) {
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("end_date_invalid")), null));

							userLogger.info( "campaign_name="
									+ campaignName + " end date is before current date, end_date=" + finalEndDate
									+ " current_date=" + c.getTime());

							return false;
						} else if (finalEndDate.before(finalStartDate)) {
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("end_date_before_start_date")), null));
							userLogger.info( "campaign_name="
									+ campaignName + " end date is before start date, end_date=" + finalEndDate
									+ " start_date=" + finalStartDate);

							return false;
						}
						userLogger.info( "campaign_name="
								+ campaignName + " ,dates is validates successfully");

					}
					return true;
				} catch (ParseException e) {
					userLogger.error("Error while validating date for campaign_name=" + campaignName, e);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_date")), null));
                                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
                                        return false;
				}

			} else {
				// scheduled no frequency
				userLogger.info( "campaign_name=" + campaignName
						+ " ,is scheduled but with no frequency");

				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
				String tempStartDate = startDateAndTime;
				String tempStopTime = stopTime;
				Calendar c = Calendar.getInstance();
				Calendar ct = Calendar.getInstance();
				Calendar cd = Calendar.getInstance();

				try {
					finalStartDate = formatter.parse(tempStartDate);

					if (tempStopTime == null || tempStopTime.trim().isEmpty()) {
						userLogger.info( "campaign_name="
								+ campaignName + " ,stop time is set null");
						finalStopTime = null;
					} else {
						finalStopTime = timeFormatter.parse(tempStopTime);
						ct.setTime(finalStopTime);

						userLogger.info( "campaign_name="
								+ campaignName + " ,stop time is set, stop_time=" + finalStopTime);
					}

					c.clear(Calendar.SECOND);
					c.clear(Calendar.MILLISECOND);

					ct.set(2014, 01, 01);
					cd.setTime(finalStartDate);
					cd.set(2014, 01, 01);

					if (!finalStartDate.equals(c.getTime())) {
						if (finalStartDate.before(c.getTime())) {
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("start_date_invalid")), null));
							userLogger.info( "campaign_name="
									+ campaignName + " ,start date is before current date ,start_date="
									+ finalStartDate + " current_date=" + c.getTime());

							return false;
						} else if (finalStopTime != null) {
							if (ct.before(cd)) {
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("stop_time_invalid")), null));
								userLogger.info( "campaign_name="
										+ campaignName + " stop time is after start time" + " ,stop_time="
										+ finalStopTime + " start_date=" + finalStartDate);
								return false;
							}
							return true;
						} else {
							return true;
						}
					} else if (finalStopTime != null) {
						if (ct.before(cd)) {
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("stop_time_invalid")), null));
							userLogger.info( "campaign_name="
									+ campaignName + " stop time is after start time" + " ,stop_time=" + finalStopTime
									+ " start_date=" + finalStartDate);
							return false;
						}

						return true;
					} else {
						userLogger.info( "campaign_name="
								+ campaignName + " ,Date is validated successfully");
						return true;
					}
				} catch (ParseException e) {
					userLogger.error( "Error while validating date for campaign_name=" + campaignName, e);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_date")), null));
                      appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
					return false;
				}
			}
		}
		}finally{
            ThreadContext.pop();
		}
	}

	/**
	 * validation method to make fields required
	 * 
	 * @return boolean true or false
	 */
	private boolean checkRequiredField() {
//		UserTrxInfo userInfo = new UserTrxInfo();
//		String trxId = TrxId.getTrxId();
//		userInfo.setTrxId(trxId);
		
		if (campaignName.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_name_required")), null));
			userLogger.info( "Trying to create campaign without campaign name");
			return false;
		} else if (selectedList.isEmpty() && selectedContact.isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("list_contacts_required")), null));
			userLogger.info( "Trying to create campaign campaign_name=" + campaignName
					+ " without choosing any list or adding contacts");
			return false;

		} else if (text.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							String.format(Util.getLocalizedLabel("text_required")), null));
			userLogger.info( "Trying to create campaign campaign_name=" + campaignName + " without sms text");
			return false;
		} else if (!onceFlag && choosenFrequency == null) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("frequency_required")), null));
			userLogger.info( "Trying to create campaign_name="
					+ campaignName + " without choosing frequency");
			return false;
		} else if (!onceFlag && !choosenFrequency.trim().isEmpty() && endDate.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("end_date_required")), null));
			userLogger.info("Trying to create campaign campaign_name=" + campaignName
					+ " with frequency but without end date");
			return false;
		} else if (!onceFlag && choosenFrequency.trim().isEmpty() && endDate.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("frequency_required")), null));
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("end_date_required")), null));
			userLogger.info( "Trying to create campaign camapign_name=" + campaignName
					+ " without choosing frequency and entering end date");
			return false;
		} else if (scheduleFlag && startDateAndTime.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							String.format(Util.getLocalizedLabel("invalid_date")), null));
			userLogger.info( "Trying to create  scheduled campaign_name=" + campaignName + " without start date");
			return false;

		} else if (senderName == null || senderName.trim().equals("")) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("invalid_sender_name")), null));
			userLogger.info( "sender_name=" + senderName
					+ " it is empty");
			return false;
		} else {
			userLogger
					.info( "Trying to create camapign campaign_name=" + campaignName
							+ " with successfully fullfiled fields");
			return true;
		}

	}

	private boolean checkFieldLenght() {
//		UserTrxInfo userInfo = new UserTrxInfo();
		try {
//			userInfo.setTrxId(TrxId.getTrxId());
			if (campaignName.length() > 100) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_campaign_name_lenght")), null));
				userLogger.info( "Trying to create campaign with invalid number of characters for the campaign_name="
						+ campaignName);
				return false;
			} else if (text.length() > 4000) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_campaign_text_lenght")), null));
				userLogger.info( "Trying to create campaign campaign_name=" + campaignName
						+ " with invalid number of characters for the campaign sms text=" + encyrptionUtil.encrypt(text));
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance()
					.addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR,
									String.format(Util.getLocalizedLabel("error")), null));
			userLogger.error("Error while checking field lenght for campaign campaign_name=" + campaignName, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return false;
		}
	}

	// Check
	// methods------------------------------------------------------------------

	public String getRemovedList(String testList) {
		try {
			for (int i = 0; i < selectedList.size(); i++) {
				userLogger.info("Trying to create campaign campaign_name=" + campaignName + "and remove list "
						+ selectedList.get(i).getListId() + " from selected lists");
				if (testList.equals(selectedList.get(i).getListName())) {
					numOfSelectedLists = numOfSelectedLists - selectedList.get(i).getContactsCount();
					checked.put(selectedList.get(i).getListId(), false);
					selectedList.remove(i);
				}
			}
			if (numOfSelectedLists > 0) {
				contactsSelected = true;
				userLogger.info( " Count of Selected contacts : "
						+ numOfSelectedLists);
			} else
				contactsSelected = false;
		} catch (Exception e) {
			userLogger.error(" Error while trying to create campaign campaign_name=" + campaignName
					+ "and remove list from selected lists");
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}

		return "";
	}

	public String getRemovedContact(String testMsisdn) {
		try {
			for (int i = 0; i < selectedContact.size(); i++) {
				userLogger.info( "Selected contact with msisdn "
						+ encyrptionUtil.encrypt(selectedContact.get(i).getMsisdn()) + " is removed from the selected contact list");
				if (testMsisdn.equals(selectedContact.get(i).getMsisdn())) {
					numOfSelectedLists = numOfSelectedLists - 1;
					checked2.put(selectedContact.get(i).getMsisdn(), false);
					selectedContact.remove(i);
				}
				if (numOfSelectedLists > 0) {
					contactsSelected = true;
					userLogger.info(" Count of Selected contacts : " + numOfSelectedLists);
				} else
					contactsSelected = false;
			}
		} catch (Exception e) {
			userLogger.error("Error while removing selected contact from contact list");
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
		return "";
	}

	/**
	 * for value change listener to get if the box is checked or not
	 * 
	 * @return void
	 */
	public void dateChecked(ValueChangeEvent e) {
		if (e.getNewValue().toString().equals("true")) {
			saveButtonFlag = true;
			startDateAndTimeFlag = true;
			stopTimeFlag = true;
			if (frequencyFlag == true) {
				endDateFlag = true;
			}
		} else if (e.getNewValue().toString().equals("false")) {
			saveButtonFlag = false;
			stopTimeFlag = false;
			startDateAndTimeFlag = false;
			if (frequencyFlag == true) {
				endDateFlag = true;
			}
		}
	}

	/**
	 * for value change listener to get if the box is checked or not
	 * 
	 * @return void
	 */
	public void frequencyChecked(ValueChangeEvent e) {
		if (e.getNewValue().toString().equals("true")) {
			frequencyFlag = false;
			endDateFlag = false;
		} else {
			endDateFlag = true;
			frequencyFlag = true;

		}
	}

	public void isTempChecked(ValueChangeEvent e) {

		if (e.getNewValue().toString().equals("true")) {
			templateChecked = true;
		} else {
			templateChecked = false;
		}
	}

	/**
	 * for value change listener to get if the box is checked or not
	 * 
	 * @return void
	 */
	public void tempChecked() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			templateList = new ArrayList<Template>();

			userLogger.debug( "Setting TemplateManegmentService port............");
			templateManegmentServicePort = portObj.getTemplateManegmentServicePort();
			userLogger.info("Done Setting TemplateManegmentService port");

			userLogger.info( "Calling get user templates....");
			TemplatesResultSet tempResult = templateManegmentServicePort.getUserAndAdminTemplates(userInfo);
			ResponseStatus status = tempResult.getStatus();
			userLogger.info( "templateManegmentServicePort response is:(" + status + ").");

			switch (status) {
			case SUCCESS:
				templateList = tempResult.getTemplateList();
				userLogger.info("Template list populate successfully, count=" + templateList.size());
				break;
			case FAIL:
				userLogger.error( "Error while getting user template list");
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.info( "INELIGIBLE_ACCOUNT, not allowed to display the list of templates");
				break;
			case TEMPLATES_NOT_FOUND:
				userLogger.info( "TEMPLATES_NOT_FOUND");
				break;
			case INVALID_REQUEST:
				userLogger.info( "INVALID_REQUEST for retrieving templates");
				break;
			default:
				userLogger.error( "Undefined status=" + status + " while getting template list");

				break;
			}

		} catch (Exception ex) {
			userLogger.error( "Error while getting user template list", ex);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void getTempClicked(ValueChangeEvent e) {

		text = e.getNewValue().toString();
	}

	// Action
	// methods--------------------------------------------------------------

	/**
	 * for creating a campaign and submitting the form
	 * 
	 * @return string
	 */
	public void createCampaign(AjaxBehaviorEvent e) {

//		UserTrxInfo userInfo = new UserTrxInfo();
//		createCampaignUserInfo
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		
		UserTrxInfo userInfo;
		userInfo = (UserTrxInfo) session.getAttribute("createCampaignUserInfo");
		String trxId;
		if(userInfo == null)
		{
			userInfo = new UserTrxInfo();
			trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
		}else {
			 trxId = userInfo.getTrxId();
		}
//		String logTrxId = StringUtils.getUIcompositTrxId(loginTrxId, trxId);

		SubmittedCampaign campaign = new SubmittedCampaign();
		if (languageSelected.equals("true")) {
			languageSelected = "ENGLISH";
		} else {
			languageSelected = "ARABIC";
		}
		try {
	        ThreadContext.push(trxId);
			if (checkRequiredField()) {
				if (checkFieldLenght()) {
					if (validateDate()) {

						if (validateCustmoizeSmsText()) {

							campaign.setCampaignName(campaignName);
							campaign.setLanguage(Language.valueOf(languageSelected));
							campaign.setSmsText(text);
							if (senderName.contains(" (Intra)")) {
								String[] sender = senderName.split(" ");
								senderName = sender[0];
							}
							campaign.setSenderName(senderName);

							campaign.setRegisteredDelivery((Boolean) Configs.REGESTERED_DELIVERY.getValue());
							// campaign.getIndividualContact()

							if (!selectedList.isEmpty()) {
								for (int i = 0; i < selectedList.size(); i++) {
									campaign.getContactList().add(selectedList.get(i).getListId());
									// TODO add logging
								}
							}

							if (!selectedContact.isEmpty()) {
								for (int i = 0; i < selectedContact.size(); i++) {
									campaign.getIndividualContact().add(selectedContact.get(i));
								}
							}

							if (onceFlag) {
								campaign.setScheduleFrequency(ScheduleFrequency.ONCE);
							} else {
								campaign.setScheduleFrequency(ScheduleFrequency.valueOf(choosenFrequency));
							}

							if (scheduleFlag == false) {
								if (frequencyFlag == true) {
									campaign.setScheduleStartTimestamp(fromDateToString(new Date()));
									campaign.setScheduleStopTime(null);
									campaign.setScheduleEndTimestamp(fromDateToString(finalEndDate));
									campaign.setScheduledFlag(false);
								} else {
									// now
									campaign.setScheduleStartTimestamp(fromDateToString(new Date()));
									campaign.setScheduleStopTime(null);
									campaign.setScheduleEndTimestamp(null);
									campaign.setScheduledFlag(false);
								}
							} else {
								// later
								campaign.setScheduleStartTimestamp(fromDateToString(finalStartDate));
								if (finalStopTime == null) {
									campaign.setScheduleStopTime(null);
								} else {
									campaign.setScheduleStopTime(fromDateToString(finalStopTime));
								}

								if (finalEndDate == null) {
									campaign.setScheduleEndTimestamp(null);
								} else {
									campaign.setScheduleEndTimestamp(fromDateToString(finalEndDate));
								}

								campaign.setScheduledFlag(true);
							}

							if (intraSenderNameChoosen)
								campaign.setCampaignType(CampaignType.INTRA_CAMPAIGN);
							else if (customizeCampFlag) {
								campaign.setCampaignType(CampaignType.CUSTOMIZED_CAMPAIGN);
							} else
								campaign.setCampaignType(CampaignType.NORMAL_CAMPAIGN);

							userInfo.setUser(user);

							userLogger.debug( "create campaign [campaign_name=" + campaign.getCampaignName() + ",start_date="
									+ campaign.getScheduleStartTimestamp() + ",end_date="
									+ campaign.getScheduleEndTimestamp() + ",stop_date="
									+ campaign.getScheduleStopTime() + ",sender_name=" + campaign.getSenderName()
									+ ",schedule_flag=" + campaign.isScheduledFlag() + ",frequency="
									+ campaign.getScheduleFrequency().name() + ",text=" + encyrptionUtil.encrypt(campaign.getSmsText())
									+ ",individual_contacts_count=" + campaign.getIndividualContact().size()
									+ ",contacts_lists_size=" + campaign.getContactList().size() + ",language="
									+ campaign.getLanguage().name() + ", campaign type= "
									+ campaign.getCampaignType().value() + "]");

							CampaignResult status = campaignServicePort.createCampaign(userInfo, campaign);
							ResponseStatus response = status.getStatus();
							userLogger.debug( " Creating Campagin result status: ( "
									+ status.getStatus() + "), ErrorMessage: (" + status.getErrorMessage() + " ).");

							switch (response) {
							case INVALID_CAMPAIGN:
								for (int j = 0; j < status.getCampaignValidationStatus().size(); j++) {
									CampaignValidationStatus resp = status.getCampaignValidationStatus().get(j);
									switch (resp) {
									case DUPLICAT_CAMPAIGN_NAME:
										FacesContext.getCurrentInstance().addMessage(
												null,
												new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
														.getLocalizedLabel("invalid_campaign_name")), null));

										userLogger.info( "Trying to create with duplicated campaign_name="
												+ campaign.getCampaignName());

										break;

									case INVALID_SCHEDULING:
										FacesContext.getCurrentInstance().addMessage(
												null,
												new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
														.getLocalizedLabel("invalid_date")), null));

										userLogger.info( "Trying to create campaign_name=" + campaign.getCampaignName()
												+ " with invalid schedule");

										break;

									case INVALID_SENDER_NAME:
										FacesContext.getCurrentInstance().addMessage(
												null,
												new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
														.getLocalizedLabel("invalid_sender_name")), null));

										userLogger.info( "Trying to create camaign_name=" + campaign.getCampaignName()
												+ " with invalid sender_name=" + campaign.getSenderName());

										break;

									case INVALID_SMS_TEXT:
										FacesContext.getCurrentInstance().addMessage(
												null,
												new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
														.getLocalizedLabel("invalid_campaign_text_lenght")), null));

										userLogger.info("Trying to create campaign_name=" + campaign.getCampaignName()
												+ " with invalid sms_text=" + encyrptionUtil.encrypt(campaign.getSmsText()));

										break;
									default:
										FacesContext.getCurrentInstance().addMessage(
												null,
												new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
														.getLocalizedLabel("campaign_created_failure")), null));

										userLogger.info( "Unknown status=" + resp + " while trying to create campaign_name="
												+ campaign.getCampaignName());
										break;
									}// end switch

								}// end for
								break;
							case INELIGIBLE_ACCOUNT:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("invalid_account_state")), null));

								userLogger.info( "INELIGIBLE_ACCOUNT ,not allowed to create campaign_name="
										+ campaign.getCampaignName());

								break;
							case FAIL:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("campaign_created_failure")), null));

								userLogger.info( "Error while trying to create campaign_name=" + campaign.getCampaignName()
										+ status.getErrorMessage());

								break;
							case CAMPAIGN_NOT_FOUND:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("campaign_created_failure")), null));

								userLogger.info( "Trying to create campaign_name=" + campaign.getCampaignName()
										+ " that is not found");

								break;
							case INVALID_CAMPAIGN_STATE:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("invalid_campaign_state")), null));

								userLogger.info( "Trying to create campaign_name=" + campaign.getCampaignName()
										+ " with uneditable status");

								break;

							case INVALID_CAMPAIGN_LIST:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("invalid_campaign_list")), null));

								userLogger.info( "Error while create campaign_name=" + campaign.getCampaignName()
										+ " because of empty list");

								break;

							case SUCCESS:
								// FacesContext.getCurrentInstance().addMessage(
								// null,
								// new
								// FacesMessage(FacesMessage.SEVERITY_INFO,
								// String.format(Util
								// .getLocalizedLabel("campaign_created_successfully")),
								// null));
								goTohistory = true;

								userLogger.info( "Campaign_name="
										+ campaign.getCampaignName() + " is created successfully");

								break;
							case INVALID_REQUEST:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("campaign_created_failure")), null));

								userLogger.info( "Invalid request while trying to create campaign_name="
										+ campaign.getCampaignName());

								break;
							case ACCOUNT_QUOTA_NOT_FOUND:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("account_quota_not_found")), null));

								userLogger.info( "account quota record not found for this account ");

								break;

							case ACCOUNT_QUOTA_EXCEEDED:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("account_quota_exceed")), null));

								userLogger.info( "account_quota_exceed while creating campaing = "
										+ campaign.getCampaignName());

								break;

							// case a
							default:
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("campaign_created_failure")), null));

								userLogger.info( "Unknown response="
										+ response + " while trying to create campaign_name="
										+ campaign.getCampaignName());

								break;

							}// end big switch

						}
					}

				}
			}

		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_created_failure")), null));
			userLogger.error("Error while creating campaign_name=" + campaign.getCampaignName(), ex);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void clearForm(AjaxBehaviorEvent e) {
		setText(null);
		setStartDateAndTime(null);
		setEndDate(null);
		setCampaignName(null);
	}

	/*
	 * Converts XMLGregorianCalendar to java.util.Date in Java
	 */
	public static Date toDate(XMLGregorianCalendar calendar) {
		if (calendar == null) {
			return null;
		}
		return calendar.toGregorianCalendar().getTime();
	}

	public void getSelectedLists(AjaxBehaviorEvent e) {
		UserTrxInfo userInfo;
		String trxId;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		userInfo = (UserTrxInfo) session.getAttribute("createCampaignUserInfo");
		if (userInfo == null) {
			userInfo = new UserTrxInfo();
			trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
			session.setAttribute("createCampaignUserInfo", userInfo);
		}else {
			trxId = userInfo.getTrxId();
		}
		List<ContactListInfo> allLists = new ArrayList<ContactListInfo>();
		try {
	        ThreadContext.push(trxId);
			userLogger.debug("Trying to get all lists with their ids");
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
				allLists = resultSet.getContactListInfoResultSet();
				userLogger.info("list of contact lists is populating successfully with count=" + allLists.size()
						+ " to find the checked list from it");

				break;
			case FAIL:
				userLogger.info("list of contact lists is not populated");
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.info(" is not allowed to display lists of contact lists");
				break;

			default:
				userLogger.info("Unknown response status " + resultSet.getStatus()
						+ " , trying to populate list of contact lists");
				break;
			}

			for (Contact contact : newIndividualList) {
				userLogger.trace( "adding new contacts to virtual list contact_msisdn=" + encyrptionUtil.encrypt(contact.getMsisdn())
						+ " ,contact_first_name=" + contact.getFirstName());
			}
                        if(newIndividualList!=null && !newIndividualList.isEmpty()){
                            userLogger.debug( "adding new contacts to virtual list, contacts_count=" + newIndividualList.size());
                            ResultStatus status = listsManegementServicePort.handleVirtualList(userInfo, newIndividualList);
                            ResponseStatus response = status.getStatus();
                            userLogger.info("listsManegementServicePort response is:(" + response + ").");

                            switch (response) {
                            case CONTACTS_NOT_FOUND:
                                    userLogger.info("Contacts not found");
                                    break;
                            case SUCCESS:
                                    userLogger.info("Contacts added to individual list successfully");
                                    break;
                            case INELIGIBLE_ACCOUNT:
                                    for (Contact contact : newIndividualList) {
                                            individualContactList.remove(contact);
                                    }

                                    FacesContext.getCurrentInstance().addMessage(
                                                    null,
                                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                    .getLocalizedLabel("add_new_individual_contacts_failure")), null));

                                    userLogger.info("INELIGIBLE_ACCOUNT, not allowed to add new contact to individual list");
                                    break;
                            case FAIL:
                                    for (Contact contact : newIndividualList) {
                                            individualContactList.remove(contact);
                                    }

                                    FacesContext.getCurrentInstance().addMessage(
                                                    null,
                                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                    .getLocalizedLabel("add_new_individual_contacts_failure")), null));

                                    userLogger.info( "Error while adding contacts to individual list");
                                    break;
                            default:
                                    for (Contact contact : newIndividualList) {
                                            individualContactList.remove(contact);
                                    }

                                    FacesContext.getCurrentInstance().addMessage(
                                                    null,
                                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                    .getLocalizedLabel("add_new_individual_contacts_failure")), null));

                                    userLogger.info( "Undefined response status=" + response + " while adding contacts to individual list");
                                    break;
                            }
                        }
                        
			String lists_log =  "Previously selected lists: ";
			for (ContactListInfo selected : selectedList) {
				lists_log += " [id: " +selected.getListId()+", name: " +selected.getListName()+"]";;
			}
			
			String contacts_log = "Previously selected individual contacts: ";
			for (Contact selected : selectedContact) {
				contacts_log += " [msisdn: " +encyrptionUtil.encrypt(selected.getMsisdn())+", name: " +selected.getFirstName()+"]";
			}
			
			selectedList.clear();
			selectedContact.clear();
			numOfSelectedLists = 0;
			lists_log+="currently selected lists";
			for (ContactListInfo list : allLists) {
				if (checked.get(list.getListId()) != null) {
					if (checked.get(list.getListId()) != false) {
						selectedList.add(list);
						lists_log+=" [Id: " + list.getListId() +", name: " + list.getListName()+"] ";
						numOfSelectedLists = numOfSelectedLists + list.getContactsCount();
					}
				}// end if
			}// end for
			contacts_log+="currently selected individual Contacts:";
			for (Contact contact : individualContactList) {
				if (checked2.get(contact.getMsisdn()) != null) {
					if (checked2.get(contact.getMsisdn()) != false) {
						selectedContact.add(contact);
						contacts_log += " [msisdn: " +encyrptionUtil.encrypt(contact.getMsisdn())+", name: " +contact.getFirstName()+"]";
						numOfSelectedLists = numOfSelectedLists + 1;
					}
				}// end if
			}// end for
			userLogger.debug(contacts_log);
			userLogger.debug(lists_log);

		} catch (Exception ex) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("add_new_individual_contacts_failure")), null));
			userLogger.error( "Error while adding new contacts to individual contacts list or getting all lists ids list", ex);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

		} finally {
			if (numOfSelectedLists > 0) {
				contactsSelected = true;
				userLogger.info(" Count of Selected contacts : " + numOfSelectedLists);
			} else
				contactsSelected = false;
			ThreadContext.pop();
		}
		

	}

	public void searchList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			searchMode = true;
		
			userInfo.setUser(user);

			userLogger.info("Searching for list");

			if (listNameSearch != null) {
				if (!listNameSearch.trim().equals("")) {
					userLogger.info("Searching for list with name="
							+ listNameSearch);

					List<ListType> listType = new ArrayList<ListType>();
					if (intraSenderNameChoosen) {
						// intra and subIntra
						listType.add(ListType.INTRA_LIST);
						listType.add(ListType.INTRA_SUB_LIST);

					} else if (customizeCampFlag) {
						// customized
						listType.add(ListType.CUSTOMIZED_LIST);
					} else {
						// all
						listType.add(ListType.NORMAL_LIST);
						listType.add(ListType.VIRTUAL_LIST);
						listType.add(ListType.INTRA_LIST);
						listType.add(ListType.INTRA_SUB_LIST);
						listType.add(ListType.CUSTOMIZED_LIST);

					}
					ContactListResultSet result = listsManegementServicePort.searchLists(userInfo, listNameSearch,
							listType);
					ResponseStatus state = result.getStatus();
					userLogger.info( "listsManegementServicePort response is:(" + state + ").");

					switch (state) {
					case SUCCESS:
						listInfo = result.getContactListInfoSet();
						userLogger.info( "Found lists/list with name="
								+ listNameSearch + " successfully with count=" + listInfo.size());
					case FAIL:
						userLogger.info( "Error while searching for list_name=" + listNameSearch);
						break;
					case LIST_NOT_FOUND:
						userLogger.info( "List_name=" + listNameSearch
								+ " is not found");
						break;
					case INELIGIBLE_ACCOUNT:
						userLogger.info("INELIGIBLE_ACCOUNT, not allowed to display lists");
					default:
						userLogger.info("Undefined result status="
								+ state + " while searching for list_name=" + listNameSearch);

						break;
					}
				} else {
					populateListsOfContacts();
				}
			} else {
				populateListsOfContacts();
			}
		} catch (Exception e) {
			userLogger.error( "Error while searching list_name=" + listNameSearch+". "+ e.getMessage());
			appLogger.error("Error while searching list_name=" + listNameSearch, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	public Boolean getReadonly() {
		return FacesContext.getCurrentInstance().getRenderResponse();
	}

	private String fromDateToString(Date date) {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			userInfo.setTrxId(TrxId.getTrxId());
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			String reportDate = df.format(date);
			return reportDate;
		} catch (Exception e) {
			userLogger.error( "Error while parsing from date="
					+ date + " to string", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return null;
		}
	}

	// Change sender
	// name-----------------------------------------------------------

	public String changeSenderNameNew() {
		ProvTrxInfo userInfo = new ProvTrxInfo();
		final String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			if (changeSenderName.length() > 11) {
				senderNameSucces = false;
				userLogger.info( "Changing sender name, too long name=" + changeSenderName);
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_sender_name_lenght")), null));
			} else if (changeSenderName.trim().isEmpty()) {
				userLogger.info( "Changing sender name, no name");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_sender_name")), null));
			} else {
				SenderType senderType = SMSUtils.getSenderType(changeSenderName);
				if (senderType != SenderType.ALPHANUMERIC) {
					userLogger.info( "Sender name is not alphanumeric");
					senderNameSucces = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_sender_name")), null));
				} else {
					if (senderName == null || senderName.trim().equals("")) {
						userInfo.setAccountId(user.getAccountId());
						senderNameSucces = false;
						userLogger.info( "User didn't choose one of his multi senders to change");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("choose_sender_to_change")), null));
					} else {

						userInfo.setAccountId(user.getAccountId());
						senderNameSucces = true;

						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
										.getLocalizedLabel("change_sender_name_in_process")), null));

							new Thread(new Runnable() {
								
								 Map<String, String> values = ThreadContext.getImmutableContext();
								
								@Override
								public void run() {
									if (values != null) {
									
										// for (String name : values.keySet()) {
										//
										// String key = name.toString();
										// String value = values.get(name).toString();
										// }
										String loginId = (String) Configs.LOGIN_ID.getValue();
										CloseableThreadContext.Instance ctc = CloseableThreadContext.put(loginId, values.get(loginId));
										
									}
								
									userLogger.debug("Start calling requestChangeSender ......");
									ProvTrxInfo userInfo = new ProvTrxInfo();
									userInfo.setTrxId(trxId);
									userInfo.setAccountId(user.getAccountId());
									ProvResultStatus result = null;
									if (sendersList.size() > 1) {
										result = serviceProvisioningPort.requestChangeSender(userInfo, senderName, changeSenderName);
										
									} else {
										result = serviceProvisioningPort.requestChangeSender(userInfo, sendersList.get(0), changeSenderName);
									}
									userLogger.debug("Done calling requestChangeSender , returned status=" + result.getStatus());
									
								}
							}).start();

						userLogger.info( "User request is being processed");

					}
				}
			}
		} catch (InvalidSMSSender e1) {
			senderNameSucces = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("invalid_sender_name")), null));
			userLogger.error( "Invalid sms sender=" + changeSenderName, e1);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		return "";
	}

	public void clearAfterCancel() {
		changeSenderName = "";
	}

	public void getChoosenSenderName(ValueChangeEvent e) {
		intraSenderNameChoosen = false;
		senderNameNotSelected = true;
		individualContacts = false;
		if (e.getNewValue() != null && !e.getNewValue().toString().trim().equals("")) {
			senderNameNotSelected = false;
			senderName = e.getNewValue().toString();
			if (senderName.contains(" (Intra)")) {
				String[] sender = senderName.split(" ");
				senderName = sender[0];
				intraSenderNameChoosen = true;
				individualContacts = false;
			}
			if (userAcc.getAccount().getIntraSender() != null) {
				if (intraSendersList.contains(senderName)) {
					intraSenderNameChoosen = true;
					individualContacts = false;
				} else {
					intraSenderNameChoosen = false;
					if (customizeCampFlag)
						individualContacts = false;
					else
						individualContacts = true;
				}
				userLogger.info( "intra sender name flag: ("
						+ intraSenderNameChoosen + ") individualContacts = " + individualContacts + " customizeflag = "
						+ customizeCampFlag + ").");

			}

		}
		selectedList = new ArrayList<ContactListInfo>();
		selectedContact = new ArrayList<Contact>();
		individualContactList = new ArrayList<Contact>();
		checked = new HashMap<Integer, Boolean>();
		checked2 = new HashMap<String, Boolean>();
		numOfSelectedLists = 0;
		String logSenderr = (String) ((e.getNewValue() == null) ? "" : e.getNewValue());
		userLogger.info( "choosen sender_name=" + logSenderr
				+ " .. intraSenderNameChoosen: " + intraSenderNameChoosen + ".. individualContacts: "
				+ individualContacts + "..customizeCampFlag:" + customizeCampFlag);
	}

	public String addNewSenderName() {
		ProvTrxInfo userInfo = new ProvTrxInfo();
		final String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			if (newSenderName.length() > 11) {
				senderNameSucces = false;
				userLogger.info( "Adding new sender name, too long name=" + newSenderName);
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_sender_name_lenght")), null));
			} else if (newSenderName.trim().isEmpty()) {
				userLogger.info( "Adding new sender name, no name");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_sender_name")), null));
			} else {
				SenderType senderType = SMSUtils.getSenderType(newSenderName);
				if (senderType != SenderType.ALPHANUMERIC) {
					userLogger.info( "Sender name is not alphanumeric");
					senderNameSucces = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("invalid_sender_name")), null));
				} else {

					userInfo.setAccountId(user.getAccountId());
					senderNameSucces = true;

					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
									.getLocalizedLabel("change_sender_name_in_process")), null));

					new Thread(new Runnable() {

						 Map<String, String> values = ThreadContext.getImmutableContext();
							
							@Override
							public void run() {
								if (values != null) {
								
									// for (String name : values.keySet()) {
									//
									// String key = name.toString();
									// String value = values.get(name).toString();
									// }
									String loginId = (String) Configs.LOGIN_ID.getValue();
									CloseableThreadContext.Instance ctc = CloseableThreadContext.put(loginId, values.get(loginId));
									
								}
							userLogger.debug("Start calling add new sender request ......");
							ProvTrxInfo userInfo = new ProvTrxInfo();
							userInfo.setTrxId(trxId);
							userInfo.setAccountId(user.getAccountId());
							ProvResultStatus result = serviceProvisioningPort.requestAddSender(userInfo, newSenderName);
							userLogger.debug("Done calling adding new sender request , returned status=" + result.getStatus());

						}
					}).start();

					userLogger.info("User request is being processed");

				}
			}
		} catch (InvalidSMSSender e1) {
			senderNameSucces = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("invalid_sender_name")), null));
			userLogger.error(  "Invalid sms sender=" + newSenderName, e1);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.INVALID_REQUEST, "Invalid SMS sender");
		}finally{
            ThreadContext.pop();
        }
		return "";
	}

	public String deleteSingleSender() {
		ProvTrxInfo userInfo = new ProvTrxInfo();
		AccountUserTrxInfo userTRXInfo = new AccountUserTrxInfo();
		String trxId = TrxId.getTrxId();
		
		userInfo.setTrxId(trxId);
		userTRXInfo.setTrxId(trxId);

		try {
	        ThreadContext.push(trxId);
			facesContext = FacesContext.getCurrentInstance();
			request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

			serviceProvisioningPort = portObj.getServiceProvisioningPort();
			accountManegmentServicePort = portObj.getAccountServicePort();
			if (senderName == null || senderName.trim().isEmpty() || senderName.trim().equals("")) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("choose_sender_to_delete")), null));
			} else {
				userInfo.setAccountId(user.getAccountId());
				userTRXInfo.setUser(accManagUser);

				ProvResultStatus result = serviceProvisioningPort.requestDeleteSender(userInfo, senderName);

				ProvResponseStatus response = result.getStatus();
				userLogger.info( "serviceProvisioningPort response is:(" + response + ").");
				switch (response) {
				case FAIL:
					userLogger.info("Error while deleting sender name=" + senderName);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("delete_sender_failure")), null));
					break;
				case SUCCESS:
					userLogger.info( "Sender name=" + senderName
							+ " deleted successfully");

					if (request.getSession().getAttribute(USER_KEY) != null) {
						UserAccount updatedUser = new UserAccount();
						String userName = ((UserAccount) request.getSession().getAttribute(USER_KEY))
								.getAccount().getCompanyDomain();
						AccountResultFullInfo res = accountManegmentServicePort.findAccountByCompanyNameFullInfo(
								userTRXInfo, ((UserAccount) request.getSession().getAttribute(USER_KEY))
										.getAccount().getCompanyDomain());
						if (res.getStatus().equals(com.edafa.web2sms.service.acc_manag.enums.ResponseStatus.SUCCESS)) {
							updatedUser.setAccount(res.getAccount());
							List<String> newsendersList = res.getAccount().getSender();
							sendersList.clear();
							sendersList.addAll(newsendersList);
							if (newsendersList.size() > 1) {
								String sendersStrings = "";
								for (String s : newsendersList) {
									sendersStrings = sendersStrings + s + " , ";
								}
								userLogger.info("This user has multi senders=[" + sendersStrings + "]");
							} else {
								userLogger.info("This user has one sender_name="
										+ res.getAccount().getSender().get(0));

								senderName = sendersList.get(0);
							}
							updatedUser.setUsername(userName);

							request.getSession().removeAttribute(USER_KEY);

							request.getSession().setAttribute(USER_KEY, updatedUser);
						}

					}

					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
									.getLocalizedLabel("delete_sender_success")), null));

					break;
				case SENDER_NAME_ALREADY_ATTACHED:
					userLogger.info( "Sender name already attached");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("delete_sender_failure")), null));
					break;
				case INVALID_SENDER:
					userLogger.info("Invalid sender to delete , sender name=" + senderName);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("delete_sender_failure")), null));
					break;
				default:
					userLogger.info("Error while deleting sender, sender name=" + senderName);
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("delete_sender_failure")), null));

				}
			}
		} catch (Exception e1) {
			senderNameSucces = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("delete_sender_failure")), null));
			userLogger.error( "Error while deleting sender", e1);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		return "";
	}

	// add new contact part-------------------------------------------------

	private boolean checkRequiredField2() {
		if (firstName.trim().isEmpty() && msisdn.trim().isEmpty()) {
			return true;
		} else if (msisdn.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("msisdn_required")), null));
			userLogger.info( "MSISDN field is empty while adding new contact to individual list");
			return false;
		}
		return true;
	}

	public String createNewContact() {
		try {
			boolean valid = false;
//			UserTrxInfo userInfo = new UserTrxInfo();
//			String trxId= TrxId.getTrxId();
//			userInfo.setTrxId(trxId);
//			userInfo.setUser(user);
			if (checkRequiredField2()) {
				if (SMSUtils.validateLocalAddress(msisdn) || SMSUtils.validateInternationalAddress(msisdn)) {
					userLogger.info( "Msisdn=" + encyrptionUtil.encrypt(msisdn)
							+ " is valid while adding contact to indiviual list");
					if (individualContactList.size() > 0) {
						for (Contact cont : individualContactList) {
							if (cont.getMsisdn().equals(msisdn)) {
								contactAddedSuccess = false;
								valid = false;
								FacesContext.getCurrentInstance().addMessage(
										null,
										new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
												.getLocalizedLabel("duplicate_msisdn")), null));
								userLogger.info("User entered duplicated msisdn=" + encyrptionUtil.encrypt(msisdn));
								break;
							} else {
								valid = true;
							}
						}

						if (valid) {
							Contact contact = new Contact();
							if (firstName == null || firstName.equals("")) {
								contact.setFirstName(null);
							} else {
								contact.setFirstName(firstName);
							}

							contact.setMsisdn(msisdn);
							individualContactList.add(contact);
							newIndividualList.add(contact);
							checked2.put(msisdn, true);
							contactAddedSuccess = true;
							firstName = "";
							msisdn = "";
							userLogger.info(" added individual contact, msisdn=" + encyrptionUtil.encrypt(contact.getMsisdn()) + " first_name="
									+ contact.getFirstName());
						}
					} else {
						Contact contact = new Contact();
						if (firstName == null || firstName.equals("")) {
							contact.setFirstName(null);
						} else {
							contact.setFirstName(firstName);
						}
						contact.setMsisdn(msisdn);
						individualContactList.add(contact);
						newIndividualList.add(contact);
						checked2.put(msisdn, true);
						contactAddedSuccess = true;
						firstName = "";
						msisdn = "";
						userLogger.info(" added individual contact, msisdn=" + encyrptionUtil.encrypt(contact.getMsisdn()) + " first_name="
								+ contact.getFirstName());
					}

				} else {
					contactAddedSuccess = false;
					userLogger.info( "Msisdn=" + encyrptionUtil.encrypt(msisdn)
							+ " not valid  while adding contact to indiviual list");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("create_individula_contact_failure")), null));
				}
			}
		} catch (Exception e) {
			contactAddedSuccess = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("create_individula_contact_failure")), null));
			userLogger.error("error while creating individual contact with msisdn=" + encyrptionUtil.encrypt(msisdn), e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
		return null;
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
		populateListsOfContacts(); // Load requested page.
	}

	public void page(ActionEvent event) {
		page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
	}

	public void pageFirst1() {
		page1(0);
	}

	public void pageNext1() {
		page1(firstRow2 + rowsPerPage2);
	}

	public void pagePrevious1() {
		page1(firstRow2 - rowsPerPage2);
	}

	public void pageLast1() {
		page1(totalRows2 - ((totalRows2 % rowsPerPage2 != 0) ? totalRows2 % rowsPerPage2 : rowsPerPage2));
	}

	private void page1(int firstRow2) {
		this.firstRow2 = firstRow2;
		populateContactLists(); // Load requested page.
	}

	public void page1(ActionEvent event) {
		page1(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage2);
	}

	// ---------------------------------------------------------------------------

	public void getUpdateListId(String listId2) {
		listId = listId2;
		contactList = new ArrayList<Contact>();
		try {
			if (checked.get(Integer.valueOf(listId2))) {
				listCheckedFlag = true;
			} else {
				listCheckedFlag = false;
				firstRow2 = 0;
				populateContactLists();
			}

		} catch (Exception e) {
			listCheckedFlag = false;
			firstRow2 = 0;
			populateContactLists();
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public void populateContactLists() {
		 UserTrxInfo userInfo = new UserTrxInfo();
	        String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			listsManegementServicePort = portObj.getListsManegementServicePort();

			userLogger.debug("Getting contacts of selected list_id=" + listId + " from=" + firstRow2 + " ,rows_count="
					+ rowsPerPage2);

			ContactResultSet result = listsManegementServicePort.getContactListWithPagination(userInfo,
					Integer.valueOf(listId), firstRow2, rowsPerPage2);
			ResponseStatus state = result.getStatus();
			userLogger.info("listsManegementServicePort response is:(" + state + ").");
			switch (state) {
			case SUCCESS:
				for (Contact contact : result.getContacts()) {
					contactList.add(contact);
					userLogger.trace( "Selected list_id=" + listId
							+ " contact [msisdn=" + encyrptionUtil.encrypt(contact.getMsisdn()) + ",first_name=" + contact.getFirstName());
				}
				userLogger.info("List of contact lists is populated successfully, rows_count=" + contactList.size());
				break;
			case FAIL:
				contactList = null;
				userLogger.info("Error while getting list of contat lists list_id=" + listId);
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.info("INELIGIBLE_ACCOUNT, not allowed to display list of contacts list_id=" + listId);
			default:
				userLogger.info("Unkown response status=" + state + " for getting list of contact lists list_id=" + listId);
				break;
			}
			userLogger.info("list menu of list_id=" + listId + " is populated successfully");

		} catch (Exception e) {
			userLogger.error( "error while populating list_id=" + listId, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void getSelectedContactsFromList() {
		try {
			for (Contact contact : contactList) {
				boolean contactExist = false;
				if (checked3.get(contact.getMsisdn())) {
					for (Contact individualContact : individualContactList) {
						if (individualContact.getMsisdn().equals(contact.getMsisdn())) {
							checked2.put(individualContact.getMsisdn(), true);
							contactExist = true;
							break;
						}
					}
					if (!contactExist) {
						checked2.put(contact.getMsisdn(), true);
						individualContactList.add(contact);
					}
				} else {
					checked2.put(contact.getMsisdn(), false);
					individualContactList.remove(contact);
				}
			}

		} catch (Exception e) {
			userLogger.error( "Error while getting selected contacts from list", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	/**
	 * customized campaign check box listener
	 */

	public void setCustomizedCampaignFlag(ValueChangeEvent e) {
		AccountUserTrxInfo userInfo = new AccountUserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
			ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(accManagUser);
		if (e.getNewValue().toString().equals("true")) {
			customizeCampFlag = true;
			sendersList = new ArrayList<String>();
			sendersList.addAll(userAcc.getAccount().getSender());
			if (sendersList.size() == 1) {
				senderNameNotSelected = false;
				individualContacts = false;
				senderName = sendersList.get(0);
				choosenSender = senderName;
				userLogger.debug("choosenSender is : " + choosenSender);
				userLogger.debug("senderNameNotSelected is : " + senderNameNotSelected);

			}

		} else {
			customizeCampFlag = false;
			sendersList = new ArrayList<String>();
			sendersList.addAll(userAcc.getAccount().getSender());
			userLogger.info( "Getting account by company name to get all senders. ");

			accountManegmentServicePort = portObj.getAccountServicePort();
			AccountResultFullInfo accResult = accountManegmentServicePort.findAccountByCompanyNameFullInfo(userInfo,
					userAcc.getAccount().getCompanyName());
			com.edafa.web2sms.service.acc_manag.enums.ResponseStatus status = accResult.getStatus();

			userLogger.info("Getting account by company name finished by status: (" + status + ").");

			switch (status) {
			case SUCCESS:
				List<String> intraSenders = accResult.getAccount().getIntraSender();
				if (intraSenders != null)
					for (int i = 0; i < intraSenders.size(); i++) {
						if (!sendersList.contains(intraSenders.get(i) + " (Intra)"))
							sendersList.add(intraSenders.get(i) + " (Intra)");
					}

				break;

			case FAIL:
				userLogger.error( "Getting account by company name finished by status: (" + status + ").");

				break;
			case ACCT_NOT_EXIST:
				userLogger.error( "Getting account by company name finished by status: (" + status + ").");
				break;
			default:
				userLogger.error( "Getting account by company name finished by undefined status: (" + status + ").");
				break;
			}
			// for (int i = 0; i < userAcc.getAccount().getIntraSender().size();
			// i++) {
			// String intraSender = userAcc.getAccount().getIntraSender().get(i)
			// + " (Intra)";
			// sendersList.add(intraSender);
			// }
			if (sendersList.size() == 1) {
				senderNameNotSelected = false;
				individualContacts = true;
				if (sendersList.get(0).contains("(Intra)")) {
					intraSenderNameChoosen = true;
					individualContacts = false;
				}
				senderName = sendersList.get(0);
				choosenSender = senderName;
			}
		}
		selectedList = new ArrayList<ContactListInfo>();
		selectedContact = new ArrayList<Contact>();
		individualContactList = new ArrayList<Contact>();
		checked = new HashMap<Integer, Boolean>();
		checked2 = new HashMap<String, Boolean>();
		numOfSelectedLists = 0;
		if (sendersList.size() != 1) {
			senderName = new String();
			choosenSender = new String();
			senderNameNotSelected = true;
			userLogger.info("customize camp flag: " + customizeCampFlag);
			}
			populateListsOfContacts();
		} finally {
			ThreadContext.pop();
		}
	}

	/**
	 * method to ensure that the SMS text contains $[1-5] if campaign is
	 * customized camp.
	 * */

	// TODO should be configuration value ?!
	boolean validateCustmoizeSmsText() {
		if (customizeCampFlag) {
			String encyrptedText= encyrptionUtil.encrypt(text);
			userLogger.info( "validate sms text: (" + encyrptedText
					+ ") against this pattern (?s).*[$][1-5](?s).* ");
			if (!text.matches("(?s).*[$][1-5](?s).*")) {
				userLogger.info( "sms text: (" + encyrptedText
						+ ") doesn't match pattern.");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("invalid_customize_sms_text")), null));
				return false;
			}
		}

		return true;
	}

	public String setSenderNameErrorMessage() {
		userLogger.info( "setSenderNameErrorMessage");
		FacesContext.getCurrentInstance().addMessage(
				null,
				new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
						.getLocalizedLabel("choose_sender_name")), null));
		return "";
	}
}
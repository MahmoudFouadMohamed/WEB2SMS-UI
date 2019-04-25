package com.edafa.web2sms.ui.campaign;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.service.campaign.model.CampaignResult;
import com.edafa.service.campaign.model.CampaignResultSet;
import com.edafa.web2sms.encyrptionutil.Interfaces.EncyrptionUtilInterface;
import com.edafa.web2sms.prov.enums.ProvResponseStatus;
import com.edafa.web2sms.prov.model.ProvResultStatus;
import com.edafa.web2sms.service.acc_manag.account.AccountManegementService;
import com.edafa.web2sms.service.acc_manag.account.model.AccountResultFullInfo;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.campaign.CampaignManagementService;
import com.edafa.web2sms.service.campaign.CampaignManagementService_Service;
import com.edafa.web2sms.service.campaign.model.CampaignDetailsResult;
import com.edafa.web2sms.service.enums.CampaignType;
import com.edafa.web2sms.service.enums.ListType;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.list.model.ContactListInfoResultSet;
import com.edafa.web2sms.service.list.model.ContactListResultSet;
import com.edafa.web2sms.service.list.model.ContactResultSet;
import com.edafa.web2sms.service.lists.ListsManegementService;
import com.edafa.web2sms.service.lists.ListsManegementService_Service;
import com.edafa.web2sms.service.model.Campaign;
import com.edafa.web2sms.service.model.Contact;
import com.edafa.web2sms.service.model.ContactListInfo;
import com.edafa.web2sms.service.model.CountResult;
import com.edafa.web2sms.service.model.ProvTrxInfo;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.Template;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.model.enums.CampaignAction;
import com.edafa.web2sms.service.model.enums.CampaignStatus;
import com.edafa.web2sms.service.model.enums.CampaignValidationStatus;
import com.edafa.web2sms.service.model.enums.Language;
import com.edafa.web2sms.service.model.enums.ScheduleFrequency;
import com.edafa.web2sms.service.prov.ServiceProvisioning;
import com.edafa.web2sms.service.prov.ServiceProvisioningImplService;
import com.edafa.web2sms.service.template.TemplateManegementService;
import com.edafa.web2sms.service.template.TemplateManegementService_Service;
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

@ManagedBean(name = "campaignActiveBean")
@ViewScoped
public class CampaignActiveBean {

    CampaignManagementService_Service campaignManagementService;
    CampaignManagementService campaignServicePort;
    private List<Campaign> activeCampaignList = new ArrayList<Campaign>();

    private int totalRows;
    private int firstRow;
    private int rowsPerPage;
    private int pageRange;
    private Integer[] pages;
    private int currentPage;
    private int totalPages;

    private int totalRows1;
    private int firstRow1;
    private int rowsPerPage1;

    private int refreshSec;
    private boolean tableFlag = true;
    private Campaign resumeObj;
    private Campaign pauseObj;
    private Campaign cancelObj;
    private Campaign campaignObj = new Campaign();
    Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
    Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());

    private Campaign campaignEditObj = new Campaign();
    private boolean scheduleFlag;
    private boolean endDateFlag;
    private boolean dateAndTimeFlag;
    private boolean onceFlag = true;
    private boolean frequencyFlag;
    private String testList;
    private int numOfSelectedLists = 0;
    private boolean templateDiv;
    private boolean templateFlag;
    private String choosenTemplate;
    private List<Template> templateList;
    TemplateManegementService_Service templateManegmentService_Service;
    TemplateManegementService templateManegmentServicePort;
    ListsManegementService_Service listsManegementService_Service;
    ListsManegementService listsManegementServicePort;
    ServiceProvisioningImplService serviceProvisioningService;
    ServiceProvisioning serviceProvisioningPort;
    AccountManegementService accountManegmentServicePort;
    private String languageSelected = "ENGLISH";
    private String listNameSearch;
    private List<ContactListInfo> listInfo = new ArrayList<ContactListInfo>();
    private Map<Integer, Boolean> checked;
    private List<ContactListInfo> selectedList = new ArrayList<ContactListInfo>();
    private String newSmsText;
    private String startDate;
    private String endDate;
    private Date finalStartDate;
    private Date finalEndDate;
    private String editedLanguage;
    FacesContext facesContext = FacesContext.getCurrentInstance();
    HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
    static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
    UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
    private String stopTime;
    private Date finalStopTime;
    private boolean hideList;
    private boolean saveButtonFlag;

    private String changeSenderName;
    private boolean senderNameSucces;

    private String firstName;
    private String msisdn;

    private boolean contactAddedSuccess;
    private List<Contact> individualContactList;
    private Map<String, Boolean> checked2;
    private List<Contact> selectedContact;
    private String testMsisdn;
    
    @EJB
    private EncyrptionUtilInterface encyrptionUtil;
    @EJB
    WSClients portObj;

    @EJB
    AppErrorManagerAdapter appErrorManagerAdapter;

    private List<Contact> contactList;
    private String listId;
    private Map<String, Boolean> checked3;

    private boolean searchMode;
    private List<ContactListInfo> archiveList;
    private boolean listCheckedFlag = true;
    private List<ContactListInfo> contactListInfoList;
    private boolean goTohistory;
    private List<Contact> newIndividualList;
    private String frequencyName = "";
    private boolean checkIsScheduled;
    List<String> activeUserSenderList = new ArrayList<String>();
    private String newSenderName;

    private boolean templateChecked;

    /**
     * intra campaign properties..
     */
    private boolean intraCampFlag;
    /**
     * customized camp param
     */

    private boolean customizeCampFlag;

    private CampaignType editedCampType;
    private boolean normalCampFlag;
    private boolean individualFlag;
    private List<Contact> individualEditObjList;
    private User user = new User();
    private AccManagUser accManagUser = new AccManagUser();

    private boolean campaignEdited = false;
    private boolean isScheduleFlagUpdated = false;
    private boolean isCampaignNameUpdated = false;
    private boolean isCampaignScheduledStartDateUpdated = false;
    private boolean isCampaignScheduledStopTimeUpdated = false;
    private boolean isCampaignFrequencyFlagUpdated = false;
    private boolean isCampaignSchedualFrequencyUpdated = false;
    private boolean isCampaignSchedualEndTimeUpdated=false;
    private boolean isSenderNameUpdated = false;
    private boolean isCampaignTextUpdated = false;
    private boolean isCampaignSendToUpdated = false;
    Campaign campaignBeforeUpdate = new Campaign();
   
    ArrayList<Integer> contactListInfoListBeforeUpdate;
    ArrayList<String> individualEditObjListBeforeUpdate ;
    // Setters and
    // getters-----------------------------------------------------------
    public boolean isSearchMode() {
        return searchMode;
    }

    public boolean isCampaignEdited() {
		return campaignEdited;
	}

	public void setCampaignEdited(boolean campaignEdited) {
		this.campaignEdited = campaignEdited;
	}

	public List<Contact> getIndividualEditObjList() {
        return individualEditObjList;
    }

    public void setIndividualEditObjList(List<Contact> individualEditObjList) {
        this.individualEditObjList = individualEditObjList;
    }

    public boolean isIndividualFlag() {
        return individualFlag;
    }

    public void setIndividualFlag(boolean individualFlag) {
        this.individualFlag = individualFlag;
    }

    public boolean isNormalCampFlag() {
        return normalCampFlag;
    }

    public void setNormalCampFlag(boolean normalCampFlag) {
        this.normalCampFlag = normalCampFlag;
    }

    public CampaignType getEditedCampType() {
        return editedCampType;
    }

    public void setEditedCampType(CampaignType editedCampType) {
        this.editedCampType = editedCampType;
    }

    public boolean isIntraCampFlag() {
        return intraCampFlag;
    }

    public void setIntraCampFlag(boolean intraCampFlag) {
        this.intraCampFlag = intraCampFlag;
    }

    public boolean isCustomizeCampFlag() {
        return customizeCampFlag;
    }

    public void setCustomizeCampFlag(boolean customizeCampFlag) {
        this.customizeCampFlag = customizeCampFlag;
    }

    public boolean isTemplateChecked() {
        return templateChecked;
    }

    public void setTemplateChecked(boolean templateChecked) {
        this.templateChecked = templateChecked;
    }

    public String getNewSenderName() {
        return newSenderName;
    }

    public void setNewSenderName(String newSenderName) {
        this.newSenderName = newSenderName;
    }

    public boolean isCheckIsScheduled() {
        return checkIsScheduled;
    }

    public void setCheckIsScheduled(boolean checkIsScheduled) {
        this.checkIsScheduled = checkIsScheduled;
    }

    public List<String> getActiveUserSenderList() {
        return activeUserSenderList;
    }

    public void setActiveUserSenderList(List<String> activeUserSenderList) {
        this.activeUserSenderList = activeUserSenderList;
    }

    public boolean isGoTohistory() {
        return goTohistory;
    }

    public void setGoTohistory(boolean goTohistory) {
        this.goTohistory = goTohistory;
    }

    public void setSearchMode(boolean searchMode) {
        this.searchMode = searchMode;
    }

    public boolean isListCheckedFlag() {
        return listCheckedFlag;
    }

    public void setListCheckedFlag(boolean listCheckedFlag) {
        this.listCheckedFlag = listCheckedFlag;
    }

    public Campaign getCampaignEditObj() {
        return campaignEditObj;
    }

    public boolean isSenderNameSucces() {
        return senderNameSucces;
    }

    public String getTestMsisdn() {
        return testMsisdn;
    }

    public void setTestMsisdn(String testMsisdn) {
        this.testMsisdn = testMsisdn;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean isContactAddedSuccess() {
        return contactAddedSuccess;
    }

    public void setContactAddedSuccess(boolean contactAddedSuccess) {
        this.contactAddedSuccess = contactAddedSuccess;
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

    public void setSenderNameSucces(boolean senderNameSucces) {
        this.senderNameSucces = senderNameSucces;
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

    public boolean isHideList() {
        return hideList;
    }

    public void setHideList(boolean hideList) {
        this.hideList = hideList;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public Map<Integer, Boolean> getChecked() {
        return checked;
    }

    public void setChecked(Map<Integer, Boolean> checked) {
        this.checked = checked;
    }

    public List<ContactListInfo> getListInfo() {
        return listInfo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setListInfo(List<ContactListInfo> listInfo) {
        this.listInfo = listInfo;
    }

    public String getListNameSearch() {
        return listNameSearch;
    }

    public void setListNameSearch(String listNameSearch) {
        this.listNameSearch = listNameSearch;
    }

    public String getLanguageSelected() {
        return languageSelected;
    }

    public String getNewSmsText() {
        return newSmsText;
    }

    public String getEditedLanguage() {
        return editedLanguage;
    }

    public void setEditedLanguage(String editedLanguage) {
        this.editedLanguage = editedLanguage;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setNewSmsText(String newSmsText) {
        this.newSmsText = newSmsText;
    }

    public void setLanguageSelected(String languageSelected) {
        this.languageSelected = languageSelected;
    }

    public List<Template> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<Template> templateList) {
        this.templateList = templateList;
    }

    public String getChoosenTemplate() {
        return choosenTemplate;
    }

    public void setChoosenTemplate(String choosenTemplate) {
        this.choosenTemplate = choosenTemplate;
    }

    public boolean isTemplateFlag() {
        return templateFlag;
    }

    public void setTemplateFlag(boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    public boolean isTemplateDiv() {
        return templateDiv;
    }

    public void setTemplateDiv(boolean templateDiv) {
        this.templateDiv = templateDiv;
    }

    public String getTestList() {
        return testList;
    }

    public int getNumOfSelectedLists() {
        return numOfSelectedLists;
    }

    public void setNumOfSelectedLists(int numOfSelectedLists) {
        this.numOfSelectedLists = numOfSelectedLists;
    }

    public void setTestList(String testList) {
        this.testList = testList;
    }

    public boolean isFrequencyFlag() {
        return frequencyFlag;
    }

    public void setFrequencyFlag(boolean frequencyFlag) {
        this.frequencyFlag = frequencyFlag;
    }

    public boolean isOnceFlag() {
        return onceFlag;
    }

    public void setOnceFlag(boolean onceFlag) {
        this.onceFlag = onceFlag;
    }

    public boolean isDateAndTimeFlag() {
        return dateAndTimeFlag;
    }

    public void setDateAndTimeFlag(boolean dateAndTimeFlag) {
        this.dateAndTimeFlag = dateAndTimeFlag;
    }

    public boolean isEndDateFlag() {
        return endDateFlag;
    }

    public void setEndDateFlag(boolean endDateFlag) {
        this.endDateFlag = endDateFlag;
    }

    public boolean isScheduleFlag() {
        return scheduleFlag;
    }

    public void setScheduleFlag(boolean scheduleFlag) {
        this.scheduleFlag = scheduleFlag;
    }

    public void setCampaignEditObj(Campaign campaignEditObj) {
        this.campaignEditObj = campaignEditObj;
    }

    public Campaign getCampaignObj() {
        return campaignObj;
    }

    public void setCampaignObj(Campaign campaignObj) {
        this.campaignObj = campaignObj;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public Campaign getResumeObj() {
        return resumeObj;
    }

    public void setResumeObj(Campaign resumeObj) {
        this.resumeObj = resumeObj;
    }

    public Campaign getPauseObj() {
        return pauseObj;
    }

    public void setPauseObj(Campaign pauseObj) {
        this.pauseObj = pauseObj;
    }

    public Campaign getCancelObj() {
        return cancelObj;
    }

    public void setCancelObj(Campaign cancelObj) {
        this.cancelObj = cancelObj;
    }

    public List<Contact> getIndividualContactList() {
        return individualContactList;
    }

    public void setIndividualContactList(List<Contact> individualContactList) {
        this.individualContactList = individualContactList;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List<Campaign> getActiveCampaignList() {
        return activeCampaignList;
    }

    public void setActiveCampaignList(List<Campaign> activeCampaignList) {
        this.activeCampaignList = activeCampaignList;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public List<Contact> getSelectedContact() {
        return selectedContact;
    }

    public void setSelectedContact(List<Contact> selectedContact) {
        this.selectedContact = selectedContact;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getPageRange() {
        return pageRange;
    }

    public void setPageRange(int pageRange) {
        this.pageRange = pageRange;
    }

    public Integer[] getPages() {
        return pages;
    }

    public List<ContactListInfo> getContactListInfoList() {
        return contactListInfoList;
    }

    public void setContactListInfoList(List<ContactListInfo> contactListInfoList) {
        this.contactListInfoList = contactListInfoList;
    }

    public void setPages(Integer[] pages) {
        this.pages = pages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public Map<String, Boolean> getChecked3() {
        return checked3;
    }

    public void setChecked3(Map<String, Boolean> checked3) {
        this.checked3 = checked3;
    }

    public boolean isTableFlag() {
        return tableFlag;
    }

    public void setTableFlag(boolean tableFlag) {
        this.tableFlag = tableFlag;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Map<String, Boolean> getChecked2() {
        return checked2;
    }

    public void setChecked2(Map<String, Boolean> checked2) {
        this.checked2 = checked2;
    }

    public int getRefreshSec() {
        return refreshSec;
    }

    public void setRefreshSec(int refreshSec) {
        this.refreshSec = refreshSec;
    }

    // Constructor---------------------------------------------------
    /**
     * bean constructor
     */
    public CampaignActiveBean() {
        try {
            
        	user.setAccountId(userAcc.getAccount().getAccountId());
            user.setUsername(userAcc.getUsername());

            checked = new HashMap<Integer, Boolean>();
            checked2 = new HashMap<String, Boolean>();
            checked3 = new HashMap<String, Boolean>();
            individualContactList = new ArrayList<Contact>();
            individualEditObjList = new ArrayList<Contact>();
            selectedContact = new ArrayList<Contact>();
        } catch (Exception ex) {
            activeCampaignList = null;
            userLogger.error("Error while setting accout id and name ", ex);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }
    }

  /*  public String logTrxId(String trxId) {
        StringBuilder sb = new StringBuilder();
        sb.append("UserTrx");
        sb.append("(");
        sb.append(trxId);
        sb.append("): ");
        return sb.toString();
    }
*/
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
        polpulateList(); // Load requested page.
    }

    public void page(ActionEvent event) {
        page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
    }

    public void pageFirst1() {
        page1(0);
    }

    public void pageNext1() {
        page1(firstRow1 + rowsPerPage1);
    }

    public void pagePrevious1() {
        page1(firstRow1 - rowsPerPage1);
    }

    public void pageLast1() {
        page1(totalRows1 - ((totalRows1 % rowsPerPage1 != 0) ? totalRows1 % rowsPerPage1 : rowsPerPage1));
    }

    private void page1(int firstRow1) {
        this.firstRow1 = firstRow1;
        populateListOfLists(); // Load requested page.
    }

    public void page1(ActionEvent event) {
        page1(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage1);
    }

    // Action------------------------------------------------------
    @PostConstruct
    public void populateServices() {
        UserTrxInfo userInfo = new UserTrxInfo();
        String trxId = TrxId.getTrxId() ;
       
        try {
        	
        	ThreadContext.push(trxId);
        	userInfo.setTrxId(trxId);
            userInfo.setUser(user);
            
            try {
                /*
                 * URL ServiceProvisioningURL = new URL((String)
                 * Configs.SERVICE_PROVISIONING_WEBSERVICE_URL.getValue());
                 */

                accManagUser.setUsername(userAcc.getUsername());
                accManagUser.setAccountId(userAcc.getAccountId());
                
                userLogger.info("Setting service provisioning port instance......");
                serviceProvisioningPort = portObj.getServiceProvisioningPort();
                /*
                 * userLogger.info(logTrxId +
                 * userLogInfo(user.getAccountId(), user.getUsername()) +
                 * "Service provisioning service is intialized with url " +
                 * ServiceProvisioningURL);
                 */

                campaignServicePort = portObj.getCampaignServicePort();
                
                facesContext = FacesContext.getCurrentInstance();
                request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
                userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
            } catch (Exception e) {
                userLogger.error("Error while setting service provisioning instance ", e);
                appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
            }
            rowsPerPage = 4; // Default rows per page (max amount of rows to be
            // displayed at once).
            pageRange = 9;

            rowsPerPage1 = 7; // Default rows per page (max amount of rows to
            // be
            // displayed at once).
            polpulateList();
        } catch (Exception e) {
            userLogger.error("Error while setting services port", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
    }

    private void polpulateList() {
    	UserTrxInfo userInfo = new UserTrxInfo();
        String trxId = TrxId.getTrxId();
        try {
        	
        	ThreadContext.push(trxId);
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);
            List<CampaignStatus> campaignStatuses = new ArrayList<>();
            campaignStatuses.add(CampaignStatus.NEW);
            campaignStatuses.add(CampaignStatus.PAUSED);
            campaignStatuses.add(CampaignStatus.RUNNING);
            campaignStatuses.add(CampaignStatus.ON_HOLD);
            campaignStatuses.add(CampaignStatus.PARTIAL_RUN);
            campaignStatuses.add(CampaignStatus.WAITING_APPROVAL);


            userLogger.debug("Getting active campaigns from=" + firstRow + " to=" + rowsPerPage);

            CampaignResultSet result = portObj.getCampaignServicePort().searchCampaigns(userInfo, null, firstRow, rowsPerPage, campaignStatuses);

            switch (result.getStatus()) {
                case INVALID_REQUEST:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_active_campaigns_error")), null));
                    activeCampaignList = null;
                    userLogger.info(result.getStatus() + "status to get active campaign lists");
                    break;
                case SUCCESS:
                    if (activeCampaignList != null) {
                        activeCampaignList.clear();
                    } else {
                        activeCampaignList = new ArrayList<Campaign>();
                    }
                    List<Campaign> campList = result.getCampaign();
                    for (Campaign campaign : campList) {
                        if (campaign.getScheduleEndTimestamp() != null) {
                            campaign.setScheduleEndTimestamp(campaign.getScheduleEndTimestamp().substring(0, 11));
                        }
                    }

                    activeCampaignList.addAll(campList);

                    userLogger.info("Campaign Active list for is set successfully, "
                    		+ "rows_count=" + activeCampaignList.size());
                    break;
                case FAIL:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_active_campaigns_error")), null));

                    activeCampaignList = null;
                    userLogger.error( result.getStatus() + " status while loading active campaigns");
                    break;
                case INELIGIBLE_ACCOUNT:
                    activeCampaignList = null;
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("ineligible_view_active_campaigns")), null));
                    userLogger.warn( "Can not view his active campaigns list, status=" + result.getStatus());
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_active_campaigns_error")), null));

                    userLogger.error("Unknown status=" + result.getStatus() + " ,while loading active campaigns");
                    break;
            }

            // get refresh seconds
            int num = (Integer) Configs.REFRESH_PERIOD.getValue();
            if (num == 0) {
                refreshSec = 10;
                userLogger.info("Active campaign will refresh every 10 seconds");
            } else {
                refreshSec = num;
                userLogger.info("Active campaign will refresh every " + refreshSec + " seconds");
            }
            CountResult countResult = campaignServicePort.countSearchCampaigns(userInfo, null, campaignStatuses);

            if (countResult.getStatus() == ResponseStatus.SUCCESS) {
                totalRows = countResult.getCount();
            } else {
                totalRows = 0;
            }
            
            userLogger.info("Total found number of rows of active campaigns is rows_count=" + totalRows);

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

            if (activeCampaignList == null || totalRows == 0 || activeCampaignList.size() == 0) {
                tableFlag = false;
                userLogger.info( "No activated campaigns found in the database");
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("get_active_campaigns_error")), null));

            activeCampaignList = null;
            tableFlag = false;
            userLogger.error("Error while loading active campaigns"+e.getMessage());
            appLogger.error( "Error while loading active campaigns", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }

    }

    // check flag--------------------------------------------
    public boolean checkFlag(String status, String flagName) {
        if (status.equals("PAUSED") && flagName.equals("resume")) {
            return true;
        } else if (status.equals("NEW") && flagName.equals("new")) {
            return true;
        } else if (status.equals("RUNNING") && flagName.equals("pause")) {
            return true;
        } else if (status.equals("PARTIAL_RUN") && flagName.equals("partial-run")) {
            return true;
        } else if (status.equals("ON_HOLD") && flagName.equals("onHold")) {
            return true;
        } else if (status.equals("WAITING_APPROVAL") && flagName.equals("waiting_approval")){
        	return true;
        }else {
            return false;
        }
    }

    /**
     * change the job action
     *
     * @return string
     */
    public void changeJobStatus(String action) {
    	 UserTrxInfo userInfo = new UserTrxInfo();
         String trxId = TrxId.getTrxId();
         try {
			
			ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
            ResultStatus status = new ResultStatus();

            userLogger.info("calling action change on campaign");

            if (action.equals("PAUSE")) {
                userLogger.debug("calling action PAUSE to change campaign_id=" + pauseObj.getCampaignId());
                status = campaignServicePort.updateCampaignAction(userInfo, pauseObj.getCampaignId(),
                        CampaignAction.PAUSE);
            } else if (action.equals("RESUME")) {
                userLogger.debug("calling action RESUME to change campaign_id=" + resumeObj.getCampaignId());
                status = campaignServicePort.updateCampaignAction(userInfo, resumeObj.getCampaignId(),
                        CampaignAction.RESUME);
            } else if (action.equals("CANCEL")) {
                userLogger.debug("calling action CANCEL to change campaign_id=" + cancelObj.getCampaignId());
                status = campaignServicePort.updateCampaignAction(userInfo, cancelObj.getCampaignId(),
                        CampaignAction.CANCEL);
            }

            switch (status.getStatus()) {
                case SUCCESS:
                    userLogger.info("Changed action of campaign");
                    break;

                case FAIL:
                    userLogger.error("Error while changing campaign action");
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_change_campaign_status")), null));

                    break;

                case INELIGIBLE_ACCOUNT:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("ineligible_user")), null));
                    userLogger.warn("Ineligible to change active campaign status");
                    break;

                case INVALID_REQUEST:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_change_campaign_status")), null));

                    userLogger.warn("invalid request to chnage active campaign status");
                    // error page
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_change_campaign_status")), null));

                    userLogger.error("Unknown response=" + status.getStatus() + " while changing campaign status");
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("error_change_campaign_status")), null));
            userLogger.error("Error while changing the actived campaign status", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.pop();
		}
    }

    public void call() {
        polpulateList();
    }

    // Edit campaign
    // part------------------------------------------------------------------------------------------------
    /**
     *
     * *************************************************************************
     * ***************************************************
     * **********************
     * ****************************************************
     * **************************************************
     *
     *
     */
    public String populateEditObj() {
        AccountUserTrxInfo accUserInfo = new AccountUserTrxInfo();
        UserTrxInfo userInfo = new UserTrxInfo();
        archiveList = new ArrayList<ContactListInfo>();
        contactListInfoList = new ArrayList<ContactListInfo>();
        newIndividualList = new ArrayList<Contact>();
        String trxId = TrxId.getTrxId();
        try {
        	
            ThreadContext.push(trxId);
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);
            accUserInfo.setTrxId(trxId);
            accUserInfo.setUser(accManagUser);
            campaignEditObj = campaignObj;

            if (campaignEditObj != null) {
                editedCampType = campaignEditObj.getCampaignType();

                if (editedCampType.equals(CampaignType.CUSTOMIZED_CAMPAIGN)) {
                    customizeCampFlag = true;
                    individualFlag = false;
                } else if (editedCampType.equals(CampaignType.INTRA_CAMPAIGN)) {
                    intraCampFlag = true;
                    individualFlag = false;
                } else if (editedCampType.equals(CampaignType.NORMAL_CAMPAIGN)) {
                    normalCampFlag = true;
                    individualFlag = true;
                }

                userLogger.debug("Edited campaign [id="
                        + campaignEditObj.getCampaignId() + ",campaign_name=" + campaignEditObj.getCampaignName()
                        + ",status=" + campaignEditObj.getStatus().name() + ",creation_date="
                        + campaignEditObj.getCreationTimestamp() + ",start_date="
                        + campaignEditObj.getScheduleStartTimestamp() + ",end_date="
                        + campaignEditObj.getScheduleEndTimestamp() + ",stop_date="
                        + campaignEditObj.getScheduleStopTime() + ",sender_name=" + campaignEditObj.getSenderName()
                        + ",schedule_flag=" + campaignEditObj.isScheduledFlag() + ",frequency="
                        + campaignEditObj.getScheduleFrequency().name() + ",text=" +encyrptionUtil.encrypt(campaignEditObj.getSmsText())
                        + campaignEditObj.getCampaignType().value() + "]");

                if (campaignEditObj.getStatus() == CampaignStatus.NEW) {
                    userLogger.debug("Contact list of edited campaign with id=" + campaignEditObj.getCampaignId()
                            + " with status=NEW ,can not be edited");
                    hideList = true;
                }

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                startDate = campaignEditObj.getScheduleStartTimestamp();
                if (campaignEditObj.getScheduleEndTimestamp() != null) {
                    endDate = formatter.format(formatter.parse(campaignEditObj.getScheduleEndTimestamp()));
                } else {
                    endDate = " ";
                }

                if (campaignEditObj.getScheduleStopTime() != null) {
                    stopTime = timeFormatter.format(dateFormatter.parse(campaignEditObj.getScheduleStopTime()));
                } else {
                    stopTime = " ";
                }

                if (campaignEditObj.isScheduledFlag()) {
                    saveButtonFlag = true;
                    scheduleFlag = true;
                    dateAndTimeFlag = true;
                }

                if (campaignEditObj.getScheduleFrequency().name().equals(ScheduleFrequency.ONCE.name())) {
                    onceFlag = true;
                } else {
                    onceFlag = false;
                    frequencyFlag = true;
                    endDateFlag = true;
                    frequencyName = campaignEditObj.getScheduleFrequency().name();
                }

                activeUserSenderList = new ArrayList<String>();
                if (normalCampFlag) {
                    activeUserSenderList = userAcc.getAccount().getSender();
                } else if (customizeCampFlag) {
                    activeUserSenderList = userAcc.getAccount().getSender();
                } else if (intraCampFlag) {
                    // activeUserSenderList =
                    // userAcc.getAccount().getIntraSender();
                    userLogger.info("Getting account by company name to get all senders. ");

                    accountManegmentServicePort = portObj.getAccountServicePort();
                    AccountResultFullInfo accResult = accountManegmentServicePort.findAccountByCompanyNameFullInfo(
                            accUserInfo, userAcc.getAccount().getCompanyName());
                    com.edafa.web2sms.service.acc_manag.enums.ResponseStatus status = accResult.getStatus();

                    userLogger.info("Getting account by company name finished by status: (" + status + ").");

                    switch (status) {
                        case SUCCESS:
                            List<String> intraSenders = accResult.getAccount().getIntraSender();
                            if (intraSenders != null) {
                                for (int i = 0; i < intraSenders.size(); i++) {
                                    if (!activeUserSenderList.contains(intraSenders.get(i))) {
                                        activeUserSenderList.add(intraSenders.get(i));
                                    }
                                }
                            }

                            break;

                        case FAIL:
                            userLogger.error("Getting account by company name finished by status: (" + status + ").");

                            break;
                        case ACCT_NOT_EXIST:
                            userLogger.error("Getting account by company name finished by status: (" + status + ").");
                            break;
                        default:
                            userLogger.error("Getting account by company name finished by undefined status: (" + status + ").");
                            break;
                    }
                }

                CampaignDetailsResult result = campaignServicePort.getCampaignDetailes(userInfo,
                        campaignEditObj.getCampaignId());
                ResponseStatus status = result.getStatus();
                switch (status) {
                    case SUCCESS:
                        contactListInfoList = result.getCampaignDetails().getContactLists();
                        individualEditObjList = result.getCampaignDetails().getIndividualContacts();

                        selectedList = contactListInfoList;
                        for (ContactListInfo list : contactListInfoList) {
                            checked.put(list.getListId(), true);
                            numOfSelectedLists = numOfSelectedLists + list.getContactsCount();
                        }

                        selectedContact.addAll(individualEditObjList);
                        individualContactList.addAll(individualEditObjList);
                        for (int i = 0; i < individualEditObjList.size(); i++) {
                            checked2.put(individualEditObjList.get(i).getMsisdn(), true);
                            numOfSelectedLists = numOfSelectedLists + 1;
                        }

                        userLogger.info(" Lists for campaign with id="
                                + campaignEditObj.getCampaignId() + " is retrieved successfully , records="
                                + contactListInfoList.size() + " and individual contacts contacts="
                                + individualEditObjList.size());
                        break;
                    case INVALID_REQUEST:
                        userLogger.warn("INVALID_REQUEST while retrieving lists for campaign with id="
                                + campaignEditObj.getCampaignId());
                        break;
                    case INELIGIBLE_ACCOUNT:
                        userLogger.warn("INELIGIBLE_ACCOUNT , can't retrieve lists for campaign_id="
                                + campaignEditObj.getCampaignId());
                        break;
                    case FAIL:
                        userLogger.error("Error while retrieving lists for campaign with id=" + campaignEditObj.getCampaignId());
                        break;

                    default:
                        userLogger.error(" while retrieving lists for campaign_id=" + campaignEditObj.getCampaignId());
                        break;
                }

                newSmsText = campaignEditObj.getSmsText();
            }

            if (campaignEditObj.getLanguage().name().equals("ENGLISH")) {
                editedLanguage = "true";
            } else {
                editedLanguage = "false";
            }

            tempChecked();
            // TODO check campaign type.. for populating available sender names
            // and lists.
            
            campaignBeforeUpdate = new Campaign();
            campaignBeforeUpdate.setAction(campaignEditObj.getAction());
            campaignBeforeUpdate.setCampaignName(campaignEditObj.getCampaignName());
            campaignBeforeUpdate.setCampaignType(campaignEditObj.getCampaignType());
            campaignBeforeUpdate.setCreationTimestamp(campaignEditObj.getCreationTimestamp());
            campaignBeforeUpdate.setEndTimestamp(campaignEditObj.getScheduleEndTimestamp());
            campaignBeforeUpdate.setLanguage(campaignEditObj.getLanguage());
            campaignBeforeUpdate.setScheduledFlag(campaignEditObj.isScheduledFlag()); 
            campaignBeforeUpdate.setScheduleEndTimestamp(campaignEditObj.getScheduleEndTimestamp());
            campaignBeforeUpdate.setScheduleFrequency(campaignEditObj.getScheduleFrequency());
            campaignBeforeUpdate.setScheduleStartTimestamp(campaignEditObj.getScheduleStartTimestamp());
            campaignBeforeUpdate.setScheduleStopTime(campaignEditObj.getScheduleStopTime());
            campaignBeforeUpdate.setSenderName(campaignEditObj.getSenderName());
            campaignBeforeUpdate.setSmsCount(campaignEditObj.getSmsCount());
            campaignBeforeUpdate.setSmsSegCount(campaignEditObj.getSmsSegCount());
            campaignBeforeUpdate.setSmsText(campaignEditObj.getSmsText());
            campaignBeforeUpdate.setStartTimestamp(campaignEditObj.getStartTimestamp());
            campaignBeforeUpdate.setType(campaignEditObj.getType());
            contactListInfoListBeforeUpdate = new ArrayList<Integer>();
            individualEditObjListBeforeUpdate= new ArrayList<String>();
            for (ContactListInfo list : contactListInfoList) {
            	contactListInfoListBeforeUpdate.add(list.getListId());
			}
            for (Contact contact : individualEditObjList) {
            	individualEditObjListBeforeUpdate.add(contact.getMsisdn());
			}
           
        } catch (Exception e) {
            userLogger.error("Error while getting edited campaign object", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
        return "";
    }

    public void populateListOfLists() {

    	UserTrxInfo userInfo = new UserTrxInfo();
        String trxId = TrxId.getTrxId();
        try {
			
			
			ThreadContext.push(trxId);
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);
            listsManegementServicePort = portObj.getListsManegementServicePort();
            // individualContactList = new ArrayList<Contact>();

            List<ListType> typesList = new ArrayList<ListType>();
            if (intraCampFlag) {
                typesList.add(ListType.INTRA_LIST);
                typesList.add(ListType.INTRA_SUB_LIST);
                userLogger.debug("list types: intra and sub intra.");
            } else if (customizeCampFlag) {
                typesList.add(ListType.CUSTOMIZED_LIST);
                userLogger.debug("getting customized lists only.");
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
                        individualContactList = new ArrayList<Contact>();
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
                                        userLogger.info( "Error while getting list of individual contacts");
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
                    userLogger.info( "Error while populating list of contat lists");
                    break;
                case INELIGIBLE_ACCOUNT:
                    userLogger.info( "INELIGIBLE_ACCOUNT, not allowed to display list of contacts");
                default:
                    userLogger.info("Unkown response status=" + state + " for populating list of contact lists");
                    break;
            }

        } catch (Exception e) {
            userLogger.error("Error while populating list", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }

    }

    // Validate date ------------------------------------------------
    private boolean isRescheduled(Campaign origCampSched) {
        if (onceFlag == true) {
            frequencyName = "ONCE";
        }
        boolean reschduled = !origCampSched.isScheduledFlag().equals(scheduleFlag)
                || !origCampSched.getScheduleFrequency().name().equals(frequencyName)
                || !origCampSched.getScheduleStartTimestamp().equals(startDate);

        if (!reschduled && origCampSched.getScheduleEndTimestamp() != null) {
            reschduled = !origCampSched.getScheduleEndTimestamp().equals(endDate);
        }

        if (!reschduled && origCampSched.getScheduleStopTime() != null) {
            reschduled = !origCampSched.getScheduleStopTime().equals(stopTime);
        }

        return reschduled;
    }

    /**
     * Validate date
     *
     * @return boolean, true is valid ,false is not valid
     * @param string date
     */
    private boolean validateDate() {
        userLogger.debug("Validating date for edited campaign [id=" + campaignEditObj.getCampaignId() + ",campaign_name="
                + campaignEditObj.getCampaignName() + ",status=" + campaignEditObj.getStatus().name()
                + ",creation_date=" + campaignEditObj.getCreationTimestamp() + ",start_date="
                + campaignEditObj.getScheduleStartTimestamp() + ",end_date="
                + campaignEditObj.getScheduleEndTimestamp() + ",stop_date=" + campaignEditObj.getScheduleStopTime()
                + ",sender_name=" + campaignEditObj.getSenderName() + ",schedule_flag="
                + campaignEditObj.isScheduledFlag() + ",frequency=" + campaignEditObj.getScheduleFrequency().name()
                + ",text=" + encyrptionUtil.encrypt(campaignEditObj.getSmsText()) + "]");
        checkIsScheduled = isRescheduled(campaignEditObj);
        if (checkIsScheduled) {
            if (scheduleFlag == false) {
                userLogger.debug("Campaign_id="
                        + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                        + " is starting as soon as possible");

                if (frequencyFlag == true) {
                    // check end date
                    userLogger.debug("Campaign_id="
                            + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                            + " has a frequency=" + campaignEditObj.getScheduleFrequency().name());

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String tempEndDate = endDate;
                    Calendar c = Calendar.getInstance();

                    try {
                        finalEndDate = formatter.parse(tempEndDate);

                        if (tempEndDate == null || tempEndDate.trim().isEmpty()) {
                            userLogger.debug("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " has no end date");

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
                                userLogger.info( "Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName() + " end date is before current date"
                                        + " ,end_date=" + campaignEditObj.getScheduleEndTimestamp() + " current_date="
                                        + c.getTime());
                                return false;
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("invalid_date")), null));
                        userLogger.error( "Campaign_id="
                                + campaignEditObj.getCampaignId() + " ,campaign_name="
                                + campaignEditObj.getCampaignName() + " ,error while pasring end_date="
                                + tempEndDate, e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
                        return false;
                    }
                } else {
                    // now all
                    userLogger.debug("Campaign_id="
                            + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                            + " is starting as soon as possible with stop_date=null and  end_date=null");

                    stopTime = null;
                    endDate = null;
                    return true;
                }
            } else {
                if (frequencyFlag == true) {
                    // scheduled with frequency
                    userLogger.debug( "Campaign_id="
                            + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                            + " is scheduled with frequency");

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat endDateformatter = new SimpleDateFormat("dd/MM/yyyy");
                    String tempStartDate = startDate;
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
                            userLogger.debug( "Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " stop time is set to null");

                            finalStopTime = null;
                        } else {
                            finalStopTime = timeFormatter.parse(tempStopTime);
                            ct.setTime(finalStopTime);

                            userLogger.debug( "Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " stop_time=" + finalStopTime);
                        }

                        c.clear(Calendar.SECOND);
                        c.clear(Calendar.MILLISECOND);

                        ct.set(2014, 01, 01);
                        cd.setTime(finalStartDate);
                        cd.set(2014, 01, 01);

                        if (!finalStartDate.equals(c.getTime())) {
                            userLogger.debug("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " start date don't equal current date"
                                    + " ,start_date=" + campaignEditObj.getScheduleStartTimestamp() + " current_date="
                                    + c.getTime());

                            if (finalStartDate.before(c.getTime())) {
                                FacesContext.getCurrentInstance().addMessage(
                                        null,
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                        .getLocalizedLabel("start_date_invalid")), null));
                                userLogger.info( "Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName() + " start date is before current date"
                                        + " ,start_date=" + campaignEditObj.getScheduleStartTimestamp()
                                        + " current_date=" + c.getTime());
                                return false;
                            } else if (finalStopTime != null) {
                                userLogger.info("Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName() + " stop time is not null, stop_time="
                                        + campaignEditObj.getScheduleStopTime());
                                if (ct.before(cd)) {
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("stop_time_invalid")), null));
                                    userLogger.info( "Campaign_id=" + campaignEditObj.getCampaignId() + " ,campaign_name="
                                            + campaignEditObj.getCampaignName() + " stop time is after start time"
                                            + " ,stop_time=" + campaignEditObj.getScheduleStopTime() + " start_date="
                                            + campaignEditObj.getScheduleStartTimestamp());
                                    return false;
                                }
                            }
                        }

                        if (finalStopTime != null) {
                            userLogger.info("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " stop time is not null ,stop_time="
                                    + campaignEditObj.getScheduleStopTime());

                            if (ct.before(cd)) {
                                FacesContext.getCurrentInstance().addMessage(
                                        null,
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                        .getLocalizedLabel("stop_time_invalid")), null));

                                userLogger.info("Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName() + " stop time is after start time"
                                        + " ,stop_time=" + campaignEditObj.getScheduleStopTime() + " start_date="
                                        + campaignEditObj.getScheduleStartTimestamp());
                                return false;
                            }
                        }

                        if (tempEndDate == null || tempEndDate.trim().isEmpty()) {
                            FacesContext.getCurrentInstance().addMessage(
                                    null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                    .getLocalizedLabel("end_date_required")), null));

                            userLogger.info("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " no end date");

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

                                userLogger.info("Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName()
                                        + " end date is before current date, end_date="
                                        + campaignEditObj.getScheduleEndTimestamp() + " current_date=" + c.getTime());

                                return false;
                            } else if (finalEndDate.before(finalStartDate)) {
                                FacesContext.getCurrentInstance().addMessage(
                                        null,
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                        .getLocalizedLabel("end_date_before_start_date")), null));

                                userLogger.info("Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName()
                                        + " end date is before start date, end_date="
                                        + campaignEditObj.getScheduleEndTimestamp() + " start_date="
                                        + campaignEditObj.getScheduleStartTimestamp());
                                return false;
                            }
                            userLogger.info("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " ,dates is validates successfully");
                        }
                        return true;
                    } catch (ParseException e) {
                        userLogger.error("Error while validating date for campaign_id=" + campaignEditObj.getCampaignId()
                                + " ,campaign_name=" + campaignEditObj.getCampaignName(), e);
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("invalid_date")), null));
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
                        return false;
                    }

                } else {
                    // scheduled no frequency
                    userLogger.info("Campaign_id="
                            + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                            + " ,is scheduled but with no frequency");

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                    String tempStartDate = startDate;
                    String tempStopTime = stopTime;
                    Calendar c = Calendar.getInstance();
                    Calendar ct = Calendar.getInstance();
                    Calendar cd = Calendar.getInstance();

                    try {
                        finalStartDate = formatter.parse(tempStartDate);

                        if (tempStopTime == null || tempStopTime.trim().isEmpty()) {
                            userLogger.info("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " ,stop time is set null");

                            finalStopTime = null;
                        } else {
                            finalStopTime = timeFormatter.parse(tempStopTime);
                            ct.setTime(finalStopTime);

                            userLogger.info("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " ,stop time is set, stop_time="
                                    + finalStopTime);
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

                                userLogger.info("Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName()
                                        + " ,start date is before current date ,start_date="
                                        + campaignEditObj.getScheduleStartTimestamp() + " current_date=" + c.getTime());

                                return false;
                            } else if (finalStopTime != null) {
                                if (ct.before(cd)) {
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("stop_time_invalid")), null));

                                    userLogger.info("Campaign_id=" + campaignEditObj.getCampaignId() + " ,campaign_name="
                                            + campaignEditObj.getCampaignName() + " stop time is after start time"
                                            + " ,stop_time=" + campaignEditObj.getScheduleStopTime() + " start_date="
                                            + campaignEditObj.getScheduleStartTimestamp());
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

                                userLogger.info("Campaign_id="
                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                        + campaignEditObj.getCampaignName() + " stop time is after start time"
                                        + " ,stop_time=" + campaignEditObj.getScheduleStopTime() + " start_date="
                                        + campaignEditObj.getScheduleStartTimestamp());
                                return false;
                            }
                            return true;
                        } else {
                            userLogger.info("Campaign_id="
                                    + campaignEditObj.getCampaignId() + " ,campaign_name="
                                    + campaignEditObj.getCampaignName() + " ,Date is validated successfully");
                            return true;
                        }
                    } catch (ParseException e) {
                        userLogger.error("Error while validating date for campaign_id=" + campaignEditObj.getCampaignId()
                                + " ,campaign_name=" + campaignEditObj.getCampaignName(), e);
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("invalid_date")), null));
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
                        return false;
                    }
                }
            }
        } else {
            return true;
        }
    }

    /**
     * validation method to make fields required
     *
     * @return boolean true or false
     */
    private boolean checkRequiredField() {
        if (campaignEditObj.getCampaignName().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("campaign_name_required")), null));
            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " without campaign name");
            return false;
        } else if (contactListInfoList.isEmpty() && individualContactList.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("list_contacts_required")), null));
            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                    + " without choosing any list or adding contacts");
            return false;
        } else if (newSmsText.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            String.format(Util.getLocalizedLabel("text_required")), null));
            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                    + " without sms text");
            return false;
        } else if (!onceFlag && campaignEditObj.getScheduleFrequency() == null) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("frequency_required")), null));
            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                    + " without choosing frequency");
            return false;
        } else if (!onceFlag && endDate.trim().isEmpty()
                && !campaignEditObj.getScheduleFrequency().toString().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("end_date_required")), null));
            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                    + " with frequency but without end date");
            return false;
        } else if (!onceFlag && endDate.trim().isEmpty()
                && campaignEditObj.getScheduleFrequency().toString().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("frequency_required")), null));
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("end_date_required")), null));

            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " ,camapign_name=" + campaignEditObj.getCampaignName()
                    + " without choosing frequency and entering end date");
            return false;
        } else if (scheduleFlag && startDate.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            String.format(Util.getLocalizedLabel("invalid_date")), null));
            userLogger.info("Trying to edit scheduled campaign_id=" + campaignEditObj.getCampaignId() + " ,campaign_name="
                    + campaignEditObj.getCampaignName() + " without start date");
            return false;

        } else {
            userLogger.info("Trying to edit campaign_id="
                    + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                    + " with successfully fullfiled fields");
            return true;
        }
    }

    private boolean checkFieldLenght() {
        try {
            if (campaignEditObj.getCampaignName().length() > 100) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("invalid_campaign_name_lenght")), null));
                userLogger.info("Trying to edit campaign_id="
                        + campaignEditObj.getCampaignId() + " with invalid number of characters for the campaign_name="
                        + campaignEditObj.getCampaignName());
                return false;
            } else if (newSmsText.length() > 4000) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("invalid_campaign_text_lenght")), null));
                userLogger.info("Trying to edit campaign_id="
                        + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                        + " with invalid number of characters for the campaign sms text="
                        + encyrptionUtil.encrypt(campaignEditObj.getSmsText()));
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
            userLogger.error("Error while checking field lenght for campaign_id=" + campaignEditObj.getCampaignId()
                    + " ,campaign_name=" + campaignEditObj.getCampaignName(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
            return false;
        }
    }

    // Check
    // methods-------------------------------------------------------------
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
		if (campaignBeforeUpdate.getScheduleFrequency() != null &&
				! campaignBeforeUpdate.getScheduleFrequency().equals(ScheduleFrequency.ONCE)) {
			if (frequencyFlag == false) {
				isCampaignFrequencyFlagUpdated = true;
				isCampaignSchedualFrequencyUpdated = false;
				campaignEditObj.setScheduleFrequency(ScheduleFrequency.ONCE);
			}else {
				isCampaignFrequencyFlagUpdated = false;
				isCampaignSchedualFrequencyUpdated = false;
				campaignEditObj.setScheduleFrequency(campaignBeforeUpdate.getScheduleFrequency());
				campaignEditObj.setScheduleEndTimestamp(campaignBeforeUpdate.getScheduleEndTimestamp());
			}
		} else {
			if (frequencyFlag == false) {
				isCampaignFrequencyFlagUpdated = false;
				isCampaignSchedualFrequencyUpdated = false;
				campaignEditObj.setScheduleFrequency(campaignBeforeUpdate.getScheduleFrequency());
				campaignEditObj.setScheduleEndTimestamp(campaignBeforeUpdate.getScheduleEndTimestamp());
			}else {
				isCampaignFrequencyFlagUpdated = true;
				isCampaignSchedualFrequencyUpdated=false;
				campaignEditObj.setScheduleFrequency(ScheduleFrequency.ONCE);
			}
		}
		executeCampaginEditedFlag();
	}

    /**
     * for value change listener to get if the box is checked or not
     *
     * @return void
     */
    public void dateChecked(ValueChangeEvent e) {
        if (e.getNewValue().toString().equals("true")) {
            saveButtonFlag = true;
            dateAndTimeFlag = true;
            if (frequencyFlag == true) {
                endDateFlag = true;
            }
        } else if (e.getNewValue().toString().equals("false")) {
            saveButtonFlag = false;
            dateAndTimeFlag = false;
            if (frequencyFlag == true) {
                endDateFlag = true;
            }
        }
		if (campaignBeforeUpdate.isScheduledFlag() != dateAndTimeFlag) {
			isScheduleFlagUpdated = true;
		} else {
			isScheduleFlagUpdated = false;
		}
		executeCampaginEditedFlag();
	}
	
	public void campaingStartDateChange(String startDate) {
		if (campaignBeforeUpdate.getScheduleStartTimestamp() != null)
			if (!campaignBeforeUpdate.getScheduleStartTimestamp().equals(startDate)) {
				isCampaignScheduledStartDateUpdated = true;
			} else {
				isCampaignScheduledStartDateUpdated = false;
			}
		executeCampaginEditedFlag();
	}
	
	public void campaingStopTimeChange(String stopTime) {
		if (campaignBeforeUpdate.getScheduleStopTime() != null) {
			if (!campaignBeforeUpdate.getScheduleStopTime().equals(stopTime)) {
				isCampaignScheduledStopTimeUpdated = true;
			} else {
				isCampaignScheduledStopTimeUpdated = false;
			}
		} else {
			if (stopTime == null || stopTime.trim().equals("")) {
				isCampaignScheduledStopTimeUpdated = false;
			} else {
				isCampaignScheduledStopTimeUpdated = true;
			}
			
		}
		executeCampaginEditedFlag();
	}
	
	public void campaingscheduleFrequencyChange(String schedualFrequency) {
		if (campaignBeforeUpdate.getScheduleFrequency() != null) {
			if (!campaignBeforeUpdate.getScheduleFrequency().name().equals(schedualFrequency)) {
				isCampaignSchedualFrequencyUpdated = true;
			} else {
				isCampaignSchedualFrequencyUpdated = false;
			}
		} else {
			if (schedualFrequency == null || schedualFrequency.trim().equals("")) {
				isCampaignSchedualFrequencyUpdated = false;
			} else {
				isCampaignSchedualFrequencyUpdated = true;
			}
		}
		executeCampaginEditedFlag();
	}
	
	
	public void campaingTemplateChange(String text) {
		if (!campaignBeforeUpdate.getSmsText().equals(text)) {
			isCampaignTextUpdated = true;
		} else {
			isCampaignTextUpdated = false;
		}
		executeCampaginEditedFlag();
	}
	public void campaingScheduleEndDateChange(String endTime) {
		String x = new String(endTime.trim());
		if (campaignBeforeUpdate.getScheduleEndTimestamp() != null) {
			String x2 = new String(campaignBeforeUpdate.getScheduleEndTimestamp().trim());
			if (!x2.equals(x)) {
				isCampaignSchedualEndTimeUpdated = true;
			} else {
				isCampaignSchedualEndTimeUpdated = false;
			}
		} else {
			if (x == null || x.trim().equals("")) {
				isCampaignSchedualEndTimeUpdated = false;
			} else {
				isCampaignSchedualEndTimeUpdated = true;
			}
		}
		executeCampaginEditedFlag();
	}
	
	public void executeCampaginEditedFlag() {
		campaignEdited = isCampaignNameUpdated || isScheduleFlagUpdated 
				|| isCampaignScheduledStopTimeUpdated || isCampaignScheduledStartDateUpdated
				|| isCampaignFrequencyFlagUpdated || isCampaignSchedualFrequencyUpdated
				|| isCampaignSchedualEndTimeUpdated || isSenderNameUpdated
				|| isCampaignTextUpdated || isCampaignSendToUpdated;
	}
    public String getRemovedContact(String testMsisdn) {
        try {
            for (int i = 0; i < selectedContact.size(); i++) {
                userLogger.info("Selected contact with msisdn "
                        + encyrptionUtil.encrypt(selectedContact.get(i).getMsisdn()) + " is removed from the selected contact list");
                if (testMsisdn.equals(selectedContact.get(i).getMsisdn())) {
                    numOfSelectedLists = numOfSelectedLists - 1;
                    checked2.put(selectedContact.get(i).getMsisdn(), false);
                    selectedContact.remove(i);
                }
            }
            reExecuteSendToFlag();
        } catch (Exception e) {
            userLogger.error("Error while removing selected contact from contact list");
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

        }
        return "";
    }

    public String getRemovedList(String testList) {
        try {
            for (int i = 0; i < selectedList.size(); i++) {
                if (testList.equals(selectedList.get(i).getListName())) {
                    userLogger.info("Trying to edit campaign "
                            + campaignEditObj.getCampaignId() + " ,campaign_name=" + campaignEditObj.getCampaignName()
                            + "and remove list " + selectedList.get(i).getListId() + " from selected lists");
                    numOfSelectedLists = numOfSelectedLists - selectedList.get(i).getContactsCount();
                    checked.put(selectedList.get(i).getListId(), false);
                    selectedList.remove(i);
                }
            }
            reExecuteSendToFlag();
        } catch (Exception e) {
            userLogger.error( " Error while trying to edit campaign " + campaignEditObj.getCampaignId() + " ,campaign_name="
                    + campaignEditObj.getCampaignName() + "and remove list from selected lists");
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

        }
        return "";
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
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);
            ThreadContext.push(trxId);
            templateList = new ArrayList<Template>();

            userLogger.debug("Setting TemplateManegmentService port............");
            templateManegmentServicePort = portObj.getTemplateManegmentServicePort();
            userLogger.info("Done Setting TemplateManegmentService port");

            userLogger.info( "Calling get user templates....");
            TemplatesResultSet tempResult = templateManegmentServicePort.getUserAndAdminTemplates(userInfo);

            ResponseStatus status = tempResult.getStatus();

            switch (status) {
                case SUCCESS:
                    templateList = tempResult.getTemplateList();
                    userLogger.info("Template list populate successfully, count=" + templateList.size());
                    break;
                case FAIL:
                    userLogger.error("Error while getting user template list");
                    break;
                case INELIGIBLE_ACCOUNT:
                    userLogger.warn("INELIGIBLE_ACCOUNT, not allowed to display the list of templates");
                    break;
                case TEMPLATES_NOT_FOUND:
                    userLogger.warn("TEMPLATES_NOT_FOUND");
                    break;
                case INVALID_REQUEST:
                    userLogger.warn("INVALID_REQUEST for retrieving templates");
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
        setNewSmsText(e.getNewValue().toString());
        campaingTemplateChange(newSmsText);
    }

	public void campaingNameChange(String name) {
		if (!campaignBeforeUpdate.getCampaignName().equals(name)) {
			isCampaignNameUpdated = true;
		}else {
			isCampaignNameUpdated = false;
		}
        executeCampaginEditedFlag();

	}
	
    
    // Edit Action
    // methods--------------------------------------------------------------
    /**
     * for creating a campaign and submitting the form
     *
     * @return string
     */
    public void editCampaign(AjaxBehaviorEvent e) {
    	UserTrxInfo userInfo = new UserTrxInfo();
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);
        userInfo.setUser(user);

        if (editedLanguage.equals("true")) {
            campaignEditObj.setLanguage(Language.ENGLISH);
        } else {
            campaignEditObj.setLanguage(Language.ARABIC);
        }

        try {
            ThreadContext.push(trxId);
            if (checkRequiredField()) {
                if (checkFieldLenght()) {
                    if (validateDate()) {
                        if (validateCustmoizeSmsText()) {

                            campaignEditObj.setSmsText(newSmsText);
                            campaignEditObj.setRegisteredDelivery((Boolean) Configs.REGESTERED_DELIVERY.getValue());

                            if (checkIsScheduled) {
                                if (scheduleFlag == false) {
                                    if (frequencyFlag == true) {
                                        // campaignEditObj.setScheduleStartTimestamp(campaignEditObj.g);
                                        campaignEditObj.setScheduleStopTime(null);
                                        campaignEditObj.setScheduleEndTimestamp(fromDateToString(finalEndDate));
                                        campaignEditObj.setScheduledFlag(false);
                                    } else {
                                        // now
                                        // campaignEditObj.setScheduleStartTimestamp(startDate);
                                        campaignEditObj.setScheduleStopTime(null);
                                        campaignEditObj.setScheduleEndTimestamp(null);
                                        campaignEditObj.setScheduledFlag(false);
                                    }
                                } else {
                                    // later

                                    campaignEditObj.setScheduleStartTimestamp(fromDateToString(finalStartDate));
                                    if (finalStopTime == null) {
                                        campaignEditObj.setScheduleStopTime(null);
                                    } else {
                                        campaignEditObj.setScheduleStopTime(fromDateToString(finalStopTime));
                                    }

                                    if (finalEndDate == null) {
                                        campaignEditObj.setScheduleEndTimestamp(null);
                                    } else {
                                        campaignEditObj.setScheduleEndTimestamp(fromDateToString(finalEndDate));
                                    }

                                    campaignEditObj.setScheduledFlag(true);
                                }
                            }

                            if (!selectedList.isEmpty()) {
                                for (int i = 0; i < selectedList.size(); i++) {
                                    campaignEditObj.getContactList().add(selectedList.get(i).getListId());
                                }
                            }

                            if (!selectedContact.isEmpty()) {
                                individualContactList.clear();
                                for (int i = 0; i < selectedContact.size(); i++) {
                                    individualContactList.add(selectedContact.get(i));
                                    campaignEditObj.getIndividualContact().add(selectedContact.get(i));
                                }
                            }

                            userLogger.debug( "Editting campaign [id="
                                    + campaignEditObj.getCampaignId() + ",campaign_name="
                                    + campaignEditObj.getCampaignName() + ",status="
                                    + campaignEditObj.getStatus().name() + ",creation_date="
                                    + campaignEditObj.getCreationTimestamp() + ",start_date="
                                    + campaignEditObj.getScheduleStartTimestamp() + ",end_date="
                                    + campaignEditObj.getScheduleEndTimestamp() + ",stop_date="
                                    + campaignEditObj.getScheduleStopTime() + ",sender_name="
                                    + campaignEditObj.getSenderName() + ",schedule_flag="
                                    + campaignEditObj.isScheduledFlag() + ",frequency="
                                    + campaignEditObj.getScheduleFrequency().name() + ",text="
                                    + encyrptionUtil.encrypt(campaignEditObj.getSmsText()) + ",individual_contacts_count="
                                    + campaignEditObj.getIndividualContact().size() + ",contacts_lists_size="
                                    + campaignEditObj.getContactList().size() + ",language="
                                    + campaignEditObj.getLanguage().name() + "]");

                            CampaignResult status = campaignServicePort.updateCampaign(userInfo, campaignEditObj);
                            ResponseStatus response = status.getStatus();

                            switch (response) {
                                case INVALID_CAMPAIGN:

                                    for (int j = 0; j < status.getCampaignValidationStatus().size(); j++) {
                                        CampaignValidationStatus resp = status.getCampaignValidationStatus().get(j);
                                        switch (resp) {
                                            // case DUPLICAT_CAMPAIGN_NAME :
                                            // FacesContext.getCurrentInstance().addMessage(
                                            // null,
                                            // new
                                            // FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            // String.format(Util
                                            // .getLocalizedLabel("invalid_campaign_name")),
                                            // null));
                                            //
                                            // userLogger.info(logTrxId
                                            // + userLogInfo(user.getAccountId(),
                                            // user.getUsername())
                                            // + "Trying to edit campaign_id=" +
                                            // campaignEditObj.getCampaignId()
                                            // + " with duplicated campaign_name=" +
                                            // campaignEditObj.getCampaignName());
                                            // break;

                                            case INVALID_SCHEDULING:
                                                FacesContext.getCurrentInstance().addMessage(
                                                        null,
                                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                        .getLocalizedLabel("invalid_date")), null));

                                                userLogger.warn( "Trying to edit campaign_id=" + campaignEditObj.getCampaignId()
                                                        + " ,campaign_name=" + campaignEditObj.getCampaignName()
                                                        + " with invalid schedule");

                                                break;

                                            case INVALID_SENDER_NAME:
                                                FacesContext.getCurrentInstance().addMessage(
                                                        null,
                                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                        .getLocalizedLabel("invalid_sender_name")), null));
                                                userLogger.warn( "Trying to edit campaign_id=" + campaignEditObj.getCampaignId()
                                                        + " camaign_name=" + campaignEditObj.getCampaignName()
                                                        + " with invalid sender_name=" + campaignEditObj.getSenderName());
                                                break;

                                            case INVALID_SMS_TEXT:
                                                FacesContext.getCurrentInstance().addMessage(
                                                        null,
                                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                        .getLocalizedLabel("invalid_campaign_text_lenght")), null));
                                                userLogger.warn("Trying to edit campaign_id=" + campaignEditObj.getCampaignId()
                                                        + " campaign_name=" + campaignEditObj.getCampaignName()
                                                        + " with invalid sms_text=" + campaignEditObj.getSmsText());
                                                break;
                                            default:
                                                FacesContext.getCurrentInstance().addMessage(
                                                        null,
                                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                        .getLocalizedLabel("campaign_edit_failure")), null));

                                                userLogger.error("Unknown status=" + resp + " while trying to edit campaign_id="
                                                        + campaignEditObj.getCampaignId() + " ,campaign_name="
                                                        + campaignEditObj.getCampaignName());
                                                break;
                                        }// end switch

                                    }// end for
                                    break;
                                case INELIGIBLE_ACCOUNT:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("ineligible_user")), null));
                                    userLogger.warn( "INELIGIBLE_ACCOUNT ,not allowed to edit campaign_id="
                                            + campaignEditObj.getCampaignId() + " ,campaign_name="
                                            + campaignEditObj.getCampaignName());
                                    break;
                                case FAIL:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("campaign_edit_failure")), null));
                                    userLogger.error("Error while trying to edit campaign_id=" + campaignEditObj.getCampaignId()
                                            + " ,campaign_name=" + campaignEditObj.getCampaignName());
                                    break;
                                case CAMPAIGN_NOT_FOUND:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("campaign_edit_failure")), null));
                                    userLogger
                                            .warn( "Trying to edit campaign_id=" + campaignEditObj.getCampaignId()
                                                    + " ,campaign_name=" + campaignEditObj.getCampaignName()
                                                    + " that is not found");
                                    break;
                                case INVALID_CAMPAIGN_STATE:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("invalid_campaign_state")), null));
                                    userLogger.warn("Trying to edit campaign_id=" + campaignEditObj.getCampaignId()
                                            + " ,campaign_name=" + campaignEditObj.getCampaignName()
                                            + " with uneditable status");

                                    break;
                                case SUCCESS:
                                    goTohistory = true;
                                    // FacesContext.getCurrentInstance().addMessage(
                                    // null,
                                    // new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    // String.format(Util
                                    // .getLocalizedLabel("campaign_edit_successfully")),
                                    // null));
                                    userLogger.info("Campaign_id="
                                            + campaignEditObj.getCampaignId() + " ,campaign_name="
                                            + campaignEditObj.getCampaignName() + " is editted successfully");
                                    break;
                                case INVALID_REQUEST:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("campaign_edit_failure")), null));
                                    userLogger.warn("Invalid request while trying to edit campaign_id="
                                            + campaignEditObj.getCampaignId() + " ,campaign_name="
                                            + campaignEditObj.getCampaignName());
                                    break;

                                case INVALID_CAMPAIGN_LIST:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("invalid_campaign_list")), null));

                                    userLogger.info("Error while edit campaign_name=" + campaignEditObj.getCampaignName()
                                            + " because of empty list");

                                    break;

                                case ACCOUNT_QUOTA_NOT_FOUND:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("account_quota_not_found")), null));

                                    userLogger.info("account quota record not found for this account = ");

                                    break;

                                case ACCOUNT_QUOTA_EXCEEDED:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("account_quota_exceed")), null));

                                    userLogger.info( "account_quota_exceed while editing campaing = "
                                            + campaignEditObj.getCampaignName());

                                    break;

                                default:
                                    FacesContext.getCurrentInstance().addMessage(
                                            null,
                                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                            .getLocalizedLabel("campaign_edit_failure")), null));
                                    userLogger.error("Unknown response="
                                            + response + " while trying to edit campaign_id"
                                            + campaignEditObj.getCampaignId() + " ,campaign_name="
                                            + campaignEditObj.getCampaignName());
                                    break;
                            }// end big switch
                        }
                    }// end validate date if
                }// end field lenght if

            }

        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("campaign_edit_failure")), null));
            userLogger.error("Error while editing campaign_id=" + campaignEditObj.getCampaignId() + " ,campaign_name="
                    + campaignEditObj.getCampaignName(), ex);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

        }finally{
            ThreadContext.pop();
        }
    }

    /**
     * Converts XMLGregorianCalendar to java.util.Date in Java
     */
    public static Date toDate(XMLGregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }
        return calendar.toGregorianCalendar().getTime();
    }

    public void getSelectedLists(AjaxBehaviorEvent e) {
    	UserTrxInfo userInfo = new UserTrxInfo();
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);
        userInfo.setUser(user);
        List<ContactListInfo> allLists = new ArrayList<ContactListInfo>();
        try {
            ThreadContext.push(trxId);
            userLogger.debug("adding new contacts to virtual list, contacts_count=" + newIndividualList.size());

            userLogger.debug( "Trying to get all lists with their ids");
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
                    userLogger.info( "list of contact lists is populating successfully with count=" + allLists.size()
                            + " to find the checked list from it");

                    break;
                case FAIL:
                    userLogger.info("list of contact lists is not populated");
                    break;
                case INELIGIBLE_ACCOUNT:
                    userLogger.info( " This user is not allowed to display lists of contact lists");
                    break;

                default:
                    userLogger.info( "Unknown response status " + resultSet.getStatus()
                            + " , trying to populate list of contact lists");
                    break;
            }
            // TODO remove/ encyrpt msisdns??
            for (Contact contact : newIndividualList) {
                userLogger.trace("adding new contacts to virtual list contact_msisdn=" + encyrptionUtil.encrypt(contact.getMsisdn())
                        + " ,contact_first_name=" + contact.getFirstName());
            }

            ResultStatus status = listsManegementServicePort.handleVirtualList(userInfo, newIndividualList);
            ResponseStatus response = status.getStatus();
            userLogger.info("listsManegementServicePort response is:(" + response + ").");

            switch (response) {
                case CONTACTS_NOT_FOUND:
                    userLogger.info( "Contacts not found");
                    break;
                case SUCCESS:
                    userLogger.info( "Contacts added to individual list successfully");
                    break;
                case INELIGIBLE_ACCOUNT:
                    for (Contact contact : newIndividualList) {
                        individualContactList.remove(contact);
                    }

                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("add_new_individual_contacts_failure")), null));

                    userLogger.info( "INELIGIBLE_ACCOUNT, not allowed to add new contact to individual list");
                    break;
                case FAIL:
                    for (Contact contact : newIndividualList) {
                        individualContactList.remove(contact);
                    }

                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("add_new_individual_contacts_failure")), null));

                    userLogger.info("Error while adding contacts to individual list");
                    break;
                default:
                    for (Contact contact : newIndividualList) {
                        individualContactList.remove(contact);
                    }

                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("add_new_individual_contacts_failure")), null));

                    userLogger.info("Undefined response status=" + response + " while adding contacts to individual list");
                    break;
            }

            selectedList.clear();
            selectedContact.clear();
            numOfSelectedLists = 0;
            for (ContactListInfo list : allLists) {
                if (checked.get(list.getListId()) != null) {
                    if (checked.get(list.getListId()) != false) {
                        selectedList.add(list);
                        numOfSelectedLists = numOfSelectedLists + list.getContactsCount();
                    }
                }// end if
            }// end for

            for (Contact contact : individualContactList) {
                if (checked2.get(contact.getMsisdn()) != null) {
                    if (checked2.get(contact.getMsisdn()) != false) {
                        selectedContact.add(contact);
                        numOfSelectedLists = numOfSelectedLists + 1;
                    }
                }// end if
            }// end for
            reExecuteSendToFlag();
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("add_new_individual_contacts_failure")), null));
            userLogger.error("Error while adding new contacts to individual contacts list or getting all lists ids list", ex);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

        }finally{
            ThreadContext.pop();
        }
        userLogger.info(" Count of Selected contacts : "
                + numOfSelectedLists);

    }

    private void reExecuteSendToFlag() {
         List<Integer> selectedLists = new ArrayList<Integer>();
			List<String> selectedContacts = new ArrayList<String>();
			for (ContactListInfo list : selectedList) {
				selectedLists.add(list.getListId());
			}
			for (Contact contact : selectedContact) {
				selectedContacts.add(contact.getMsisdn());
			}
			if (contactListInfoListBeforeUpdate.equals(selectedLists) 
				&& individualEditObjListBeforeUpdate.equals(selectedContacts)){
				isCampaignSendToUpdated = false;
			} else {
				isCampaignSendToUpdated = true;
			}
			executeCampaginEditedFlag();
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
                    userLogger.info( "Searching for list with name="
                            + listNameSearch);
                    List<ListType> listTypes = new ArrayList<ListType>();

                    if (intraCampFlag) {
                        listTypes.add(ListType.INTRA_LIST);
                        listTypes.add(ListType.INTRA_SUB_LIST);
                    } else if (customizeCampFlag) {
                        listTypes.add(ListType.CUSTOMIZED_LIST);
                    } else {
                        listTypes.add(ListType.CUSTOMIZED_LIST);
                        listTypes.add(ListType.INTRA_LIST);
                        listTypes.add(ListType.INTRA_SUB_LIST);
                        listTypes.add(ListType.NORMAL_LIST);
                        listTypes.add(ListType.VIRTUAL_LIST);
                    }
                    ContactListResultSet result = listsManegementServicePort.searchLists(userInfo, listNameSearch,
                            listTypes);
                    ResponseStatus state = result.getStatus();

                    switch (state) {
                        case SUCCESS:
                            listInfo = result.getContactListInfoSet();
                            userLogger.info("Found lists/list with name="
                                    + listNameSearch + " successfully with count=" + listInfo.size());
                        case FAIL:
                            userLogger.error("Error while searching for list_name=" + listNameSearch);
                            break;
                        case LIST_NOT_FOUND:
                            userLogger.warn( "List_name=" + listNameSearch
                                    + " is not found");
                            break;
                        case INELIGIBLE_ACCOUNT:
                            userLogger.warn("INELIGIBLE_ACCOUNT, not allowed to display lists");
                        default:
                            userLogger.error("Undefined result status="
                                    + state + " while searching for list_name=" + listNameSearch);
                            break;
                    }
                } else {
                    populateListOfLists();
                }
            } else {
                populateListOfLists();
            }
        } catch (Exception e) {
            userLogger.error("Error while searching list_name=" + listNameSearch, e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

        }finally{
            ThreadContext.pop();
        }
    }

    public String fromDateToString(Date date) {
        try {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String reportDate = df.format(date);
            return reportDate;
        } catch (Exception e) {
            userLogger.error("Error while parsing from date="
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
        userInfo.setTrxId(trxId);
        try {
        	
            ThreadContext.push(trxId);
            if (changeSenderName.length() > 11) {
                senderNameSucces = false;
                userLogger.info("Changing sender name, too long name=" + changeSenderName);
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("invalid_sender_name_lenght")), null));
            } else if (changeSenderName.trim().isEmpty()) {
                userLogger.info("Changing sender name, no name");
                // FacesContext.getCurrentInstance().addMessage(null,
                // new FacesMessage(FacesMessage.SEVERITY_ERROR,
                // String.format(Util.getLocalizedLabel("invalid_sender_name")),
                // null));
            } else {
                SenderType senderType = SMSUtils.getSenderType(changeSenderName);
                if (senderType != SenderType.ALPHANUMERIC) {
                    userLogger.info("Sender name is not alphanumeric");
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
                            userLogger.debug("Start calling requestChangeSender ......");
                            ProvTrxInfo userInfo = new ProvTrxInfo();
                            userInfo.setTrxId(trxId);
                            userInfo.setAccountId(user.getAccountId());
                            ProvResultStatus result;
                            if (userAcc.getAccount().getSender().size() > 1) {
                                // TODO: multi sender editing while editing
                                // campaign
                                // choosen
                                result = serviceProvisioningPort.requestChangeSender(userInfo,
                                        campaignEditObj.getSenderName(), changeSenderName);
                            } else {
                                result = serviceProvisioningPort.requestChangeSender(userInfo,
                                        campaignEditObj.getSenderName(), changeSenderName);
                            }

                            userLogger.debug("Done caling requestChangeSender , returned status=" + result.getStatus());

                        }
                    }).start();

                    userLogger.info("User request is being processed");
                    // ProvResponseStatus status = result.getStatus();
                    //
                    // switch (status) {
                    // case INVALID_REQUEST :
                    // case FAIL :
                    // case ACCOUNT_NOT_FOUND :
                    // case SR_CREATION_FAILED :
                    // // senderNameSucces = false;
                    // FacesContext.getCurrentInstance().addMessage(
                    // null,
                    // new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    // String.format(Util.getLocalizedLabel("change_sender_failure")),
                    // null));
                    // userLogger.info(logTrxId +
                    // "Error while changeing sender name for user " +
                    // user.getUsername()
                    // + " with id " + user.getAccountId());
                    // break;
                    // case INVALID_SENDER :
                    // // senderNameSucces = false;
                    // FacesContext.getCurrentInstance()
                    // .addMessage(
                    // null,
                    // new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    // String.format(Util.getLocalizedLabel("invalid_sender_name")),
                    // null));
                    // userLogger.info(logTrxId +
                    // "Invalid sender");
                    // break;
                    // case SENDER_NAME_ALREADY_ATTACHED :
                    // // senderNameSucces = false;
                    // FacesContext.getCurrentInstance().addMessage(
                    // null,
                    // new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    // String.format(Util
                    // .getLocalizedLabel("sender_name_already_attached")),
                    // null));
                    // userLogger.info(logTrxId +
                    // "Sender name already attached");
                    // break;
                    // case SUCCESS :
                    // // senderNameSucces = true;
                    // FacesContext.getCurrentInstance().addMessage(
                    // null,
                    // new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    // String.format(Util.getLocalizedLabel("change_sender_success")),
                    // null));
                    // userLogger.info(logTrxId +
                    // "Sender name changed successfully for user " +
                    // user.getUsername()
                    // + " with id " + user.getAccountId());
                    // break;
                    //
                    // default :
                    // // senderNameSucces = false;
                    // FacesContext.getCurrentInstance().addMessage(
                    // null,
                    // new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    // String.format(Util.getLocalizedLabel("change_sender_failure")),
                    // null));
                    // userLogger.info(logTrxId +
                    // "Undefiend status for changing sender name for user" +
                    // user.getUsername()
                    // + " with id " + user.getAccountId());
                    // break;
                    // }
                }
            }
        } catch (InvalidSMSSender e1) {
            senderNameSucces = false;
            // FacesContext.getCurrentInstance().addMessage(null,
            // new FacesMessage(FacesMessage.SEVERITY_ERROR,
            // String.format(Util.getLocalizedLabel("invalid_sender_name")),
            // null));
            userLogger.error( "Invalid sms sender=" + changeSenderName, e1);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.INVALID_REQUEST, "Invalid SMS sender");

        }finally{
            ThreadContext.pop();
        }
        return "";
    }

    public void clearAfterCancel() {
        changeSenderName = "";
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
            UserTrxInfo userInfo = new UserTrxInfo();
            String trxId = TrxId.getTrxId();
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);
            
            ThreadContext.push(trxId);
            if (checkRequiredField2()) {
                if (SMSUtils.validateLocalAddress(msisdn) || SMSUtils.validateInternationalAddress(msisdn)) {
                    userLogger.info("Msisdn=" + encyrptionUtil.encrypt(msisdn)
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
                    userLogger.info("validation result while dding contact to indiviual list: Msisdn is not valid");
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
            userLogger.error( "error while creating individual contact with msisdn=" + encyrptionUtil.encrypt(msisdn), e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
        return null;
    }

    // get contacts of each list-------------------------------------
    public void getUpdateListId(String listId2) {
        listId = listId2;
        contactList = new ArrayList<Contact>();
        try {
            if (checked.get(Integer.valueOf(listId2))) {
                listCheckedFlag = true;
            } else {
                listCheckedFlag = false;
                firstRow1 = 0;
                populateContactLists();
            }
        } catch (Exception e) {
            listCheckedFlag = false;
            firstRow1 = 0;
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

            userLogger.debug("Getting contacts of selected list_id=" + listId + " from=" + firstRow1 + " ,rows_count="
                    + rowsPerPage1);

            ContactResultSet result = listsManegementServicePort.getContactListWithPagination(userInfo,
                    Integer.valueOf(listId), firstRow1, rowsPerPage1);
            ResponseStatus state = result.getStatus();

            switch (state) {
                case SUCCESS:
                    for (Contact contact : result.getContacts()) {
                        contactList.add(contact);
                        userLogger.trace("Selected list_id=" + listId
                                + " contact [msisdn=" + encyrptionUtil.encrypt(contact.getMsisdn()) + ",first_name=" + contact.getFirstName());
                    }
                    userLogger.info( "List of contact lists is populated successfully, rows_count=" + contactList.size());
                    break;
                case FAIL:
                    contactList = null;
                    userLogger.error( "Error while getting list of contat lists list_id=" + listId);
                    break;
                case INELIGIBLE_ACCOUNT:
                    userLogger.warn( "INELIGIBLE_ACCOUNT, not allowed to display list of contacts list_id=" + listId);
                default:
                    userLogger.error( "Unkown response status=" + state + " for getting list of contact lists list_id=" + listId);
                    break;
            }
            userLogger.info("list menu of list_id=" + listId + " is populated successfully");

        } catch (Exception e) {
            userLogger.error("error while populating list_id=" + listId, e);
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
            userLogger.error("Error while getting selected contacts from list", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }
    }

    public void checkValueChanged(AjaxBehaviorEvent event) {
        if (checked.get(Integer.valueOf(listId))) {
            listCheckedFlag = true;
        }
        listCheckedFlag = false;
    }

    public void cleanForm() {
        msisdn = "";
    }

    public String deleteSingleSender() {
        ProvTrxInfo userInfo = new ProvTrxInfo();
        AccountUserTrxInfo userTRXInfo = new AccountUserTrxInfo();
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);
        userTRXInfo.setTrxId(userInfo.getTrxId());

		try {
			
			ThreadContext.push(trxId);
            serviceProvisioningPort = portObj.getServiceProvisioningPort();
            accountManegmentServicePort = portObj.getAccountServicePort();
            facesContext = FacesContext.getCurrentInstance();
            request = (HttpServletRequest) facesContext.getExternalContext().getRequest();


            if (campaignEditObj.getSenderName() == null || campaignEditObj.getSenderName().trim().isEmpty()
                    || campaignEditObj.getSenderName().trim().equals("")) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("choose_sender_to_delete")), null));
            } else {
                userInfo.setAccountId(user.getAccountId());
                userTRXInfo.setUser(accManagUser);

                ProvResultStatus result = serviceProvisioningPort.requestDeleteSender(userInfo,
                        campaignEditObj.getSenderName());

                ProvResponseStatus response = result.getStatus();

                switch (response) {
                    case FAIL:
                        userLogger.info( "Error while deleting sender name=" + campaignEditObj.getSenderName());
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("delete_sender_failure")), null));
                        break;
                    case SUCCESS:
                        userLogger.info("Sender name="
                                + campaignEditObj.getSenderName() + " deleted successfully");

                        if (request.getSession().getAttribute(USER_KEY) != null) {
                            UserAccount updatedUser = new UserAccount();
                            String userName = ((UserAccount) request.getSession().getAttribute(USER_KEY))
                                    .getAccount().getCompanyDomain();
                            AccountResultFullInfo res = accountManegmentServicePort.findAccountByCompanyNameFullInfo(
                                    userTRXInfo, userAcc.getAccount().getCompanyDomain());
                            if (res.getStatus().equals(com.edafa.web2sms.service.acc_manag.enums.ResponseStatus.SUCCESS)) {
                                updatedUser.setAccount(res.getAccount());
                                List<String> newsendersList = res.getAccount().getSender();

                                activeUserSenderList.clear();
                                activeUserSenderList.addAll(newsendersList);

                                if (newsendersList.size() > 1) {
                                    String sendersStrings = "";
                                    for (String s : newsendersList) {
                                        sendersStrings = sendersStrings + s + " , ";
                                    }
                                    userLogger.info( " has multi senders=[" + sendersStrings + "]");
                                } else {
                                    userLogger.info( " has one sender_name="
                                            + res.getAccount().getSender().get(0));

                                    campaignEditObj.setSenderName(activeUserSenderList.get(0));
                                }
                                updatedUser.setUsername(userName);

                                request.getSession().removeAttribute(USER_KEY);

                                request.getSession().setAttribute(USER_KEY, updatedUser);
                            }else{
                                userLogger.info("failed to read sender names");
                            }

                        }else{
                            userLogger.info("no user in session");
                        }

                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                                .getLocalizedLabel("delete_sender_success")), null));

                        break;
                    case SENDER_NAME_ALREADY_ATTACHED:
                        userLogger.info("Sender name already attached");
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("delete_sender_failure")), null));
                        break;
                    case INVALID_SENDER:
                        userLogger.info("Invalid sender to delete , sender name=" + campaignEditObj.getSenderName());
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("delete_sender_failure")), null));
                        break;
                    default:
                        userLogger.info("Error while deleting sender, sender name=" + campaignEditObj.getSenderName());
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
            userLogger.error("Error while deleting sender", e1);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
        return "";
    }

    public void getChoosenSenderName(ValueChangeEvent e) {
        userLogger.info("choose new sender_name="
                + e.getNewValue());

        if (e.getNewValue() != null || !e.getNewValue().toString().trim().equals("")) {
            campaignEditObj.setSenderName(e.getNewValue().toString());
            userLogger.info("choosen sender_name="
                    + e.getNewValue());
            if(campaignBeforeUpdate.getSenderName().equals(e.getNewValue().toString())){
            	isSenderNameUpdated=false;
            }else{
            	isSenderNameUpdated=true;
            }
        }
        executeCampaginEditedFlag();
    }

    public String addNewSenderName() {
        ProvTrxInfo userInfo = new ProvTrxInfo();
        final String trxId = TrxId.getTrxId();
        try {
        	
            ThreadContext.push(trxId);
            if (newSenderName.length() > 11) {
                senderNameSucces = false;
                userLogger.info("Adding new sender name, too long name=" + newSenderName);
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("invalid_sender_name_lenght")), null));
            } else if (newSenderName.trim().isEmpty()) {
                userLogger.info("Adding new sender name, no name");
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("invalid_sender_name")), null));
            } else {
                SenderType senderType = SMSUtils.getSenderType(newSenderName);
                if (senderType != SenderType.ALPHANUMERIC) {
                    userLogger.info("Sender name is not alphanumeric");
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
            userLogger.error("Invalid sms sender=" + newSenderName, e1);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

        }finally{
            ThreadContext.pop();
        }
        return "";
    }

    public void isTempChecked(ValueChangeEvent e) {

        if (e.getNewValue().toString().equals("true")) {
            templateChecked = true;
        } else {
            templateChecked = false;
        }
    }

    // TODO should be configuration value ?!
    boolean validateCustmoizeSmsText() {
        if (customizeCampFlag) {
        	String ecyrptedTemplate = encyrptionUtil.encrypt(newSmsText);
            userLogger.trace("validate sms text: (" + ecyrptedTemplate
                    + ") against this pattern (?s).*[$][1-5](?s).* ");
            if (!newSmsText.matches("(?s).*[$][1-5](?s).*")) {
                userLogger.warn("sms text: (" + ecyrptedTemplate
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
}

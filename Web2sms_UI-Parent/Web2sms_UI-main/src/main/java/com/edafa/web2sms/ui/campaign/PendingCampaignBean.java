package com.edafa.web2sms.ui.campaign;

import static com.edafa.web2sms.utils.StringUtils.concatenate;
import static com.edafa.web2sms.utils.StringUtils.logTrxId;
import static com.edafa.web2sms.utils.StringUtils.userLogInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.service.campaign.model.CampaignResultSet;
import com.edafa.web2sms.service.campaign.CampaignManagementService;
import com.edafa.web2sms.service.campaign.CampaignManagementService_Service;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.Campaign;
import com.edafa.web2sms.service.model.CountResult;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.model.enums.CampaignAction;
import com.edafa.web2sms.service.model.enums.CampaignStatus;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

@ManagedBean(name = "pendingCampaignBean")
@ViewScoped
public class PendingCampaignBean {

    CampaignManagementService_Service campaignManagementService;
    CampaignManagementService campaignServicePort;
    private List<Campaign> pendingCampaignList = new ArrayList<>();
    private Campaign selectedCampaign;
    private CampaignAction selectedAction;
    private final CampaignAction approveAction = CampaignAction.APPROVE;
    private final CampaignAction rejectAction = CampaignAction.REJECT;
    private int totalRows;
    private int firstRow;
    private int rowsPerPage;
    private int pageRange;
    private Integer[] pages;
    private int currentPage;
    private int totalPages;
    private boolean tableFlag = true;
    private String redirectOutcome;
    private Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
    private Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
    private final FacesContext facesContext = FacesContext.getCurrentInstance();
    private final HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
    static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
    private final UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);

    @EJB
    private WSClients portObj;

    @EJB
    private AppErrorManagerAdapter appErrorManagerAdapter;

    private User user = new User();

    // Constructor---------------------------------------------------
    /**
     * bean constructor
     */
    public PendingCampaignBean() {
        try {
            user.setAccountId(userAcc.getAccount().getAccountId());
            user.setUsername(userAcc.getUsername());
        } catch (Exception ex) {
            pendingCampaignList = null;
            String logMsg = concatenate("Error while setting accout id and name ");
            userLogger.error(concatenate(logMsg, ex.getMessage()));
            appLogger.error(logMsg, ex);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }
    }

    // Action------------------------------------------------------
    @PostConstruct
    public void populateServices() {
        try {
            campaignServicePort = portObj.getCampaignServicePort();
            rowsPerPage = 4; // Default rows per page (max amount of rows to be displayed at once).
            pageRange = 9;

            populateList();

        } catch (Exception e) {
            String logMsg = "Error while setting services port ";
            userLogger.error(logMsg, e.getMessage());
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }
    }

    public void populateList() {
        UserTrxInfo userInfo = new UserTrxInfo();
        String userLogInfo = userLogInfo(user.getAccountId(), user.getUsername());
        String trxId = TrxId.getTrxId();
        try {
            ThreadContext.push(trxId);
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);
//            String logTrxId = logTrxId(userInfo.getTrxId());

            List<CampaignStatus> needsToBeApprovedStatus = new ArrayList<>();
            needsToBeApprovedStatus.add(CampaignStatus.WAITING_APPROVAL);
            userLogger.debug("Calling searchCampaigns with status waiting approval from=", firstRow, " to=", rowsPerPage);
            CampaignResultSet result = campaignServicePort.searchCampaigns(userInfo, null, firstRow, rowsPerPage, needsToBeApprovedStatus);
            userLogger.info("Called searchCampaigns with status waiting approval from=", firstRow, " to=", rowsPerPage
                    + ",response status= " + result.getStatus());

            switch (result.getStatus()) {
                case SUCCESS:
                    if (pendingCampaignList != null) {
                        pendingCampaignList.clear();
                    } else {
                        pendingCampaignList = new ArrayList<Campaign>();
                    }
                    List<Campaign> campList = result.getCampaign();
                    for (Campaign campaign : campList) {
                        if (campaign.getScheduleEndTimestamp() != null) {
                            campaign.setScheduleEndTimestamp(campaign.getScheduleEndTimestamp().substring(0, 11));
                        }
                    }

                    pendingCampaignList.addAll(campList);

                    userLogger.info("Campaign pending list for is set successfully, rows_count="+ pendingCampaignList.size());
                    break;
                case INVALID_REQUEST:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_pending_campaigns_error")), null));
                    pendingCampaignList = null;
                    userLogger.warn( "status to get pending campaign lists");
                    break;
                case FAIL:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_pending_campaigns_error")), null));

                    pendingCampaignList = null;
                    userLogger.warn(" status while loading pending campaigns");
                    break;
                case INELIGIBLE_ACCOUNT:
                    pendingCampaignList = null;
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("ineligible_user")), null));
                    userLogger.warn( "Can not view his pending campaigns list, status=", result.getStatus());
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_pending_campaigns_error")), null));
                    userLogger.warn("Unknown status=", result.getStatus(), " ,while loading pending campaigns");
                    break;
            }
            userLogger.debug( "Calling countSearchCampaigns with status waiting approval");
            CountResult countResult = campaignServicePort.countSearchCampaigns(userInfo, null, needsToBeApprovedStatus);
            userLogger.info( "Called countSearchCampaigns with status waiting approval,response status= " 
                    + result.getStatus());
            ResponseStatus countResultStatus = countResult.getStatus();
            switch (countResultStatus) {
                case SUCCESS:
                    totalRows = countResult.getCount();
                    userLogger.info( "Total found number of rows of pending campaigns is rows_count=", totalRows);

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

                    break;
                default:
                    pendingCampaignList = null;
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("get_pending_campaigns_error")), null));
                    userLogger.warn("Unknown status=", countResult.getStatus(), " ,while loading pending campaigns count");
                    break;
            }
            if (pendingCampaignList == null || totalRows == 0 || pendingCampaignList.isEmpty()) {
                tableFlag = false;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("get_pending_campaigns_error")), null));

            pendingCampaignList = null;
            tableFlag = false;
            String logMsg =  "Error while loading pending campaigns ";
            userLogger.error(logMsg, e.getMessage());
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }

    }

    public void changeCampaignAction() {
        UserTrxInfo userInfo = new UserTrxInfo();
        String userLogInfo = userLogInfo(user.getAccountId(), user.getUsername());
        String trxId = TrxId.getTrxId();
        try {
        	ThreadContext.push(trxId);
            redirectOutcome = "";
            userInfo.setTrxId(trxId);
            userInfo.setUser(user);

            userLogger.debug("calling status change on campaign "+ selectedCampaign.getCampaignId()+ " to "+ selectedAction.toString());
            ResultStatus status = campaignServicePort.updateCampaignStatus(userInfo, selectedCampaign.getCampaignId(), selectedAction);

            switch (status.getStatus()) {
                case SUCCESS:
                    userLogger.info("Changed status of campaign to "+ selectedAction.toString());
                    redirectOutcome = (totalRows <= 1) ? "Campaigns.xhtml" : "Campaigns.xhtml?status=pending";
                    break;

                case FAIL:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_change_campaign_status")), null));
                    userLogger.warn("Error while changing campaign status");
                    break;

                case INELIGIBLE_ACCOUNT:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("ineligible_user")), null));
                    userLogger.warn( "Ineligible to change pending campaign status");
                    break;

                case INVALID_REQUEST:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_change_campaign_status")), null));

                    userLogger.warn("Invalid request to change pending campaign status");
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_change_campaign_status")), null));
                    userLogger.warn("Unknown response=", status.getStatus(), " while changing campaign status");
                    break;
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("error_change_campaign_status")), null));

            String logMsg =  "Error while changing the campaign status ";
            userLogger.error(logMsg, e.getMessage());
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
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
        populateList(); // Load requested page.
    }

    public void page(ActionEvent event) {
        page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
    }

    // -----------------------------------------------------------------------------
    /**
     * Converts XMLGregorianCalendar to java.util.Date in Java
     */
    public static Date toDate(XMLGregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }
        return calendar.toGregorianCalendar().getTime();
    }

    public String fromDateToString(Date date) {
        try {
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

    // -----------------------------------------------------------------------------
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public List<Campaign> getPendingCampaignList() {
        return pendingCampaignList;
    }

    public void setPendingCampaignList(List<Campaign> pendingCampaignList) {
        this.pendingCampaignList = pendingCampaignList;
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

    public void setPages(Integer[] pages) {
        this.pages = pages;
    }

    public int getCurrentPage() {
        return currentPage;
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

    public Campaign getSelectedCampaign() {
        return selectedCampaign;
    }

    public void setSelectedCampaign(Campaign selectedCampaign) {
        this.selectedCampaign = selectedCampaign;
    }

    public CampaignAction getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(CampaignAction selectedAction) {
        this.selectedAction = selectedAction;
    }

    public CampaignAction getApproveAction() {
        return approveAction;
    }

    public CampaignAction getRejectAction() {
        return rejectAction;
    }

    public String getRedirectOutcome() {
        return redirectOutcome;
    }

    public void setRedirectOutcome(String redirectOutcome) {
        this.redirectOutcome = redirectOutcome;
    }

}

package com.edafa.web2sms.ui.locale;

import static com.edafa.web2sms.utils.StringUtils.concatenate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.encyrptionutil.Interfaces.EncyrptionUtilInterface;
import com.edafa.web2sms.service.acc_manag.account.AccountManegementService;
import com.edafa.web2sms.service.acc_manag.account.model.QuotaInquiryResult;
import com.edafa.web2sms.service.acc_manag.enums.ResponseStatus;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.model.CountResult;
import com.edafa.web2sms.service.model.QuotaInfo;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.model.enums.Action;
import com.edafa.web2sms.service.model.enums.CampaignStatus;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;


@ManagedBean(name = "mainBean")
@ViewScoped
public class MainBean {

    double totalSms = 0;
    double consumedQuota;
    int reservedQuota = 0;

    int reservedAndConsumed = 0;
    int consumedRatio;
    int remainingQuota;
    int consumedRatioWidth;

    private boolean showUsersTab = false;
    private boolean showGroupsTab = false;
    private boolean canShowPendingNotification = false;
    private boolean canViewPendingCampaigns=false;
    private boolean canApproveCampaigns=false;
    private boolean canRejectCampaigns=false;
    private int waitingApprovalCount = 0;
    private String tabsClass = "tab";
    private int tabsCount = 4;
    private boolean canCreateCampaign = false;
    private boolean canEditCampaign = false;

    String redirectUrl;
    boolean directLogin =(boolean) Configs.ENABLE_DIRECT_LOGIN.getValue();
    @EJB
    WSClients wsClients;

    @EJB
    AppErrorManagerAdapter appErrorManagerAdapter;

    AccountManegementService accountServicePort;

    @EJB
    EncyrptionUtilInterface encyrptionUtil;
    static final String USER_ACTIONS = (String) Configs.USER_ACTIONS.getValue();
    static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
    FacesContext facesContext;
    HttpServletRequest request;

    UserAccount userAcc;
    Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
    Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
    User user = new User();
    AccManagUser acctManagUser = new AccManagUser();
    // Constructor--------------------------------------------------------------------------

//    @PostConstruct
//    void init() {
//        AccountUserTrxInfo userInfo = new AccountUserTrxInfo();
//        try {
//            initLangUserAndActions();
//
//            userInfo.setTrxId(TrxId.getTrxId());
//
//            userInfo.setUser(acctManagUser);
//
//            accountServicePort = wsClients.getAccountServicePort();
//            QuotaInfo quotaInfo;
//
//            userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "Getting quota info" + " with msisdn=" + userAcc.getAccount().getBillingMsisdn());
//
//            QuotaInquiryResult result = accountServicePort.getQuotaInfoByMSISDN(userInfo, userAcc.getAccount().getBillingMsisdn());
//            ResponseStatus status = result.getStatus();
//            switch (status) {
//                case SUCCESS:
//                    quotaInfo = result.getQuotaInfo();
//                    totalSms = quotaInfo.getGrantedSMS();
//                    consumedQuota = quotaInfo.getConsumedSMS();
//                    reservedQuota = quotaInfo.getReservedSMS();
//                    reservedAndConsumed = (int) consumedQuota + reservedQuota;
//                    consumedRatio = (int) ((consumedQuota / totalSms) * 100);
//                    consumedRatioWidth = (consumedRatio * 140) / 100;
//                    remainingQuota = ((int) totalSms) - ((int) consumedQuota);
//                    userLogger.debug(logTrxId(userInfo.getTrxId()) + "totalSms: " + totalSms + " consumedQuota: " + consumedQuota + " reservedQuota: " + reservedQuota + " reservedAndConsumed: "
//                            + reservedAndConsumed + " consumedRatio: " + consumedRatio + " comsumedRationWidth: " + consumedRatioWidth + " remainingQuota: " + remainingQuota);
//                    userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "total sms and consumed quota and ratio is populated successfully");
//                    break;
//
//                default:
//                    totalSms = 0;
//                    consumedQuota = 0;
//                    consumedRatio = 0;
//                    remainingQuota = 0;
//                    reservedQuota = 0;
//                    reservedAndConsumed = 0;
//                    consumedRatioWidth = 0;
//                    userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "undifined response status=" + status + " while retreiving quota");
//                    break;
//            }
//        } catch (Exception e) {
//            totalSms = 0;
//            consumedQuota = 0;
//            consumedRatio = 0;
//            consumedRatioWidth = 0;
//            remainingQuota = 0;
//            userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "Error while populating the main page with quota info and msisdn="
//                    + userAcc.getAccount().getBillingMsisdn(), e);
//        }
//    }

    private void initLangUserAndActions(){
        String lan = "";
        try {
            facesContext = FacesContext.getCurrentInstance();
            request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
            userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
            
            user.setAccountId(userAcc.getAccount().getAccountId());
            user.setUsername(userAcc.getUsername());
            
            acctManagUser.setAccountId(userAcc.getAccount().getAccountId());
            acctManagUser.setUsername(userAcc.getUsername());
            
            List<Action> usersActions = (List<Action>) request.getSession().getAttribute(USER_ACTIONS);

            for (int i = 0; i < usersActions.size(); i++) {

                if (usersActions.get(i).equals(Action.VIEW_GROUPS)) {
                    userLogger.debug(concatenate("User can view groups"));

                    showGroupsTab = true;
                    tabsCount++;
                } else if (usersActions.get(i).equals(Action.VIEW_USERS)) {
                    userLogger.debug(concatenate("User can view users"));
                    showUsersTab = true;
                    tabsCount++;
                } else if (usersActions.get(i).equals(Action.APPROVE_CAMPAIGN)) {
                    userLogger.debug(concatenate("User can approve campaigns"));
                    canApproveCampaigns = true;
                } else if (usersActions.get(i).equals(Action.REJECT_CAMPAIGN)) {
                    userLogger.debug(concatenate("User can reject campaigns"));
                    canRejectCampaigns = true;
                } else if (usersActions.get(i).equals(Action.VIEW_PENDING_CAMPAIGNS)) {
                    userLogger.debug(concatenate("User can view pending campaigns"));
                    canViewPendingCampaigns = true;
                } else if (usersActions.get(i).equals(Action.CREATE_CAMPAIGN)){
                     userLogger.debug(concatenate("User can create campaigns"));
                     canCreateCampaign = true;
                }else if (usersActions.get(i).equals(Action.EDIT_CAMPAIGN)){
                     userLogger.debug(concatenate("User can edit campaigns"));
                 canEditCampaign = true;
            }
            }
            if(canViewPendingCampaigns&&(canApproveCampaigns||canRejectCampaigns)){
                    userLogger.debug(concatenate("User can see pending campaigns notification"));
                    canShowPendingNotification = true;
            }
            if (tabsCount == 6) {
                tabsClass = "tab-of-6";
            } else if (tabsCount == 5) {
                tabsClass = "tab-of-5";
            }
        } catch (Exception e) {
            userLogger.error("Error while initializing main bean"+e.getMessage());
            appLogger.error("Error while initializing main bean", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
        }
    }

    @PostConstruct
    void init() {
        AccountUserTrxInfo userInfo = new AccountUserTrxInfo();
        try {
        	if(directLogin)
        		redirectUrl = (String) Configs.DIRECT_LOGIN_REDIRECT_URL.getValue();
        	else
                redirectUrl = (String) Configs.LOGIN_REDIRECT_URL.getValue();

            initLangUserAndActions();
//            user.setAccountId(userAcc.getAccount().getAccountId());
//            user.setUsername(userAcc.getUsername());
        	String trxId = TrxId.getTrxId();
            userInfo.setTrxId(trxId);
            ThreadContext.push(trxId);
//            acctManagUser.setAccountId(userAcc.getAccount().getAccountId());
//            acctManagUser.setUsername(userAcc.getUsername());

            userInfo.setUser(acctManagUser);

            accountServicePort = wsClients.getAccountServicePort();
            QuotaInfo quotaInfo;

            userLogger.info( "Getting quota info with msisdn=" + encyrptionUtil.encrypt(userAcc.getAccount().getBillingMsisdn()));

            QuotaInquiryResult result = accountServicePort.getQuotaInfoByMSISDN(userInfo, userAcc.getAccount().getBillingMsisdn());
            ResponseStatus status = result.getStatus();
            switch (status) {
                case SUCCESS:
                    quotaInfo = result.getQuotaInfo();
                    totalSms = quotaInfo.getGrantedSMS();
                    consumedQuota = quotaInfo.getConsumedSMS();
                    reservedQuota = quotaInfo.getReservedSMS();
                    reservedAndConsumed = (int) consumedQuota + reservedQuota;
                    consumedRatio = (int) ((consumedQuota / totalSms) * 100);
                    consumedRatioWidth = (consumedRatio * 140) / 100;
                    remainingQuota = ((int) totalSms) - ((int) consumedQuota);
                    userLogger.debug( "totalSms: " + totalSms + " consumedQuota: " + consumedQuota + " reservedQuota: " + reservedQuota + " reservedAndConsumed: "
                            + reservedAndConsumed + " consumedRatio: " + consumedRatio + " comsumedRationWidth: " + consumedRatioWidth + " remainingQuota: " + remainingQuota);
                    userLogger.info( "total sms and consumed quota and ratio is populated successfully");
                    break;

                default:
                    totalSms = 0;
                    consumedQuota = 0;
                    consumedRatio = 0;
                    remainingQuota = 0;
                    reservedQuota = 0;
                    reservedAndConsumed = 0;
                    consumedRatioWidth = 0;
                    userLogger.error( "undifined response status=" + status + " while retreiving quota");
                    break;
            }
        } catch (Exception e) {
            totalSms = 0;
            consumedQuota = 0;
            consumedRatio = 0;
            consumedRatioWidth = 0;
            remainingQuota = 0;
            userLogger.error( "Error while populating the main page with quota info and msisdn="
                    + encyrptionUtil.encrypt(userAcc.getAccount().getBillingMsisdn()), e);
        }finally {
        	ThreadContext.pop();
        }
    }

    public void refreshWaitingApprovalCount() {

        if (canShowPendingNotification) {
            UserTrxInfo userInfo = new UserTrxInfo();
            try {
                userInfo.setTrxId(TrxId.getTrxId());
                userInfo.setUser(user);

                List<CampaignStatus> needsToBeApprovedStatus = new ArrayList<>();
                needsToBeApprovedStatus.add(CampaignStatus.WAITING_APPROVAL);
                userLogger.debug(concatenate( "Calling countSearchCampaigns with status waiting approval"));
                
                CountResult needsApprovalCount = wsClients.getCampaignServicePort().countSearchCampaigns(userInfo, null, needsToBeApprovedStatus);
                com.edafa.web2sms.service.enums.ResponseStatus resultStatus = needsApprovalCount.getStatus();
                userLogger.info(concatenate(  "Called countSearchCampaigns with status waiting approval, result status ", resultStatus));
                
                switch (resultStatus) {
                    case SUCCESS:
                        waitingApprovalCount = needsApprovalCount.getCount();
                        userLogger.debug(concatenate(  "Waiting approval campaigns count " + waitingApprovalCount));
                        break;
                    default:
                        waitingApprovalCount = 0;
                        userLogger.debug(  "Waiting approval campaigns count is set to 0 due to service failure");
                }
                
            } catch (WebServiceException ex) {
                waitingApprovalCount = 0;
                String logMsg=concatenate( "Error while countig campaigns waiting for approval, count set to 0");
                userLogger.error(concatenate(logMsg, ex.getMessage()));
                appLogger.error(logMsg,ex);
                appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.FAILED_OPERATION, "WebService failure");
            } catch (Exception ex) {
                waitingApprovalCount = 0;
                String logMsg=concatenate("Error while countig campaigns waiting for approval, count set to 0");
                userLogger.error(concatenate(logMsg, ex.getMessage()));
                appLogger.error(logMsg,ex);
                appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
            }
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

    // Setters and
    // getters---------------------------------------------------------------------------
    public int getTotalSms() {
        return (int) totalSms;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getConsumedRatioWidth() {
        return consumedRatioWidth;
    }

    public void setConsumedRatioWidth(int consumedRatioWidth) {
        this.consumedRatioWidth = consumedRatioWidth;
    }

    public void setTotalSms(double totalSms) {
        this.totalSms = totalSms;
    }

    public int getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(int remainingQuota) {
        this.remainingQuota = remainingQuota;
    }

    public UserAccount getUserAcc() {
        return userAcc;
    }

    public void setUserAcc(UserAccount userAcc) {
        this.userAcc = userAcc;
    }

    public int getConsumedQuota() {
        return (int) consumedQuota;
    }

    public void setConsumedQuota(double consumedQuota) {
        this.consumedQuota = consumedQuota;
    }

    public int getConsumedRatio() {
        return consumedRatio;
    }

    public void setConsumedRatio(int consumedRatio) {
        this.consumedRatio = consumedRatio;
    }
    
    public int getReservedQuota() {
        return reservedQuota;
    }

    public void setReservedQuota(int reservedQuota) {
        this.reservedQuota = reservedQuota;
    }

    public int getReservedAndConsumed() {
        return reservedAndConsumed;
    }

    public void setReservedAndConsumed(int reservedAndConsumed) {
        this.reservedAndConsumed = reservedAndConsumed;
    }

    public boolean isShowUsersTab() {
        return showUsersTab;
    }

    public void setShowUsersTab(boolean showUsersTab) {
        this.showUsersTab = showUsersTab;
    }

    public boolean isShowGroupsTab() {
        return showGroupsTab;
    }

    public void setShowGroupsTab(boolean showGroupsTab) {
        this.showGroupsTab = showGroupsTab;
    }

    public String getTabsClass() {
        return tabsClass;
    }

    public void setTabsClass(String tabsClass) {
        this.tabsClass = tabsClass;
    }

    public boolean isCanShowPendingNotification() {
        return canShowPendingNotification;
    }

    public void setCanShowPendingNotification(boolean canShowPendingNotification) {
        this.canShowPendingNotification = canShowPendingNotification;
    }

    public boolean isCanViewPendingCampaigns() {
        return canViewPendingCampaigns;
    }

    public void setCanViewPendingCampaigns(boolean canViewPendingCampaigns) {
        this.canViewPendingCampaigns = canViewPendingCampaigns;
    }

    public boolean isCanApproveCampaigns() {
        return canApproveCampaigns;
    }

    public void setCanApproveCampaigns(boolean canApproveCampaigns) {
        this.canApproveCampaigns = canApproveCampaigns;
    }

    public boolean isCanRejectCampaigns() {
        return canRejectCampaigns;
    }

    public void setCanRejectCampaigns(boolean canRejectCampaigns) {
        this.canRejectCampaigns = canRejectCampaigns;
    }

    public int getWaitingApprovalCount() {
        refreshWaitingApprovalCount();
        return waitingApprovalCount;
    }

    public void setWaitingApprovalCount(int waitingApprovalCount) {
        this.waitingApprovalCount = waitingApprovalCount;
    }

	public boolean isCanCreateCampaign() {
		return canCreateCampaign;
	}

	public void setCanCreateCampaign(boolean canCreateCampaign) {
		this.canCreateCampaign = canCreateCampaign;
	}

	public boolean isCanEditCampaign() {
		return canEditCampaign;
	}

	public void setCanEditCampaign(boolean canEditCampaign) {
		this.canEditCampaign = canEditCampaign;
	}

    
}

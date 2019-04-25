/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.edafa.web2sms.ui.login;

import com.edafa.web2sms.service.acc_manag.account.model.AccountResultFullInfo;
import com.edafa.web2sms.service.acc_manag.account.user.UserLoginService;
import com.edafa.web2sms.service.acc_manag.account.user.UserManegementService;
import com.edafa.web2sms.service.acc_manag.enums.ResponseStatus;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountFullInfo;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.acc_manag.model.ResultStatus;
import com.edafa.web2sms.ui.filters.IndirectLoginFilter;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;

import static com.edafa.web2sms.utils.StringUtils.concatenate;
import static com.edafa.web2sms.utils.StringUtils.logTrxId;
import static com.edafa.web2sms.utils.StringUtils.userLogInfo;

import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 *
 * @author israa-edafa
 */
@ViewScoped
@ManagedBean(name = "loginBean")
public class LoginBean {

    private String userName;
    private String password;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
    private String splitUserName;
    private String companyName;
    private int step;
    private boolean showForgetPassword;
    private boolean showResendCode;
    private boolean goToCampaigns;
    private boolean requestOldPassword;
    private boolean isTempPassword;
    private boolean logOutUser;
    private boolean passwordChangedSuccess;
    private final Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
    private final Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
    @EJB
    private WSClients portObj;
    @EJB
    private AppErrorManagerAdapter appErrorManagerAdapter;
    private UserLoginService userLoginService;
    private UserManegementService userManegementService;
    private FacesContext facesContext;
    private HttpServletRequest request;
    private HttpSession session;
    private final AccManagUser acctManagUser = new AccManagUser();
    private final AccountUserTrxInfo userInfo = new AccountUserTrxInfo();
    private UserAccount userAccount = new UserAccount();
    static final String USER_ACTIONS = (String) Configs.USER_ACTIONS.getValue();
    static final String LANGUAGE = (String) Configs.LANGUAGE_ATTRIBUTE.getValue();
//    static final String REQUEST_TOKEN = (String) Configs.TOKEN_KEY.getValue();
    static final String ACTIVE_PASSWORD = (String) Configs.ACTIVE_PASSWORD.getValue();
    static final String OTP = (String) Configs.OTP_KEY.getValue();
    static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
    static final String LOGIN_ID = (String) Configs.LOGIN_ID.getValue();
    public boolean directLoginFlag ;
    private String userLogInfo;
    private String language = "en";
	static final String isDirectLogin = "isLoginDirect";

    @PostConstruct
    public void init() {
        facesContext = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        session = request.getSession();
        directLoginFlag = (boolean) Configs.ENABLE_DIRECT_LOGIN.getValue();
        userAccount = (UserAccount) session.getAttribute(USER_KEY);
        language = (String) request.getSession().getAttribute(LANGUAGE);

        userLogger.debug("session has language " + language);

        if (userAccount != null) {
            acctManagUser.setAccountId(userAccount.getAccount().getAccountId());
            acctManagUser.setUsername(userAccount.getUsername());
            userLogInfo = userLogInfo(userAccount.getAccount().getAccountId(), userAccount.getUsername());
        } else {
            userAccount = new UserAccount();
        }
        userInfo.setUser(acctManagUser);

        if (request.getAttribute("change-password") != null) {//go to change password
            step = 3;
            oldPassword = (String) session.getAttribute(OTP);
            requestOldPassword = (oldPassword == null || oldPassword.isEmpty());
        } else {
            step = 1;
            showForgetPassword = false;
            showResendCode = false;
        }

    }

    public void checkUser() {
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);
//        String logTrxId = logTrxId(userInfo.getTrxId());
        try {
            ThreadContext.push(trxId);
            userLogger.info(concatenate("calling check user for login username= ", userName, ", and language = ", language));

            String[] userNameArr = userName.split("@");
            if (userNameArr.length < 2) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("error_login")), null));
                userLogger.warn(concatenate("Invalid user name format, username = ", userName));
                return;
            } else {
                splitUserName = userNameArr[0];
                companyName = userNameArr[1];
            }

            acctManagUser.setUsername(splitUserName);

            userLoginService = portObj.getUserLoginPort();
            ResultStatus userCheckStatus = userLoginService.checkUserForLogin(userInfo, companyName, language);
            ResponseStatus status = userCheckStatus.getStatus();
            userLogger.info(concatenate("userLoginServicePort checkUserForLogin response is:(", status, ")."));

            switch (status) {
                case TEMP_PASSWORD_REQUIRED:// temp password sent to user
                    userLogger.info(concatenate("sms sent to user's msisdn with temp password"));
                    step = 2;
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("temp_pass_req")), null));
                    showResendCode = true;
                    break;
                case USER_CONTACTED:// temp password sent to user
                    userLogger.info(concatenate("sms sent to user's msisdn with temp password"));
                    step = 2;
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("code_sent")), null));
                    showResendCode = true;
                    break;
                case CUSTOMER_CARE:// user has no msisdn or user is blocked
                    userLogger.info(concatenate("user has no saved msisdn , or is blocked, needs customer care help"));
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("contact_customer_care")), null));
                    break;
                case PASSWORD_REQUIRED:// user exist and password need for login
                    userLogger.info(concatenate("user exists and has password, proceed to login next step"));
                    step = 2;
                    showForgetPassword = true;
                    break;
                case FAILED_LOGIN:// user not exist at the system
                    userLogger.info(concatenate("user doesn't exist"));
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_login")), null));
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                    .getLocalizedLabel("error_login")), null));
            String logMsg = concatenate("Error while checking for user ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        } finally {
            ThreadContext.pop();
        }
    }

    public void login() {
        String trxId = TrxId.getTrxId();
        try {
            ThreadContext.push(trxId);
            if (password == null || password.isEmpty()) {
                userLogger.warn(concatenate("missing password field"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("mandatory_field")), null));
                return;
            }
            userInfo.setTrxId(trxId);
            String logTrxId = logTrxId(userInfo.getTrxId());

            isTempPassword = false;
            userLogger.info(concatenate(logTrxId , "calling login = ", userName, ", and language = ", language));

            AccountResultFullInfo loginResponse = userLoginService.userLogin(userInfo, companyName, password, language);
            ResponseStatus status = loginResponse.getStatus();
            userLogger.info(concatenate(logTrxId , "userLoginServicePort userLogin response is:(", status, ")."));

            switch (status) {
                case SUCCESS: // password correct
                    if (initUserSession(userInfo.getTrxId(), loginResponse.getAccount(), true)) {
                        goToCampaigns = true;
                    } else {
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("user_has_no_actions")), null));
                    }
                    break;
                case CUSTOMER_CARE: // user exceeded faild logins
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("contact_customer_care")), null));
                    break;
                case USER_CONTACTED: // temp expired or more than one login with temp
                    userLogger.info(concatenate(logTrxId,"sms re-sent to user's msisdn with temp password"));
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("code_sent")), null));
                    showResendCode = true;
                    showForgetPassword = false;
                    break;
                case CHANGE_PASSWORD: // new password is needed due to the last is temp, expired
                    isTempPassword = true;
                    if (initUserSession(userInfo.getTrxId(), loginResponse.getAccount(), false)) {
                        step = 3;
                        userLogger.info(concatenate(logTrxId,"user loged in using temp password successfully, must change password"));
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                                .getLocalizedLabel("change_pass_prompt")), null));
                        requestOldPassword = false;
                        showForgetPassword = false;
                        showResendCode = false;
                        oldPassword = password;
                    } else {
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("user_has_no_actions")), null));
                    }
                    break;
                case CHANGE_PASSWORD_OLD_REQUIRED: // new password is needed due to expired 
                    if (initUserSession(userInfo.getTrxId(), loginResponse.getAccount(), false)) {
                        step = 3;
                        userLogger.info(concatenate(logTrxId,"user loged in using his expired password, must change password"));
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                                .getLocalizedLabel("change_pass_prompt")), null));
                        requestOldPassword = true;
                        showForgetPassword = false;
                        showResendCode = false;
                    } else {
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("user_has_no_actions")), null));
                    }
                    break;
                case FAIL: // unexpected failure
                    userLogger.info(concatenate(logTrxId,"failed to login"));
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_login")), null));
                    break;
                case FAILED_LOGIN: // wrong password
                    userLogger.info(concatenate(logTrxId,"wrong password"));
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("wrong_password")), null));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_login")), null));
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                    .getLocalizedLabel("error_login")), null));
            String logMsg = concatenate("Error while logging in ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        } finally {
            ThreadContext.pop();
        }
    }

    public void changePassword(boolean fromLoginPage) {
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);

        passwordChangedSuccess = false;
        logOutUser = false;
//         String logTrxId = logTrxId(userInfo.getTrxId());
        try {
            if (oldPassword == null || newPassword == null || confirmPassword == null || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                userLogger.info(concatenate("missing change password mandatory field"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("mandatory_field")), null));
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                userLogger.info(concatenate("new password doesn't match confirm password"));
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("new_pass_confirm_mismatch")), null));
                return;
            }

            ThreadContext.push(trxId);

            userLogger.info(concatenate("calling change password"));

            userManegementService = portObj.getUserManagementPorts();
            ResultStatus changePassResult = userManegementService.changeUserPassword(userInfo, oldPassword, newPassword);
            ResponseStatus status = changePassResult.getStatus();
            userLogger.info(concatenate("userManegementService changeUserPassword response is:(", status, ")."));

            switch (status) {
                case SUCCESS: // password correct
                    if (fromLoginPage) {
                        goToCampaigns = true;
                        session.setAttribute(ACTIVE_PASSWORD, true);
                        session.removeAttribute(OTP);
                    } else {
                        passwordChangedSuccess = true;
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                                .getLocalizedLabel("password_change_success")), null));
                    }
                    break;
                case CUSTOMER_CARE: // user exceeded failed logins
                    session.invalidate();
                    if (fromLoginPage) {
                        step = 1;
                        showForgetPassword = false;
                        showResendCode = false;
                        requestOldPassword = false;
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("contact_customer_care")), null));
                    } else {
                        logOutUser = true;
                        FacesContext.getCurrentInstance().addMessage(
                                null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                .getLocalizedLabel("user_logged_out_contact_customer_care")), null));
                    }

                    break;
                case BAD_PASSWORD: // new password break the rules
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("bad_password")), null));
                    break;
                case SAME_OLD_PASSWORD: //  old password equals new one
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("same_old_password")), null));
                    break;
                case FAILED_LOGIN: // wrong old password so JUST ENTER IT AGAIN
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("wrong_old_password")), null));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("error_login")), null));
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                    .getLocalizedLabel("error_login")), null));
            String logMsg = concatenate("Error while changing user password ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        } finally {
            ThreadContext.pop();
        }
    }

    public void handleForgetPassword() {
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);
//        String logTrxId = logTrxId(userInfo.getTrxId());
        try {
            ThreadContext.push(trxId);
            userLogger.info(concatenate("calling forget password login username= ", userName, ", and language = ", language));

            ResultStatus forgetPassResult = userLoginService.userForgetPassword(userInfo, companyName, language);
            ResponseStatus status = forgetPassResult.getStatus();
            userLogger.info(concatenate("userLoginService userForgetPassword response is:(", status, ")."));

            switch (status) {
                case CUSTOMER_CARE:// user exceeded new temp requests
                    userLogger.info(concatenate("user exceeded allowed number of temp passwords"));
                    showForgetPassword = false;
                    showResendCode = true;
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("contact_customer_care")), null));

                    break;
                case USER_CONTACTED:// temp password sent to user
                    userLogger.info(concatenate("sms sent to user's msisdn with temp password"));
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("code_sent")), null));
                    showResendCode = true;
                    showForgetPassword = false;
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("general_error")), null));
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                    .getLocalizedLabel("general_error")), null));
            String logMsg = concatenate("Error while handling user forget password ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        } finally {
            ThreadContext.pop();
        }
    }

    public void resendCode() {
        String trxId = TrxId.getTrxId();
        userInfo.setTrxId(trxId);
//        String logTrxId = logTrxId(userInfo.getTrxId());
        try {
            ThreadContext.push(trxId);
            userLogger.info(concatenate("calling resend code"));

            ResultStatus resendPassResult = userLoginService.resendTempPassword(userInfo, companyName, language);
            ResponseStatus status = resendPassResult.getStatus();
            userLogger.info(concatenate("userLoginService resendTempPassword response is:(", status, ")."));

            switch (status) {
                case CUSTOMER_CARE:// user exceeded new temp requests
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("contact_customer_care")), null));

                    break;
                case USER_CONTACTED:// temp password sent to user
                    userLogger.info(concatenate("sms sent to user's msisdn with temp password"));
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("code_sent")), null));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("general_error")), null));
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                    .getLocalizedLabel("general_error")), null));
            String logMsg = concatenate("Error while calling resend temp password ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        } finally {
            ThreadContext.pop();
        }
    }

    public void cancelChangePassword() {
        oldPassword = "";
        newPassword = "";
        confirmPassword="";
        passwordChangedSuccess = false;
        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
        while (msgIterator.hasNext()) {
            msgIterator.next();
            msgIterator.remove();
        }

    }

    public String logOut() {
        userLogger.info("user logged out");
        session.invalidate();

        // TODO change to be configuration? 
        return "/login/login.xhtml?faces-redirect=true";
    }

    private boolean initUserSession(String trxId, AccountFullInfo account, boolean isActivePassword) {
//        String logTrxId = logTrxId(trxId);
//        userLogInfo = userLogInfo(account.getAccountId(), account.getLoginUser().getUsername());
        if (account.getLoginUser().getUserActions() != null
                && !account.getLoginUser().getUserActions().isEmpty()) {
            userInfo.getUser().setAccountId(account.getAccountId());

            userAccount.setAccount(account);
            List<String> sendersList = account.getSender();
            if (sendersList.size() > 1) {
                String sendersStrings = "";
                for (String s : sendersList) {
                    sendersStrings = sendersStrings + s + " , ";
                }
                userLogger.info(concatenate(" has multi senders=[", sendersStrings, "]"));
            } else {
                userLogger.info(concatenate(" has one sender_name=", account.getSender().get(0)));
            }

            userAccount.setUsername(account.getLoginUser().getUsername());

            session.setAttribute(USER_KEY, userAccount);
            session.setAttribute(isDirectLogin, false);
            session.setAttribute(USER_ACTIONS, account.getLoginUser().getUserActions());
            session.setAttribute(LOGIN_ID, trxId);
            if (isActivePassword) {
                session.setAttribute(ACTIVE_PASSWORD, true);
            } else {
                if (isTempPassword) {
                    session.setAttribute(OTP, password);
                }
            }
            StringBuilder userActionsStr = new StringBuilder("[ ");
            for (int i = 0; i < account.getLoginUser().getUserActions().size(); i++) {
                if (i == 0) {
                    userActionsStr.append(account.getLoginUser().getUserActions().get(i).toString());
                } else {
                    userActionsStr.append(", ").append(account.getLoginUser().getUserActions().get(i).toString());
                }
            }
            userActionsStr.append(" ].");
            userLogger.info(concatenate("Session is set for user and his list of actions : ", userActionsStr.toString()));
            return true;
        } else {
            userLogger.info(concatenate("User has no actions"));
            return false;
        }
    }

    //==================================
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isShowForgetPassword() {
        return showForgetPassword;
    }

    public void setShowForgetPassword(boolean showForgetPassword) {
        this.showForgetPassword = showForgetPassword;
    }

    public boolean isShowResendCode() {
        return showResendCode;
    }

    public void setShowResendCode(boolean showResendCode) {
        this.showResendCode = showResendCode;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public boolean isGoToCampaigns() {
        return goToCampaigns;
    }

    public void setGoToCampaigns(boolean goToCampaigns) {
        this.goToCampaigns = goToCampaigns;
    }

    public boolean isRequestOldPasswrod() {
        return requestOldPassword;
    }

    public void setRequestOldPasswrod(boolean requestOldPasswrod) {
        this.requestOldPassword = requestOldPasswrod;
    }

    public boolean isLogOutUser() {
        return logOutUser;
    }

    public void setLogOutUser(boolean logOutUser) {
        this.logOutUser = logOutUser;
    }

    public boolean isPasswordChangedSuccess() {
        return passwordChangedSuccess;
    }

    public void setPasswordChangedSuccess(boolean passwordChangedSuccess) {
        this.passwordChangedSuccess = passwordChangedSuccess;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

	public  boolean isDirectloginflag() {
		return directLoginFlag;
	}

	public boolean isDirectLoginFlag() {
		return directLoginFlag;
	}

	public void setDirectLoginFlag(boolean directLoginFlag) {
		this.directLoginFlag = directLoginFlag;
	}
    
    

}

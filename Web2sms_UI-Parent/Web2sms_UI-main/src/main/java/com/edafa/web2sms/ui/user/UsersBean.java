/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.edafa.web2sms.ui.user;

import static com.edafa.web2sms.utils.StringUtils.concatenate;
import static com.edafa.web2sms.utils.StringUtils.logTrxId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.ListDataModel;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.service.acc_manag.account.group.AccountGroupsResultSet;
import com.edafa.web2sms.service.acc_manag.account.group.GroupManagementService;
import com.edafa.web2sms.service.acc_manag.account.user.UserManegementService;
import com.edafa.web2sms.service.acc_manag.enums.ResponseStatus;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.acc_manag.model.CountResult;
import com.edafa.web2sms.service.acc_manag.model.GroupBasicInfo;
import com.edafa.web2sms.service.acc_manag.model.ResultStatus;
import com.edafa.web2sms.service.acc_manag.model.UserDetails;
import com.edafa.web2sms.service.acc_manag.user.AccountUsersResultSet;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.enums.Action;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

/**
 *
 * @author israa-edafa
 */
@ViewScoped
@ManagedBean(name = "usersBean")
public class UsersBean {

    private List<UserDetails> usersList;
    private ListDataModel<UserDetails> usersListData;
    private List<GroupBasicInfo> groupsLists = new ArrayList<>();
    private UserDetails displayedUser;
    private String userNameSearch = "";
    private String lastSearchText = "";
    private boolean showPagination;
    private boolean showUserForm;
    private int firstRow;
    private Integer[] pages;
    private int rowsPerPage;
    private int currentPage;
    private int totalRows;
    private int totalPages;
    private int pageRange;
    private boolean hasEditGroupUsersPriv;
    private boolean isEditSelfGroup;
    private boolean hasEditUserDataPriv;
    private boolean userUpdatedSuccessfully;
    private boolean canViewGroups;
    private boolean groupsContainSelfGroup = false;
    private int displayedUserIndex;
    private String userLogInfo;
    private UserManegementService usersManegementServicePort;
    private GroupManagementService groupsManagementServicePort;
    private final Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
    private final Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
    @EJB
    private WSClients portObj;
    @EJB
    private AppErrorManagerAdapter appErrorManagerAdapter;
    private final FacesContext facesContext = FacesContext.getCurrentInstance();
    private final HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
    static final String USER_ACTIONS = (String) Configs.USER_ACTIONS.getValue();
    static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
    private final UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
    private final List<Action> userActions = (List<Action>) request.getSession().getAttribute(USER_ACTIONS);
    private final User user = new User();
    private final AccManagUser acctManagUser = new AccManagUser();

    private final AccountUserTrxInfo userInfo = new AccountUserTrxInfo();

    private final Pattern EMAIL_PATTERN = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");

    @PostConstruct
    public void init() {

        rowsPerPage = 10;
        pageRange = 10;
        user.setAccountId(userAcc.getAccount().getAccountId());
        user.setUsername(userAcc.getUsername());

        acctManagUser.setAccountId(userAcc.getAccount().getAccountId());
        acctManagUser.setUsername(userAcc.getUsername());

        userInfo.setUser(acctManagUser);
//        userLogInfo = userLogInfo(user.getAccountId(), user.getUsername());

        try {
        	String trxId = TrxId.getTrxId() ;
        	ThreadContext.push(trxId);
            hasEditUserDataPriv = userActions.contains(Action.EDIT_USER_INFO);
            hasEditGroupUsersPriv = userActions.contains(Action.EDIT_GROUP_USERS);
            canViewGroups = userActions.contains(Action.VIEW_GROUPS);
            userLogger.debug("hasEditUserDataPriv: " + hasEditUserDataPriv);
            userLogger.debug("hasEditGroupUsersPriv: " + hasEditGroupUsersPriv);
            userLogger.debug("canViewGroups: " + canViewGroups);
            userInfo.setTrxId(trxId);
            populateList(userInfo);
            initPagination(userInfo);
        } catch (Exception e) {
            handleListLoadingError();
            String logMsg =  "Error while initializing users page. ";
            userLogger.error(logMsg, e.getMessage());
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
    }

    private void populateList(AccountUserTrxInfo userInfo) {
//        String logTrxId = logTrxId(userInfo.getTrxId());
        try {
            usersManegementServicePort = portObj.getUserManagementPorts();
            userLogger.debug(concatenate( "Getting paginated users from=", firstRow, " to=", rowsPerPage, " and user name= ", userNameSearch.trim()));
            AccountUsersResultSet result = usersManegementServicePort.getAccountUsers(userInfo, userNameSearch.trim(), firstRow, rowsPerPage);
            ResponseStatus state = result.getStatus();
            userLogger.info(concatenate( "usersManegementServicePort getAccountUsers response is:(", state, ")."));
            lastSearchText = userNameSearch;
            switch (state) {

                case SUCCESS:
                    usersList = result.getAccountUsers().getUser();
                    usersListData = new ListDataModel<>(usersList);
                    userLogger.info(concatenate( "list of users is populating successfully, rows_count=", usersList.size()));
                    break;
                case FAIL:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("loading_failed")), null));
                    userLogger.warn("Fail while getting list of users");
                    break;
                case INELIGIBLE_ACCOUNT:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("ineligible_view_users")), null));
                    userLogger.warn("Ineligible user, not allowed to display lists of users");
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("loading_failed")), null));
                    userLogger.warn(concatenate("Unknown response status=", result.getStatus(), " , while getting list of users"));
                    break;
            }

            if (usersList == null || usersList.isEmpty()) {
                if (usersList == null) {
                    usersList = new ArrayList<>();
                }
                usersListData = new ListDataModel<>(usersList);
                showPagination = false;
            } else {
                showPagination = true;
            }

        } catch (Exception e) {
            handleListLoadingError();
            String logMsg = concatenate( "Error while getting paginated lists of users from=", firstRow, " to=", rowsPerPage, ", userName search text = ", userNameSearch + ". ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        }
    }

    private void initPagination(AccountUserTrxInfo userInfo) {
        if (showPagination) {
//            String logTrxId = logTrxId(userInfo.getTrxId());
            try {
                CountResult countResult = usersManegementServicePort.countAccountUsers(userInfo, userNameSearch.trim());
                ResponseStatus state = countResult.getStatus();
                switch (state) {
                    case SUCCESS:
                        totalRows = countResult.getCount();
                        userLogger.info(concatenate( "count users with user name= ", userNameSearch.trim(), ", total users count =", totalRows));
                        break;
                    default:
                        userLogger.warn(concatenate("Error while getting users count, response status=", countResult.getStatus()));
                        handleListLoadingError();
                        return;
                }

                currentPage = (totalRows / rowsPerPage) - ((totalRows - firstRow) / rowsPerPage) + 1;
                // Set totalPages and pages.
                totalPages = (totalRows / rowsPerPage) + ((totalRows % rowsPerPage != 0) ? 1 : 0);
                int pagesLength = Math.min(pageRange, totalPages);
                pages = new Integer[pagesLength];

                // firstPage must be greater than 0 and lesser than totalPages-pageLength.
                int firstPage = Math.min(Math.max(0, currentPage - (pageRange / 2)), totalPages - pagesLength);

                // Create pages (page numbers for page links).
                for (int i = 0; i < pagesLength; i++) {
                    pages[i] = ++firstPage;
                    if (pagesLength > 10) {
                        pagesLength = 10;
                    }
                }
            } catch (Exception e) {
                handleListLoadingError();
                String logMsg = concatenate( "Error while getting count of users. ");
                userLogger.error(concatenate(logMsg, e.getMessage()));
                appLogger.error(logMsg, e);
                appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
            }
        }
    }

    public void searchForUser() {

        if (!lastSearchText.trim().equals(userNameSearch.trim())) {
            try {
            	String trxId = TrxId.getTrxId();
            	ThreadContext.push(trxId);
                userInfo.setTrxId(trxId);
                firstRow = 0;
                populateList(userInfo);
                initPagination(userInfo);
            } catch (Exception e) {
                String logMsg = concatenate( "Error while loading users. ");
                userLogger.error(concatenate(logMsg, e.getMessage()));
                appLogger.error(logMsg, e);
                appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
            }finally{
                ThreadContext.pop();
            }
        }
    }

    public void goToFirstPage() {
        goToPage(0);
    }

    public void goToLastPage() {
        goToPage(totalRows - ((totalRows % rowsPerPage != 0) ? totalRows % rowsPerPage : rowsPerPage));
    }

    private void goToPage(int firstRow) {
        this.firstRow = firstRow;
        try {
        	String trxId = TrxId.getTrxId();
        	 ThreadContext.push(trxId);
            userInfo.setTrxId(trxId);
            populateList(userInfo);
            currentPage = (totalRows / rowsPerPage) - ((totalRows - firstRow) / rowsPerPage) + 1;
        } catch (Exception e) {
            String logMsg = concatenate(logTrxId(userInfo.getTrxId()), userLogInfo, "Error while loading users page ", currentPage, ". ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.pop();
		}
    }

    public void goToPage(ActionEvent event) {
        goToPage(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
    }

    private void loadGroups() {
        try {
        	String trxId = TrxId.getTrxId();
            userInfo.setTrxId(trxId);
            ThreadContext.push(trxId);
//            String logTrxId = logTrxId(userInfo.getTrxId());
            groupsManagementServicePort = portObj.getGroupManagementPorts();
            userLogger.debug(concatenate( "Getting all groups"));
            AccountGroupsResultSet result = groupsManagementServicePort.getAccountGroups(userInfo, null, 0, 0);
            ResponseStatus state = result.getStatus();
            userLogger.info(concatenate( "groupsManagementServicePort getAccountGroups response is:(", state, ")."));

            switch (state) {
                case SUCCESS:
                    groupsLists = result.getAccountGroups().getGroup();
                    userLogger.info(concatenate("list of groups is retreived successfully, rows_count=", groupsLists.size()));
                    for (int i = 0; i < groupsLists.size(); i++) {
                        GroupBasicInfo group = groupsLists.get(i);
                        if (group.isDefault()) {
//                            defaultGroupId = group.getGroupId();
                            groupsLists.remove(i);
                            groupsLists.add(0, group);
                            break;
                        }
                    }
                    break;
                case FAIL:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("loading_failed")), null));
                    userLogger.warn(concatenate( "Fail while getting list of groups"));
                    break;
                case INELIGIBLE_ACCOUNT:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("loading_failed")), null));
                    userLogger.warn(concatenate( "Ineligible user, not allowed to display lists of groups"));
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("loading_failed")), null));
                    userLogger.warn(concatenate("Unknown response status=", result.getStatus(), " , while getting list of groups"));
                    break;
            }
            if (groupsLists == null) {
                groupsLists = new ArrayList<>();
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                    .getLocalizedLabel("loading_failed")), null));
            if (groupsLists == null) {
                groupsLists = new ArrayList<>();
            }
            String logMsg = concatenate( "Error while getting list of groups. ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
    }

    public void loadUser(int index) {
        groupsContainSelfGroup=false;
        if (canViewGroups) {
            groupsLists.clear();
            loadGroups();
            for (int i = 0; i < groupsLists.size(); i++) {
                GroupBasicInfo group = groupsLists.get(i);
                if (group.getGroupId().equals(usersList.get(index).getGroupId())) {
                    groupsContainSelfGroup=true;
                    break;
                }
            }
            userLogger.debug(concatenate( "groups retreived contains user's group: ",groupsContainSelfGroup));
        } 
        if(!canViewGroups || !groupsContainSelfGroup){
            userLogger.debug(concatenate("groups list will be cleared, user's group will be used instead"));
            groupsLists.clear();
            GroupBasicInfo usersGroup = new GroupBasicInfo();
            usersGroup.setGroupId(usersList.get(index).getGroupId());
            usersGroup.setGroupName(usersList.get(index).getGroupName());

            groupsLists.add(usersGroup);
        }

        if (groupsLists != null && !groupsLists.isEmpty()) {
            displayedUser = new UserDetails();
            displayedUser.setAccountId(usersList.get(index).getAccountId());
            displayedUser.setEmail(usersList.get(index).getEmail());
            displayedUser.setGroupId(usersList.get(index).getGroupId());
            displayedUser.setName(usersList.get(index).getName());
            displayedUser.setPhoneNumber(usersList.get(index).getPhoneNumber());
            displayedUser.setUserId(usersList.get(index).getUserId());
            displayedUser.setUsername(usersList.get(index).getUsername());
            isEditSelfGroup = user.getUsername().equals(displayedUser.getUsername());
            displayedUserIndex = index;
            userUpdatedSuccessfully = false;
            userLogger.debug(concatenate( "User ", displayedUser.getUserId() , " is selected for editing"));
            showUserForm = true;
        }
    }

    public void editUser() {
        try {
            if (displayedUser.getEmail() != null && !displayedUser.getEmail().isEmpty() && !isValidEmail(displayedUser.getEmail())) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                        .getLocalizedLabel("invalid_email")), null));
                userLogger.info(concatenate( "invalid email"));
                return;
            }
            String trxId = TrxId.getTrxId();
            ThreadContext.push(trxId);
            userInfo.setTrxId(trxId);
//            String logTrxId = logTrxId(userInfo.getTrxId());

            usersManegementServicePort = portObj.getUserManagementPorts();
            userLogger.debug(concatenate( "Editing user data"));
            ResultStatus result = usersManegementServicePort.updateUserData(userInfo, displayedUser);
            ResponseStatus state = result.getStatus();
            userLogger.info(concatenate("usersManegementServicePort updateUserData response is:(", state, ")."));

            switch (state) {
                case SUCCESS:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                            .getLocalizedLabel("user_updated_successfully")), null));
                    userLogger.info(concatenate( "user data updated successfully"));
                    populateList(userInfo);
                    userUpdatedSuccessfully = true;
                    break;
                case FAIL:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("user_update_fail")), null));
                    userLogger.warn(concatenate( "Fail while editing user data"));
                    break;
                case INELIGIBLE_ACCOUNT:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("ineligible_edit_users")), null));
                    userLogger.warn(concatenate( "Ineligible user, not allowed to edit user data"));
                    break;
                case INVALID_MSISDN:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("invalid_phone_number")), null));
                    userLogger.warn(concatenate( "Invalid msisdn"));
                    break;
                default:
                    FacesContext.getCurrentInstance().addMessage(
                            null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                            .getLocalizedLabel("user_update_fail")), null));
                    userLogger.warn(concatenate( "Unknown response status=", result.getStatus(), " , while editing user data"));
                    break;
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                    .getLocalizedLabel("user_update_fail")), null));
            String logMsg = concatenate("Error while editing user data. ");
            userLogger.error(concatenate(logMsg, e.getMessage()));
            appLogger.error(logMsg, e);
            appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
        }finally{
            ThreadContext.pop();
        }
    }

    public void cancelEdit() {
        userLogger.debug(concatenate( "Editing user ", displayedUser.getUserId() + " is canceled"));
        displayedUser = null;
        showUserForm = false;
        userUpdatedSuccessfully = false;
    }

    private void handleListLoadingError() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                .getLocalizedLabel("loading_failed")), null));
        if (usersList == null) {
            usersList = new ArrayList<>();
        }
        usersListData = new ListDataModel<>(usersList);
        showPagination = false;
    }

    private boolean isValidEmail(String email) {
        boolean validEmail = EMAIL_PATTERN.matcher(email).matches();
        return validEmail;
    }

    //==========================
    public List<UserDetails> getUsersList() {
        return usersList;
    }

    public void setUsersList(List<UserDetails> usersList) {
        this.usersList = usersList;
    }

    public String getUserNameSearch() {
        return userNameSearch;
    }

    public void setUserNameSearch(String userNameSearch) {
        this.userNameSearch = userNameSearch;
    }

    public boolean isShowPagination() {
        return showPagination;
    }

    public void setShowPagination(boolean showPagination) {
        this.showPagination = showPagination;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(int firstRow) {
        this.firstRow = firstRow;
    }

    public Integer[] getPages() {
        return pages;
    }

    public void setPages(Integer[] pages) {
        this.pages = pages;
    }

    public int getRowsPerPage() {
        return rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public UserDetails getDisplayedUser() {
        return displayedUser;
    }

    public void setDisplayedUser(UserDetails displayedUser) {
        this.displayedUser = displayedUser;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageRange() {
        return pageRange;
    }

    public void setPageRange(int pageRange) {
        this.pageRange = pageRange;
    }

    public boolean isShowUserForm() {
        return showUserForm;
    }

    public void setShowUserForm(boolean showUserForm) {
        this.showUserForm = showUserForm;
    }

    public ListDataModel<UserDetails> getUsersListData() {
        return usersListData;
    }

    public void setUsersListData(ListDataModel<UserDetails> usersListData) {
        this.usersListData = usersListData;
    }

    public boolean isHasManageGroupsPriv() {
        return hasEditGroupUsersPriv && canViewGroups;
    }

    public void setHasManageGroupsPriv(boolean hasManageGroupsPriv) {
        this.hasEditGroupUsersPriv = hasManageGroupsPriv;
    }

    public List<GroupBasicInfo> getGroupsLists() {
        return groupsLists;
    }

    public void setGroupsLists(List<GroupBasicInfo> groupsLists) {
        this.groupsLists = groupsLists;
    }

    public boolean isHasEditUserDataPriv() {
        return hasEditUserDataPriv;
    }

    public void setHasEditUserDataPriv(boolean hasEditUserDataPriv) {
        this.hasEditUserDataPriv = hasEditUserDataPriv;
    }

    public boolean isIsEditSelfGroup() {
        return isEditSelfGroup;
    }

    public void setIsEditSelfGroup(boolean isEditSelfGroup) {
        this.isEditSelfGroup = isEditSelfGroup;
    }

    public User getUser() {
        return user;
    }

    public boolean isUserUpdatedSuccessfully() {
        return userUpdatedSuccessfully;
    }

    public void setUserUpdatedSuccessfully(boolean userUpdatedSuccessfully) {
        this.userUpdatedSuccessfully = userUpdatedSuccessfully;
    }

    public boolean isCanViewGroups() {
        return canViewGroups;
    }

    public void setCanViewGroups(boolean canViewGroups) {
        this.canViewGroups = canViewGroups;
    }

    public boolean isGroupsContainSelfGroup() {
        return groupsContainSelfGroup;
    }

    public void setGroupsContainSelfGroup(boolean groupsContainSelfGroup) {
        this.groupsContainSelfGroup = groupsContainSelfGroup;
    }

}

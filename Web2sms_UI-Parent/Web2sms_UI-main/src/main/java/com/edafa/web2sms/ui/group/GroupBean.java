package com.edafa.web2sms.ui.group;

import static com.edafa.web2sms.utils.StringUtils.concatenate;
import static com.edafa.web2sms.utils.StringUtils.logTrxId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.service.acc_manag.account.AccountManegementService;
import com.edafa.web2sms.service.acc_manag.account.group.AccountGroupResultSet;
import com.edafa.web2sms.service.acc_manag.account.group.AccountGroupsResultSet;
import com.edafa.web2sms.service.acc_manag.account.group.GroupManagementService;
import com.edafa.web2sms.service.acc_manag.account.model.PrivilegesResult;
import com.edafa.web2sms.service.acc_manag.account.user.UserManegementService;
import com.edafa.web2sms.service.acc_manag.enums.ResponseStatus;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.acc_manag.model.CountResult;
import com.edafa.web2sms.service.acc_manag.model.Group;
import com.edafa.web2sms.service.acc_manag.model.Group.GroupPrivileges;
import com.edafa.web2sms.service.acc_manag.model.Group.GroupUsers;
import com.edafa.web2sms.service.acc_manag.model.GroupBasicInfo;
import com.edafa.web2sms.service.acc_manag.model.GroupUser;
import com.edafa.web2sms.service.acc_manag.model.Privilege;
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

@ViewScoped
@ManagedBean(name = "groupBean")
public class GroupBean {
	
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
	
	FacesContext facesContext = FacesContext.getCurrentInstance();
        static final String USER_ACTIONS = (String) Configs.USER_ACTIONS.getValue();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
	private final UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
	private final List<Action> userActions = (List<Action>) request.getSession().getAttribute(USER_ACTIONS);
	private final User user = new User();
	
	private int totalRows;
	private int firstRow;
	private int rowsPerPage;
	private int pageRange;
	private Integer[] pages;
	private int currentPage;
	private int totalPages;
	private int totalRowsNum;
	
	private String searchValue = "";
	private List<GroupBasicInfo> listofGroups = new ArrayList<GroupBasicInfo>();
	private final AccManagUser acctManagUser = new AccManagUser();
	private AccountUserTrxInfo acctManagUserInfo = new AccountUserTrxInfo();
	private String userLogInfo;
	private Map<String, Boolean> selectedUsers;
	private GroupBasicInfo groupBasic;
	private Group group;
	private String searchInGroup;
	private List<Privilege> allPrivileges;
	private Map<String, Boolean> selectedPrivillages;
	private String selectedGroup;
	private List<GroupUser> allDefaultGroupUsers;
	private boolean tableFlag = false;
	private boolean editFlag = true;
	private String groupName = "";
	private boolean editeDeleteFlag = false;
	private boolean adminsFlag = false;
	private String groupAdminId = "";
	private String editedGroupName = "";
	private boolean canCreateGroup = false;
	private boolean canDeleteGroup = false;
	private boolean canEditGroupPriv = false;
	private boolean canEditGroupUsers = false;
	private boolean canEditGroup = false;
	private boolean canMarkGroupsAdmin = false;
	private boolean canViewDefaultGroupUsers = false;
	private boolean isSelfGroupEdit = false;
	
	@EJB
	WSClients portObj;
	
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;
	
	GroupManagementService groupManagementService;
	
	AccountManegementService accountManagementService;
	
	UserManegementService userManagementService;
	
	@PostConstruct
	public void init() {
		rowsPerPage = 10;
		pageRange = 10;
		groupName = "";
		selectedUsers = new HashMap<String, Boolean>();
		selectedPrivillages = new HashMap<String, Boolean>();
		
		allDefaultGroupUsers = new ArrayList<GroupUser>();
		
		user.setAccountId(userAcc.getAccount().getAccountId());
		user.setUsername(userAcc.getUsername());
		
		acctManagUser.setAccountId(userAcc.getAccount().getAccountId());
		acctManagUser.setUsername(userAcc.getUsername());
		
		acctManagUserInfo.setUser(acctManagUser);
//		userLogInfo = userLogInfo(user.getAccountId(), user.getUsername());
		
		groupManagementService = portObj.getGroupManagementPorts();
		accountManagementService = portObj.getAccountServicePort();
		userManagementService = portObj.getUserManagementPorts();
		populateGroups();
		// getAllPrivilages();
		initGroupPagination();
		initActions();
		
	}
	
	private void initActions() {
		for (int i = 0; i < userActions.size(); i++) {
			Action action = userActions.get(i);
			if (action == Action.CREATE_GROUP) {
				canCreateGroup = true;
				userLogger.info("canCreateGroup");
			} else
				if (action == Action.DELETE_GROUP) {
					canDeleteGroup = true;
					userLogger.info( "canDeleteGroup");
				} else
					if (action == Action.EDIT_GROUP) {
						canEditGroup = true;
						userLogger.info( "canEditGroup");
					} else
						if (action == Action.EDIT_GROUP_PRIVILEGES) {
							canEditGroupPriv = true;
							userLogger.info("canEditGroupPriv");
						} else
							if (action == Action.EDIT_GROUP_USERS) {
								canEditGroupUsers = true;
								userLogger.info("canEditGroupUsers");
							} else
								if (action == Action.MARK_GROUP_ADMIN) {
									canMarkGroupsAdmin = true;
									userLogger.info("canMarkGroupsAdmin");
								} else
									if (action == Action.VIEW_DEFAULT_GROUP_USERS) {
										canViewDefaultGroupUsers = true;
										userLogger.info("canViewDefaultGroupUsers");
									}
		}
	}
	
	public void initGroupPagination() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
		try {
	        ThreadContext.push(trxId);
			if (tableFlag) {
				try {
					userLogger.info("calling groupManagementService countAccountGroups with parameters : searchValue:" + searchValue);
					CountResult countResult = groupManagementService.countAccountGroups(acctManagUserInfo, searchValue.trim());
					ResponseStatus state = countResult.getStatus();
					userLogger.info( "countAccountGroups response is :" + state);
					
					switch (state) {
						case SUCCESS:
							totalRows = countResult.getCount();
							userLogger.info( "count of groups with search param: "+ searchValue.trim()+ "is : "+ totalRows);
							break;
						default:
							userLogger.warn( "Error while getting groups' count, response status="+ countResult.getStatus());
							return;
					}
					
					currentPage = (totalRows / rowsPerPage) - ((totalRows - firstRow) / rowsPerPage) + 1;
					// Set totalPages and pages.
					totalPages = (totalRows / rowsPerPage) + ((totalRows % rowsPerPage != 0) ? 1 : 0);
					int pagesLength = Math.min(pageRange, totalPages);
					pages = new Integer[pagesLength];
					// firstPage must be greater than 0 and less than totalPages-pageLength.
					int firstPage = Math.min(Math.max(0, currentPage - (pageRange / 2)), totalPages - pagesLength);
					// Create pages (page numbers for page links).
					for (int i = 0; i < pagesLength; i++) {
						pages[i] = ++firstPage;
						if (pagesLength > 10) {
							pagesLength = 10;
						}
					}
				} catch (Exception e) {
//					String logMsg = concatenate(logTrxId, userLogInfo, "Error while getting count of users. ");
					userLogger.error("Error while getting count of users. ", e.getMessage());
					appLogger.error("Error while getting count of users. ", e);
					appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
				}
			}
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			// Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			// while (msgIterator.hasNext()) {
			// msgIterator.next();
			// msgIterator.remove();
			// }
			// FacesContext.getCurrentInstance().addMessage(null, new
			// FacesMessage(FacesMessage.SEVERITY_ERROR,
			// String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			//
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}
		
	}
	
	public void getAllDefaultUsers() {
		List<UserDetails> users = new ArrayList<UserDetails>();
		String trxId=TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
			ThreadContext.push(trxId);

			userLogger.info("calling userManagementService getDefaultGroupUsers with search:" + "" + ", firstRow: " + 0 + " , rowsPerPage: " + 0);
			
			AccountUsersResultSet result = userManagementService.getDefaultGroupUsers(acctManagUserInfo, "", 0, 0);
			
			ResponseStatus state = result.getStatus();
			
			userLogger.info( "getDefaultGroupUsers response is: (", state, ").");
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			switch (state) {
			
				case SUCCESS:
					users = result.getAccountUsers().getUser();
					userLogger.info( "returned " + users.size()+" users in default group");
					
					allDefaultGroupUsers = convertDefaultUsersToGroupUsers(users);
					for (GroupUser user : allDefaultGroupUsers) {
						selectedUsers.put(user.getUserId(), false);
						userLogger.trace( "converted user in default group : [ userId: "+ user.getUserId()+ ", userName: "+ user.getUsername()+ " ].");
					}
					break;
				case FAIL:
					
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// String.format(Util.getLocalizedLabel("loading_failed")), null));
					userLogger.warn( "Fail to display default users");
					break;
				case INELIGIBLE_ACCOUNT:
					
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// String.format(Util.getLocalizedLabel("ineligible_user")), null));
					userLogger.warn("Ineligible user, not allowed to display default users");
					break;
				default:
					
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// String.format(Util.getLocalizedLabel("loading_failed")), null));
					userLogger.warn("Unknown response status="+result.getStatus()+ " , to display default users");
					break;
			}
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			// Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			// while (msgIterator.hasNext()) {
			// msgIterator.next();
			// msgIterator.remove();
			// }
			
			// FacesContext.getCurrentInstance().addMessage(null, new
			// FacesMessage(FacesMessage.SEVERITY_ERROR,
			// String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
	}
	
	public void getAllPrivilages() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		
		try {
	        ThreadContext.push(trxId);
			userLogger.info("calling accountManagementService getAllSystemPrivileges");
			
			PrivilegesResult result = accountManagementService.getAllSystemPrivileges(acctManagUserInfo);
			
			ResponseStatus state = result.getStatus();
			
			userLogger.info("getAllSystemPrivileges response is:("+ state+").");
			
			selectedPrivillages = new HashMap<String, Boolean>();
			// Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			// while (msgIterator.hasNext()) {
			// msgIterator.next();
			// msgIterator.remove();
			// }
			switch (state) {
				case SUCCESS:
					allPrivileges = result.getPrivilege();
					userLogger.info("returned list of privileges size is :", allPrivileges.size());
					for (Privilege privilege : allPrivileges) {
						selectedPrivillages.put(privilege.getPrivilegeId(), false);
						userLogger.trace("system privilage [privilage id: "+ privilege.getPrivilegeId()+ ", privilage name: "+ privilege.getPrivilegeName()+" ].");
					}
					break;
				case FAIL:
					
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// String.format(Util.getLocalizedLabel("loading_failed")), null));
					userLogger.warn("Fail while getting list of system privilages");
					break;
				case INELIGIBLE_ACCOUNT:
					
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// String.format(Util.getLocalizedLabel("ineligible_user")), null));
					userLogger.warn( "Ineligible user, not allowed to display list of system privilages");
					break;
				default:
					
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_ERROR,
					// String.format(Util.getLocalizedLabel("loading_failed")), null));
					userLogger.warn("Unknown response status=" +result.getStatus()+" , while getting list of list of system privilages");
					break;
			}
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			// Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			// while (msgIterator.hasNext()) {
			// msgIterator.next();
			// msgIterator.remove();
			// }
			//
			// FacesContext.getCurrentInstance().addMessage(null, new
			// FacesMessage(FacesMessage.SEVERITY_ERROR,
			// String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
	}
	
	public String createNewGroupListener() {
		editFlag = false;
		return "";
	}
	
	public String updateGroupListener() {
		editFlag = true;
		return "";
	}
	
	public void populateGroups() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
			ThreadContext.push(trxId);
			userLogger.info( "calling GroupsManegementServicePort getAccountGroups with parameters : searchValue:" + searchValue + ", firstRow: " + firstRow
					+ " , rowsPerPage: " + rowsPerPage);
			
			AccountGroupsResultSet result = groupManagementService.getAccountGroups(acctManagUserInfo, searchValue.trim(), firstRow, rowsPerPage);
			
			ResponseStatus state = result.getStatus();
			
			userLogger.info("getAccountGroups response is:("+ state+").");
			// Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			// while (msgIterator.hasNext()) {
			// msgIterator.next();
			// msgIterator.remove();
			// }
			switch (state) {
			
				case SUCCESS:
					listofGroups = result.getAccountGroups().getGroup();
					if (listofGroups != null && !listofGroups.isEmpty()) {
						tableFlag = true;
					} else {
						tableFlag = false;
					}
					userLogger.info( "list of groups is populating successfully, with size="+listofGroups.size());
					// if (!tableFlag) {
					// FacesContext.getCurrentInstance().addMessage(null, new
					// FacesMessage(FacesMessage.SEVERITY_INFO,
					// String.format(Util.getLocalizedLabel("No_groups_found")), null));
					// }
					break;
				case FAIL:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
					userLogger.warn( "Fail while getting list of groups");
					break;
				case INELIGIBLE_ACCOUNT:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
					userLogger.warn("Ineligible user, not allowed to display list of groups");
					break;
				default:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
					userLogger.warn("Unknown response status="+ result.getStatus()+ " , while getting list of groups");
					break;
			}
			
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
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
	}
	
	public void searchForGroup() {
		firstRow = 0;
		populateGroups();
		initGroupPagination();
		
	}
	
	public void getSelectedGroup(GroupBasicInfo group) {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
	        ThreadContext.push(trxId);
			if (null == group) {
				editFlag = false;
				this.groupBasic = new GroupBasicInfo();
				this.group = new Group();
				getAllPrivilages();
				editedGroupName = "";
				groupName = "";
				adminsFlag = false;
				userLogger.trace("add new group panel is rendered");
			} else {
				editFlag = true;
				this.groupBasic = group;
				
				userLogger.info( "calling GroupsManegementServicePort getAccountGroupFullInfo with group [group name:"+ groupBasic.getGroupName()+ " group id: "+
						groupBasic.getGroupId()+ "].");
				
				AccountGroupResultSet result = groupManagementService.getAccountGroupFullInfo(acctManagUserInfo, groupBasic);
				ResponseStatus state = result.getStatus();
				userLogger.info( " getAccountGroupFullInfo response is:("+ state+ ").");
				
				// Iterator<FacesMessage> msgIterator =
				// FacesContext.getCurrentInstance().getMessages();
				// while (msgIterator.hasNext()) {
				// msgIterator.next();
				// msgIterator.remove();
				// }
				switch (state) {
				
					case SUCCESS:
						this.group = result.getGroup();
						editedGroupName = group.getGroupName();
						userLogger.debug(" returned group full info is: "+ grouptoString(this.group));
						getAllPrivilages();
						for (Privilege privilege : allPrivileges) {
							for (int j = 0; j < this.group.getGroupPrivileges().getPrivilege().size(); j++) {
								if (privilege.getPrivilegeId().equals(this.group.getGroupPrivileges().getPrivilege().get(j).getPrivilegeId())) {
									selectedPrivillages.put(privilege.getPrivilegeId(), true);
								}
							}
						}
						// getAllDefaultUsers();
						// for (GroupUser user : allDefaultGroupUsers) {
						// for (int j = 0; j < this.group.getGroupUsers().getUser().size(); j++) {
						//
						// if
						// (user.getUserId().equals(this.group.getGroupUsers().getUser().get(j).getUserId()))
						// {
						// selectedUsers.put(user.getUserId(), true);
						// }
						// }
						// }
						break;
					case FAIL:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
						userLogger.warn("Fail while getting group full info");
						break;
					case INELIGIBLE_ACCOUNT:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
						userLogger.warn("Ineligible user, not allowed to display group full info");
						break;
					default:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
						userLogger.warn( "Unknown response status="+ result.getStatus()+ " , while getting group full info");
						break;
				}
			}
			if (editFlag) {
				if (groupBasic.isDefault()) {
					editeDeleteFlag = false;
				} else {
					editeDeleteFlag = true;
				}
				if (groupBasic.getGroupName().equals(Configs.ADMINS_GROUP_NAME.getValue())) {
					adminsFlag = true;
				} else
					adminsFlag = false;
			} else {
				editeDeleteFlag = false;
			}
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
		
	}
	
	public void changeGroupName() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
	        ThreadContext.push(trxId);
		if (editedGroupName != null && !editedGroupName.isEmpty() && !editedGroupName.trim().equals("")) {
			
			try {
				userLogger.info( "calling GroupsManegementServicePort updateGroup to change group name from "+ group.getGroupName()+ "to :", editedGroupName);
				group.setGroupName(editedGroupName);
				groupBasic.setGroupName(editedGroupName);
				ResultStatus result = groupManagementService.updateGroup(acctManagUserInfo, group);
				
				ResponseStatus state = result.getStatus();
				userLogger.info("changing Group name response is:("+ state+ ").");
				// Iterator<FacesMessage> msgIterator =
				// FacesContext.getCurrentInstance().getMessages();
				// while (msgIterator.hasNext()) {
				// msgIterator.next();
				// msgIterator.remove();
				// }
				switch (state) {
				
					case SUCCESS:
						populateGroups();
						break;
					case FAIL:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_updated_fail")), null));
						userLogger.warn("Fail while changing group name");
						break;
					case INELIGIBLE_ACCOUNT:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
						userLogger.warn( "Ineligible user, not allowed to change group name");
						break;
					default:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_updated_fail")), null));
						userLogger.warn("Unknown response status="+ result.getStatus()+ " , while changing group name");
						break;
				}
				
				getSelectedGroup(this.groupBasic);
			} catch (Exception e) {
				getSelectedGroup(this.groupBasic);
//				String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
				userLogger.error("un excepected error", e.getMessage());
				Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
				while (msgIterator.hasNext()) {
					msgIterator.next();
					msgIterator.remove();
				}
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
				
				appLogger.error("un excepected error", e);
				appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			}
		} else {
                        editedGroupName=this.groupBasic.getGroupName();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_name_required")), null));
			userLogger.warn( "group name is empty");
			
			}
		} finally {
			ThreadContext.pop();
		}
	}
	
	public String changeGroupPriviliges() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
	        ThreadContext.push(trxId);
			List<Privilege> newGroupPrivileges = new ArrayList<Privilege>();
			for (Map.Entry<String, Boolean> e : selectedPrivillages.entrySet()) {
				String key = e.getKey();
				Boolean value = e.getValue();
				if (value.equals(true)) {
					for (int i = 0; i < allPrivileges.size(); i++) {
						if (allPrivileges.get(i).getPrivilegeId().equals(key)) {
							newGroupPrivileges.add(allPrivileges.get(i));
						}
					}
				}
			}
			
			String privilageStr = "privilages [ ";
			for (Privilege privilige : newGroupPrivileges) {
				privilageStr = " ( privilage id : " + privilige.getPrivilegeId() + ", privilage name : " + privilige.getPrivilegeId() + "),";
			}
			privilageStr = "]. ";
			userLogger.debug( "Selected privilages : " + privilageStr);
			
			GroupPrivileges gp = new GroupPrivileges();
			gp.getPrivilege().addAll(newGroupPrivileges);
			group.setGroupPrivileges(gp);
			
			userLogger.info( "calling GroupsManegementServicePort updateGroup- change privilage");
			ResultStatus result = groupManagementService.updateGroup(acctManagUserInfo, group);
			
			ResponseStatus state = result.getStatus();
			userLogger.info(" update group privilages response is:("+ state+ ").");
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			switch (state) {
			
				case SUCCESS:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util.getLocalizedLabel("group_updated_success")), null));
					populateGroups();
					initGroupPagination();
					break;
				case FAIL:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_updated_fail")), null));
					userLogger.warn( "Fail while updating group privilages");
					break;
				case INELIGIBLE_ACCOUNT:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
					userLogger.warn( "Ineligible user, not allowed to update group privilages");
					break;
				default:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_updated_fail")), null));
					userLogger.warn("Unknown response status="+ result.getStatus()+ " , while updating group privilages");
					break;
			}
			getSelectedGroup(this.groupBasic);
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
		return "";
		
	}
	
	public void populateGroupUsersToEdit(GroupBasicInfo group) {
		isSelfGroupEdit = false;
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
		allDefaultGroupUsers.clear();
		this.groupBasic = group;
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
			ThreadContext.push(trxId);
			userLogger.info( "calling GroupsManegementServicePort getAccountGroupFullInfo with groupBasic: ["+ groupBasic.getGroupName()+ "].");
			
			AccountGroupResultSet result = groupManagementService.getAccountGroupFullInfo(acctManagUserInfo, groupBasic);
			ResponseStatus state = result.getStatus();
			userLogger.info("getAccountGroupFullInfo response is:("+ state+ ").");
			selectedUsers = new HashMap<String, Boolean>();
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			switch (state) {
			
				case SUCCESS:
					this.group = result.getGroup();
					this.groupAdminId = this.group.getGroupAdminId();
					// TODO ask may
					if (canViewDefaultGroupUsers)
						getAllDefaultUsers();
					for (GroupUser user : this.group.getGroupUsers().getUser()) {
						if (user.getUsername().equals(userAcc.getUsername()))
							isSelfGroupEdit = true;
						selectedUsers.put(user.getUserId(), true);
						if (!this.group.isDefault()) {
							allDefaultGroupUsers.add(user);
						}
					}
					
					if (allDefaultGroupUsers.size() == 0) {
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util.getLocalizedLabel("group_has_no_users")), null));
					}
					break;
				case FAIL:
					userLogger.warn("Fail while getting group full info");
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
					
					break;
				case INELIGIBLE_ACCOUNT:
					userLogger.warn("Ineligible user, not allowed to get group full info");
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
					break;
				default:
					userLogger.warn("Unknown response status="+ result.getStatus()+ " , while getting group full info");
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
					
					break;
			}
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
		
	}
	
	public void changeGroupUsers() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
			List<GroupUser> newGroupUsers = new ArrayList<GroupUser>();
			boolean isAdminRemoved = true;
			for (Map.Entry<String, Boolean> e : selectedUsers.entrySet()) {
				String key = e.getKey();
				Boolean value = e.getValue();
				if (value.equals(true)) {
					if (key.equals(groupAdminId)) {
						isAdminRemoved = false;
					}
					for (int i = 0; i < allDefaultGroupUsers.size(); i++) {
						if (allDefaultGroupUsers.get(i).getUserId().equals(key)) {
							newGroupUsers.add(allDefaultGroupUsers.get(i));
						}
					}
				}
			}
			if (isAdminRemoved) {
				groupAdminId = null;
				group.setGroupAdminId(null);
			}
			
			String selectedUsersStr = "Users [ ";
			for (GroupUser user : newGroupUsers) {
				selectedUsersStr = " ( user id : " + user.getUserId() + ", user name : " + user.getUsername() + "),";
			}
			selectedUsersStr += "]. ";
			userLogger.debug( "Selected users : " + selectedUsersStr);
			
			GroupUsers gu = new GroupUsers();
			gu.getUser().addAll(newGroupUsers);
			group.setGroupUsers(gu);
			
			userLogger.info( "calling GroupsManegementServicePort updateGroup to change group users of group " + group.getGroupName());
			ResultStatus result = groupManagementService.updateGroup(acctManagUserInfo, group);
			
			ResponseStatus state = result.getStatus();
			userLogger.info("updateGroup response is:("+ state+ ").");
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			switch (state) {
			
				case SUCCESS:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util.getLocalizedLabel("group_updated_success")), null));
					populateGroups();
					
					break;
				case FAIL:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_updated_fail")), null));
					userLogger.warn( "Fail while updating group users");
					break;
				case INELIGIBLE_ACCOUNT:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
					userLogger.warn("Ineligible user, not allowed to update group users");
					break;
				case LAST_ADMIN_NOT_REMOVABLE:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("last_admin_error")), null));
					userLogger.warn( "can not remove last user in Admins group");
					break;
				default:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_updated_fail")), null));
					userLogger.warn("Unknown response status="+ result.getStatus()+ " , while updating group users");
					break;
			}
			getSelectedGroup(this.groupBasic);
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}
	}
	
	public void deleteGroup() {
		String  trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
			 ThreadContext.push(trxId);
			userLogger.info( "calling GroupsManegementServicePort deleteGroup with group, name:" + group.getGroupName());
			ResultStatus result = groupManagementService.deleteGroup(acctManagUserInfo, groupBasic);
			
			ResponseStatus state = result.getStatus();
			userLogger.info("deleteGroup response is:("+ state+ ").");
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			switch (state) {
			
				case SUCCESS:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util.getLocalizedLabel("group_deleted_Success")), null));
					populateGroups();
					initGroupPagination();
					break;
				case FAIL:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_deletion_failed")), null));
					userLogger.warn( "Fail while deleting group");
					break;
				case INELIGIBLE_ACCOUNT:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
					userLogger.warn("Ineligible user, not allowed to delete group");
					break;
				default:
					
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_deletion_failed")), null));
					userLogger.warn( "Unknown response status="+ result.getStatus()+ " , while deleting group");
					break;
			}
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, );
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}
		
	}
	
	public String createGroup() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
	        ThreadContext.push(trxId);
		if (groupName != null && !groupName.isEmpty() && !groupName.trim().equals("")) {
			this.group.setGroupName(groupName);
			try {
				List<Privilege> newGroupPrivileges = new ArrayList<Privilege>();
				for (Map.Entry<String, Boolean> e : selectedPrivillages.entrySet()) {
					String key = e.getKey();
					Boolean value = e.getValue();
					if (value.equals(true)) {
						for (int i = 0; i < allPrivileges.size(); i++) {
							if (allPrivileges.get(i).getPrivilegeId().equals(key)) {
								newGroupPrivileges.add(allPrivileges.get(i));
							}
						}
					}
				}
				
				String privilageStr = "privilages [ ";
				for (Privilege privilige : newGroupPrivileges) {
					privilageStr = " ( privilage id : " + privilige.getPrivilegeId() + ", privilage name : " + privilige.getPrivilegeId() + "),";
				}
				privilageStr = "]. ";
				userLogger.debug( "create group with privilages "+privilageStr+ "and group name: ["+ groupName+ " ]");
				
				GroupPrivileges gp = new GroupPrivileges();
				gp.getPrivilege().addAll(newGroupPrivileges);
				group.setGroupPrivileges(gp);
				
				userLogger.info( "calling GroupsManegementServicePort createGroup");
				
				ResultStatus result = groupManagementService.createGroup(acctManagUserInfo, group);
				
				ResponseStatus state = result.getStatus();
				userLogger.info( "GroupsManegementServicePort createGroup response is:("+ state+ ").");
				Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
				while (msgIterator.hasNext()) {
					msgIterator.next();
					msgIterator.remove();
				}
				switch (state) {
				
					case SUCCESS:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util.getLocalizedLabel("group_created_success")), null));
						populateGroups();
						initGroupPagination();
						break;
					case FAIL:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_creation_fail")), null));
						userLogger.warn("Fail while creating group");
						break;
					case INELIGIBLE_ACCOUNT:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
						userLogger.warn( "Ineligible user, not allowed to create group");
						break;
					default:
						
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_creation_fail")), null));
						userLogger.warn("un excepected error"+ "Unknown response status="+ result.getStatus()+ " , while creating group");
						break;
				}
			} catch (Exception e) {
//				String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
				userLogger.error("un excepected error", e.getMessage());
				Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
				while (msgIterator.hasNext()) {
					msgIterator.next();
					msgIterator.remove();
				}
				
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
				
				appLogger.error("un excepected error", e);
				appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
				
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("group_name_required")), null));
			userLogger.warn( "group name is empty");
			
			}
		} finally {
			ThreadContext.pop();
		}
		return "";
		
	}
	
	public void markAsGroupAdmin(String groupUserId, String groupUserName) {
		
		boolean userFound = false;
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
	        ThreadContext.push(trxId);
			userLogger.info( "update group admin request with user id"+ groupUserId);
			
			for (GroupUser groupUser : this.group.getGroupUsers().getUser()) {
				if (groupUser.getUserId().equals(groupUserId)) {
					userFound = true;
					break;
				}
			}
			if (!userFound) {
				userLogger.info("new admin id ("+ groupUserId+") is not attached on group, will add it in group's users");
				GroupUser gu = new GroupUser();
				gu.setUserId(groupUserId);
                                gu.setUsername(groupUserName);
				this.group.getGroupUsers().getUser().add(gu);
				selectedUsers.put(groupUserId, true);
			}
			groupAdminId = groupUserId;
			this.group.setGroupAdminId(groupUserId);
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
		// userLogger.info(concatenate(logTrxId, userLogInfo,
		// "calling GroupsManegementServicePort updateGroup with new admin ", groupUserId, "."));
		//
		// ResultStatus result = groupManagementService.updateGroup(acctManagUserInfo, this.group);
		//
		// ResponseStatus state = result.getStatus();
		// userLogger.info(concatenate(logTrxId, userLogInfo, "change group admin response is:(",
		// state, ")."));
		//
		// switch (state) {
		//
		// case SUCCESS:
		// FacesContext.getCurrentInstance().addMessage(null, new
		// FacesMessage(FacesMessage.SEVERITY_INFO,
		// String.format(Util.getLocalizedLabel("group_Admin_changed")), null));
		// break;
		// case FAIL:
		// FacesContext.getCurrentInstance().addMessage(null, new
		// FacesMessage(FacesMessage.SEVERITY_ERROR,
		// String.format(Util.getLocalizedLabel("group_admin_change_failed")), null));
		// userLogger.warn(concatenate(logTrxId, userLogInfo, "Fail to change group admin"));
		// break;
		// case INELIGIBLE_ACCOUNT:
		// FacesContext.getCurrentInstance().addMessage(null, new
		// FacesMessage(FacesMessage.SEVERITY_ERROR,
		// String.format(Util.getLocalizedLabel("ineligible_user")), null));
		// userLogger.warn(concatenate(logTrxId, userLogInfo,
		// "Ineligible user, not allowed to change group admin"));
		// default:
		// FacesContext.getCurrentInstance().addMessage(null, new
		// FacesMessage(FacesMessage.SEVERITY_ERROR,
		// String.format(Util.getLocalizedLabel("group_admin_change_failed")), null));
		// userLogger.warn(concatenate(logTrxId, userLogInfo, "Unknown response status=",
		// result.getStatus(), " , while changing group admin"));
		// break;
		// }
		// populateGroupUsersToEdit(groupBasic);
	}
	
	public void revokeGroupAdmin() {
		String trxId = TrxId.getTrxId();
		acctManagUserInfo.setTrxId(trxId);
//		String logTrxId = logTrxId(acctManagUserInfo.getTrxId());
		try {
	        ThreadContext.push(trxId);
			userLogger.info( "revoke group admin with user id", groupAdminId);
			
			groupAdminId = "";
			this.group.setGroupAdminId(null);
			userLogger.info( "admin has been revoked");
		} catch (Exception e) {
//			String logMsg = concatenate(logTrxId, userLogInfo, "un excepected error");
			userLogger.error("un excepected error", e.getMessage());
			Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
			
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
			
			appLogger.error("un excepected error", e);
			appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.GENERAL_ERROR, "Generic failure");
			
		}finally{
            ThreadContext.pop();
        }
	}
	
	public void cancel() {
		groupName = "";
		editedGroupName = groupBasic.getGroupName();
	}
	
	private List<GroupUser> convertDefaultUsersToGroupUsers(List<UserDetails> usersDetails) {
		List<GroupUser> groupUsers = new ArrayList<GroupUser>();
		
		for (UserDetails userDetails : usersDetails) {
			GroupUser groupUser = new GroupUser();
			groupUser.setUserId(userDetails.getUserId());
			groupUser.setUsername(userDetails.getUsername());
			
			groupUsers.add(groupUser);
		}
		
		return groupUsers;
	}
	// pagination methods
	
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
		populateGroups();
	}
	
	public void page(ActionEvent event) {
		page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
	}
	
	// getters and setters
	
	public int getTotalRows() {
		return totalRows;
	}
	
	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
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
	
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	public int getTotalPages() {
		return totalPages;
	}
	
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	
	public int getTotalRowsNum() {
		return totalRowsNum;
	}
	
	public void setTotalRowsNum(int totalRowsNum) {
		this.totalRowsNum = totalRowsNum;
	}
	
	public String getSearchValue() {
		return searchValue;
	}
	
	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
	
	public List<GroupBasicInfo> getListofGroups() {
		return listofGroups;
	}
	
	public void setListofGroups(List<GroupBasicInfo> listofGroups) {
		this.listofGroups = listofGroups;
	}
	
	public Map<String, Boolean> getSelectedUsers() {
		return selectedUsers;
	}
	
	public void setSelectedUsers(Map<String, Boolean> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}
	
	public List<GroupUser> getAllDefaultGroupUsers() {
		return allDefaultGroupUsers;
	}
	
	public void setAllDefaultGroupUsers(List<GroupUser> allDefaultGroupUsers) {
		this.allDefaultGroupUsers = allDefaultGroupUsers;
	}
	
	public GroupBasicInfo getGroupBasic() {
		return groupBasic;
	}
	
	public void setGroupBasic(GroupBasicInfo groupBasic) {
		this.groupBasic = groupBasic;
	}
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public String getSearchInGroup() {
		return searchInGroup;
	}
	
	public void setSearchInGroup(String searchInGroup) {
		this.searchInGroup = searchInGroup;
	}
	
	public List<Privilege> getAllPrivileges() {
		return allPrivileges;
	}
	
	public void setAllPrivileges(List<Privilege> allPrivileges) {
		this.allPrivileges = allPrivileges;
	}
	
	public Map<String, Boolean> getSelectedPrivillages() {
		return selectedPrivillages;
	}
	
	public void setSelectedPrivillages(Map<String, Boolean> selectedPrivillages) {
		this.selectedPrivillages = selectedPrivillages;
	}
	
	public String getSelectedGroup() {
		return selectedGroup;
	}
	
	public void setSelectedGroup(String selectedGroup) {
		this.selectedGroup = selectedGroup;
	}
	
	public boolean isTableFlag() {
		return tableFlag;
	}
	
	public void setTableFlag(boolean tableFlag) {
		this.tableFlag = tableFlag;
	}
	
	public boolean isEditFlag() {
		return editFlag;
	}
	
	public void setEditFlag(boolean editFlag) {
		this.editFlag = editFlag;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public boolean isEditeDeleteFlag() {
		return editeDeleteFlag;
	}
	
	public void setEditeDeleteFlag(boolean editeDeleteFlag) {
		this.editeDeleteFlag = editeDeleteFlag;
	}
	
	public boolean isAdminsFlag() {
		return adminsFlag;
	}
	
	public void setAdminsFlag(boolean adminsFlag) {
		this.adminsFlag = adminsFlag;
	}
	
	public String getGroupAdminId() {
		return groupAdminId;
	}
	
	public void setGroupAdminId(String groupAdminId) {
		this.groupAdminId = groupAdminId;
	}
	
	public String getEditedGroupName() {
		return editedGroupName;
	}
	
	public void setEditedGroupName(String editedGroupName) {
		this.editedGroupName = editedGroupName;
	}
	
	private String grouptoString(Group group) {
		StringBuilder builder = new StringBuilder();
		builder.append("GroupModel [");
		if (groupAdminId != null) {
			builder.append("groupAdminId=");
			builder.append(group.getGroupAdminId());
			builder.append(", ");
		}
		if (group.getGroupUsers().getUser() != null) {
			builder.append("groupUsers size=");
			builder.append(group.getGroupUsers().getUser().size());
			builder.append(", ");
		}
		if (group.getGroupPrivileges().getPrivilege() != null) {
			builder.append("privileges size=");
			builder.append(group.getGroupPrivileges().getPrivilege().size());
			builder.append(", ");
		}
		if (group.getGroupId() != null) {
			builder.append("groupId=");
			builder.append(group.getGroupId());
			builder.append(", ");
		}
		if (group.getGroupName() != null) {
			builder.append("groupName=");
			builder.append(group.getGroupName());
			builder.append(", ");
		}
		builder.append("defaultGroup=");
		builder.append(group.isDefault());
		
		builder.append("]");
		return builder.toString();
	}
	
	public UserAccount getUserAcc() {
		return userAcc;
	}
	
	public boolean isCanCreateGroup() {
		return canCreateGroup && canEditGroupPriv;
	}
	
	public void setCanCreateGroup(boolean canCreateGroup) {
		this.canCreateGroup = canCreateGroup;
	}
	
	public boolean isCanDeleteGroup() {
		return canDeleteGroup && !isSelfGroup();
	}
	
	public void setCanDeleteGroup(boolean canDeleteGroup) {
		this.canDeleteGroup = canDeleteGroup;
	}
	
	public boolean isCanEditGroupPriv() {
		if (editFlag) {
			return canEditGroupPriv && canEditGroup && !isSelfGroup();
		} else {
			return canEditGroupPriv && canCreateGroup;
		}
	}
	
	public void setCanEditGroupPriv(boolean canEditGroupPriv) {
		this.canEditGroupPriv = canEditGroupPriv;
	}
	
	public boolean isCanEditGroupUsers() {
		return canEditGroupUsers && canEditGroup;
	}
	
	public void setCanEditGroupUsers(boolean canEditGroupUsers) {
		this.canEditGroupUsers = canEditGroupUsers;
	}
	
	public boolean isCanEditGroup() {
		return canEditGroup;
	}
	
	public void setCanEditGroup(boolean canEditGroup) {
		this.canEditGroup = canEditGroup;
	}
	
	public boolean isCanMarkGroupsAdmin() {
		return canMarkGroupsAdmin && canEditGroupUsers && canEditGroup && (!isAdminsGroup());
	}
	
	private boolean isSelfGroup() {
		if (group != null) {
			for (int j = 0; j < this.group.getGroupUsers().getUser().size(); j++) {
//                            System.out.println("this.group.getGroupUsers().getUser().get(j).getUsername()"+this.group.getGroupUsers().getUser().get(j).getUsername());
                            	if (this.group.getGroupUsers().getUser().get(j).getUsername().equals(userAcc.getUsername())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isAdminsGroup() {
		if (group != null)
			return group.getGroupName().equals(Configs.ADMINS_GROUP_NAME.getValue());
		else
			return false;
	}
	
	public boolean isDefaultGroup() {
		if (group != null)
			return group.isDefault();
		else
			return false;
	}
	
	public void setCanMarkGroupsAdmin(boolean canMarkGroupsAdmin) {
		this.canMarkGroupsAdmin = canMarkGroupsAdmin;
	}
	
	public String getMsgsClassForUsersPanel() {
		if (allDefaultGroupUsers.size() == 0)
			return "disclaimer success empty-message-class";
		else
			return "disclaimer success";
	}
	
	public boolean isIsSelfGroupEdit() {
		return isSelfGroupEdit;
	}
	
	public void setIsSelfGroupEdit(boolean isSelfGroupEdit) {
		this.isSelfGroupEdit = isSelfGroupEdit;
	}
	
}

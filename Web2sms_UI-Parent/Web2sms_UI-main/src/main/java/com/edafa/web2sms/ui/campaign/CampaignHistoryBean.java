package com.edafa.web2sms.ui.campaign;

import java.util.ArrayList;
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
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.service.campaign.model.CampaignResultSet;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.campaign.CampaignManagementService;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.Campaign;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.ui.filters.DirectLoginFilter;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

@ManagedBean(name = "campaignHistoryBean")
@ViewScoped
public class CampaignHistoryBean {

	@EJB
	WSClients wsClients;
        
        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;

	CampaignManagementService campaignServicePort;
	private List<Campaign> historyCampaignList = new ArrayList<Campaign>();

	private int totalRows;
	private int firstRow;
	private int rowsPerPage;
	private int pageRange;
	private Integer[] pages;
	private int currentPage;
	private int totalPages;

	private boolean tableFlag = true;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());

	private Map<Integer, Boolean> checked;
	private boolean historyTableFlag = true;

	FacesContext facesContext = FacesContext.getCurrentInstance();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
	User user = new User();
	AccManagUser accUser = new AccManagUser();
	// Setters and
	// getters--------------------------------------------------------

	public List<Campaign> getHistoryCampaignList() {
		return historyCampaignList;
	}

	public boolean isHistoryTableFlag() {
		return historyTableFlag;
	}

	public void setHistoryTableFlag(boolean historyTableFlag) {
		this.historyTableFlag = historyTableFlag;
	}

	public Map<Integer, Boolean> getChecked() {
		return checked;
	}

	public void setChecked(Map<Integer, Boolean> checked) {
		this.checked = checked;
	}

	public void setHistoryCampaignList(List<Campaign> historyCampaignList) {
		this.historyCampaignList = historyCampaignList;
	}

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

	public boolean isTableFlag() {
		return tableFlag;
	}

	public void setTableFlag(boolean tableFlag) {
		this.tableFlag = tableFlag;
	}

	// Constructor-------------------------------------------------

	public CampaignHistoryBean() {
//		UserTrxInfo userInfo = new UserTrxInfo();
//		userInfo.setTrxId(TrxId.getTrxId());
		user.setAccountId(userAcc.getAccount().getAccountId());
		user.setUsername(userAcc.getUsername());
		
		accUser.setAccountId(userAcc.getAccount().getAccountId());
	}

	@PostConstruct
	private void postMethod() {
		campaignServicePort = wsClients.getCampaignServicePort();
		rowsPerPage = 5; // Default rows per page (max amount of rows to be
		// displayed at once).
		pageRange = 9;
		populateList();
	}

	public void populateList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
			ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
			userLogger.debug("Getting history campaigns from="
					+ firstRow + " to=" + rowsPerPage);

			CampaignResultSet result = campaignServicePort.getHistoryCampaigns(userInfo, firstRow, rowsPerPage);
			switch (result.getStatus()) {
				case INVALID_REQUEST :
					historyCampaignList = null;
					userLogger.warn("invalid request for getting history campaign list");
					FacesContext.getCurrentInstance()
							.addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR,
											String.format(Util.getLocalizedLabel("get_history_campaigns_error")), null));

					break;
				case SUCCESS :
					historyCampaignList = result.getCampaign();
					userLogger.info( "history campaign list is filled successfully rows_count=" + historyCampaignList.size());
					break;
				case FAIL :
					FacesContext.getCurrentInstance()
							.addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR,
											String.format(Util.getLocalizedLabel("get_history_campaigns_error")), null));

					historyCampaignList = null;
					userLogger.error( "Error while getting history campaign list");
					break;
				case INELIGIBLE_ACCOUNT :
					historyCampaignList = null;
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_view_history")), null));
					userLogger.warn( "historyCampaignList ,not allowed to view campaign history list");
					break;
				default :
					FacesContext.getCurrentInstance()
							.addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR,
											String.format(Util.getLocalizedLabel("get_history_campaigns_error")), null));

					userLogger.error("Unknown response status="
							+ result.getStatus() + " for populating campaign history list");
					break;
			}

			totalRows = campaignServicePort.getCampaignHistoryCount(user);

			userLogger.info( "Total campaign history list rows_count=" + totalRows);

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

			if (historyCampaignList == null || historyCampaignList.size() == 0) {
				historyTableFlag = false;
                tableFlag=false;
				userLogger.info( "History campaign list is empty, the table will be disabled");
			}

			checked = new HashMap<Integer, Boolean>();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("get_history_campaigns_error")), null));

			userLogger.error( "Error while getting history campaign list", e);
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

	public void deleteHistory() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
			ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
			ResultStatus status = new ResultStatus();
			List<Campaign> checkedhistory = new ArrayList<Campaign>();
			for (Campaign campaign : historyCampaignList) {
				if (checked.get((campaign.getCampaignId()))) {
					checkedhistory.add(campaign);
				}// end if
			}// end for

			List<String> camIds = new ArrayList<String>();
			for (int i = 0; i < checkedhistory.size(); i++) {
				camIds.add(checkedhistory.get(i).getCampaignId());
				userLogger.info("Trying to delete campaign history " + checkedhistory.get(i).getCampaignId());
			}

			status = campaignServicePort.deleteHistory(userInfo, camIds);

			ResponseStatus resp = status.getStatus();
			switch (resp) {
				case SUCCESS :
					userLogger.info("Campaign/Campaigns deleted successfully");
					break;
				case FAIL :
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("delete_history_error")), null));

					userLogger.error( "Error while deleting campaign history");
					break;
				case INELIGIBLE_ACCOUNT :
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("ineligible_user")), null));
                                        userLogger.warn( "INELIGIBLE_ACCOUNT, not allowed to delete campaign history");
					break;
				default :
					FacesContext.getCurrentInstance().addMessage(null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("delete_history_error")), null));

					userLogger.error("Unknown response status="
							+ resp + " while deleting campaign history");
					break;
			}
			populateList();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("delete_history_error")), null));
			userLogger.info( "Error while deleting campaigns from history", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
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

    
        public String getEmptyString(){
            return "";
        }
}

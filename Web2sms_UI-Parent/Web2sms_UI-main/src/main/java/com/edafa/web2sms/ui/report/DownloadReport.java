package com.edafa.web2sms.ui.report;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.FileTokenResult;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.report.ReportManagementService;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.file.DownloadedFileInfo;
import com.edafa.web2sms.utils.file.FileDownloadClient;

/**
 * Servlet implementation class DownloadReport
 */
@WebServlet("/DownloadReport")
public class DownloadReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	ReportManagementService reportServicePort;
	@EJB
	WSClients portObj;
        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;
	DownloadedFileInfo result;
	InputStream input;
	UserTrxInfo userInfo = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadReport() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String campaignId = "";
		try {
			PrintWriter out = null;
			HttpSession session = request.getSession();
			campaignId = (String) session.getAttribute("campaignReportId");
			userInfo = (UserTrxInfo) session.getAttribute("UserInfo");
			String trxId = userInfo.getTrxId();
	        ThreadContext.push(trxId);
			userLogger.info( "Get report service port, to generate detailed report of campaign id=" + campaignId);

			reportServicePort = portObj.getReportServicePort();
			String exportReportTitle= "Export Report";
			String error = "Error";
			String failErrorMessage= "Error while exporting report";
//			String ineligibleMessage = "You are ineligible to export report.";
			String noDataMessage = "Sorry, There's no data for this campaign.";
			
			FileTokenResult token = reportServicePort.generateDetailedCampaignReport(userInfo, campaignId);
			ResponseStatus status = token.getStatus();
			switch (status) {
				case SUCCESS :
					exportReport(userInfo, token, response);
					userLogger.info(" generated detailed report of campaign id=" + campaignId + " successfully");
					break;
				case FAIL :
					userLogger.error( "Error could not get the file token " + status + token.getErrorMessage()
							+ " to generate detailed report for campaign id=" + campaignId);
					
					out = response.getWriter();
					out.print("<!DOCTYPE html><html><head><title>"+exportReportTitle+"</title></head><body><h2 ><font color='red'>"+error+"</font></h2><h3>"
							+ failErrorMessage + "</h3></body></html>");
				
					break;
				case CAMPAIGN_NOT_FOUND :
					userLogger.warn("Campaign with id=" + campaignId + " is not found to generate detailed report");

					out = response.getWriter();
					out.print("<!DOCTYPE html><html><head><title>"+exportReportTitle+"</title></head><body><h2 ><font color='red'>"+error+"</font></h2><h3>"
							+ noDataMessage + "</h3></body></html>");
					break;
				// case
				default :
					userLogger.error( "Undefined response status=" + status + " to generate detailed report of campaign id=" + campaignId);

					out = response.getWriter();
					out.print("<!DOCTYPE html><html><head><title>"+exportReportTitle+"</title></head><body><h2 ><font color='red'>"+error+"</font></h2><h3>"
							+ failErrorMessage + "</h3></body></html>");
					break;
			}

		} catch (Exception e) {
			userLogger.error( "Error while exporting campaign id=" + campaignId, e);
           appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
                        
		}finally{
            ThreadContext.pop();
        }
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public void exportReport(UserTrxInfo userInfo, FileTokenResult token, HttpServletResponse response) throws IOException {
		OutputStream output = null;
		try {
			FileDownloadClient fileDownload = new FileDownloadClient();
			result = fileDownload.downloadFile(userInfo.getTrxId(), token.getFileToken(), (String) Configs.DOWNLOAD_REPORT_SERVER_LINK.getValue());
			input = result.getFileInputStream();
			userLogger.info( "file token contains the file name is successfully returned " + token.getFileToken());
			userLogger.info( "Exporting report for ");

			response.setHeader("Content-Type", "application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + result.getFileName() + "\"");

			output = response.getOutputStream();
			// Now you can write the InputStream of the file to the above
			// OutputStream the usual way.

			int octet;

			while ((octet = input.read()) != -1) {
				output.write(octet);
			}

			input.close();

			output.flush();
			output.close();

			userLogger.info( "Done exporting report for file " + result.getFileName());

		} catch (Exception e) {
			input.close();
			output.flush();
			output.close();
			userLogger.error( "Error while exporting report file=" + result.getFileName(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
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
}

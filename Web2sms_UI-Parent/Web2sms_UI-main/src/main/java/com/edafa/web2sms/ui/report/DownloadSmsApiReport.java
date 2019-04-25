package com.edafa.web2sms.ui.report;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

import com.edafa.web2sms.service.model.UserReportRequest;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.report.ReportManagementService;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.file.DownloadedFileInfo;
import com.edafa.web2sms.utils.file.XmlFileHttpClient;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Servlet implementation class DownloadReport
 */
@WebServlet("/DownloadSmsApiReport")
public class DownloadSmsApiReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	ReportManagementService reportServicePort;
	@EJB
	WSClients portObj;
	@EJB
	XmlFileHttpClient xmlClient;
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;
	DownloadedFileInfo result;
	InputStream input;
	UserTrxInfo userInfo = new UserTrxInfo();
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadSmsApiReport() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			HttpSession session = request.getSession();
			userInfo = (UserTrxInfo) session.getAttribute("UserInfo");
			String token = (String) session.getAttribute("SMS_report_token");
			ThreadContext.push(userInfo.getTrxId());
			userLogger.info(logTrxId(userInfo.getTrxId()) + "downloading file with token: " + token);
			exportReport(userInfo, token, response);
		} catch (Exception e) {
			userLogger.error(logTrxId(userInfo.getTrxId()) + "Error while downloading sms api ", e);
			appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.pop();
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}
	
	public void exportReport(UserTrxInfo userInfo, String token, HttpServletResponse response) throws IOException {
		OutputStream output = null;
		try {
			result = downloadFile(userInfo.getTrxId(), token, (String) Configs.DOWNLOAD_REPORT_SERVER_LINK.getValue());
			input = result.getFileInputStream();
			userLogger.info( "Exporting report with token: " + token);
			response.setHeader("Content-Type", "application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + result.getFileName() + "\"");
			output = response.getOutputStream();
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
	
	public DownloadedFileInfo downloadFile(String trxId, String fileName, String url) throws IOException {
		DownloadedFileInfo result;
		UserReportRequest request = new UserReportRequest();
		request.setFileToken(fileName);
		request.setTrx(trxId);
		ClientResponse response = xmlClient.sendHttpXmlRequest(url, request);
		InputStream input = response.getEntityInputStream();
		if (response.getStatus() != 200 || input == null)
			throw new FileNotFoundException();
		result = new DownloadedFileInfo((String) response.getHeaders().get("Content-Disposition").get(0).split("=")[1], 0, response.getEntityInputStream());
		return result;
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

package com.edafa.web2sms.ui.report;

import com.edafa.web2sms.reporting.service.model.CampaignAggregationReport;
import com.edafa.web2sms.service.model.SummaryReport;

public class UploadedPDFReport {
	private CampaignAggregationReport campaignReportObj;
	private SummaryReport grandTotal;
	private String listName;

	public CampaignAggregationReport getCampaignReportObj() {
		return campaignReportObj;
	}

	public void setCampaignReportObj(CampaignAggregationReport campaignReportObj) {
		this.campaignReportObj = campaignReportObj;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public SummaryReport getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(SummaryReport grandTotal) {
		this.grandTotal = grandTotal;
	}

}

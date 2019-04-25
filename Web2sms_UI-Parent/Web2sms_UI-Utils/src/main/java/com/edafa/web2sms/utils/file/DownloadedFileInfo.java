package com.edafa.web2sms.utils.file;

import java.io.InputStream;

public class DownloadedFileInfo {

	InputStream is;
	String fileName;
	Integer fileSize;

	public DownloadedFileInfo(String fileName, Integer fileSize, InputStream is) {
		super();
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.is = is;
	}

	public InputStream getFileInputStream() {
		return is;
	}

	public String getFileName() {
		return fileName;
	}

	public Integer getFileSize() {
		return fileSize;
	}

}

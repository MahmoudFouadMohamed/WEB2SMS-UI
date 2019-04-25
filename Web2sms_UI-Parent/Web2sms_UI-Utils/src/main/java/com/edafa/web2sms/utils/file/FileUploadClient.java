package com.edafa.web2sms.utils.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * Session Bean implementation class FileUploadClient
 */
@Stateless
@LocalBean
public class FileUploadClient {

	/**
	 * Default constructor.
	 */
	public FileUploadClient() {
		// TODO Auto-generated constructor stub
	}

	public String upload(String url, File uploadFile) throws IOException {
		WebResource resource = Client.create().resource(url);
		FormDataMultiPart form = new FormDataMultiPart();
		form.field("fileName", uploadFile.getName());
		FormDataBodyPart fdp = new FormDataBodyPart("file", new FileInputStream(uploadFile),
				MediaType.APPLICATION_OCTET_STREAM_TYPE);
		form.bodyPart(fdp);
		String response = resource.type(MediaType.MULTIPART_FORM_DATA).post(String.class, form);
		return response;
	}

	public String upload(String url, InputStream is) throws IOException {
		WebResource resource = Client.create().resource(url);
		FormDataMultiPart form = new FormDataMultiPart();
		// form.field("fileName", uploadFile.getName());
		FormDataBodyPart fdp = new FormDataBodyPart("file", is, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		form.bodyPart(fdp);
		String response = resource.type(MediaType.MULTIPART_FORM_DATA).post(String.class, form);
		return response;
	}

}

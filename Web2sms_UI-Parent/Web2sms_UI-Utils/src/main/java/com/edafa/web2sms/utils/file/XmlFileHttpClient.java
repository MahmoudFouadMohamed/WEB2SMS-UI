package com.edafa.web2sms.utils.file;

import java.net.URI;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;

import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Stateless
@LocalBean
public class XmlFileHttpClient {
	
	private static Client client;
	Logger appLogger;
	
	@PostConstruct
	void init() {
		appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
		client = createClient();
	}
	
	/**
	 * Template method to allow tooling to override Client factory
	 */
	private Client createClientInstance(ClientConfig cc) {
		return Client.create(cc);
	}
	
	/**
	 * Create a new Client instance
	 */
	private Client createClient() {
		ClientConfig cc = new DefaultClientConfig();
		Client c = createClientInstance(cc);
		return c;
	}
	
	public ClientResponse sendHttpXmlRequest(String baseURI, Object input) {
		ClientResponse cr = null;
		try {
			cr = sendHttpXmlRequest(URI.create(baseURI), input);
		} catch (Exception ex) {
			appLogger.error("Error while send HttpXml(download report) Request: ", ex);
			if (appLogger.isDebugEnabled()) {
				appLogger.debug("Error while send HttpXml(download report) Request: " + input);
			}
		}
		return cr;
	}
	
	public ClientResponse sendHttpXmlRequest(URI baseURI, Object input) {
		if (client == null) {
			client = createClient();
		}
		
		WebResource webResource = client.resource(baseURI);
		ClientResponse response = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, input);
		if (response.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
			return null;
		}
		
		return response;
	}
}

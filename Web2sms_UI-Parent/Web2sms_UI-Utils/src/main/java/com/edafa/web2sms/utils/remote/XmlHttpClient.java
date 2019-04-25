package com.edafa.web2sms.utils.remote;

import java.net.URI;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.edafa.web2sms.utils.configs.enums.Configs;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@Singleton
@LocalBean
public class XmlHttpClient {

        private static Client errorsRaisingClient;

	@PostConstruct
	void init() {
                errorsRaisingClient = createClient();
	}

	/**
	 * Template method to allow tooling to customize the new Client
	 * 
	 */
	private void customizeClientConfiguration(ClientConfig cc) {
		cc.getProperties().put(ClientConfig.PROPERTY_THREADPOOL_SIZE, 10);
	}

	/**
	 * Template method to allow tooling to override Client factory
	 * 
	 */
	private Client createClientInstance(ClientConfig cc) {
		return Client.create(cc);
	}

	/**
	 * Create a new Client instance
	 * 
	 */
	private Client createClient() {
		ClientConfig cc = new DefaultClientConfig();
		customizeClientConfiguration(cc);
		Client c = createClientInstance(cc);
		Integer connectTimeout = (Integer) Configs.WS_CLIENT_CONNECT_TIMEOUT.getValue();
		Integer requestTimeout = (Integer) Configs.WS_CLIENT_REQUEST_TIMEOUT.getValue();
		c.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectTimeout);
		c.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, requestTimeout);
		return c;
	}

//	public ClientResponse sendHttpXmlRequest(URI baseURI, Map<String, Object> formMap) {
//		if (client == null) {
//			client = createClient();
//		}
//
//		WebResource webResource = client.resource(baseURI);
//		ClientResponse response2 = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, formMap);
//
//		if (response2.getStatus() != 200) {
//			throw new WebApplicationExceptionMessage(Response.status(response2.getClientResponseStatus()).build());
//		}
//		return response2;
//	}

	public ClientResponse sendHttpXmlRequest(String baseURI, Object input) {
		return sendHttpXmlRequest(URI.create(baseURI), input);
	}

	public ClientResponse sendHttpXmlRequest(URI baseURI, Object input) {

		if (errorsRaisingClient == null) {
			errorsRaisingClient = createClient();
		}

		WebResource webResource = errorsRaisingClient.resource(baseURI);
		ClientResponse response2 = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, input);
		return response2;
	}
}

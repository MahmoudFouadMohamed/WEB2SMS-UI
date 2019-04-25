package com.edafa.web2sms.ui.locale;


import javax.faces.FacesException;

import javax.faces.context.ExternalContext;
import javax.faces.context.ExternalContextWrapper;

import com.sun.faces.context.ExternalContextFactoryImpl;

/**
* {@link ExternalContextFactory} to prevent session id in resource urls.
*/
public class ExternalContextFactory extends ExternalContextFactoryImpl
{
  /**
   * {@inheritDoc}
   */
  @Override
  public ExternalContext getExternalContext(
    Object context,
    Object request,
    Object response)
    throws FacesException
  {
    final ExternalContext externalContext = 
      super.getExternalContext(context, request, response); 

    return new ExternalContextWrapper()
    {
      @Override
      public ExternalContext getWrapped()
      {
        return externalContext;
      }

      @Override
      public String encodeResourceURL(String url)
      {
        return shouldEncode(url) ? super.encodeResourceURL(url) : url;
      }

      private boolean shouldEncode(String url)
      { 
        // Decide here whether you want to encode url.
        // E.g. in case of h:outputLink you may want to have session id in url,
        // so your decision is based on some marker (like &session=1) in url. 
        return false;
      }
    };
  }
}
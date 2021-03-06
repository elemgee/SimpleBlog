package com.wowodc.rest.controllers;

import java.io.IOException;
import java.util.NoSuchElementException;

import sun.misc.BASE64Decoder;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.wowodc.model.Person;

import er.extensions.appserver.ERXHttpStatusCodes;
import er.rest.format.ERXRestFormat;
import er.rest.routes.ERXRouteController;

public class BaseRestController extends ERXRouteController {

  private Person authenticatedUser;
  
  public BaseRestController(WORequest request) {
    super(request);
  }

  public WOActionResults updateAction() throws Throwable {
    return errorResponse(ERXHttpStatusCodes.METHOD_NOT_ALLOWED);
  }

  public WOActionResults createAction() throws Throwable {
    return errorResponse(ERXHttpStatusCodes.METHOD_NOT_ALLOWED);
  }

  @Override
  protected ERXRestFormat defaultFormat() {
    return ERXRestFormat.json();
  }

  @Override
  protected boolean isAutomaticHtmlRoutingEnabled() {
    return true;
  }

  protected void initAuthentication() throws NotAuthorizedException {
    String authValue = request().headerForKey( "authorization" );

    if( authValue != null ) {
      try {
        byte[] authBytes = new BASE64Decoder().decodeBuffer( authValue.replace( "Basic ", "" ) );
        String[] parts = new String( authBytes ).split( ":", 2 );
        String username = parts[0];
        String password = parts[1];
        setAuthenticatedUser(Person.validateLogin(editingContext(), username, password));
      } catch ( IOException e ) {
        log.error( "Could not decode basic auth data: " + e.getMessage() );
        e.printStackTrace();
      }
    } else {
      throw new NotAuthorizedException();
    }
  }

  protected Person authenticatedUser() {
    return authenticatedUser;
  }

  @Override
  public WOActionResults performActionNamed(String actionName, boolean throwExceptions)  {
    if (!isHTMlFormat()) {
      try {
        initAuthentication();
      } catch (NoSuchElementException ex) {
        WOResponse response = (WOResponse)errorResponse(ERXHttpStatusCodes.UNAUTHORIZED);
        response.setHeader("Basic realm=\"Members\"", "WWW-Authenticate");
        return response;
      } catch (NotAuthorizedException ex) {
        WOResponse response = (WOResponse)errorResponse(ERXHttpStatusCodes.UNAUTHORIZED);
        response.setHeader("Basic realm=\"Members\"", "WWW-Authenticate");
        return response;
      }
    }
    return super.performActionNamed(actionName, throwExceptions);
  }

  protected void setAuthenticatedUser(Person authenticatedUser) {
    this.authenticatedUser = authenticatedUser;
  }
  
  protected boolean isHTMlFormat() {
    return (ERXRestFormat.html().name().equals(this.format().name())) ? true: false;
  }
  
  public class NotAuthorizedException extends Exception {

    public NotAuthorizedException() {
      super();
    }

  }

}

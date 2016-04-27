package com.enonic.xp.web.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Multimap;

import com.enonic.xp.web.HttpMethod;

public interface WebRequest
{
    HttpMethod getMethod();

    String getScheme();

    String getHost();

    int getPort();

    String getPath();

    Multimap<String, String> getParams();

    String getUrl();

    Map<String, String> getCookies();

    Object getBody();

    String getBodyAsString();

    HttpServletRequest getRawRequest();

    boolean isWebSocket();

//    String getBaseUri();

//    Branch getBranch();

//    RenderMode getMode();

//    String rewriteUri( final String uri );

//    Site getSite();

//    Content getContent();

//    PageTemplate getPageTemplate();

//    Component getComponent();

//    ApplicationKey getApplicationKey()

//    PageDescriptor getPageDescriptor()

//    ContentPath getContentPath()

//    String getEndpointPath()

//    String getContentType()

//    ControllerScript getControllerScript()

}

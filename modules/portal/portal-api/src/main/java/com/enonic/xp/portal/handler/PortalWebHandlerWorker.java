package com.enonic.xp.portal.handler;

import com.google.common.net.HttpHeaders;

import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.PortalWebRequest;
import com.enonic.xp.portal.PortalWebResponse;
import com.enonic.xp.web.HttpStatus;
import com.enonic.xp.web.handler.WebException;
import com.enonic.xp.web.websocket.WebSocketConfig;
import com.enonic.xp.web.websocket.WebSocketEndpoint;

public abstract class PortalWebHandlerWorker
{
    protected PortalWebRequest portalWebRequest;

    protected PortalWebResponse portalWebResponse;

    public PortalWebHandlerWorker( final Builder builder )
    {
        portalWebRequest = builder.portalWebRequest;
        portalWebResponse = builder.portalWebResponse;
    }

    public abstract PortalWebResponse execute()
        throws Exception;

    public WebSocketEndpoint newWebSocketEndpoint( final WebSocketConfig config )
        throws Exception
    {
        return null;
    }

    protected final WebException notFound( final String message, final Object... args )
    {
        return new WebException( HttpStatus.NOT_FOUND, String.format( message, args ) );
    }

    protected final WebException forbidden( final String message, final Object... args )
    {
        return new WebException( HttpStatus.FORBIDDEN, String.format( message, args ) );
    }

    protected void setResponseCacheable( final boolean isPublic )
    {
        final String cacheControlValue = ( isPublic ? "public" : "private" ) + ", max-age=31536000";
        portalWebResponse.setHeader( HttpHeaders.CACHE_CONTROL, cacheControlValue );
    }

    //TODO Temporary fix until renaming of PortalWebRequest to PortalRequest
    @Deprecated
    protected PortalRequest convertToPortalRequest( PortalWebRequest portalWebRequest )
    {
        final PortalRequest portalRequest = new PortalRequest();
        portalRequest.setMethod( portalWebRequest.getMethod() );
        portalRequest.getParams().putAll( portalWebRequest.getParams() );
        portalRequest.getHeaders().putAll( portalWebRequest.getHeaders() );
        portalRequest.getCookies().putAll( portalWebRequest.getCookies() );
        portalRequest.setScheme( portalWebRequest.getScheme() );
        portalRequest.setHost( portalWebRequest.getHost() );
        portalRequest.setPort( portalWebRequest.getPort() );
        portalRequest.setPath( portalWebRequest.getPath() );
        portalRequest.setUrl( portalWebRequest.getUrl() );
        portalRequest.setMode( portalWebRequest.getMode() );
        portalRequest.setBranch( portalWebRequest.getBranch() );
        portalRequest.setContentPath( portalWebRequest.getContentPath() );
        portalRequest.setBaseUri( portalWebRequest.getBaseUri() );
        portalRequest.setSite( portalWebRequest.getSite() );
        portalRequest.setContent( portalWebRequest.getContent() );
        portalRequest.setPageTemplate( portalWebRequest.getPageTemplate() );
        portalRequest.setComponent( portalWebRequest.getComponent() );
        portalRequest.setApplicationKey( portalWebRequest.getApplicationKey() );
        portalRequest.setPageDescriptor( portalWebRequest.getPageDescriptor() );
        portalRequest.setControllerScript( portalWebRequest.getControllerScript() );
        portalRequest.setEndpointPath( portalWebRequest.getEndpointPath() );
        portalRequest.setContentType( portalWebRequest.getContentType() );
        portalRequest.setBody( portalWebRequest.getBody() );
        portalRequest.setRawRequest( portalWebRequest.getRawRequest() );
        portalRequest.setWebSocket( portalWebRequest.isWebSocket() );
        return portalRequest;
    }

    //TODO Temporary fix until renaming of PortalWebResponse to PortalResponse
    @Deprecated
    protected PortalWebResponse convertToPortalWebResponse( final PortalResponse portalResponse )
    {
        final PortalWebResponse portalWebResponse = new PortalWebResponse();
        portalWebResponse.setStatus( portalResponse.getStatus() );
        portalWebResponse.setContentType( portalResponse.getContentType() );
        portalWebResponse.getHeaders().putAll( portalResponse.getHeaders() );
        portalWebResponse.getCookies().addAll( portalResponse.getCookies() );
        portalWebResponse.setWebSocketConfig( portalResponse.getWebSocket() );
        portalWebResponse.setBody( portalResponse.getBody() );
        portalWebResponse.setPostProcess( portalResponse.isPostProcess() );
        portalWebResponse.setContributions( portalResponse.getContributions() );
        portalWebResponse.setApplyFilters( portalResponse.applyFilters() );
        return portalWebResponse;
    }

    public static class Builder<T extends Builder>
    {
        private PortalWebRequest portalWebRequest;

        private PortalWebResponse portalWebResponse;

        protected Builder()
        {
        }

        public T portalWebRequest( final PortalWebRequest portalWebRequest )
        {
            this.portalWebRequest = portalWebRequest;
            return (T) this;
        }

        public T portalWebResponse( final PortalWebResponse portalWebResponse )
        {
            this.portalWebResponse = portalWebResponse;
            return (T) this;
        }
    }
}

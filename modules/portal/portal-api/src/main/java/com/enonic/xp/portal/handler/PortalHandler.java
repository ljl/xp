package com.enonic.xp.portal.handler;

import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.web.websocket.WebSocketConfig;
import com.enonic.xp.web.websocket.WebSocketEndpoint;

public interface PortalHandler
{
    int getOrder();

    boolean canHandle( PortalRequest req );

    PortalResponse handle( PortalRequest req )
        throws Exception;

    WebSocketEndpoint newWebSocketEndpoint( PortalRequest req, WebSocketConfig config )
        throws Exception;
}

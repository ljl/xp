package com.enonic.xp.admin.impl.portal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.admin.tool.AdminToolDescriptorService;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.page.DescriptorKey;
import com.enonic.xp.portal.controller.ControllerScriptFactory;
import com.enonic.xp.web.handler.BaseWebHandler;
import com.enonic.xp.web.handler.WebHandler;
import com.enonic.xp.web.handler.WebHandlerChain;
import com.enonic.xp.web.handler.WebRequest;
import com.enonic.xp.web.handler.WebResponse;

@Component(immediate = true, service = WebHandler.class)
public final class ToolWebHandler
    extends BaseWebHandler
{
    private final static String ADMIN_TOOL_PREFIX = "/admin/tool/";

    private final static Pattern PATTERN = Pattern.compile( "([^/]+)/([^/]+)" );

    private final static DescriptorKey DEFAULT_DESCRIPTOR_KEY = DescriptorKey.from( "com.enonic.xp.admin.ui:home" );

    private AdminToolDescriptorService adminToolDescriptorService;

    private ControllerScriptFactory controllerScriptFactory;

    @Override
    protected boolean canHandle( final WebRequest webRequest )
    {
        return webRequest.getPath().startsWith( ADMIN_TOOL_PREFIX );
    }

    @Override
    protected WebResponse doHandle( final WebRequest webRequest, final WebResponse webResponse, final WebHandlerChain webHandlerChain )
    {

        final String subPath = webRequest.getPath().substring( ADMIN_TOOL_PREFIX.length() );
        final Matcher matcher = PATTERN.matcher( subPath );

        final DescriptorKey descriptorKey;
        if ( matcher.find() )
        {
            final ApplicationKey applicationKey = ApplicationKey.from( matcher.group( 1 ) );
            final String adminToolName = matcher.group( 2 );
            descriptorKey = DescriptorKey.from( applicationKey, adminToolName );
        }
        else
        {
            descriptorKey = DEFAULT_DESCRIPTOR_KEY;
        }

        return ToolWebHandlerWorker.create().
            webRequest( webRequest ).
            webResponse( webResponse ).
            controllerScriptFactory( controllerScriptFactory ).
            adminToolDescriptorService( adminToolDescriptorService ).
            descriptorKey( descriptorKey ).
            build().
            execute();
    }

    @Reference
    public void setAdminToolDescriptorService( final AdminToolDescriptorService adminToolDescriptorService )
    {
        this.adminToolDescriptorService = adminToolDescriptorService;
    }

    @Reference
    public void setControllerScriptFactory( final ControllerScriptFactory controllerScriptFactory )
    {
        this.controllerScriptFactory = controllerScriptFactory;
    }
}

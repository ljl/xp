package com.enonic.xp.portal.impl.url;

import java.time.Instant;

import org.junit.Before;
import org.mockito.Mockito;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationService;
import com.enonic.xp.branch.BranchId;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.impl.macro.MacroServiceImpl;
import com.enonic.xp.portal.PortalRequest;

public abstract class AbstractPortalUrlServiceImplTest
{
    protected PortalRequest portalRequest;

    protected PortalUrlServiceImpl service;

    protected ContentService contentService;

    protected ApplicationService applicationService;

    @Before
    public void setup()
    {
        final ApplicationKey applicationKey = ApplicationKey.from( "myapplication" );
        final Application application = Mockito.mock( Application.class );
        Mockito.when( application.getKey() ).thenReturn( applicationKey );
        Mockito.when( application.getModifiedTime() ).thenReturn( Instant.MAX );

        this.portalRequest = new PortalRequest();
        this.portalRequest.setBranchId( BranchId.from( "draft" ) );
        this.portalRequest.setApplicationKey( applicationKey );
        this.portalRequest.setBaseUri( "/portal" );
        this.portalRequest.setContentPath( ContentPath.from( "context/path" ) );

        this.service = new PortalUrlServiceImpl();
        this.service.setMacroService( new MacroServiceImpl() );

        this.contentService = Mockito.mock( ContentService.class );
        this.service.setContentService( this.contentService );

        this.applicationService = Mockito.mock( ApplicationService.class );
        Mockito.when( this.applicationService.getInstalledApplication( applicationKey ) ).thenReturn( application );
        this.service.setApplicationService( this.applicationService );
    }
}

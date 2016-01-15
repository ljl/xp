package com.enonic.xp.admin.impl.rest.resource.application;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import com.enonic.xp.admin.impl.json.application.ApplicationJson;
import com.enonic.xp.admin.impl.rest.resource.ResourceConstants;
import com.enonic.xp.admin.impl.rest.resource.application.json.ApplicationInstallParams;
import com.enonic.xp.admin.impl.rest.resource.application.json.ApplicationInstalledJson;
import com.enonic.xp.admin.impl.rest.resource.application.json.ApplicationListParams;
import com.enonic.xp.admin.impl.rest.resource.application.json.ApplicationSuccessJson;
import com.enonic.xp.admin.impl.rest.resource.application.json.ListApplicationJson;
import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.app.ApplicationService;
import com.enonic.xp.app.Applications;
import com.enonic.xp.jaxrs.JaxRsComponent;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.site.SiteDescriptor;
import com.enonic.xp.site.SiteService;
import com.enonic.xp.web.multipart.MultipartForm;
import com.enonic.xp.web.multipart.MultipartItem;

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;

@Path(ResourceConstants.REST_ROOT + "application")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(RoleKeys.ADMIN_LOGIN_ID)
@Component(immediate = true)
public final class ApplicationResource
    implements JaxRsComponent
{
    private ApplicationService applicationService;

    private SiteService siteService;

    @GET
    @Path("list")
    public ListApplicationJson list( @QueryParam("query") final String query )
    {
        Applications applications = this.applicationService.getInstalledApplications();
        if ( StringUtils.isNotBlank( query ) )
        {
            applications = Applications.from( applications.stream().
                filter( ( application ) -> containsIgnoreCase( application.getDisplayName(), query ) ||
                    containsIgnoreCase( application.getMaxSystemVersion(), query ) ||
                    containsIgnoreCase( application.getMinSystemVersion(), query ) ||
                    containsIgnoreCase( application.getSystemVersion(), query ) ||
                    containsIgnoreCase( application.getUrl(), query ) ||
                    containsIgnoreCase( application.getVendorName(), query ) ||
                    containsIgnoreCase( application.getVendorUrl(), query ) ).
                collect( Collectors.toList() ) );
        }

        final ListApplicationJson json = new ListApplicationJson();
        for ( final Application application : applications )
        {
            json.add( application, this.siteService.getDescriptor( application.getKey() ) );
        }

        return json;
    }

    @GET
    public ApplicationJson getByKey( @QueryParam("applicationKey") String applicationKey )
    {
        final Application application = this.applicationService.getInstalledApplication( ApplicationKey.from( applicationKey ) );
        final SiteDescriptor siteDescriptor = this.siteService.getDescriptor( ApplicationKey.from( applicationKey ) );
        return new ApplicationJson( application, siteDescriptor );
    }

    @POST
    @Path("start")
    @Consumes(MediaType.APPLICATION_JSON)
    public ApplicationSuccessJson start( final ApplicationListParams params )
        throws Exception
    {
        params.getKeys().forEach( this.applicationService::startApplication );
        return new ApplicationSuccessJson();
    }

    @POST
    @Path("stop")
    @Consumes(MediaType.APPLICATION_JSON)
    public ApplicationSuccessJson stop( final ApplicationListParams params )
        throws Exception
    {
        params.getKeys().forEach( this.applicationService::stopApplication );
        return new ApplicationSuccessJson();
    }

    @POST
    @Path("install")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ApplicationInstalledJson install( final MultipartForm form )
        throws Exception
    {
        final MultipartItem appFile = form.get( "file" );

        if ( appFile == null )
        {
            throw new RuntimeException( "Missing file item" );
        }

        final ByteSource byteSource = appFile.getBytes();

        final Application application = this.applicationService.installApplication( byteSource );

        return new ApplicationInstalledJson( application );
    }

    @POST
    @Path("installUrl")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ApplicationInstalledJson installUrl( final ApplicationInstallParams params )
        throws Exception
    {
        final String urlString = params.getURL();

        final URL url;
        try
        {
            url = new URL( urlString );
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( "cannot fetch from URL " + urlString, e );
        }

        final InputStream inputStream = url.openStream();

        final Application application =
            this.applicationService.installApplication( ByteSource.wrap( ByteStreams.toByteArray( inputStream ) ) );

        return new ApplicationInstalledJson( application );
    }

    @Reference
    public void setApplicationService( final ApplicationService applicationService )
    {
        this.applicationService = applicationService;
    }

    @Reference
    public void setSiteService( final SiteService siteService )
    {
        this.siteService = siteService;
    }

}


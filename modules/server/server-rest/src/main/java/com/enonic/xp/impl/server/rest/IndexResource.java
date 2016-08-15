package com.enonic.xp.impl.server.rest;

import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.branch.BranchIds;
import com.enonic.xp.impl.server.rest.model.ReindexRequestJson;
import com.enonic.xp.impl.server.rest.model.ReindexResultJson;
import com.enonic.xp.impl.server.rest.model.UpdateIndexSettingsRequestJson;
import com.enonic.xp.impl.server.rest.model.UpdateIndexSettingsResultJson;
import com.enonic.xp.index.IndexService;
import com.enonic.xp.index.ReindexParams;
import com.enonic.xp.index.ReindexResult;
import com.enonic.xp.index.UpdateIndexSettingsParams;
import com.enonic.xp.index.UpdateIndexSettingsResult;
import com.enonic.xp.jaxrs.JaxRsComponent;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.RoleKeys;

@Path("/api/repo/index")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed(RoleKeys.ADMIN_ID)
@Component(immediate = true)
public final class IndexResource
    implements JaxRsComponent
{
    private IndexService indexService;

    @POST
    @Path("reindex")
    public ReindexResultJson reindex( final ReindexRequestJson request )
    {
        final ReindexResult result = this.indexService.reindex( ReindexParams.create().
            setBranchIds( parseBranches( request.branches ) ).
            initialize( request.initialize ).
            repositoryId( parseRepositoryId( request.repository ) ).
            build() );

        return ReindexResultJson.create( result );
    }

    @POST
    @Path("updateSettings")
    public UpdateIndexSettingsResultJson updateSettings( final UpdateIndexSettingsRequestJson request )
    {
        final UpdateIndexSettingsResult result = this.indexService.updateIndexSettings( UpdateIndexSettingsParams.create().
            indexName( request.indexName ).
            settings( request.settings.toString() ).
            build() );

        return UpdateIndexSettingsResultJson.create( result );
    }

    @Reference
    public void setIndexService( final IndexService indexService )
    {
        this.indexService = indexService;
    }

    private static BranchIds parseBranches( final String branches )
    {
        final Iterable<String> split = Splitter.on( "," ).split( branches );
        final Iterable<BranchId> parsed = Lists.newArrayList( split ).stream().map( BranchId::from ).collect( Collectors.toList() );
        return BranchIds.from( parsed );
    }

    private static RepositoryId parseRepositoryId( final String repository )
    {
        return RepositoryId.from( repository );
    }
}

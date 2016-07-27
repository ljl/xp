package com.enonic.xp.lib.content;

import java.util.ArrayList;
import java.util.List;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentIds;
import com.enonic.xp.content.ContentNotFoundException;
import com.enonic.xp.content.ContentPath;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.content.PushContentParams;
import com.enonic.xp.content.PushContentsResult;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.lib.content.mapper.PushContentResultMapper;
import com.enonic.xp.script.bean.BeanContext;
import com.enonic.xp.script.bean.ScriptBean;

public final class PublishContentHandler
    implements ScriptBean
{
    private String[] keys;

    private String targetBranch;

    private String sourceBranch;

    private Boolean includeChildren;

    private Boolean includeDependencies;

    private ContentService contentService;

    public PushContentResultMapper execute()
    {
        final Context context = ContextBuilder.
            from( ContextAccessor.current() ).
            branch( this.sourceBranch ).
            build();

        return context.callWith( this::publishContent );
    }

    private PushContentResultMapper publishContent()
    {
        final List<ContentPath> contentNotFound = new ArrayList<>();
        final List<ContentId> contentIds = new ArrayList<>();

        for ( final String key : this.keys )
        {
            if ( key.startsWith( "/" ) )
            {
                final ContentPath path = ContentPath.from( key );
                final Content content = getByPath( path );
                if ( content != null )
                {
                    contentIds.add( content.getId() );
                }
                else
                {
                    contentNotFound.add( path );
                }
            }
            else
            {
                contentIds.add( ContentId.from( key ) );
            }
        }

        final PushContentParams.Builder builder = PushContentParams.create();
        builder.contentIds( ContentIds.from( contentIds ) );
        builder.target( BranchId.from( targetBranch ) );
        if ( this.includeChildren != null )
        {
            builder.includeChildren( this.includeChildren );
        }
        if ( this.includeDependencies != null )
        {
            builder.includeDependencies( includeDependencies );
        }
        final PushContentsResult result = this.contentService.push( builder.build() );
        return result != null ? new PushContentResultMapper( result, contentNotFound ) : null;
    }

    private Content getByPath( final ContentPath contentPath )
    {
        try
        {
            return this.contentService.getByPath( contentPath );
        }
        catch ( ContentNotFoundException e )
        {
            return null;
        }
    }

    public void setKeys( final String[] keys )
    {
        this.keys = keys;
    }

    public void setTargetBranch( final String targetBranch )
    {
        this.targetBranch = targetBranch;
    }

    public void setSourceBranch( final String sourceBranch )
    {
        this.sourceBranch = sourceBranch;
    }

    public void setIncludeChildren( final Boolean includeChildren )
    {
        this.includeChildren = includeChildren;
    }

    public void setIncludeDependencies( final Boolean includeDependencies )
    {
        this.includeDependencies = includeDependencies;
    }

    @Override
    public void initialize( final BeanContext context )
    {
        this.contentService = context.getService( ContentService.class ).get();
    }
}

package com.enonic.wem.core.content;

import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentConstants;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentNotFoundException;
import com.enonic.wem.api.entity.EntityId;
import com.enonic.wem.api.entity.NoEntityWithIdFoundException;
import com.enonic.wem.api.entity.Node;
import com.enonic.wem.api.util.Exceptions;


final class GetContentByIdCommand
    extends AbstractContentCommand
{
    private final ContentId contentId;

    GetContentByIdCommand( final Builder builder )
    {
        super( builder );
        this.contentId = builder.contentId;
    }

    Content execute()
    {
        final EntityId entityId = EntityId.from( contentId.toString() );

        try
        {
            final Node node = nodeService.getById( entityId, ContentConstants.DEFAULT_CONTEXT );
            return getTranslator().fromNode( node );
        }
        catch ( NoEntityWithIdFoundException e )
        {
            throw new ContentNotFoundException( contentId );
        }
        catch ( Exception e )
        {
            throw Exceptions.newRutime( "Error getting node" ).withCause( e );
        }
    }

    public static Builder create( final ContentId contentId )
    {
        return new Builder( contentId );
    }

    public static class Builder
        extends AbstractContentCommand.Builder<Builder>
    {
        private ContentId contentId;

        public Builder( final ContentId contentId )
        {
            this.contentId = contentId;
        }

        public GetContentByIdCommand build()
        {
            return new GetContentByIdCommand( this );
        }
    }
}


package com.enonic.wem.core.content;

import com.enonic.wem.api.blob.BlobService;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.Contents;
import com.enonic.wem.api.context.Context;
import com.enonic.wem.api.entity.NodeService;
import com.enonic.wem.api.schema.content.ContentTypeService;

final class ChildContentIdsResolver
{
    private final NodeService nodeService;

    private final ContentTypeService contentTypeService;

    private final BlobService blobService;

    private final Context context;

    private ChildContentIdsResolver( final Builder builder )
    {
        this.nodeService = builder.nodeService;
        this.contentTypeService = builder.contentTypeService;
        this.blobService = builder.blobService;
        this.context = builder.context;
    }

    Content resolve( final Content content )
    {
        final Contents children = GetChildContentCommand.create( content.getPath() ).
            nodeService( this.nodeService ).
            contentTypeService( this.contentTypeService ).
            context( this.context ).
            blobService( this.blobService ).
            populateChildIds( true ).
            build().
            execute();

        if ( children.isNotEmpty() )
        {
            return populateWithChildrenIds( content, children );
        }
        else
        {
            return content;
        }
    }

    private Content populateWithChildrenIds( final Content content, final Contents children )
    {
        final Content.Builder builder = Content.newContent( content );
        for ( Content child : children )
        {
            builder.addChildId( child.getId() );
        }

        return builder.build();
    }

    Contents resolve( final Contents contents )
    {
        final Contents.Builder builder = new Contents.Builder();

        for ( final Content content : contents )
        {
            builder.add( resolve( content ) );
        }

        return builder.build();
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
    {
        private NodeService nodeService;

        private ContentTypeService contentTypeService;

        private BlobService blobService;

        private Context context;

        public Builder nodeService( final NodeService nodeService )
        {
            this.nodeService = nodeService;
            return this;
        }

        public Builder contentTypeService( final ContentTypeService contentTypeService )
        {
            this.contentTypeService = contentTypeService;
            return this;
        }

        public Builder blobService( final BlobService blobService )
        {
            this.blobService = blobService;
            return this;
        }

        public Builder context( final Context context )
        {
            this.context = context;
            return this;
        }

        public ChildContentIdsResolver build()
        {
            return new ChildContentIdsResolver( this );
        }
    }


}

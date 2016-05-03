package com.enonic.xp.portal.impl.handler.attachment;

import com.google.common.io.ByteSource;
import com.google.common.net.MediaType;

import com.enonic.xp.attachment.Attachment;
import com.enonic.xp.attachment.Attachments;
import com.enonic.xp.content.Content;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.content.ContentId;
import com.enonic.xp.content.ContentService;
import com.enonic.xp.portal.PortalWebResponse;
import com.enonic.xp.portal.handler.PortalWebHandlerWorker;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.Permission;
import com.enonic.xp.web.HttpStatus;

final class AttachmentWebHandlerWorker
    extends PortalWebHandlerWorker
{
    private final ContentService contentService;

    private final ContentId id;

    private final String name;

    private final boolean download;

    private final boolean cacheable;

    private AttachmentWebHandlerWorker( final Builder builder )
    {
        super( builder );
        contentService = builder.contentService;
        id = builder.id;
        name = builder.name;
        download = builder.download;
        cacheable = builder.cacheable;
    }

    public static Builder create()
    {
        return new Builder();
    }


    @Override
    public PortalWebResponse execute()
    {
        final Content content = getContent( this.id );
        final Attachment attachment = resolveAttachment( content, this.name );
        final ByteSource binary = resolveBinary( this.id, attachment );

        this.portalWebResponse.setStatus( HttpStatus.OK );
        this.portalWebResponse.setContentType( MediaType.parse( attachment.getMimeType() ) );
        this.portalWebResponse.setBody( binary );

        if ( this.download )
        {
            this.portalWebResponse.setHeader( "Content-Disposition", "attachment; filename=" + attachment.getName() );
        }
        if ( this.cacheable )
        {
            final AccessControlEntry publicAccessControlEntry = content.getPermissions().getEntry( RoleKeys.EVERYONE );
            final boolean everyoneCanRead = publicAccessControlEntry != null && publicAccessControlEntry.isAllowed( Permission.READ );
            final boolean masterBranch = ContentConstants.BRANCH_MASTER.equals( this.portalWebRequest.getBranch() );
            setResponseCacheable( everyoneCanRead && masterBranch );
        }
        return this.portalWebResponse;
    }

    private Content getContent( final ContentId contentId )
    {
        final Content content = this.contentService.getById( contentId );
        if ( content == null )
        {
            throw notFound( "Content with id [%s] not found", contentId.toString() );
        }

        return content;
    }

    private ByteSource resolveBinary( final ContentId id, final Attachment attachment )
    {
        final ByteSource binary = this.contentService.getBinary( id, attachment.getBinaryReference() );
        if ( binary == null )
        {
            throw notFound( "Binary [%s] not found for [%s]", attachment.getBinaryReference(), id );
        }

        return binary;
    }

    private Attachment resolveAttachment( final Content content, final String name )
    {
        final Attachments attachments = content.getAttachments();
        final Attachment attachment = attachments.byName( name );
        if ( attachment != null )
        {
            return attachment;
        }

        throw notFound( "Attachment [%s] not found for [%s]", name, content.getPath() );
    }

    public static final class Builder
        extends PortalWebHandlerWorker.Builder<Builder>
    {
        private ContentService contentService;

        private ContentId id;

        private String name;

        private boolean download;

        private boolean cacheable;

        private Builder()
        {
        }

        public Builder contentService( final ContentService contentService )
        {
            this.contentService = contentService;
            return this;
        }

        public Builder id( final ContentId id )
        {
            this.id = id;
            return this;
        }

        public Builder name( final String name )
        {
            this.name = name;
            return this;
        }

        public Builder download( final boolean download )
        {
            this.download = download;
            return this;
        }

        public Builder cacheable( final boolean cacheable )
        {
            this.cacheable = cacheable;
            return this;
        }

        public AttachmentWebHandlerWorker build()
        {
            return new AttachmentWebHandlerWorker( this );
        }
    }
}

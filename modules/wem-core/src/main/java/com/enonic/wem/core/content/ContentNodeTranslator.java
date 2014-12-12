package com.enonic.wem.core.content;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;

import com.google.common.io.ByteSource;

import com.enonic.wem.api.Name;
import com.enonic.wem.api.blob.Blob;
import com.enonic.wem.api.blob.BlobService;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentConstants;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentName;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.Contents;
import com.enonic.wem.api.content.CreateContentParams;
import com.enonic.wem.api.content.attachment.Attachment;
import com.enonic.wem.api.content.attachment.Attachments;
import com.enonic.wem.api.content.thumb.Thumbnail;
import com.enonic.wem.api.data.PropertySet;
import com.enonic.wem.api.data.PropertyTree;
import com.enonic.wem.api.index.IndexConfigDocument;
import com.enonic.wem.api.node.AttachmentPropertyNames;
import com.enonic.wem.api.node.CreateNodeParams;
import com.enonic.wem.api.node.Node;
import com.enonic.wem.api.node.NodeEditor;
import com.enonic.wem.api.node.NodeId;
import com.enonic.wem.api.node.NodeName;
import com.enonic.wem.api.node.NodePath;
import com.enonic.wem.api.node.Nodes;
import com.enonic.wem.api.node.UpdateNodeParams;
import com.enonic.wem.api.schema.content.ContentType;
import com.enonic.wem.api.schema.content.ContentTypeName;
import com.enonic.wem.api.schema.content.ContentTypeService;
import com.enonic.wem.api.schema.content.GetContentTypeParams;
import com.enonic.wem.core.content.serializer.ThumbnailAttachmentSerializer;

public class ContentNodeTranslator
{

    private static final String THUMBNAIL_MIME_TYPE = "image/png";

    private static final ContentAttachmentNodeTranslator CONTENT_ATTACHMENT_NODE_TRANSLATOR = new ContentAttachmentNodeTranslator();

    private final ContentDataSerializer CONTENT_SERIALIZER = new ContentDataSerializer();

    private ContentTypeService contentTypeService;

    private BlobService blobService;

    public CreateNodeParams toCreateNode( final CreateContentParams params )
    {
        if ( params.getName() == null || StringUtils.isEmpty( params.getName().toString() ) )
        {
            params.name( Name.ensureValidName( params.getDisplayName() ) );
        }

        final PropertyTree contentAsData = new PropertyTree();
        CONTENT_SERIALIZER.toData( params, contentAsData.getRoot() );

        processAttachments2( params.getAttachments(), contentAsData );

        final IndexConfigDocument indexConfigDocument = ContentIndexConfigFactory.create();

        final com.enonic.wem.api.node.Attachments.Builder nodeAttachmentsBuilder = processAttachments( params );

        return CreateNodeParams.create().
            name( resolveNodeName( params.getName() ) ).
            parent( resolveParentNodePath( params.getParentContentPath() ) ).
            data( contentAsData ).
            attachments( nodeAttachmentsBuilder.build() ).
            indexConfigDocument( indexConfigDocument ).
            permissions( params.getPermissions() ).
            inheritPermissions( params.isInheritPermissions() ).
            nodeType( ContentConstants.CONTENT_NODE_COLLECTION ).
            build();
    }

    private void processAttachments2( final Attachments attachments, final PropertyTree contentAsData )
    {
        final PropertySet attachementsSet = ContentAttachmentsNodeTranslator2.translate( contentAsData, attachments );
        contentAsData.addSet( AttachmentPropertyNames.ROOT, attachementsSet );
    }

    private com.enonic.wem.api.node.Attachments.Builder processAttachments( final CreateContentParams params )
    {
        final Attachments contentAttachments = params.getAttachments();

        final com.enonic.wem.api.node.Attachments.Builder nodeAttachmentsBuilder = com.enonic.wem.api.node.Attachments.builder().
            addAll( CONTENT_ATTACHMENT_NODE_TRANSLATOR.toNodeAttachments( contentAttachments ) );

        final Thumbnail thumbnail = resolveThumbnailAttachment( params );

        if ( thumbnail != null )
        {
            nodeAttachmentsBuilder.add( ThumbnailAttachmentSerializer.toAttachment( thumbnail ) );
        }
        return nodeAttachmentsBuilder;
    }

    public UpdateNodeParams toUpdateNodeCommand( final Content content, final Attachments attachments )
    {
        return new UpdateNodeParams().
            id( NodeId.from( content.getId() ) ).
            editor( toNodeEditor( content, attachments ) );
    }

    public Contents fromNodes( final Nodes nodes )
    {
        final Contents.Builder contents = Contents.builder();

        for ( final Node node : nodes )
        {
            contents.add( doGetFromNode( node ) );
        }

        return contents.build();
    }

    public Content fromNode( final Node node )
    {
        return doGetFromNode( node );
    }


    private Content doGetFromNode( final Node node )
    {
        final NodePath parentNodePath = node.path().getParentPath();
        final NodePath parentContentPathAsNodePath = parentNodePath.removeFromBeginning( ContentConstants.CONTENT_ROOT_PATH );
        final ContentPath parentContentPath = ContentPath.from( parentContentPathAsNodePath.toString() );

        final com.enonic.wem.api.node.Attachments nodeAttachments = node.attachments();

        final com.enonic.wem.api.node.Attachment thumbnailAttachment =
            nodeAttachments.getAttachment( ThumbnailAttachmentSerializer.THUMB_NAME );

        final Thumbnail thumbnail;

        if ( thumbnailAttachment != null )
        {
            thumbnail = ThumbnailAttachmentSerializer.toThumbnail( thumbnailAttachment );
        }
        else
        {
            thumbnail = null;
        }

        final Content.Builder builder = CONTENT_SERIALIZER.fromData( node.data().getRoot() );

        final Attachments attachments = CONTENT_ATTACHMENT_NODE_TRANSLATOR.toContentAttachments( node.attachments() );

        builder.
            id( ContentId.from( node.id().toString() ) ).
            parentPath( parentContentPath ).
            name( node.name().toString() ).
            createdTime( node.getCreatedTime() ).
            creator( node.getCreator() ).
            modifiedTime( node.getModifiedTime() ).
            modifier( node.getModifier() ).
            hasChildren( node.getHasChildren() ).
            childOrder( node.getChildOrder() ).
            permissions( node.getPermissions() ).
            inheritPermissions( node.inheritsPermissions() ).
            thumbnail( thumbnail ).
            attachments( attachments );

        return builder.build();
    }

    private NodeEditor toNodeEditor( final Content content, final Attachments attachments )
    {
        final PropertyTree data = new PropertyTree();
        CONTENT_SERIALIZER.toData( content, data.getRoot() );

        final IndexConfigDocument indexConfigDocument = ContentIndexConfigFactory.create();

        return editableNode -> {

            final com.enonic.wem.api.node.Attachments contentAttachmentsAsNodeAttachments =
                CONTENT_ATTACHMENT_NODE_TRANSLATOR.toNodeAttachments( attachments );

            processAttachments2( attachments, data );

            final com.enonic.wem.api.node.Attachments.Builder nodeAttachmentsBuilder = com.enonic.wem.api.node.Attachments.builder().
                addAll( contentAttachmentsAsNodeAttachments );

            final com.enonic.wem.api.node.Attachment thumbnailAttachment =
                ThumbnailAttachmentSerializer.toAttachment( content.getThumbnail() );

            if ( thumbnailAttachment != null )
            {
                nodeAttachmentsBuilder.add( thumbnailAttachment );
            }
            editableNode.name = NodeName.from( content.getName().toString() );
            editableNode.attachments = nodeAttachmentsBuilder.build();
            editableNode.indexConfigDocument = indexConfigDocument;
            editableNode.data = data;
            editableNode.permissions = content.getPermissions();
            editableNode.inheritPermissions = content.inheritsPermissions();
        };
    }

    private String resolveNodeName( final ContentName name )
    {
        if ( name instanceof ContentName.Unnamed )
        {
            ContentName.Unnamed unnammed = (ContentName.Unnamed) name;
            if ( !unnammed.hasUniqueness() )
            {
                return ContentName.Unnamed.withUniqueness().toString();
            }
        }
        return name.toString();
    }

    private NodePath resolveParentNodePath( final ContentPath parentContentPath )
    {
        return NodePath.newPath( ContentConstants.CONTENT_ROOT_PATH ).elements( parentContentPath.toString() ).build();
    }

    private Thumbnail resolveThumbnailAttachment( final CreateContentParams params )
    {
        final ContentType contentType = getContentType( params.getContentType() );
        if ( contentType.getSuperType() == null )
        {
            return null;
        }

        if ( contentType.getSuperType().isMedia() )
        {
            Attachment mediaAttachment = params.getAttachment( params.getName().toString() );
            if ( mediaAttachment == null )
            {
                mediaAttachment = params.getAttachments().first();
            }
            if ( mediaAttachment != null )
            {
                return createThumbnail( mediaAttachment );
            }
        }
        return null;
    }

    private Thumbnail createThumbnail( final Attachment origin )
    {
        final Blob originalImage = blobService.get( origin.getBlobKey() );
        final ByteSource source = ThumbnailFactory.resolve( originalImage );
        final Blob thumbnailBlob;
        try (final InputStream stream = source.openStream())
        {
            thumbnailBlob = blobService.create( stream );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to create thumbnail blob for attachment: " + origin.getNameWithoutExtension() +
                                            ( origin.getExtension() == null || origin.getExtension().equals( "" )
                                                ? ""
                                                : "." + origin.getExtension() ), e );
        }
        return Thumbnail.from( thumbnailBlob.getKey(), THUMBNAIL_MIME_TYPE, thumbnailBlob.getLength() );
    }

    private ContentType getContentType( final ContentTypeName contentTypeName )
    {
        return contentTypeService.getByName( new GetContentTypeParams().contentTypeName( contentTypeName ) );
    }

    public void setContentTypeService( final ContentTypeService contentTypeService )
    {
        this.contentTypeService = contentTypeService;
    }

    public void setBlobService( final BlobService blobService )
    {
        this.blobService = blobService;
    }
}

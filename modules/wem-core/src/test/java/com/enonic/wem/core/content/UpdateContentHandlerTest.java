package com.enonic.wem.core.content;


import javax.jcr.Session;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.wem.api.Client;
import com.enonic.wem.api.account.AccountKey;
import com.enonic.wem.api.account.UserKey;
import com.enonic.wem.api.command.content.UpdateContent;
import com.enonic.wem.api.command.content.UpdateContentResult;
import com.enonic.wem.api.command.content.ValidateContentData;
import com.enonic.wem.api.content.Content;
import com.enonic.wem.api.content.ContentId;
import com.enonic.wem.api.content.ContentPath;
import com.enonic.wem.api.content.data.ContentData;
import com.enonic.wem.api.content.data.Property;
import com.enonic.wem.api.content.data.type.ValueTypes;
import com.enonic.wem.api.content.editor.ContentEditors;
import com.enonic.wem.api.schema.content.validator.DataValidationErrors;
import com.enonic.wem.core.command.AbstractCommandHandlerTest;
import com.enonic.wem.core.content.dao.ContentDao;
import com.enonic.wem.core.index.IndexService;
import com.enonic.wem.core.relationship.RelationshipService;

import static junit.framework.Assert.assertEquals;

public class UpdateContentHandlerTest
    extends AbstractCommandHandlerTest
{
    private static final DateTime CREATED_TIME = new DateTime( 2013, 1, 1, 12, 0, 0, 0 );

    private static final DateTime UPDATED_TIME = new DateTime( 2013, 1, 1, 13, 0, 0, 0 );

    private UpdateContentHandler handler;

    private ContentDao contentDao;

    private RelationshipService relationshipService;

    @Before
    public void before()
        throws Exception
    {
        super.client = Mockito.mock( Client.class );
        super.initialize();

        contentDao = Mockito.mock( ContentDao.class );
        relationshipService = Mockito.mock( RelationshipService.class );
        IndexService indexService = Mockito.mock( IndexService.class );

        handler = new UpdateContentHandler();
        handler.setContentDao( contentDao );
        handler.setRelationshipService( relationshipService );
        handler.setIndexService( indexService );

        Mockito.when( super.client.execute( Mockito.isA( ValidateContentData.class ) ) ).thenReturn( DataValidationErrors.empty() );
    }

    @Test
    public void given_content_not_found_when_handle_then_NOT_FOUND_is_returned()
        throws Exception
    {
        // setup
        DateTimeUtils.setCurrentMillisFixed( UPDATED_TIME.getMillis() );

        ContentData existingContentData = new ContentData();
        existingContentData.add( Property.newProperty().name( "myData" ).type( ValueTypes.TEXT ).value( "aaa" ).build() );

        Mockito.when( contentDao.select( Mockito.eq( ContentPath.from( "myContent" ) ), Mockito.any( Session.class ) ) ).thenReturn( null );

        ContentData unchangedContentData = new ContentData();
        unchangedContentData.add( Property.newProperty().name( "myData" ).type( ValueTypes.TEXT ).value( "aaa" ).build() );

        UpdateContent command = new UpdateContent().
            modifier( AccountKey.superUser() ).
            selector( ContentPath.from( "myContent" ) ).
            editor( ContentEditors.setContentData( unchangedContentData ) );

        // exercise
        handler.handle( context, command );

        // verify
        UpdateContentResult result = command.getResult();
        assertEquals( UpdateContentResult.Type.NOT_FOUND, result.getType() );
        Mockito.verify( contentDao, Mockito.times( 0 ) ).update( Mockito.any( Content.class ), Mockito.eq( true ),
                                                                 Mockito.any( Session.class ) );
    }

    @Test
    public void contentDao_update_not_invoked_when_nothing_is_changed()
        throws Exception
    {
        // setup
        DateTimeUtils.setCurrentMillisFixed( UPDATED_TIME.getMillis() );

        ContentData existingContentData = new ContentData();
        existingContentData.add( Property.newProperty().name( "myData" ).type( ValueTypes.TEXT ).value( "aaa" ).build() );

        Content existingContent = createContent( existingContentData );

        Mockito.when( contentDao.select( Mockito.eq( existingContent.getPath() ), Mockito.any( Session.class ) ) ).thenReturn(
            existingContent );

        ContentData unchangedContentData = new ContentData();
        unchangedContentData.add( Property.newProperty().name( "myData" ).type( ValueTypes.TEXT ).value( "aaa" ).build() );

        UpdateContent command = new UpdateContent().
            modifier( AccountKey.superUser() ).
            selector( existingContent.getPath() ).
            editor( ContentEditors.setContentData( unchangedContentData ) );

        // exercise
        handler.handle( context, command );

        // verify
        Mockito.verify( contentDao, Mockito.times( 0 ) ).update( Mockito.any( Content.class ), Mockito.eq( true ),
                                                                 Mockito.any( Session.class ) );
    }

    @Test
    public void modifiedTime_updated_when_something_is_changed()
        throws Exception
    {
        // setup
        DateTimeUtils.setCurrentMillisFixed( UPDATED_TIME.getMillis() );

        ContentData existingContentData = new ContentData();
        existingContentData.add( Property.newProperty().name( "myData" ).type( ValueTypes.TEXT ).value( "aaa" ).build() );

        Content existingContent = createContent( existingContentData );

        Mockito.when( contentDao.select( Mockito.eq( existingContent.getPath() ), Mockito.any( Session.class ) ) ).thenReturn(
            existingContent );

        ContentData changedContentData = new ContentData();
        changedContentData.add( Property.newProperty().name( "myData" ).type( ValueTypes.TEXT ).value( "bbb" ).build() );

        UpdateContent command = new UpdateContent().
            modifier( AccountKey.superUser() ).
            selector( existingContent.getPath() ).
            editor( ContentEditors.setContentData( changedContentData ) );

        // exercise
        handler.handle( context, command );

        // verify
        Content storedContent = Content.newContent( createContent( existingContentData ) ).
            modifiedTime( UPDATED_TIME ).
            modifier( AccountKey.superUser() ).
            contentData( changedContentData ).
            build();
        Mockito.verify( contentDao, Mockito.times( 1 ) ).update( Mockito.refEq( storedContent ), Mockito.eq( true ),
                                                                 Mockito.any( Session.class ) );
    }

    private Content createContent( final ContentData contentData )
    {
        return Content.newContent().
            id( ContentId.from( "1" ) ).
            name( "myContent" ).
            createdTime( CREATED_TIME ).
            displayName( "MyContent" ).
            owner( UserKey.superUser() ).
            contentData( contentData ).
            build();
    }
}

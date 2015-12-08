package com.enonic.xp.lib.content;

import org.junit.Test;
import org.mockito.Mockito;

import com.enonic.xp.content.Content;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.security.acl.Permission;

public class GetPermissionsHandlerTest
    extends BaseContentHandlerTest
{
    @Test
    public void testExample()
    {
        final AccessControlList acl = AccessControlList.create().
            add( AccessControlEntry.create().principal( PrincipalKey.ofAnonymous() ).allow( Permission.READ ).build() ).
            build();

        final Content content = TestDataFixtures.newExampleContentBuilder().
            permissions( acl ).
            inheritPermissions( false ).
            build();
        Mockito.when( this.contentService.getByPath( Mockito.any() ) ).thenReturn( content );

        runScript( "/site/lib/xp/examples/getPermissions.js" );
    }
}

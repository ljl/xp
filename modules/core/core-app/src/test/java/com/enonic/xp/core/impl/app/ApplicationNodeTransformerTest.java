package com.enonic.xp.core.impl.app;

import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Version;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import com.enonic.xp.app.Application;
import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.node.AttachedBinaries;
import com.enonic.xp.node.AttachedBinary;
import com.enonic.xp.node.CreateBinaries;
import com.enonic.xp.node.CreateBinary;
import com.enonic.xp.node.CreateNodeParams;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.util.BinaryReference;

import static org.junit.Assert.*;

public class ApplicationNodeTransformerTest
    extends BundleBasedTest
{
    @Test
    public void binary_reference_added()
        throws Exception
    {
        final Application app = Mockito.mock( Application.class );
        Mockito.when( app.getKey() ).thenReturn( ApplicationKey.from( "myApp" ) );
        Mockito.when( app.getVersion() ).thenReturn( Version.valueOf( "1.0.0" ) );
        Mockito.when( app.getMaxSystemVersion() ).thenReturn( "1.0.0" );
        Mockito.when( app.getMinSystemVersion() ).thenReturn( "1.0.0." );
        Mockito.when( app.getDisplayName() ).thenReturn( "displayName" );

        final ByteSource appSource = ByteSource.wrap( ByteStreams.toByteArray( newBundle( "myBundle", true ).build() ) );

        final CreateNodeParams createNodeParams = ApplicationNodeTransformer.toCreateNodeParams( app, appSource );

        final PropertyTree data = createNodeParams.getData();

        final BinaryReference binaryReference = data.getBinaryReference( ApplicationNodeTransformer.APPLICATION_BINARY_REF );

        assertNotNull( binaryReference );

        final CreateBinary createBinary = createNodeParams.getCreateBinaries().get( binaryReference );

        assertEquals( appSource, createBinary.getByteSource() );
    }


    @Test
    public void app_binary_updated()
        throws Exception
    {

        final PropertyTree data = new PropertyTree();
        final BinaryReference appReference = BinaryReference.from( ApplicationNodeTransformer.APPLICATION_BINARY_REF );
        data.addBinaryReference( ApplicationNodeTransformer.APPLICATION_BINARY_REF, appReference );

        final Node existingNode = Node.create().
            id( NodeId.from( "myNode" ) ).
            parentPath( NodePath.ROOT ).
            name( "myNode" ).
            data( data ).
            attachedBinaries( AttachedBinaries.create().
                add( new AttachedBinary( appReference, "abc" ) ).
                build() ).
            build();

        final Application app = Mockito.mock( Application.class );
        Mockito.when( app.getKey() ).thenReturn( ApplicationKey.from( "myApp" ) );
        Mockito.when( app.getVersion() ).thenReturn( Version.valueOf( "1.0.0" ) );
        Mockito.when( app.getMaxSystemVersion() ).thenReturn( "1.0.0" );
        Mockito.when( app.getMinSystemVersion() ).thenReturn( "1.0.0." );
        Mockito.when( app.getDisplayName() ).thenReturn( "displayName" );

        final ByteSource updatedSource = ByteSource.wrap( ByteStreams.toByteArray( newBundle( "myBundleUpdated", true ).build() ) );
        final UpdateNodeParams updateNodeParams = ApplicationNodeTransformer.toUpdateNodeParams( app, updatedSource, existingNode );

        final CreateBinaries createBinaries = updateNodeParams.getCreateBinaries();

        assertEquals( updatedSource, createBinaries.get( appReference ).getByteSource() );
    }
}
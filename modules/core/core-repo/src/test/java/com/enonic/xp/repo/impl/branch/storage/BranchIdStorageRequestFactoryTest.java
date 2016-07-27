package com.enonic.xp.repo.impl.branch.storage;

import org.junit.Test;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeState;
import com.enonic.xp.node.NodeVersionId;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.storage.StoreRequest;
import com.enonic.xp.repository.RepositoryId;

import static org.junit.Assert.*;

public class BranchIdStorageRequestFactoryTest
{

    @Test
    public void create()
        throws Exception
    {
        final StoreRequest storeRequest = BranchStorageRequestFactory.create( NodeBranchEntry.create().
            nodeId( NodeId.from( "nodeId" ) ).
            nodePath( NodePath.create( "nodePath" ).build() ).
            nodeState( NodeState.DEFAULT ).
            nodeVersionId( NodeVersionId.from( "nodeVersionId" ) ).
            build(), InternalContext.create().
            branch( BranchId.from( "myBranch" ) ).
            repositoryId( RepositoryId.from( "myRepoId" ) ).
            build() );

        assertEquals( storeRequest.getId(), "nodeId" );
    }
}
package com.enonic.xp.repo.impl.branch.storage;

import java.time.Instant;

import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.StorageSettings;
import com.enonic.xp.repo.impl.storage.StaticStorageType;
import com.enonic.xp.repo.impl.storage.StorageData;
import com.enonic.xp.repo.impl.storage.StoreRequest;
import com.enonic.xp.repo.impl.storage.StoreStorageName;

class BranchStorageRequestFactory
{
    public static StoreRequest create( final NodeBranchEntry nodeBranchEntry, final InternalContext context )
    {

        final StorageData data = StorageData.create().
            add( BranchIndexPath.VERSION_ID.getPath(), nodeBranchEntry.getVersionId().toString() ).
            add( BranchIndexPath.BRANCH_NAME.getPath(), context.getBranch().getName() ).
            add( BranchIndexPath.NODE_ID.getPath(), nodeBranchEntry.getNodeId().toString() ).
            add( BranchIndexPath.STATE.getPath(), nodeBranchEntry.getNodeState().value() ).
            add( BranchIndexPath.PATH.getPath(), nodeBranchEntry.getNodePath().toString() ).
            add( BranchIndexPath.TIMESTAMP.getPath(),
                 nodeBranchEntry.getTimestamp() != null ? nodeBranchEntry.getTimestamp() : Instant.now() ).
            build();

        final NodeId nodeId = nodeBranchEntry.getNodeId();

        return StoreRequest.create().
            id( new BranchDocumentId( nodeId, context.getBranch() ).toString() ).
            nodePath( nodeBranchEntry.getNodePath() ).
            forceRefresh( false ).
            settings( StorageSettings.create().
                storageName( StoreStorageName.from( context.getRepositoryId() ) ).
                storageType( StaticStorageType.BRANCH ).
                build() ).
            data( data ).
            //  parent( new NodeVersionDocumentId( nodeId, nodeBranchEntry.getVersionId() ).toString() ).
            //  routing( nodeId.toString() ).
                build();
    }


}

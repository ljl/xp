package com.enonic.xp.repo.impl.storage;

import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.ReturnFields;
import com.enonic.xp.repo.impl.ReturnValues;
import com.enonic.xp.repo.impl.StorageSettings;
import com.enonic.xp.repo.impl.elasticsearch.NodeStoreDocumentFactory;
import com.enonic.xp.repo.impl.elasticsearch.document.IndexDocument;
import com.enonic.xp.repository.RepositoryId;

@Component
public class IndexDataServiceImpl
    implements IndexDataService
{
    private StorageDao storageDao;

    @Override
    public ReturnValues get( final NodeId nodeId, final ReturnFields returnFields, final InternalContext context )
    {
        final GetResult result = storageDao.getById( createGetByIdRequest( nodeId, returnFields, context ) );

        return result.getReturnValues();
    }

    private GetByIdRequest createGetByIdRequest( final NodeId nodeId, final ReturnFields returnFields, final InternalContext context )
    {
        return GetByIdRequest.create().
            storageSettings( StorageSettings.create().
                branch( context.getBranchId() ).
                indexType( IndexType.SEARCH ).
                repositoryId( context.getRepositoryId() ).
                branch( context.getBranchId() ).
                build() ).
            returnFields( returnFields ).
            id( nodeId.toString() ).
            build();
    }

    @Override
    public ReturnValues get( final NodeIds nodeIds, final ReturnFields returnFields, final InternalContext context )
    {
        final GetByIdsRequest getByIdsRequest = new GetByIdsRequest();

        for ( final NodeId nodeId : nodeIds )
        {
            getByIdsRequest.add( createGetByIdRequest( nodeId, returnFields, context ) );
        }

        final GetResults result = storageDao.getByIds( getByIdsRequest );

        final ReturnValues.Builder allResultValues = ReturnValues.create();

        for ( GetResult getResult : result )
        {
            final ReturnValues returnValues = getResult.getReturnValues();

            for ( final String key : returnValues.getReturnValues().keySet() )
            {
                allResultValues.add( key, returnValues.get( key ).getValues() );
            }
        }

        return allResultValues.build();
    }

    @Override
    public void delete( final NodeId nodeId, final InternalContext context )
    {
        this.storageDao.delete( DeleteRequest.create().
            settings( StorageSettings.create().
                indexType( IndexType.BRANCH ).
                repositoryId( context.getRepositoryId() ).
                branch( context.getBranchId() ).
                build() ).
            id( nodeId.toString() ).
            build() );
    }


    @Override
    public void delete( final NodeIds nodeIds, final InternalContext context )
    {
        this.storageDao.delete( DeleteRequests.create().
            settings( StorageSettings.create().
                indexType( IndexType.BRANCH ).
                repositoryId( context.getRepositoryId() ).
                branch( context.getBranchId() ).
                build() ).
            ids( nodeIds.getAsStrings() ).
            build() );
    }

    @Override
    public void store( final Node node, final InternalContext context )
    {
        final Collection<IndexDocument> indexDocuments = NodeStoreDocumentFactory.createBuilder().
            node( node ).
            branch( context.getBranchId() ).
            repositoryId( context.getRepositoryId() ).
            build().
            create();

        this.storageDao.store( indexDocuments );
    }


    @Override
    public void push( final NodeIds nodeIds, final BranchId targetBranchId, final RepositoryId targetRepo, final InternalContext context )
    {
        this.storageDao.copy( CopyRequest.create().
            storageSettings( StorageSettings.create().
                indexType( IndexType.BRANCH ).
                repositoryId( context.getRepositoryId() ).
                branch( context.getBranchId() ).
                build() ).
            nodeIds( nodeIds ).
            targetBranch( targetBranchId ).
            targetRepo( targetRepo ).
            build() );
    }

    @Reference
    public void setStorageDao( final StorageDao storageDao )
    {
        this.storageDao = storageDao;
    }
}

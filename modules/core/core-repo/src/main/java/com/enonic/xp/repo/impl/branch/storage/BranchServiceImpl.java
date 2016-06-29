package com.enonic.xp.repo.impl.branch.storage;

import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.common.Strings;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.Sets;

import com.enonic.xp.data.ValueFactory;
import com.enonic.xp.node.NodeBranchEntries;
import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodePaths;
import com.enonic.xp.query.filter.ValueFilter;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.ReturnFields;
import com.enonic.xp.repo.impl.StorageSettings;
import com.enonic.xp.repo.impl.branch.BranchService;
import com.enonic.xp.repo.impl.branch.search.NodeBranchQuery;
import com.enonic.xp.repo.impl.branch.search.NodeBranchQueryResult;
import com.enonic.xp.repo.impl.branch.search.NodeBranchQueryResultFactory;
import com.enonic.xp.repo.impl.cache.BranchPath;
import com.enonic.xp.repo.impl.cache.PathCache;
import com.enonic.xp.repo.impl.cache.PathCacheImpl;
import com.enonic.xp.repo.impl.search.SearchDao;
import com.enonic.xp.repo.impl.search.SearchRequest;
import com.enonic.xp.repo.impl.search.result.SearchHit;
import com.enonic.xp.repo.impl.search.result.SearchResult;
import com.enonic.xp.repo.impl.storage.DeleteRequests;
import com.enonic.xp.repo.impl.storage.GetByIdRequest;
import com.enonic.xp.repo.impl.storage.GetResult;
import com.enonic.xp.repo.impl.storage.StaticStorageType;
import com.enonic.xp.repo.impl.storage.StorageDao;
import com.enonic.xp.repo.impl.storage.StoreRequest;
import com.enonic.xp.repo.impl.storage.StoreStorageName;

@Component
public class BranchServiceImpl
    implements BranchService
{
    private static final ReturnFields BRANCH_RETURN_FIELDS =
        ReturnFields.from( BranchIndexPath.NODE_ID, BranchIndexPath.VERSION_ID, BranchIndexPath.STATE, BranchIndexPath.PATH,
                           BranchIndexPath.TIMESTAMP, BranchIndexPath.REFERENCES );

    private static final int BATCHED_EXECUTOR_LIMIT = 1000;

    private final PathCache pathCache = new PathCacheImpl();

    private StorageDao storageDao;

    private SearchDao searchDao;

    @Override
    public String store( final NodeBranchEntry nodeBranchEntry, final InternalContext context )
    {
        return doStore( nodeBranchEntry, context );
    }

    private String doStore( final NodeBranchEntry nodeBranchEntry, final InternalContext context )
    {
        final StoreRequest storeRequest = BranchStorageRequestFactory.create( nodeBranchEntry, context );
        final String id = this.storageDao.store( storeRequest );

        doCache( context, nodeBranchEntry.getNodePath(), BranchDocumentId.from( id ) );

        return id;
    }

    @Override
    public String move( final MoveBranchParams moveBranchParams, final InternalContext context )
    {
        final NodeBranchEntry nodeBranchEntry = moveBranchParams.getNodeBranchEntry();

        this.pathCache.evict( createPath( moveBranchParams.getPreviousPath(), context ) );

        return doStore( nodeBranchEntry, context );
    }

    @Override
    public void delete( final NodeId nodeId, final InternalContext context )
    {
        final NodeBranchEntry nodeBranchEntry = doGetById( nodeId, context );

        if ( nodeBranchEntry == null )
        {
            return;
        }

        storageDao.delete( BranchDeleteRequestFactory.create( nodeId, context ) );

        pathCache.evict( createPath( nodeBranchEntry.getNodePath(), context ) );
    }

    @Override
    public void delete( final NodeIds nodeIds, final InternalContext context )
    {
        final NodeBranchEntries nodeBranchEntries = getIgnoreOrder( nodeIds, context );

        nodeBranchEntries.forEach( entry -> pathCache.evict( createPath( entry.getNodePath(), context ) ) );

        storageDao.delete( DeleteRequests.create().
            forceRefresh( false ).
            ids( nodeIds.stream().
                map( nodeId -> new BranchDocumentId( nodeId, context.getBranch() ).toString() ).
                collect( Collectors.toSet() ) ).
            settings( createStorageSettings( context ) ).
            build() );
    }

    @Override
    public NodeBranchEntry get( final NodeId nodeId, final InternalContext context )
    {
        return doGetById( nodeId, context );
    }

    private NodeBranchEntry doGetById( final NodeId nodeId, final InternalContext context )
    {
        final GetByIdRequest getByIdRequest = createGetByIdRequest( nodeId, context );
        final GetResult getResult = this.storageDao.getById( getByIdRequest );

        if ( getResult.isEmpty() )
        {
            return null;
        }

        final NodeBranchEntry nodeBranchEntry = NodeBranchVersionFactory.create( getResult.getReturnValues() );

        return nodeBranchEntry;
    }

    @Override
    public NodeBranchEntries get( final NodeIds nodeIds, final boolean keepOrder, final InternalContext context )
    {
        if ( keepOrder )
        {
            return getKeepOrder( nodeIds, context );
        }

        return getIgnoreOrder( nodeIds, context );
    }

    @Override
    public NodeBranchEntry get( final NodePath nodePath, final InternalContext context )
    {
        return doGetByPathNew( nodePath, context );
    }

    @Override
    public NodeBranchEntries get( final NodePaths nodePaths, final InternalContext context )
    {
        Set<NodeBranchEntry> nodeBranchEntries = Sets.newHashSet();

        for ( final NodePath nodePath : nodePaths )
        {
            final NodeBranchEntry branchVersion = doGetByPathNew( nodePath, context );

            if ( branchVersion != null )
            {
                nodeBranchEntries.add( branchVersion );
            }
        }

        return NodeBranchEntries.from( nodeBranchEntries );
    }

    @Override
    public void cachePath( final NodeId nodeId, final NodePath nodePath, final InternalContext context )
    {
        doCache( context, nodePath, nodeId );
    }

    @Override
    public void evictPath( final NodePath nodePath, final InternalContext context )
    {
        pathCache.evict( new BranchPath( context.getBranch(), nodePath ) );
    }

    private BranchPath createPath( final NodePath nodePath, final InternalContext context )
    {
        return new BranchPath( context.getBranch(), nodePath );
    }


    private NodeBranchEntry doGetByPathNew( final NodePath nodePath, final InternalContext context )
    {
        final String id = this.pathCache.get( new BranchPath( context.getBranch(), nodePath ) );

        if ( id != null )
        {
            final NodeId nodeId = createNodeId( id );
            return doGetById( nodeId, context );
        }

        final NodeBranchQuery query = NodeBranchQuery.create().
            addQueryFilter( ValueFilter.create().
                fieldName( BranchIndexPath.PATH.getPath() ).
                addValue( ValueFactory.newString( nodePath.toString() ) ).build() ).
            addQueryFilter( ValueFilter.create().
                fieldName( BranchIndexPath.BRANCH_NAME.getPath() ).
                addValue( ValueFactory.newString( context.getBranch().getName() ) ).
                build() ).
            size( 1 ).
            build();

        final SearchResult result = this.searchDao.search( SearchRequest.create().
            settings( createStorageSettings( context ) ).
            returnFields( BRANCH_RETURN_FIELDS ).
            acl( context.getPrincipalsKeys() ).
            query( query ).
            build() );

        if ( !result.isEmpty() )
        {
            final SearchHit firstHit = result.getResults().getFirstHit();

            final GetResult getResult = createGetResult( firstHit );

            doCacheResult( context, getResult );

            return NodeBranchVersionFactory.create( getResult.getReturnValues() );
        }

        return null;
    }

    private NodeId createNodeId( final String id )
    {
        final int branchSeparator = id.lastIndexOf( "_" );

        if ( branchSeparator < 0 )
        {
            throw new StorageException( "Invalid BranchNodeId: " + id );
        }

        return NodeId.from( Strings.substring( id, 0, branchSeparator ) );
    }

    private void doCacheResult( final InternalContext context, final GetResult getResult )
    {
        final NodeBranchEntry nodeBranchEntry = NodeBranchVersionFactory.create( getResult.getReturnValues() );

        doCache( context, nodeBranchEntry.getNodePath(), nodeBranchEntry.getNodeId() );
    }

    private void doCache( final InternalContext context, final NodePath nodePath, final NodeId nodeId )
    {
        doCache( context, nodePath, new BranchDocumentId( nodeId, context.getBranch() ) );
    }

    private void doCache( final InternalContext context, final NodePath nodePath, final BranchDocumentId branchDocumentId )
    {
        pathCache.cache( new BranchPath( context.getBranch(), nodePath ), branchDocumentId );
    }

    private NodeBranchEntries getKeepOrder( final NodeIds nodeIds, final InternalContext context )
    {
        final NodeBranchEntries.Builder builder = NodeBranchEntries.create();

        final GetBranchEntriesMethod getBranchEntriesMethod = GetBranchEntriesMethod.create().
            context( context ).
            pathCache( this.pathCache ).
            returnFields( BRANCH_RETURN_FIELDS ).
            storageDao( this.storageDao ).
            build();

        if ( nodeIds.getSize() > BATCHED_EXECUTOR_LIMIT )
        {
            builder.addAll( BatchedBranchEntryExecutor.create().
                nodeIds( nodeIds ).
                method( getBranchEntriesMethod ).
                build().
                execute() );
        }
        else
        {
            getBranchEntriesMethod.execute( nodeIds.getSet(), builder );
        }

        return builder.build();
    }

    private NodeBranchEntries getIgnoreOrder( final NodeIds nodeIds, final InternalContext context )
    {
        final SearchResult results = this.searchDao.search( SearchRequest.create().
            query( NodeBranchQuery.create().
                addQueryFilter( ValueFilter.create().
                    fieldName( BranchIndexPath.BRANCH_NAME.getPath() ).
                    addValue( ValueFactory.newString( context.getBranch().getName() ) ).
                    build() ).
                addQueryFilter( ValueFilter.create().
                    fieldName( BranchIndexPath.NODE_ID.getPath() ).
                    addValues( nodeIds.getAsStrings() ).
                    build() ).
                size( nodeIds.getSize() ).
                build() ).
            returnFields( BRANCH_RETURN_FIELDS ).
            settings( createStorageSettings( context ) ).
            build() );

        final NodeBranchQueryResult nodeBranchEntries = NodeBranchQueryResultFactory.create( results );
        return NodeBranchEntries.from( nodeBranchEntries.getList() );
    }


    private GetResult createGetResult( final SearchHit searchHit )
    {
        return GetResult.create().
            id( searchHit.getId() ).
            version( searchHit.getVersion() ).
            returnValues( searchHit.getReturnValues() ).
            source( searchHit.getSource() ).
            build();
    }

    private GetByIdRequest createGetByIdRequest( final NodeId nodeId, final InternalContext context )
    {
        return GetByIdRequest.create().
            id( new BranchDocumentId( nodeId, context.getBranch() ).toString() ).
            storageSettings( createStorageSettings( context ) ).
            returnFields( BRANCH_RETURN_FIELDS ).
            routing( nodeId.toString() ).
            build();
    }

    private StorageSettings createStorageSettings( final InternalContext context )
    {
        return StorageSettings.create().
            storageName( StoreStorageName.from( context.getRepositoryId() ) ).
            storageType( StaticStorageType.BRANCH ).
            build();
    }

    @Reference
    public void setStorageDao( final StorageDao storageDao )
    {
        this.storageDao = storageDao;
    }

    @Reference
    public void setSearchDao( final SearchDao searchDao )
    {
        this.searchDao = searchDao;
    }
}


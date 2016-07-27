package com.enonic.xp.repo.impl.node;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.mockito.Mockito;

import com.enonic.xp.blob.BlobStore;
import com.enonic.xp.branch.BranchId;
import com.enonic.xp.content.ContentConstants;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.index.PatternIndexConfigDocument;
import com.enonic.xp.node.CreateNodeParams;
import com.enonic.xp.node.CreateRootNodeParams;
import com.enonic.xp.node.FindNodesByParentParams;
import com.enonic.xp.node.FindNodesByParentResult;
import com.enonic.xp.node.FindNodesByQueryResult;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.node.NodePath;
import com.enonic.xp.node.NodeQuery;
import com.enonic.xp.node.Nodes;
import com.enonic.xp.node.PushNodesResult;
import com.enonic.xp.node.UpdateNodeParams;
import com.enonic.xp.query.parser.QueryParser;
import com.enonic.xp.repo.impl.branch.storage.BranchServiceImpl;
import com.enonic.xp.repo.impl.config.RepoConfiguration;
import com.enonic.xp.repo.impl.elasticsearch.AbstractElasticsearchIntegrationTest;
import com.enonic.xp.repo.impl.elasticsearch.IndexServiceInternalImpl;
import com.enonic.xp.repo.impl.elasticsearch.search.SearchDaoImpl;
import com.enonic.xp.repo.impl.elasticsearch.snapshot.SnapshotServiceImpl;
import com.enonic.xp.repo.impl.elasticsearch.storage.StorageDaoImpl;
import com.enonic.xp.repo.impl.node.dao.NodeVersionDaoImpl;
import com.enonic.xp.repo.impl.repository.IndexNameResolver;
import com.enonic.xp.repo.impl.repository.RepositoryInitializer;
import com.enonic.xp.repo.impl.repository.RepositoryServiceImpl;
import com.enonic.xp.repo.impl.search.SearchServiceImpl;
import com.enonic.xp.repo.impl.storage.IndexDataServiceImpl;
import com.enonic.xp.repo.impl.storage.StorageServiceImpl;
import com.enonic.xp.repo.impl.version.VersionServiceImpl;
import com.enonic.xp.repository.Repository;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.PrincipalKey;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.SystemConstants;
import com.enonic.xp.security.User;
import com.enonic.xp.security.UserStoreKey;
import com.enonic.xp.security.acl.AccessControlEntry;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.security.auth.AuthenticationInfo;
import com.enonic.xp.util.Reference;

import static org.junit.Assert.*;

public abstract class AbstractNodeTest
    extends AbstractElasticsearchIntegrationTest
{
    public static final User TEST_DEFAULT_USER =
        User.create().key( PrincipalKey.ofUser( UserStoreKey.system(), "test-user" ) ).login( "test-user" ).build();

    public static final AuthenticationInfo TEST_DEFAULT_USER_AUTHINFO = AuthenticationInfo.create().
        principals( RoleKeys.AUTHENTICATED ).
        user( TEST_DEFAULT_USER ).
        build();

    protected static final BranchId WS_DEFAULT = BranchId.create().
        value( "draft" ).
        build();

    protected static final BranchId WS_OTHER = BranchId.create().
        value( "master" ).
        build();

    protected static final Context CTX_DEFAULT = ContextBuilder.create().
        branch( WS_DEFAULT ).
        repositoryId( TEST_REPO.getId() ).
        authInfo( TEST_DEFAULT_USER_AUTHINFO ).
        build();

    protected static final Context CTX_OTHER = ContextBuilder.create().
        branch( WS_OTHER ).
        repositoryId( TEST_REPO.getId() ).
        authInfo( TEST_DEFAULT_USER_AUTHINFO ).
        build();

    public BlobStore blobStore;

    protected NodeVersionDaoImpl nodeDao;

    protected VersionServiceImpl versionService;

    protected BranchServiceImpl branchService;

    protected IndexServiceInternalImpl indexServiceInternal;

    protected SnapshotServiceImpl snapshotService;

    protected StorageServiceImpl storageService;

    protected SearchServiceImpl searchService;

    protected SearchDaoImpl searchDao;

    protected RepositoryServiceImpl repositoryService;

    protected IndexDataServiceImpl indexedDataService;

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        final RepoConfiguration repoConfig = Mockito.mock( RepoConfiguration.class );
        Mockito.when( repoConfig.getSnapshotsDir() ).thenReturn( new File( this.xpHome.getRoot(), "repo/snapshots" ) );

        System.setProperty( "xp.home", xpHome.getRoot().getPath() );

        ContextAccessor.INSTANCE.set( CTX_DEFAULT );

        this.blobStore = new MemoryBlobStore();

        final StorageDaoImpl storageDao = new StorageDaoImpl();
        storageDao.setClient( this.client );

        this.searchDao = new SearchDaoImpl();
        this.searchDao.setClient( this.client );

        this.indexServiceInternal = new IndexServiceInternalImpl();
        this.indexServiceInternal.setClient( this.client );

        // Branch and version-services

        this.branchService = new BranchServiceImpl();
        this.branchService.setStorageDao( storageDao );
        this.branchService.setSearchDao( this.searchDao );

        this.versionService = new VersionServiceImpl();
        this.versionService.setStorageDao( storageDao );

        // Storage-service

        this.nodeDao = new NodeVersionDaoImpl();
        this.nodeDao.setConfiguration( repoConfig );
        this.nodeDao.setBlobStore( blobStore );

        this.indexedDataService = new IndexDataServiceImpl();
        this.indexedDataService.setStorageDao( storageDao );

        this.storageService = new StorageServiceImpl();
        this.storageService.setVersionService( this.versionService );
        this.storageService.setBranchService( this.branchService );
        this.storageService.setIndexServiceInternal( this.indexServiceInternal );
        this.storageService.setNodeVersionDao( this.nodeDao );
        this.storageService.setIndexDataService( this.indexedDataService );

        // Search-service

        this.searchService = new SearchServiceImpl();
        this.searchService.setSearchDao( this.searchDao );

        this.snapshotService = new SnapshotServiceImpl();
        this.snapshotService.setClient( this.client );
        this.snapshotService.setConfiguration( repoConfig );

        repositoryService = new RepositoryServiceImpl();
        repositoryService.setIndexServiceInternal( this.indexServiceInternal );

        createRepository( TEST_REPO );
        createRepository( SystemConstants.SYSTEM_REPO );
        waitForClusterHealth();
    }

    void createRepository( final Repository repository )
    {
        NodeServiceImpl nodeService = new NodeServiceImpl();
        nodeService.setIndexServiceInternal( indexServiceInternal );
        nodeService.setSnapshotService( this.snapshotService );
        nodeService.setStorageService( this.storageService );

        RepositoryInitializer repositoryInitializer = new RepositoryInitializer( indexServiceInternal, repositoryService );
        repositoryInitializer.initializeRepositories( repository.getId() );

        refresh();
    }

    protected Node createDefaultRootNode()
    {
        final AccessControlList rootPermissions =
            AccessControlList.of( AccessControlEntry.create().principal( TEST_DEFAULT_USER.getKey() ).allowAll().build() );
        final CreateRootNodeParams createRootParams = CreateRootNodeParams.create().permissions( rootPermissions ).
            build();

        return CreateRootNodeCommand.create().
            params( createRootParams ).
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    protected Node updateNode( final UpdateNodeParams updateNodeParams )
    {
        return UpdateNodeCommand.create().
            params( updateNodeParams ).
            indexServiceInternal( this.indexServiceInternal ).
            binaryBlobStore( this.blobStore ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    void createContentRepository()
    {
        createRepository( TEST_REPO );
    }


    protected Node createNode( final CreateNodeParams createNodeParams, final boolean refresh )
    {
        final CreateNodeParams createParamsWithAnalyzer = CreateNodeParams.create( createNodeParams ).
            indexConfigDocument( PatternIndexConfigDocument.create().
                analyzer( ContentConstants.DOCUMENT_INDEX_DEFAULT_ANALYZER ).
                build() ).
            build();

        final Node createdNode = CreateNodeCommand.create().
            indexServiceInternal( this.indexServiceInternal ).
            binaryBlobStore( this.blobStore ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            params( createParamsWithAnalyzer ).
            build().
            execute();

        if ( refresh )
        {
            refresh();
        }

        return createdNode;
    }


    protected Node createNode( final CreateNodeParams createNodeParams )
    {
        return createNode( createNodeParams, true );
    }

    Node getNodeById( final NodeId nodeId )
    {
        return GetNodeByIdCommand.create().
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            id( nodeId ).
            build().
            execute();
    }

    Node getNodeByPath( final NodePath nodePath )
    {
        return GetNodeByPathCommand.create().
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            nodePath( nodePath ).
            build().
            execute();
    }


    FindNodesByParentResult findByParent( final FindNodesByParentParams params )
    {
        return FindNodesByParentCommand.create().
            params( params ).
            indexServiceInternal( indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    public FindNodesByQueryResult doFindByQuery( final NodeQuery query )
    {
        return FindNodesByQueryCommand.create().
            query( query ).
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    protected void printContentRepoIndex()
    {
        printAllIndexContent( IndexNameResolver.resolveIndexName( TEST_REPO.getId(), IndexType.SEARCH ), WS_DEFAULT.getValue() );
    }

    protected void printContentRepoIndex( final RepositoryId repositoryId, final BranchId branchId )
    {
        printAllIndexContent( IndexNameResolver.resolveIndexName( repositoryId, IndexType.SEARCH ), branchId.getValue() );
    }

    protected void printBranchIndex()
    {
        printAllIndexContent( IndexNameResolver.resolveIndexName( CTX_DEFAULT.getRepositoryId(), IndexType.BRANCH ),
                              ContextAccessor.current().getBranch().getValue() );
    }

    protected void printVersionIndex()
    {
        printAllIndexContent( IndexNameResolver.resolveIndexName( CTX_DEFAULT.getRepositoryId(), IndexType.VERSION ),
                              IndexType.VERSION.getName() );
    }

    protected PushNodesResult pushNodes( final BranchId target, final NodeId... nodeIds )
    {
        return doPushNodes( NodeIds.from( Arrays.asList( nodeIds ) ), target );
    }

    protected PushNodesResult pushNodes( final NodeIds nodeIds, final BranchId target )
    {
        return doPushNodes( nodeIds, target );
    }

    private PushNodesResult doPushNodes( final NodeIds nodeIds, final BranchId target )
    {
        return PushNodesCommand.create().
            ids( nodeIds ).
            target( target ).
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    protected NodeIds doDeleteNode( final NodeId nodeId )
    {
        return DeleteNodeByIdCommand.create().
            nodeId( nodeId ).
            indexServiceInternal( this.indexServiceInternal ).
            storageService( this.storageService ).
            searchService( this.searchService ).
            build().
            execute();
    }

    protected void queryAndAssert( final String queryString, final int expected )
    {
        final FindNodesByQueryResult result = doQuery( queryString );

        assertEquals( expected, result.getNodeIds().getSize() );
    }

    protected FindNodesByQueryResult doQuery( final String queryString )
    {
        final NodeQuery query = NodeQuery.create().
            query( QueryParser.parse( queryString ) ).
            build();

        return doFindByQuery( query );
    }

    protected void assertOrder( final FindNodesByQueryResult result, Node... nodes )
    {
        assertEquals( nodes.length, result.getHits() );

        final Iterator<Node> iterator = getNodes( result.getNodeIds() ).iterator();

        for ( final Node node : nodes )
        {
            assertEquals( node.id(), iterator.next().id() );
        }
    }

    protected void assertOrder( final FindNodesByQueryResult result, String... ids )
    {
        assertEquals( ids.length, result.getHits() );

        final Iterator<Node> iterator = getNodes( result.getNodeIds() ).iterator();

        for ( final String id : ids )
        {
            assertEquals( id, iterator.next().id().toString() );
        }
    }

    protected final void createNodes( final Node parent, final int numberOfNodes, final int maxLevels, final int level )
    {
        for ( int i = 0; i < numberOfNodes; i++ )
        {
            final PropertyTree data = new PropertyTree();
            data.addReference( "myRef", new Reference( parent.id() ) );

            final Node node = createNode( CreateNodeParams.create().
                name( "nodeName_" + level + "-" + i ).
                parent( parent.path() ).
                data( data ).
                build(), false );

            if ( level < maxLevels )
            {
                createNodes( node, numberOfNodes, maxLevels, level + 1 );
            }
        }
    }

    protected Node getNode( final NodeId nodeId )
    {
        return GetNodeByIdCommand.create().
            id( nodeId ).
            indexServiceInternal( indexServiceInternal ).
            storageService( storageService ).
            searchService( searchService ).
            build().
            execute();

    }

    protected Nodes getNodes( final NodeIds nodeIds )
    {
        return GetNodesByIdsCommand.create().
            ids( nodeIds ).
            indexServiceInternal( indexServiceInternal ).
            storageService( storageService ).
            searchService( searchService ).
            build().
            execute();

    }
}

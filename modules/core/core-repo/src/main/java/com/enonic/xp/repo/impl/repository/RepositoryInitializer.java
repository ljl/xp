package com.enonic.xp.repo.impl.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.repo.impl.elasticsearch.ClusterHealthStatus;
import com.enonic.xp.repo.impl.elasticsearch.ClusterStatusCode;
import com.enonic.xp.repo.impl.index.IndexServiceInternal;
import com.enonic.xp.repo.impl.index.OldIndexSettings;
import com.enonic.xp.repository.IndexSettingsFactory;
import com.enonic.xp.repository.IndicesSettings;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.repository.RepositorySettings;

public final class RepositoryInitializer
{
    private final static String CLUSTER_HEALTH_TIMEOUT_VALUE = "10s";

    private final static Logger LOG = LoggerFactory.getLogger( RepositoryInitializer.class );

    private final IndexServiceInternal indexServiceInternal;

    private final RepositoryService repositoryService;

    public RepositoryInitializer( final IndexServiceInternal indexServiceInternal, final RepositoryService repoService )
    {
        this.indexServiceInternal = indexServiceInternal;
        this.repositoryService = repoService;
    }

    public void initializeRepositories( final RepositoryId... repositoryIds )
    {
        if ( !checkClusterHealth() )
        {
            throw new RepositoryException( "Unable to initialize repositories" );
        }

        final boolean isMaster = indexServiceInternal.isMaster();

        for ( final RepositoryId repositoryId : repositoryIds )
        {
            if ( !isInitialized( repositoryId ) && isMaster )
            {
                doInitializeRepo( repositoryId );
            }
            else
            {
                waitForInitialized( repositoryId );
            }
        }

        //test();
    }

    public void test()
    {
        final PropertyTree settings = new PropertyTree();
        final PropertySet index = settings.addSet( "index" );
        index.setLong( "number_of_shards", 4L );
        index.setLong( "number_of_replicas", 4L );

        final RepositorySettings repoSettings = RepositorySettings.create().
            name( "myrep-" + System.currentTimeMillis() ).
            indiciesSettings( IndicesSettings.create().
                add( IndexType.VERSION, IndexSettingsFactory.from( "{}" ) ).
                build() ).
            build();

        repositoryService.create( repoSettings );
    }

    private boolean checkClusterHealth()
    {
        try
        {
            final ClusterHealthStatus clusterHealth = indexServiceInternal.getClusterHealth( CLUSTER_HEALTH_TIMEOUT_VALUE );

            if ( clusterHealth.isTimedOut() || clusterHealth.getClusterStatusCode().equals( ClusterStatusCode.RED ) )
            {
                LOG.error( "Cluster not healthy: " + "timed out: " + clusterHealth.isTimedOut() + ", state: " +
                               clusterHealth.getClusterStatusCode() );
                return false;
            }

            return true;
        }
        catch ( Exception e )
        {
            LOG.error( "Failed to get cluster health status", e );
        }

        return false;
    }

    private void doInitializeRepo( final RepositoryId repositoryId )
    {
        LOG.info( "Initializing repositoryId {}", repositoryId );

        createIndexes( repositoryId );

        final String storageIndexName = getStoreIndexName( repositoryId );
        final String searchIndexName = getSearchIndexName( repositoryId );

        indexServiceInternal.applyMapping( storageIndexName, IndexType.BRANCH,
                                           RepositoryIndexMappingProvider.getBranchMapping( repositoryId ) );

        indexServiceInternal.applyMapping( storageIndexName, IndexType.VERSION,
                                           RepositoryIndexMappingProvider.getVersionMapping( repositoryId ) );

        indexServiceInternal.applyMapping( searchIndexName, IndexType.SEARCH,
                                           RepositoryIndexMappingProvider.getSearchMappings( repositoryId ) );

        indexServiceInternal.refresh( storageIndexName, searchIndexName );
    }

    private void waitForInitialized( final RepositoryId repositoryId )
    {
        LOG.info( "Waiting for repository '{}' to be initialized", repositoryId );

        final String storageIndexName = getStoreIndexName( repositoryId );
        final String searchIndexName = getSearchIndexName( repositoryId );

        indexServiceInternal.getClusterHealth( CLUSTER_HEALTH_TIMEOUT_VALUE, storageIndexName, searchIndexName );
    }

    private void createIndexes( final RepositoryId repositoryId )
    {
        createStorageIndex( repositoryId );
        createSearchIndex( repositoryId );
    }

    private void createSearchIndex( final RepositoryId repositoryId )
    {
        LOG.info( "Create search-index for repositoryId {}", repositoryId );
        final String searchIndexName = getSearchIndexName( repositoryId );
        final OldIndexSettings searchOldIndexSettings = RepositorySearchIndexSettingsProvider.getSettings( repositoryId );
        LOG.debug( "Applying search-index settings for repo {}: {}", repositoryId, searchOldIndexSettings.getSettingsAsString() );
        indexServiceInternal.createIndex( searchIndexName, searchOldIndexSettings );
    }

    private void createStorageIndex( final RepositoryId repositoryId )
    {
        LOG.info( "Create storage-index for repositoryId {}", repositoryId );
        final String storageIndexName = getStoreIndexName( repositoryId );
        final OldIndexSettings storageOldIndexSettings = RepositoryStorageSettingsProvider.getSettings( repositoryId );
        LOG.debug( "Applying storage-index settings for repo {}: {}", repositoryId, storageOldIndexSettings.getSettingsAsString() );
        indexServiceInternal.createIndex( storageIndexName, storageOldIndexSettings );
    }

    private boolean isInitialized( final RepositoryId repositoryId )
    {
        final String storageIndexName = getStoreIndexName( repositoryId );
        final String searchIndexName = getSearchIndexName( repositoryId );

        return indexServiceInternal.indicesExists( storageIndexName, searchIndexName );
    }

    private String getStoreIndexName( final RepositoryId repositoryId )
    {
        return IndexNameResolver.resolveStorageIndexName( repositoryId );
    }

    private String getSearchIndexName( final RepositoryId repositoryId )
    {
        return IndexNameResolver.resolveSearchIndexName( repositoryId );
    }

}

package com.enonic.wem.core.elasticsearch;

import java.time.Instant;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.enonic.wem.api.repository.RepositoryId;
import com.enonic.wem.core.elasticsearch.xcontent.VersionXContentBuilderFactory;
import com.enonic.wem.core.entity.FindNodeVersionsResult;
import com.enonic.wem.core.entity.NodeId;
import com.enonic.wem.core.entity.NodeVersion;
import com.enonic.wem.core.entity.NodeVersionId;
import com.enonic.wem.core.entity.NodeVersions;
import com.enonic.wem.core.index.IndexType;
import com.enonic.wem.core.index.result.SearchResult;
import com.enonic.wem.core.index.result.SearchResultEntry;
import com.enonic.wem.core.index.result.SearchResultField;
import com.enonic.wem.core.repository.StorageNameResolver;
import com.enonic.wem.core.version.GetVersionsQuery;
import com.enonic.wem.core.version.NodeVersionDocument;
import com.enonic.wem.core.version.VersionService;

import static com.enonic.wem.core.elasticsearch.xcontent.VersionXContentBuilderFactory.NODE_ID_FIELD_NAME;
import static com.enonic.wem.core.elasticsearch.xcontent.VersionXContentBuilderFactory.NODE_VERSION_ID_FIELD_NAME;
import static com.enonic.wem.core.elasticsearch.xcontent.VersionXContentBuilderFactory.TIMESTAMP_ID_FIELD_NAME;

public class ElasticsearchVersionService
    implements VersionService
{
    private static final boolean DEFAULT_REFRESH = true;

    private ElasticsearchDao elasticsearchDao;

    @Override
    public void store( final NodeVersionDocument nodeVersionDocument, final RepositoryId repositoryId )
    {
        final IndexRequest versionsDocument = Requests.indexRequest().
            index( StorageNameResolver.resolveStorageIndexName( repositoryId ) ).
            type( IndexType.VERSION.getName() ).
            source( VersionXContentBuilderFactory.create( nodeVersionDocument ) ).
            id( nodeVersionDocument.getNodeVersionId().toString() ).
            refresh( DEFAULT_REFRESH );

        elasticsearchDao.store( versionsDocument );
    }

    @Override
    public NodeVersion getVersion( final NodeVersionId nodeVersionId, final RepositoryId repositoryId )
    {
        final SearchResult searchResult = doGetFromVersionId( nodeVersionId, repositoryId );

        final SearchResultEntry searchHit = searchResult.getResults().getFirstHit();

        return createVersionEntry( searchHit );
    }

    private NodeVersion createVersionEntry( final SearchResultEntry hit )
    {
        final String timestamp = getStringValue( hit, TIMESTAMP_ID_FIELD_NAME, true );
        final String versionId = getStringValue( hit, NODE_VERSION_ID_FIELD_NAME, true );

        return new NodeVersion( NodeVersionId.from( versionId ), Instant.parse( timestamp ) );
    }

    @Override
    public FindNodeVersionsResult findVersions( final GetVersionsQuery query, final RepositoryId repositoryId )
    {
        final SearchResult searchResults = doGetFromNodeId( query.getNodeId(), query.getFrom(), query.getSize(), repositoryId );

        final FindNodeVersionsResult.Builder findEntityVersionResultBuilder = FindNodeVersionsResult.create();

        findEntityVersionResultBuilder.hits( searchResults.getResults().getSize() );
        findEntityVersionResultBuilder.totalHits( searchResults.getResults().getTotalHits() );
        findEntityVersionResultBuilder.from( query.getFrom() );
        findEntityVersionResultBuilder.to( query.getSize() );

        final NodeVersions nodeVersions = buildEntityVersions( query, searchResults );

        findEntityVersionResultBuilder.entityVersions( nodeVersions );

        return findEntityVersionResultBuilder.build();
    }

    private NodeVersions buildEntityVersions( final GetVersionsQuery query, final SearchResult searchResults )
    {
        final NodeVersions.Builder entityVersionsBuilder = NodeVersions.create( query.getNodeId() );

        for ( final SearchResultEntry searchResult : searchResults.getResults() )
        {
            entityVersionsBuilder.add( createVersionEntry( searchResult ) );
        }

        return entityVersionsBuilder.build();
    }

    private SearchResult doGetFromNodeId( final NodeId id, final int from, final int size, final RepositoryId repositoryId )
    {
        final TermQueryBuilder nodeIdQuery = new TermQueryBuilder( NODE_ID_FIELD_NAME, id.toString() );

        final QueryProperties queryProperties =
            createQueryMetaData( from, size, repositoryId, TIMESTAMP_ID_FIELD_NAME, NODE_VERSION_ID_FIELD_NAME );

        final SearchResult searchResults = elasticsearchDao.find( queryProperties, nodeIdQuery );

        if ( searchResults.isEmpty() )
        {
            throw new RuntimeException( "Did not find version entry with id: " + id );
        }
        return searchResults;
    }

    private SearchResult doGetFromVersionId( final NodeVersionId nodeVersionId, final RepositoryId repositoryId )
    {
        final TermQueryBuilder blobKeyQuery = new TermQueryBuilder( NODE_VERSION_ID_FIELD_NAME, nodeVersionId.toString() );

        final QueryProperties queryProperties =
            createQueryMetaData( 0, 1, repositoryId, NODE_VERSION_ID_FIELD_NAME, TIMESTAMP_ID_FIELD_NAME );

        final SearchResult searchResult = elasticsearchDao.find( queryProperties, blobKeyQuery );

        if ( searchResult.isEmpty() )
        {
            throw new RuntimeException( "Did not find version entry with blobKey: " + nodeVersionId );
        }
        return searchResult;
    }

    private QueryProperties createQueryMetaData( final int from, final int size, final RepositoryId repositoryId,
                                                 final String... fieldNames )
    {

        final SortBuilder descendingTimestampSort = new FieldSortBuilder( TIMESTAMP_ID_FIELD_NAME ).order( SortOrder.DESC );

        // TODO: Temp fix
        return QueryProperties.create( StorageNameResolver.resolveStorageIndexName( repositoryId ) ).
            indexTypeName( IndexType.VERSION.getName() ).
            addFields( fieldNames ).
            size( size ).
            from( from ).
            addSort( descendingTimestampSort ).
            build();
    }

    private String getStringValue( final SearchResultEntry hit, final String fieldName, final boolean required )
    {
        final SearchResultField field = hit.getField( fieldName, required );

        if ( field == null )
        {
            return null;
        }

        return field.getValue().toString();
    }

    public void setElasticsearchDao( final ElasticsearchDao elasticsearchDao )
    {
        this.elasticsearchDao = elasticsearchDao;
    }
}

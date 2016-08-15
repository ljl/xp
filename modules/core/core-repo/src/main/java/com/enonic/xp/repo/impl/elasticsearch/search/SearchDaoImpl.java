package com.enonic.xp.repo.impl.elasticsearch.search;

import org.elasticsearch.client.Client;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.node.NodeQuery;
import com.enonic.xp.query.Query;
import com.enonic.xp.repo.impl.branch.search.ChangelogQuery;
import com.enonic.xp.repo.impl.branch.search.NodeBranchQuery;
import com.enonic.xp.repo.impl.elasticsearch.executor.SearchExecutor;
import com.enonic.xp.repo.impl.elasticsearch.query.ElasticsearchQuery;
import com.enonic.xp.repo.impl.elasticsearch.query.translator.ChangelogQueryTranslator;
import com.enonic.xp.repo.impl.elasticsearch.query.translator.NodeBranchQueryTranslator;
import com.enonic.xp.repo.impl.elasticsearch.query.translator.NodeQueryTranslator;
import com.enonic.xp.repo.impl.elasticsearch.query.translator.NodeVersionDiffQueryTranslator;
import com.enonic.xp.repo.impl.elasticsearch.query.translator.NodeVersionQueryTranslator;
import com.enonic.xp.repo.impl.search.SearchDao;
import com.enonic.xp.repo.impl.search.SearchRequest;
import com.enonic.xp.repo.impl.search.result.SearchResult;
import com.enonic.xp.repo.impl.version.search.NodeVersionDiffQuery;
import com.enonic.xp.repo.impl.version.search.NodeVersionQuery;

@Component
public class SearchDaoImpl
    implements SearchDao
{
    private final NodeQueryTranslator nodeQueryTranslator = new NodeQueryTranslator();

    private final NodeVersionQueryTranslator nodeVersionQueryTranslator = new NodeVersionQueryTranslator();

    private final NodeVersionDiffQueryTranslator nodeVersionDiffQueryTranslator = new NodeVersionDiffQueryTranslator();

    private final NodeBranchQueryTranslator nodeBranchQueryTranslator = new NodeBranchQueryTranslator();

    private final ChangelogQueryTranslator changelogQueryTranslator = new ChangelogQueryTranslator();

    private Client client;

    @Override
    public SearchResult search( final SearchRequest searchRequest )
    {
        final ElasticsearchQuery esQuery = translateQuery( searchRequest );

        return SearchExecutor.create( this.client ).
            build().
            execute( esQuery );
    }

    private ElasticsearchQuery translateQuery( final SearchRequest searchRequest )
    {
        final Query query = searchRequest.getQuery();

        if ( query instanceof NodeQuery )
        {
            return nodeQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof NodeVersionQuery )
        {
            return nodeVersionQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof NodeVersionDiffQuery )
        {
            return nodeVersionDiffQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof NodeBranchQuery )
        {
            return nodeBranchQueryTranslator.translate( searchRequest );
        }

        if ( query instanceof ChangelogQuery )
        {
            return changelogQueryTranslator.translate( searchRequest );
        }

        throw new UnsupportedOperationException( "Queries of type " + query.getClass() + " not implemented yes" );
    }

    @Reference
    public void setClient( final Client client )
    {
        this.client = client;
    }
}

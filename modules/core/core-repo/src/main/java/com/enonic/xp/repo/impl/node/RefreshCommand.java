package com.enonic.xp.repo.impl.node;

import java.util.List;

import com.google.common.collect.Lists;

import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.node.RefreshMode;
import com.enonic.xp.repo.impl.index.IndexServiceInternal;
import com.enonic.xp.repo.impl.repository.IndexNameResolver;
import com.enonic.xp.repository.RepositoryId;

public class RefreshCommand
{
    private RefreshMode refreshMode;

    private IndexServiceInternal indexServiceInternal;

    private RefreshCommand( Builder builder )
    {
        refreshMode = builder.refreshMode;
        indexServiceInternal = builder.indexServiceInternal;
    }

    public void execute()
    {
        final Context context = ContextAccessor.current();

        final List<String> indices = Lists.newArrayList();

        final RepositoryId repositoryId = context.getRepositoryId();

        if ( refreshMode.equals( RefreshMode.ALL ) )
        {
            indices.add( IndexNameResolver.resolveIndexName( repositoryId, IndexType.SEARCH ) );
            indices.add( IndexNameResolver.resolveIndexName( repositoryId, IndexType.BRANCH ) );
            indices.add( IndexNameResolver.resolveIndexName( repositoryId, IndexType.VERSION ) );
        }
        else if ( refreshMode.equals( RefreshMode.SEARCH ) )
        {
            indices.add( IndexNameResolver.resolveIndexName( repositoryId, IndexType.SEARCH ) );
        }
        else
        {
            indices.add( IndexNameResolver.resolveIndexName( repositoryId, IndexType.BRANCH ) );
            indices.add( IndexNameResolver.resolveIndexName( repositoryId, IndexType.VERSION ) );
        }

        this.indexServiceInternal.refresh( indices.toArray( new String[indices.size()] ) );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private RefreshMode refreshMode;

        private IndexServiceInternal indexServiceInternal;

        private Builder()
        {
        }

        public Builder refreshMode( RefreshMode refreshMode )
        {
            this.refreshMode = refreshMode;
            return this;
        }

        public Builder indexServiceInternal( IndexServiceInternal indexServiceInternal )
        {
            this.indexServiceInternal = indexServiceInternal;
            return this;
        }

        public RefreshCommand build()
        {
            return new RefreshCommand( this );
        }
    }
}

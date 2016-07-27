package com.enonic.xp.index;

import java.util.Set;

import com.google.common.annotations.Beta;
import com.google.common.collect.Sets;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.branch.BranchIds;
import com.enonic.xp.repository.RepositoryId;

@Beta
public class ReindexParams
{
    private final boolean initialize;

    private final RepositoryId repositoryId;

    private final BranchIds branchIds;

    private ReindexParams( Builder builder )
    {
        initialize = builder.initialize;
        repositoryId = builder.repositoryId;
        branchIds = BranchIds.from( builder.branchIds );
    }

    public static Builder create()
    {
        return new Builder();
    }

    public boolean isInitialize()
    {
        return initialize;
    }

    public RepositoryId getRepositoryId()
    {
        return repositoryId;
    }

    public BranchIds getBranchIds()
    {
        return branchIds;
    }


    public static final class Builder
    {
        private final Set<BranchId> branchIds = Sets.newHashSet();

        private boolean initialize;

        private RepositoryId repositoryId;

        private Builder()
        {
        }

        public Builder initialize( final boolean initialize )
        {
            this.initialize = initialize;
            return this;
        }

        public Builder repositoryId( final RepositoryId repositoryId )
        {
            this.repositoryId = repositoryId;
            return this;
        }

        public Builder addBranch( final BranchId branchId )
        {
            this.branchIds.add( branchId );
            return this;
        }

        public Builder setBranchIds( final BranchIds branchIds )
        {
            this.branchIds.addAll( branchIds.getSet() );
            return this;
        }

        public ReindexParams build()
        {
            return new ReindexParams( this );
        }
    }
}



package com.enonic.xp.node;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.support.AbstractImmutableEntitySet;

public class PushNodeEntries
    extends AbstractImmutableEntitySet<PushNodeEntry>
{
    private BranchId targetBranchId;

    private RepositoryId targetRepo;

    private PushNodeEntries( final Builder builder )
    {
        super( ImmutableSet.copyOf( builder.entries ) );
        targetBranchId = builder.targetBranchId;
        targetRepo = builder.targetRepo;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public NodeIds getNodeIds()
    {
        return NodeIds.from( this.set.stream().
            map( ( entry ) -> entry.getNodeBranchEntry().getNodeId() ).
            collect( Collectors.toSet() ) );
    }

    public BranchId getTargetBranchId()
    {
        return targetBranchId;
    }

    public RepositoryId getTargetRepo()
    {
        return targetRepo;
    }

    public static final class Builder
    {
        private Set<PushNodeEntry> entries = Sets.newHashSet();

        private BranchId targetBranchId;

        private RepositoryId targetRepo;

        private Builder()
        {
        }

        public Builder add( final PushNodeEntry val )
        {
            this.entries.add( val );
            return this;
        }

        public Builder addAll( final Collection<PushNodeEntry> values )
        {
            this.entries.addAll( values );
            return this;
        }


        public PushNodeEntries build()
        {
            return new PushNodeEntries( this );
        }

        public Builder targetBranch( final BranchId val )
        {
            targetBranchId = val;
            return this;
        }

        public Builder targetRepo( final RepositoryId val )
        {
            targetRepo = val;
            return this;
        }
    }
}

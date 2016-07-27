package com.enonic.xp.repo.impl.node;

import com.google.common.base.Preconditions;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.content.CompareStatus;
import com.enonic.xp.context.Context;
import com.enonic.xp.node.NodeBranchEntry;
import com.enonic.xp.node.NodeComparison;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.storage.StorageService;

public class AbstractCompareNodeCommand
{
    protected final BranchId target;

    protected final StorageService storageService;

    AbstractCompareNodeCommand( Builder builder )
    {
        target = builder.target;
        this.storageService = builder.storageService;
    }

    NodeComparison doCompareNodeVersions( final Context context, final NodeId nodeId )
    {
        final NodeBranchEntry sourceWsVersion = storageService.getBranchNodeVersion( nodeId, InternalContext.from( context ) );
        final NodeBranchEntry targetWsVersion = storageService.getBranchNodeVersion( nodeId, InternalContext.create( context ).
            branch( this.target ).
            build() );

        final CompareStatus compareStatus = CompareStatusResolver.create().
            source( sourceWsVersion ).
            target( targetWsVersion ).
            storageService( this.storageService ).
            build().
            resolve();

        return new NodeComparison( sourceWsVersion, targetWsVersion, compareStatus );
    }


    public static class Builder<B extends Builder>
    {
        private BranchId target;

        private StorageService storageService;

        @SuppressWarnings("unchecked")
        public B target( final BranchId target )
        {
            this.target = target;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B storageService( final StorageService storageService )
        {
            this.storageService = storageService;
            return (B) this;
        }

        void validate()
        {
            Preconditions.checkNotNull( target );
            Preconditions.checkNotNull( storageService );
        }

        public AbstractCompareNodeCommand build()
        {
            return new AbstractCompareNodeCommand( this );
        }
    }
}

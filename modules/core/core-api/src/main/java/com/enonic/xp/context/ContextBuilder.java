package com.enonic.xp.context;

import java.util.Map;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.auth.AuthenticationInfo;

@Beta
public final class ContextBuilder
{
    private LocalScope localScope;

    private final Map<String, Object> attributes;

    private ContextBuilder()
    {
        this.attributes = Maps.newHashMap();
    }

    public ContextBuilder repositoryId( final String value )
    {
        return repositoryId( RepositoryId.from( value ) );
    }

    public ContextBuilder repositoryId( final RepositoryId value )
    {
        return attribute( value );
    }

    public ContextBuilder branch( final String value )
    {
        return branch( BranchId.from( value ) );
    }

    public ContextBuilder branch( final BranchId value )
    {
        return attribute( value );
    }

    public ContextBuilder authInfo( final AuthenticationInfo value )
    {
        return attribute( value );
    }

    public ContextBuilder attribute( final String key, final Object value )
    {
        this.attributes.put( key, value );
        return this;
    }

    public <T> ContextBuilder attribute( final T value )
    {
        return attribute( value.getClass().getName(), value );
    }

    public Context build()
    {
        if ( this.localScope == null )
        {
            this.localScope = new LocalScopeImpl();
        }

        return new ContextImpl( ImmutableMap.copyOf( this.attributes ), this.localScope );
    }

    public static ContextBuilder create()
    {
        return new ContextBuilder();
    }

    public static ContextBuilder from( final Context parent )
    {
        final ContextBuilder builder = new ContextBuilder();
        builder.localScope = parent.getLocalScope();
        builder.attributes.putAll( parent.getAttributes() );
        return builder;
    }
}

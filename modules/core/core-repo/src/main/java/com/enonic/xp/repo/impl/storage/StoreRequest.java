package com.enonic.xp.repo.impl.storage;

import com.google.common.collect.Multimap;

import com.enonic.xp.node.NodePath;
import com.enonic.xp.repo.impl.StorageSettings;

public class StoreRequest
{
    private final StorageData data;

    private final StorageSettings settings;

    private final boolean forceRefresh;

    private final int timeout;

    private final String id;

    private final NodePath path;

    private StoreRequest( Builder builder )
    {
        this.data = builder.data;
        this.settings = builder.settings;
        this.forceRefresh = builder.forceRefresh;
        this.timeout = builder.timeout;
        this.id = builder.id;
        this.path = builder.path;

    }

    public static Builder create()
    {
        return new Builder();
    }

    public Multimap<String, Object> getEntries()
    {
        return this.data.getValues();
    }

    public StorageSettings getSettings()
    {
        return settings;
    }

    public String getTimeout()
    {
        return timeout + "s";
    }

    public boolean isForceRefresh()
    {
        return forceRefresh;
    }

    public String getId()
    {
        return id;
    }

    private NodePath getPath()
    {
        return path;
    }

    public static final class Builder
    {
        private StorageData data;

        private StorageSettings settings;

        private boolean forceRefresh = false;

        private int timeout = 5;

        private String id;

        private NodePath path;

        private Builder()
        {
        }

        public Builder data( StorageData data )
        {
            this.data = data;
            return this;
        }

        public Builder settings( StorageSettings settings )
        {
            this.settings = settings;
            return this;
        }

        public Builder forceRefresh( boolean forceRefresh )
        {
            this.forceRefresh = forceRefresh;
            return this;
        }

        public Builder timeout( final int timeout )
        {
            this.timeout = timeout;
            return this;
        }

        public Builder id( String id )
        {
            this.id = id;
            return this;
        }

        public Builder nodePath( final NodePath nodePath )
        {
            this.path = nodePath;
            return this;
        }

        public StoreRequest build()
        {
            return new StoreRequest( this );
        }
    }


    @Override
    public String toString()
    {
        return "StoreRequest{" +
            "data=" + data +
            ", settings=" + settings +
            ", forceRefresh=" + forceRefresh +
            ", timeout=" + timeout +
            ", id='" + id + '\'' +
            ", path=" + path +
            '}';
    }
}

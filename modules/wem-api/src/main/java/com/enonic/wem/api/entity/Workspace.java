package com.enonic.wem.api.entity;

public class Workspace
{

    public static final String SEPARATOR = "-";

    public static final String PREFIX = "workspace";

    private final String name;

    public Workspace( final String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String getSearchIndexName()
    {
        return PREFIX + SEPARATOR + name;
    }

    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        final Workspace workspace = (Workspace) o;

        if ( !name.equals( workspace.name ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public String toString()
    {
        return name;
    }
}



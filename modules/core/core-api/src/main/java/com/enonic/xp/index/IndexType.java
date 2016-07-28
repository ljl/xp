package com.enonic.xp.index;

import com.google.common.annotations.Beta;

@Beta
public enum IndexType
{
    SEARCH( true ),
    BRANCH( true ),
    VERSION( false ),
    CHANGELOG( true );

    private final boolean dynamicTypes;

    IndexType( final boolean dynamicTypes )
    {
        this.dynamicTypes = dynamicTypes;
    }

    public boolean isDynamicTypes()
    {
        return dynamicTypes;
    }

    public String getName()
    {
        return this.name().toLowerCase();
    }
}

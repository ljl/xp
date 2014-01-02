package com.enonic.wem.core.entity.dao;

import java.util.List;
import java.util.Map;

import com.enonic.wem.api.entity.EntityId;
import com.enonic.wem.api.entity.NoNodeAtPathFoundException;
import com.enonic.wem.api.entity.NodePath;

class NodeIdByPath
{
    private final Map<NodePath, EntityId> itemIdByPath;

    NodeIdByPath( Map<NodePath, EntityId> itemIdByPath )
    {
        this.itemIdByPath = itemIdByPath;
    }

    boolean pathHasItem( final NodePath path )
    {
        final List<NodePath> parentPaths = path.getParentPaths();
        for ( int i = parentPaths.size() - 1; i >= 0; i-- )
        {
            final NodePath parentPath = parentPaths.get( i );
            if ( !parentPath.isRoot() && !itemIdByPath.containsKey( parentPath ) )
            {
                return false;
            }
        }

        return path.isRoot() || itemIdByPath.containsKey( path );
    }

    void put( final NodePath path, final EntityId id )
    {
        itemIdByPath.put( path, id );
    }

    EntityId get( final NodePath path )
    {
        final EntityId id = itemIdByPath.get( path );
        if ( id == null )
        {
            throw new NoNodeAtPathFoundException( path );
        }
        return id;
    }
}
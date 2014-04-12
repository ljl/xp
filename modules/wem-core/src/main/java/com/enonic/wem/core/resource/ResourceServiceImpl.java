package com.enonic.wem.core.resource;

import javax.inject.Inject;

import com.enonic.wem.api.resource.Resource;
import com.enonic.wem.api.resource.ResourceNotFoundException;
import com.enonic.wem.api.resource.ResourceKey;
import com.enonic.wem.api.resource.ResourceKeys;
import com.enonic.wem.api.resource.ResourceService;
import com.enonic.wem.core.config.SystemConfig;

public final class ResourceServiceImpl
    implements ResourceService
{
    private final ClassLoader classLoader;

    protected final SystemConfig systemConfig;

    @Inject
    public ResourceServiceImpl( final SystemConfig systemConfig )
    {
        this( systemConfig, null );
    }

    public ResourceServiceImpl( final SystemConfig systemConfig, final ClassLoader classLoader )
    {
        this.systemConfig = systemConfig;
        this.classLoader = classLoader != null ? classLoader : this.getClass().getClassLoader();
    }

    @Override
    public boolean hasResource( final ResourceKey key )
    {
        return resolve( key ) != null;
    }

    @Override
    public Resource getResource( final ResourceKey key )
        throws ResourceNotFoundException
    {
        final Resource resource = resolve( key );
        if ( resource != null )
        {
            return resource;
        }

        throw new ResourceNotFoundException( key );
    }

    private ResourceResolver createResolver( final ResourceKey key )
    {
        if ( key.getModule().isSystem() )
        {
            return new SystemResourceResolver().classLoader( this.classLoader );
        }
        else
        {
            return new ModuleResourceResolver().systemConfig( this.systemConfig );
        }
    }

    @Override
    public ResourceKeys getChildren( final ResourceKey parent )
    {
        return createResolver( parent ).getChildren( parent );
    }

    private Resource resolve( final ResourceKey key )
    {
        return createResolver( key ).resolve( key );
    }
}

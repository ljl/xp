package com.enonic.xp.web.vhost.impl.config;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.enonic.xp.security.UserStoreKey;
import com.enonic.xp.web.vhost.impl.mapping.VirtualHostMapping;

public class VirtualHostConfigImplTest
{
    private VirtualHostConfigImpl config;

    @Before
    public void setup()
    {
        this.config = new VirtualHostConfigImpl();
    }

    @Test
    public void testNoConfig()
    {
        Assert.assertEquals( false, this.config.isEnabled() );
        Assert.assertNotNull( this.config.getMappings() );

        final List<VirtualHostMapping> mappings = Lists.newArrayList( this.config.getMappings() );
        Assert.assertEquals( 0, mappings.size() );
    }

    @Test
    public void testLoadedConfig_none()
        throws Exception
    {
        loadConfig( "none" );

        Assert.assertEquals( false, this.config.isEnabled() );
        Assert.assertNotNull( this.config.getMappings() );

        final List<VirtualHostMapping> mappings = Lists.newArrayList( this.config.getMappings() );
        Assert.assertEquals( 0, mappings.size() );
    }

    @Test
    public void testLoadedConfig_simple()
        throws Exception
    {
        loadConfig( "simple" );

        Assert.assertEquals( true, this.config.isEnabled() );

        final List<VirtualHostMapping> mappings = Lists.newArrayList( this.config.getMappings() );

        Assert.assertNotNull( mappings );
        Assert.assertEquals( 1, mappings.size() );

        assertMapping( mappings.get( 0 ), "a", "localhost", "/status", "/full/path/status", null );
    }

    @Test
    public void testLoadedConfig_disable()
        throws Exception
    {
        loadConfig( "disable" );

        Assert.assertFalse( this.config.isEnabled() );
    }

    @Test
    public void testLoadedConfig_complete()
        throws Exception
    {
        loadConfig( "complete" );

        Assert.assertEquals( true, this.config.isEnabled() );

        final List<VirtualHostMapping> mappings = Lists.newArrayList( this.config.getMappings() );

        Assert.assertNotNull( mappings );
        Assert.assertEquals( 3, mappings.size() );

        assertMapping( mappings.get( 1 ), "a", "localhost", "/status/a", "/full/path/status/a", UserStoreKey.system() );
        assertMapping( mappings.get( 0 ), "b", "enonic.com", "/status/b", "/full/path/status/b", UserStoreKey.from( "enonic" ) );
        assertMapping( mappings.get( 2 ), "c", "localhost", "/status/c", "/full/path/status/c", null );
    }

    private void loadConfig( final String name )
        throws Exception
    {
        final String path = "vhost-" + name + ".properties";
        final InputStream in = getClass().getResourceAsStream( path );

        Assert.assertNotNull( "Properties file [" + path + "] not found", in );

        final Properties props = new Properties();
        props.load( in );

        final Map<String, String> map = Maps.fromProperties( props );
        this.config.configure( map );
    }

    private void assertMapping( final VirtualHostMapping mapping, final String name, final String host, final String source,
                                final String target, final UserStoreKey userStoreKey )
    {
        Assert.assertEquals( name, mapping.getName() );
        Assert.assertEquals( host, mapping.getHost() );
        Assert.assertEquals( source, mapping.getSource() );
        Assert.assertEquals( target, mapping.getTarget() );
        Assert.assertEquals( userStoreKey, mapping.getUserStoreKey() );
    }
}

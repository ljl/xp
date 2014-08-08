package com.enonic.wem.portal.script.compiler;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

import com.enonic.wem.portal.script.loader.ScriptSource;

public final class ScriptCompilerImpl
    implements ScriptCompiler
{
    private final ScriptCache cache;

    public ScriptCompilerImpl()
    {
        this.cache = new ScriptCache();
    }

    @Override
    public Script compile( final Context context, final ScriptSource source )
    {
        final String key = source.getName() + "_" + source.getTimestamp();
        final Script script = this.cache.get( key );
        if ( script != null )
        {
            return script;
        }

        final Script compiled = doCompile( context, source );
        this.cache.put( key, compiled );
        return compiled;
    }

    private Script doCompile( final Context context, final ScriptSource source )
    {
        return context.compileString( source.getScriptAsString(), source.getName(), 1, null );
    }
}

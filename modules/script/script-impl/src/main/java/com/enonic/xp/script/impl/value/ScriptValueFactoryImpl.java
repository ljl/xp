package com.enonic.xp.script.impl.value;

import jdk.nashorn.api.scripting.JSObject;

import com.enonic.xp.script.ScriptValue;
import com.enonic.xp.script.impl.util.JavascriptHelper;
import com.enonic.xp.script.impl.util.NashornHelper;

public final class ScriptValueFactoryImpl
    implements ScriptValueFactory
{
    private final JavascriptHelper helper;

    public ScriptValueFactoryImpl( final JavascriptHelper helper )
    {
        this.helper = helper;
    }

    @Override
    public JavascriptHelper getJavascriptHelper()
    {
        return this.helper;
    }

    @Override
    public ScriptValue newValue( final Object value )
    {
        if ( value == null )
        {
            return null;
        }

        if ( NashornHelper.isUndefined( value ) )
        {
            return null;
        }

        if ( value instanceof JSObject )
        {
            return newValue( (JSObject) value );
        }

        return new ScalarScriptValue( value );
    }

    private ScriptValue newValue( final JSObject value )
    {
        if ( NashornHelper.isDateType( value ) )
        {
            return new ScalarScriptValue( NashornHelper.toDate( value ) );
        }

        if ( value.isFunction() )
        {
            return new FunctionScriptValue( this, value );
        }

        if ( value.isArray() )
        {
            return new ArrayScriptValue( this, value );
        }

        return new ObjectScriptValue( this, value );
    }
}

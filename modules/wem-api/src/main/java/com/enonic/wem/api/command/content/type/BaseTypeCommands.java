package com.enonic.wem.api.command.content.type;

public final class BaseTypeCommands
{
    public GetBaseTypes get()
    {
        return new GetBaseTypes();
    }

    public GetBaseTypeTree getTree()
    {
        return new GetBaseTypeTree();
    }
}

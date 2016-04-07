package com.enonic.xp.node;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteSource;

import com.enonic.xp.util.BinaryReference;

@Beta
public class CreateBinary
{
    private final BinaryReference reference;

    private final ByteSource byteSource;

    public CreateBinary( final BinaryReference reference, final ByteSource byteSource )
    {
        this.reference = reference;
        this.byteSource = byteSource;
    }

    public BinaryReference getReference()
    {
        return reference;
    }

    public ByteSource getByteSource()
    {
        return byteSource;
    }
}



package com.enonic.wem.web.rest.rpc.content.type;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.enonic.wem.api.content.type.BaseType;
import com.enonic.wem.api.content.type.BaseTypes;
import com.enonic.wem.api.content.type.ContentType;
import com.enonic.wem.core.content.type.BaseTypeJsonSerializer;
import com.enonic.wem.web.json.JsonResult;
import com.enonic.wem.web.rest.resource.content.ContentTypeImageUriResolver;

final class ListBaseTypesRpcJsonResult
    extends JsonResult
{
    private final BaseTypeJsonSerializer baseTypeSerializer = new BaseTypeJsonSerializer();

    private final BaseTypes baseTypes;

    public ListBaseTypesRpcJsonResult( final BaseTypes baseTypes )
    {
        this.baseTypes = baseTypes;
    }

    @Override
    protected void serialize( final ObjectNode json )
    {
        final ArrayNode contentTypeArray = arrayNode();
        for ( BaseType baseType : baseTypes )
        {
            final JsonNode contentTypeJson = serializeContentType( baseType );
            contentTypeArray.add( contentTypeJson );
        }
        json.put( "baseTypes", contentTypeArray );
    }

    private JsonNode serializeContentType( final BaseType baseType )
    {
        final ObjectNode baseTypeJson = (ObjectNode) baseTypeSerializer.toJson( baseType );
        if ( baseType instanceof ContentType )
        {
            baseTypeJson.put( "iconUrl", ContentTypeImageUriResolver.resolve( ( (ContentType) baseType ).getQualifiedName() ) );
        }
        return baseTypeJson;
    }
}

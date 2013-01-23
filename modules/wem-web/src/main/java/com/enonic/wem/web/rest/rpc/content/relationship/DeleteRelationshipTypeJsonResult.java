package com.enonic.wem.web.rest.rpc.content.relationship;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.enonic.wem.api.command.content.relationship.RelationshipTypeDeletionResult;
import com.enonic.wem.api.content.relationship.QualifiedRelationshipTypeName;
import com.enonic.wem.web.json.JsonResult;

final class DeleteRelationshipTypeJsonResult
    extends JsonResult
{
    private final RelationshipTypeDeletionResult relationshipTypeDeletionResult;

    public DeleteRelationshipTypeJsonResult( final RelationshipTypeDeletionResult RelationshipTypeDeletionResult )
    {
        this.relationshipTypeDeletionResult = RelationshipTypeDeletionResult;
    }

    @Override
    protected void serialize( final ObjectNode json )
    {
        json.put( "success", !relationshipTypeDeletionResult.hasFailures() );
        json.put( "successes", serializeSuccesses( relationshipTypeDeletionResult.successes() ) );
        json.put( "failures", serializeFailures( relationshipTypeDeletionResult.failures() ) );
    }

    private ArrayNode serializeFailures( Iterable<RelationshipTypeDeletionResult.Failure> failures )
    {
        final ArrayNode array = arrayNode();
        for ( RelationshipTypeDeletionResult.Failure failure : failures )
        {
            final ObjectNode objectNode = array.addObject();
            objectNode.put( "qualifiedRelationshipTypeName", failure.relationshipTypeName.toString() );
            objectNode.put( "reason", failure.reason );
        }
        return array;
    }

    private ArrayNode serializeSuccesses( Iterable<QualifiedRelationshipTypeName> successes )
    {
        final ArrayNode array = arrayNode();
        for ( QualifiedRelationshipTypeName success : successes )
        {
            final ObjectNode objectNode = array.addObject();
            objectNode.put( "qualifiedRelationshipTypeName", success.toString() );
        }
        return array;
    }
}


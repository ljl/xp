module api_remote_relationshiptype {

    export interface RelationshipType extends api_remote.Item {
        name:string;
        displayName:string;
        iconUrl:string;
        module:string;
        fromSemantic:string;
        toSemantic:string;
        allowedFromTypes:string[];
        allowedToTypes:string[];
    }
    
    export interface DeleteParams {
        qualifiedRelationshipTypeNames:string[];
    }

    export interface DeleteRelationshipTypeSuccess {
        qualifiedRelationshipTypeName:string;
    }

    export interface DeleteRelationshipTypeFailure {
        qualifiedRelationshipTypeName:string;
        reason:string;
    }

    export interface DeleteResult {
        successes:DeleteRelationshipTypeSuccess[];
        failures:DeleteRelationshipTypeFailure[];
    }

    export interface GetParams {
        qualifiedRelationshipTypeName:string;
        format:string;
    }

    export interface GetResult {
        iconUrl:string;
        relationshipType:RelationshipType;
    }

    export interface CreateOrUpdateParams {
        relationshipType:string;
        iconReference:string;
    }

    export interface CreateOrUpdateResult {
        created:bool;
        updated:bool;
    }

}
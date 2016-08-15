package com.enonic.xp.impl.server.rest.model;

import java.util.Set;

import com.enonic.xp.node.RestoreResult;

public class RestoreResultJson
{
    private Set<String> indices;

    private String name;

    private String message;

    private boolean failed;

    private RestoreResultJson()
    {
    }

    public static RestoreResultJson from( final RestoreResult restoreResult )
    {
        final RestoreResultJson snapshotResultJson = new RestoreResultJson();
        snapshotResultJson.indices = restoreResult.getIndices();
        snapshotResultJson.name = restoreResult.getName();
        snapshotResultJson.message = restoreResult.getMessage();
        snapshotResultJson.failed = restoreResult.isFailed();

        return snapshotResultJson;
    }

    public Set<String> getIndices()
    {
        return indices;
    }

    public String getName()
    {
        return name;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isFailed()
    {
        return failed;
    }
}

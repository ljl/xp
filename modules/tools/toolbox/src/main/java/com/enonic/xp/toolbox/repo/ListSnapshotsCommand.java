package com.enonic.xp.toolbox.repo;

import io.airlift.airline.Command;

@Command(name = "list-snapshots", description = "Returns a list of existing snapshots with name and status.")
public final class ListSnapshotsCommand
    extends RepoCommand
{
    public static final String LIST_SNAPSHOTS_REST_PATH = "/api/repo/snapshot/list";

    @Override
    protected void execute()
        throws Exception
    {
        final String result = getRequest( LIST_SNAPSHOTS_REST_PATH );
        System.out.println( result );
    }
}

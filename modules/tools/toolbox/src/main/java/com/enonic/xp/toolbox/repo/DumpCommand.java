package com.enonic.xp.toolbox.repo;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.airlift.airline.Command;
import io.airlift.airline.Option;

import com.enonic.xp.toolbox.util.JsonHelper;

@Command(name = "dump", description = "Export data from every repository.")
public final class DumpCommand
    extends RepoCommand
{
    public static final String SYSTEM_DUMP_REST_PATH = "/api/system/dump";

    @Option(name = "-t", description = "Dump name.", required = true)
    public String target;

    @Override
    protected void execute()
        throws Exception
    {
        final String result = postRequest( SYSTEM_DUMP_REST_PATH, createJsonRequest() );
        System.out.println( result );
    }

    private ObjectNode createJsonRequest()
    {
        final ObjectNode json = JsonHelper.newObjectNode();
        json.put( "name", this.target );
        return json;
    }
}

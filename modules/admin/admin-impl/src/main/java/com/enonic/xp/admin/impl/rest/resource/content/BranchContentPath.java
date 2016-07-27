package com.enonic.xp.admin.impl.rest.resource.content;


import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.content.ContentPath;

class BranchContentPath
{
    private final static String SEPARATOR = ":";

    private final BranchId branchId;

    private final ContentPath contentPath;

    private BranchContentPath( final BranchId branchId, final ContentPath contentPath )
    {
        this.branchId = branchId;
        this.contentPath = contentPath;
    }

    private static BranchContentPath from( final String branch, final String contentPath )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( branch ), "Branch cannot be empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( contentPath ), "ContentPath cannot be empty" );

        return new BranchContentPath( BranchId.from( branch ), ContentPath.from( contentPath ) );
    }

    static BranchContentPath from( final String repoPath )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( repoPath ) );

        final String[] elements = repoPath.split( Pattern.quote( SEPARATOR ) );

        Preconditions.checkArgument( elements.length == 2, "Not a valid branch content path" );

        return BranchContentPath.from( elements[0], elements[1] );
    }

    BranchId getBranchId()
    {
        return branchId;
    }

    ContentPath getContentPath()
    {
        return contentPath;
    }
}

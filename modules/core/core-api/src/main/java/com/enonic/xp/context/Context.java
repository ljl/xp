package com.enonic.xp.context;

import java.util.concurrent.Callable;

import com.google.common.annotations.Beta;

import com.enonic.xp.branch.BranchId;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.auth.AuthenticationInfo;

@Beta
public interface Context
    extends ScopeAttributes
{
    RepositoryId getRepositoryId();

    BranchId getBranch();

    AuthenticationInfo getAuthInfo();

    void runWith( Runnable runnable );

    <T> T callWith( Callable<T> runnable );

    LocalScope getLocalScope();
}

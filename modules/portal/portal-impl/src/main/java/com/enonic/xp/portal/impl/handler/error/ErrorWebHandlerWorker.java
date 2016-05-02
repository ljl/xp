package com.enonic.xp.portal.impl.handler.error;

import com.enonic.xp.portal.PortalException;
import com.enonic.xp.portal.PortalWebRequest;
import com.enonic.xp.portal.PortalWebResponse;
import com.enonic.xp.portal.handler.PortalWebHandlerWorker;
import com.enonic.xp.web.HttpStatus;

final class ErrorWebHandlerWorker
    extends PortalWebHandlerWorker
{
    private HttpStatus code;

    private String message;

    private ErrorWebHandlerWorker( final Builder builder )
    {
        portalWebRequest = builder.portalWebRequest;
        portalWebResponse = builder.portalWebResponse;
        code = builder.code;
        message = builder.message;
    }

    public static Builder create()
    {
        return new Builder();
    }

    @Override
    public PortalWebResponse execute()
    {
        if ( this.code == null )
        {
            this.code = HttpStatus.NOT_FOUND;
        }

        if ( this.message == null )
        {
            this.message = this.code.getReasonPhrase();
        }

        throw new PortalException( this.code, this.message );
    }

    public static final class Builder
    {
        public PortalWebRequest portalWebRequest;

        public PortalWebResponse portalWebResponse;

        private HttpStatus code;

        private String message;

        private Builder()
        {
        }

        public Builder portalWebRequest( final PortalWebRequest portalWebRequest )
        {
            this.portalWebRequest = portalWebRequest;
            return this;
        }

        public Builder portalWebResponse( final PortalWebResponse portalWebResponse )
        {
            this.portalWebResponse = portalWebResponse;
            return this;
        }

        public Builder code( final HttpStatus code )
        {
            this.code = code;
            return this;
        }

        public Builder message( final String message )
        {
            this.message = message;
            return this;
        }

        public ErrorWebHandlerWorker build()
        {
            return new ErrorWebHandlerWorker( this );
        }
    }
}

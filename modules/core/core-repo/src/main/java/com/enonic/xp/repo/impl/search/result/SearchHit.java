package com.enonic.xp.repo.impl.search.result;

import com.enonic.xp.repo.impl.ReturnValue;
import com.enonic.xp.repo.impl.ReturnValues;
import com.enonic.xp.repo.impl.index.IndexFieldNameNormalizer;

public class SearchHit
{
    private final float score;

    private final String id;

    private final ReturnValues returnValues;

    private final long version;

    private SearchHit( final Builder builder )
    {
        score = builder.score;
        id = builder.id;
        returnValues = builder.returnValues;
        version = builder.version;
    }

    public static Builder create()
    {
        return new Builder();
    }

    public float getScore()
    {
        return score;
    }

    public String getId()
    {
        return id;
    }

    public long getVersion()
    {
        return version;
    }

    public ReturnValue getField( final String fieldName )
    {
        return doGetField( fieldName, false );
    }

    public ReturnValue getField( final String fieldName, final boolean failOnMissing )
    {
        return doGetField( fieldName, failOnMissing );
    }

    public ReturnValues getReturnValues()
    {
        return returnValues;
    }

    private ReturnValue doGetField( final String fieldName, final boolean failOnMissing )
    {
        final String normalizedFieldName = IndexFieldNameNormalizer.normalize( fieldName );

        final ReturnValue returnValue = returnValues.get( normalizedFieldName );

        if ( failOnMissing && returnValue == null )
        {
            throw new RuntimeException( "Expected field " + normalizedFieldName + " in result not found" );
        }

        return returnValue;
    }

    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof SearchHit ) )
        {
            return false;
        }

        final SearchHit that = (SearchHit) o;

        if ( id != null ? !id.equals( that.id ) : that.id != null )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }

    public static class Builder
    {
        private float score = 0;

        private String id;

        private ReturnValues returnValues;

        private long version;

        private Builder()
        {
        }

        public Builder score( final float score )
        {
            this.score = score;
            return this;
        }

        public Builder id( final String id )
        {
            this.id = id;
            return this;
        }

        public Builder returnValues( final ReturnValues returnValues )
        {
            this.returnValues = returnValues;
            return this;
        }

        public Builder version( final long val )
        {
            version = val;
            return this;
        }

        public SearchHit build()
        {
            return new SearchHit( this );
        }
    }
}

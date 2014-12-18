package com.enonic.wem.api.query.aggregation;

import com.enonic.wem.api.util.GeoPoint;

public class GeoDistanceAggregationQuery
    extends AbstractRangeAggregationQuery<DistanceRange>
{
    private final GeoPoint origin;

    private final String unit;

    private GeoDistanceAggregationQuery( final Builder builder )
    {
        super( builder, builder.ranges );
        this.origin = builder.origin;
        this.unit = builder.unit;
    }

    public GeoPoint getOrigin()
    {
        return origin;
    }

    public String getUnit()
    {
        return unit;
    }

    public static Builder create( final String name )
    {
        return new Builder( name );
    }

    public static final class Builder
        extends AbstractRangeAggregationQuery.Builder<Builder, DistanceRange>
    {
        private GeoPoint origin;

        private String unit;

        private Builder( final String name )
        {
            super( name );
        }

        public Builder origin( GeoPoint origin )
        {
            this.origin = origin;
            return this;
        }

        public Builder unit( String unit )
        {
            this.unit = unit;
            return this;
        }

        public GeoDistanceAggregationQuery build()
        {
            return new GeoDistanceAggregationQuery( this );
        }
    }
}
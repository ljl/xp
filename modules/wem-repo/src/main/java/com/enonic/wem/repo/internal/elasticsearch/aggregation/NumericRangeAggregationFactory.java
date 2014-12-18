package com.enonic.wem.repo.internal.elasticsearch.aggregation;

import java.util.Collection;

import org.elasticsearch.search.aggregations.bucket.range.Range;

import com.enonic.wem.api.aggregation.BucketAggregation;
import com.enonic.wem.api.aggregation.Buckets;
import com.enonic.wem.api.aggregation.NumericRangeBucket;

class NumericRangeAggregationFactory
    extends AggregationsFactory
{
    static BucketAggregation create( final Range rangeAggregtaion )
    {
        return BucketAggregation.bucketAggregation( rangeAggregtaion.getName() ).
            buckets( createBuckets( rangeAggregtaion.getBuckets() ) ).
            build();
    }

    private static Buckets createBuckets( final Collection<? extends Range.Bucket> buckets )
    {
        final Buckets.Builder bucketsBuilder = new Buckets.Builder();

        for ( final Range.Bucket bucket : buckets )
        {
            final NumericRangeBucket.Builder builder = NumericRangeBucket.create().
                from( bucket.getFrom() ).
                to( bucket.getTo() ).
                key( bucket.getKey() ).
                docCount( bucket.getDocCount() );

            doAddSubAggregations( bucket, builder );

            bucketsBuilder.add( builder.build() );
        }
        return bucketsBuilder.build();
    }

}
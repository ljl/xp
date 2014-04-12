package com.enonic.wem.core.content.page.image;

import com.enonic.wem.api.content.page.image.ImageDescriptor;
import com.enonic.wem.api.content.page.image.ImageDescriptorKey;
import com.enonic.wem.api.content.page.image.ImageDescriptorNotFoundException;
import com.enonic.wem.api.resource.ResourceNotFoundException;

final class GetImageDescriptorCommand
    extends AbstractGetImageDescriptorCommand<GetImageDescriptorCommand>
{
    private ImageDescriptorKey key;

    public ImageDescriptor execute()
    {
        try
        {
            return getImageDescriptor( this.key );
        }
        catch ( ResourceNotFoundException e )
        {
            throw new ImageDescriptorNotFoundException( this.key, e );
        }
    }

    public GetImageDescriptorCommand key( final ImageDescriptorKey key )
    {
        this.key = key;
        return this;
    }
}

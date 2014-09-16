package com.enonic.wem.api.content.page;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import com.enonic.wem.api.content.page.image.ImageComponentJson;
import com.enonic.wem.api.content.page.layout.LayoutComponentJson;
import com.enonic.wem.api.content.page.part.PartComponentJson;
import com.enonic.wem.api.content.page.text.TextComponentJson;

@SuppressWarnings("UnusedDeclaration")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({@JsonSubTypes.Type(value = ImageComponentJson.class, name = "ImageComponent"),
                  @JsonSubTypes.Type(value = PartComponentJson.class, name = "PartComponent"),
                  @JsonSubTypes.Type(value = LayoutComponentJson.class, name = "LayoutComponent"),
                  @JsonSubTypes.Type(value = TextComponentJson.class, name = "TextComponent")})
public abstract class AbstractPageComponentJson<COMPONENT extends PageComponent>
{
    private final COMPONENT component;

    protected AbstractPageComponentJson( final COMPONENT component )
    {
        this.component = component;
    }

    public String getName()
    {
        return component.getName().toString();
    }

    @JsonIgnore
    public COMPONENT getComponent()
    {
        return this.component;
    }

    public static AbstractPageComponentJson fromPageComponent( final PageComponent component )
    {
        return PageComponentJsonSerializer.toJson( component );
    }
}

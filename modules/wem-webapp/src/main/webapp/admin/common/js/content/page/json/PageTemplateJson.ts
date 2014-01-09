module api.content.page.json{

    export interface PageTemplateJson extends PageTemplateSummaryJson {

        descriptor:PageDescriptorJson;

        regions: api.content.page.region.json.RegionJson[];

        config: api.data.json.DataTypeWrapperJson[];

        canRender: string[];

    }
}
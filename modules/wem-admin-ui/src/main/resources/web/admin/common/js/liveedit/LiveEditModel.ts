module api.liveedit {

    import PropertyTree = api.data.PropertyTree;
    import Content = api.content.Content;
    import Descriptor = api.content.page.Descriptor;
    import DescriptorKey = api.content.page.DescriptorKey;
    import GetPageDescriptorByKeyRequest = api.content.page.GetPageDescriptorByKeyRequest;
    import PageModel = api.content.page.PageModel;
    import PageRegionsBuilder = api.content.page.PageRegionsBuilder;
    import PageMode = api.content.page.PageMode;
    import PageTemplate = api.content.page.PageTemplate;
    import PageDescriptor = api.content.page.PageDescriptor;
    import PageTemplateKey = api.content.page.PageTemplateKey;
    import GetPageTemplateByKeyRequest = api.content.page.GetPageTemplateByKeyRequest;
    import SiteModel = api.content.site.SiteModel;

    export class LiveEditModel {

        private content: Content;

        private siteModel: SiteModel;

        private pageModel: PageModel;

        constructor(siteModel: SiteModel) {
            this.siteModel = siteModel;
        }

        init(value: Content, defaultTemplate: PageTemplate, defaultTemplateDescriptor: PageDescriptor): wemQ.Promise<void> {

            this.content = value;

            return this.initPageModel(defaultTemplate, defaultTemplateDescriptor).then((pageModel: PageModel) => {

                this.pageModel = pageModel;
            });
        }

        private initPageModel(defaultPageTemplate: PageTemplate, defaultTemplateDescriptor: PageDescriptor): Q.Promise<PageModel> {

            var deferred = wemQ.defer<PageModel>();

            var pageModel = new PageModel(this, defaultPageTemplate, defaultTemplateDescriptor);
            var pageMode = this.content.getPageMode();

            var pageDescriptorPromise: wemQ.Promise<PageDescriptor> = null;
            var pageTemplatePromise: wemQ.Promise<PageTemplate> = null;

            if (this.content.isPageTemplate()) {

                if (pageMode == PageMode.FORCED_CONTROLLER) {

                    pageDescriptorPromise = this.loadPageDescriptor(this.content.getPage().getController());
                    pageDescriptorPromise.then((pageDescriptor: PageDescriptor) => {
                        pageModel.setController(pageDescriptor, this);
                        pageModel.setRegions(this.content.getPage().getRegions().clone(), this);
                        pageModel.setConfig(this.content.getPage().getConfig().copy(), this);
                    });
                }
                else if (pageMode == PageMode.NO_CONTROLLER) {

                    pageModel.setController(null, this);
                    pageModel.setRegions(new PageRegionsBuilder().build(), this);
                    pageModel.setConfig(new PropertyTree(api.Client.get().getPropertyIdProvider()), this);
                }
                else {
                    throw new Error("Unsupported PageMode for a PageTemplate: " + pageMode);
                }
            }
            else {
                if (pageMode == PageMode.FORCED_TEMPLATE) {

                    pageTemplatePromise = this.loadPageTemplate(this.content.getPage().getTemplate());
                    pageTemplatePromise.then((pageTemplate: PageTemplate) => {
                        pageDescriptorPromise = this.loadPageDescriptor(pageTemplate.getController());
                        pageDescriptorPromise.then((pageDescriptor: PageDescriptor) => {
                            pageModel.setTemplate(pageTemplate, pageDescriptor, this);
                            pageModel.setRegions(pageTemplate.getRegions().clone(), this);
                            pageModel.setConfig(pageTemplate.getConfig().copy(), this);
                        });
                    });
                }
                else if (pageMode == PageMode.AUTOMATIC) {
                    pageModel.setTemplate(null, defaultTemplateDescriptor, this);
                    pageModel.setRegions(defaultPageTemplate.getRegions().clone(), this);
                    pageModel.setConfig(defaultPageTemplate.getConfig().copy(), this);
                }
                else {
                    throw new Error("Unsupported PageMode for a Content: " + pageMode);
                }
            }

            var promises: wemQ.Promise<any>[] = [];
            if (pageDescriptorPromise) {
                promises.push(pageDescriptorPromise);
            }
            if (pageTemplatePromise) {
                promises.push(pageTemplatePromise);
            }
            if (promises.length > 0) {
                wemQ.all(promises).then(() => {

                    deferred.resolve(pageModel);

                }).catch((reason: any) => {
                    api.DefaultErrorHandler.handle(reason);
                }).done();
            }
            else {
                deferred.resolve(pageModel);
            }

            return deferred.promise;
        }

        private loadPageTemplate(key: PageTemplateKey): wemQ.Promise<PageTemplate> {
            return new GetPageTemplateByKeyRequest(key).sendAndParse();
        }

        private loadPageDescriptor(key: DescriptorKey): wemQ.Promise<PageDescriptor> {
            return new GetPageDescriptorByKeyRequest(key).sendAndParse();
        }

        setContent(value: Content) {
            this.content = value;
        }

        getContent(): Content {
            return this.content;
        }

        getSiteModel(): SiteModel {
            return this.siteModel;
        }

        getPageModel(): PageModel {
            return this.pageModel;
        }
    }
}
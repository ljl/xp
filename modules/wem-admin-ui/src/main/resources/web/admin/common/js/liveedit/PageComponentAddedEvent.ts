module api.liveedit {

    import Event2 = api.event.Event2;
    import ComponentPath = api.content.page.ComponentPath;
    import PageComponentType = api.content.page.PageComponentType;
    import RegionPath = api.content.page.RegionPath;
    import PageComponent = api.content.page.PageComponent;

    export class PageComponentAddedEvent extends Event2 {

        private pageComponentView: PageComponentView<PageComponent>;

        private type: PageComponentType;

        private region: RegionPath;

        private precedingComponent: ComponentPath;

        setPageComponentView(pageComponentView: PageComponentView<PageComponent>): PageComponentAddedEvent {
            this.pageComponentView = pageComponentView;
            this.type = pageComponentView.getType().toPageComponentType();
            return this;
        }

        setRegion(region: RegionPath): PageComponentAddedEvent {
            this.region = region;
            return this;
        }

        setPrecedingComponent(precedingComponent: ComponentPath): PageComponentAddedEvent {
            this.precedingComponent = precedingComponent;
            return this;
        }

        getPageComponentView(): PageComponentView<PageComponent> {
            return this.pageComponentView;
        }

        getType(): PageComponentType {
            return this.type;
        }

        getRegion(): RegionPath {
            return this.region;
        }

        getPrecedingComponent(): ComponentPath {
            return this.precedingComponent;
        }

        static on(handler: (event: PageComponentAddedEvent) => void, contextWindow: Window = window) {
            Event2.bind(api.util.getFullName(this), handler, contextWindow);
        }

        static un(handler?: (event: PageComponentAddedEvent) => void, contextWindow: Window = window) {
            Event2.unbind(api.util.getFullName(this), handler, contextWindow);
        }
    }
}
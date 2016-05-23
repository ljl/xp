module api.content {

    export class UnpublishContentRequest extends ContentResourceRequest<UnpublishContentResult, any> {

        private ids: ContentId[] = [];

        private includeChildren: boolean;

        constructor(contentId?: ContentId) {
            super();
            this.setHeavyOperation(true);
            super.setMethod("POST");
            if (contentId) {
                this.addId(contentId);
            }
        }

        setIds(contentIds: ContentId[]): UnpublishContentRequest {
            this.ids = contentIds;
            return this;
        }

        addId(contentId: ContentId): UnpublishContentRequest {
            this.ids.push(contentId);
            return this;
        }

        setIncludeChildren(include: boolean): UnpublishContentRequest {
            this.includeChildren = include;
            return this;
        }

        getParams(): Object {
            return {
                includeChildren: this.includeChildren,
                ids: this.ids.map((el) => {
                    return el.toString();
                })
            };
        }

        getRequestPath(): api.rest.Path {
            return api.rest.Path.fromParent(super.getResourcePath(), "unpublish");
        }

        static feedback(jsonResponse: api.rest.JsonResponse<UnpublishContentResult>) {

            var result = jsonResponse.getResult(),
                total = result.successes.length;

            switch (total) {
            case 0:
                api.notify.showFeedback('Nothing to unpublish.');
                break;
            case 1:
                if (total === 1) {
                    api.notify.showFeedback(`"${result.successes[0].name}" was unpublished`);
                }
                break;
            default: // > 1
                if (total > 0) {
                    api.notify.showFeedback(`${total} items were unpublished`);
                }
            }
        }
    }
}
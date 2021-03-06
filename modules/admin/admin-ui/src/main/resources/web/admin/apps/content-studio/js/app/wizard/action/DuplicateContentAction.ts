import "../../../api.ts";

export class DuplicateContentAction extends api.ui.Action {

    constructor(wizardPanel: api.app.wizard.WizardPanel<api.content.Content>) {
        super("Duplicate");
        this.onExecuted(() => {
            var source = wizardPanel.getPersistedItem();
            new api.content.resource.DuplicateContentRequest(source.getContentId()).sendAndParse().then((content: api.content.Content) => {
                var summaryAndStatus = api.content.ContentSummaryAndCompareStatus.fromContentSummary(content);
                new api.content.event.EditContentEvent([summaryAndStatus]).fire();
                api.notify.showFeedback('\"' + source.getDisplayName() + '\" duplicated');
            })
        });
    }
}

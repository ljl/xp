import "../../api.ts";
import {PrincipalDescriptionWizardStepForm} from "./PrincipalDescriptionWizardStepForm";
import {PrincipalWizardPanel} from "./PrincipalWizardPanel";
import {PrincipalWizardPanelParams} from "./PrincipalWizardPanelParams";
import {PrincipalMembersWizardStepForm} from "./PrincipalMembersWizardStepForm";

import Principal = api.security.Principal;

import ConfirmationDialog = api.ui.dialog.ConfirmationDialog;
import WizardStep = api.app.wizard.WizardStep;

export class GroupRoleWizardPanel extends PrincipalWizardPanel {

    private descriptionWizardStepForm: PrincipalDescriptionWizardStepForm;
    private membersWizardStepForm: PrincipalMembersWizardStepForm;

    constructor(membersWizardStepForm: PrincipalMembersWizardStepForm, params: PrincipalWizardPanelParams) {

        this.descriptionWizardStepForm = new PrincipalDescriptionWizardStepForm();
        this.membersWizardStepForm = membersWizardStepForm;

        super(params);

        this.addClass("group-role-wizard-panel");
    }

    getDescriptionWizardStepForm(): PrincipalDescriptionWizardStepForm {
        return this.descriptionWizardStepForm;
    }

    getMembersWizardStepForm(): PrincipalMembersWizardStepForm {
        return this.membersWizardStepForm;
    }

    doLayout(persistedPrincipal: Principal): wemQ.Promise<void> {

        return super.doLayout(persistedPrincipal).then(() => {

            if (this.isRendered()) {

                var viewedPrincipal = this.assembleViewedItem();
                if (!this.isPersistedEqualsViewed()) {

                    console.warn("Received Principal from server differs from what's viewed:");
                    console.warn(" viewedPrincipal: ", viewedPrincipal);
                    console.warn(" persistedPrincipal: ", persistedPrincipal);

                    ConfirmationDialog.get().setQuestion(
                        "Received Principal from server differs from what you have. Would you like to load changes from server?").setYesCallback(
                        () => this.doLayoutPersistedItem(persistedPrincipal ? persistedPrincipal.clone() : null)).setNoCallback(
                        () => {/* Do nothing */
                        }).show();
                }

                return wemQ<void>(null);
            } else {
                return this.doLayoutPersistedItem(persistedPrincipal ? persistedPrincipal.clone() : null);
            }

        });
    }

    protected doLayoutPersistedItem(principal: Principal): wemQ.Promise<void> {

        return super.doLayoutPersistedItem(principal).then(() => {
            if (!!principal) {
                this.getDescriptionWizardStepForm().layout(principal);
                this.getMembersWizardStepForm().layout(principal);
            }
        });
    }

    hasUnsavedChanges(): boolean {
        var persistedPrincipal = this.getPersistedItem();
        var wizardHeader = this.getWizardHeader();
        if (persistedPrincipal == undefined) {
            return wizardHeader.getName() !== "" ||
                   wizardHeader.getDisplayName() !== "" ||
                   this.membersWizardStepForm.getMembers().length !== 0;
        } else {
            return !this.isPersistedEqualsViewed();
        }
    }

    isPersistedEqualsViewed(): boolean {
        throw new Error("Must be implemented by inheritors");
    }

    show() {
        setTimeout(() => {
            super.show();
        }, 0);

    }
}

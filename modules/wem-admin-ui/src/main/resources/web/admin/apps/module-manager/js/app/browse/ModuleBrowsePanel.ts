module app.browse {

    import ModuleSummary = api.module.ModuleSummary;
    import TreeNode = api.ui.treegrid.TreeNode;

    export class ModuleBrowsePanel extends api.app.browse.BrowsePanel<api.module.ModuleSummary> {

        private browseActions: app.browse.ModuleBrowseActions;

        private moduleTreeGridPanel: ModuleTreeGrid;

        private toolbar: ModuleBrowseToolbar;

        constructor() {
            var treeGridContextMenu = new app.browse.ModuleTreeGridContextMenu();
            this.moduleTreeGridPanel = new ModuleTreeGrid(); // TODO add contextMenu

            this.browseActions = ModuleBrowseActions.init(this.moduleTreeGridPanel);
            treeGridContextMenu.setActions(this.browseActions);

            this.toolbar = new ModuleBrowseToolbar(this.browseActions);
            var browseItemPanel = components.detailPanel = new ModuleBrowseItemPanel();

            super({
                browseToolbar: this.toolbar,
                treeGridPanel2: this.moduleTreeGridPanel,
                browseItemPanel: browseItemPanel,
                filterPanel: undefined
            });

            this.moduleTreeGridPanel.onRowSelectionChanged((selectedRows: TreeNode<ModuleSummary>[]) => {
                this.browseActions.updateActionsEnabledState(<any[]>selectedRows.map((elem) => {
                    return elem.getData();
                }));
            });

            this.registerEvents();
        }

        private registerEvents() {
            app.browse.StopModuleEvent.on((event: app.browse.StopModuleEvent) => {
                var moduleKeys: string[] = event.getModules().map<string>((mod: ModuleSummary) => {
                    return mod.getModuleKey().toString();
                });
                new api.module.StopModuleRequest(moduleKeys).sendAndParse()
                    .then(() => {
                        this.moduleTreeGridPanel.reload();
                    }).done();
            });
            app.browse.StartModuleEvent.on((event: app.browse.StartModuleEvent) => {
                var moduleKeys: string[] = event.getModules().map<string>((mod: ModuleSummary) => {
                    return mod.getModuleKey().toString();
                });
                new api.module.StartModuleRequest(moduleKeys).sendAndParse()
                    .then(() => {
                        this.moduleTreeGridPanel.reload();
                    }).done();
            });
            app.browse.UpdateModuleEvent.on((event: app.browse.UpdateModuleEvent) => {
                var moduleKeys: string[] = event.getModules().map<string>((mod: ModuleSummary) => {
                    return mod.getModuleKey().toString();
                });
                new api.module.UpdateModuleRequest(moduleKeys).sendAndParse()
                    .then(() => {
                        this.moduleTreeGridPanel.reload();
                    }).done();
            });
            app.browse.UninstallModuleEvent.on((event: app.browse.UninstallModuleEvent) => {
                var moduleKeys: string[] = event.getModules().map<string>((mod: ModuleSummary) => {
                    return mod.getModuleKey().toString();
                });
                new api.module.UninstallModuleRequest(moduleKeys).sendAndParse()
                    .then(() => {
                        this.moduleTreeGridPanel.reload();
                    }).done();
            });

            var installModuleDialog: app.browse.InstallModuleDialog = new app.browse.InstallModuleDialog(this.moduleTreeGridPanel);
            app.browse.InstallModuleEvent.on((event: app.browse.InstallModuleEvent) => {
                installModuleDialog.open();
            });

            app.browse.RefreshModulesEvent.on((event: app.browse.RefreshModulesEvent) => {
                this.moduleTreeGridPanel.reload();
            });

        }
    }

}
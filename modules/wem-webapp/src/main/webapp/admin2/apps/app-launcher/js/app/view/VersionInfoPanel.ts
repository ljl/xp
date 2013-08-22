module app_view {

    export class VersionInfoPanel extends api_dom.DivEl {

        private version:string;

        constructor(version:string) {
            super(null, 'admin-home-version-info');
        }

        setVersion(version: string): void {
            this.version = version;
            this.getEl().setInnerHtml(version);
        }

        getVersion():string {
            return this.version;
        }
    }

}

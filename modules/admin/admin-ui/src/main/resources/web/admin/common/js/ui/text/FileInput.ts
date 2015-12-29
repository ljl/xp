module api.ui.text {

    import InputEl = api.dom.InputEl;

    import Content = api.content.Content;
    import MediaUploader = api.content.MediaUploaderEl;

    import FileUploadStartedEvent = api.ui.uploader.FileUploadStartedEvent;
    import FileUploadProgressEvent = api.ui.uploader.FileUploadProgressEvent;
    import FileUploadedEvent = api.ui.uploader.FileUploadedEvent;
    import FileUploadCompleteEvent = api.ui.uploader.FileUploadCompleteEvent;
    import FileUploadFailedEvent = api.ui.uploader.FileUploadFailedEvent;

    export class FileInput extends api.dom.FormInputEl {

        private textInput: InputEl;
        private mediaUploaderEl: MediaUploader;

        constructor(className?: string) {
            super("div", "file-input");

            if (className) {
                this.addClass(className);
            }

            this.textInput = new InputEl("text");

            this.mediaUploaderEl = new api.content.MediaUploaderEl({
                operation: api.content.MediaUploaderElOperation.create,
                name: 'file-input-uploader',
                allowDrop: false,
                showResult: false,
                allowMultiSelection: true,
                deferred: true  // wait till it's shown
            });

            this.mediaUploaderEl.onUploadStarted((event: api.ui.uploader.FileUploadStartedEvent<api.content.Content>) => {
                var names = event.getUploadItems().map((uploadItem: api.ui.uploader.UploadItem<api.content.Content>) => {
                    return uploadItem.getName();
                });
                this.textInput.setValue(names.join(', '));
            });

            this.appendChildren(<api.dom.FormInputEl>this.textInput, <api.dom.FormInputEl>this.mediaUploaderEl);
        }

        setUploaderParams(params: {[key: string]: any}): FileInput {
            this.mediaUploaderEl.setParams(params);
            return this;
        }

        getUploaderParams(): {[key: string]: string} {
            return this.mediaUploaderEl.getParams();
        }

        setPlaceholder(placeholder: string): FileInput {
            this.textInput.setPlaceholder(placeholder);
            return this;
        }

        getPlaceholder(): string {
            return this.textInput.getPlaceholder();
        }

        getValue(): string {
            return this.textInput.getValue();
        }

        setValue(value: string): FileInput {
            this.textInput.setValue(value);
            return this;
        }

        onChange(listener: (event: Event) => void) {
            this.textInput.onChange(listener);
        }

        unChange(listener: (event: Event) => void) {
            this.textInput.unChange(listener);
        }

        onInput(listener: (event: Event) => void) {
            this.textInput.onInput(listener);
        }

        unInput(listener: (event: Event) => void) {
            this.textInput.unInput(listener);
        }

        giveFocus(): boolean {
            return this.textInput.giveFocus();
        }

        giveBlur(): boolean {
            return this.textInput.giveBlur();
        }

        reset(): FileInput {
            this.textInput.reset();
            this.mediaUploaderEl.reset();
            return this;
        }

        stop(): FileInput {
            this.mediaUploaderEl.stop();
            return this;
        }

        getUploader(): MediaUploader{
            return this.mediaUploaderEl;
        }

        onUploadStarted(listener: (event: FileUploadStartedEvent<Content>) => void) {
            this.mediaUploaderEl.onUploadStarted(listener);
        }

        unUploadStarted(listener: (event: FileUploadStartedEvent<Content>) => void) {
            this.mediaUploaderEl.unUploadStarted(listener);
        }

        onUploadProgress(listener: (event: FileUploadProgressEvent<Content>) => void) {
            this.mediaUploaderEl.onUploadProgress(listener);
        }

        unUploadProgress(listener: (event: FileUploadProgressEvent<Content>) => void) {
            this.mediaUploaderEl.unUploadProgress(listener);
        }

        onFileUploaded(listener: (event: FileUploadedEvent<Content>) => void) {
            this.mediaUploaderEl.onFileUploaded(listener);
        }

        unFileUploaded(listener: (event: FileUploadedEvent<Content>) => void) {
            this.mediaUploaderEl.unFileUploaded(listener);
        }

        onUploadCompleted(listener: (event: FileUploadCompleteEvent<Content>) => void) {
            this.mediaUploaderEl.onUploadCompleted(listener);
        }

        unUploadCompleted(listener: (event: FileUploadCompleteEvent<Content>) => void) {
            this.mediaUploaderEl.unUploadCompleted(listener);
        }

        onUploadReset(listener: () => void) {
            this.mediaUploaderEl.onUploadReset(listener);
        }

        unUploadReset(listener: () => void) {
            this.mediaUploaderEl.unUploadReset(listener);
        }

        onUploadFailed(listener: (event: FileUploadFailedEvent<Content>) => void) {
            this.mediaUploaderEl.onUploadFailed(listener);
        }

        unUploadFailed(listener: (event: FileUploadFailedEvent<Content>) => void) {
            this.mediaUploaderEl.unUploadFailed(listener);
        }

    }
}
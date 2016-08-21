import {ContentSummaryLoader} from "../../../resource/ContentSummaryLoader";
import {SelectedOption} from "../../../../ui/selector/combobox/SelectedOption";
import {Option} from "../../../../ui/selector/Option";
import {RichComboBox} from "../../../../ui/selector/combobox/RichComboBox";
import {RichComboBoxBuilder} from "../../../../ui/selector/combobox/RichComboBox";
import {ImageSelectorDisplayValue} from "./ImageSelectorDisplayValue";
import {ImageSelectorViewer} from "./ImageSelectorViewer";
import {ImageSelectorSelectedOptionsView} from "./ImageSelectorSelectedOptionsView";
import {BaseLoader} from "../../../../util/loader/BaseLoader";
import {ContentSummary} from "../../../ContentSummary";
import {ContentQueryResultJson} from "../../../json/ContentQueryResultJson";
import {ContentSummaryJson} from "../../../json/ContentSummaryJson";

export class ImageContentComboBox extends RichComboBox<ImageSelectorDisplayValue> {

        constructor(builder: ImageContentComboBoxBuilder) {

            var loader = builder.loader ? builder.loader : new ContentSummaryLoader();

            var richComboBoxBuilder = new RichComboBoxBuilder().
                setComboBoxName(builder.name ? builder.name : 'imageContentSelector').
                setLoader(loader).
                setSelectedOptionsView(builder.selectedOptionsView || new ImageSelectorSelectedOptionsView()).
                setMaximumOccurrences(builder.maximumOccurrences).
                setOptionDisplayValueViewer(new ImageSelectorViewer()).
                setDelayedInputValueChangedHandling(750).
                setValue(builder.value).
                setMinWidth(builder.minWidth).
                setRemoveMissingSelectedOptions(true).
                setDisplayMissingSelectedOptions(true);

            // Actually the hack.
            // ImageSelectorSelectedOptionsView and BaseSelectedOptionsView<ContentSummary> are incompatible in loaders.
            super(<RichComboBoxBuilder<ImageSelectorDisplayValue>>richComboBoxBuilder);

            if (builder.postLoad) {
                this.handleLastRange(builder.postLoad);
            }
        }

        createOption(value: ContentSummary): Option<ImageSelectorDisplayValue> {
            return {
                value: this.getDisplayValueId(value),
                displayValue: ImageSelectorDisplayValue.fromContentSummary(value)
            }
        }

        public static create(): ImageContentComboBoxBuilder {
            return new ImageContentComboBoxBuilder();
        }
    }

    export class ImageContentComboBoxBuilder {

        name: string;

        maximumOccurrences: number = 0;

        loader: BaseLoader<ContentQueryResultJson<ContentSummaryJson>, ContentSummary>;

        minWidth: number;

        selectedOptionsView: ImageSelectorSelectedOptionsView;

        optionDisplayValueViewer: ImageSelectorViewer;

        postLoad: () => void;

        value: string;

        setName(value: string): ImageContentComboBoxBuilder {
            this.name = value;
            return this;
        }

        setValue(value: string): ImageContentComboBoxBuilder {
            this.value = value;
            return this;
        }

        setMaximumOccurrences(maximumOccurrences: number): ImageContentComboBoxBuilder {
            this.maximumOccurrences = maximumOccurrences;
            return this;
        }

        setLoader(loader: BaseLoader<ContentQueryResultJson<ContentSummaryJson>, ContentSummary>): ImageContentComboBoxBuilder {
            this.loader = loader;
            return this;
        }

        setMinWidth(value: number): ImageContentComboBoxBuilder {
            this.minWidth = value;
            return this;
        }

        setSelectedOptionsView(value: ImageSelectorSelectedOptionsView): ImageContentComboBoxBuilder {
            this.selectedOptionsView = value;
            return this;
        }

        setOptionDisplayValueViewer(value: ImageSelectorViewer): ImageContentComboBoxBuilder {
            this.optionDisplayValueViewer = value;
            return this;
        }

        setPostLoad(postLoad: () => void) {
            this.postLoad = postLoad;
            return this;
        }

        build(): ImageContentComboBox {
            return new ImageContentComboBox(this);
        }

    }

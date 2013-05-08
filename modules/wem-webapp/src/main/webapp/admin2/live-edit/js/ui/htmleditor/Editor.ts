module LiveEdit.ui {
    var $ = $liveedit;

    export class Editor extends LiveEdit.ui.Base {

        toolbar;

        constructor() {
            super();

            this.toolbar = new LiveEdit.ui.EditorToolbar();
            this.registerGlobalListeners();

            console.log('Editor instantiated. Using jQuery ' + $().jquery);
        }


        registerGlobalListeners() {
            var me = this;
            $(window).on('component.onParagraphEdit', function (event, $paragraph) {
                me.activate($paragraph);
            });
            $(window).on('component.onParagraphEditLeave', function (event, $paragraph) {
                me.deActivate($paragraph);
            });
            $(window).on('editorToolbar.onButtonClick', function (event, tag) {
                // Simplest implementation for now.
                document.execCommand(tag, false, null);
            });
        }


        activate($paragraph) {
            $paragraph.get(0).contentEditable = true;
            $paragraph.get(0).focus();

        }


        deActivate($paragraph) {
            $paragraph.get(0).contentEditable = false;
            $paragraph.get(0).blur();
        }

    }
}
/*
AdminLiveEdit.namespace.useNamespace('AdminLiveEdit.view.htmleditor');

(function ($) {
    'use strict';

    var editor = AdminLiveEdit.view.htmleditor.Editor = function () {

        this.toolbar = new AdminLiveEdit.view.htmleditor.Toolbar();

        this.registerGlobalListeners();
    };

    var proto = editor.prototype;

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


    proto.registerGlobalListeners = function () {
        var me = this;
        $(window).on('component.onParagraphEdit', function (event, $paragraph) {
            me.activate($paragraph);
        });
        $(window).on('component.onParagraphEditLeave', function (event, $paragraph) {
            me.deActivate($paragraph);
        });
        $(window).on('editorToolbar.onButtonClick', function (event, tag) {
            // Simplest implementation for now.
            document.execCommand(tag, false, null);
        });
    };


    proto.activate = function ($paragraph) {
        $paragraph.get(0).contentEditable = true;
        $paragraph.get(0).focus();

    };


    proto.deActivate = function ($paragraph) {
        $paragraph.get(0).contentEditable = false;
        $paragraph.get(0).blur();
    };

}($liveedit));
*/

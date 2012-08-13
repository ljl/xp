(function () {
    // Namespaces
    AdminLiveEdit.components = {};
    AdminLiveEdit.components.events = {};


    AdminLiveEdit.components.events.Base = function () {
        this.selector = '';
    };


    AdminLiveEdit.components.events.Base.prototype = {
        attachMouseOverEvent: function () {
            var self = this;
            $liveedit(document).on('mouseover', this.selector, function (event) {
                var $component = $liveedit(this);
                var componentIsDescendantOfSelected = $component.parents('.live-edit-selected-component').length === 1;
                // TODO: remove reference to DragDrop, use PubSub.
                var disableHover = componentIsDescendantOfSelected || AdminLiveEdit.ui.DragDrop.isDragging();
                if (disableHover) {
                    return;
                }
                event.stopPropagation();

                $liveedit.publish('/ui/highlighter/on-highlight', [$component]);
            });
        },


        attachMouseOutEvent: function () {
            $liveedit(document).on('mouseout', function (event) {
                $liveedit.publish('/ui/highlighter/on-hide');
            });
        },


        attachClickEvent: function () {
            $liveedit(document).on('click touchstart', this.selector, function (event) {
                event.stopPropagation();
                event.preventDefault();
                var $closestComponentFromTarget = $liveedit(event.target).closest('[data-live-edit-type]');
                var componentIsSelected = $closestComponentFromTarget.hasClass('live-edit-selected-component');
                if (componentIsSelected) {
                    $liveedit.publish('/ui/componentselector/on-deselect');
                } else {
                    $liveedit.publish('/ui/componentselector/on-select', [$closestComponentFromTarget]);
                }
                return false;
            });
        },


        getAll: function () {
            return $liveedit(this.selector);
        }

    };
}());

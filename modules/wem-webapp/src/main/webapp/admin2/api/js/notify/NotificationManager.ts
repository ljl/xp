module admin.api.notify {

    class NotificationManager {

        // Position inside document body.
        // Available: 'tl', 't', 'tr', 'bl', 'b', 'br'.
        // Default is 'b'.
        position:String = 'b';
        // Space between notification items

        space:Number = 3;

        lifetime:Number = 5000;

        slideDuration:Number = 1000;

        timers:Object;

        el:any;

        tpl:any = {
            manager: new Ext.Template(
                '<div class="admin-notification-container">',
                '   <div class="admin-notification-wrapper"></div>',
                '</div>'
            ),
            notification: new Ext.Template(
                '<div class="admin-notification" style="height: 0; opacity: 0;">',
                '   <div class="admin-notification-inner">',
                '       <a class="admin-notification-remove" href="#">X</a>',
                '       <div class="admin-notification-content">{message}</div>',
                '   </div>',
                '</div>'
            ),
            error: new Ext.Template(
                '<span>{message}</span>'
            ),
            publish: new Ext.XTemplate(
                '<span style="float: right; margin-left: 30px;"><a href="#" class="admin-notification-result">See result</a> or <a href="#" class="admin-notification-publish">Publish to other locations</a></span>',
                '<span style="line-height: 1.5em;">',
                '<tpl if="contentName"> "{contentName}"</tpl> published successfully!',
                '</span> '
            ),
            general: new Ext.XTemplate(
                '<span style="float: right; margin-left: 30px;"><a href="#" class="admin-notification-publish">Publish</a> or <a href="#" class="admin-notification-close">Close</a></span>',
                '<span style="line-height: 1.5em;">',
                '<tpl if="contentName"> "{contentName}"</tpl> saved successfully!',
                '</span> '
            )
        };


        constructor() {
            // hash stored timers for active notifications
            this.timers = {};

            this.render();

            admin.api.message.addListener('showNotification', this.showNotification, this);
            admin.api.message.addListener('removeNotification', this.removeNotification, this);
        }


        render():void {
            var me = this,
                node,
                pos = this.position;

            // render manager template to document body
            node = this.tpl.manager.append(Ext.getBody());
            this.el = Ext.get(node);

            // check position or set to default
            (pos[0] == 't') || (pos[0] == 'b') || (this.position = pos = 'b');

            // align verticaly
            this.getEl().setStyle(((pos[0] == 't') ? 'top' : 'bottom'), 0);

            // align horizontaly
            this.getWrapperEl().setStyle({ margin: 'auto' });

            // me.getWrapperEl().setStyle((pos[1] != 'r') ? (pos[1] == 'l') ? { marginLeft: 0 } : { margin: 'auto' } : { marginRight: 0 });
        }


        showNotification(type, args, opts):void {
            this[type] ? this[type](args) : this.notify({});
        }


        removeNotification(mark):void {
            var me = this,
                notifications = Ext.select('.admin-notification[data-mark=' + mark + ']');

            notifications.each(function (notificationEl) {
                me.remove(notificationEl);
            });
        }


        /**
         *    Acceptable options:
         *    message
         *    backgroundColor - css color
         *    listeners - object or array of objects passed to Ext.Element.addListener,
         *    lifetime - milliseconds or negative for permanent notification
         *    mark - to identify this notification
         *    single - if true only one notification with specified mark will be created
         */
        notify(nOpts):void {
            var me = this,
                notificationEl,
                height;

            if (me.isRendered(nOpts)) {
                return;
            }

            notificationEl = me.renderNotification(nOpts);
            me.setNotificationListeners(notificationEl, nOpts);

            height = me.getInnerEl(notificationEl).getHeight();

            notificationEl.animate({
                duration: me.slideDuration,
                to: {
                    height: height + me.space,
                    opacity: 1
                },
                callback: function () {
                    if (nOpts.lifetime < 0) {
                        return;
                    }

                    me.timers[notificationEl.id] = {
                        remainingTime: (nOpts.lifetime || me.lifetime)
                    };
                    me.startTimer(notificationEl);
                }
            });
        }


        error(message):void {
            var defaultMessage = 'Lost connection to server - Please wait until connection is restored',
                opts;

            opts = Ext.apply({
                message: defaultMessage,
                backgroundColor: 'red'
            }, Ext.isString(message) ? { message: message } : message);

            opts.message = this.tpl.error.apply(opts);

            this.notify(opts);
        }


        general(opts):void {
            var notificationOpts = {
                message: this.tpl.general.apply(opts),
                backgroundColor: '#4294de',
                listeners: []
            };

            if (Ext.isFunction(opts.resultCallback)) {
                notificationOpts.listeners.push({
                    click: {
                        fn: opts.resultCallback,
                        delegate: '.admin-notification-result',
                        stopEvent: true
                    }
                });
            }
            if (Ext.isFunction(opts.publishCallback)) {
                notificationOpts.listeners.push({
                    click: {
                        fn: opts.publishCallback,
                        delegate: '.admin-notification-publish',
                        stopEvent: true
                    }
                });
            }

            this.notify(notificationOpts);
        }

        /**
         *  Returns notification id
         */
        publish(opts):void {
            var notificationOpts = {
                message: this.tpl.publish.apply(opts),
                backgroundColor: '#669c34',
                listeners: []
            };

            if (Ext.isFunction(opts.publishCallback)) {
                notificationOpts.listeners.push({
                    click: {
                        fn: opts.publishCallback,
                        delegate: '.admin-notification-publish',
                        stopEvent: true
                    }
                });
            }
            if (Ext.isFunction(opts.closeCallback)) {
                notificationOpts.listeners.push({
                    click: {
                        fn: opts.closeCallback,
                        delegate: '.admin-notification-close',
                        stopEvent: true
                    }
                });
            }

            this.notify(notificationOpts);
        }


        isRendered(nOpts):Boolean {
            return nOpts.single && nOpts.mark
                && this.getEl().select('.admin-notification[data-mark=' + nOpts.mark + ']').getCount() > 0;
        }


        renderNotification(nOpts):any {
            var me = this,
                tpl = me.tpl.notification,
                style = {},
                notificationEl;

            // create notification DOM element
            notificationEl = (me.position[0] == 't')
                ? tpl.insertFirst(me.getWrapperEl(), nOpts, true)
                : tpl.append(me.getWrapperEl(), nOpts, true);

            // set notification style
            if (nOpts.backgroundColor) {
                style['backgroundColor'] = nOpts.backgroundColor;
            }

            // nOpts.backgroundColor && (style.backgroundColor = nOpts.backgroundColor);
            (me.position[0] == 't')
                ? (style['marginBottom'] = me.space + 'px')
                : (style['marginTop'] = me.space + 'px');
            me.getInnerEl(notificationEl).setStyle(style);

            // set mark to identify this notification
            if (nOpts.mark) {
                notificationEl.set({ 'data-mark': nOpts.mark });
            }

            return notificationEl;
        }


        setNotificationListeners(notificationEl, nOpts):void {
            var me = this;

            // set default listeners
            notificationEl.on({
                click: {
                    fn: function () {
                        me.remove(notificationEl);
                    },
                    delegate: '.admin-notification-remove',
                    stopEvent: true
                },
                mouseover: function () {
                    me.stopTimer(notificationEl);
                },
                mouseleave: function () {
                    me.startTimer(notificationEl);
                }
            });

            if (nOpts.listeners) {
                Ext.each(nOpts.listeners, function (listener) {
                    notificationEl.on(listener);
                });
            }
        }


        remove(notificationEl):void {
            var me = this;

            Ext.isElement(notificationEl) || (notificationEl = Ext.get(notificationEl));

            if (!notificationEl) {
                return;
            }

            notificationEl.animate({
                duration: me.slideDuration,
                to: {
                    height: 0,
                    opacity: 0
                },
                callback: function () {
                    Ext.removeNode(notificationEl.dom);
                }
            });

            delete me.timers[notificationEl.id];
        }


        startTimer(notificationEl):void {
            var me = this,
                timer = me.timers[notificationEl.id];

            if (!timer) {
                return;
            }

            timer.id = setTimeout(
                function () {
                    me.remove(notificationEl);
                },
                timer.remainingTime
            );

            timer.startTime = Date.now();
        }


        stopTimer(notificationEl):void {
            var timer = this.timers[notificationEl.id];

            if (!timer || !timer.id) {
                return;
            }

            clearTimeout(timer.id);
            timer.id = null;
            timer.remainingTime -= Date.now() - timer.startTime;
        }


        getEl():any {
            return this.el;
        }


        getWrapperEl():any {
            return this.el.first('.admin-notification-wrapper');
        }


        getInnerEl(notificationEl):any {
            return notificationEl.down('.admin-notification-inner');
        }

        getNotificationEl(node):any {
            Ext.isElement(node) || (node = Ext.get(node));
            return node.up('.admin-notification', this.getEl());
        }

    }


    var manager = new NotificationManager();
}

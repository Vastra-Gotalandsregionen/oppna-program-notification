AUI().add('rp-notifications-bar', function (A) {
        var Lang = A.Lang,
            isArray = Lang.isArray,
            isFunction = Lang.isFunction,
            isNull = Lang.isNull,
            isObject = Lang.isObject,
            isString = Lang.isString,
            isUndefined = Lang.isUndefined,
            getClassName = A.ClassNameManager.getClassName,
            concat = function () {
                return Array.prototype.slice.call(arguments).join(SPACE);
            },

            NOTIFICATIONS_LIST_NODE = 'notificationsListNode',

            NAME = 'rp-notifications-bar',
            NS = 'rp-notifications-bar',

            CSS_ACTIVE = 'active',
            CSS_HIDDEN = 'aui-helper-hidden',
            CSS_NOTIFICATIONS_ITEM = 'notifications-bar-item',
            CSS_NOTIFICATIONS_OVERLAY = 'notifications-overlay'
            ;

        var NotificationsBar = A.Component.create(
            {
                ATTRS:{

                    notificationsListNode:{
                        value:'#notificationsList',
                        setter:A.one
                    }

                },
                EXTENDS:A.Component,
                NAME:NAME,
                NS:NS,

                notificationOverlays:null,

                prototype:{

                    initializer:function (config) {
                        var instance = this;

                    },

                    renderUI:function () {
                        var instance = this;

                        instance._initNotificationOverlays();
                    },

                    bindUI:function () {
                        var instance = this;

                        var listNode = instance.get(NOTIFICATIONS_LIST_NODE);
                        var notificationLinks = listNode.all('.' + CSS_NOTIFICATIONS_ITEM + ' a');
                        notificationLinks.on('click', function (e) {
                            e.preventDefault();
                        });
                    },

                    _initNotificationOverlays:function () {
                        var instance = this;

                        var listNode = instance.get(NOTIFICATIONS_LIST_NODE);

                        var listItemNodes = listNode.all('.' + CSS_NOTIFICATIONS_ITEM);

                        var notificationOverlays = [];

                        listItemNodes.each(function (node, index, list) {

                            var nodeLink = node.one('a');
//                    		var nodeLinkHtml = nodeLink.html();

                            var overlay = new A.OverlayContextPanel({
                                align:{
                                    node:nodeLink,
                                    points:[A.WidgetPositionAlign.TR, A.WidgetPositionAlign.TL]
                                },
                                anim:true,
                                bodyContent:'',
                                boundingBox:'#notification-overlay-context-panel',
                                cancellableHide:true,
                                cssClass:CSS_NOTIFICATIONS_OVERLAY,
                                hideDelay:200,
                                hideOnDocumentClick:true,
                                showArrow:false,
                                trigger:nodeLink,
                                width:'300px'
                            });

                            overlay.on('hide', instance._onNotificationOverlayHide, instance);
                            overlay.on('show', instance._onNotificationOverlayShow, instance);

                            var triggerURL = nodeLink.getAttribute('href');
                            var loadURL = triggerURL.replace('p_p_state=normal', 'p_p_state=exclusive');

                            overlay.plug(A.Plugin.IO, {
                                autoLoad:false,
                                method:'GET',
                                uri:loadURL,
                                type:'Widget'
                            });

                            overlay.render();

                            notificationOverlays.push(overlay);

                        });

                        instance.notificationOverlays = notificationOverlays;

                    },

                    _onNotificationOverlayHide:function (e) {
                        var instance = this;

                        var overlay = e.currentTarget;

                        var triggers = overlay.get('trigger');

                        triggers.each(function (item, index, list) {
                            var listItem = item.ancestor('.' + CSS_NOTIFICATIONS_ITEM);
                            listItem.removeClass(CSS_ACTIVE);
                        });
                    },

                    _onNotificationOverlayShow:function (e) {
                        var instance = this;

                        var overlay = e.currentTarget;

                        var triggers = overlay.get('trigger');

                        triggers.each(function (item, index, list) {
                            var listItem = item.ancestor('.' + CSS_NOTIFICATIONS_ITEM);
                            listItem.addClass(CSS_ACTIVE);
                            item.one('span').hide(); //todo erik får granska
                        });

                        A.Array.each(instance.notificationOverlays, function (object, index, list) {
                            overlay.io.stop();
                            object.hide();
                        });

                        overlay.io.start();

                        // Hide the counter

                    },

                    _someFunction:function () {
                        var instance = this;
                    }

                }
            }
        );

        A.NotificationsBar = NotificationsBar;

    }, 1, {
        requires:[
            'aui-base',
            'aui-io',
            'aui-loading-mask',
            'aui-overlay',
            'substitute'
        ]
    }
);

function reloadNotifications(resourceUrl) {
    var items = null;
    a.io.request(resourceUrl, {
        cache:false,
        sync:true,
        timeout:1000,
        dataType:'json',
        method:'get',
        on:{
            success:function () {
                console.log("success");
                items = this.get('responseData');
                console.log("success2");

                var element = document.getElementById('alfresco-count');
                var value = items['alfrescoCount'];
                checkNewValue(value, element);
                element.innerHTML = value;

                var element = document.getElementById('usd-issues-count');
                var value = items['usdIssuesCount'];
                checkNewValue(value, element);
                element.innerHTML = value;

                var element = document.getElementById('email-count');
                var value = items['emailCount'];
                checkNewValue(value, element);
                element.innerHTML = value;

                var element = document.getElementById('random-count');
                var value = items['randomCount'];
                console.log(value + " " + element.innerHTML);
                checkNewValue(value, element);
                element.innerHTML = value;

                var element = document.getElementById('invoices-count');
                var value = items['invoicesCount'];
                checkNewValue(value, element);
                element.innerHTML = value;

            },
            failure:function () {
            }
        }
    });
}

function checkNewValue(value, element) {
    if (value != element.innerHTML) {
        // New value -> highlight if more than zero
        var classNameLI = element.parentNode.parentNode.parentNode.className;
        var addition = "";
        if (classNameLI.indexOf("last") != -1) {
            addition = " last" //this is the last list item
        }
        if (value > 0) {
            element.style.fontSize = "1.1em";
            element.parentNode.className = "count";//('aui-helper-hidden');
            element.parentNode.parentNode.parentNode.className = "notifications-bar-item notifications-bar-email" + addition;
        } else {
            element.parentNode.className = "count aui-helper-hidden";
            element.parentNode.parentNode.parentNode.className = "notifications-bar-item notifications-bar-email aui-helper-hidden" + addition;
        }

    } else {
        element.style.fontSize = "0.9em";
    }
}
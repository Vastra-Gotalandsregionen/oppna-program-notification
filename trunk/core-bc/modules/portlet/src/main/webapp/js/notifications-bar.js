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

            NODE_ITEM_INVOICES = 'nodeItemInvoices',
            NODE_ITEM_USD = 'nodeItemUsd',
            NODE_ITEM_ALFRESCO = 'nodeItemAlfresco',
            NODE_ITEM_RANDOM = 'nodeItemRandom',
            NODE_ITEM_EMAIL = 'nodeItemEmail',
            NODE_ITEM_SOCIAL_REQUEST = 'nodeItemSocialRequest',

            UPDATE_NOTIFICATIONS_INTERVAL = 'updateNotificationsInterval',
            UPDATE_NOTIFICATIONS_URL = 'updateNotificationsUrl',
            UPDATE_NOTIFICATIONS_NO_CACHE_URL = 'updateNotificationsNoCacheUrl',

            CSS_ACTIVE = 'active',
            CSS_HIDDEN = 'aui-helper-hidden',
            CSS_COUNT_HIGHLIGHT = 'count-highlight',
            CSS_NOTIFICATIONS_ITEM = 'notifications-bar-item',
            CSS_NOTIFICATIONS_OVERLAY = 'notifications-overlay'
            ;

        var NotificationsBar = A.Component.create(
            {
                ATTRS:{
                	
                	nodeItemInvoices: {
                		setter: A.one
                	},
                	
                	nodeItemUsd: {
                		setter: A.one
                	},
                	
                	nodeItemAlfresco: {
                		setter: A.one
                	},
                	
                	nodeItemRandom: {
                		setter: A.one
                	},
                	
                	nodeItemEmail: {
                		setter: A.one
                	},

                    nodeItemSocialRequest: {
                		setter: A.one
                	},

                    notificationsListNode: {
                        setter: A.one
                    },
                    
                    updateNotificationsInterval: {
                    	value: 10000
                    },
                    
                    updateNotificationsUrl: {
                    	value: ''
                    },
                    
                    updateNotificationsNoCacheUrl: {
                    	value: ''
                    }

                },
                EXTENDS:A.Component,
                NAME:NAME,
                NS:NS,

                notificationOverlays:null,
                updateNotificationsIO: null,

                prototype:{

                    initializer:function (config) {
                        var instance = this;
                        
                        instance.notificationOverlays = null;
                        instance.updateNotificationsIO = null;
                    },

                    renderUI:function () {
                        var instance = this;
                        
                        //instance._initConsole();
                        
                        instance._initNotificationsUpdate();
                        instance._initNotificationOverlays();
                    },

                    bindUI:function () {
                        var instance = this;

                        var listNode = instance.get(NOTIFICATIONS_LIST_NODE);
                        var notificationLinks = listNode.all('.' + CSS_NOTIFICATIONS_ITEM + ' a');
                        notificationLinks.on('click', function (e) {
                        	e.halt(true);
                        });
                    },
                    
                    _initConsole: function() {
                    	var instance = this;
                    	
                    	var consoleSettings = {
	            	        newestOnTop: true,
	            	        visible: true
                    	};
                    	
                    	var console =  new A.Console(consoleSettings).render();
                    },
                    
                    _initNotificationOverlays:function () {
                        var instance = this;

                        var listNode = instance.get(NOTIFICATIONS_LIST_NODE);

                        var listItemNodes = listNode.all('.' + CSS_NOTIFICATIONS_ITEM);

                        var notificationOverlays = [];

                        listItemNodes.each(function (node, index, list) {

                            var nodeLink = node.one('a');

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
                                cache:false,
                                method:'POST', // Need to post to avoid caching in IE
                                uri:loadURL,
                                type:'Widget'
                            });

                            overlay.render();

                            notificationOverlays.push(overlay);

                        });

                        instance.notificationOverlays = notificationOverlays;
                    },
                    
                    _initNotificationsUpdate: function() {
                    	var instance = this;
                    	
                    	instance._updateNotifications(instance.get(UPDATE_NOTIFICATIONS_NO_CACHE_URL));
                    	
                    	A.later(instance.get(UPDATE_NOTIFICATIONS_INTERVAL), instance, instance._updateNotifications, [], true);
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
                            item.one('.count').removeClass(CSS_COUNT_HIGHLIGHT);
                        });

                        A.Array.each(instance.notificationOverlays, function (object, index, list) {
                            overlay.io.stop();
                            object.hide();
                        });

                        overlay.io.start();
                    },
                    
                    _onUpdateNotificationsSuccess: function(event, id, xhr) {
                    	
                    	var instance = this;

                        var responseText = xhr.responseText;

                        if (responseText.hasOwnProperty('length') && responseText.length < 1) {
                            return;
                        }

                        var responseJSON = A.JSON.parse(responseText);
                    	
                    	if(!isNull(responseJSON)) {
                        	var alfrescoCount = responseJSON['alfrescoCount'];
                        	var usdIssuesCount = responseJSON['usdIssuesCount'];
                        	var emailCount = responseJSON['emailCount'];
                        	var randomCount = responseJSON['randomCount'];
                        	var invoicesCount = responseJSON['invoicesCount'];
                        	var socialRequestCount = responseJSON['socialRequestCount'];

                        	instance._updateCounterHtml(instance.get(NODE_ITEM_ALFRESCO), alfrescoCount);
                        	instance._updateCounterHtml(instance.get(NODE_ITEM_USD), usdIssuesCount);
                        	instance._updateCounterHtml(instance.get(NODE_ITEM_EMAIL), emailCount);
                        	instance._updateCounterHtml(instance.get(NODE_ITEM_RANDOM), randomCount);
                        	instance._updateCounterHtml(instance.get(NODE_ITEM_INVOICES), invoicesCount);
                        	instance._updateCounterHtml(instance.get(NODE_ITEM_SOCIAL_REQUEST), socialRequestCount);
                    	}
                    },
                    
                    _updateCounterHtml: function(listNode, value) {
                    	var instance = this;
                    	
                    	var countWrapperNode = listNode.one('.count');
                    	var countNode = countWrapperNode.one('span');
                    	var countNodeValueStr = countNode.html();
                    	var countNodeValue = parseInt(countNodeValueStr);
                    	
                    	if(isNull(value) || value <= 0) {
                    		countWrapperNode.hide();
                    	}
                    	else if(value == countNodeValue) {
                            countWrapperNode.show(); // In some cases this is needed even though it shouldn't be
                        }
                    	else {
                            listNode.show();
                            countWrapperNode.show();
                            countWrapperNode.addClass(CSS_COUNT_HIGHLIGHT);
                            countNode.html(value);
                        }
                    },
                    
                    _updateNotifications: function(updateUrl) {
                    	var instance = this;

                    	if(updateUrl == '' || isNull(updateUrl) || isUndefined(updateUrl)) {
                    		updateUrl = instance.get(UPDATE_NOTIFICATIONS_URL);
                    	}

                    	if(isNull(instance.updateNotificationsIO)) {

                    		instance.updateNotificationsIO = new A.io.request(updateUrl, {
                            	autoLoad: false,
                                cache: false,
                                sync: false,
                                timeout: instance.get(UPDATE_NOTIFICATIONS_INTERVAL),
                                dataType: 'json',
                                method: 'POST'  // Need to post to avoid caching in IE
                            });

                        	// Success handler
                    		instance.updateNotificationsIO.on('success', instance._onUpdateNotificationsSuccess, instance);
                    	}
                    	else {
                    		
                    		if(instance.updateNotificationsIO.get('active')) {
                    			instance.updateNotificationsIO.stop();
                    		}
                    		
							// Update io data params
                    		instance.updateNotificationsIO.set('uri', updateUrl);
                    	}
                    	
                    	instance.updateNotificationsIO.start();
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
            'console',
            'substitute'
        ]
    }
);
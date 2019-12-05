/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* global define, module, require, exports */

(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['jquery',
                'nf.Actions',
                'nf.Birdseye',
                'nf.Storage',
                'nf.CanvasUtils',
                'nf.ProcessGroupConfiguration',
                'nf.ErrorHandler',
                'nf.Graph'],
            function ($, nfActions, nfBirdseye, nfStorage, nfCanvasUtils, nfProcessGroupConfiguration, nfErrorHandler,nfGraph) {
                return (nf.ng.Canvas.TemplatesControlsCtrl = factory($, nfActions, nfBirdseye, nfStorage, nfCanvasUtils, nfProcessGroupConfiguration));
            });
    } else if (typeof exports === 'object' && typeof module === 'object') {
        module.exports = (nf.ng.Canvas.TemplatesControlsCtrl =
            factory(require('jquery'),
                require('nf.Actions'),
                require('nf.Birdseye'),
                require('nf.Storage'),
                require('nf.CanvasUtils'),
                require('nf.ProcessGroupConfiguration'),
                require('nf.ErrorHandler'),
                require('nf.Graph'),));
    } else {
        nf.ng.Canvas.TemplatesControlsCtrl = factory(root.$,
            root.nf.Actions,
            root.nf.Birdseye,
            root.nf.Storage,
            root.nf.CanvasUtils,
            root.nf.ProcessGroupConfiguration,
            root.nf.ErrorHandler,
            root.nf.Graph,);
    }
}(this, function ($, nfActions, nfBirdseye, nfStorage, nfCanvasUtils, nfProcessGroupConfiguration, nfErrorHandler, nfGraph) {
    'use strict';

    return function (serviceProvider, navigateCtrl, operateCtrl) {
        'use strict';

        /**
         * Hides the specified graph control.
         *
         * @param {jQuery} graphControl
         */
        var config = {
            urls: {
                templates: apiHost + "/nifi-api/flow/templates",
                official: "https://172.18.28.230:8443/orchsym-api/template/official/search",
                favorites: "https://172.18.28.230:8443/orchsym-pro-api/favorites/templates/search",
                custom: apiHost + "/nifi-api/orchsym-template/custom/search"
            }
        };

        function TemplatesControlsCtrl(navigateCtrl, operateCtrl) {
            this.navigateCtrl = navigateCtrl;
            this.operateCtrl = operateCtrl;
        }

        var createTemplate = function (templateId, pt) {
            var instantiateTemplateInstance = {
                'templateId': templateId,
                'originX': pt.x,
                'originY': pt.y
            };

            // create a new instance of the new template
            $.ajax({
                type: 'POST',
                url: serviceProvider.headerCtrl.toolboxCtrl.config.urls.api + '/process-groups/' + encodeURIComponent(nfCanvasUtils.getGroupId()) + '/template-instance',
                data: JSON.stringify(instantiateTemplateInstance),
                dataType: 'json',
                contentType: 'application/json'
            }).done(function (response) {
                // populate the graph accordingly
                nfGraph.add(response.flow, {
                    'selectAll': true
                });

                // update component visibility
                nfGraph.updateVisibility();

                // update the birdseye
                nfBirdseye.refresh();
            }).fail(nfErrorHandler.handleAjaxError);
        };

        TemplatesControlsCtrl.prototype = {
            constructor: TemplatesControlsCtrl,

            /**
             *  Register the header controller.
             */
            register: function () {
                if (serviceProvider.templatesControlsCtrl === undefined) {
                    serviceProvider.register('templatesControlsCtrl', templatesControlsCtrl);
                }
            },

            openListBox: function(key){
                this.openList[key] = !this.openList[key];
            },

            /**
             * Initialize the graph controls.
             */
            init: function () {
                var _this = this;
                this.officialName = nf._.msg('nf-processor-component.officialName');
                this.favoritesName = nf._.msg('nf-processor-component.favoritesName');
                this.customName = nf._.msg('nf-processor-component.customName');

                this.openList = {
                    official: true,
                    favorites: true,
                    custom: true,
                };
                $.ajax({
                    type: 'GET',
                    url: config.urls.official,
                    dataType: 'json'
                }).done(function (response) {
                    // ensure there are groups specified
                    _this.templatesOfficialBackList = response.results;
                    _this.templatesOfficialList = response.results;
                }).fail(function (error){
                    _this.templatesOfficialList = [{
                        "id":  "125dab6b-5692-3dbb-ab39-57663acc59a5",
                        "groupId":  "61a3e170-016c-1000-60bc-d51087f4578f",
                        "name":  "Test Data",
                        "description":  "sss ",
                        "timestamp":  "11/28/2019 16:58:08 CST",
                        "additions":
                            {
                                "MODIFIED_USER":  "admin@orchsym.com",
                                "MODIFIED_TIMESTAMP":  "1574932922677",
                                "IS_FAVORITE":  "true",
                                "SOURCE_TYPE":  "SAVE_AS",
                                "TEMPLATE_TYPE":  "APPLICATION",
                                "CREATED_USER":  "admin@orchsym.com",
                                "CREATED_TIMESTAMP":  "1574931488485"
                            },
                        "encoding-version":  "1.2"
                    }];
                    _this.templatesOfficialBackList = [{
                        "id":  "125dab6b-5692-3dbb-ab39-57663acc59a5",
                        "groupId":  "61a3e170-016c-1000-60bc-d51087f4578f",
                        "name":  "Test Data",
                        "description":  "sss ",
                        "timestamp":  "11/28/2019 16:58:08 CST",
                        "additions":
                            {
                                "MODIFIED_USER":  "admin@orchsym.com",
                                "MODIFIED_TIMESTAMP":  "1574932922677",
                                "IS_FAVORITE":  "true",
                                "SOURCE_TYPE":  "SAVE_AS",
                                "TEMPLATE_TYPE":  "APPLICATION",
                                "CREATED_USER":  "admin@orchsym.com",
                                "CREATED_TIMESTAMP":  "1574931488485"
                            },
                        "encoding-version":  "1.2"
                    }];
                });
                $.ajax({
                    type: 'GET',
                    url: config.urls.favorites,
                    dataType: 'json'
                }).done(function (response) {
                    // ensure there are groups specified
                    _this.templatesFavoritesBackList = response.results;
                    _this.templatesFavoritesList = response.results;
                }).fail(function (error){
                    _this.templatesFavoritesList = [{
                        "id":  "125dab6b-5692-3dbb-ab39-57663acc59a5",
                        "groupId":  "61a3e170-016c-1000-60bc-d51087f4578f",
                        "name":  "Test Data",
                        "description":  "sss ",
                        "timestamp":  "11/28/2019 16:58:08 CST",
                        "additions":
                            {
                                "MODIFIED_USER":  "admin@orchsym.com",
                                "MODIFIED_TIMESTAMP":  "1574932922677",
                                "IS_FAVORITE":  "true",
                                "SOURCE_TYPE":  "SAVE_AS",
                                "TEMPLATE_TYPE":  "APPLICATION",
                                "CREATED_USER":  "admin@orchsym.com",
                                "CREATED_TIMESTAMP":  "1574931488485"
                            },
                        "encoding-version":  "1.2"
                    }];
                    _this.templatesFavoritesBackList = [{
                        "id":  "125dab6b-5692-3dbb-ab39-57663acc59a5",
                        "groupId":  "61a3e170-016c-1000-60bc-d51087f4578f",
                        "name":  "Test Data",
                        "description":  "sss ",
                        "timestamp":  "11/28/2019 16:58:08 CST",
                        "additions":
                            {
                                "MODIFIED_USER":  "admin@orchsym.com",
                                "MODIFIED_TIMESTAMP":  "1574932922677",
                                "IS_FAVORITE":  "true",
                                "SOURCE_TYPE":  "SAVE_AS",
                                "TEMPLATE_TYPE":  "APPLICATION",
                                "CREATED_USER":  "admin@orchsym.com",
                                "CREATED_TIMESTAMP":  "1574931488485"
                            },
                        "encoding-version":  "1.2"
                    }];
                });
                $.ajax({
                    type: 'GET',
                    url: config.urls.custom,
                    dataType: 'json'
                }).done(function (response) {
                    // ensure there are groups specified
                    _this.templatesCustomBackList = response.results;
                    _this.templatesCustomList = response.results;
                }).fail(function (error){
                    _this.templatesCustomList = [{
                        "id":  "125dab6b-5692-3dbb-ab39-57663acc59a5",
                        "groupId":  "61a3e170-016c-1000-60bc-d51087f4578f",
                        "name":  "Test Data",
                        "description":  "sss ",
                        "timestamp":  "11/28/2019 16:58:08 CST",
                        "additions":
                            {
                                "MODIFIED_USER":  "admin@orchsym.com",
                                "MODIFIED_TIMESTAMP":  "1574932922677",
                                "IS_FAVORITE":  "true",
                                "SOURCE_TYPE":  "SAVE_AS",
                                "TEMPLATE_TYPE":  "APPLICATION",
                                "CREATED_USER":  "admin@orchsym.com",
                                "CREATED_TIMESTAMP":  "1574931488485"
                            },
                        "encoding-version":  "1.2"
                    }];
                    _this.templatesCustomBackList = [{
                        "id":  "125dab6b-5692-3dbb-ab39-57663acc59a5",
                        "groupId":  "61a3e170-016c-1000-60bc-d51087f4578f",
                        "name":  "Test Data",
                        "description":  "sss ",
                        "timestamp":  "11/28/2019 16:58:08 CST",
                        "additions":
                            {
                                "MODIFIED_USER":  "admin@orchsym.com",
                                "MODIFIED_TIMESTAMP":  "1574932922677",
                                "IS_FAVORITE":  "true",
                                "SOURCE_TYPE":  "SAVE_AS",
                                "TEMPLATE_TYPE":  "APPLICATION",
                                "CREATED_USER":  "admin@orchsym.com",
                                "CREATED_TIMESTAMP":  "1574931488485"
                            },
                        "encoding-version":  "1.2"
                    }];
                });
            },

            search: function(){
                var text = $("#search-template-input").val();

                this.templatesOfficialList = this.templatesOfficialBackList.filter(function(item){
                    return item.name.toLocaleLowerCase().indexOf(text.toLocaleLowerCase()) !== -1;
                })
                this.templatesFavoritesList = this.templatesFavoritesBackList.filter(function(item){
                    return item.name.toLocaleLowerCase().indexOf(text.toLocaleLowerCase()) !== -1;
                })
                this.templatesCustomList = this.templatesCustomBackList.filter(function(item){
                    return item.name.toLocaleLowerCase().indexOf(text.toLocaleLowerCase()) !== -1;
                })
            },

            draggableTemplatesConfig: function (templates) {
                return {
                    zIndex: 1011,
                    revert: true,
                    revertDuration: 0,
                    cancel: false,
                    containment: 'body',
                    cursor: '-webkit-grabbing',
                    start: function (e, ui) {
                        // hide the context menu if necessary
                        // nfContextMenu.hide();
                        // $(e.target).addClass("drag-item");
                        // $(e.target).css({
                        //     marginTop: $(e.target).offsetTop
                        // });
                        // console.log("start", $(e.target), $(e.target).attr("class"))
                    },
                    stop: function (e, ui) {
                        var translate = nfCanvasUtils.getCanvasTranslate();
                        var scale = nfCanvasUtils.getCanvasScale();

                        var mouseX = e.originalEvent.pageX;
                        var mouseY = e.originalEvent.pageY - nfCanvasUtils.getCanvasOffset();

                        // invoke the drop handler if we're over the canvas
                        if (mouseX >= 0 && mouseY >= 0) {
                            // adjust the x and y coordinates accordingly
                            var x = (mouseX / scale) - (translate[0] / scale);
                            var y = (mouseY / scale) - (translate[1] / scale);
                            createTemplate(templates.id, {x:x, y:y});
                        }
                        // $(e.target).removeClass("drag-item");
                        // $(e.target).css({
                        //     marginTop: 0
                        // });
                    },
                    helper: function (event) {
                        var marginTop = $('#template-list').scrollTop()+10;
                        var marginLeft = event.originalEvent.pageX - $('#template-panel')[0].offsetLeft-22;
                        console.log("marginLeft", event.originalEvent.pageX , $('#template-panel')[0].offsetLeft, marginLeft)
                        return $('<div style="width:44px;height:44px;margin-left:' + marginLeft + 'px;margin-top:'+marginTop+'px"><span class="right-icon hicon-template" style="font-size:28px;" /></div>');
                    }
                }
            }
        };

        var templatesControlsCtrl = new TemplatesControlsCtrl(navigateCtrl, operateCtrl);
        templatesControlsCtrl.register();
        return templatesControlsCtrl;
    };
}));

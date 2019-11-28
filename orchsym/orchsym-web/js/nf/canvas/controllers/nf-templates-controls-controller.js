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
                'nf.ErrorHandler'],
            function ($, nfActions, nfBirdseye, nfStorage, nfCanvasUtils, nfProcessGroupConfiguration, nfErrorHandler) {
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
                require('nf.ErrorHandler')));
    } else {
        nf.ng.Canvas.TemplatesControlsCtrl = factory(root.$,
            root.nf.Actions,
            root.nf.Birdseye,
            root.nf.Storage,
            root.nf.CanvasUtils,
            root.nf.ProcessGroupConfiguration,
            root.nf.ErrorHandler);
    }
}(this, function ($, nfActions, nfBirdseye, nfStorage, nfCanvasUtils, nfProcessGroupConfiguration, nfErrorHandler) {
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
                official: apiHost + "/orchsym-api/template/official/search",
                favorites: apiHost + "/orchsym-pro-api/favorites/templates/search",
                custom: apiHost + "/nifi-api/orchsym-template/custom/search"
            }
        };

        function TemplatesControlsCtrl(navigateCtrl, operateCtrl) {
            this.navigateCtrl = navigateCtrl;
            this.operateCtrl = operateCtrl;
        }

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
                });
                $.ajax({
                    type: 'GET',
                    url: config.urls.favorites,
                    dataType: 'json'
                }).done(function (response) {
                    // ensure there are groups specified
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
                });
                $.ajax({
                    type: 'GET',
                    url: config.urls.custom,
                    dataType: 'json'
                }).done(function (response) {
                    // ensure there are groups specified
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
                });
            }
        };

        var templatesControlsCtrl = new TemplatesControlsCtrl(navigateCtrl, operateCtrl);
        templatesControlsCtrl.register();
        return templatesControlsCtrl;
    };
}));

/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
Ext.define('Ssp.controller.tool.map.SemesterPanelContainerViewController', {
    extend: 'Deft.mvc.ViewController',
    mixins: [ 'Deft.mixin.Injectable' ],
    inject:{
		appEventsController: 'appEventsController',
    	termsStore:'termsStore',
    	mapPlanService:'mapPlanService',
		formUtils: 'formRendererUtils',
		person: 'currentPerson',
		authenticatedPerson: 'authenticatedPerson',
        personLite: 'personLite',
    	currentMapPlan: 'currentMapPlan',
		electiveStore : 'electiveStore',
    },
    
	control: {
	    	view: {
				afterlayout: {
					fn: 'onAfterLayout',
					single: true
				},
	    	},
	},

	semesterStores: [],
	init: function() {
		var me=this;
		var id = me.personLite.get('id');
	    me.resetForm();

		me.appEventsController.assignEvent({eventName: 'onLoadMapPlan', callBackFunc: me.onLoadMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onLoadTemplatePlan', callBackFunc: me.onLoadTemplatePlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onCreateNewMapPlan', callBackFunc: me.onCreateNewMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onShowMain', callBackFunc: me.onShowMain, scope: me});
		
		me.appEventsController.assignEvent({eventName: 'onSaveAsTemplatePlan', callBackFunc: me.onSaveAsTemplatePlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onSaveTemplatePlan', callBackFunc: me.onSaveTemplatePlan, scope: me});
		
		me.appEventsController.assignEvent({eventName: 'onSaveAsMapPlan', callBackFunc: me.onSaveAsMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onSaveMapPlan', callBackFunc: me.onSaveMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onPrintMapPlan', callBackFunc: me.onPrintMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onShowMapPlanOverView', callBackFunc: me.onShowMapPlanOverView, scope: me});
		me.appEventsController.assignEvent({eventName: 'onEmailMapPlan', callBackFunc: me.onEmailMapPlan, scope: me});

		me.appEventsController.assignEvent({eventName: 'updateAllPlanHours', callBackFunc: me.updateAllPlanHours, scope: me});

		return me.callParent(arguments);
    },
    resetForm: function() {
        var me = this;
        me.getView().getForm().reset();
    },
    
    newServiceSuccessHandler: function(name, callback, serviceResponses) {
        var me = this;
        return me.newServiceHandler(name, callback, serviceResponses, function(name, serviceResponses, response) {
            serviceResponses.successes[name] = response;
        });
    },

    newServiceFailureHandler: function(name, callback, serviceResponses) {
        var me = this;
        return me.newServiceHandler(name, callback, serviceResponses, function(name, serviceResponses, response) {
            serviceResponses.failures[name] = response;
        });
    },

    newServiceHandler: function(name, callback, serviceResponses, serviceResponsesCallback) {
        return function(r, scope) {
            var me = scope;
            serviceResponses.responseCnt++;
            if ( serviceResponsesCallback ) {
                serviceResponsesCallback.apply(me, [name, serviceResponses, r]);
            }
            if ( callback ) {
                callback.apply(me, [ serviceResponses ]);
            }
            me.afterServiceHandler(serviceResponses);
        };
    },

    getMapPlanServiceSuccess: function(serviceResponses, isTemplate) {
        var me = this;
        var mapResponse = serviceResponses.successes.map;
		if(!mapResponse || !mapResponse.responseText || mapResponse.responseText.trim().length == 0) {
			if(me.termsStore.isLoading()) {
				 me.termsStore.on('load', this.getMapPlanServiceFailure, this, {single: true});
				return;
			}
			//Open Load Saved Plan Screen
	        var me=this; 
			if(me.allPlansPopUp == null || me.allPlansPopUp.isDestroyed)
				me.allPlansPopUp = Ext.create('Ssp.view.tools.map.LoadPlans',{hidden:true,onInit:true});
			me.allPlansPopUp.show();
       	} else {
			me.currentMapPlan.loadFromServer(Ext.decode(mapResponse.responseText));
			if(isTemplate){
				me.currentMapPlan.set("planCourses", me.currentMapPlan.get('templateCourses'));
			}
			me.onCreateMapPlan();
			me.populatePlanStores();
			me.updateAllPlanHours();
			me.currentMapPlan.set('isTemplate', false);
		}
    },

    onLoadMapPlan: function () {
    	var me = this;
		me.onCreateMapPlan();
		me.populatePlanStores();
		me.updateAllPlanHours();  
		me.getView().setLoading(false);
    },
    
    onLoadTemplatePlan: function () {
    	var me = this;
    	me.currentMapPlan.set("planCourses", me.currentMapPlan.get('templateCourses'));
    	me.currentMapPlan.set('isTemplate', true);
		me.onCreateMapPlan();
		me.populatePlanStores();
		me.updateAllPlanHours();  
		me.getView().setLoading(false);
    },
    
    getMapPlanServiceFailure: function() {
		var me = this;
		me.onCreateNewMapPlan();
		me.updateAllPlanHours();
		me.currentMapPlan.set('personId',me.personLite.get('id'));
		me.currentMapPlan.set('ownerId',me.personLite.get('id'));
    },
 
	onAfterLayout: function(){
		var me = this;

		me.getView().setLoading(true);
		if(me.termsStore.getTotalCount() == 0){
			me.termsStore.addListener("load", me.onTermsStoreLoad, me);
			me.termsStore.load();
		}else{
			me.fireInitialiseMap();
		}
	},
	
	fireInitialiseMap: function(){
		var me = this;
		var id = me.personLite.get('id');
	    
	    if (id != "") {
			me.getView().setLoading(true);
			var serviceResponses = {
                failures: {},
                successes: {},
                responseCnt: 0,
                expectedResponseCnt: 1
            }
	    	 me.mapPlanService.getCurrent(id, {
	             success: me.newServiceSuccessHandler('map', me.getMapPlanServiceSuccess, serviceResponses),
	             failure: me.newServiceFailureHandler('map', me.getMapPlanServiceFailure, serviceResponses),
	             scope: me
	         });
	    }
	},
	
	getTerms: function(mapPlan){
		var me = this;
		var terms;
		me.semesterStores = [];
		var termsStore = me.termsStore.getCurrentAndFutureTermsStore(5);			
		if(mapPlan){
			var mapTerms = me.termsStore.getTermsFromTermCodes(me.mapPlanService.getTermCodes(mapPlan));
			mapTerms.forEach(function(mapTerm){
				if(termsStore.find("code", mapTerm.get("code")) < 0)
						termsStore.add(mapTerm);	
			});
		}
		termsStore.sort('startDate','ASC');
		return termsStore.getRange(0);
	},
	
	fillSemesterStores: function(terms){
		var me = this;
		terms.forEach(function(term){
			me.semesterStores[term.get('code')] = new Ssp.store.SemesterCourses();
		});
	},
	
	onTermsStoreLoad:function(){
		var me = this;
		me.termsStore.removeListener( "onTermsStoreLoad", me.onTermsStoreLoad, me );
		me.fireInitialiseMap();
	},
	
	onCreateNewMapPlan:function(){
		var me = this;
		me.currentMapPlan.clearMapPlan();
		me.currentMapPlan.set('personId',  me.personLite.get('id'));
		me.currentMapPlan.set('ownerId',  me.authenticatedPerson.get('id'));
		me.currentMapPlan.set('name','New Plan');
		me.currentMapPlan.set('termNotes',[]);
		me.onCreateMapPlan();
		me.populatePlanStores();
		me.updateAllPlanHours();
	},


	populatePlanStores:function(){
		var me = this;
		var planCourses = me.currentMapPlan.get('planCourses');
		if(planCourses && planCourses.length > 0){
			planCourses.forEach(function(planCourse){
				var termStore = me.semesterStores[planCourse.termCode];
				termStore.suspendEvents();
				var semesterCourse = new Ssp.model.tool.map.SemesterCourse(planCourse);
				termStore.add(semesterCourse);
				termStore.resumeEvents();
			}) 
		}
	},
	
	onCreateMapPlan:function(){
		var me = this;
		var view  = me.getView().getComponent("semestersets");
		if(view == null){
			return;
		}
		view.removeAll(true);

		var terms = me.getTerms(me.currentMapPlan);
		
		if(terms.length == 0){
			return;
		}
		
		me.fillSemesterStores(terms);

		var termsets = [];
		terms.forEach(function(term){
			if(termsets.hasOwnProperty(term.get("reportYear"))){
				termsets[term.get("reportYear")][termsets[term.get("reportYear")].length] = term;
			}else{
				termsets[term.get("reportYear")]=[];
				termsets[term.get("reportYear")][0] = term;
			};
		});

		termsets.forEach(function(termSet){
			var yearView = view.add(new Ext.form.FieldSet({
				xtype : 'fieldset',
				border: 0,
				title : '',
				padding : '2 2 2 2',
				margin : '0 0 0 0',
				layout : 'hbox',
				autoScroll : true,
				minHeight: 204,
				itemId : 'year' + termSet[0].get("reportYear"),
				flex : 1,
			}));
		
			termSet.forEach(function(term){
				var termCode = term.get('code');
				var panelName = term.get("name");
				var isPast = me.termsStore.isPastTerm(termCode);
				yearView.add(me.createSemesterPanel(panelName, termCode, me.semesterStores[termCode]));
			});
		});
		me.appEventsController.getApplication().fireEvent("onUpdateCurrentMapPlanPlanToolView");
		me.getView().setLoading(false);
    },
	
	createSemesterPanel: function(semesterName, termCode, semesterStore){
		var me = this;
		
		var semesterPanel = new Ssp.view.tools.map.SemesterPanel({
			title:semesterName,
			itemId:termCode,
			store:semesterStore
		});
		
		if(!me.termsStore.isPastTerm(termCode)){
		 	var semesterGrid = new Ssp.view.tools.map.SemesterGrid({
				store:semesterStore,
				scroll: true,
			});
			semesterPanel.add(semesterGrid);
		}else{
		 	var semesterGrid = new Ssp.view.tools.map.SemesterGrid({
				store:semesterStore,
				scroll: true,
				enableDragAndDrop: false
			});
			semesterPanel.add(semesterGrid);
		}

		return semesterPanel;
	},
	
	afterServiceHandler: function(serviceResponses) {
        var me = this;
        if ( serviceResponses.responseCnt >= serviceResponses.expectedResponseCnt ) {
            //me.getView().setLoading(false);
        }
    },

	onSaveAsMapPlan: function(){
		var me = this;
		me.save(true);


	},
	onSaveMapPlan: function(){
		var me = this;
		me.save(false);
	},
		
	save: function(saveAs) {
		var me = this;
		me.getView().setLoading(true);
		var callbacks = new Object();
		 var serviceResponses = {
            failures: {},
            successes: {},
            responseCnt: 0,
            expectedResponseCnt: 1
        }
    callbacks.success = me.newServiceSuccessHandler('map', me.onSaveCompleteSuccess, serviceResponses);
    callbacks.failure = me.newServiceFailureHandler('map', me.onSaveCompleteFailure, serviceResponses);
    callbacks.scope = me;
    me.mapPlanService.save(me.semesterStores, callbacks, me.currentMapPlan, me.getView(), saveAs);
  },
  
  onSaveAsTemplatePlan: function(){
    var me = this;
    me.saveTemplate(true);
  },
  onSaveTemplatePlan: function(){
    var me = this;
    me.saveTemplate(false);
  },  
  
  
  saveTemplate: function(saveAs) {
    var me = this;
    me.getView().setLoading(true);
    var callbacks = new Object();
    var serviceResponses = {
            failures: {},
            successes: {},
            responseCnt: 0,
            expectedResponseCnt: 1
        }
    callbacks.success = me.newServiceSuccessHandler('map', me.onSaveTemplateCompleteSuccess, serviceResponses);
    callbacks.failure = me.newServiceFailureHandler('map', me.onSaveTemplateCompleteFailure, serviceResponses);
    callbacks.scope = me;
    me.mapPlanService.saveTemplate(me.semesterStores, callbacks, me.currentMapPlan, me.getView(), saveAs);
  },
	onShowMain: function(){
    	var me=this;
    	var mainView = Ext.ComponentQuery.query('mainview')[0];
    	var arrViewItems;
    	
    	if (mainView.items.length > 0)
		{
			mainView.removeAll();
		}
		
		arrViewItems = [{xtype:'search',flex: 2},
					    {xtype: 'studentrecord', flex: 4}];
		
		mainView.add( arrViewItems );	
	},	
	
	onSaveCompleteSuccess: function(serviceResponses){
		var me = this;
		Ext.Msg.alert('Your changes have been saved.'); 
		me.getMapPlanServiceSuccess(serviceResponses);
		me.currentMapPlan.set("isTemplate", false)
		me.getView().setLoading(false);
	},
	
	onSaveCompleteFailure: function(serviceResponses){
		var me = this;
		me.getView().setLoading(false);
	},	

	onSaveTemplateCompleteSuccess: function(serviceResponses){
    var me = this;
    me.getMapPlanServiceSuccess(serviceResponses, true);
    
    Ext.Msg.alert('Your changes have been saved.');   
    me.getView().setLoading(false);
  },
  
  onSaveTemplateCompleteFailure: function(serviceResponses){
    var me = this;
    me.getView().setLoading(false);
  },  
	updateAllPlanHours: function(){
		var me = this;
		var parent =  me.getView();
		var panels = parent.query("semesterpanel");
		var planHours = 0;
		var devHours = 0;
		panels.forEach(function(panel){
			var storeGrid = panel.query("semestergrid")[0];
			var store = storeGrid.getStore();
			var semesterBottomDock = panel.getDockedComponent("semesterBottomDock");
			
			var hours = me.updateTermHours(store, semesterBottomDock);
			planHours += hours.planHours;
			devHours += hours.devHours;
		})
		Ext.getCmp('currentTotalPlanCrHrs').setValue(planHours);
		Ext.getCmp('currentPlanTotalDevCrHrs').setValue(devHours);
		
	},
	
	updateTermHours: function(store, semesterBottomDock){
		var models = store.getRange(0);
		var totalHours = 0;
		var totalDevHours = 0;
		models.forEach(function(model){
			var value = model.get('creditHours');
			/**** TODO the following is in the constructor but drag and drop does not call the constructor *****/
			if(!value){
				value = model.get('minCreditHours');
				model.set('creditHours', value);
			}
			totalHours += value;
			if(model.get('isDev')){
				totalDevHours += value;
			}
		});
		var termCreditHours = semesterBottomDock.getComponent('termCrHrs');
		termCreditHours.setText("" + totalHours + "");
		var hours = new Object();
		hours.planHours = totalHours;
		hours.devHours = totalDevHours;
		return hours;
	},
	

	
	onEmailMapPlan: function(metaData){
		var me = this;
		me.getView().setLoading(true);
		var serviceResponses = {
                failures: {},
                successes: {},
                responseCnt: 0,
                expectedResponseCnt: 1
            }
		me.mapPlanService.email(me.semesterStores, metaData, {
            success: me.newServiceSuccessHandler('emailMap', me.emailMapPlanServiceSuccess, serviceResponses),
            failure: me.newServiceFailureHandler('emailMap', me.emailMapPlanServiceFailure, serviceResponses),
            scope: me
        });
	},
	
	 emailMapPlanServiceSuccess: function(serviceResponses) {
	        var me = this;
	        var mapResponse = serviceResponses.successes.emailMap;
	       	me.onEmailComplete(mapResponse.responseText);
			me.getView().setLoading(false);
	 },

	emailMapPlanServiceFailure: function() {
		var me = this;
		me.getView().setLoading(false);
	},
	
	onEmailComplete: function(responseText){
		Ext.Msg.alert('SSP Email Service', responseText);
	},
	
	
	onPrintMapPlan: function(metaData){
		var me = this;
		me.getView().setLoading(true);
		var serviceResponses = {
                failures: {},
                successes: {},
                responseCnt: 0,
                expectedResponseCnt: 1
            }
		me.mapPlanService.print(me.semesterStores, metaData, {
            success: me.newServiceSuccessHandler('printMap', me.printMapPlanServiceSuccess, serviceResponses),
            failure: me.newServiceFailureHandler('printMap', me.printMapPlanServiceFailure, serviceResponses),
            scope: me
        });
	},
	
	onShowMapPlanOverView: function(){
		var me = this;
		me.getView().setLoading(true);
		var serviceResponses = {
                failures: {},
                successes: {},
                responseCnt: 0,
                expectedResponseCnt: 1,
				show: true,
            }
		var metaData = new Ssp.model.tool.map.PlanOutputData();
		metaData.set('outputFormat', 'fullFormat');
		metaData.set('includeCourseDescription', true);
		metaData.set('includeHeaderFooter', true);
		metaData.set('includeTotalTimeExpected', true);
		metaData.set('includeFinancialAidInformation', true);
		
		me.mapPlanService.print(me.semesterStores, metaData, {
            success: me.newServiceSuccessHandler('printMap', me.printMapPlanServiceSuccess, serviceResponses),
            failure: me.newServiceFailureHandler('printMap', me.printMapPlanServiceFailure, serviceResponses),
            scope: me
        });
	},
	
	 printMapPlanServiceSuccess: function(serviceResponses) {
	        var me = this;
	        var mapResponse = serviceResponses.successes.printMap;
	       	me.onPrintComplete(mapResponse.responseText, serviceResponses.show);
			me.getView().setLoading(false);
	 },

	printMapPlanServiceFailure: function() {
		var me = this;
		me.getView().setLoading(false);
	},
	
	onPrintComplete: function(htmlPrint, show){
		var me = this;
        var myWindow = window.open('', '', 'width=500,height=600,scrollbars=yes');
        myWindow.document.write(htmlPrint);
		myWindow.document.title = me.currentMapPlan.get("name");
		if(!show)
        	myWindow.print();
	},

	destroy: function() {
        var me=this;
		if(me.coursePlanDetails != null && !me.coursePlanDetails.isDestroyed){
			me.coursePlanDetails.close();
		}
		me.termsStore.removeListener("load", me.onCreateNewMapPlan, me);
		me.appEventsController.removeEvent({eventName: 'onCreateNewMapPlan', callBackFunc: me.onCreateNewMapPlan, scope: me});
        
		me.appEventsController.removeEvent({eventName: 'onPrintMapPlan', callBackFunc: me.onPrintMapPlan, scope: me});
		me.appEventsController.assignEvent({eventName: 'onShowMapPlanOverView', callBackFunc: me.onShowMapPlanOverView, scope: me});
		me.appEventsController.removeEvent({eventName: 'onEmailMapPlan', callBackFunc: me.onEmailMapPlan, scope: me});
		me.appEventsController.removeEvent({eventName: 'onShowMain', callBackFunc: me.onCreateNewMapPlan, scope: me});
		me.appEventsController.removeEvent({eventName: 'onSaveAsMapPlan', callBackFunc: me.onSaveAsMapPlan, scope: me});
		me.appEventsController.removeEvent({eventName: 'onSaveMapPlan', callBackFunc: me.onSaveMapPlan, scope: me});
		
		me.appEventsController.removeEvent({eventName: 'onSaveAsTemplatePlan', callBackFunc: me.onSaveAsTemplatePlan, scope: me});
		me.appEventsController.removeEvent({eventName: 'onSaveTemplatePlan', callBackFunc: me.onSaveTemplatePlan, scope: me});

		  
        me.appEventsController.removeEvent({eventName: 'updateAllPlanHours', callBackFunc: me.updateAllPlanHours, scope: me});
		me.appEventsController.removeEvent({eventName: 'onLoadMapPlan', callBackFunc: me.onLoadMapPlan, scope: me});
		me.appEventsController.removeEvent({eventName: 'onLoadTemplatePlan', callBackFunc: me.onLoadMapPlan, scope: me});
        
		return me.callParent( arguments );
    },
});
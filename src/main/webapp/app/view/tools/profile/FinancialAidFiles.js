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
Ext.define('Ssp.view.tools.profile.FinancialAidFiles', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.financialAidFiles',
    mixins: ['Deft.mixin.Injectable'],
    store:Ext.create('Ext.data.Store', {
    	fields: [{name: 'code', type: 'string'},
                 {name: 'description', type: 'string'},
                 {name: 'status', type: 'string'},
                 {name: 'name', type: 'string'}]
	}),
    width: '100%',
    height: '100%',
    initComponent: function(){
        var me = this;
        Ext.apply(me, {
            queryMode:'local',
            store: me.store,
            autoScroll: true,
            columns: [{
            	xtype: 'gridcolumn',
                text: 'File Code',
                dataIndex: 'code',
                flex: 0.2
            },{
            	xtype: 'gridcolumn',
            	text: 'File Name',
                dataIndex: 'name',
                flex: 0.2
            },{
            	xtype: 'gridcolumn',
            	text: 'File Description',
                dataIndex: 'description',
                flex: 0.4
            },{
            	xtype: 'gridcolumn',
                text: 'File Status',
                dataIndex: 'status',
                flex: 0.2
            }]
        });
        
        return me.callParent(arguments);
    }
});
/*******************************************************************************
 * Copyright 2012 Fatminds, Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.fatminds.vaadin_cmis_integration.demo;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.dellroad.stuff.vaadin.SpringContextApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.fatminds.vaadin.cmis.CmisContainer;
import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;


/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class AddonDemoApplication extends SpringContextApplication
{
	
	private static final Logger log = LoggerFactory.getLogger(AddonDemoApplication.class);
	
	public static final String DEMO_SUBFOLDER = "cmis-demo";
	
    private Window window;
    
	@Override
	protected void initSpringApplication(ConfigurableWebApplicationContext arg0) {
        window = new Window("CMIS / Alfresco integration");
        setMainWindow(window);
        DemoPage demo = new DemoPage();
        window.addComponent(demo);
        demo.init();
	}
	
}

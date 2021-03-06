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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fatminds.cmis.form.CmisFormTemplate;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class DemoTemplateForm extends CustomComponent implements CmisFormTemplate{

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private GridLayout gridLayout_1;
	@AutoGenerated
	private TextField txtDataItemToAddEdit;
	@AutoGenerated
	private Label label_1;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	/**
	 * The constructor should first build the main layout, set the
	 * composition root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the
	 * visual editor.
	 */
	public DemoTemplateForm() {
		buildMainLayout();
		setCompositionRoot(mainLayout);
		
}

	public HashMap<String, Component> getCmisFieldComponents() {
		HashMap<String, Component> componentMap = new HashMap<String, Component>();
		componentMap.put("cmiscustom:docprop_string", txtDataItemToAddEdit);
		return componentMap;
	}

	public List<ComponentContainer> getComponentContainers() {
		List<ComponentContainer> layouts = new ArrayList<ComponentContainer>();
		layouts.add(mainLayout);
		layouts.add(gridLayout_1);
		return layouts;
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("350px");
		mainLayout.setHeight("110px");
		mainLayout.setMargin(false);
		mainLayout.setSpacing(true);
		
		// top-level component properties
		setWidth("350px");
		setHeight("110px");
		
		// gridLayout_1
		gridLayout_1 = buildGridLayout_1();
		mainLayout.addComponent(gridLayout_1);
		mainLayout.setComponentAlignment(gridLayout_1, new Alignment(20));
		
		return mainLayout;
	}

	@AutoGenerated
	private GridLayout buildGridLayout_1() {
		// common part: create layout
		gridLayout_1 = new GridLayout();
		gridLayout_1.setImmediate(false);
		gridLayout_1.setWidth("350px");
		gridLayout_1.setHeight("110px");
		gridLayout_1.setMargin(true);
		gridLayout_1.setSpacing(true);
		gridLayout_1.setRows(2);
		
		// label_1
		label_1 = new Label();
		label_1.setImmediate(false);
		label_1.setWidth("-1px");
		label_1.setHeight("-1px");
		label_1.setValue("Edit cmiscustom:docprop_string");
		gridLayout_1.addComponent(label_1, 0, 0);
		
		// txtDataItemToAddEdit
		txtDataItemToAddEdit = new TextField();
		txtDataItemToAddEdit.setImmediate(false);
		txtDataItemToAddEdit.setWidth("300px");
		txtDataItemToAddEdit.setHeight("24px");
		txtDataItemToAddEdit.setInputPrompt("Enter string value");
		txtDataItemToAddEdit.setSecret(false);
		gridLayout_1.addComponent(txtDataItemToAddEdit, 0, 1);
		
		return gridLayout_1;
	}

}

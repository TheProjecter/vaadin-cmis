package com.fatminds.vaadin.cmis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.vaadin.addon.customfield.FieldWrapper;
import org.vaadin.addon.customfield.PropertyConverter;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Field;

public class CmisManyField extends FieldWrapper<ArrayList> {
	
	private static final long serialVersionUID = 1L;

	
	public CmisManyField(Field wrappedField,
			PropertyConverter<ArrayList, Set> converter,
			Class<ArrayList> class1) {
		super(wrappedField, converter, class1);
		setCompositionRoot(wrappedField);
		
	}
	
	
	
}

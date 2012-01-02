package com.fatminds.vaadin.cmis.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.vaadin.addon.customfield.PropertyConverter;

import com.vaadin.data.Property.ConversionException;

public class CalendarPropertyConverter extends PropertyConverter<GregorianCalendar, Date> {

	private static final Log log = LogFactory.getLog(CalendarPropertyConverter.class);

	private static final long serialVersionUID = 1L;

	public CalendarPropertyConverter() {
		super(GregorianCalendar.class);
	}



   @Override
    public Date format(GregorianCalendar value) {
    	if (value == null){
    		return null;
    	}
    	return value.getTime();
    }

    @Override
    public GregorianCalendar parse(Date value) throws ConversionException {
    	if (value == null){
    		return null;
    	}
       	GregorianCalendar cal = new GregorianCalendar();
    	cal.setTime(value);
    	return cal;
     }
    
    @Override
    public Class getType() {
        return Date.class;
    }
    
	
}

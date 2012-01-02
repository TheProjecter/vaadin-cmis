package com.fatminds.vaadin.cmis;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.vaadin.addon.customfield.FieldWrapper;
import org.vaadin.addon.customfield.PropertyConverter;

import com.fatminds.vaadin.cmis.property.CalendarPropertyConverter;
import com.fatminds.vaadin.cmis.property.DatePropertyConverter;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.PopupDateField;

public class CmisDateField extends FieldWrapper<GregorianCalendar> {

    private PopupDateField dateField;

    public CmisDateField(String caption) {

        super(new PopupDateField(), new CalendarPropertyConverter(), GregorianCalendar.class);

        this.dateField = (PopupDateField)getWrappedField();
        this.dateField.setValue(new Date());
        this.dateField.setResolution(PopupDateField.RESOLUTION_DAY);


        setCaption(caption);
        setCompositionRoot(this.dateField);
    }

    /*
    @Override
    protected Date format(GregorianCalendar value) {
    	if (value == null){
    		return null;
    	}
    	return value.getTime();
    }

    @Override
    protected GregorianCalendar parse(Object formattedValue) throws ConversionException {
       	GregorianCalendar cal = new GregorianCalendar();
    	cal.setTime((Date)formattedValue);
    	return cal;
     }*/
}

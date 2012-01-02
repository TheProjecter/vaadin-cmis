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
import com.vaadin.ui.InlineDateField;
import com.vaadin.ui.PopupDateField;

public class CmisDateTimeInlineField extends FieldWrapper<GregorianCalendar> {

    private InlineDateField datetimeField;

    public CmisDateTimeInlineField(String caption) {

        super(new InlineDateField(), new CalendarPropertyConverter(), GregorianCalendar.class);

        this.datetimeField = (InlineDateField)getWrappedField();
        this.datetimeField.setValue(new Date());
        this.datetimeField.setResolution(InlineDateField.RESOLUTION_MIN);
        this.datetimeField.setShowISOWeekNumbers(true);


        setCaption(caption);
        setCompositionRoot(this.datetimeField);
    }

 
}

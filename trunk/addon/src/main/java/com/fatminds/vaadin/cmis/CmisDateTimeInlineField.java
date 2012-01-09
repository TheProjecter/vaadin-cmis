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

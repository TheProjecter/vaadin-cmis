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

public class DatePropertyConverter extends PropertyConverter<Date, Object> {

	private static final Log log = LogFactory.getLog(DatePropertyConverter.class);

	private static final long serialVersionUID = 1L;

	public DatePropertyConverter() {
		super(Date.class);
	}



   @Override
/*    public GregorianCalendar format(Date value) {
    	if (value == null){
    		return null;
    	}
       	GregorianCalendar cal = new GregorianCalendar();
    	cal.setTime(value);
    	return cal;
    }*/

   public Date format(Date value) {
   	return value;
   }
   
    @Override
    public Date parse(Object value) throws ConversionException {
    	if (value instanceof GregorianCalendar){
    		return ((GregorianCalendar)value).getTime();
    	}else{
    		return (Date)value;
    	}
     }
	
}

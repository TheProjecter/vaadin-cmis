/**
 * 
 */
package com.fatminds.vaadin.cmis.validation;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractValidator;

/**
 * @author vijay
 *
 */
public class UrlValidator extends AbstractValidator{

	protected String[] schemes = {"http","https"};
	protected org.apache.commons.validator.UrlValidator urlValidator = new org.apache.commons.validator.UrlValidator(schemes);

	public UrlValidator(String errorMessage) {
	        super(errorMessage);
	}
	
	// The isValid() method returns simply a boolean value, so
    // it can not return an error message.
    public boolean isValid(Object value) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof String)) {
        	throw new IllegalArgumentException("UrlValidator expects String input, got a " + value.getClass());
        }
        String url = (String)value;
        if (!url.startsWith("http")){
        	url = "http://" + url;
        }
        return urlValidator.isValid(url);
    }

    // Upon failure, the validate() method throws an exception
    // with an error message.
    /*
    public void validate(Object value)
                throws InvalidValueException {
        if (!isValid(value)) {
            if (value != null &&
                !value.toString().startsWith("")) {
                throw new InvalidValueException(
                    "Invalid Url. Please correct the url!");
            } 
        }
    }*/

}

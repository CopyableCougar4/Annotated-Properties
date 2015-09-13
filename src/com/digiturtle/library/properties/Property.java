package com.digiturtle.library.properties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents an annotated property that is saved and loaded by a Properties instance
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
	
	/**
	 * This value is used to mark a unique value for storing the property
	 * if the object name is generic or repeated
	 */
	public String alias() default "";

}

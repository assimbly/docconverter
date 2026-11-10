package org.assimbly.docconverter;

/**
 * Unchecked failure from {@link DocConverter} or {@link StringConverter}.
 * The original cause is preserved and can be inspected with {@link #getCause()}.
 */
public class DocConversionException extends RuntimeException {

	public DocConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}

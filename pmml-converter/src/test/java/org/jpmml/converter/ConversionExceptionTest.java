/*
 * Copyright (c) 2026 Villu Ruusmann
 *
 * This file is part of JPMML-Converter
 *
 * JPMML-Converter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-Converter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-Converter.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jpmml.converter;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConversionExceptionTest {

	@Test
	public void printStackTrace(){
		Object context = new Formattable(){

			@Override
			public String format(){
				return "Here";
			}
		};

		ConversionException exception = new ConversionException("Can't do that")
			.setSolution("Do this")
			.setExample("example 1\nexample 2\nexample 3")
			.setDocumentation("reference 1\nreference 2")
			.setContext(context);

		ConversionException.SECTION_SEPARATOR = System.lineSeparator();

		String string = printStackTrace(exception);

		assertTrue(string.contains("Solution:\n"));
		assertTrue(string.contains("Example:\n"));
		assertTrue(string.contains("Documentation:\n"));
		assertTrue(string.contains("Context:\n"));
		assertTrue(string.contains("Stack trace:\n"));

		assertEquals(6, countSections(string));

		exception.setStackTrace(new StackTraceElement[0]);

		string = printStackTrace(exception);

		assertFalse(string.contains("Stack trace:\n"));

		assertEquals(5, countSections(string));

		ConversionException.SECTION_SEPARATOR = "";

		string = printStackTrace(exception);

		assertEquals(1, countSections(string));
	}

	static
	private String printStackTrace(Exception exception){
		StringWriter stringWriter = new StringWriter();

		try(PrintWriter printWriter = new PrintWriter(stringWriter)){
			exception.printStackTrace(printWriter);
		}

		return stringWriter.toString();
	}

	static
	private int countSections(String string){
		return (string.split("\n\n")).length;
	}
}
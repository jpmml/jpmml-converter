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

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

public class ConsoleFormatter extends SimpleFormatter {

	@Override
	public String format(LogRecord record){
		String string = super.format(record);

		Throwable throwable = record.getThrown();
		if(throwable != null){
			String throwableString = throwable.toString();

			string = string.replaceFirst("(?m)^" + Pattern.quote(throwableString), ConsoleFormatter.EXCEPTION_HEADER + throwableString);
			string = string.replaceAll("(?m)^\\s*Caused by:\\s*", ConsoleFormatter.CAUSED_BY_HEADER);
		}

		return string;
	}

	static
	private String header(String text, char separator){
		StringBuilder sb = new StringBuilder();

		int indent = (ConsoleFormatter.HEADER_WIDTH - text.length()) / 2;

		sb.append(System.lineSeparator());
		sb.append(String.valueOf(separator).repeat(ConsoleFormatter.HEADER_WIDTH));
		sb.append(System.lineSeparator());
		sb.append(" ".repeat(indent)).append(text);
		sb.append(System.lineSeparator());
		sb.append(String.valueOf(separator).repeat(ConsoleFormatter.HEADER_WIDTH));
		sb.append(System.lineSeparator());

		return sb.toString();
	}

	// Identical to Apace Maven line lengths
	public static int HEADER_WIDTH = 79;

	public static String EXCEPTION_HEADER = header("EXCEPTION", '=');
	public static String CAUSED_BY_HEADER = header("Caused by", '-');
}
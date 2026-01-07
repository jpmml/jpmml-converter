/*
 * Copyright (c) 2025 Villu Ruusmann
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ConversionException extends RuntimeException {

	private Object context = null;

	private String solution = null;

	private String example = null;

	private String documentation = null;


	public ConversionException(String message){
		super(message);
	}

	public ConversionException(String message, Throwable cause){
		super(message, cause);
	}

	@Override
	public String getLocalizedMessage(){
		StringBuilder sb = new StringBuilder();

		sb.append(super.getLocalizedMessage());

		List<String> sections = new ArrayList<>();

		Object context = getContext();
		if(context != null){
			sections.add(formatContext(context));
		}

		String solution = getSolution();
		if(solution != null){
			sections.add(formatSolution(solution));
		}

		String example = getExample();
		if(example != null){
			sections.add(formatExample(example));
		}

		String documentation = getDocumentation();
		if(documentation != null){
			sections.add(formatDocumentation(documentation));
		}

		StackTraceElement[] stackTrace = getStackTrace();
		// Add a "Stack trace" pseudo-section only if there are other custom sections present
		if(!sections.isEmpty() && (stackTrace != null && stackTrace.length > 0)){
			sections.add(formatStackTrace());
		}

		for(Iterator<String> it = sections.iterator(); it.hasNext(); ){
			String section = it.next();

			if(section.isEmpty()){
				continue;
			}

			sb.append(lineSeparator());
			sb.append(sectionSeparator());

			sb.append(section);
		}

		return sb.toString();
	}

	protected String formatContext(Object context){
		return formatSection("Context", context);
	}

	protected String formatSolution(String solution){
		return formatSection("Solution", solution);
	}

	protected String formatExample(String example){
		return formatSection("Example", example);
	}

	protected String formatDocumentation(String documentation){
		return formatSection("Documentation", documentation);
	}

	protected String formatStackTrace(){
		return formatSection("Stack trace", "");
	}

	protected String formatSection(String name, Object value){
		return formatSection(name, String.valueOf(value));
	}

	protected String formatSection(String name, String value){
		String header = MessageFormat.format(ConversionException.SECTION_HEADER_PATTERN, name);

		StringBuilder sb = new StringBuilder();

		sb.append(header);

		if(!value.isEmpty()){
			Stream<String> lines = value.lines();

			lines.forEach(line -> {
				sb.append(lineSeparator());

				sb.append(blockIndent()).append(line);
			});
		}

		return sb.toString();
	}

	protected String lineSeparator(){
		return ConversionException.LINE_SEPARATOR;
	}

	protected String sectionSeparator(){
		return ConversionException.SECTION_SEPARATOR;
	}

	protected String blockIndent(){
		return ConversionException.BLOCK_INDENT;
	}

	public Object getContext(){
		return this.context;
	}

	public ConversionException setContext(Object context){
		this.context = context;

		return this;
	}

	public String getSolution(){
		return this.solution;
	}

	public ConversionException setSolution(String solution){
		this.solution = solution;

		return this;
	}

	public String getExample(){
		return this.example;
	}


	public ConversionException setExample(String example){
		this.example = example;

		return this;
	}

	public String getDocumentation(){
		return this.documentation;
	}

	public ConversionException setDocumentation(String documentation){
		this.documentation = documentation;

		return this;
	}

	@Override
	synchronized
	public ConversionException initCause(Throwable cause){
		return (ConversionException)super.initCause(cause);
	}

	@Override
	synchronized
	public ConversionException fillInStackTrace(){
		return (ConversionException)super.fillInStackTrace();
	}

	public static String LINE_SEPARATOR = System.lineSeparator();
	public static String SECTION_SEPARATOR = "";

	// Match the default indentation of stack trace elements
	public static String BLOCK_INDENT = "\t";

	public static String SECTION_HEADER_PATTERN = "{0}:";
}
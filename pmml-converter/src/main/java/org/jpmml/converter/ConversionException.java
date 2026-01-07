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

		Object context = getContext();
		if(context != null){
			sb.append(System.lineSeparator());

			sb.append(formatContext(context));
		}

		String solution = getSolution();
		if(solution != null){
			sb.append(System.lineSeparator());

			sb.append(formatSolution(solution));
		}

		String example = getExample();
		if(example != null){
			sb.append(System.lineSeparator());

			sb.append(formatExample(example));
		}

		String documentation = getDocumentation();
		if(documentation != null){
			sb.append(System.lineSeparator());

			sb.append(formatDocumentation(documentation));
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

	protected String formatSection(String name, Object value){
		return name + ": " + value;
	}

	protected String formatSection(String name, String value){

		// Single-line
		if(value.indexOf('\n') < 0){
			return name + ": " + value;
		} else

		// Multi-line
		{
			StringBuilder sb = new StringBuilder();

			sb.append(name).append(':');

			Stream<String> lines = value.lines();
			lines.forEach(line -> {
				sb.append(System.lineSeparator());

				sb.append(line);
			});

			return sb.toString();
		}
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
}
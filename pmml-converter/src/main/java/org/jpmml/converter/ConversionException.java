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

public class ConversionException extends RuntimeException {

	private Object context = null;

	private String solution = null;

	private String[] references = null;


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

			sb.append("Context: ").append(getLocalizedContext());
		}

		String solution = getSolution();
		if(solution != null){
			sb.append(System.lineSeparator());

			sb.append("Solution: ").append(solution);
		}

		String[] references = getReferences();
		if(references != null && references.length > 0){
			sb.append(System.lineSeparator());

			sb.append("References:");

			for(String reference : references){
				sb.append(System.lineSeparator());

				sb.append(reference);
			}
		}

		return sb.toString();
	}

	protected String getLocalizedContext(){
		Object context = getContext();

		return String.valueOf(context);
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

	public String[] getReferences(){
		return this.references;
	}

	public ConversionException setReferences(String... references){
		this.references = references;

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
/*
 * Copyright (c) 2014 Villu Ruusmann
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

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import rexp.Rexp;
import rexp.Rexp.STRING;

public class REXPUtil {

	private REXPUtil(){
	}

	static
	public <E> List<E> getRow(List<E> matrix, int k, int rows, int columns){
		List<E> row = Lists.newArrayList();

		for(int i = 0; i < columns; i++){
			row.add(matrix.get((i * rows) + k));
		}

		return row;
	}

	static
	public <E> List<E> getColumn(List<E> matrix, int k, int rows, int columns){
		return matrix.subList(k * rows, (k * rows) + rows);
	}

	static
	public boolean inherits(Rexp.REXP rexp, String name){
		Rexp.REXP clazz = REXPUtil.attribute(rexp, "class");

		for(int i = 0; i < clazz.getStringValueCount(); i++){
			STRING clazzValue = clazz.getStringValue(i);

			if((name).equals(clazzValue.getStrval())){
				return true;
			}
		}

		return false;
	}

	static
	public Rexp.REXP field(Rexp.REXP rexp, String name){
		Rexp.REXP names = attribute(rexp, "names");

		List<String> fields = Lists.newArrayList();

		for(int i = 0; i < names.getStringValueCount(); i++){
			STRING nameValue = names.getStringValue(i);

			if((name).equals(nameValue.getStrval())){
				return rexp.getRexpValue(i);
			}

			fields.add(nameValue.getStrval());
		}

		throw new IllegalArgumentException("Field " + name + " not in " + fields);
	}

	static
	public Rexp.REXP attribute(Rexp.REXP rexp, String name){
		List<String> attributes = Lists.newArrayList();

		for(int i = 0; i < rexp.getAttrNameCount(); i++){

			if((rexp.getAttrName(i)).equals(name)){
				return rexp.getAttrValue(i);
			}

			attributes.add(rexp.getAttrName(i));
		}

		throw new IllegalArgumentException("Attribute " + name + " not in " + attributes);
	}

	static
	public List<String> getStringList(Rexp.REXP rexp){
		Function<STRING, String> function = new Function<STRING, String>(){

			@Override
			public String apply(STRING string){
				return string.getStrval();
			}
		};

		return Lists.transform(rexp.getStringValueList(), function);
	}

	static
	public Integer asInteger(Number number){

		if(number instanceof Integer){
			return (Integer)number;
		}

		double value = number.doubleValue();

		if(DoubleMath.isMathematicalInteger(value)){
			return number.intValue();
		}

		throw new IllegalArgumentException();
	}

	static
	public Double asDouble(Number number){

		if(number instanceof Double){
			return (Double)number;
		}

		return number.doubleValue();
	}
}
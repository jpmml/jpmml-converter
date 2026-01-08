/*
 * Copyright (c) 2019 Villu Ruusmann
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.math.DoubleMath;
import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;

public class TypeUtil {

	private TypeUtil(){
	}

	static
	public DataType getDataType(Object value){

		if(value instanceof String){
			return DataType.STRING;
		} else

		if((value instanceof Byte) || (value instanceof Short) || (value instanceof Integer) || (value instanceof Long)){
			return DataType.INTEGER;
		} else

		if(value instanceof Float){
			return DataType.FLOAT;
		} else

		if(value instanceof Double){
			return DataType.DOUBLE;
		} else

		if(value instanceof Boolean){
			return DataType.BOOLEAN;
		}

		throw new IllegalArgumentException("Expected a Java primitive value, got " + (value != null ? ExceptionUtil.formatClass(value.getClass()) : null));
	}

	static
	public DataType getDataType(String value){

		try {
			Integer.parseInt(value);

			return DataType.INTEGER;
		} catch(NumberFormatException integerNfe){

			try {
				double doubleValue = Double.parseDouble(value);

				if(DoubleMath.isMathematicalInteger(doubleValue)){
					return DataType.INTEGER;
				}

				return DataType.DOUBLE;
			} catch(NumberFormatException doubleNfe){
				return DataType.STRING;
			}
		}
	}

	static
	public DataType getDataType(Collection<?> values){
		return getDataType(values, null);
	}

	static
	public DataType getDataType(Collection<?> values, DataType defaultDataType){

		if(values.isEmpty()){

			if(defaultDataType != null){
				return defaultDataType;
			}

			throw new IllegalArgumentException();
		}

		boolean allStrings = true;

		Set<DataType> dataTypes = new HashSet<>();

		for(Object value : values){
			DataType dataType;

			if(value instanceof String){
				dataType = getDataType((String)value);
			} else

			{
				allStrings = false;

				dataType = getDataType(value);
			}

			dataTypes.add(dataType);
		}

		if(dataTypes.size() == 1){
			return Iterables.getOnlyElement(dataTypes);
		} else

		{
			if(allStrings){
				return DataType.STRING;
			}

			throw new IllegalArgumentException("Expected all values to be of the same data type, got " + ExceptionUtil.formatValues(dataTypes));
		}
	}

	static
	public OpType getOpType(DataType dataType){

		switch(dataType){
			case STRING:
				return OpType.CATEGORICAL;
			case INTEGER:
			case FLOAT:
			case DOUBLE:
				return OpType.CONTINUOUS;
			case BOOLEAN:
				return OpType.CATEGORICAL;
			case DATE:
			case DATE_TIME:
				return OpType.ORDINAL;
			default:
				throw new IllegalArgumentException();
		}
	}
}
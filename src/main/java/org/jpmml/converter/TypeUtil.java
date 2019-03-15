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

import com.google.common.math.DoubleMath;
import org.dmg.pmml.DataType;

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

		throw new IllegalArgumentException();
	}

	static
	public DataType getDataType(Collection<String> values){

		if(values.isEmpty()){
			throw new IllegalArgumentException();
		}

		DataType dataType = DataType.INTEGER;

		for(String value : values){

			switch(dataType){
				case INTEGER:
					try {
						Integer.parseInt(value);

						continue;
					} catch(NumberFormatException integerNfe){

						try {
							double doubleValue = Double.parseDouble(value);

							if(DoubleMath.isMathematicalInteger(doubleValue)){
								continue;
							}

							dataType = DataType.DOUBLE;
						} catch(NumberFormatException doubleNfe){
							dataType = DataType.STRING;
						}
					}
					break;
				case DOUBLE:
					try {
						Double.parseDouble(value);

						continue;
					} catch(NumberFormatException nfe){
						dataType = DataType.STRING;
					}
					break;
				case STRING:
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		return dataType;
	}
}
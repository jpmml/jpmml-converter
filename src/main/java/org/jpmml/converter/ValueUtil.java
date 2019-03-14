/*
 * Copyright (c) 2015 Villu Ruusmann
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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;
import org.dmg.pmml.DataType;

public class ValueUtil {

	private ValueUtil(){
	}

	static
	public boolean isZero(Number number){
		return equals(number, ZERO);
	}

	static
	public boolean isOne(Number number){
		return equals(number, ONE);
	}

	static
	public boolean isZeroLike(Number number){
		return isZero(number) || Double.isNaN(number.doubleValue());
	}

	static
	public Number asNumber(Object object){

		if(object instanceof Number){
			return (Number)object;
		}

		throw new IllegalArgumentException();
	}

	static
	public List<Number> asNumbers(List<?> objects){

		if(objects == null){
			return null;
		}

		return Lists.transform(objects, object -> asNumber(object));
	}

	static
	public int asInt(Number number){

		if(number instanceof Integer){
			return (Integer)number;
		}

		double value = number.doubleValue();

		if(DoubleMath.isMathematicalInteger(value)){
			return Ints.checkedCast((long)value);
		}

		throw new IllegalArgumentException();
	}

	static
	public Integer asInteger(Number number){
		return asInt(number);
	}

	static
	public List<Integer> asIntegers(List<? extends Number> numbers){

		if(numbers == null){
			return null;
		}

		return Lists.transform(numbers, number -> asInteger(number));
	}

	static
	public Double asDouble(Number number){

		if(number instanceof Double){
			return (Double)number;
		}

		return number.doubleValue();
	}

	static
	public List<Double> asDoubles(List<? extends Number> numbers){

		if(numbers == null){
			return null;
		}

		return Lists.transform(numbers, number -> asDouble(number));
	}

	static
	public Double floatToDouble(Float value){
		return Double.parseDouble(Float.toString(value));
	}

	static
	public List<Double> floatsToDoubles(List<Float> values){

		if(values == null){
			return null;
		}

		return Lists.transform(values, value -> floatToDouble(value));
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

	static
	public BitSet getIndices(List<? extends Number> values, Number targetValue){
		BitSet result = new BitSet(values.size());

		for(int i = 0; i < values.size(); i++){
			Number value = values.get(i);

			if(equals(value, targetValue)){
				result.set(i, true);
			}
		}

		return result;
	}

	static
	public boolean isSparse(List<? extends Number> values, Number defaultValue, double threshold){

		if(threshold < 0d || threshold > 1d){
			throw new IllegalArgumentException();
		}

		int count = 0;

		for(Number value : values){

			if(equals(value, defaultValue)){
				count++;
			}
		}

		return ((double)count / (double)values.size()) >= threshold;
	}

	static
	public <E> List<E> filterByIndices(List<E> list, BitSet filter){
		List<E> result = new ArrayList<>(list.size());

		for(int i = 0; i < list.size(); i++){
			E element = list.get(i);

			if(filter.get(i)){
				result.add(element);
			}
		}

		return result;
	}

	static
	public boolean equals(Number value, Number targetValue){

		if((value).equals(targetValue)){
			return true;
		}

		return (value.doubleValue() == targetValue.doubleValue());
	}

	private static final Double ZERO = Double.valueOf(0d);
	private static final Double ONE = Double.valueOf(1d);
}
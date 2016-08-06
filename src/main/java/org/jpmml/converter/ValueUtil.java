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
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;

public class ValueUtil {

	private ValueUtil(){
	}

	static
	public String formatValue(Object object){

		if(object instanceof Number){
			Number number = (Number)object;

			return ValueUtil.formatValue(number);
		}

		return object.toString();
	}

	static
	public String formatValue(Number number){
		double value = number.doubleValue();

		if(DoubleMath.isMathematicalInteger(value)){
			return Long.toString((long)value);
		}

		return number.toString();
	}

	static
	public String formatArrayValue(List<String> values){
		StringBuilder sb = new StringBuilder();

		String sep = "";

		for(String value : values){
			sb.append(sep);

			sep = " ";

			if(value.indexOf(' ') > -1){
				sb.append('\"').append(value).append('\"');
			} else

			{
				sb.append(value);
			}
		}

		return sb.toString();
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

		Function<Number, Integer> function = new Function<Number, Integer>(){

			@Override
			public Integer apply(Number number){
				return asInteger(number);
			}
		};

		return Lists.transform(numbers, function);
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

		Function<Number, Double> function = new Function<Number, Double>(){

			@Override
			public Double apply(Number number){
				return asDouble(number);
			}
		};

		return Lists.transform(numbers, function);
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
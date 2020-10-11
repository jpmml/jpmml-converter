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
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.google.common.primitives.Ints;
import org.dmg.pmml.MathContext;
import org.dmg.pmml.adapters.ObjectUtil;

public class ValueUtil {

	private ValueUtil(){
	}

	static
	public String asString(Object value){
		value = ObjectUtil.toSimpleValue(value);

		return value.toString();
	}

	static
	public boolean isNaN(Object value){
		return Objects.equals(Float.NaN, value) || Objects.equals(Double.NaN, value);
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
		return isZero(number) ||isNaN(number);
	}

	static
	public Number asNumber(Object object){

		if(object instanceof Number){
			return (Number)object;
		}

		throw new IllegalArgumentException("Expected number, got " + object);
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

		throw new IllegalArgumentException("Expected integer, got " + number);
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
	public Number narrow(double value){

		if(DoubleMath.isMathematicalInteger(value)){
			return narrow((long)value);
		}

		return value;
	}

	static
	public Number narrow(long value){

		if(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE){
			return (byte)value;
		} else

		if(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE){
			return (short)value;
		} else

		if(value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE){
			return (int)value;
		} else

		{
			return value;
		}
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

		if(Objects.equals(value, targetValue)){
			return true;
		}

		return (value.doubleValue() == targetValue.doubleValue());
	}

	static
	public Number add(MathContext mathContext, Number first, Number second){

		if(mathContext == null){
			mathContext = MathContext.DOUBLE;
		}

		switch(mathContext){
			case FLOAT:
				return (first.floatValue() + second.floatValue());
			case DOUBLE:
				return (first.doubleValue() + second.doubleValue());
			default:
				throw new IllegalArgumentException();
		}
	}

	static
	public Number multiply(MathContext mathContext, Number first, Number second){

		if(mathContext == null){
			mathContext = MathContext.DOUBLE;
		}

		switch(mathContext){
			case FLOAT:
				return (first.floatValue() * second.floatValue());
			case DOUBLE:
				return (first.doubleValue() * second.doubleValue());
			default:
				throw new IllegalArgumentException();
		}
	}

	private static final Double ZERO = Double.valueOf(0d);
	private static final Double ONE = Double.valueOf(1d);
}
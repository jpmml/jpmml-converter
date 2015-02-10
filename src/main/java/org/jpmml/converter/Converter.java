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

import com.google.common.math.DoubleMath;
import org.dmg.pmml.PMML;
import rexp.Rexp;

abstract
public class Converter {

	abstract
	public PMML convert(Rexp.REXP rexp);

	static
	public String formatValue(Number number){
		double value = number.doubleValue();

		if(DoubleMath.isMathematicalInteger(value)){
			return Long.toString(number.longValue());
		}

		return Double.toString(value);
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
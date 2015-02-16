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

import java.util.Comparator;

import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;

abstract
public class FieldComparator<F extends Field> implements Comparator<F> {

	private boolean caseSensitive = false;


	@Override
	public int compare(F left, F right){
		FieldName leftName = left.getName();
		FieldName rightName = right.getName();

		boolean caseSensitive = isCaseSensitive();
		if(caseSensitive){
			return (leftName.getValue()).compareTo(rightName.getValue());
		} else

		{
			return (leftName.getValue()).compareToIgnoreCase(rightName.getValue());
		}
	}

	public boolean isCaseSensitive(){
		return this.caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive){
		this.caseSensitive = caseSensitive;
	}
}
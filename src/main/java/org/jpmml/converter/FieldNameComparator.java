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

import org.dmg.pmml.FieldName;
import org.dmg.pmml.Indexable;

public class FieldNameComparator<E extends Indexable<FieldName>> implements Comparator<E> {

	private boolean caseSensitive = false;


	@Override
	public int compare(E left, E right){
		FieldName leftKey = left.getKey();
		FieldName rightKey = right.getKey();

		boolean caseSensitive = isCaseSensitive();
		if(caseSensitive){
			return (leftKey.getValue()).compareTo(rightKey.getValue());
		} else

		{
			return (leftKey.getValue()).compareToIgnoreCase(rightKey.getValue());
		}
	}

	public boolean isCaseSensitive(){
		return this.caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive){
		this.caseSensitive = caseSensitive;
	}
}
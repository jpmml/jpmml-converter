/*
 * Copyright (c) 2016 Villu Ruusmann
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
import java.util.List;

import org.dmg.pmml.FieldName;

public class SchemaUtil {

	private SchemaUtil(){
	}

	static
	public FieldName createTargetField(){
		return FieldName.create("y");
	}

	static
	public List<FieldName> createActiveFields(int size){
		List<FieldName> result = new ArrayList<>(size);

		for(int i = 0; i < size; i++){
			result.add(FieldName.create("x" + String.valueOf(i + 1)));
		}

		return result;
	}
}
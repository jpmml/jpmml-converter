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

import java.util.List;

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.TypeDefinitionField;

public class ListFeature extends ContinuousFeature {

	private List<String> values = null;


	public ListFeature(TypeDefinitionField field, List<String> values){
		this(field.getName(), field.getDataType(), values);
	}

	public ListFeature(FieldName name, DataType dataType, List<String> values){
		super(name, dataType);

		setValues(values);
	}

	public String getValue(int index){
		List<String> values = getValues();

		return values.get(index);
	}

	public List<String> getValues(){
		return this.values;
	}

	private void setValues(List<String> values){
		this.values = values;
	}
}
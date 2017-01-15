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

import com.google.common.base.Objects.ToStringHelper;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.TypeDefinitionField;

public class CategoricalFeature extends Feature {

	private List<String> values = null;


	public CategoricalFeature(PMMLEncoder encoder, DataField dataField){
		this(encoder, dataField, PMMLUtil.getValues(dataField));
	}

	public CategoricalFeature(PMMLEncoder encoder, TypeDefinitionField field, List<String> values){
		this(encoder, field.getName(), field.getDataType(), values);
	}

	public CategoricalFeature(PMMLEncoder encoder, FieldName name, DataType dataType, List<String> values){
		super(encoder, name, dataType);

		setValues(values);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		DataType dataType = getDataType();
		switch(dataType){
			case INTEGER:
			case FLOAT:
			case DOUBLE:
				break;
			default:
				throw new UnsupportedOperationException();
		}

		return new ContinuousFeature(encoder, getName(), getDataType());
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("values", getValues());
	}

	public String getValue(int index){
		List<String> values = getValues();

		return values.get(index);
	}

	public List<String> getValues(){
		return this.values;
	}

	private void setValues(List<String> values){

		if(values == null || values.size() < 1){
			throw new IllegalArgumentException();
		}

		this.values = values;
	}
}
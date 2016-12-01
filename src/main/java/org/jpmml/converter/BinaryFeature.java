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

import com.google.common.base.Objects.ToStringHelper;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.TypeDefinitionField;

public class BinaryFeature extends Feature {

	private String value = null;


	public BinaryFeature(TypeDefinitionField field, String value){
		this(field.getName(), field.getDataType(), value);
	}

	public BinaryFeature(FieldName name, DataType dataType, String value){
		super(name, dataType);

		setValue(value);
	}

	@Override
	protected ToStringHelper toStringHelper(){
		ToStringHelper helper = super.toStringHelper()
			.add("value", getValue());

		return helper;
	}

	public String getValue(){
		return this.value;
	}

	private void setValue(String value){
		this.value = value;
	}
}

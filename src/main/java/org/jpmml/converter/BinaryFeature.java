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
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.NormDiscrete;
import org.dmg.pmml.OpType;
import org.dmg.pmml.TypeDefinitionField;

public class BinaryFeature extends Feature implements HasDerivedName {

	private String value = null;


	public BinaryFeature(PMMLEncoder encoder, TypeDefinitionField field, String value){
		this(encoder, field.getName(), field.getDataType(), value);
	}

	public BinaryFeature(PMMLEncoder encoder, FieldName name, DataType dataType, String value){
		super(encoder, name, dataType);

		setValue(value);
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create((getName()).getValue() + "=" + getValue());
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		FieldName derivedName = getDerivedName();

		DerivedField derivedField = encoder.getDerivedField(derivedName);
		if(derivedField == null){
			NormDiscrete normDiscrete = new NormDiscrete(getName(), getValue());

			derivedField = encoder.createDerivedField(derivedName, OpType.CONTINUOUS, DataType.DOUBLE, normDiscrete);
		}

		return new ContinuousFeature(encoder, derivedField);
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("value", getValue());
	}

	public String getValue(){
		return this.value;
	}

	private void setValue(String value){

		if(value == null){
			throw new IllegalArgumentException();
		}

		this.value = value;
	}
}

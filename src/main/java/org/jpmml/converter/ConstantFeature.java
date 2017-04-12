/*
 * Copyright (c) 2017 Villu Ruusmann
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
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;

public class ConstantFeature extends Feature implements HasDerivedName {

	private Number value = null;


	public ConstantFeature(PMMLEncoder encoder, Number value){
		super(encoder, getName(value), getDataType(value));

		setValue(value);
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create("constant(" + (getName()).getValue() + ")");
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		FieldName derivedName = getDerivedName();

		DerivedField derivedField = encoder.getDerivedField(derivedName);
		if(derivedField == null){
			Constant constant = PMMLUtil.createConstant(getValue());

			derivedField = encoder.createDerivedField(derivedName, OpType.CONTINUOUS, getDataType(), constant);
		}

		return new ContinuousFeature(encoder, derivedField);
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("value", getValue());
	}

	public Number getValue(){
		return this.value;
	}

	private void setValue(Number value){

		if(value == null){
			throw new IllegalArgumentException();
		}

		this.value = value;
	}

	static
	private FieldName getName(Number number){

		if((number instanceof Integer) || (number instanceof Long)){
			return FieldName.create(number.toString());
		} else

		if(number instanceof Float){
			return FieldName.create(number.toString() + "f");
		} else

		if(number instanceof Double){
			return FieldName.create(number.toString());
		}

		throw new IllegalArgumentException();
	}

	static
	private DataType getDataType(Number number){

		if((number instanceof Integer) || (number instanceof Long)){
			return DataType.INTEGER;
		} else

		if(number instanceof Float){
			return DataType.FLOAT;
		} else

		if(number instanceof Double){
			return DataType.DOUBLE;
		}

		throw new IllegalArgumentException();
	}
}
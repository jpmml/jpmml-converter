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

import java.util.Objects;

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.jpmml.model.ToStringHelper;

public class ConstantFeature extends Feature implements HasDerivedName {

	private Number value = null;


	public ConstantFeature(PMMLEncoder encoder, Number value){
		this(encoder, FieldName.create(ValueUtil.formatValue(value) + (value instanceof Float ? "f" : "")), ValueUtil.getDataType(value), value);
	}

	public ConstantFeature(PMMLEncoder encoder, FieldName name, DataType dataType, Number value){
		super(encoder, name, dataType);

		setValue(value);
	}

	@Override
	public FieldName getDerivedName(){
		return FieldName.create("constant(" + (getName()).getValue() + ")");
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		return toContinuousFeature(getDerivedName(), getDataType(), () -> PMMLUtil.createConstant(getValue(), getDataType()));
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(this.getValue());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof ConstantFeature){
			ConstantFeature that = (ConstantFeature)object;

			return super.equals(object) && Objects.equals(this.getValue(), that.getValue());
		}

		return false;
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
}
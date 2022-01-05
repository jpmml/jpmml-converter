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

import java.util.Objects;

import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;
import org.dmg.pmml.NormDiscrete;
import org.jpmml.model.ToStringHelper;

public class BinaryFeature extends Feature implements HasDerivedName {

	private Object value = null;


	public BinaryFeature(PMMLEncoder encoder, Field<?> field, Object value){
		this(encoder, field.requireName(), field.getDataType(), value);
	}

	public BinaryFeature(PMMLEncoder encoder, Feature feature, Object value){
		this(encoder, feature.getName(), feature.getDataType(), value);
	}

	public BinaryFeature(PMMLEncoder encoder, String name, DataType dataType, Object value){
		super(encoder, name, dataType);

		setValue(value);
	}

	@Override
	public String getDerivedName(){
		return getName() + "=" + getValue();
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		return toContinuousFeature(getDerivedName(), DataType.DOUBLE, () -> new NormDiscrete(getName(), getValue()));
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(this.getValue());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof BinaryFeature){
			BinaryFeature that = (BinaryFeature)object;

			return super.equals(object) && Objects.equals(this.getValue(), that.getValue());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("value", getValue());
	}

	public Object getValue(){
		return this.value;
	}

	private void setValue(Object value){
		this.value = Objects.requireNonNull(value);
	}
}

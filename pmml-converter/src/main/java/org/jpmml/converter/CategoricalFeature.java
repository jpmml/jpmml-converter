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
import java.util.Objects;

import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;
import org.dmg.pmml.HasDiscreteDomain;
import org.jpmml.model.ToStringHelper;

public class CategoricalFeature extends Feature {

	private List<?> values = null;


	public <F extends Field<F> & HasDiscreteDomain<F>> CategoricalFeature(PMMLEncoder encoder, F field){
		this(encoder, field, PMMLUtil.<F>getValues(field));
	}

	public CategoricalFeature(PMMLEncoder encoder, Field<?> field, List<?> values){
		this(encoder, field.requireName(), field.requireDataType(), values);
	}

	public CategoricalFeature(PMMLEncoder encoder, Feature feature, List<?> values){
		this(encoder, feature.getName(), feature.getDataType(), values);
	}

	public CategoricalFeature(PMMLEncoder encoder, String name, DataType dataType, List<?> values){
		super(encoder, name, dataType);

		setValues(values);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = getEncoder();

		Field<?> field = encoder.toContinuous(getName());

		return new ContinuousFeature(encoder, field);
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(this.getValues());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof CategoricalFeature){
			CategoricalFeature that = (CategoricalFeature)object;

			return super.equals(object) && Objects.equals(this.getValues(), that.getValues());
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("values", getValues());
	}

	public int size(){
		List<?> values = getValues();

		return values.size();
	}

	public Object getValue(int index){
		List<?> values = getValues();

		return values.get(index);
	}

	public List<?> getValues(){
		return this.values;
	}

	private void setValues(List<?> values){
		values = Objects.requireNonNull(values);

		if(values.isEmpty()){
			throw new IllegalArgumentException();
		}

		this.values = values;
	}
}
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
import org.dmg.pmml.Field;
import org.dmg.pmml.HasDiscreteDomain;
import org.dmg.pmml.OpType;

public class CategoricalLabel extends DiscreteLabel {

	public <F extends Field<F> & HasDiscreteDomain<F>> CategoricalLabel(F field){
		this(field.requireName(), field.requireDataType(), FieldUtil.getValues(field));
	}

	public CategoricalLabel(CategoricalFeature categoricalFeature){
		this(categoricalFeature.getName(), categoricalFeature.getDataType(), categoricalFeature.getValues());
	}

	public CategoricalLabel(DataType dataType, List<?> values){
		super(dataType, values);
	}

	public CategoricalLabel(String name, DataType dataType, List<?> values){
		super(name, dataType, values);
	}

	@Override
	public OpType getOpType(){
		return OpType.CATEGORICAL;
	}

	@Override
	public CategoricalLabel toRenamedLabel(String name){
		return new CategoricalLabel(name, getDataType(), getValues());
	}

	@Override
	public CategoricalLabel toAnonymousLabel(){
		return (CategoricalLabel)super.toAnonymousLabel();
	}

	@Override
	public CategoricalLabel toCategoricalLabel(){
		return this;
	}

	@Override
	public OrdinalLabel toOrdinalLabel(){
		return new OrdinalLabel(getName(), getDataType(), getValues());
	}

	@Override
	public int hashCode(){
		return super.hashCode();
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof CategoricalLabel){
			CategoricalLabel that = (CategoricalLabel)object;

			return super.equals(object);
		}

		return false;
	}
}
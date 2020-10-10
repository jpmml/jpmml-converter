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
import java.util.function.Supplier;

import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.OpType;
import org.jpmml.model.ToStringHelper;

abstract
public class Feature {

	private PMMLEncoder encoder = null;

	private FieldName name = null;

	private DataType dataType = null;


	public Feature(PMMLEncoder encoder, FieldName name, DataType dataType){
		setEncoder(encoder);
		setName(name);
		setDataType(dataType);
	}

	abstract
	public ContinuousFeature toContinuousFeature();

	public ContinuousFeature toContinuousFeature(DataType dataType){
		ContinuousFeature continuousFeature = toContinuousFeature();

		if((dataType).equals(continuousFeature.getDataType())){
			return continuousFeature;
		}

		FieldName name = FieldNameUtil.create((dataType.name()).toLowerCase(), continuousFeature);

		return toContinuousFeature(name, dataType, continuousFeature::ref);
	}

	protected ContinuousFeature toContinuousFeature(FieldName name, DataType dataType, Supplier<? extends Expression> expressionSupplier){
		PMMLEncoder encoder = ensureEncoder();

		DerivedField derivedField = encoder.ensureDerivedField(name, OpType.CONTINUOUS, dataType, expressionSupplier);

		return new ContinuousFeature(encoder, derivedField);
	}

	public FieldRef ref(){
		return new FieldRef(getName());
	}

	public Field<?> getField(){
		PMMLEncoder encoder = ensureEncoder();

		return encoder.getField(getName());
	}

	@Override
	public int hashCode(){
		int result = 0;

		result = (31 * result) + Objects.hashCode(this.getEncoder());
		result = (31 * result) + Objects.hashCode(this.getName());
		result = (31 * result) + Objects.hashCode(this.getDataType());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof Feature){
			Feature that = (Feature)object;

			return (this.getClass()).equals(that.getClass()) && Objects.equals(this.getEncoder(), that.getEncoder()) && Objects.equals(this.getName(), that.getName()) && Objects.equals(this.getDataType(), that.getDataType());
		}

		return false;
	}

	@Override
	public String toString(){
		ToStringHelper helper = toStringHelper();

		return helper.toString();
	}

	protected ToStringHelper toStringHelper(){
		return new ToStringHelper(this)
			.add("name", getName())
			.add("dataType", getDataType());
	}

	protected PMMLEncoder ensureEncoder(){
		PMMLEncoder encoder = getEncoder();

		if(encoder == null){
			throw new IllegalStateException();
		}

		return encoder;
	}

	public PMMLEncoder getEncoder(){
		return this.encoder;
	}

	private void setEncoder(PMMLEncoder encoder){
		this.encoder = encoder;
	}

	public FieldName getName(){
		return this.name;
	}

	private void setName(FieldName name){
		this.name = Objects.requireNonNull(name);
	}

	public DataType getDataType(){
		return this.dataType;
	}

	private void setDataType(DataType dataType){
		this.dataType = Objects.requireNonNull(dataType);
	}
}
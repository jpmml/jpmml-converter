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

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;

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

	public FieldRef ref(){
		FieldRef fieldRef = new FieldRef(getName());

		return fieldRef;
	}

	@Override
	public String toString(){
		ToStringHelper helper = toStringHelper();

		return helper.toString();
	}

	protected ToStringHelper toStringHelper(){
		ToStringHelper helper = Objects.toStringHelper(this)
			.add("name", getName())
			.add("dataType", getDataType());

		return helper;
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

		if(name == null){
			throw new IllegalArgumentException();
		}

		this.name = name;
	}

	public DataType getDataType(){
		return this.dataType;
	}

	private void setDataType(DataType dataType){

		if(dataType == null){
			throw new IllegalArgumentException();
		}

		this.dataType = dataType;
	}
}
/*
 * Copyright (c) 2022 Villu Ruusmann
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
import org.jpmml.model.ToStringHelper;

abstract
public class ScalarLabel extends Label {

	private String name = null;

	private DataType dataType = null;


	public ScalarLabel(String name, DataType dataType){
		setName(name);
		setDataType(dataType);
	}

	abstract
	public ScalarLabel toRenamedLabel(String name);

	public ScalarLabel toAnonymousLabel(){
		return toRenamedLabel(null);
	}

	public boolean isAnonymous(){
		String name = getName();

		return (name == null);
	}

	@Override
	public int hashCode(){
		int result = 0;

		result = (31 * result) + Objects.hashCode(this.getName());
		result = (31 * result) + Objects.hashCode(this.getDataType());

		return result;
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof ScalarLabel){
			ScalarLabel that = (ScalarLabel)object;

			if(Objects.equals(this.getClass(), that.getClass())){
				return Objects.equals(this.getName(), that.getName()) && Objects.equals(this.getDataType(), that.getDataType());
			}
		}

		return false;
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("name", getName())
			.add("dataType", getDataType());
	}

	public String getName(){
		return this.name;
	}

	private void setName(String name){
		this.name = name;
	}

	public DataType getDataType(){
		return this.dataType;
	}

	private void setDataType(DataType dataType){
		this.dataType = Objects.requireNonNull(dataType);
	}
}
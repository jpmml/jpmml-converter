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

import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;

public class ContinuousLabel extends Label {

	public ContinuousLabel(Field<?> field){
		this(field.requireName(), field.requireDataType());
	}

	public ContinuousLabel(DataType dataType){
		this(null, dataType);
	}

	public ContinuousLabel(String name, DataType dataType){
		super(name, dataType);
	}

	@Override
	public ContinuousLabel toRenamedLabel(String name){
		return new ContinuousLabel(name, getDataType());
	}

	@Override
	public ContinuousLabel toAnonymousLabel(){
		return (ContinuousLabel)super.toAnonymousLabel();
	}
}
/*
 * Copyright (c) 2018 Villu Ruusmann
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

public class ObjectFeature extends Feature {

	public ObjectFeature(PMMLEncoder encoder, Field<?> field){
		this(encoder, field.requireName(), field.getDataType());
	}

	public ObjectFeature(PMMLEncoder encoder, String name, DataType dataType){
		super(encoder, name, dataType);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		throw new UnsupportedOperationException("Feature " + getName() + " (" + this + ") cannot be cast to a continuous feature");
	}
}
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

import java.util.ArrayList;
import java.util.List;

import org.dmg.pmml.DataType;

abstract
public class ModelTest {

	static
	public ContinuousFeature createContinuousFeature(ModelEncoder encoder, String name){
		return new ContinuousFeature(encoder, name, DataType.DOUBLE);
	}

	static
	public ConstantFeature createConstantFeature(ModelEncoder encoder, Number number){
		return new ConstantFeature(encoder, number);
	}

	static
	public InteractionFeature createInteractionFeature(ModelEncoder encoder, Object... objects){
		List<Feature> features = new ArrayList<>();

		StringBuilder sb = new StringBuilder();

		String sep = "";

		for(Object object : objects){
			sb.append(sep);

			sep = ":";

			sb.append(object);

			if(object instanceof String){
				String name = (String)object;

				features.add(createContinuousFeature(encoder, name));
			} else

			if(object instanceof Number){
				Number number = (Number)object;

				features.add(createConstantFeature(encoder, number));
			} else

			{
				throw new IllegalArgumentException();
			}
		}

		return new InteractionFeature(encoder, sb.toString(), DataType.DOUBLE, features);
	}
}
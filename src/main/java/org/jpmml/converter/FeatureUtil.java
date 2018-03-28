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
import java.util.StringJoiner;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dmg.pmml.FieldName;

public class FeatureUtil {

	private FeatureUtil(){
	}

	static
	public FieldName createName(String function, Feature feature){
		return FieldName.create(function + "(" + getName(feature).getValue() + ")");
	}

	static
	public FieldName createName(String function, Feature feature, int index){
		return FieldName.create(function + "(" + getName(feature).getValue() + ")" + "[" + index + "]");
	}

	static
	public FieldName createName(String function, List<? extends Feature> features){
		Function<Feature, String> nameFunction = new Function<Feature, String>(){

			@Override
			public String apply(Feature feature){
				FieldName name = getName(feature);

				return name.getValue();
			}
		};

		List<String> values;

		if(features.size() <= 5){
			values = Lists.transform(features, nameFunction);
		} else

		{
			values = new ArrayList<>();

			values.addAll(Lists.transform(features.subList(0, 2), nameFunction));
			values.add("..");
			values.addAll(Lists.transform(features.subList(features.size() - 2, features.size()), nameFunction));
		}

		StringJoiner joiner = new StringJoiner(", ", function + "(", ")");

		for(String value : values){
			joiner.add(value);
		}

		return FieldName.create(joiner.toString());
	}

	static
	public FieldName getName(Feature feature){

		if(feature instanceof HasDerivedName){
			HasDerivedName hasDerivedName = (HasDerivedName)feature;

			return hasDerivedName.getDerivedName();
		}

		return feature.getName();
	}
}
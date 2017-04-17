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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
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
	public FieldName getName(Feature feature){

		if(feature instanceof HasDerivedName){
			HasDerivedName hasDerivedName = (HasDerivedName)feature;

			return hasDerivedName.getDerivedName();
		}

		return feature.getName();
	}

	static
	public String formatFeatureList(List<Feature> features){
		Function<Feature, FieldName> function = new Function<Feature, FieldName>(){

			@Override
			public FieldName apply(Feature feature){
				return FeatureUtil.getName(feature);
			}
		};

		return formatNameList(Lists.transform(features, function));
	}

	static
	public String formatNameList(List<FieldName> names){
		Function<FieldName, String> function = new Function<FieldName, String>(){

			@Override
			public String apply(FieldName name){
				return name.getValue();
			}
		};

		List<String> values;

		if(names.size() <= 5){
			values = Lists.transform(names, function);
		} else

		{
			values = new ArrayList<>();

			values.addAll(Lists.transform(names.subList(0, 2), function));
			values.add("..");
			values.addAll(Lists.transform(names.subList(names.size() - 2, names.size()), function));
		}

		return JOINER.join(values);
	}

	private static final Joiner JOINER = Joiner.on(", ");
}
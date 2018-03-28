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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		Stream<FieldName> nameStream;

		if(features.size() <= 5){
			nameStream = features.stream()
				.map(feature -> getName(feature));
		} else

		{
			nameStream = Stream.of(
				features.subList(0, 2).stream()
					.map(feature -> getName(feature)),
				Stream.of(FieldName.create("..")),
				features.subList(features.size() - 2, features.size()).stream()
					.map(feature -> getName(feature))
			).flatMap(x -> x);
		}

		String value = nameStream
			.map(name -> name.getValue())
			.collect(Collectors.joining(", ", function + "(", ")"));

		return FieldName.create(value);
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
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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import org.dmg.pmml.DataType;

public class ArrayFeature extends ObjectFeature {

	private List<Feature> features = null;


	public ArrayFeature(PMMLEncoder encoder, List<Feature> features){
		super(encoder, getName(features), getDataType(features));

		setFeatures(features);
	}

	public List<Feature> getFeatures(){
		return this.features;
	}

	private void setFeatures(List<Feature> features){
		this.features = Objects.requireNonNull(features);
	}

	static
	private String getName(List<Feature> features){
		return features.stream()
			.map(feature -> FeatureUtil.getName(feature))
			.collect(Collectors.joining(", ", "[", "]"));
	}

	static
	public DataType getDataType(List<Feature> features){
		Set<DataType> dataTypes = features.stream()
			.map(feature -> feature.getDataType())
			.collect(Collectors.toSet());

		// XXX
		return Iterables.getOnlyElement(dataTypes);
	}
}
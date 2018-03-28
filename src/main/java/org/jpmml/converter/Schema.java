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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Schema {

	private Label label = null;

	private List<? extends Feature> features = null;


	public Schema(Label label, List<? extends Feature> features){
		setLabel(label);
		setFeatures(features);
	}

	public Schema toAnonymousSchema(){
		Label label = getLabel();
		List<? extends Feature> features = getFeatures();

		return new Schema(label != null ? label.toAnonymousLabel() : null, features);
	}

	public Schema toSubSchema(int[] indexes){
		Label label = getLabel();
		List<? extends Feature> features = getFeatures();

		List<Feature> selectedFeatures = new ArrayList<>(indexes.length);

		for(int index : indexes){
			Feature feature = features.get(index);

			selectedFeatures.add(feature);
		}

		return new Schema(label, selectedFeatures);
	}

	public Schema toTransformedSchema(Function<Feature, Feature> function){
		Label label = getLabel();
		List<? extends Feature> features = getFeatures();

		List<? extends Feature> transformedFeatures = features.stream()
			.map(function)
			.collect(Collectors.toList());

		return new Schema(label, transformedFeatures);
	}

	public Label getLabel(){
		return this.label;
	}

	private void setLabel(Label label){
		this.label = label;
	}

	public Feature getFeature(int index){
		List<? extends Feature> features = getFeatures();

		return features.get(index);
	}

	public List<? extends Feature> getFeatures(){
		return this.features;
	}

	private void setFeatures(List<? extends Feature> features){

		if(features == null){
			throw new IllegalArgumentException();
		}

		this.features = features;
	}
}
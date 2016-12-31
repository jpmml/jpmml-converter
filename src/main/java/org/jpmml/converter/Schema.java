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

public class Schema {

	private Label label = null;

	private List<Feature> features = null;


	public Schema(Label label, List<Feature> features){
		setLabel(label);
		setFeatures(features);
	}

	public Schema toAnonymousSchema(){
		Label label = getLabel();
		List<Feature> features = getFeatures();

		Schema schema = new Schema(label != null ? label.toAnonymousLabel() : null, features);

		return schema;
	}

	public Schema toSubSchema(int[] indexes){
		List<Feature> features = new ArrayList<>(indexes.length);

		for(int index : indexes){
			Feature feature = getFeature(index);

			features.add(feature);
		}

		Schema schema = new Schema(getLabel(), features);

		return schema;
	}

	public Label getLabel(){
		return this.label;
	}

	private void setLabel(Label label){
		this.label = label;
	}

	public Feature getFeature(int index){
		List<Feature> features = getFeatures();

		return features.get(index);
	}

	public List<Feature> getFeatures(){
		return this.features;
	}

	private void setFeatures(List<Feature> features){
		this.features = features;
	}
}
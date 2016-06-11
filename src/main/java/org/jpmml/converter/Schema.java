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

import org.dmg.pmml.FieldName;

public class Schema {

	private FieldName targetField = null;

	private List<String> targetCategories = null;

	private List<FieldName> activeFields = null;

	private List<Feature> features = null;


	public Schema(FieldName targetField, List<String> targetCategories, List<FieldName> activeFields, List<Feature> features){
		setTargetField(targetField);
		setTargetCategories(targetCategories);
		setActiveFields(activeFields);
		setFeatures(features);
	}

	public Schema toAnonymousSchema(){
		Schema schema = new Schema(null, getTargetCategories(), getActiveFields(), getFeatures());

		return schema;
	}

	public Schema toSubSchema(int[] indexes){
		List<Feature> features = new ArrayList<>(indexes.length);

		for(int index : indexes){
			Feature feature = getFeature(index);

			features.add(feature);
		}

		Schema schema = new Schema(getTargetField(), getTargetCategories(), getActiveFields(), features);

		return schema;
	}

	public FieldName getTargetField(){
		return this.targetField;
	}

	private void setTargetField(FieldName targetField){
		this.targetField = targetField;
	}

	public List<String> getTargetCategories(){
		return this.targetCategories;
	}

	private void setTargetCategories(List<String> targetCategories){
		this.targetCategories = targetCategories;
	}

	public List<FieldName> getActiveFields(){
		return this.activeFields;
	}

	private void setActiveFields(List<FieldName> activeFields){
		this.activeFields = activeFields;
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
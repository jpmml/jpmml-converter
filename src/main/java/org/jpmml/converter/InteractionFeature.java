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

import com.google.common.base.Objects.ToStringHelper;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.TypeDefinitionField;

public class InteractionFeature extends ContinuousFeature {

	private List<Feature> features = null;


	public InteractionFeature(TypeDefinitionField field, List<Feature> features){
		this(field.getName(), field.getDataType(), features);
	}

	public InteractionFeature(FieldName name, DataType dataType, List<Feature> features){
		super(name, dataType);

		setFeatures(features);
	}

	@Override
	protected ToStringHelper toStringHelper(){
		ToStringHelper helper = super.toStringHelper()
			.add("features", getFeatures());

		return helper;
	}

	public List<Feature> getInputFeatures(){
		List<Feature> result = new ArrayList<>();

		List<Feature> features = getFeatures();
		for(Feature feature : features){

			if(feature instanceof InteractionFeature){
				InteractionFeature interactionFeature = (InteractionFeature)feature;

				result.addAll(interactionFeature.getInputFeatures());
			} else

			{
				result.add(feature);
			}
		}

		return result;
	}

	public List<Feature> getFeatures(){
		return this.features;
	}

	private void setFeatures(List<Feature> features){
		this.features = features;
	}
}
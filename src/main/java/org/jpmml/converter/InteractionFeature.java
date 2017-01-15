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
import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dmg.pmml.TypeDefinitionField;

public class InteractionFeature extends Feature {

	private List<? extends Feature> features = null;


	public InteractionFeature(PMMLEncoder encoder, TypeDefinitionField field, List<? extends Feature> features){
		this(encoder, field.getName(), field.getDataType(), features);
	}

	public InteractionFeature(PMMLEncoder encoder, FieldName name, DataType dataType, List<? extends Feature> features){
		super(encoder, name, dataType);

		setFeatures(features);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		PMMLEncoder encoder = ensureEncoder();

		List<? extends Feature> features = getFeatures();

		DerivedField derivedField = encoder.getDerivedField(getName());
		if(derivedField == null){
			Apply apply = PMMLUtil.createApply("*", ((features.get(0)).toContinuousFeature()).ref(), ((features.get(1)).toContinuousFeature()).ref());

			for(int i = 2; i < features.size(); i++){
				apply = PMMLUtil.createApply("*", apply, ((features.get(i)).toContinuousFeature()).ref());
			}

			derivedField = encoder.createDerivedField(getName(), OpType.CONTINUOUS, DataType.DOUBLE, apply);
		}

		return new ContinuousFeature(encoder, derivedField);
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("features", getFeatures());
	}

	public List<? extends Feature> getInputFeatures(){
		List<Feature> result = new ArrayList<>();

		List<? extends Feature> features = getFeatures();
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

	public List<? extends Feature> getFeatures(){
		return this.features;
	}

	private void setFeatures(List<? extends Feature> features){

		if(features == null || features.size() < 2){
			throw new IllegalArgumentException();
		}

		this.features = features;
	}
}
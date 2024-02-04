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
import java.util.Objects;
import java.util.function.Supplier;

import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.model.ToStringHelper;

public class InteractionFeature extends Feature {

	private List<? extends Feature> features = null;


	public InteractionFeature(PMMLEncoder encoder, Field<?> field, List<? extends Feature> features){
		this(encoder, field.requireName(), field.requireDataType(), features);
	}

	public InteractionFeature(PMMLEncoder encoder, String name, DataType dataType, List<? extends Feature> features){
		super(encoder, name, dataType);

		setFeatures(features);
	}

	@Override
	public ContinuousFeature toContinuousFeature(){
		Supplier<Apply> applySupplier = () -> {
			List<? extends Feature> features = getFeatures();

			Apply apply = ExpressionUtil.createApply(PMMLFunctions.MULTIPLY, ((features.get(0)).toContinuousFeature()).ref(), ((features.get(1)).toContinuousFeature()).ref());

			for(int i = 2; i < features.size(); i++){
				apply = ExpressionUtil.createApply(PMMLFunctions.MULTIPLY, apply, ((features.get(i)).toContinuousFeature()).ref());
			}

			return apply;
		};

		return toContinuousFeature(getName(), DataType.DOUBLE, applySupplier);
	}

	@Override
	public int hashCode(){
		return (31 * super.hashCode()) + Objects.hashCode(this.getFeatures());
	}

	@Override
	public boolean equals(Object object){

		if(object instanceof InteractionFeature){
			InteractionFeature that = (InteractionFeature)object;

			return super.equals(object) && Objects.equals(this.getFeatures(), that.getFeatures());
		}

		return false;
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
		features = Objects.requireNonNull(features);

		if(features.size() < 2){
			throw new IllegalArgumentException();
		}

		this.features = features;
	}
}
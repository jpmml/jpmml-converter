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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.Iterables;
import org.dmg.pmml.Apply;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Field;
import org.dmg.pmml.MathContext;
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

	public Feature simplify(){
		PMMLEncoder encoder = getEncoder();
		String name = getName();
		DataType dataType = getDataType();
		List<? extends Feature> features = getFeatures();
		List<? extends Feature> flattenedFeatures = getFlattenedFeatures();

		List<Feature> result = new ArrayList<>();

		Map<String, Object> binaryValues = new HashMap<>();

		for(int i = 0; i < flattenedFeatures.size(); i++){
			Feature flattenedFeature = flattenedFeatures.get(i);

			if(flattenedFeature instanceof BinaryFeature){
				BinaryFeature binaryFeature = (BinaryFeature)flattenedFeature;

				if(result.contains(binaryFeature)){
					continue;
				}

				Object existingValue = binaryValues.put(binaryFeature.getName(), binaryFeature.getValue());
				if(existingValue != null && !Objects.equals(existingValue, binaryFeature.getValue())){
					return new ConstantFeature(encoder, 0d);
				}
			} else

			if(flattenedFeature instanceof BooleanFeature){
				BooleanFeature booleanFeature = (BooleanFeature)flattenedFeature;

				if(result.contains(booleanFeature)){
					continue;
				}
			} else

			if(flattenedFeature instanceof ConstantFeature){
				ConstantFeature constantFeature = (ConstantFeature)flattenedFeature;

				Number value = constantFeature.getValue();

				if(ValueUtil.isZero(value)){
					return constantFeature;
				} else

				if(ValueUtil.isOne(value)){
					continue;
				}
			}

			result.add(flattenedFeature);
		}

		if(result.size() == 0){
			return new ConstantFeature(encoder, 1d);
		} else

		if(result.size() == 1){
			return Iterables.getOnlyElement(result);
		} else

		{
			if(Objects.equals(result, features)){
				return this;
			}

			return new InteractionFeature(encoder, name, dataType, result);
		}
	}

	public Canonicalization canonicalize(MathContext mathContext){
		PMMLEncoder encoder = getEncoder();
		String name = getName();
		DataType dataType = getDataType();
		Number factor = null;
		List<Feature> flattenedFeatures = new ArrayList<>(getFlattenedFeatures());

		for(Iterator<? extends Feature> it = flattenedFeatures.iterator(); it.hasNext(); ){
			Feature flattenedFeature = it.next();

			if(flattenedFeature instanceof ConstantFeature){
				ConstantFeature constantFeature = (ConstantFeature)flattenedFeature;

				if(factor == null){
					factor = constantFeature.getValue();
				} else

				{
					factor = ValueUtil.multiply(mathContext, factor, constantFeature.getValue());
				}

				it.remove();
			}
		}

		Feature feature;

		if(flattenedFeatures.size() == 0){
			feature = null;
		} else

		if(flattenedFeatures.size() == 1){
			feature = Iterables.getOnlyElement(flattenedFeatures);
		} else

		{
			feature = new CanonicalInteractionFeature(encoder, name, dataType, flattenedFeatures);
		}

		return new Canonicalization(factor, feature);
	}

	public List<? extends Feature> getFlattenedFeatures(){
		List<Feature> result = new ArrayList<>();

		List<? extends Feature> features = getFeatures();
		for(Feature feature : features){

			if(feature instanceof InteractionFeature){
				InteractionFeature interactionFeature = (InteractionFeature)feature;

				result.addAll(interactionFeature.getFlattenedFeatures());
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

	static
	public class Canonicalization {

		private Number factor = null;

		private Feature feature = null;


		Canonicalization(Number factor, Feature feature){
			setFactor(factor);
			setFeature(feature);
		}

		public Number getFactor(){
			return this.factor;
		}

		private void setFactor(Number factor){
			this.factor = factor;
		}

		public Feature getFeature(){
			return this.feature;
		}

		private void setFeature(Feature feature){
			this.feature = feature;
		}
	}
}
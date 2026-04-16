/*
 * Copyright (c) 2026 Villu Ruusmann
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
import java.util.function.Function;

import org.dmg.pmml.Apply;
import org.dmg.pmml.Expression;
import org.dmg.pmml.Field;
import org.dmg.pmml.HasDiscreteDomain;
import org.dmg.pmml.PMMLFunctions;

public class NormalizerUtil {

	private NormalizerUtil(){
	}

	static
	public Apply encodeL1Norm(List<? extends Feature> features){
		return encodeAggregation(
			PMMLFunctions.SUM,
			(feature) -> {
				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				if(isInvariant(feature)){
					return continuousFeature.ref();
				}

				return ExpressionUtil.createApply(PMMLFunctions.ABS, continuousFeature.ref());
			},
			features
		);
	}

	static
	public Apply encodeL2Norm(List<? extends Feature> features){
		return ExpressionUtil.createApply(
			PMMLFunctions.SQRT,
			encodeAggregation(
				PMMLFunctions.SUM,
				(feature) -> {
					ContinuousFeature continuousFeature = feature.toContinuousFeature();

					if(isInvariant(feature)){
						return continuousFeature.ref();
					}

					return ExpressionUtil.createApply(PMMLFunctions.POW, continuousFeature.ref(), ExpressionUtil.createConstant(2d));
				},
				features
			)
		);
	}

	static
	public Apply encodeMaxNorm(List<? extends Feature> features){
		return encodeAggregation(
			PMMLFunctions.MAX,
			(feature) -> {
				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				if(isInvariant(feature)){
					return continuousFeature.ref();
				}

				return ExpressionUtil.createApply(PMMLFunctions.ABS, continuousFeature.ref());
			},
			features
		);
	}

	static
	private boolean isInvariant(Feature feature){

		if(feature instanceof BinaryFeature){
			BinaryFeature binaryFeature = (BinaryFeature)feature;

			return true;
		} else

		if(feature instanceof ConstantFeature){
			ConstantFeature constantFeature = (ConstantFeature)feature;

			Number value = constantFeature.getValue();

			return ValueUtil.isZero(value) || ValueUtil.isOne(value);
		} else

		{
			return false;
		}
	}

	static
	public List<Feature> aggregateFeatures(List<? extends Feature> features, PMMLEncoder encoder){
		List<Feature> result = new ArrayList<>();

		for(int i = 0, max = features.size(); i < max; ){
			Feature feature = features.get(i);

			if(feature instanceof BinaryFeature){
				BinaryFeature binaryFeature = (BinaryFeature)feature;

				String name = binaryFeature.getName();

				Field<?> field = encoder.getField(name);

				List<Object> values = new ArrayList<>();
				values.add(binaryFeature.getValue());

				int j = i + 1;

				while(j < max){
					Feature nextFeature = features.get(j);

					if(nextFeature instanceof BinaryFeature){
						BinaryFeature nextBinaryFeature = (BinaryFeature)nextFeature;

						String nextName = nextBinaryFeature.getName();

						if(Objects.equals(name, nextName)){
							values.add(nextBinaryFeature.getValue());

							j++;

							continue;
						}
					}

					break;
				}

				boolean fullDomain = false;

				if(field instanceof HasDiscreteDomain){
					List<?> validValues = FieldUtil.getValues((Field & HasDiscreteDomain)field);

					fullDomain = Objects.equals(values, validValues);
				} // End if

				if(fullDomain){
					ConstantFeature constantFeature = new ConstantFeature(encoder, 1d);

					result.add(constantFeature);

					i = j;
				} else

				{
					result.add(binaryFeature);

					i++;
				}
			} else

			{
				result.add(feature);

				i++;
			}
		}

		return result;
	}

	static
	private Apply encodeAggregation(String function, Function<Feature, Expression> featureEncoder, List<? extends Feature> features){
		Apply apply = ExpressionUtil.createApply(function);

		for(Feature feature : features){
			apply.addExpressions(featureEncoder.apply(feature));
		}

		return apply;
	}
}
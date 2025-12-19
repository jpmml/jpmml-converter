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
package org.jpmml.converter.regression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Iterables;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.MathContext;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.regression.CategoricalPredictor;
import org.dmg.pmml.regression.NumericPredictor;
import org.dmg.pmml.regression.PredictorTerm;
import org.dmg.pmml.regression.RegressionModel;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.BooleanFeature;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.ConstantFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.ContinuousLabel;
import org.jpmml.converter.Feature;
import org.jpmml.converter.InteractionFeature;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.OrdinalLabel;
import org.jpmml.converter.PowerFeature;
import org.jpmml.converter.ProductFeature;
import org.jpmml.converter.Schema;
import org.jpmml.converter.SchemaUtil;
import org.jpmml.converter.ValueUtil;

public class RegressionModelUtil {

	private RegressionModelUtil(){
	}

	static
	public RegressionModel createRegression(List<? extends Feature> features, List<? extends Number> coefficients, Number intercept, RegressionModel.NormalizationMethod normalizationMethod, Schema schema){
		return createRegression(null, features, coefficients, intercept, normalizationMethod, schema);
	}

	static
	public RegressionModel createRegression(MathContext mathContext, List<? extends Feature> features, List<? extends Number> coefficients, Number intercept, RegressionModel.NormalizationMethod normalizationMethod, Schema schema){
		ContinuousLabel continuousLabel = schema.requireContinuousLabel();

		if(normalizationMethod != null){

			switch(normalizationMethod){
				case NONE:
				case SOFTMAX:
				case LOGIT:
				case PROBIT:
				case CLOGLOG:
				case EXP:
				case LOGLOG:
				case CAUCHIT:
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		RegressionModel regressionModel = new RegressionModel(MiningFunction.REGRESSION, ModelUtil.createMiningSchema(continuousLabel), null)
			.setNormalizationMethod(normalizationMethod)
			.setMathContext(ModelUtil.simplifyMathContext(mathContext))
			.addRegressionTables(createRegressionTable(mathContext, features, coefficients, intercept));

		return regressionModel;
	}

	static
	public RegressionModel createBinaryLogisticClassification(List<? extends Feature> features, List<? extends Number> coefficients, Number intercept, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		return createBinaryLogisticClassification(null, features, coefficients, intercept, normalizationMethod, hasProbabilityDistribution, schema);
	}

	static
	public RegressionModel createBinaryLogisticClassification(MathContext mathContext, List<? extends Feature> features, List<? extends Number> coefficients, Number intercept, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		CategoricalLabel categoricalLabel = schema.requireCategoricalLabel();

		SchemaUtil.checkSize(2, categoricalLabel);

		if(normalizationMethod != null){

			switch(normalizationMethod){
				case NONE:
				case LOGIT:
				case PROBIT:
				case CLOGLOG:
				case LOGLOG:
				case CAUCHIT:
					break;
				default:
					throw new IllegalArgumentException();
			}
		}

		RegressionTable activeRegressionTable = RegressionModelUtil.createRegressionTable(mathContext, features, coefficients, intercept)
			.setTargetCategory(categoricalLabel.getValue(1));

		RegressionTable passiveRegressionTable = RegressionModelUtil.createRegressionTable(mathContext, Collections.emptyList(), Collections.emptyList(), null)
			.setTargetCategory(categoricalLabel.getValue(0));

		RegressionModel regressionModel = new RegressionModel(MiningFunction.CLASSIFICATION, ModelUtil.createMiningSchema(categoricalLabel), null)
			.setNormalizationMethod(normalizationMethod)
			.setMathContext(ModelUtil.simplifyMathContext(mathContext))
			.addRegressionTables(activeRegressionTable, passiveRegressionTable)
			.setOutput(hasProbabilityDistribution ? ModelUtil.createProbabilityOutput(mathContext, categoricalLabel) : null);

		return regressionModel;
	}

	static
	public RegressionModel createOrdinalClassification(Feature feature, List<? extends Number> thresholds, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		return createOrdinalClassification(null, feature, thresholds, normalizationMethod, hasProbabilityDistribution, schema);
	}

	static
	public RegressionModel createOrdinalClassification(MathContext mathContext, Feature feature, List<? extends Number> thresholds, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		OrdinalLabel ordinalLabel = schema.requireOrdinalLabel();

		SchemaUtil.checkSize(thresholds.size() + 1, ordinalLabel);

		switch(normalizationMethod){
			case NONE:
			case LOGIT:
			case PROBIT:
			case CLOGLOG:
			case LOGLOG:
			case CAUCHIT:
				break;
			default:
				throw new IllegalArgumentException();
		}

		List<RegressionTable> regressionTables = new ArrayList<>();

		for(int i = 0; i < thresholds.size(); i++){
			Number threshold = thresholds.get(i);

			RegressionTable regressionTable = RegressionModelUtil.createRegressionTable(mathContext, Collections.singletonList(feature), Collections.singletonList(1d), threshold)
				.setTargetCategory(ordinalLabel.getValue(i));

			regressionTables.add(regressionTable);
		}

		{
			RegressionTable trivialRegressionTable = RegressionModelUtil.createRegressionTable(mathContext, Collections.emptyList(), Collections.emptyList(), 1000d)
				.setTargetCategory(ordinalLabel.getValue(ordinalLabel.size() - 1));

			regressionTables.add(trivialRegressionTable);
		}

		RegressionModel regressionModel = new RegressionModel(MiningFunction.CLASSIFICATION, ModelUtil.createMiningSchema(ordinalLabel), regressionTables)
			.setNormalizationMethod(normalizationMethod)
			.setOutput(hasProbabilityDistribution ? ModelUtil.createProbabilityOutput(mathContext, ordinalLabel) : null);

		return regressionModel;
	}

	static
	public RegressionTable createRegressionTable(List<? extends Feature> features, List<? extends Number> coefficients, Number intercept){
		return createRegressionTable(null, features, coefficients, intercept);
	}

	static
	public RegressionTable createRegressionTable(MathContext mathContext, List<? extends Feature> features, List<? extends Number> coefficients, Number intercept){

		if(features.size() != coefficients.size()){
			throw new IllegalArgumentException();
		}

		RegressionTable regressionTable = new RegressionTable(0d);

		if(intercept != null && !ValueUtil.isZeroLike(intercept)){
			regressionTable.setIntercept(intercept);
		}

		Map<PredictorKey, NumericPredictor> numericPredictors = new LinkedHashMap<>();
		Map<PredictorKey, CategoricalPredictor> categoricalPredictors = new LinkedHashMap<>();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);
			Number coefficient = coefficients.get(i);

			if(coefficient == null || ValueUtil.isZeroLike(coefficient)){
				continue;
			} // End if

			if(feature instanceof ProductFeature){
				ProductFeature productFeature = (ProductFeature)feature;

				feature = productFeature.getFeature();
				coefficient = ValueUtil.multiply(mathContext, coefficient, productFeature.getFactor());
			} // End if

			if(feature instanceof BinaryFeature){
				BinaryFeature binaryFeature = (BinaryFeature)feature;

				PredictorKey predictorKey = new PredictorKey(binaryFeature.getName(), binaryFeature.getValue());

				CategoricalPredictor categoricalPredictor = categoricalPredictors.get(predictorKey);
				if(categoricalPredictor == null){
					categoricalPredictor = new CategoricalPredictor()
						.setField(binaryFeature.getName())
						.setValue(binaryFeature.getValue())
						.setCoefficient(coefficient);

					categoricalPredictors.put(predictorKey, categoricalPredictor);

					regressionTable.addCategoricalPredictors(categoricalPredictor);
				} else

				{
					categoricalPredictor.setCoefficient(ValueUtil.add(mathContext, categoricalPredictor.requireCoefficient(), coefficient));
				}
			} else

			if(feature instanceof BooleanFeature){
				BooleanFeature booleanFeature = (BooleanFeature)feature;

				PredictorKey predictorKey = new PredictorKey(booleanFeature.getName(), BooleanFeature.VALUE_TRUE);

				CategoricalPredictor categoricalPredictor = categoricalPredictors.get(predictorKey);
				if(categoricalPredictor == null){
					categoricalPredictor = new CategoricalPredictor()
						.setField(booleanFeature.getName())
						.setValue(BooleanFeature.VALUE_TRUE)
						.setCoefficient(coefficient);

					categoricalPredictors.put(predictorKey, categoricalPredictor);

					regressionTable.addCategoricalPredictors(categoricalPredictor);
				} else

				{
					categoricalPredictor.setCoefficient(ValueUtil.add(mathContext, categoricalPredictor.requireCoefficient(), coefficient));
				}
			} else

			if(feature instanceof ConstantFeature){
				ConstantFeature constantFeature = (ConstantFeature)feature;

				Number value = ValueUtil.add(mathContext, regressionTable.requireIntercept(), ValueUtil.multiply(mathContext, coefficient, constantFeature.getValue()));

				regressionTable.setIntercept(value);
			} else

			if(feature instanceof InteractionFeature){
				InteractionFeature interactionFeature = (InteractionFeature)feature;

				PredictorTerm predictorTerm = new PredictorTerm()
					.setName(interactionFeature.getName())
					.setCoefficient(coefficient);

				List<? extends Feature> inputFeatures = interactionFeature.getInputFeatures();
				for(Feature inputFeature : inputFeatures){

					if(inputFeature instanceof ConstantFeature){
						ConstantFeature constantFeature = (ConstantFeature)inputFeature;

						Number value = ValueUtil.multiply(mathContext, predictorTerm.requireCoefficient(), constantFeature.getValue());

						predictorTerm.setCoefficient(value);
					} else

					{
						inputFeature = inputFeature.toContinuousFeature();

						predictorTerm.addFieldRefs(inputFeature.ref());
					}
				}

				List<FieldRef> fieldRefs = predictorTerm.getFieldRefs();
				if(fieldRefs.size() == 0){
					Number value = ValueUtil.add(mathContext, regressionTable.getIntercept(), predictorTerm.requireCoefficient());

					regressionTable.setIntercept(value);
				} else

				if(fieldRefs.size() == 1){
					FieldRef fieldRef = Iterables.getOnlyElement(fieldRefs);

					NumericPredictor numericPredictor = new NumericPredictor()
						.setField(fieldRef.requireField())
						.setCoefficient(predictorTerm.requireCoefficient());

					regressionTable.addNumericPredictors(numericPredictor);
				} else

				{
					regressionTable.addPredictorTerms(predictorTerm);
				}
			} else

			if(feature instanceof PowerFeature){
				PowerFeature powerFeature = (PowerFeature)feature;

				NumericPredictor numericPredictor = new NumericPredictor()
					.setField(powerFeature.getName())
					.setExponent(powerFeature.getPower())
					.setCoefficient(coefficient);

				regressionTable.addNumericPredictors(numericPredictor);
			} else

			{
				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				PredictorKey predictorKey = new PredictorKey(continuousFeature.getName());

				NumericPredictor numericPredictor = numericPredictors.get(predictorKey);
				if(numericPredictor == null){
					numericPredictor = new NumericPredictor()
						.setField(continuousFeature.getName())
						.setCoefficient(coefficient);

					numericPredictors.put(predictorKey, numericPredictor);

					regressionTable.addNumericPredictors(numericPredictor);
				} else

				{
					numericPredictor.setCoefficient(ValueUtil.add(mathContext, numericPredictor.requireCoefficient(), coefficient));
				}
			}
		}

		return regressionTable;
	}

	static
	private class PredictorKey {

		private String name = null;

		private Object value = null;


		private PredictorKey(String name){
			this(name, null);
		}

		private PredictorKey(String name, Object value){
			this.name = name;
			this.value = value;
		}

		@Override
		public boolean equals(Object object){

			if(object instanceof PredictorKey){
				PredictorKey that = (PredictorKey)object;

				return Objects.equals(this.name, that.name) && Objects.equals(this.value, that.value);
			}

			return false;
		}

		@Override
		public int hashCode(){
			int result = 0;

			result = (31 * result) + Objects.hashCode(this.name);
			result = (31 * result) + Objects.hashCode(this.value);

			return result;
		}
	}
}
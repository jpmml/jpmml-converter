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
package org.jpmml.converter.general_regression;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.MathContext;
import org.dmg.pmml.general_regression.CovariateList;
import org.dmg.pmml.general_regression.FactorList;
import org.dmg.pmml.general_regression.GeneralRegressionModel;
import org.dmg.pmml.general_regression.PCell;
import org.dmg.pmml.general_regression.PPCell;
import org.dmg.pmml.general_regression.PPMatrix;
import org.dmg.pmml.general_regression.ParamMatrix;
import org.dmg.pmml.general_regression.Parameter;
import org.dmg.pmml.general_regression.ParameterList;
import org.dmg.pmml.general_regression.Predictor;
import org.dmg.pmml.general_regression.PredictorList;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.BooleanFeature;
import org.jpmml.converter.ConstantFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.InteractionFeature;
import org.jpmml.converter.PowerFeature;
import org.jpmml.converter.ProductFeature;
import org.jpmml.converter.ValueUtil;

public class GeneralRegressionModelUtil {

	private GeneralRegressionModelUtil(){
	}

	static
	public GeneralRegressionModel encodeRegressionTable(GeneralRegressionModel generalRegressionModel, List<? extends Feature> features, List<? extends Number> coefficients, Number intercept, Object targetCategory){
		return encodeRegressionTable(null, generalRegressionModel, features, coefficients, intercept, targetCategory);
	}

	static
	public GeneralRegressionModel encodeRegressionTable(MathContext mathContext, GeneralRegressionModel generalRegressionModel, List<? extends Feature> features, List<? extends Number> coefficients, Number intercept, Object targetCategory){

		if(features.size() != coefficients.size()){
			throw new IllegalArgumentException();
		}

		ParameterList parameterList = generalRegressionModel.getParameterList();
		if(parameterList == null){
			parameterList = new ParameterList();

			generalRegressionModel.setParameterList(parameterList);
		}

		PPMatrix ppMatrix = generalRegressionModel.getPPMatrix();
		if(ppMatrix == null){
			ppMatrix = new PPMatrix();

			generalRegressionModel.setPPMatrix(ppMatrix);
		}

		ParamMatrix paramMatrix = generalRegressionModel.getParamMatrix();
		if(paramMatrix == null){
			paramMatrix = new ParamMatrix();

			generalRegressionModel.setParamMatrix(paramMatrix);
		}

		int p = (parameterList.getParameters()).size();

		if(intercept != null && !ValueUtil.isZeroLike(intercept)){
			Parameter parameter = new Parameter("p" + String.valueOf(p))
				.setLabel("(intercept)");

			parameterList.addParameters(parameter);

			p++;

			PCell pCell = new PCell(parameter.getName(), intercept)
				.setTargetCategory(targetCategory);

			paramMatrix.addPCells(pCell);
		}

		Set<String> covariateFieldNames = new LinkedHashSet<>();
		Set<String> factorFieldNames = new LinkedHashSet<>();

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
			}

			Parameter parameter = new Parameter("p" + String.valueOf(p));

			parameterList.addParameters(parameter);

			p++;

			Number multiplier = createPPCells(mathContext, feature, parameter, ppMatrix, covariateFieldNames, factorFieldNames);
			if(!ValueUtil.isOne(multiplier)){
				coefficient = ValueUtil.multiply(mathContext, coefficient, multiplier);
			}

			PCell pCell = new PCell(parameter.getName(), coefficient)
				.setTargetCategory(targetCategory);

			paramMatrix.addPCells(pCell);
		}

		if(covariateFieldNames.size() > 0){
			CovariateList covariateList = generalRegressionModel.getCovariateList();

			if(covariateList == null){
				covariateList = new CovariateList();

				generalRegressionModel.setCovariateList(covariateList);
			}

			createPredictors(covariateList, covariateFieldNames);
		} // End if

		if(factorFieldNames.size() > 0){
			FactorList factorList = generalRegressionModel.getFactorList();

			if(factorList == null){
				factorList = new FactorList();

				generalRegressionModel.setFactorList(factorList);
			}

			createPredictors(factorList, factorFieldNames);
		}

		return generalRegressionModel;
	}

	static
	private Number createPPCells(MathContext mathContext, Feature feature, Parameter parameter, PPMatrix ppMatrix, Set<String> covariateFieldNames, Set<String> factorFieldNames){

		if(feature instanceof BinaryFeature){
			BinaryFeature binaryFeature = (BinaryFeature)feature;

			return createPPCell(binaryFeature.getValue(), binaryFeature.getName(), parameter, ppMatrix, factorFieldNames);
		} else

		if(feature instanceof BooleanFeature){
			BooleanFeature booleanFeature = (BooleanFeature)feature;

			return createPPCell(BooleanFeature.VALUE_TRUE, booleanFeature.getName(), parameter, ppMatrix, factorFieldNames);
		} else

		if(feature instanceof ConstantFeature){
			ConstantFeature constantFeature = (ConstantFeature)feature;

			return constantFeature.getValue();
		} else

		if(feature instanceof InteractionFeature){
			InteractionFeature interactionFeature = (InteractionFeature)feature;

			Number result = 1d;

			List<? extends Feature> inputFeatures = interactionFeature.getInputFeatures();
			for(Feature inputFeature : inputFeatures){
				Number value = createPPCells(mathContext, inputFeature, parameter, ppMatrix, covariateFieldNames, factorFieldNames);

				result = ValueUtil.multiply(mathContext, result, value);
			}

			return result;
		} else

		if(feature instanceof PowerFeature){
			PowerFeature powerFeature = (PowerFeature)feature;

			return createPPCell(String.valueOf(powerFeature.getPower()), powerFeature.getName(), parameter, ppMatrix, covariateFieldNames);
		} else

		{
			ContinuousFeature continuousFeature = feature.toContinuousFeature();

			return createPPCell("1", continuousFeature.getName(), parameter, ppMatrix, covariateFieldNames);
		}
	}

	static
	private Number createPPCell(Object value, String fieldName, Parameter parameter, PPMatrix ppMatrix, Set<String> fieldNames){
		PPCell ppCell = new PPCell(value, fieldName, parameter.getName());

		ppMatrix.addPPCells(ppCell);

		fieldNames.add(ppCell.getField());

		return 1d;
	}

	static
	private void createPredictors(PredictorList predictorList, Set<String> fieldNames){
		fieldNames = new LinkedHashSet<>(fieldNames);

		List<Predictor> predictors = predictorList.getPredictors();
		for(Predictor predictor : predictors){
			fieldNames.remove(predictor.getField());
		}

		if(fieldNames.isEmpty()){
			return;
		}

		for(String fieldName : fieldNames){
			Predictor predictor = new Predictor(fieldName);

			predictorList.addPredictors(predictor);
		}
	}
}
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

import org.dmg.pmml.FieldName;
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
	public GeneralRegressionModel encodeRegressionTable(GeneralRegressionModel generalRegressionModel, List<? extends Feature> features, List<Double> coefficients, Double intercept, String targetCategory){

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

		Set<FieldName> covariates = new LinkedHashSet<>();

		Set<FieldName> factors = new LinkedHashSet<>();

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);
			Double coefficient = coefficients.get(i);

			if(coefficient == null || ValueUtil.isZeroLike(coefficient)){
				continue;
			} // End if

			if(feature instanceof ProductFeature){
				ProductFeature productFeature = (ProductFeature)feature;

				feature = productFeature.getFeature();
				coefficient = (productFeature.getFactor()).doubleValue() * coefficient;
			}

			Parameter parameter = new Parameter("p" + String.valueOf(p));

			parameterList.addParameters(parameter);

			p++;

			double multiplier = createPPCells(feature, parameter, ppMatrix, covariates, factors);
			if(multiplier != 1d){
				coefficient = (multiplier * coefficient);
			}

			PCell pCell = new PCell(parameter.getName(), coefficient)
				.setTargetCategory(targetCategory);

			paramMatrix.addPCells(pCell);
		}

		if(covariates.size() > 0){
			CovariateList covariateList = generalRegressionModel.getCovariateList();

			if(covariateList == null){
				covariateList = new CovariateList();

				generalRegressionModel.setCovariateList(covariateList);
			}

			createPredictors(covariateList, covariates);
		} // End if

		if(factors.size() > 0){
			FactorList factorList = generalRegressionModel.getFactorList();

			if(factorList == null){
				factorList = new FactorList();

				generalRegressionModel.setFactorList(factorList);
			}

			createPredictors(factorList, factors);
		}

		return generalRegressionModel;
	}

	static
	private double createPPCells(Feature feature, Parameter parameter, PPMatrix ppMatrix, Set<FieldName> covariates, Set<FieldName> factors){

		if(feature instanceof BinaryFeature){
			BinaryFeature binaryFeature = (BinaryFeature)feature;

			return createPPCell(binaryFeature.getValue(), binaryFeature.getName(), parameter, ppMatrix, factors);
		} else

		if(feature instanceof BooleanFeature){
			BooleanFeature booleanFeature = (BooleanFeature)feature;

			return createPPCell("true", booleanFeature.getName(), parameter, ppMatrix, factors);
		} else

		if(feature instanceof ConstantFeature){
			ConstantFeature constantFeature = (ConstantFeature)feature;

			return (constantFeature.getValue()).doubleValue();
		} else

		if(feature instanceof InteractionFeature){
			InteractionFeature interactionFeature = (InteractionFeature)feature;

			double result = 1d;

			List<? extends Feature> inputFeatures = interactionFeature.getInputFeatures();
			for(Feature inputFeature : inputFeatures){
				result *= createPPCells(inputFeature, parameter, ppMatrix, covariates, factors);
			}

			return result;
		} else

		if(feature instanceof PowerFeature){
			PowerFeature powerFeature = (PowerFeature)feature;

			return createPPCell(String.valueOf(powerFeature.getPower()), powerFeature.getName(), parameter, ppMatrix, covariates);
		} else

		{
			ContinuousFeature continuousFeature = feature.toContinuousFeature();

			return createPPCell("1", continuousFeature.getName(), parameter, ppMatrix, covariates);
		}
	}

	static
	private double createPPCell(String value, FieldName name, Parameter parameter, PPMatrix ppMatrix, Set<FieldName> predictorNames){
		PPCell ppCell = new PPCell(value, name, parameter.getName());

		ppMatrix.addPPCells(ppCell);

		predictorNames.add(ppCell.getField());

		return 1d;
	}

	static
	private void createPredictors(PredictorList predictorList, Set<FieldName> names){
		names = new LinkedHashSet<>(names);

		List<Predictor> predictors = predictorList.getPredictors();
		for(Predictor predictor : predictors){
			names.remove(predictor.getField());
		}

		if(names.isEmpty()){
			return;
		}

		for(FieldName name : names){
			Predictor predictor = new Predictor(name);

			predictorList.addPredictors(predictor);
		}
	}
}
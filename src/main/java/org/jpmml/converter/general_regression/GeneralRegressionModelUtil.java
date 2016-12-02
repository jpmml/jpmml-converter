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
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.InteractionFeature;
import org.jpmml.converter.ValueUtil;

public class GeneralRegressionModelUtil {

	private GeneralRegressionModelUtil(){
	}

	static
	public GeneralRegressionModel encodeRegressionTable(GeneralRegressionModel generalRegressionModel, List<Feature> features, Double intercept, List<Double> coefficients, String targetCategory){

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

		if(intercept != null && !ValueUtil.isZero(intercept)){
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

			if(coefficient == null || coefficient.isNaN()){
				continue;
			}

			Parameter parameter = new Parameter("p" + String.valueOf(p));

			parameterList.addParameters(parameter);

			p++;

			createPPCells(feature, parameter, ppMatrix, covariates, factors);

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
	private void createPPCells(Feature feature, Parameter parameter, PPMatrix ppMatrix, Set<FieldName> covariates, Set<FieldName> factors){

		if(feature instanceof BinaryFeature){
			BinaryFeature binaryFeature = (BinaryFeature)feature;

			PPCell ppCell = new PPCell(binaryFeature.getValue(), binaryFeature.getName(), parameter.getName());

			ppMatrix.addPPCells(ppCell);

			factors.add(ppCell.getPredictorName());
		} else

		if(feature instanceof InteractionFeature){
			InteractionFeature interactionFeature = (InteractionFeature)feature;

			List<Feature> inputFeatures = interactionFeature.getInputFeatures();
			for(Feature inputFeature : inputFeatures){
				createPPCells(inputFeature, parameter, ppMatrix, covariates, factors);
			}
		} else

		if(feature instanceof ContinuousFeature){
			ContinuousFeature continuousFeature = (ContinuousFeature)feature;

			PPCell ppCell = new PPCell("1", continuousFeature.getName(), parameter.getName());

			ppMatrix.addPPCells(ppCell);

			covariates.add(ppCell.getPredictorName());
		} else

		{
			throw new IllegalArgumentException();
		}
	}

	static
	private void createPredictors(PredictorList predictorList, Set<FieldName> names){
		names = new LinkedHashSet<>(names);

		List<Predictor> predictors = predictorList.getPredictors();
		for(Predictor predictor : predictors){
			names.remove(predictor.getName());
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
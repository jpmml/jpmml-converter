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

import java.util.List;

import org.dmg.pmml.regression.CategoricalPredictor;
import org.dmg.pmml.regression.NumericPredictor;
import org.dmg.pmml.regression.PredictorTerm;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.InteractionFeature;
import org.jpmml.converter.PowerFeature;

public class RegressionModelUtil {

	private RegressionModelUtil(){
	}

	static
	public RegressionTable createRegressionTable(List<? extends Feature> features, Double intercept, List<Double> coefficients){

		if(features.size() != coefficients.size()){
			throw new IllegalArgumentException();
		}

		RegressionTable regressionTable = new RegressionTable(intercept != null ? intercept : 0d);

		for(int i = 0; i < features.size(); i++){
			Feature feature = features.get(i);
			Double coefficient = coefficients.get(i);

			if(coefficient == null || coefficient.isNaN()){
				continue;
			} // End if

			if(feature instanceof BinaryFeature){
				BinaryFeature binaryFeature = (BinaryFeature)feature;

				CategoricalPredictor categoricalPredictor = new CategoricalPredictor()
					.setName(binaryFeature.getName())
					.setValue(binaryFeature.getValue())
					.setCoefficient(coefficient);

				regressionTable.addCategoricalPredictors(categoricalPredictor);
			} else

			if(feature instanceof InteractionFeature){
				InteractionFeature interactionFeature = (InteractionFeature)feature;

				PredictorTerm predictorTerm = new PredictorTerm()
					.setName(interactionFeature.getName())
					.setCoefficient(coefficient);

				List<? extends Feature> inputFeatures = interactionFeature.getInputFeatures();
				for(Feature inputFeature : inputFeatures){
					inputFeature = inputFeature.toContinuousFeature();

					predictorTerm.addFieldRefs(inputFeature.ref());
				}

				regressionTable.addPredictorTerms(predictorTerm);
			} else

			if(feature instanceof PowerFeature){
				PowerFeature powerFeature = (PowerFeature)feature;

				NumericPredictor numericPredictor = new NumericPredictor()
					.setName(powerFeature.getName())
					.setExponent(powerFeature.getPower())
					.setCoefficient(coefficient);

				regressionTable.addNumericPredictors(numericPredictor);
			} else

			{
				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				NumericPredictor numericPredictor = new NumericPredictor()
					.setName(continuousFeature.getName())
					.setCoefficient(coefficient);

				regressionTable.addNumericPredictors(numericPredictor);
			}
		}

		return regressionTable;
	}
}
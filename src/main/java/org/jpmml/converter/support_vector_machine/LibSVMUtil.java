/*
 * Copyright (c) 2017 Villu Ruusmann
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
package org.jpmml.converter.support_vector_machine;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.regression.CategoricalPredictor;
import org.dmg.pmml.support_vector_machine.Coefficient;
import org.dmg.pmml.support_vector_machine.Coefficients;
import org.dmg.pmml.support_vector_machine.SupportVector;
import org.dmg.pmml.support_vector_machine.SupportVectorMachine;
import org.dmg.pmml.support_vector_machine.SupportVectorMachineModel;
import org.dmg.pmml.support_vector_machine.SupportVectors;
import org.dmg.pmml.support_vector_machine.VectorDictionary;
import org.dmg.pmml.support_vector_machine.VectorFields;
import org.dmg.pmml.support_vector_machine.VectorInstance;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.CMatrixUtil;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.Matrix;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.PMMLUtil;
import org.jpmml.converter.Schema;
import org.jpmml.converter.ValueUtil;

public class LibSVMUtil {

	private LibSVMUtil(){
	}

	static
	public SupportVectorMachineModel createRegression(Matrix<Double> sv, List<String> ids, Double rho, List<Double> coefs, Schema schema){
		VectorDictionary vectorDictionary = LibSVMUtil.createVectorDictionary(sv, ids, schema);

		List<VectorInstance> vectorInstances = vectorDictionary.getVectorInstances();

		List<SupportVectorMachine> supportVectorMachines = new ArrayList<>();
		supportVectorMachines.add(LibSVMUtil.createSupportVectorMachine(vectorInstances, rho, coefs));

		SupportVectorMachineModel supportVectorMachineModel = new SupportVectorMachineModel(MiningFunction.REGRESSION, ModelUtil.createMiningSchema(schema), vectorDictionary, supportVectorMachines);

		return supportVectorMachineModel;
	}

	static
	public SupportVectorMachineModel createClassification(Matrix<Double> sv, List<Integer> nSv, List<String> ids, List<Double> rho, List<Double> coefs, Schema schema){
		int numberOfVectors = sv.getRows();
		int numberOfFeatures = sv.getColumns();

		VectorDictionary vectorDictionary = LibSVMUtil.createVectorDictionary(sv, ids, schema);

		List<VectorInstance> vectorInstances = vectorDictionary.getVectorInstances();

		List<SupportVectorMachine> supportVectorMachines = new ArrayList<>();

		int[] offsets = new int[nSv.size() + 1];

		for(int i = 0; i < nSv.size(); i++){
			offsets[i + 1] = offsets[i] + nSv.get(i);
		}

		int i = 0;

		CategoricalLabel categoricalLabel = (CategoricalLabel)schema.getLabel();

		for(int first = 0, size = categoricalLabel.size(); first < size; first++){

			for(int second = first + 1; second < size; second++){
				List<VectorInstance> svmVectorInstances = new ArrayList<>();
				svmVectorInstances.addAll(slice(vectorInstances, offsets, first));
				svmVectorInstances.addAll(slice(vectorInstances, offsets, second));

				Double svmRho = rho.get(i);

				List<Double> svmCoefs = new ArrayList<>();
				svmCoefs.addAll(slice(CMatrixUtil.getRow(coefs, size - 1, numberOfVectors, second - 1), offsets, first));
				svmCoefs.addAll(slice(CMatrixUtil.getRow(coefs, size - 1, numberOfVectors, first), offsets, second));

				SupportVectorMachine supportVectorMachine = LibSVMUtil.createSupportVectorMachine(svmVectorInstances, svmRho, svmCoefs)
					.setTargetCategory(categoricalLabel.getValue(first))
					.setAlternateTargetCategory(categoricalLabel.getValue(second));

				supportVectorMachines.add(supportVectorMachine);

				i++;
			}
		}

		SupportVectorMachineModel supportVectorMachineModel = new SupportVectorMachineModel(MiningFunction.CLASSIFICATION, ModelUtil.createMiningSchema(schema), vectorDictionary, supportVectorMachines)
			.setClassificationMethod(SupportVectorMachineModel.ClassificationMethod.ONE_AGAINST_ONE);

		return supportVectorMachineModel;
	}

	static
	public VectorDictionary createVectorDictionary(Matrix<Double> sv, List<String> ids, Schema schema){
		int numberOfVectors = sv.getRows();
		int numberOfFeatures = sv.getColumns();

		List<Feature> features = schema.getFeatures();

		if(numberOfFeatures != features.size()){
			throw new IllegalArgumentException();
		}

		BitSet featureMask = new BitSet(numberOfFeatures);

		Double defaultValue = Double.valueOf(0d);

		for(int i = 0; i < numberOfVectors; i++){
			List<? extends Number> values = sv.getRowValues(i);

			BitSet vectorFeatureMask = ValueUtil.getIndices(values, defaultValue);

			// Set bits that correspond to non-zero values
			vectorFeatureMask.flip(0, numberOfFeatures);

			featureMask.or(vectorFeatureMask);
		}

		int numberOfUsedFeatures = featureMask.cardinality();

		VectorFields vectorFields = new VectorFields();

		for(int i = 0; i < numberOfFeatures; i++){
			Feature feature = schema.getFeature(i);

			if(!featureMask.get(i)){
				continue;
			} // End if

			if(feature instanceof BinaryFeature){
				BinaryFeature binaryFeature = (BinaryFeature)feature;

				CategoricalPredictor categoricalPredictor = new CategoricalPredictor(binaryFeature.getName(), binaryFeature.getValue(), 1d);

				vectorFields.addContent(categoricalPredictor);
			} else

			{
				ContinuousFeature continuousFeature = feature.toContinuousFeature();

				vectorFields.addContent(continuousFeature.ref());
			}
		}

		VectorDictionary vectorDictionary = new VectorDictionary(vectorFields);

		for(int i = 0; i < numberOfVectors; i++){
			List<? extends Number> values = sv.getRowValues(i);

			if(numberOfUsedFeatures < numberOfFeatures){
				values = ValueUtil.filterByIndices(values, featureMask);
			}

			VectorInstance vectorInstance = new VectorInstance(ids.get(i));

			if(ValueUtil.isSparse(values, defaultValue, 0.75d)){
				vectorInstance.setRealSparseArray(PMMLUtil.createRealSparseArray(values, defaultValue));
			} else

			{
				vectorInstance.setArray(PMMLUtil.createRealArray(values));
			}

			vectorDictionary.addVectorInstances(vectorInstance);
		}

		return vectorDictionary;
	}

	static
	public SupportVectorMachine createSupportVectorMachine(List<VectorInstance> vectorInstances, Double rho, List<Double> coefs){

		if(vectorInstances.size() != coefs.size()){
			throw new IllegalArgumentException();
		}

		Coefficients coefficients = new Coefficients()
			.setAbsoluteValue(rho);

		SupportVectors supportVectors = new SupportVectors();

		for(int i = 0; i < vectorInstances.size(); i++){
			VectorInstance vectorInstance = vectorInstances.get(i);

			Coefficient coefficient = new Coefficient()
				.setValue(coefs.get(i));

			coefficients.addCoefficients(coefficient);

			SupportVector supportVector = new SupportVector(vectorInstance.getId());

			supportVectors.addSupportVectors(supportVector);
		}

		SupportVectorMachine supportVectorMachine = new SupportVectorMachine(coefficients)
			.setSupportVectors(supportVectors);

		return supportVectorMachine;
	}

	static
	private <E> List<E> slice(List<E> list, int[] offsets, int index){
		return list.subList(offsets[index], offsets[index + 1]);
	}
}
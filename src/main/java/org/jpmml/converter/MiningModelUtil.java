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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.NumericPredictor;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.RegressionModel;
import org.dmg.pmml.RegressionNormalizationMethodType;
import org.dmg.pmml.RegressionTable;
import org.dmg.pmml.Segment;
import org.dmg.pmml.Segmentation;
import org.dmg.pmml.True;

public class MiningModelUtil {

	private MiningModelUtil(){
	}

	static
	public MiningModel createBinaryLogisticClassification(Schema schema, Model model, double coefficient, boolean hasProbabilityDistribution){
		return createBinaryLogisticClassification(schema.getTargetField(), schema.getTargetCategories(), schema.getActiveFields(), model, coefficient, hasProbabilityDistribution);
	}

	static
	public MiningModel createBinaryLogisticClassification(FieldName targetField, List<String> targetCategories, List<FieldName> activeFields, Model model, double coefficient, boolean hasProbabilityDistribution){

		if(targetCategories.size() != 2){
			throw new IllegalArgumentException();
		}

		FieldName inputField = MiningModelUtil.MODEL_PREDICTION.apply(model);

		RegressionTable activeRegressionTable = new RegressionTable(0d)
			.setTargetCategory(targetCategories.get(0))
			.addNumericPredictors(new NumericPredictor(inputField, coefficient));

		RegressionTable passiveRegressionTable = new RegressionTable(0d)
			.setTargetCategory(targetCategories.get(1));

		Output output = (hasProbabilityDistribution ? new Output(ModelUtil.createProbabilityFields(targetCategories)) : null);

		MiningSchema miningSchema = ModelUtil.createMiningSchema(targetField, Collections.singletonList(inputField));

		RegressionModel regressionModel = new RegressionModel(MiningFunctionType.CLASSIFICATION, miningSchema, null)
			.setNormalizationMethod(RegressionNormalizationMethodType.SOFTMAX)
			.addRegressionTables(activeRegressionTable, passiveRegressionTable)
			.setOutput(output);

		List<Model> segmentationModels = Arrays.asList(model, regressionModel);

		return createModelChain(targetField, activeFields, segmentationModels);
	}

	static
	public MiningModel createClassification(Schema schema, List<? extends Model> models, RegressionNormalizationMethodType regressionNormalizationMethod, boolean hasProbabilityDistribution){
		return createClassification(schema.getTargetField(), schema.getTargetCategories(), schema.getActiveFields(), models, regressionNormalizationMethod, hasProbabilityDistribution);
	}

	static
	public MiningModel createClassification(FieldName targetField, List<String> targetCategories, List<FieldName> activeFields, List<? extends Model> models, RegressionNormalizationMethodType regressionNormalizationMethod, boolean hasProbabilityDistribution){

		if(targetCategories.size() != models.size()){
			throw new IllegalArgumentException();
		}

		List<FieldName> inputFields = Lists.transform(models, MiningModelUtil.MODEL_PREDICTION);

		List<RegressionTable> regressionTables = new ArrayList<>();

		for(int i = 0; i < targetCategories.size(); i++){
			RegressionTable regressionTable = new RegressionTable(0d)
				.setTargetCategory(targetCategories.get(i))
				.addNumericPredictors(new NumericPredictor(inputFields.get(i), 1d));

			regressionTables.add(regressionTable);
		}

		Output output = (hasProbabilityDistribution ? new Output(ModelUtil.createProbabilityFields(targetCategories)) : null);

		MiningSchema miningSchema = ModelUtil.createMiningSchema(targetField, inputFields);

		RegressionModel regressionModel = new RegressionModel(MiningFunctionType.CLASSIFICATION, miningSchema, regressionTables)
			.setNormalizationMethod(regressionNormalizationMethod)
			.setOutput(output);

		List<Model> segmentationModels = new ArrayList<>(models);
		segmentationModels.add(regressionModel);

		return createModelChain(targetField, activeFields, segmentationModels);
	}

	static
	private MiningModel createModelChain(FieldName targetField, List<FieldName> activeFields, List<? extends Model> models){
		Segmentation segmentation = createSegmentation(MultipleModelMethodType.MODEL_CHAIN, models);

		MiningSchema miningSchema = ModelUtil.createMiningSchema(targetField, activeFields);

		MiningModel miningModel = new MiningModel(MiningFunctionType.CLASSIFICATION, miningSchema)
			.setSegmentation(segmentation);

		return miningModel;
	}

	static
	public Segmentation createSegmentation(MultipleModelMethodType multipleModelMethod, List<? extends Model> models){
		return createSegmentation(multipleModelMethod, models, null);
	}

	static
	public Segmentation createSegmentation(MultipleModelMethodType multipleModelMethod, List<? extends Model> models, List<? extends Number> weights){

		if((weights != null) && (models.size() != weights.size())){
			throw new IllegalArgumentException();
		}

		List<Segment> segments = new ArrayList<>();

		for(int i = 0; i < models.size(); i++){
			Model model = models.get(i);
			Number weight = (weights != null ? weights.get(i) : null);

			Segment segment = new Segment()
				.setId(String.valueOf(i + 1))
				.setPredicate(new True())
				.setModel(model);

			if(weight != null && !ValueUtil.isOne(weight)){
				segment.setWeight(ValueUtil.asDouble(weight));
			}

			segments.add(segment);
		}

		Segmentation segmentation = new Segmentation(multipleModelMethod, segments);

		return segmentation;
	}

	private static final Function<Model, FieldName> MODEL_PREDICTION = new Function<Model, FieldName>(){

		@Override
		public FieldName apply(Model model){
			Output output = model.getOutput();

			if(output == null || !output.hasOutputFields()){
				throw new IllegalArgumentException();
			}

			OutputField outputField = Iterables.getLast(output.getOutputFields());

			return outputField.getName();
		}
	};
}
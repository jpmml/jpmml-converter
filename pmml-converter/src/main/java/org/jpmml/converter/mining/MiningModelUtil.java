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
package org.jpmml.converter.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Iterables;
import org.dmg.pmml.MathContext;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.True;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.mining.Segmentation;
import org.dmg.pmml.regression.RegressionModel;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.PMMLEncoder;
import org.jpmml.converter.Schema;
import org.jpmml.converter.SchemaUtil;
import org.jpmml.converter.ValueUtil;
import org.jpmml.converter.regression.RegressionModelUtil;

public class MiningModelUtil {

	private MiningModelUtil(){
	}

	static
	public MiningModel createRegression(Model model, RegressionModel.NormalizationMethod normalizationMethod, Schema schema){
		Feature feature = getPrediction(model, schema);

		RegressionModel regressionModel = RegressionModelUtil.createRegression(model.getMathContext(), Collections.singletonList(feature), Collections.singletonList(1d), null, normalizationMethod, schema);

		return createModelChain(Arrays.asList(model, regressionModel));
	}

	static
	public MiningModel createBinaryLogisticClassification(Model model, double coefficient, double intercept, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		Feature feature = getPrediction(model, schema);

		RegressionModel regressionModel = RegressionModelUtil.createBinaryLogisticClassification(model.getMathContext(), Collections.singletonList(feature), Collections.singletonList(coefficient), intercept, normalizationMethod, hasProbabilityDistribution, schema);

		return createModelChain(Arrays.asList(model, regressionModel));
	}

	static
	public MiningModel createClassification(List<? extends Model> models, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		CategoricalLabel categoricalLabel = (CategoricalLabel)schema.getLabel();

		SchemaUtil.checkSize(models.size(), categoricalLabel);

		if(normalizationMethod != null){

			switch(normalizationMethod){
				case NONE:
					if(categoricalLabel.size() < 3){
						throw new IllegalArgumentException();
					}
					break;
				case SIMPLEMAX:
				case SOFTMAX:
					if(categoricalLabel.size() < 2){
						throw new IllegalArgumentException();
					}
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else

		{
			if(categoricalLabel.size() < 3){
				throw new IllegalArgumentException();
			}
		}

		MathContext mathContext = null;

		List<RegressionTable> regressionTables = new ArrayList<>();

		for(int i = 0; i < categoricalLabel.size(); i++){
			Model model = models.get(i);

			MathContext modelMathContext = model.getMathContext();
			if(modelMathContext == null){
				modelMathContext = MathContext.DOUBLE;
			} // End if

			if(mathContext == null){
				mathContext = modelMathContext;
			} else

			{
				if(!Objects.equals(mathContext, modelMathContext)){
					throw new IllegalArgumentException();
				}
			}

			Feature feature = getPrediction(model, schema);

			RegressionTable regressionTable = RegressionModelUtil.createRegressionTable(mathContext, Collections.singletonList(feature), Collections.singletonList(1d), null)
				.setTargetCategory(categoricalLabel.getValue(i));

			regressionTables.add(regressionTable);
		}

		RegressionModel regressionModel = new RegressionModel(MiningFunction.CLASSIFICATION, ModelUtil.createMiningSchema(categoricalLabel), regressionTables)
			.setNormalizationMethod(normalizationMethod)
			.setMathContext(ModelUtil.simplifyMathContext(mathContext))
			.setOutput(hasProbabilityDistribution ? ModelUtil.createProbabilityOutput(mathContext, categoricalLabel) : null);

		List<Model> segmentationModels = new ArrayList<>(models);
		segmentationModels.add(regressionModel);

		return createModelChain(segmentationModels);
	}

	static
	public MiningModel createModelChain(List<? extends Model> models){
		return createModelChain(models, Segmentation.MissingPredictionTreatment.RETURN_MISSING);
	}

	static
	public MiningModel createModelChain(List<? extends Model> models, Segmentation.MissingPredictionTreatment missingPredictionTreatment){

		if(models.isEmpty()){
			throw new IllegalArgumentException();
		}

		MiningSchema miningSchema = new MiningSchema();

		models.stream()
			.map(Model::getMiningSchema)
			.map(MiningSchema::getMiningFields)
			.flatMap(List::stream)
			.filter(miningField -> {
				MiningField.UsageType usageType = miningField.getUsageType();

				switch(usageType){
					case PREDICTED:
					case TARGET:
						return true;
					default:
						return false;
				}
			})
			.map(MiningField::getName)
			.distinct()
			.map(name -> ModelUtil.createMiningField(name, MiningField.UsageType.TARGET))
			.forEach(miningSchema::addMiningFields);

		Segmentation segmentation = createSegmentation(Segmentation.MultipleModelMethod.MODEL_CHAIN, models)
			.setMissingPredictionTreatment(missingPredictionTreatment);

		Model lastModel = Iterables.getLast(models);

		MiningModel miningModel = new MiningModel(lastModel.getMiningFunction(), miningSchema)
			.setMathContext(ModelUtil.simplifyMathContext(lastModel.getMathContext()))
			.setSegmentation(segmentation);

		return miningModel;
	}

	static
	public Segmentation createSegmentation(Segmentation.MultipleModelMethod multipleModelMethod, List<? extends Model> models){
		return createSegmentation(multipleModelMethod, models, null);
	}

	static
	public Segmentation createSegmentation(Segmentation.MultipleModelMethod multipleModelMethod, List<? extends Model> models, List<? extends Number> weights){

		if((weights != null) && (models.size() != weights.size())){
			throw new IllegalArgumentException();
		}

		List<Segment> segments = new ArrayList<>();

		for(int i = 0; i < models.size(); i++){
			Model model = models.get(i);
			Number weight = (weights != null ? weights.get(i) : null);

			Segment segment = new Segment(True.INSTANCE, model)
				.setId(String.valueOf(i + 1));

			if(weight != null && !ValueUtil.isOne(weight)){
				segment.setWeight(weight);
			}

			segments.add(segment);
		}

		return new Segmentation(multipleModelMethod, segments);
	}

	static
	public Model getFinalModel(Model model){

		if(model instanceof MiningModel){
			MiningModel miningModel = (MiningModel)model;

			return getFinalModel(miningModel);
		}

		return model;
	}

	static
	public Model getFinalModel(MiningModel miningModel){
		Segmentation segmentation = miningModel.getSegmentation();

		Segmentation.MultipleModelMethod multipleModelMethod = segmentation.getMultipleModelMethod();
		switch(multipleModelMethod){
			case SELECT_FIRST:
			case SELECT_ALL:
				throw new IllegalArgumentException();
			case MODEL_CHAIN:
				{
					List<Segment> segments = segmentation.getSegments();
					if(segments.isEmpty()){
						throw new IllegalArgumentException();
					}

					Segment finalSegment = segments.get(segments.size() - 1);

					Predicate predicate = finalSegment.getPredicate();
					if(!(predicate instanceof True)){
						throw new IllegalArgumentException();
					}

					Model model = finalSegment.getModel();

					return getFinalModel(model);
				}
			default:
				break;
		}

		return miningModel;
	}

	static
	private ContinuousFeature getPrediction(Model model, Schema schema){
		Output output = model.getOutput();

		if(output == null || !output.hasOutputFields()){
			throw new IllegalArgumentException();
		}

		OutputField outputField = Iterables.getLast(output.getOutputFields());

		PMMLEncoder encoder = schema.getEncoder();

		return new ContinuousFeature(encoder, outputField);
	}
}
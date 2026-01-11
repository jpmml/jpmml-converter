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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import org.dmg.pmml.MathContext;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.ResultFeature;
import org.dmg.pmml.True;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.mining.Segmentation;
import org.dmg.pmml.regression.RegressionModel;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.converter.CategoricalLabel;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelEncoder;
import org.jpmml.converter.ModelUtil;
import org.jpmml.converter.Schema;
import org.jpmml.converter.ValueUtil;
import org.jpmml.converter.regression.RegressionModelUtil;
import org.jpmml.model.InvalidElementException;
import org.jpmml.model.ReflectionUtil;
import org.jpmml.model.UnsupportedAttributeException;

public class MiningModelUtil {

	private MiningModelUtil(){
	}

	static
	public MiningModel createRegression(Model model, RegressionModel.NormalizationMethod normalizationMethod, Schema schema){
		Feature feature = getPrediction(model, schema);

		MathContext mathContext = model.getMathContext();

		RegressionModel regressionModel = RegressionModelUtil.createRegression(mathContext, Collections.singletonList(feature), Collections.singletonList(1d), null, normalizationMethod, schema);

		MiningModel miningModel = createModelChain(Arrays.asList(model, regressionModel), Segmentation.MissingPredictionTreatment.RETURN_MISSING)
			.setMathContext(ModelUtil.simplifyMathContext(mathContext));

		return miningModel;
	}

	static
	public MiningModel createBinaryLogisticClassification(Model model, double coefficient, double intercept, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		Feature feature = getPrediction(model, schema);

		MathContext mathContext = model.getMathContext();

		RegressionModel regressionModel = RegressionModelUtil.createBinaryLogisticClassification(mathContext, Collections.singletonList(feature), Collections.singletonList(coefficient), intercept, normalizationMethod, hasProbabilityDistribution, schema);

		MiningModel miningModel = createModelChain(Arrays.asList(model, regressionModel), Segmentation.MissingPredictionTreatment.RETURN_MISSING)
			.setMathContext(ModelUtil.simplifyMathContext(mathContext));

		return miningModel;
	}

	static
	public MiningModel createClassification(List<? extends Model> models, RegressionModel.NormalizationMethod normalizationMethod, boolean hasProbabilityDistribution, Schema schema){
		CategoricalLabel categoricalLabel = schema.requireCategoricalLabel()
			.expectCardinality(models.size());

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

		MiningModel miningModel = createModelChain(segmentationModels, Segmentation.MissingPredictionTreatment.RETURN_MISSING)
			.setMathContext(ModelUtil.simplifyMathContext(mathContext));

		return miningModel;
	}

	static
	public MiningModel createModelChain(List<? extends Model> models, Segmentation.MissingPredictionTreatment missingPredictionTreatment){

		if(models.isEmpty()){
			throw new IllegalArgumentException();
		}

		Model lastModel = Iterables.getLast(models);

		MiningFunction miningFunction = lastModel.requireMiningFunction();

		MiningModel miningModel = new MiningModel(miningFunction, createMiningSchema(models))
			.setSegmentation(createSegmentation(Segmentation.MultipleModelMethod.MODEL_CHAIN, missingPredictionTreatment, models));

		return miningModel;
	}

	static
	public MiningModel createMultiModelChain(List<? extends Model> models, Segmentation.MissingPredictionTreatment missingPredictionTreatment){

		if(models.isEmpty()){
			throw new IllegalArgumentException();
		}

		MiningFunction miningFunction = null;

		for(Model model : models){
			MiningFunction modelMiningFunction = model.requireMiningFunction();

			if(miningFunction == null){
				miningFunction = modelMiningFunction;
			} else

			if(miningFunction == MiningFunction.MIXED){
				// Ignored
			} else

			{
				if(!Objects.equals(miningFunction, modelMiningFunction)){
					miningFunction = MiningFunction.MIXED;
				}
			}
		}

		MiningModel miningModel = new MiningModel(miningFunction, createMiningSchema(models))
			.setSegmentation(createSegmentation(Segmentation.MultipleModelMethod.MULTI_MODEL_CHAIN, missingPredictionTreatment, models));

		return miningModel;
	}

	static
	public MiningSchema createMiningSchema(List<? extends Model> models){
		MiningSchema miningSchema = new MiningSchema();

		models.stream()
			.map(Model::requireMiningSchema)
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

		return miningSchema;
	}

	static
	public Segmentation createSegmentation(Segmentation.MultipleModelMethod multipleModelMethod, Segmentation.MissingPredictionTreatment missingPredictionTreatment, List<? extends Model> models){
		return createSegmentation(multipleModelMethod, missingPredictionTreatment, models, null);
	}

	static
	public Segmentation createSegmentation(Segmentation.MultipleModelMethod multipleModelMethod, Segmentation.MissingPredictionTreatment missingPredictionTreatment, List<? extends Model> models, List<? extends Number> weights){

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

		Segmentation segmentation = new Segmentation(multipleModelMethod, segments)
			.setMissingPredictionTreatment(missingPredictionTreatment);

		return segmentation;
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
		Segmentation segmentation = miningModel.requireSegmentation();

		Segmentation.MultipleModelMethod multipleModelMethod = segmentation.requireMultipleModelMethod();
		switch(multipleModelMethod){
			case SELECT_ALL:
				throw new UnsupportedAttributeException(segmentation, multipleModelMethod);
			case MODEL_CHAIN:
				if(isChain(segmentation)){
					List<Segment> segments = segmentation.requireSegments();

					Segment finalSegment = segments.get(segments.size() - 1);

					@SuppressWarnings("unused")
					True _true = finalSegment.requirePredicate(True.class);
					Model model = finalSegment.requireModel();

					return getFinalModel(model);
				}
				// Falls through
			default:
				break;
		}

		return miningModel;
	}

	static
	public boolean isChain(Segmentation segmentation){
		List<Segment> segments = segmentation.requireSegments();

		for(Segment segment : segments){
			Predicate predicate = segment.requirePredicate();

			if(!(predicate instanceof True)){
				return false;
			}
		}

		return true;
	}

	static
	public void optimizeOutputFields(MiningModel miningModel){
		Segmentation segmentation = miningModel.requireSegmentation();

		Map<String, OutputField> commonOutputFields = collectCommonOutputFields(segmentation);
		if(!commonOutputFields.isEmpty()){
			Output output = ModelUtil.ensureOutput(miningModel);

			removeCommonOutputFields(segmentation, commonOutputFields.keySet());

			List<OutputField> outputFields = output.getOutputFields();
			outputFields.addAll(commonOutputFields.values());
		}
	}

	static
	private Map<String, OutputField> collectCommonOutputFields(Segmentation segmentation){
		List<Segment> segments = segmentation.requireSegments();

		Map<String, OutputField> result = null;

		for(Segment segment : segments){
			Model model = segment.requireModel();

			Model finalModel = MiningModelUtil.getFinalModel(model);

			Output output = finalModel.getOutput();
			if(output != null && output.hasOutputFields()){
				List<OutputField> outputFields = output.getOutputFields();

				if(result == null){
					result = outputFields.stream()
						.filter((outputField) -> {
							ResultFeature resultFeature = outputField.getResultFeature();

							switch(resultFeature){
								case PROBABILITY:
								case AFFINITY:
									return true;
								default:
									return false;
							}
						})
						.collect(Collectors.toMap(outputField -> outputField.requireName(), outputField -> outputField));
				} else

				{
					Set<String> names = new LinkedHashSet<>();

					for(OutputField outputField : outputFields){
						String name = outputField.requireName();

						names.add(name);

						OutputField commonOutputField = result.get(name);
						if(commonOutputField != null && !ReflectionUtil.equals(outputField, commonOutputField)){
							result.remove(name);
						}
					}

					(result.keySet()).retainAll(names);
				}
			} else

			{
				result = Collections.emptyMap();
			} // End if

			if(result.isEmpty()){
				break;
			}
		}

		return result;
	}

	static
	private void removeCommonOutputFields(Segmentation segmentation, Set<String> names){
		List<Segment> segments = segmentation.requireSegments();

		for(Segment segment : segments){
			Model model = segment.requireModel();

			Model finalModel = MiningModelUtil.getFinalModel(model);

			Output output = finalModel.getOutput();
			if(output != null && output.hasOutputFields()){
				List<OutputField> outputFields = output.getOutputFields();

				outputFields.removeIf((outputField) -> {
					return names.contains(outputField.requireName());
				});

				if(outputFields.isEmpty()){
					finalModel.setOutput(null);
				}
			}
		}
	}

	static
	private ContinuousFeature getPrediction(Model model, Schema schema){
		Output output = model.getOutput();

		if(output == null || !output.hasOutputFields()){
			throw new InvalidElementException(model);
		}

		OutputField outputField = Iterables.getLast(output.getOutputFields());

		ModelEncoder encoder = schema.getEncoder();

		return new ContinuousFeature(encoder, outputField);
	}
}
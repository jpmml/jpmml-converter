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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Entity;
import org.dmg.pmml.InlineTable;
import org.dmg.pmml.MathContext;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.ModelStats;
import org.dmg.pmml.ModelVerification;
import org.dmg.pmml.NamespacePrefixes;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.ResultFeature;
import org.dmg.pmml.Row;
import org.dmg.pmml.Target;
import org.dmg.pmml.Targets;
import org.dmg.pmml.VerificationField;
import org.dmg.pmml.VerificationFields;

public class ModelUtil {

	private ModelUtil(){
	}

	static
	public MathContext simplifyMathContext(MathContext mathContext){

		if(mathContext == MathContext.DOUBLE){
			return null;
		}

		return mathContext;
	}

	static
	public MiningSchema createMiningSchema(Schema schema){
		Label label = schema.getLabel();

		return createMiningSchema(label);
	}

	static
	public MiningSchema createMiningSchema(Label label){
		MiningSchema miningSchema = new MiningSchema();

		if(label != null){
			List<ScalarLabel> scalarLabels = label.toScalarLabels();

			for(ScalarLabel scalarLabel : scalarLabels){

				if(scalarLabel.isAnonymous()){
					continue;
				}

				MiningField miningField = createMiningField(scalarLabel.getName(), MiningField.UsageType.TARGET);

				miningSchema.addMiningFields(miningField);
			}
		}

		return miningSchema;
	}

	static
	public MiningField createMiningField(String name){
		return createMiningField(name, null);
	}

	static
	public MiningField createMiningField(String name, MiningField.UsageType usageType){
		MiningField miningField = new MiningField(name)
			.setUsageType(usageType);

		return miningField;
	}

	static
	public Targets createRescaleTargets(Number slope, Number intercept, ContinuousLabel continuousLabel){
		Target target = createRescaleTarget(slope, intercept, continuousLabel);

		if(target == null){
			return null;
		}

		Targets targets = new Targets()
			.addTargets(target);

		return targets;
	}

	static
	public Targets createRescaleTargets(List<? extends Number> slopes, List<? extends Number> intercepts, Label label){
		List<ContinuousLabel> continuousLabels = label.toScalarLabels(ContinuousLabel.class);

		if(slopes != null && continuousLabels.size() != slopes.size()){
			throw new IllegalArgumentException();
		} // End if

		if(intercepts != null && continuousLabels.size() != intercepts.size()){
			throw new IllegalArgumentException();
		}

		Targets targets = new Targets();

		for(int i = 0; i < continuousLabels.size(); i++){
			ContinuousLabel continuousLabel = continuousLabels.get(i);

			Number slope = (slopes != null ? slopes.get(i) : null);
			Number intercept = (intercepts != null ? intercepts.get(i) : null);

			Target target = createRescaleTarget(slope, intercept, continuousLabel);
			if(target != null){
				targets.addTargets(target);
			}
		}

		if(!targets.hasTargets()){
			return null;
		}

		return targets;
	}

	static
	private Target createRescaleTarget(Number slope, Number intercept, ContinuousLabel continuousLabel){
		Target target = new Target()
			.setTargetField(continuousLabel.getName());

		boolean rescaled = false;

		if(slope != null && !ValueUtil.isOne(slope)){
			target.setRescaleFactor(slope);

			rescaled = true;
		} // End if

		if(intercept != null && !ValueUtil.isZeroLike(intercept)){
			target.setRescaleConstant(intercept);

			rescaled = true;
		} // End if

		if(!rescaled){
			return null;
		}

		return target;
	}

	static
	public Output ensureOutput(Model model){
		Output output = model.getOutput();

		if(output == null){
			output = new Output();

			model.setOutput(output);
		}

		return output;
	}

	static
	public Output createPredictedOutput(String name, OpType opType, DataType dataType, Transformation... transformations){
		Output output = new Output();

		OutputField outputField = createPredictedField(name, opType, dataType)
			.setFinalResult(false);

		output.addOutputFields(outputField);

		for(Transformation transformation : transformations){
			outputField = transformation.createOutputField(outputField);

			output.addOutputFields(outputField);
		}

		return output;
	}

	static
	public Output createPredictedOutput(String name, OpType opType, DataType dataType, MultiLabel multiLabel, Transformation... transformations){
		return createPredictedOutput(name, opType, dataType, multiLabel, multiLabel, transformations);
	}

	static
	public Output createPredictedOutput(String name, OpType opType, DataType dataType, MultiLabel multiLabel, MultiLabel localMultiLabel, Transformation... transformations){

		if(multiLabel.size() != localMultiLabel.size()){
			throw new IllegalArgumentException();
		}

		Output output = new Output();

		List<OutputField> outputFields = new ArrayList<>();

		for(int i = 0; i < multiLabel.size(); i++){
			ScalarLabel scalarLabel = multiLabel.getScalarLabel(i);
			ScalarLabel localScalarLabel = localMultiLabel.getScalarLabel(i);

			OutputField outputField = createPredictedField(FieldNameUtil.create(name, scalarLabel.getName()), opType, dataType)
				.setTargetField(localScalarLabel.getName())
				.setFinalResult(false);

			output.addOutputFields(outputField);

			outputFields.add(outputField);
		}

		for(Transformation transformation : transformations){
			outputFields = outputFields.stream()
				.map(transformation::createOutputField)
				.collect(Collectors.toList());

			(output.getOutputFields()).addAll(outputFields);
		}

		return output;
	}

	static
	public Output createProbabilityOutput(MathContext mathContext, DiscreteLabel discreteLabel){
		DataType dataType = DataType.DOUBLE;

		if(mathContext == MathContext.FLOAT){
			dataType = DataType.FLOAT;
		}

		return createProbabilityOutput(dataType, discreteLabel);
	}

	static
	public Output createProbabilityOutput(DataType dataType, DiscreteLabel discreteLabel){
		Output output = new Output();

		List<OutputField> outputFields = output.getOutputFields();
		outputFields.addAll(createProbabilityFields(dataType, discreteLabel.getValues()));

		return output;
	}

	static
	public Output createProbabilityOutput(MathContext mathContext, MultiLabel multiLabel){
		DataType dataType = DataType.DOUBLE;

		if(mathContext == MathContext.FLOAT){
			dataType = DataType.FLOAT;
		}

		return createProbabilityOutput(dataType, multiLabel);
	}

	static
	public Output createProbabilityOutput(DataType dataType, MultiLabel multiLabel){
		Output output = new Output();

		List<OutputField> outputFields = output.getOutputFields();
		outputFields.addAll(createProbabilityFields(dataType, multiLabel));

		return output;
	}

	static
	public Output createNeighborOutput(int count){
		Output output = new Output();

		List<OutputField> outputFields = output.getOutputFields();
		outputFields.addAll(createNeighborFields(count));

		return output;
	}

	static
	public OutputField createAffinityField(DataType dataType, Object value){
		return createAffinityField(FieldNameUtil.create(FieldNames.AFFINITY, value), dataType, value);
	}

	static
	public OutputField createAffinityField(String name, DataType dataType, Object value){
		OutputField outputField = new OutputField(name, OpType.CONTINUOUS, dataType)
			.setResultFeature(ResultFeature.AFFINITY)
			.setValue(value);

		return outputField;
	}

	static
	public List<OutputField> createAffinityFields(DataType dataType, List<? extends Entity<?>> entities){
		return entities.stream()
			.map(entity -> createAffinityField(dataType, entity.getId()))
			.collect(Collectors.toList());
	}

	static
	public OutputField createEntityIdField(String name, DataType dataType){
		OutputField outputField = new OutputField(name, OpType.CATEGORICAL, dataType)
			.setResultFeature(ResultFeature.ENTITY_ID);

		return outputField;
	}

	static
	public OutputField createEntityIdField(String name, DataType dataType, List<?> values){
		OutputField outputField = createEntityIdField(name, dataType);

		if(values != null && !values.isEmpty()){
			FieldUtil.addValues(outputField, values);
		}

		return outputField;
	}

	static
	public OutputField createPredictedField(String name, OpType opType, DataType dataType){
		OutputField outputField = new OutputField(name, opType, dataType)
			.setResultFeature(ResultFeature.PREDICTED_VALUE);

		return outputField;
	}

	static
	public OutputField createPredictedField(String name, OpType opType, DataType dataType, List<?> values){
		OutputField outputField = createPredictedField(name, opType, dataType);

		if(values != null && !values.isEmpty()){
			FieldUtil.addValues(outputField, values);
		}

		return outputField;
	}

	static
	public OutputField createProbabilityField(DataType dataType, Object value){
		return createProbabilityField(FieldNameUtil.create(FieldNames.PROBABILITY, value), dataType, value);
	}

	static
	public OutputField createProbabilityField(DataType dataType, String targetField, Object value){
		return createProbabilityField(FieldNameUtil.create(FieldNames.PROBABILITY, targetField, value), dataType, value)
			.setTargetField(targetField);
	}

	static
	public OutputField createProbabilityField(String name, DataType dataType, Object value){
		OutputField outputField = new OutputField(name, OpType.CONTINUOUS, dataType)
			.setResultFeature(ResultFeature.PROBABILITY)
			.setValue(value);

		return outputField;
	}

	static
	public List<OutputField> createProbabilityFields(DataType dataType, List<?> values){
		return values.stream()
			.map(value -> createProbabilityField(dataType, value))
			.collect(Collectors.toList());
	}

	static
	public List<OutputField> createProbabilityFields(DataType dataType, MultiLabel multiLabel){
		List<DiscreteLabel> discreteLabels = multiLabel.toScalarLabels(DiscreteLabel.class);

		List<OutputField> result = new ArrayList<>();

		for(DiscreteLabel discreteLabel : discreteLabels){
			String targetField = discreteLabel.getName();

			List<?> values = discreteLabel.getValues();
			for(Object value : values){
				result.add(createProbabilityField(dataType, targetField, value));
			}
		}

		return result;
	}

	static
	public OutputField createNeighborField(DataType dataType, int rank){
		OutputField outputField = new OutputField(FieldNameUtil.create(FieldNames.NEIGHBOR, rank), OpType.CATEGORICAL, dataType)
			.setResultFeature(ResultFeature.ENTITY_ID)
			.setRank(rank);

		return outputField;
	}

	static
	public List<OutputField> createNeighborFields(int count){
		List<OutputField> result = new ArrayList<>();

		for(int i = 0; i < count; i++){
			result.add(createNeighborField(DataType.STRING, (i + 1)));
		}

		return result;
	}

	static
	public ModelStats ensureModelStats(Model model){
		ModelStats modelStats = model.getModelStats();

		if(modelStats == null){
			modelStats = new ModelStats();

			model.setModelStats(modelStats);
		}

		return modelStats;
	}

	static
	public VerificationField createVerificationField(String name){
		String tagName = name;

		// Replace "function(arg)" with "function_arg"
		Matcher matcher = ModelUtil.FUNCTION_INVOCATION.matcher(tagName);
		if(matcher.matches()){
			tagName = (matcher.group(1) + "_" + matcher.group(2));
		}

		String column = NamespacePrefixes.JPMML_INLINETABLE + ":" + XMLUtil.createTagName(tagName);

		VerificationField verificationField = new VerificationField(name, column);

		return verificationField;
	}

	static
	public ModelVerification createModelVerification(Map<VerificationField, List<?>> data){
		VerificationFields verificationFields = new VerificationFields()
			.addVerificationFields(Iterables.toArray(data.keySet(), VerificationField.class));

		InlineTable inlineTable = PMMLUtil.createInlineTable(VerificationField::getColumn, data);

		List<Row> rows = inlineTable.getRows();

		ModelVerification modelVerification = new ModelVerification(verificationFields, inlineTable)
			.setRecordCount(rows.size());

		return modelVerification;
	}

	private static final Pattern FUNCTION_INVOCATION = Pattern.compile("^(.+)\\((.+)\\)$");
}

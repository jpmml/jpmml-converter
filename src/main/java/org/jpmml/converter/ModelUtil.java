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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.Iterables;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Entity;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.InlineTable;
import org.dmg.pmml.MathContext;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Model;
import org.dmg.pmml.ModelStats;
import org.dmg.pmml.ModelVerification;
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
		return (MathContext.DOUBLE).equals(mathContext) ? null : mathContext;
	}

	static
	public MiningSchema createMiningSchema(Label label){
		MiningSchema miningSchema = new MiningSchema();

		if(label != null){
			FieldName name = label.getName();

			if(name != null){
				MiningField miningField = createMiningField(name, MiningField.UsageType.TARGET);

				miningSchema.addMiningFields(miningField);
			}
		}

		return miningSchema;
	}

	static
	public MiningField createMiningField(FieldName name){
		return createMiningField(name, null);
	}

	static
	public MiningField createMiningField(FieldName name, MiningField.UsageType usageType){
		MiningField miningField = new MiningField(name)
			.setUsageType(usageType);

		return miningField;
	}

	static
	public Targets createRescaleTargets(Number slope, Number intercept, ContinuousLabel continuousLabel){
		FieldName name = continuousLabel.getName();

		Target target = new Target()
			.setField(name);

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

		Targets targets = new Targets()
			.addTargets(target);

		return targets;
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
	public Output createPredictedOutput(FieldName name, OpType opType, DataType dataType, Transformation... transformations){
		Output output = new Output();

		OutputField outputField = new OutputField(name, opType, dataType)
			.setResultFeature(ResultFeature.PREDICTED_VALUE)
			.setFinalResult(false);

		output.addOutputFields(outputField);

		for(Transformation transformation : transformations){
			outputField = new OutputField(transformation.getName(outputField.getName()), transformation.getOpType(outputField.getOpType()), transformation.getDataType(outputField.getDataType()))
				.setResultFeature(ResultFeature.TRANSFORMED_VALUE)
				.setFinalResult(transformation.isFinalResult())
				.setExpression(transformation.createExpression(new FieldRef(outputField.getName())));

			output.addOutputFields(outputField);
		}

		return output;
	}

	static
	public Output createProbabilityOutput(MathContext mathContext, CategoricalLabel categoricalLabel){
		DataType dataType = DataType.DOUBLE;

		if((MathContext.FLOAT).equals(mathContext)){
			dataType = DataType.FLOAT;
		}

		return createProbabilityOutput(dataType, categoricalLabel);
	}

	static
	public Output createProbabilityOutput(DataType dataType, CategoricalLabel categoricalLabel){
		Output output = new Output();

		List<OutputField> outputFields = output.getOutputFields();
		outputFields.addAll(createProbabilityFields(dataType, categoricalLabel.getValues()));

		return output;
	}

	static
	public OutputField createAffinityField(DataType dataType, Object value){
		return createAffinityField(FieldNameUtil.create("affinity", value), dataType, value);
	}

	static
	public OutputField createAffinityField(FieldName name, DataType dataType, Object value){
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
	public OutputField createEntityIdField(FieldName name){
		OutputField outputField = new OutputField(name, OpType.CATEGORICAL, DataType.STRING)
			.setResultFeature(ResultFeature.ENTITY_ID);

		return outputField;
	}

	static
	public OutputField createPredictedField(FieldName name, OpType opType, DataType dataType){
		OutputField outputField = new OutputField(name, opType, dataType)
			.setResultFeature(ResultFeature.PREDICTED_VALUE);

		return outputField;
	}

	static
	public OutputField createProbabilityField(DataType dataType, Object value){
		return createProbabilityField(FieldNameUtil.create("probability", value), dataType, value);
	}

	static
	public OutputField createProbabilityField(FieldName name, DataType dataType, Object value){
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
	public ModelStats ensureModelStats(Model model){
		ModelStats modelStats = model.getModelStats();

		if(modelStats == null){
			modelStats = new ModelStats();

			model.setModelStats(modelStats);
		}

		return modelStats;
	}

	static
	public VerificationField createVerificationField(FieldName name){
		String tagName = name.getValue();

		// Replace "function(arg)" with "function_arg"
		Matcher matcher = ModelUtil.FUNCTION_INVOCATION.matcher(tagName);
		if(matcher.matches()){
			tagName = (matcher.group(1) + "_" + matcher.group(2));
		}

		VerificationField verificationField = new VerificationField()
			.setField(name)
			.setColumn("data:" + XMLUtil.createTagName(tagName));

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

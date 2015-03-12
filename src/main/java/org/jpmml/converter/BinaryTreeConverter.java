/*
 * Copyright (c) 2015 Villu Ruusmann
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.Node;
import org.dmg.pmml.Output;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.ScoreDistribution;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.TreeModel;
import org.dmg.pmml.True;
import org.dmg.pmml.Value;
import rexp.Rexp;
import rexp.Rexp.REXP.RBOOLEAN;
import rexp.Rexp.STRING;

public class BinaryTreeConverter extends Converter {

	private MiningFunctionType miningFunction = null;

	private List<DataField> dataFields = Lists.newArrayList();


	@Override
	public PMML convert(Rexp.REXP binaryTree){
		Rexp.REXP responses = REXPUtil.field(binaryTree, "responses");
		Rexp.REXP tree = REXPUtil.field(binaryTree, "tree");

		initTargetField(responses);

		Output output = encodeOutput();

		TreeModel treeModel = encodeTreeModel(tree)
			.withOutput(output);

		Collections.sort(this.dataFields.subList(1, this.dataFields.size()), new DataFieldComparator());

		DataDictionary dataDictionary = new DataDictionary()
			.withDataFields(this.dataFields);

		PMML pmml = new PMML("4.2", PMMLUtil.createHeader(), dataDictionary)
			.withModels(treeModel);

		return pmml;
	}

	private void initTargetField(Rexp.REXP responses){
		Rexp.REXP variables = REXPUtil.field(responses, "variables");
		Rexp.REXP is_nominal = REXPUtil.field(responses, "is_nominal");
		Rexp.REXP levels = REXPUtil.field(responses, "levels");

		Rexp.REXP names = REXPUtil.attribute(variables, "names");
		if(names.getStringValueCount() > 1){
			throw new IllegalArgumentException();
		}

		STRING name = names.getStringValue(0);

		DataField dataField;

		RBOOLEAN categorical = REXPUtil.booleanField(is_nominal, name.getStrval());

		if((RBOOLEAN.T).equals(categorical)){
			this.miningFunction = MiningFunctionType.CLASSIFICATION;

			Rexp.REXP target = REXPUtil.field(variables, name.getStrval());

			Rexp.REXP targetClass = REXPUtil.attribute(target, "class");

			STRING targetClassName = targetClass.getStringValue(0);

			dataField = PMMLUtil.createDataField(FieldName.create(name.getStrval()), targetClassName.getStrval());

			Rexp.REXP targetLevels = REXPUtil.field(levels, name.getStrval());

			List<Value> values = PMMLUtil.createValues(REXPUtil.getStringList(targetLevels));

			dataField = dataField.withValues(values);
		} else

		if((RBOOLEAN.F).equals(categorical)){
			this.miningFunction = MiningFunctionType.REGRESSION;

			dataField = PMMLUtil.createDataField(FieldName.create(name.getStrval()), false);
		} else

		{
			throw new IllegalArgumentException();
		}

		this.dataFields.add(dataField);
	}

	private DataField getDataField(FieldName name, DataType dataType, List<Value> values){

		for(int i = 1; i < this.dataFields.size(); i++){
			DataField dataField = this.dataFields.get(i);

			if((dataField.getName()).equals(name)){
				return dataField;
			}
		}

		DataField dataField = new DataField()
			.withName(name)
			.withValues(values);

		dataField = PMMLUtil.refineDataField(dataField, dataType);

		this.dataFields.add(dataField);

		return dataField;
	}

	private TreeModel encodeTreeModel(Rexp.REXP tree){
		Node root = new Node()
			.withPredicate(new True());

		encodeNode(root, tree);

		DataField dataField = this.dataFields.get(0);

		FieldCollector fieldCollector = new TreeModelFieldCollector();
		fieldCollector.applyTo(root);

		MiningSchema miningSchema = new MiningSchema()
			.withMiningFields(PMMLUtil.createMiningField(dataField.getName(), FieldUsageType.TARGET))
			.withMiningFields(PMMLUtil.createMiningFields(fieldCollector));

		TreeModel treeModel = new TreeModel(this.miningFunction, miningSchema, root)
			.withSplitCharacteristic(TreeModel.SplitCharacteristic.BINARY_SPLIT);

		return treeModel;
	}

	private void encodeNode(Node node, Rexp.REXP tree){
		Rexp.REXP nodeId = REXPUtil.field(tree, "nodeID");
		Rexp.REXP terminal = REXPUtil.field(tree, "terminal");
		Rexp.REXP psplit = REXPUtil.field(tree, "psplit");
		Rexp.REXP ssplits = REXPUtil.field(tree, "ssplits");
		Rexp.REXP prediction = REXPUtil.field(tree, "prediction");
		Rexp.REXP left = REXPUtil.field(tree, "left");
		Rexp.REXP right = REXPUtil.field(tree, "right");

		node = node.withId(String.valueOf(nodeId.getIntValue(0)));

		if((RBOOLEAN.T).equals(terminal.getBooleanValue(0))){
			node = encodeScore(node, prediction);

			return;
		}

		List<Predicate> predicates = encodeSplit(psplit, ssplits);

		Node leftChild = new Node()
			.withPredicate(predicates.get(0));

		encodeNode(leftChild, left);

		Node rightChild = new Node()
			.withPredicate(predicates.get(1));

		encodeNode(rightChild, right);

		node = node.withNodes(leftChild, rightChild);
	}

	private List<Predicate> encodeSplit(Rexp.REXP split, Rexp.REXP ssplits){
		Rexp.REXP splitpoint = REXPUtil.field(split, "splitpoint");
		Rexp.REXP variableName = REXPUtil.field(split, "variableName");

		if(ssplits.getRexpValueCount() > 0){
			throw new IllegalArgumentException();
		}

		STRING name = variableName.getStringValue(0);

		FieldName field = FieldName.create(name.getStrval());

		if(splitpoint.getRealValueCount() == 1){
			DataField dataField = getDataField(field, DataType.DOUBLE, null);

			return encodeContinuousSplit(dataField, splitpoint.getRealValue(0));
		} // End if

		if(splitpoint.getIntValueCount() > 0){
			Rexp.REXP levels = REXPUtil.attribute(splitpoint, "levels");

			List<Value> values = PMMLUtil.createValues(REXPUtil.getStringList(levels));

			DataField dataField = getDataField(field, DataType.STRING, values);

			return encodeCategoricalSplit(dataField, splitpoint.getIntValueList(), values);
		}

		throw new IllegalArgumentException();
	}

	private List<Predicate> encodeContinuousSplit(DataField dataField, Double split){
		String value = PMMLUtil.formatValue(split);

		Predicate leftPredicate = new SimplePredicate()
			.withField(dataField.getName())
			.withOperator(SimplePredicate.Operator.LESS_OR_EQUAL)
			.withValue(value);

		Predicate rightPredicate = new SimplePredicate()
			.withField(dataField.getName())
			.withOperator(SimplePredicate.Operator.GREATER_THAN)
			.withValue(value);

		return Arrays.asList(leftPredicate, rightPredicate);
	}

	private List<Predicate> encodeCategoricalSplit(DataField dataField, List<Integer> splits, List<Value> values){
		List<Value> leftValues = Lists.newArrayList();
		List<Value> rightValues = Lists.newArrayList();

		if(splits.size() != values.size()){
			throw new IllegalArgumentException();
		}

		for(int i = 0; i < splits.size(); i++){
			Integer split = splits.get(i);
			Value value = values.get(i);

			if(split == 1){
				leftValues.add(value);
			} else

			{
				rightValues.add(value);
			}
		}

		Predicate leftPredicate = new SimpleSetPredicate()
			.withField(dataField.getName())
			.withBooleanOperator(SimpleSetPredicate.BooleanOperator.IS_IN)
			.withArray(PMMLUtil.createArray(dataField.getDataType(), leftValues));

		Predicate rightPredicate = new SimpleSetPredicate()
			.withField(dataField.getName())
			.withBooleanOperator(SimpleSetPredicate.BooleanOperator.IS_IN)
			.withArray(PMMLUtil.createArray(dataField.getDataType(), rightValues));

		return Arrays.asList(leftPredicate, rightPredicate);
	}

	private Node encodeScore(Node node, Rexp.REXP probabilities){

		switch(this.miningFunction){
			case CLASSIFICATION:
				return encodeClassificationScore(node, probabilities);
			case REGRESSION:
				return encodeRegressionScore(node, probabilities);
			default:
				throw new IllegalArgumentException();
		}
	}

	private Node encodeClassificationScore(Node node, Rexp.REXP probabilities){
		DataField dataField = this.dataFields.get(0);

		List<Value> values = dataField.getValues();

		if(probabilities.getRealValueCount() != values.size()){
			throw new IllegalArgumentException();
		}

		List<ScoreDistribution> scoreDistributions = node.getScoreDistributions();

		Double maxProbability = null;

		for(int i = 0; i < values.size(); i++){
			Value value = values.get(i);

			Double probability = probabilities.getRealValue(i);

			if(maxProbability == null || maxProbability.compareTo(probability) < 0){
				node = node.withScore(value.getValue());

				maxProbability = probability;
			}

			ScoreDistribution scoreDistribution = new ScoreDistribution(value.getValue(), probability);

			scoreDistributions.add(scoreDistribution);
		}

		return node;
	}

	private Node encodeRegressionScore(Node node, Rexp.REXP probabilities){

		if(probabilities.getRealValueCount() != 1){
			throw new IllegalArgumentException();
		}

		Double probability = probabilities.getRealValue(0);

		node = node.withScore(PMMLUtil.formatValue(probability));

		return node;
	}

	private Output encodeOutput(){

		switch(this.miningFunction){
			case CLASSIFICATION:
				return encodeClassificationOutput();
			default:
				return null;
		}
	}

	private Output encodeClassificationOutput(){
		DataField dataField = this.dataFields.get(0);

		Output output = new Output()
			.withOutputFields(PMMLUtil.createProbabilityFields(dataField))
			.withOutputFields(PMMLUtil.createEntityIdField(FieldName.create("nodeId")));

		return output;
	}
}
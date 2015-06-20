/*
 * Copyright (c) 2014 Villu Ruusmann
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

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.Node;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.Segment;
import org.dmg.pmml.Segmentation;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.TreeModel;
import org.dmg.pmml.True;
import org.dmg.pmml.Value;
import org.jpmml.rexp.REXPProtos;
import org.jpmml.rexp.REXPProtos.STRING;

public class RandomForestConverter extends Converter {

	private List<DataField> dataFields = Lists.newArrayList();

	private LoadingCache<ElementKey, Predicate> predicateCache = CacheBuilder.newBuilder()
		.build(new CacheLoader<ElementKey, Predicate>(){

			@Override
			public Predicate load(ElementKey key){
				Object[] content = key.getContent();

				return encodeCategoricalSplit((DataField)content[0], REXPUtil.asInteger((Number)content[1]), (Boolean)content[2]);
			}
		});


	public RandomForestConverter(){
	}

	@Override
	public PMML convert(REXPProtos.REXP randomForest){
		REXPProtos.REXP type = REXPUtil.field(randomForest, "type");
		REXPProtos.REXP forest = REXPUtil.field(randomForest, "forest");

		try {
			REXPProtos.REXP terms = REXPUtil.field(randomForest, "terms");

			// The RF model was trained using the formula interface
			initFormulaFields(terms);
		} catch(IllegalArgumentException iae){
			REXPProtos.REXP xlevels = REXPUtil.field(forest, "xlevels");

			REXPProtos.REXP xNames;

			try {
				xNames = REXPUtil.field(randomForest, "xNames");
			} catch(IllegalArgumentException iaeChild){
				xNames = REXPUtil.attribute(xlevels, "names");
			}

			REXPProtos.REXP ncat = REXPUtil.field(forest, "ncat");

			REXPProtos.REXP y;

			try {
				y = REXPUtil.field(randomForest, "y");
			} catch(IllegalArgumentException iaeChild){
				y = null;
			}

			// The RF model was trained using the matrix (ie. non-formula) interface
			initNonFormulaFields(xNames, ncat, y);
		}

		PMML pmml;

		STRING typeValue = type.getStringValue(0);

		if("regression".equals(typeValue.getStrval())){
			pmml = convertRegression(forest);
		} else

		if("classification".equals(typeValue.getStrval())){
			REXPProtos.REXP y = REXPUtil.field(randomForest, "y");

			pmml = convertClassification(forest, y);
		} else

		{
			throw new IllegalArgumentException();
		}

		return pmml;
	}

	private PMML convertRegression(REXPProtos.REXP forest){
		REXPProtos.REXP leftDaughter = REXPUtil.field(forest, "leftDaughter");
		REXPProtos.REXP rightDaughter = REXPUtil.field(forest, "rightDaughter");
		REXPProtos.REXP nodepred = REXPUtil.field(forest, "nodepred");
		REXPProtos.REXP bestvar = REXPUtil.field(forest, "bestvar");
		REXPProtos.REXP xbestsplit = REXPUtil.field(forest, "xbestsplit");
		REXPProtos.REXP ncat = REXPUtil.field(forest, "ncat");
		REXPProtos.REXP nrnodes = REXPUtil.field(forest, "nrnodes");
		REXPProtos.REXP ntree = REXPUtil.field(forest, "ntree");
		REXPProtos.REXP xlevels = REXPUtil.field(forest, "xlevels");

		initActiveFields(xlevels, ncat);

		ScoreEncoder<Double> scoreEncoder = new ScoreEncoder<Double>(){

			@Override
			public String encode(Double key){
				return PMMLUtil.formatValue(key);
			}
		};

		List<Integer> leftDaughterIndices = getIndices(leftDaughter);
		List<Integer> rightDaughterIndices = getIndices(rightDaughter);
		List<Integer> bestvarIndices = getIndices(bestvar);

		int rows = nrnodes.getIntValue(0);
		int columns = (int)ntree.getRealValue(0);

		List<TreeModel> treeModels = Lists.newArrayList();

		for(int i = 0; i < columns; i++){
			TreeModel treeModel = encodeTreeModel(
					MiningFunctionType.REGRESSION,
					REXPUtil.getColumn(leftDaughterIndices, i, rows, columns),
					REXPUtil.getColumn(rightDaughterIndices, i, rows, columns),
					scoreEncoder,
					REXPUtil.getColumn(nodepred.getRealValueList(), i, rows, columns),
					REXPUtil.getColumn(bestvarIndices, i, rows, columns),
					REXPUtil.getColumn(xbestsplit.getRealValueList(), i, rows, columns)
				);

			treeModels.add(treeModel);
		}

		return encodePMML(MiningFunctionType.REGRESSION, treeModels);
	}

	private PMML convertClassification(REXPProtos.REXP forest, REXPProtos.REXP y){
		REXPProtos.REXP bestvar = REXPUtil.field(forest, "bestvar");
		REXPProtos.REXP treemap = REXPUtil.field(forest, "treemap");
		REXPProtos.REXP nodepred = REXPUtil.field(forest, "nodepred");
		REXPProtos.REXP xbestsplit = REXPUtil.field(forest, "xbestsplit");
		REXPProtos.REXP ncat = REXPUtil.field(forest, "ncat");
		REXPProtos.REXP nrnodes = REXPUtil.field(forest, "nrnodes");
		REXPProtos.REXP ntree = REXPUtil.field(forest, "ntree");
		REXPProtos.REXP xlevels = REXPUtil.field(forest, "xlevels");

		initPredictedFields(y);
		initActiveFields(xlevels, ncat);

		ScoreEncoder<Integer> scoreEncoder = new ScoreEncoder<Integer>(){

			@Override
			public String encode(Integer key){
				Value value = getLevel(key.intValue() - 1);

				return value.getValue();
			}
		};

		List<Integer> treemapIndices = getIndices(treemap);
		List<Integer> nodepredIndices = getIndices(nodepred);
		List<Integer> bestvarIndices = getIndices(bestvar);

		int rows = nrnodes.getIntValue(0);
		int columns = (int)ntree.getRealValue(0);

		List<TreeModel> treeModels = Lists.newArrayList();

		for(int i = 0; i < columns; i++){
			List<Integer> daughters = REXPUtil.getColumn(treemapIndices, i, 2 * rows, columns);

			TreeModel treeModel = encodeTreeModel(
					MiningFunctionType.CLASSIFICATION,
					REXPUtil.getColumn(daughters, 0, rows, columns),
					REXPUtil.getColumn(daughters, 1, rows, columns),
					scoreEncoder,
					REXPUtil.getColumn(nodepredIndices, i, rows, columns),
					REXPUtil.getColumn(bestvarIndices, i, rows, columns),
					REXPUtil.getColumn(xbestsplit.getRealValueList(), i, rows, columns)
				);

			treeModels.add(treeModel);
		}

		return encodePMML(MiningFunctionType.CLASSIFICATION, treeModels);
	}

	private PMML encodePMML(MiningFunctionType miningFunction, List<TreeModel> treeModels){
		MultipleModelMethodType multipleModelMethod;

		switch(miningFunction){
			case REGRESSION:
				multipleModelMethod = MultipleModelMethodType.AVERAGE;
				break;
			case CLASSIFICATION:
				multipleModelMethod = MultipleModelMethodType.MAJORITY_VOTE;
				break;
			default:
				throw new IllegalArgumentException();
		}

		List<Segment> segments = Lists.newArrayList();

		for(int i = 0; i < treeModels.size(); i++){
			TreeModel treeModel = treeModels.get(i);

			Segment segment = new Segment()
				.setId(String.valueOf(i + 1))
				.setPredicate(new True())
				.setModel(treeModel);

			segments.add(segment);
		}

		Segmentation segmentation = new Segmentation(multipleModelMethod, segments);

		FieldTypeAnalyzer fieldTypeAnalyzer = new RandomForestFieldTypeAnalyzer();
		fieldTypeAnalyzer.applyTo(segmentation);

		PMMLUtil.refineDataFields(this.dataFields, fieldTypeAnalyzer);

		MiningSchema miningSchema = PMMLUtil.createMiningSchema(this.dataFields);

		Output output = encodeOutput(miningFunction);

		MiningModel miningModel = new MiningModel(miningFunction, miningSchema)
			.setSegmentation(segmentation)
			.setOutput(output);

		DataDictionary dataDictionary = new DataDictionary(this.dataFields);

		PMML pmml = new PMML("4.2", PMMLUtil.createHeader(), dataDictionary)
			.addModels(miningModel);

		return pmml;
	}

	private void initFormulaFields(REXPProtos.REXP terms){
		REXPProtos.REXP dataClasses = REXPUtil.attribute(terms, "dataClasses");

		REXPProtos.REXP names = REXPUtil.attribute(dataClasses, "names");

		for(int i = 0; i < names.getStringValueCount(); i++){
			STRING name = names.getStringValue(i);

			STRING dataClass = dataClasses.getStringValue(i);

			DataField dataField = PMMLUtil.createDataField(FieldName.create(name.getStrval()), dataClass.getStrval());

			this.dataFields.add(dataField);
		}
	}

	private void initNonFormulaFields(REXPProtos.REXP xNames, REXPProtos.REXP ncat, REXPProtos.REXP y){

		// Dependent variable
		{
			boolean categorical = (y != null && y.getStringValueCount() > 0);

			DataField dataField = PMMLUtil.createDataField(FieldName.create("_target"), categorical);

			this.dataFields.add(dataField);
		}

		// Independent variable(s)
		for(int i = 0; i < xNames.getStringValueCount(); i++){
			STRING xName = xNames.getStringValue(i);

			boolean categorical;

			if(ncat.getIntValueCount() > 0){
				categorical = (ncat.getIntValue(i) > 1);
			} else

			if(ncat.getRealValueCount() > 0){
				categorical = (ncat.getRealValue(i) > 1d);
			} else

			{
				throw new IllegalArgumentException();
			}

			DataField dataField = PMMLUtil.createDataField(FieldName.create(xName.getStrval()), categorical);

			this.dataFields.add(dataField);
		}
	}

	private void initActiveFields(REXPProtos.REXP xlevels, REXPProtos.REXP ncat){

		for(int i = 0; i < ncat.getIntValueCount(); i++){
			DataField dataField = this.dataFields.get(i + 1);

			boolean categorical = (ncat.getIntValue(i) > 1);
			if(!categorical){
				continue;
			}

			REXPProtos.REXP xvalues = xlevels.getRexpValue(i);

			List<Value> values = dataField.getValues();
			values.addAll(PMMLUtil.createValues(REXPUtil.getStringList(xvalues)));

			dataField = PMMLUtil.refineDataField(dataField);
		}
	}

	private void initPredictedFields(REXPProtos.REXP y){
		DataField dataField = this.dataFields.get(0);

		REXPProtos.REXP levels = REXPUtil.attribute(y, "levels");

		List<Value> values = dataField.getValues();
		values.addAll(PMMLUtil.createValues(REXPUtil.getStringList(levels)));

		dataField = PMMLUtil.refineDataField(dataField);
	}

	private <P extends Number> TreeModel encodeTreeModel(MiningFunctionType miningFunction, List<Integer> leftDaughter, List<Integer> rightDaughter, ScoreEncoder<P> scoreEncoder, List<P> nodepred, List<Integer> bestvar, List<Double> xbestsplit){
		Node root = new Node()
			.setId("1")
			.setPredicate(new True());

		encodeNode(root, 0, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

		FieldCollector fieldCollector = new TreeModelFieldCollector();
		fieldCollector.applyTo(root);

		MiningSchema miningSchema = PMMLUtil.createMiningSchema(fieldCollector);

		TreeModel treeModel = new TreeModel(miningFunction, miningSchema, root)
			.setSplitCharacteristic(TreeModel.SplitCharacteristic.BINARY_SPLIT);

		return treeModel;
	}

	private <P extends Number> void encodeNode(Node node, int i, List<Integer> leftDaughter, List<Integer> rightDaughter, List<Integer> bestvar, List<Double> xbestsplit, ScoreEncoder<P> scoreEncoder, List<P> nodepred){
		Predicate leftPredicate = null;
		Predicate rightPredicate = null;

		Integer var = bestvar.get(i);
		if(var != 0){
			DataField dataField = this.dataFields.get(var);

			Double split = xbestsplit.get(i);

			OpType opType = dataField.getOpType();

			DataType dataType = dataField.getDataType();
			switch(dataType){
				case BOOLEAN:
					opType = OpType.CONTINUOUS;
					break;
				default:
					break;
			}

			switch(opType){
				case CATEGORICAL:
					leftPredicate = this.predicateCache.getUnchecked(new ElementKey(dataField, split, Boolean.TRUE));
					rightPredicate = this.predicateCache.getUnchecked(new ElementKey(dataField, split, Boolean.FALSE));
					break;
				case CONTINUOUS:
					leftPredicate = encodeContinuousSplit(dataField, split, true);
					rightPredicate = encodeContinuousSplit(dataField, split, false);
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else

		{
			P prediction = nodepred.get(i);

			node.setScore(scoreEncoder.encode(prediction));
		}

		Integer left = leftDaughter.get(i);
		if(left != 0){
			Node leftChild = new Node()
				.setId(String.valueOf(left))
				.setPredicate(leftPredicate);

			encodeNode(leftChild, left - 1, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

			node.addNodes(leftChild);
		}

		Integer right = rightDaughter.get(i);
		if(right != 0){
			Node rightChild = new Node()
				.setId(String.valueOf(right))
				.setPredicate(rightPredicate);

			encodeNode(rightChild, right - 1, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

			node.addNodes(rightChild);
		}
	}

	private Predicate encodeCategoricalSplit(DataField dataField, Integer split, boolean left){
		List<Value> values = selectValues(dataField.getValues(), split, left);

		if(values.size() == 1){
			Value value = values.get(0);

			SimplePredicate simplePredicate = new SimplePredicate()
				.setField(dataField.getName())
				.setOperator(SimplePredicate.Operator.EQUAL)
				.setValue(value.getValue());

			return simplePredicate;
		}

		SimpleSetPredicate simpleSetPredicate = new SimpleSetPredicate()
			.setField(dataField.getName())
			.setBooleanOperator(SimpleSetPredicate.BooleanOperator.IS_IN)
			.setArray(PMMLUtil.createArray(dataField.getDataType(), values));

		return simpleSetPredicate;
	}

	private Predicate encodeContinuousSplit(DataField dataField, Double split, boolean left){
		SimplePredicate simplePredicate;

		DataType dataType = dataField.getDataType();

		if((DataType.DOUBLE).equals(dataType)){
			simplePredicate = new SimplePredicate()
				.setField(dataField.getName())
				.setOperator(left ? SimplePredicate.Operator.LESS_OR_EQUAL : SimplePredicate.Operator.GREATER_THAN)
				.setValue(PMMLUtil.formatValue(split));
		} else

		if((DataType.BOOLEAN).equals(dataType)){
			simplePredicate = new SimplePredicate()
				.setField(dataField.getName())
				.setOperator(SimplePredicate.Operator.EQUAL)
				.setValue(split.doubleValue() <= 0.5d ? Boolean.toString(!left) : Boolean.toString(left));
		} else

		{
			throw new IllegalArgumentException();
		}

		return simplePredicate;
	}

	private Output encodeOutput(MiningFunctionType miningFunction){

		switch(miningFunction){
			case CLASSIFICATION:
				return encodeClassificationOutput();
			default:
				return null;
		}
	}

	private Output encodeClassificationOutput(){
		DataField dataField = this.dataFields.get(0);

		Output output = new Output(PMMLUtil.createProbabilityFields(dataField));

		return output;
	}

	private Value getLevel(int i){
		DataField dataField = this.dataFields.get(0);

		List<Value> values = dataField.getValues();

		return values.get(i);
	}

	static
	private List<Value> selectValues(List<Value> values, Integer split, boolean left){
		List<Value> result = Lists.newArrayList();

		String string = performBinaryExpansion(split);

		for(int i = 0; i < values.size(); i++){
			Value value = values.get(i);

			boolean append;

			// Send "true" categories to the left
			if(left){
				append = ((i < string.length()) && (string.charAt(i) == '1'));
			} else

			// Send all other categories to the right
			{
				append = ((i >= string.length()) || (string.charAt(i) == '0'));
			} // End if

			if(append){
				result.add(value);
			}
		}

		return result;
	}

	static
	private String performBinaryExpansion(int value){

		if(value <= 0){
			throw new IllegalArgumentException();
		}

		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toBinaryString(value));

		// Start counting from the rightmost bit
		sb = sb.reverse();

		return sb.toString();
	}

	static
	private List<Integer> getIndices(REXPProtos.REXP rexp){
		List<Integer> intValues = rexp.getIntValueList();
		if(intValues.size() > 0){
			return intValues;
		}

		List<Double> realValues = rexp.getRealValueList();
		if(realValues.size() > 0){
			Function<Number, Integer> function = new Function<Number, Integer>(){

				@Override
				public Integer apply(Number number){
					return REXPUtil.asInteger(number);
				}
			};

			return Lists.transform(realValues, function);
		}

		throw new IllegalArgumentException();
	}

	static
	private interface ScoreEncoder<K extends Number> {

		String encode(K key);
	}
}
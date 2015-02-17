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
import org.dmg.pmml.Array;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.Header;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningFunctionType;
import org.dmg.pmml.MiningModel;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.MultipleModelMethodType;
import org.dmg.pmml.Node;
import org.dmg.pmml.OpType;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.Segment;
import org.dmg.pmml.Segmentation;
import org.dmg.pmml.SimplePredicate;
import org.dmg.pmml.SimpleSetPredicate;
import org.dmg.pmml.TreeModel;
import org.dmg.pmml.True;
import org.dmg.pmml.Value;
import rexp.Rexp;
import rexp.Rexp.STRING;

public class RandomForestConverter extends Converter {

	private List<DataField> dataFields = Lists.newArrayList();

	private LoadingCache<ElementKey, SimpleSetPredicate> predicateCache = CacheBuilder.newBuilder()
		.build(new CacheLoader<ElementKey, SimpleSetPredicate>(){

			@Override
			public SimpleSetPredicate load(ElementKey key){
				Object[] content = key.getContent();

				return encodeSimpleSetPredicate((DataField)content[0], REXPUtil.asInteger((Number)content[1]), (Boolean)content[2]);
			}
		});


	public RandomForestConverter(){
	}

	@Override
	public PMML convert(Rexp.REXP randomForest){
		Rexp.REXP type = REXPUtil.field(randomForest, "type");
		Rexp.REXP forest = REXPUtil.field(randomForest, "forest");

		try {
			Rexp.REXP terms = REXPUtil.field(randomForest, "terms");

			// The RF model was trained using the formula interface
			initFormulaFields(terms);
		} catch(IllegalArgumentException iae){
			Rexp.REXP xlevels = REXPUtil.field(forest, "xlevels");

			Rexp.REXP xNames;

			try {
				xNames = REXPUtil.field(randomForest, "xNames");
			} catch(IllegalArgumentException iaeChild){
				xNames = REXPUtil.attribute(xlevels, "names");
			}

			Rexp.REXP ncat = REXPUtil.field(forest, "ncat");

			Rexp.REXP y;

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
			Rexp.REXP y = REXPUtil.field(randomForest, "y");

			pmml = convertClassification(forest, y);
		} else

		{
			throw new IllegalArgumentException();
		}

		return pmml;
	}

	private PMML convertRegression(Rexp.REXP forest){
		Rexp.REXP leftDaughter = REXPUtil.field(forest, "leftDaughter");
		Rexp.REXP rightDaughter = REXPUtil.field(forest, "rightDaughter");
		Rexp.REXP nodepred = REXPUtil.field(forest, "nodepred");
		Rexp.REXP bestvar = REXPUtil.field(forest, "bestvar");
		Rexp.REXP xbestsplit = REXPUtil.field(forest, "xbestsplit");
		Rexp.REXP ncat = REXPUtil.field(forest, "ncat");
		Rexp.REXP nrnodes = REXPUtil.field(forest, "nrnodes");
		Rexp.REXP ntree = REXPUtil.field(forest, "ntree");
		Rexp.REXP xlevels = REXPUtil.field(forest, "xlevels");

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

	private PMML convertClassification(Rexp.REXP forest, Rexp.REXP y){
		Rexp.REXP bestvar = REXPUtil.field(forest, "bestvar");
		Rexp.REXP treemap = REXPUtil.field(forest, "treemap");
		Rexp.REXP nodepred = REXPUtil.field(forest, "nodepred");
		Rexp.REXP xbestsplit = REXPUtil.field(forest, "xbestsplit");
		Rexp.REXP ncat = REXPUtil.field(forest, "ncat");
		Rexp.REXP nrnodes = REXPUtil.field(forest, "nrnodes");
		Rexp.REXP ntree = REXPUtil.field(forest, "ntree");
		Rexp.REXP xlevels = REXPUtil.field(forest, "xlevels");

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
				.withId(String.valueOf(i + 1))
				.withPredicate(new True())
				.withModel(treeModel);

			segments.add(segment);
		}

		Segmentation segmentation = new Segmentation(multipleModelMethod, segments);

		FieldTypeAnalyzer fieldTypeAnalyzer = new RandomForestFieldTypeAnalyzer();
		fieldTypeAnalyzer.applyTo(segmentation);

		PMMLUtil.refineDataFields(this.dataFields, fieldTypeAnalyzer);

		MiningSchema miningSchema = new MiningSchema();

		for(int i = 0; i < this.dataFields.size(); i++){
			DataField dataField = this.dataFields.get(i);

			MiningField miningField = new MiningField()
				.withName(dataField.getName())
				.withUsageType(i > 0 ? FieldUsageType.ACTIVE : FieldUsageType.TARGET);

			miningSchema = miningSchema.withMiningFields(miningField);
		}

		MiningModel miningModel = new MiningModel(miningFunction, miningSchema)
			.withSegmentation(segmentation);

		DataDictionary dataDictionary = new DataDictionary()
			.withDataFields(this.dataFields);

		PMML pmml = new PMML("4.2", new Header(), dataDictionary)
			.withModels(miningModel);

		return pmml;
	}

	private void initFormulaFields(Rexp.REXP terms){
		Rexp.REXP dataClasses = REXPUtil.attribute(terms, "dataClasses");

		Rexp.REXP names = REXPUtil.attribute(dataClasses, "names");

		for(int i = 0; i < names.getStringValueCount(); i++){
			STRING name = names.getStringValue(i);

			STRING dataClass = dataClasses.getStringValue(i);

			DataField dataField = PMMLUtil.createDataField(name.getStrval(), dataClass.getStrval());

			this.dataFields.add(dataField);
		}
	}

	private void initNonFormulaFields(Rexp.REXP xNames, Rexp.REXP ncat, Rexp.REXP y){

		// Dependent variable
		{
			boolean categorical = (y != null);

			DataField dataField = PMMLUtil.createDataField("_target", categorical);

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

			DataField dataField = PMMLUtil.createDataField(xName.getStrval(), categorical);

			this.dataFields.add(dataField);
		}
	}

	private void initActiveFields(Rexp.REXP xlevels, Rexp.REXP ncat){

		for(int i = 0; i < ncat.getIntValueCount(); i++){
			DataField dataField = this.dataFields.get(i + 1);

			boolean categorical = (ncat.getIntValue(i) > 1);
			if(!categorical){
				continue;
			}

			List<Value> values = dataField.getValues();

			Rexp.REXP xvalues = xlevels.getRexpValue(i);

			for(int j = 0; j < xvalues.getStringValueCount(); j++){
				STRING xvalue = xvalues.getStringValue(j);

				values.add(new Value(xvalue.getStrval()));
			}

			dataField = PMMLUtil.refineDataField(dataField);
		}
	}

	private void initPredictedFields(Rexp.REXP y){
		DataField dataField = this.dataFields.get(0);

		List<Value> values = dataField.getValues();

		Rexp.REXP levels = REXPUtil.attribute(y, "levels");

		for(int i = 0; i < levels.getStringValueCount(); i++){
			STRING level = levels.getStringValue(i);

			values.add(new Value(level.getStrval()));
		}

		dataField = PMMLUtil.refineDataField(dataField);
	}

	private <P extends Number> TreeModel encodeTreeModel(MiningFunctionType miningFunction, List<Integer> leftDaughter, List<Integer> rightDaughter, ScoreEncoder<P> scoreEncoder, List<P> nodepred, List<Integer> bestvar, List<Double> xbestsplit){
		Node root = new Node()
			.withId("1")
			.withPredicate(new True());

		encodeNode(root, 0, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

		FieldCollector fieldCollector = new TreeModelFieldCollector();
		fieldCollector.applyTo(root);

		List<MiningField> activeFields = PMMLUtil.createMiningFields(fieldCollector);

		MiningSchema miningSchema = new MiningSchema()
			.withMiningFields(activeFields);

		TreeModel treeModel = new TreeModel(miningFunction, miningSchema, root)
			.withSplitCharacteristic(TreeModel.SplitCharacteristic.BINARY_SPLIT);

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
					leftPredicate = encodeSimplePredicate(dataField, split, true);
					rightPredicate = encodeSimplePredicate(dataField, split, false);
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else

		{
			P prediction = nodepred.get(i);

			node = node.withScore(scoreEncoder.encode(prediction));
		}

		Integer left = leftDaughter.get(i);
		if(left != 0){
			Node leftChild = new Node()
				.withId(String.valueOf(left))
				.withPredicate(leftPredicate);

			encodeNode(leftChild, left - 1, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

			node = node.withNodes(leftChild);
		}

		Integer right = rightDaughter.get(i);
		if(right != 0){
			Node rightChild = new Node()
				.withId(String.valueOf(right))
				.withPredicate(rightPredicate);

			encodeNode(rightChild, right - 1, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

			node = node.withNodes(rightChild);
		}
	}

	private SimpleSetPredicate encodeSimpleSetPredicate(DataField dataField, Integer split, boolean left){
		SimpleSetPredicate simpleSetPredicate = new SimpleSetPredicate()
			.withField(dataField.getName())
			.withBooleanOperator(SimpleSetPredicate.BooleanOperator.IS_IN)
			.withArray(encodeArray(dataField, split, left));

		return simpleSetPredicate;
	}

	private Array encodeArray(DataField dataField, Integer split, boolean left){
		List<Value> values = selectValues(dataField.getValues(), split, left);

		return PMMLUtil.createArray(dataField.getDataType(), values);
	}

	private SimplePredicate encodeSimplePredicate(DataField dataField, Double split, boolean left){
		SimplePredicate simplePredicate;

		DataType dataType = dataField.getDataType();

		if((DataType.DOUBLE).equals(dataType)){
			simplePredicate = new SimplePredicate()
				.withField(dataField.getName())
				.withOperator(left ? SimplePredicate.Operator.LESS_OR_EQUAL : SimplePredicate.Operator.GREATER_THAN)
				.withValue(PMMLUtil.formatValue(split));
		} else

		if((DataType.BOOLEAN).equals(dataType)){
			simplePredicate = new SimplePredicate()
				.withField(dataField.getName())
				.withOperator(SimplePredicate.Operator.EQUAL)
				.withValue(split.doubleValue() <= 0.5d ? Boolean.toString(!left) : Boolean.toString(left));
		} else

		{
			throw new IllegalArgumentException();
		}

		return simplePredicate;
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
	private List<Integer> getIndices(Rexp.REXP rexp){
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
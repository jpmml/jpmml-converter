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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.dmg.pmml.AbstractVisitor;
import org.dmg.pmml.Array;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
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
import org.dmg.pmml.VisitorAction;
import rexp.Rexp;
import rexp.Rexp.STRING;

public class RandomForestConverter extends Converter {

	private List<DataField> dataFields = new ArrayList<DataField>();

	private LoadingCache<PredicateKey, SimpleSetPredicate> leftSimpleSetPredicates = CacheBuilder.newBuilder()
		.build(new CacheLoader<PredicateKey, SimpleSetPredicate>(){

			@Override
			public SimpleSetPredicate load(PredicateKey key){
				return encodeSimpleSetPredicate(key.getDataField(), asInteger(key.getSplit()), true);
			}
		});

	private LoadingCache<PredicateKey, SimpleSetPredicate> rightSimpleSetPredicates = CacheBuilder.newBuilder()
		.build(new CacheLoader<PredicateKey, SimpleSetPredicate>(){

			@Override
			public SimpleSetPredicate load(PredicateKey key){
				return encodeSimpleSetPredicate(key.getDataField(), asInteger(key.getSplit()), false);
			}
		});

	private LoadingCache<PredicateKey, SimplePredicate> leftSimplePredicates = CacheBuilder.newBuilder()
		.build(new CacheLoader<PredicateKey, SimplePredicate>(){

			@Override
			public SimplePredicate load(PredicateKey key){
				return encodeSimplePredicate(key.getDataField(), asDouble(key.getSplit()), true);
			}
		});

	private LoadingCache<PredicateKey, SimplePredicate> rightSimplePredicates = CacheBuilder.newBuilder()
		.build(new CacheLoader<PredicateKey, SimplePredicate>(){

			@Override
			public SimplePredicate load(PredicateKey key){
				return encodeSimplePredicate(key.getDataField(), asDouble(key.getSplit()), false);
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
			Rexp.REXP ncat = REXPUtil.field(forest, "ncat");

			Rexp.REXP y;

			try {
				y = REXPUtil.field(randomForest, "y");
			} catch(IllegalArgumentException iaeChild){
				y = null;
			}

			// The RF model was trained using the matrix (ie. non-formula) interface
			initNonFormulaFields(xlevels, ncat, y);
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

		FieldTypeAnalyzer fieldTypeAnalyzer = new FieldTypeAnalyzer();
		pmml.accept(fieldTypeAnalyzer);

		List<DataField> dataFields = this.dataFields;
		for(DataField dataField : dataFields){
			DataType dataType = fieldTypeAnalyzer.getDataType(dataField.getName());

			// An unused field
			if(dataType == null){
				continue;
			}

			dataField = initDataField(dataField, dataType);
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
				return formatValue(key);
			}
		};

		List<Integer> leftDaughterIndices = getIndices(leftDaughter);
		List<Integer> rightDaughterIndices = getIndices(rightDaughter);
		List<Integer> bestvarIndices = getIndices(bestvar);

		int rows = nrnodes.getIntValue(0);
		int columns = (int)ntree.getRealValue(0);

		List<TreeModel> treeModels = new ArrayList<TreeModel>();

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

		List<TreeModel> treeModels = new ArrayList<TreeModel>();

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

		Set<FieldName> forestFields = new LinkedHashSet<FieldName>();

		Segmentation segmentation = new Segmentation(multipleModelMethod);

		for(int i = 0; i < treeModels.size(); i++){
			TreeModel treeModel = treeModels.get(i);

			Node root = treeModel.getNode();

			FieldCollector fieldCollector = new FieldCollector();
			root.accept(fieldCollector);

			Set<FieldName> treeFields = fieldCollector.getFields();

			forestFields.addAll(treeFields);

			MiningSchema miningSchema = treeModel.getMiningSchema();
			miningSchema = miningSchema.withMiningFields(encodeMiningFields(treeFields));

			Segment segment = new Segment()
				.withId(String.valueOf(i + 1))
				.withPredicate(new True())
				.withModel(treeModel);

			segmentation = segmentation.withSegments(segment);
		}

		DataDictionary dataDictionary = encodeDataDictionary(forestFields);

		MiningSchema miningSchema = encodeMiningSchema(forestFields);

		MiningModel miningModel = new MiningModel(miningSchema, miningFunction)
			.withSegmentation(segmentation);

		PMML pmml = new PMML(new Header(), dataDictionary, "4.2")
			.withModels(miningModel);

		return pmml;
	}

	private void initFormulaFields(Rexp.REXP terms){
		Rexp.REXP dataClasses = REXPUtil.attribute(terms, "dataClasses");

		Rexp.REXP names = REXPUtil.attribute(dataClasses, "names");

		for(int i = 0; i < names.getStringValueCount(); i++){
			STRING name = names.getStringValue(i);

			DataField dataField = new DataField()
				.withName(FieldName.create(name.getStrval()));

			STRING dataClass = dataClasses.getStringValue(i);

			String type = dataClass.getStrval();

			if("factor".equals(type)){
				dataField = initDataField(dataField, DataType.STRING);
			} else

			if("numeric".equals(type)){
				dataField = initDataField(dataField, DataType.DOUBLE);
			} else

			if("logical".equals(type)){
				dataField = initDataField(dataField, DataType.BOOLEAN);
			} else

			{
				throw new IllegalArgumentException();
			}

			this.dataFields.add(dataField);
		}
	}

	private void initNonFormulaFields(Rexp.REXP xlevels, Rexp.REXP ncat, Rexp.REXP y){

		// Dependent variable
		{
			DataField dataField = new DataField()
				.withName(FieldName.create("_target"));

			boolean classification = (y != null);

			if(classification){
				dataField = initDataField(dataField, DataType.STRING);
			} else

			{
				dataField = initDataField(dataField, DataType.DOUBLE);
			}

			this.dataFields.add(dataField);
		}

		Rexp.REXP names = REXPUtil.attribute(xlevels, "names");

		// Independent variable(s)
		for(int i = 0; i < names.getStringValueCount(); i++){
			STRING name = names.getStringValue(i);

			DataField dataField = new DataField()
				.withName(FieldName.create(name.getStrval()));

			boolean categorical = (ncat.getIntValue(i) > 1);
			if(categorical){
				dataField = initDataField(dataField, DataType.STRING);
			} else

			{
				dataField = initDataField(dataField, DataType.DOUBLE);
			}

			this.dataFields.add(dataField);
		}
	}

	private DataField initDataField(DataField dataField, DataType dataType){

		switch(dataType){
			case STRING:
				return dataField.withDataType(DataType.STRING)
					.withOptype(OpType.CATEGORICAL);
			case DOUBLE:
				return dataField.withDataType(DataType.DOUBLE)
					.withOptype(OpType.CONTINUOUS);
			case BOOLEAN:
				return dataField.withDataType(DataType.BOOLEAN)
					.withOptype(OpType.CATEGORICAL);
			default:
				throw new IllegalArgumentException();
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
	}

	private DataDictionary encodeDataDictionary(Set<FieldName> fields){
		List<DataField> dataFields = Lists.newArrayList(this.dataFields.subList(1, this.dataFields.size()));

		for(Iterator<DataField> it = dataFields.iterator(); it.hasNext(); ){
			DataField dataField = it.next();

			if(!(fields).contains(dataField.getName())){
				it.remove();
			}
		}

		Comparator<DataField> comparator = new Comparator<DataField>(){

			@Override
			public int compare(DataField left, DataField right){
				return ((left.getName()).getValue()).compareTo((right.getName()).getValue());
			}
		};
		Collections.sort(dataFields, comparator);

		DataDictionary dataDictionary = new DataDictionary()
			.withDataFields(this.dataFields.subList(0, 1))
			.withDataFields(dataFields);

		return dataDictionary;
	}

	private MiningSchema encodeMiningSchema(Set<FieldName> fields){
		DataField dataField = this.dataFields.get(0);

		MiningField targetField = new MiningField(dataField.getName())
			.withUsageType(FieldUsageType.TARGET);

		List<MiningField> activeFields = encodeMiningFields(fields);

		MiningSchema miningSchema = new MiningSchema()
			.withMiningFields(Collections.singletonList(targetField))
			.withMiningFields(activeFields);

		return miningSchema;
	}

	private List<MiningField> encodeMiningFields(Set<FieldName> fields){
		Function<FieldName, MiningField> function = new Function<FieldName, MiningField>(){

			@Override
			public MiningField apply(FieldName field){
				return new MiningField(field);
			}
		};

		List<MiningField> miningFields = Lists.newArrayList(Iterables.transform(fields, function));

		Comparator<MiningField> comparator = new Comparator<MiningField>(){

			@Override
			public int compare(MiningField left, MiningField right){
				boolean leftActive = (left.getUsageType()).equals(FieldUsageType.ACTIVE);
				boolean rightActive = (right.getUsageType()).equals(FieldUsageType.ACTIVE);

				if(leftActive && !rightActive){
					return 1;
				} // End if

				if(!leftActive && rightActive){
					return -1;
				}

				return ((left.getName()).getValue()).compareTo((right.getName()).getValue());
			}
		};
		Collections.sort(miningFields, comparator);

		return miningFields;
	}

	private <P extends Number> TreeModel encodeTreeModel(MiningFunctionType miningFunction, List<Integer> leftDaughter, List<Integer> rightDaughter, ScoreEncoder<P> scoreEncoder, List<P> nodepred, List<Integer> bestvar, List<Double> xbestsplit){
		Node root = new Node()
			.withId("1")
			.withPredicate(new True());

		encodeNode(root, 0, leftDaughter, rightDaughter, bestvar, xbestsplit, scoreEncoder, nodepred);

		TreeModel treeModel = new TreeModel(new MiningSchema(), root, miningFunction)
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

			PredicateKey key = new PredicateKey(dataField, split);

			DataType dataType = dataField.getDataType();
			switch(dataType){
				case STRING:
					leftPredicate = this.leftSimpleSetPredicates.getUnchecked(key);
					rightPredicate = this.rightSimpleSetPredicates.getUnchecked(key);
					break;
				case DOUBLE:
				case BOOLEAN:
					leftPredicate = this.leftSimplePredicates.getUnchecked(key);
					rightPredicate = this.rightSimplePredicates.getUnchecked(key);
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

	private SimpleSetPredicate encodeSimpleSetPredicate(DataField dataField, Integer split, boolean leftDaughter){
		SimpleSetPredicate simpleSetPredicate = new SimpleSetPredicate()
			.withField(dataField.getName())
			.withBooleanOperator(SimpleSetPredicate.BooleanOperator.IS_IN)
			.withArray(encodeArray(dataField, split, leftDaughter));

		return simpleSetPredicate;
	}

	private Array encodeArray(DataField dataField, Integer split, boolean leftDaughter){
		String value = formatArrayValue(dataField.getValues(), split, leftDaughter);

		Array array = new Array(value, Array.Type.STRING);

		return array;
	}

	private SimplePredicate encodeSimplePredicate(DataField dataField, Double split, boolean leftDaughter){
		SimplePredicate simplePredicate;

		DataType dataType = dataField.getDataType();

		if((DataType.DOUBLE).equals(dataType)){
			simplePredicate = new SimplePredicate()
				.withField(dataField.getName())
				.withOperator(leftDaughter ? SimplePredicate.Operator.LESS_OR_EQUAL : SimplePredicate.Operator.GREATER_THAN)
				.withValue(formatValue(split));
		} else

		if((DataType.BOOLEAN).equals(dataType)){
			simplePredicate = new SimplePredicate()
				.withField(dataField.getName())
				.withOperator(SimplePredicate.Operator.EQUAL)
				.withValue(split.doubleValue() <= 0.5d ? Boolean.toString(!leftDaughter) : Boolean.toString(leftDaughter));
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
	private String formatArrayValue(List<Value> values, Integer split, boolean leftDaughter){
		StringBuilder sb = new StringBuilder();

		String sep = "";

		String string = performBinaryExpansion(split);

		for(int i = 0; i < values.size(); i++){
			Value value = values.get(i);

			boolean append;

			// Send "true" categories to the left
			if(leftDaughter){
				append = ((i < string.length()) && (string.charAt(i) == '1'));
			} else

			// Send all other categories to the right
			{
				append = ((i >= string.length()) || (string.charAt(i) == '0'));
			} // End if

			if(append){
				sb.append(sep);

				String element = value.getValue();
				if(element.indexOf(' ') > -1){
					sb.append('\"').append(element).append('\"');
				} else

				{
					sb.append(element);
				}

				sep = " ";
			}
		}

		return sb.toString();
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
					return asInteger(number);
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

	static
	private class PredicateKey {

		private DataField dataField = null;

		private Number split = null;


		private PredicateKey(DataField dataField, Number split){
			setDataField(dataField);
			setSplit(split);
		}

		@Override
		public int hashCode(){
			return getDataField().hashCode() ^ getSplit().hashCode();
		}

		@Override
		public boolean equals(Object object){

			if(object instanceof PredicateKey){
				PredicateKey that = (PredicateKey)object;

				return (this.getDataField()).equals(that.getDataField()) && (this.getSplit()).equals(that.getSplit());
			}

			return false;
		}

		public DataField getDataField(){
			return this.dataField;
		}

		private void setDataField(DataField dataField){
			this.dataField = dataField;
		}

		public Number getSplit(){
			return this.split;
		}

		private void setSplit(Number split){
			this.split = split;
		}
	}

	static
	private class FieldCollector extends AbstractVisitor {

		private Set<FieldName> fields = new LinkedHashSet<FieldName>();


		@Override
		public VisitorAction visit(SimpleSetPredicate simpleSetPredicate){
			this.fields.add(simpleSetPredicate.getField());

			return super.visit(simpleSetPredicate);
		}

		@Override
		public VisitorAction visit(SimplePredicate simplePredicate){
			this.fields.add(simplePredicate.getField());

			return super.visit(simplePredicate);
		}

		public Set<FieldName> getFields(){
			return this.fields;
		}
	}

	static
	private class FieldTypeAnalyzer extends AbstractVisitor {

		private Map<FieldName, DataType> fieldDataTypes = new LinkedHashMap<FieldName, DataType>();


		@Override
		public VisitorAction visit(SimpleSetPredicate simpleSetPredicate){
			FieldName field = simpleSetPredicate.getField();

			addDataType(field, DataType.STRING);

			return super.visit(simpleSetPredicate);
		}

		@Override
		public VisitorAction visit(SimplePredicate simplePredicate){
			FieldName field = simplePredicate.getField();
			SimplePredicate.Operator operator = simplePredicate.getOperator();
			String value = simplePredicate.getValue();

			if((SimplePredicate.Operator.EQUAL).equals(operator) && (("true").equals(value) || ("false").equals(value))){
				addDataType(field, DataType.BOOLEAN);
			} else

			if((SimplePredicate.Operator.LESS_OR_EQUAL).equals(operator) && ("0.5").equals(value)){
				addDataType(field, DataType.BOOLEAN);
			} else

			if((SimplePredicate.Operator.GREATER_THAN).equals(operator) && ("0.5").equals(value)){
				addDataType(field, DataType.BOOLEAN);
			} else

			{
				addDataType(field, DataType.DOUBLE);
			}

			return super.visit(simplePredicate);
		}

		private void addDataType(FieldName field, DataType dataType){
			DataType fieldDataType = this.fieldDataTypes.get(field);
			if(fieldDataType == null){
				this.fieldDataTypes.put(field, dataType);

				return;
			}

			switch(fieldDataType){
				case STRING:
					return;
				case DOUBLE:
					switch(dataType){
						case STRING:
							this.fieldDataTypes.put(field, dataType);
							return;
						case DOUBLE:
						case BOOLEAN:
							return;
						default:
							throw new IllegalArgumentException();
					}
				case BOOLEAN:
					switch(dataType){
						case STRING:
						case DOUBLE:
							this.fieldDataTypes.put(field, dataType);
							return;
						case BOOLEAN:
							return;
						default:
							throw new IllegalArgumentException();
					}
				default:
					return;
			}
		}

		public DataType getDataType(FieldName field){
			return this.fieldDataTypes.get(field);
		}
	}
}
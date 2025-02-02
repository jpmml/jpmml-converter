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
package org.jpmml.converter;

import java.util.Arrays;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FeatureTest {

	@Test
	public void binaryFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CATEGORICAL, DataType.INTEGER)
			.addValues(new Value("1"), new Value("2"), new Value("3"));

		BinaryFeature binaryOne = new BinaryFeature(encoder, dataField, "1");

		ContinuousFeature continuousOne = binaryOne.toContinuousFeature();

		assertEquals("x=1", continuousOne.getName());
		assertEquals(DataType.DOUBLE, continuousOne.getDataType());

		assertNotNull(encoder.getDerivedField(continuousOne.getName()));

		ContinuousFeature continuousFloatOne = binaryOne.toContinuousFeature(DataType.FLOAT);

		assertEquals("float(" + continuousOne.getName() + ")", continuousFloatOne.getName());
		assertEquals(DataType.FLOAT, continuousFloatOne.getDataType());
	}

	@Test
	public void booleanFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CATEGORICAL, DataType.BOOLEAN);

		BooleanFeature booleanFlag = new BooleanFeature(encoder, dataField);

		ContinuousFeature continuousTrue = booleanFlag.toContinuousFeature();

		assertEquals("x=true", continuousTrue.getName());
		assertEquals(DataType.DOUBLE, continuousTrue.getDataType());

		assertNotNull(encoder.getDerivedField(continuousTrue.getName()));

		ContinuousFeature continuousFloatTrue = booleanFlag.toContinuousFeature(DataType.FLOAT);

		assertEquals("float(" + continuousTrue.getName() + ")", continuousFloatTrue.getName());
		assertEquals(DataType.FLOAT, continuousFloatTrue.getDataType());
	}

	@Test
	public void categoricalFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CATEGORICAL, DataType.INTEGER)
			.addValues(new Value("1"), new Value("2"), new Value("3"));

		CategoricalFeature categoricalInteger = new CategoricalFeature(encoder, dataField);

		assertEquals(Arrays.asList("1", "2", "3"), categoricalInteger.getValues());

		ContinuousFeature continuousInteger = categoricalInteger.toContinuousFeature();

		assertEquals(DataType.INTEGER, continuousInteger.getDataType());

		assertEquals(OpType.CONTINUOUS, dataField.requireOpType());
		assertEquals(DataType.INTEGER, dataField.requireDataType());
		assertEquals(Arrays.asList("1", "2", "3"), FieldUtil.getValues(dataField));
	}

	@Test
	public void constantFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		ConstantFeature integerOne = new ConstantFeature(encoder, 1);

		assertEquals("1", integerOne.getName());
		assertEquals(DataType.INTEGER, integerOne.getDataType());

		ContinuousFeature continuousIntegerOne = integerOne.toContinuousFeature();

		assertEquals("constant(1)", continuousIntegerOne.getName());
		assertEquals(DataType.INTEGER, continuousIntegerOne.getDataType());

		assertNotNull(encoder.getDerivedField(continuousIntegerOne.getName()));

		ConstantFeature floatOne = new ConstantFeature(encoder, 1f);

		assertEquals("1.0f", floatOne.getName());
		assertEquals(DataType.FLOAT, floatOne.getDataType());

		ConstantFeature doubleOne = new ConstantFeature(encoder, 1d);

		assertEquals("1.0", doubleOne.getName());
		assertEquals(DataType.DOUBLE, doubleOne.getDataType());
	}

	@Test
	public void interactionFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CATEGORICAL, DataType.INTEGER)
			.addValues(new Value("1"), new Value("2"), new Value("3"));

		BinaryFeature binaryOne = new BinaryFeature(encoder, dataField, "1");
		BinaryFeature binaryTwo = new BinaryFeature(encoder, dataField, "2");
		BinaryFeature binaryThree = new BinaryFeature(encoder, dataField, "3");

		InteractionFeature interactionOneThree = new InteractionFeature(encoder, "x=1:x=3", DataType.DOUBLE, Arrays.asList(binaryOne, binaryThree));

		assertEquals(Arrays.asList(binaryOne, binaryThree), interactionOneThree.getInputFeatures());

		ContinuousFeature continuousOneThree = interactionOneThree.toContinuousFeature();

		assertEquals(interactionOneThree.getName(), continuousOneThree.getName());
		assertEquals(DataType.DOUBLE, continuousOneThree.getDataType());

		assertNotNull(encoder.getDerivedField(continuousOneThree.getName()));

		assertNotNull(encoder.getDerivedField("x=1"));
		assertNull(encoder.getDerivedField("x=2"));
		assertNotNull(encoder.getDerivedField("x=3"));

		ContinuousFeature continuousFloatOneThree = interactionOneThree.toContinuousFeature(DataType.FLOAT);

		assertEquals("float(" + continuousOneThree.getName() + ")", continuousFloatOneThree.getName());
		assertEquals(DataType.FLOAT, continuousFloatOneThree.getDataType());

		InteractionFeature interactionTwoOneThree = new InteractionFeature(encoder, "x=2:x=1:x=3", DataType.DOUBLE, Arrays.asList(binaryTwo, interactionOneThree));

		assertEquals(Arrays.asList(binaryTwo, binaryOne, binaryThree), interactionTwoOneThree.getInputFeatures());

		ContinuousFeature continuousTwoOneThree = interactionTwoOneThree.toContinuousFeature();

		assertEquals(interactionTwoOneThree.getName(), continuousTwoOneThree.getName());
		assertEquals(DataType.DOUBLE, continuousTwoOneThree.getDataType());

		assertNotNull(encoder.getDerivedField(continuousFloatOneThree.getName()));

		assertNotNull(encoder.getDerivedField("x=2"));
	}

	@Test
	public void powerFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CONTINUOUS, DataType.DOUBLE);

		PowerFeature square = new PowerFeature(encoder, dataField, 2);

		ContinuousFeature continuousSquare = square.toContinuousFeature();

		assertEquals("x^2", continuousSquare.getName());
		assertEquals(DataType.DOUBLE, continuousSquare.getDataType());

		assertNotNull(encoder.getDerivedField(continuousSquare.getName()));
	}

	@Test
	public void wildcardFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CONTINUOUS, DataType.DOUBLE);

		WildcardFeature wildcard = new WildcardFeature(encoder, dataField);

		CategoricalFeature categoricalDouble = wildcard.toCategoricalFeature(Arrays.asList("1", "2", "3"));

		assertEquals(DataType.DOUBLE, categoricalDouble.getDataType());
		assertEquals(Arrays.asList("1", "2", "3"), categoricalDouble.getValues());

		assertEquals(OpType.CATEGORICAL, dataField.requireOpType());
		assertEquals(DataType.DOUBLE, dataField.requireDataType());
		assertEquals(Arrays.asList("1", "2", "3"), FieldUtil.getValues(dataField));
	}
}
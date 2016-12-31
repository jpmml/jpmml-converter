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
import org.dmg.pmml.FieldName;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Value;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FeatureTest {

	@Test
	public void binaryFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField(FieldName.create("x"), OpType.CATEGORICAL, DataType.INTEGER)
			.addValues(new Value("1"), new Value("2"), new Value("3"));

		BinaryFeature binaryOne = new BinaryFeature(encoder, dataField, "1");

		ContinuousFeature continuousOne = binaryOne.toContinuousFeature();

		assertEquals(FieldName.create("x=1"), continuousOne.getName());
		assertEquals(DataType.DOUBLE, continuousOne.getDataType());

		assertNotNull(encoder.getDerivedField(continuousOne.getName()));
	}

	@Test
	public void interactionFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField(FieldName.create("x"), OpType.CATEGORICAL, DataType.INTEGER)
			.addValues(new Value("1"), new Value("2"), new Value("3"));

		BinaryFeature binaryOne = new BinaryFeature(encoder, dataField, "1");
		BinaryFeature binaryTwo = new BinaryFeature(encoder, dataField, "2");
		BinaryFeature binaryThree = new BinaryFeature(encoder, dataField, "3");

		InteractionFeature interactionOneThree = new InteractionFeature(encoder, FieldName.create("x=1:x=3"), DataType.DOUBLE, Arrays.asList(binaryOne, binaryThree));

		assertEquals(Arrays.asList(binaryOne, binaryThree), interactionOneThree.getInputFeatures());

		ContinuousFeature continuousOneThree = interactionOneThree.toContinuousFeature();

		assertNotNull(encoder.getDerivedField(interactionOneThree.getName()));

		assertEquals(interactionOneThree.getName(), continuousOneThree.getName());
		assertEquals(DataType.DOUBLE, continuousOneThree.getDataType());

		assertNotNull(encoder.getDerivedField(FieldName.create("x=1")));
		assertNull(encoder.getDerivedField(FieldName.create("x=2")));
		assertNotNull(encoder.getDerivedField(FieldName.create("x=3")));

		InteractionFeature interactionTwoOneThree = new InteractionFeature(encoder, FieldName.create("x=2:x=1:x=3"), DataType.DOUBLE, Arrays.asList(binaryTwo, interactionOneThree));

		assertEquals(Arrays.asList(binaryTwo, binaryOne, binaryThree), interactionTwoOneThree.getInputFeatures());

		ContinuousFeature continuousTwoOneThree = interactionTwoOneThree.toContinuousFeature();

		assertNotNull(encoder.getDerivedField(interactionTwoOneThree.getName()));

		assertEquals(interactionTwoOneThree.getName(), continuousTwoOneThree.getName());
		assertEquals(DataType.DOUBLE, continuousTwoOneThree.getDataType());

		assertNotNull(encoder.getDerivedField(FieldName.create("x=2")));
	}

	@Test
	public void listFeature(){
		PMMLEncoder encoder = new PMMLEncoder();

		DataField dataField = encoder.createDataField(FieldName.create("x"), OpType.CATEGORICAL, DataType.INTEGER)
			.addValues(new Value("1"), new Value("2"), new Value("3"));

		ListFeature list = new ListFeature(encoder, dataField);

		assertEquals(Arrays.asList("1", "2", "3"), list.getValues());
	}
}
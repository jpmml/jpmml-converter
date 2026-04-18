/*
 * Copyright (c) 2026 Villu Ruusmann
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

import org.dmg.pmml.DataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class InteractionFeatureTest {

	@Test
	public void simplify(){
		PMMLEncoder encoder = new PMMLEncoder();

		ConstantFeature constantMinusOne = new ConstantFeature(encoder, -1d);
		ConstantFeature constantZero = new ConstantFeature(encoder, 0d);
		ConstantFeature constantOne = new ConstantFeature(encoder, 1d);

		BinaryFeature colorRed = new BinaryFeature(encoder, "color", DataType.STRING, "red");
		BinaryFeature colorBlue = new BinaryFeature(encoder, "color", DataType.STRING, "blue");

		List<Feature> multiplicands = new ArrayList<>();
		multiplicands.add(constantMinusOne);
		multiplicands.add(colorRed);

		InteractionFeature feature = new InteractionFeature(encoder, "parent", DataType.DOUBLE, multiplicands);

		Feature simplifiedFeature = feature.simplify();

		assertSame(feature, simplifiedFeature);

		multiplicands.add(constantOne);

		simplifiedFeature = feature.simplify();

		assertNotSame(feature, simplifiedFeature);

		checkInteractionFeature(multiplicands.subList(0, 2), simplifiedFeature);

		multiplicands.remove(2);

		List<Feature> childMultiplicands = new ArrayList<>();
		childMultiplicands.add(constantOne);
		childMultiplicands.add(constantOne);

		InteractionFeature childFeature = new InteractionFeature(encoder, "child", DataType.DOUBLE, childMultiplicands);

		multiplicands.add(childFeature);

		simplifiedFeature = feature.simplify();

		checkInteractionFeature(multiplicands.subList(0, 2), simplifiedFeature);

		childMultiplicands.set(1, colorRed);

		simplifiedFeature = feature.simplify();

		checkInteractionFeature(multiplicands.subList(0, 2), simplifiedFeature);

		multiplicands.add(colorBlue);

		simplifiedFeature = feature.simplify();

		assertNotSame(constantZero, simplifiedFeature);
		checkConstant(0d, simplifiedFeature);

		multiplicands.set(3, constantZero);

		simplifiedFeature = feature.simplify();

		assertSame(constantZero, simplifiedFeature);
		checkConstant(0d, simplifiedFeature);
	}

	static
	private void checkConstant(Number expectedValue, Feature feature){
		ConstantFeature constantFeature = (ConstantFeature)feature;

		assertEquals(expectedValue, constantFeature.getValue());
	}

	static
	private void checkInteractionFeature(List<? extends Feature> expectedFeatures, Feature feature){
		InteractionFeature interactionFeature = (InteractionFeature)feature;

		assertEquals(expectedFeatures, interactionFeature.getFeatures());
	}
}
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
package org.jpmml.converter.general_regression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.dmg.pmml.DataType;
import org.dmg.pmml.general_regression.GeneralRegressionModel;
import org.dmg.pmml.general_regression.PCell;
import org.dmg.pmml.general_regression.PPCell;
import org.dmg.pmml.general_regression.ParameterCell;
import org.jpmml.converter.BinaryFeature;
import org.jpmml.converter.BooleanFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelEncoder;
import org.jpmml.converter.ModelTest;
import org.jpmml.converter.PowerFeature;
import org.jpmml.converter.ProductFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralRegressionModelUtilTest extends ModelTest {

	@Test
	public void encodeEmpty(){
		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Collections.emptyList(), Collections.emptyList(), null, null);

		assertState(generalRegressionModel, false, false, false);
	}

	@Test
	public void encodeBinaryFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature colorRed = new BinaryFeature(encoder, "color", DataType.STRING, "red");

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Arrays.asList(colorRed, colorRed), Arrays.asList(2d, 3d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 2d + 3d, Collections.singletonList("color"));

		generalRegressionModel = new GeneralRegressionModel();

		Feature colorBlue = new BinaryFeature(encoder, "color", DataType.STRING, "blue");

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Arrays.asList(colorRed, colorBlue), Arrays.asList(2d, 3d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 2d, Collections.singletonList("color"));
		assertParameter(generalRegressionModel, "p1", 3d, Collections.singletonList("color"));
	}

	@Test
	public void encodeBooleanFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature feature = new BooleanFeature(encoder, "x");

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Arrays.asList(feature, feature), Arrays.asList(1d, 1d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 1d + 1d, Collections.singletonList("x"));
	}

	@Test
	public void encodeConstantFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature feature = createConstantFeature(encoder, 3d);

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Collections.singletonList(feature), Collections.singletonList(2d), 1d, null);

		assertState(generalRegressionModel, true, true, false);

		assertParameter(generalRegressionModel, "p0", 1d + (2d * 3d), Collections.emptyList());
	}

	@Test
	public void encodeInteractionFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature feature = createInteractionFeature(encoder, 3d, "x", 7d);

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Collections.singletonList(feature), Collections.singletonList(2d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 2d * 3d * 7d, Collections.singletonList("x"));

		generalRegressionModel = new GeneralRegressionModel();

		feature = createInteractionFeature(encoder, "x1", 5d, "x2");

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Collections.singletonList(feature), Collections.singletonList(2d), 1d, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 1d, Collections.emptyList());
		assertParameter(generalRegressionModel, "p1", (2d * 5d), Arrays.asList("x1", "x2"));
	}

	@Test
	public void encodePowerFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature feature = new PowerFeature(encoder, "x", DataType.DOUBLE, 2);

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Arrays.asList(feature, feature), Arrays.asList(1d, 4d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 1d + 4d, Collections.singletonList("x"));
	}

	@Test
	public void encodeProductFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature childFeature = new ContinuousFeature(encoder, "x", DataType.DOUBLE);

		Feature feature = new ProductFeature(encoder, childFeature, 5d){

			@Override
			public ContinuousFeature toContinuousFeature(){
				throw new UnsupportedOperationException();
			}
		};

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Arrays.asList(feature), Arrays.asList(2d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 2d * 5d, Collections.singletonList("x"));
	}

	@Test
	public void encodeWildcardFeature(){
		ModelEncoder encoder = new ModelEncoder();

		GeneralRegressionModel generalRegressionModel = new GeneralRegressionModel();

		Feature feature = new ContinuousFeature(encoder, "x", DataType.DOUBLE);

		generalRegressionModel = GeneralRegressionModelUtil.encodeRegressionTable(generalRegressionModel, Arrays.asList(feature, feature), Arrays.asList(2d, 3d), null, null);

		assertState(generalRegressionModel, true, true, true);

		assertParameter(generalRegressionModel, "p0", 2d + 3d, Collections.singletonList("x"));
	}

	static
	private void assertState(GeneralRegressionModel generalRegressionModel, boolean hasParameters, boolean hasPCells, boolean hasPPCells){
		assertEquals(hasParameters, (generalRegressionModel.getParameterList()).hasParameters());
		assertEquals(hasPCells, (generalRegressionModel.getParamMatrix()).hasPCells());
		assertEquals(hasPPCells, (generalRegressionModel.getPPMatrix()).hasPPCells());
	}

	static
	private void assertParameter(GeneralRegressionModel generalRegressionModel, String parameterName, double beta, List<String> predictorNames){
		List<PCell> pCells = getParameterCells((generalRegressionModel.getParamMatrix()).getPCells(), parameterName);
		List<PPCell> ppCells = getParameterCells((generalRegressionModel.getPPMatrix()).getPPCells(), parameterName);

		assertEquals(1, pCells.size());
		assertEquals(predictorNames.size(), ppCells.size());

		assertEquals(beta, (pCells.get(0)).getBeta());

		for(int i = 0; i < predictorNames.size(); i++){
			assertEquals(predictorNames.get(i), (ppCells.get(i)).requireField());
		}
	}

	static
	private <C extends ParameterCell> List<C> getParameterCells(List<C> cells, String parameterName){
		List<C> result = new ArrayList<>();

		for(C cell : cells){

			if(Objects.equals(parameterName, cell.requireParameterName())){
				result.add(cell);
			}
		}

		return result;
	}
}
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
package org.jpmml.converter.regression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;
import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.regression.CategoricalPredictor;
import org.dmg.pmml.regression.NumericPredictor;
import org.dmg.pmml.regression.PredictorTerm;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.converter.BooleanFeature;
import org.jpmml.converter.ContinuousFeature;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelEncoder;
import org.jpmml.converter.ModelTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegressionModelUtilTest extends ModelTest {

	@Test
	public void createRegressionTable(){
		ModelEncoder encoder = new ModelEncoder();

		RegressionTable regressionTable = RegressionModelUtil.createRegressionTable(Collections.emptyList(), Collections.emptyList(), null);

		assertState(regressionTable, 0d, false, false, false);

		Feature feature = new BooleanFeature(encoder, "x");

		regressionTable = RegressionModelUtil.createRegressionTable(Arrays.asList(feature, feature), Arrays.asList(1d, 1d), null);

		assertState(regressionTable, 0d, false, true, false);

		CategoricalPredictor categoricalPredictor = Iterables.getOnlyElement(regressionTable.getCategoricalPredictors());

		assertEquals("x", categoricalPredictor.requireField());
		assertEquals(1d + 1d, categoricalPredictor.requireCoefficient());

		feature = createConstantFeature(encoder, 3d);

		regressionTable = RegressionModelUtil.createRegressionTable(Collections.singletonList(feature), Collections.singletonList(2d), 1d);

		assertState(regressionTable, 1d + (2d * 3d), false, false, false);

		feature = createInteractionFeature(encoder, 3d, "x", 7d);

		regressionTable = RegressionModelUtil.createRegressionTable(Collections.singletonList(feature), Collections.singletonList(2d), 1d);

		assertState(regressionTable, 1d, true, false, false);

		NumericPredictor numericPredictor = Iterables.getOnlyElement(regressionTable.getNumericPredictors());

		assertEquals("x", numericPredictor.requireField());
		assertEquals((Double)(2d * 3d * 7d), (Double)numericPredictor.requireCoefficient());

		feature = createInteractionFeature(encoder, "x1", 5d, "x2");

		regressionTable = RegressionModelUtil.createRegressionTable(Collections.singletonList(feature), Collections.singletonList(2d), 1d);

		assertState(regressionTable, 1d, false, false, true);

		PredictorTerm predictorTerm = Iterables.getOnlyElement(regressionTable.getPredictorTerms());

		assertEquals((Double)(2d * 5d), (Double)predictorTerm.requireCoefficient());

		List<FieldRef> fieldRefs = predictorTerm.requireFieldRefs();

		assertEquals("x1", (fieldRefs.get(0)).requireField());
		assertEquals("x2", (fieldRefs.get(1)).requireField());

		feature = new ContinuousFeature(encoder, "x", DataType.DOUBLE);

		regressionTable = RegressionModelUtil.createRegressionTable(Arrays.asList(feature, feature), Arrays.asList(1d, 1d), 1d);

		assertState(regressionTable, 1d, true, false, false);

		numericPredictor = Iterables.getOnlyElement(regressionTable.getNumericPredictors());

		assertEquals("x", numericPredictor.requireField());
		assertEquals(1d + 1d, numericPredictor.requireCoefficient());
	}

	static
	private void assertState(RegressionTable regressionTable, double intercept, boolean hasNumericTerms, boolean hasCategoricalTerms, boolean hasInteractionTerms){
		assertEquals((Double)intercept, (Double)regressionTable.requireIntercept());

		assertEquals(hasNumericTerms, regressionTable.hasNumericPredictors());
		assertEquals(hasCategoricalTerms, regressionTable.hasCategoricalPredictors());
		assertEquals(hasInteractionTerms, regressionTable.hasPredictorTerms());
	}
}
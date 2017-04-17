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

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.regression.NumericPredictor;
import org.dmg.pmml.regression.PredictorTerm;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.converter.Feature;
import org.jpmml.converter.ModelEncoder;
import org.jpmml.converter.SchemaUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegressionModelUtilTest {

	@Test
	public void createRegressionTable(){
		ModelEncoder encoder = new ModelEncoder();

		RegressionTable regressionTable = RegressionModelUtil.createRegressionTable(Collections.<Feature>emptyList(), null, Collections.<Double>emptyList());

		assertState(regressionTable, 0d, false, false, false);

		Feature feature = SchemaUtil.createConstantFeature(encoder, 3d);

		regressionTable = RegressionModelUtil.createRegressionTable(Collections.singletonList(feature), 1d, Collections.singletonList(2d));

		assertState(regressionTable, 1d + (2d * 3d), false, false, false);

		feature = SchemaUtil.createInteractionFeature(encoder, 3d, FieldName.create("x"), 7d);

		regressionTable = RegressionModelUtil.createRegressionTable(Collections.singletonList(feature), 1d, Collections.singletonList(2d));

		assertState(regressionTable, 1d, true, false, false);

		NumericPredictor numericPredictor = Iterables.getOnlyElement(regressionTable.getNumericPredictors());

		assertEquals(FieldName.create("x"), numericPredictor.getName());
		assertEquals((Double)(2d * 3d * 7d), (Double)numericPredictor.getCoefficient());

		feature = SchemaUtil.createInteractionFeature(encoder, FieldName.create("x1"), 5d, FieldName.create("x2"));

		regressionTable = RegressionModelUtil.createRegressionTable(Collections.singletonList(feature), 1d, Collections.singletonList(2d));

		assertState(regressionTable, 1d, false, false, true);

		PredictorTerm predictorTerm = Iterables.getOnlyElement(regressionTable.getPredictorTerms());

		assertEquals((Double)(2d * 5d), (Double)predictorTerm.getCoefficient());

		List<FieldRef> fieldRefs = predictorTerm.getFieldRefs();

		assertEquals(FieldName.create("x1"), (fieldRefs.get(0)).getField());
		assertEquals(FieldName.create("x2"), (fieldRefs.get(1)).getField());
	}

	static
	private void assertState(RegressionTable regressionTable, double intercept, boolean hasNumericTerms, boolean hasCategoricalTerms, boolean hasInteractionTerms){
		assertEquals((Double)intercept, (Double)regressionTable.getIntercept());

		assertEquals(hasNumericTerms, regressionTable.hasNumericPredictors());
		assertEquals(hasCategoricalTerms, regressionTable.hasCategoricalPredictors());
		assertEquals(hasInteractionTerms, regressionTable.hasPredictorTerms());
	}
}
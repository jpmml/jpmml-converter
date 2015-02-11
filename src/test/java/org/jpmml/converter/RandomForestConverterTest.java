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

import org.jpmml.evaluator.Batch;
import org.jpmml.evaluator.BatchUtil;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RandomForestConverterTest extends ConverterTest {

	@Test
	public void convertFormulaWineQuality() throws Exception {
		Converter converter = new RandomForestConverter();

		Batch batch = createBatch(converter, "RandomForestFormula", "WineQuality");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertWineQuality() throws Exception {
		Converter converter = new RandomForestConverter();

		Batch batch = createBatch(converter, "RandomForest", "WineQuality");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertFormulaWineColor() throws Exception {
		Converter converter = new RandomForestConverter();

		Batch batch = createBatch(converter, "RandomForestFormula", "WineColor");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertWineColor() throws Exception {
		Converter converter = new RandomForestConverter();

		Batch batch = createBatch(converter, "RandomForest", "WineColor");

		assertTrue(BatchUtil.evaluate(batch));
	}
}
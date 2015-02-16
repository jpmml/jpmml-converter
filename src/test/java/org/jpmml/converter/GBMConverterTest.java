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

public class GBMConverterTest extends ConverterTest {

	@Test
	public void convertFormulaAutoNA() throws Exception {
		Batch batch = createBatch("GBMFormula", "AutoNA");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertFitAutoNA() throws Exception {
		Batch batch = createBatch("GBMFit", "AutoNA");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretFormulaAutoNA() throws Exception {
		Batch batch = createBatch("CaretGBMFormula", "AutoNA");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretFitAutoNA() throws Exception {
		Batch batch = createBatch("CaretGBMFit", "AutoNA");

		assertTrue(BatchUtil.evaluate(batch));
	}
}
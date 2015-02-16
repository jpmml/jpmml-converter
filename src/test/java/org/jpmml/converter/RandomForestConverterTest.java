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
	public void convertFormulaAudit() throws Exception {
		Batch batch = createBatch("RandomForestFormula", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertAudit() throws Exception {
		Batch batch = createBatch("RandomForest", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretFormulaAuditMatrix() throws Exception {
		Batch batch = createBatch("CaretRandomForestFormula", "AuditMatrix");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretAudit() throws Exception {
		Batch batch = createBatch("CaretRandomForest", "Audit");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertFormulaAuto() throws Exception {
		Batch batch = createBatch("RandomForestFormula", "Auto");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertAuto() throws Exception {
		Batch batch = createBatch("RandomForest", "Auto");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretFormulaAuto() throws Exception {
		Batch batch = createBatch("CaretRandomForestFormula", "Auto");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretAuto() throws Exception {
		Batch batch = createBatch("CaretRandomForest", "Auto");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertFormulaWineQuality() throws Exception {
		Batch batch = createBatch("RandomForestFormula", "WineQuality");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertWineQuality() throws Exception {
		Batch batch = createBatch("RandomForest", "WineQuality");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertFormulaWineColor() throws Exception {
		Batch batch = createBatch("RandomForestFormula", "WineColor");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertWineColor() throws Exception {
		Batch batch = createBatch("RandomForest", "WineColor");

		assertTrue(BatchUtil.evaluate(batch));
	}
}
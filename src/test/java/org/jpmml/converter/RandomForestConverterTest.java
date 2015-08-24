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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.primitives.UnsignedLong;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Value;
import org.jpmml.evaluator.Batch;
import org.jpmml.evaluator.BatchUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomForestConverterTest extends ConverterTest {

	@Test
	public void selectValues(){
		List<Value> values = Arrays.asList(new Value("1"), new Value("2"), new Value("3"), new Value("4"));

		assertEquals(Arrays.asList(values.get(0), values.get(2), values.get(3)), RandomForestConverter.selectValues(values, 13d, true));
		assertEquals(Arrays.asList(values.get(1)), RandomForestConverter.selectValues(values, 13d, false));
	}

	@Test
	public void toUnsignedLong(){
		assertEquals(UnsignedLong.valueOf("13"), RandomForestConverter.toUnsignedLong(13d));

		assertEquals(UnsignedLong.valueOf("18446744071562067968"), RandomForestConverter.toUnsignedLong(-2147483648d));
	}

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
		Batch batch = createBatch("TrainRandomForestFormula", "AuditMatrix");

		Set<FieldName> ignoredColumns = Sets.newHashSet(FieldName.create("probability_0"), FieldName.create("probability_1"));

		assertTrue(BatchUtil.evaluate(batch, ignoredColumns));
	}

	@Test
	public void convertCaretAudit() throws Exception {
		Batch batch = createBatch("TrainRandomForest", "Audit");

		Set<FieldName> ignoredColumns = Sets.newHashSet(FieldName.create("probability_0"), FieldName.create("probability_1"));

		assertTrue(BatchUtil.evaluate(batch, ignoredColumns));
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
		Batch batch = createBatch("TrainRandomForestFormula", "Auto");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertCaretAuto() throws Exception {
		Batch batch = createBatch("TrainRandomForest", "Auto");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertFormulaIris() throws Exception {
		Batch batch = createBatch("RandomForestFormula", "Iris");

		assertTrue(BatchUtil.evaluate(batch));
	}

	@Test
	public void convertIris() throws Exception {
		Batch batch = createBatch("RandomForest", "Iris");

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
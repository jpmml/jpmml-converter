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
package org.jpmml.converter.visitors;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.regression.RegressionModel;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.NestedSegmentationTest;
import org.jpmml.model.ResourceUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MiningSchemaCleanerTest {

	@Test
	public void cleanChained() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(ChainedSegmentationTest.class);

		MiningSchemaCleaner cleaner = new MiningSchemaCleaner();
		cleaner.applyTo(pmml);

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(MiningModel miningModel){
				MiningSchema miningSchema = miningModel.requireMiningSchema();

				checkMiningSchema(Arrays.asList("y", "x1", "x2", "x3"), miningSchema);

				return super.visit(miningModel);
			}

			@Override
			public VisitorAction visit(RegressionModel regressionModel){
				MiningSchema miningSchema = regressionModel.requireMiningSchema();

				Segment segment = (Segment)getParent();

				String id = segment.getId();

				if("first".equals(id)){
					checkMiningSchema(Collections.singletonList("x1"), miningSchema);
				} else

				if("second".equals(id)){
					checkMiningSchema(Collections.singletonList("x2"), miningSchema);
				} else

				if("third".equals(id)){
					checkMiningSchema(Collections.singletonList("x3"), miningSchema);
				} else

				if("sum".equals(id)){
					checkMiningSchema(Arrays.asList("y", "first_output", "second_output", "third_output"), miningSchema);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(regressionModel);
			}
		};

		visitor.applyTo(pmml);
	}

	@Test
	public void cleanNested() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(NestedSegmentationTest.class);

		MiningSchemaCleaner cleaner = new MiningSchemaCleaner();
		cleaner.applyTo(pmml);

		Visitor miningModelVisitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(MiningModel miningModel){
				MiningSchema miningSchema = miningModel.requireMiningSchema();

				String id;

				try {
					Segment segment = (Segment)getParent();

					id = segment.getId();
				} catch(ClassCastException cce){
					id = null;
				} // End try

				if(id == null){
					checkMiningSchema(Arrays.asList("x1", "x2", "x3", "x4", "x5"), miningSchema);
				} else

				if("first".equals(id)){
					checkMiningSchema(Arrays.asList("x12", "x3", "x4", "x5"), miningSchema);
				} else

				if("second".equals(id)){
					checkMiningSchema(Arrays.asList("x123", "x12345"), miningSchema);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(miningModel);
			}
		};

		miningModelVisitor.applyTo(pmml);

		Visitor regressionModelVisitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(RegressionModel regressionModel){
				MiningSchema miningSchema = regressionModel.requireMiningSchema();

				checkMiningSchema(Collections.singletonList("x123"), miningSchema);

				return super.visit(regressionModel);
			}
		};

		regressionModelVisitor.applyTo(pmml);
	}

	static
	private void checkMiningSchema(Collection<String> names, MiningSchema miningSchema){
		assertEquals(new HashSet<>(names), getFieldNames(miningSchema));
	}

	static
	private Set<String> getFieldNames(MiningSchema miningSchema){
		List<MiningField> miningFields = miningSchema.getMiningFields();

		return miningFields.stream()
			.map(MiningField::getName)
			.collect(Collectors.toSet());
	}
}
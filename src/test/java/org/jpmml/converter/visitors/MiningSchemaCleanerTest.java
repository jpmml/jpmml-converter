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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MiningSchema;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.regression.RegressionModel;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.FieldNameUtil;
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
				MiningSchema miningSchema = miningModel.getMiningSchema();

				checkMiningSchema(FieldNameUtil.create("y", "x1", "x2", "x3"), miningSchema);

				return super.visit(miningModel);
			}

			@Override
			public VisitorAction visit(RegressionModel regressionModel){
				MiningSchema miningSchema = regressionModel.getMiningSchema();

				Segment segment = (Segment)getParent();

				String id = segment.getId();

				if("first".equals(id)){
					checkMiningSchema(FieldNameUtil.create("x1"), miningSchema);
				} else

				if("second".equals(id)){
					checkMiningSchema(FieldNameUtil.create("x2"), miningSchema);
				} else

				if("third".equals(id)){
					checkMiningSchema(FieldNameUtil.create("x3"), miningSchema);
				} else

				if("sum".equals(id)){
					checkMiningSchema(FieldNameUtil.create("y", "first_output", "second_output", "third_output"), miningSchema);
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
				MiningSchema miningSchema = miningModel.getMiningSchema();

				String id;

				try {
					Segment segment = (Segment)getParent();

					id = segment.getId();
				} catch(ClassCastException cce){
					id = null;
				} // End try

				if(id == null){
					checkMiningSchema(FieldNameUtil.create("x1", "x2", "x3", "x4", "x5"), miningSchema);
				} else

				if("first".equals(id)){
					checkMiningSchema(FieldNameUtil.create("x12", "x3", "x4", "x5"), miningSchema);
				} else

				if("second".equals(id)){
					checkMiningSchema(FieldNameUtil.create("x123", "x12345"), miningSchema);
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
				MiningSchema miningSchema = regressionModel.getMiningSchema();

				checkMiningSchema(FieldNameUtil.create("x123"), miningSchema);

				return super.visit(regressionModel);
			}
		};

		regressionModelVisitor.applyTo(pmml);
	}

	static
	private void checkMiningSchema(Set<FieldName> names, MiningSchema miningSchema){
		assertEquals(names, getFieldNames(miningSchema));
	}

	static
	private Set<FieldName> getFieldNames(MiningSchema miningSchema){
		Set<FieldName> result = new LinkedHashSet<>();

		List<MiningField> miningFields = miningSchema.getMiningFields();
		for(MiningField miningField : miningFields){
			result.add(miningField.getName());
		}

		return result;
	}
}
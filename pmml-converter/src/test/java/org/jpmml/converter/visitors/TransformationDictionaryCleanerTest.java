/*
 * Copyright (c) 2016 Villu Ruusmann
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

import org.dmg.pmml.DerivedField;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.regression.RegressionModel;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.NestedSegmentationTest;
import org.jpmml.model.ResourceUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransformationDictionaryCleanerTest {

	@Test
	public void cleanChained() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(ChainedSegmentationTest.class);

		TransformationDictionaryCleaner cleaner = new TransformationDictionaryCleaner();
		cleaner.applyTo(pmml);

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(RegressionModel regressionModel){
				LocalTransformations localTransformations = regressionModel.getLocalTransformations();

				Segment segment = (Segment)getParent();

				String id = segment.getId();

				if("first".equals(id)){
					assertFalse(localTransformations.hasDerivedFields());
				} else

				if("second".equals(id)){
					checkFields(Collections.singletonList("x2_squared"), localTransformations.getDerivedFields());
				} else

				if("third".equals(id)){
					assertFalse(localTransformations.hasDerivedFields());
				} else

				if("sum".equals(id)){
					assertNull(localTransformations);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(regressionModel);
			}

			@Override
			public VisitorAction visit(TransformationDictionary transformationDictionary){
				checkFields(Collections.singletonList("x1_squared"), transformationDictionary.getDerivedFields());

				return super.visit(transformationDictionary);
			}
		};

		visitor.applyTo(pmml);

		TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();

		assertTrue(transformationDictionary.hasDerivedFields());

		List<Model> models = pmml.getModels();
		models.clear();

		cleaner.reset();

		cleaner.applyTo(pmml);

		assertFalse(transformationDictionary.hasDerivedFields());
	}

	@Test
	public void cleanNested() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(NestedSegmentationTest.class);

		TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();

		assertFalse(transformationDictionary.hasDerivedFields());

		TransformationDictionaryCleaner cleaner = new TransformationDictionaryCleaner();
		cleaner.applyTo(pmml);

		assertFalse(transformationDictionary.hasDerivedFields());

		Visitor miningModelVisitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(MiningModel miningModel){
				LocalTransformations localTransformations = miningModel.getLocalTransformations();

				String id;

				try {
					Segment segment = (Segment)getParent();

					id = segment.getId();
				} catch(ClassCastException cce){
					id = null;
				} // End try

				if(id == null){
					checkFields(Collections.singletonList("x12"), localTransformations.getDerivedFields());
				} else

				if("first".equals(id)){
					checkFields(Arrays.asList("x123", "x1234", "x12345"), localTransformations.getDerivedFields());
				} else

				if("second".equals(id)){
					assertFalse(localTransformations.hasDerivedFields());
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
				LocalTransformations localTransformations = regressionModel.getLocalTransformations();

				assertFalse(localTransformations.hasDerivedFields());

				return super.visit(regressionModel);
			}
		};

		regressionModelVisitor.applyTo(pmml);
	}

	static
	private void checkFields(Collection<String> names, Collection<DerivedField> derivedFields){
		assertEquals(new HashSet<>(names), FieldUtil.nameSet(derivedFields));
	}
}
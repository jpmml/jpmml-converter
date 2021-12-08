/*
 * Copyright (c) 2019 Villu Ruusmann
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

import org.dmg.pmml.DerivedField;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.Model;
import org.dmg.pmml.PMML;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.mining.Segment;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.NestedSegmentationTest;
import org.jpmml.model.ResourceUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class DerivedFieldRelocatorTest {

	@Test
	public void relocateChained() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(ChainedSegmentationTest.class);

		TransformationDictionaryCleaner cleaner = new TransformationDictionaryCleaner();
		cleaner.applyTo(pmml);

		DerivedFieldRelocator relocator = new DerivedFieldRelocator();
		relocator.applyTo(pmml);

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(Segment segment){
				Model model = segment.getModel();

				LocalTransformations localTransformations = model.getLocalTransformations();

				if(("first").equals(segment.getId())){
					checkFields(Collections.singletonList("x1_squared"), localTransformations.getDerivedFields());
				} else

				if(("second").equals(segment.getId())){
					checkFields(Collections.singletonList("x2_squared"), localTransformations.getDerivedFields());
				} else

				if(("third").equals(segment.getId())){
					assertFalse(localTransformations.hasDerivedFields());
				} else

				if(("sum").equals(segment.getId())){
					assertNull(localTransformations);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(segment);
			}

			@Override
			public VisitorAction visit(TransformationDictionary transformationDictionary){
				assertFalse(transformationDictionary.hasDerivedFields());

				return super.visit(pmml);
			}
		};
		visitor.applyTo(pmml);
	}

	@Test
	public void relocateNested() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(NestedSegmentationTest.class);

		DerivedFieldRelocator relocator = new DerivedFieldRelocator();
		relocator.applyTo(pmml);

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(Segment segment){
				Model model = segment.getModel();

				LocalTransformations localTransformations = model.getLocalTransformations();

				if(("first").equals(segment.getId())){
					assertFalse(localTransformations.hasDerivedFields());
				} else

				if(("second").equals(segment.getId())){
					checkFields(Arrays.asList("x12", "x123", "x1234", "x12345"), localTransformations.getDerivedFields());
				} else

				if(("third").equals(segment.getId())){
					assertFalse(localTransformations.hasDerivedFields());
				} else

				{
					throw new AssertionError();
				}

				return super.visit(segment);
			}
		};
		visitor.applyTo(pmml);
	}

	static
	private void checkFields(Collection<String> names, Collection<DerivedField> fields){
		assertEquals(new HashSet<>(names), FieldUtil.nameSet(fields));
	}
}
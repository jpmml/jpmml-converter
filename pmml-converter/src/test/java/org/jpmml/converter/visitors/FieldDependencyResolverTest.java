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
import java.util.Map;
import java.util.Set;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.NestedSegmentationTest;
import org.jpmml.model.ResourceUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldDependencyResolverTest {

	@Test
	public void resolveChained() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(ChainedSegmentationTest.class);

		FieldDependencyResolver resolver = new FieldDependencyResolver();
		resolver.applyTo(pmml);

		Map<Field<?>, Set<Field<?>>> dependencies = resolver.getDependencies();

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(DataField dataField){
				checkFields(Collections.emptyList(), dependencies.get(dataField));

				return super.visit(dataField);
			}

			@Override
			public VisitorAction visit(DerivedField derivedField){
				Set<Field<?>> fields = dependencies.get(derivedField);

				String name = derivedField.requireName();

				if("x1_squared".equals(name)){
					checkFields(Collections.singletonList("x1"), fields);
				} else

				if("x1_cubed".equals(name)){
					checkFields(Arrays.asList("x1", "x1_squared"), fields);
				} else

				if("x2_squared".equals(name) || "x2_cubed".equals(name)){
					checkFields(Collections.singletonList("x2"), fields);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(derivedField);
			}

			@Override
			public VisitorAction visit(OutputField outputField){
				checkFields(Collections.emptyList(), dependencies.get(outputField));

				return super.visit(outputField);
			}
		};

		visitor.applyTo(pmml);
	}

	@Test
	public void resolveNested() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(NestedSegmentationTest.class);

		FieldDependencyResolver resolver = new FieldDependencyResolver();
		resolver.applyTo(pmml);

		Map<Field<?>, Set<Field<?>>> dependencies = resolver.getDependencies();

		Visitor visitor = new AbstractVisitor(){

			@Override
			public VisitorAction visit(DerivedField derivedField){
				Set<Field<?>> fields = dependencies.get(derivedField);

				String name = derivedField.requireName();

				if("x12".equals(name)){
					checkFields(Arrays.asList("x1", "x2"), fields);
				} else

				if("x123".equals(name)){
					checkFields(Arrays.asList("x12", "x3"), fields);
				} else

				if("x1234".equals(name)){
					checkFields(Arrays.asList("x123", "x4"), fields);
				} else

				if("x12345".equals(name)){
					checkFields(Arrays.asList("x1234", "x5"), fields);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(derivedField);
			}
		};

		visitor.applyTo(pmml);
	}

	static
	private void checkFields(Collection<String> names, Set<Field<?>> fields){
		assertEquals(new HashSet<>(names), FieldUtil.nameSet(fields));
	}
}
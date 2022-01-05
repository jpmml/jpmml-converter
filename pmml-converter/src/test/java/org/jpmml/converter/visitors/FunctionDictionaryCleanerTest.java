/*
 * Copyright (c) 2020 Villu Ruusmann
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.dmg.pmml.Apply;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.LocalTransformations;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLFunctions;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.Visitor;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.ChainedSegmentationTest;
import org.jpmml.model.ResourceUtil;
import org.jpmml.model.visitors.AbstractVisitor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FunctionDictionaryCleanerTest {

	@Test
	public void cleanChained() throws Exception {
		PMML pmml = ResourceUtil.unmarshal(ChainedSegmentationTest.class);

		TransformationDictionary transformationDictionary = pmml.getTransformationDictionary();

		FunctionDictionaryCleaner cleaner = new FunctionDictionaryCleaner();
		cleaner.applyTo(pmml);

		checkFunctions(Arrays.asList("square", "cube"), transformationDictionary.getDefineFunctions());

		Visitor fieldModifier = new AbstractVisitor(){

			@Override
			public VisitorAction visit(DerivedField derivedField){

				if(("x2_squared").equals(derivedField.requireName())){
					FieldRef fieldRef = new FieldRef("x2");

					Apply apply = new Apply(PMMLFunctions.ADD)
						.addExpressions(fieldRef, fieldRef);

					derivedField.setExpression(apply);
				}

				return super.visit(derivedField);
			}
		};
		fieldModifier.applyTo(pmml);

		cleaner.reset();

		cleaner.applyTo(pmml);

		checkFunctions(Collections.singletonList("cube"), transformationDictionary.getDefineFunctions());

		Visitor fieldRemover = new AbstractVisitor(){

			@Override
			public VisitorAction visit(LocalTransformations localTransformations){

				if(localTransformations.hasDerivedFields()){
					List<DerivedField> derivedFields = localTransformations.getDerivedFields();

					for(Iterator<DerivedField> it = derivedFields.iterator(); it.hasNext(); ){
						DerivedField derivedField = it.next();

						if(("x2_cubed").equals(derivedField.requireName())){
							it.remove();
						}
					}
				}

				return super.visit(localTransformations);
			}
		};
		fieldRemover.applyTo(pmml);

		cleaner.reset();

		cleaner.applyTo(pmml);

		checkFunctions(Collections.emptyList(), transformationDictionary.getDefineFunctions());
	}

	static
	private void checkFunctions(Collection<String> names, Collection<DefineFunction> defineFunctions){
		assertEquals(new HashSet<>(names), nameSet(defineFunctions));
	}

	static
	private Set<String> nameSet(Collection<DefineFunction> defineFunctions){
		return defineFunctions.stream()
			.map(DefineFunction::requireName)
			.collect(Collectors.toSet());
	}
}
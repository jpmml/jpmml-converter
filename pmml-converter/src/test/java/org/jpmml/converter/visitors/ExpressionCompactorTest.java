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

import org.dmg.pmml.Apply;
import org.dmg.pmml.Constant;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.converter.ExpressionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpressionCompactorTest {

	@Test
	public void compactComparisonExpression(){
		FieldRef fieldRef = new FieldRef("x");

		Apply apply = compact(ExpressionUtil.createApply(PMMLFunctions.EQUAL, fieldRef, ExpressionUtil.createMissingConstant()));

		assertEquals(PMMLFunctions.ISMISSING, apply.requireFunction());
		assertEquals(Arrays.asList(fieldRef), apply.getExpressions());

		apply = compact(ExpressionUtil.createApply(PMMLFunctions.NOTEQUAL, ExpressionUtil.createMissingConstant(), fieldRef));

		assertEquals(PMMLFunctions.ISNOTMISSING, apply.requireFunction());
		assertEquals(Arrays.asList(fieldRef), apply.getExpressions());
	}

	@Test
	public void compactLogicalExpression(){
		FieldRef fieldRef = new FieldRef("x");

		Apply first = ExpressionUtil.createApply(PMMLFunctions.EQUAL, fieldRef, ExpressionUtil.createConstant("1"));

		Apply leftLeftChild = ExpressionUtil.createApply(PMMLFunctions.EQUAL, fieldRef, ExpressionUtil.createConstant("2/L/L"));
		Apply leftRightChild = ExpressionUtil.createApply(PMMLFunctions.EQUAL, fieldRef, ExpressionUtil.createConstant("2/L/R"));

		Apply leftChild = ExpressionUtil.createApply(PMMLFunctions.OR, leftLeftChild, leftRightChild);
		Apply rightChild = ExpressionUtil.createApply(PMMLFunctions.EQUAL, fieldRef, ExpressionUtil.createConstant("2/R"));

		Apply second = ExpressionUtil.createApply(PMMLFunctions.OR, leftChild, rightChild);

		Apply third = ExpressionUtil.createApply(PMMLFunctions.EQUAL, fieldRef, ExpressionUtil.createConstant("3"));

		Apply apply = compact(ExpressionUtil.createApply(PMMLFunctions.OR, first, second, third));

		assertEquals(PMMLFunctions.OR, apply.requireFunction());
		assertEquals(Arrays.asList(first, leftLeftChild, leftRightChild, rightChild, third), apply.getExpressions());
	}

	@Test
	public void compactConcatExpression(){
		FieldRef hours = new FieldRef("hours");
		FieldRef minutes = new FieldRef("minutes");
		FieldRef seconds = new FieldRef("seconds");

		Constant separator = ExpressionUtil.createConstant(":");

		Apply apply = compact(ExpressionUtil.createApply(PMMLFunctions.CONCAT, hours, ExpressionUtil.createApply(PMMLFunctions.CONCAT, separator, minutes), ExpressionUtil.createApply(PMMLFunctions.CONCAT, separator, seconds)));

		assertEquals(PMMLFunctions.CONCAT, apply.requireFunction());
		assertEquals(Arrays.asList(hours, separator, minutes, separator, seconds), apply.getExpressions());
	}

	@Test
	public void compactNegationExpression(){
		FieldRef fieldRef = new FieldRef("x");

		checkNegation(PMMLFunctions.ISMISSING, PMMLFunctions.ISNOTMISSING, fieldRef);

		Constant constant = ExpressionUtil.createConstant(0);

		checkNegation(PMMLFunctions.EQUAL, PMMLFunctions.NOTEQUAL, fieldRef, constant);
		checkNegation(PMMLFunctions.LESSTHAN, PMMLFunctions.GREATEROREQUAL, fieldRef, constant);
		checkNegation(PMMLFunctions.LESSOREQUAL, PMMLFunctions.GREATERTHAN, fieldRef, constant);
	}

	static
	private void checkNegation(String function, String negatedFunction, Expression... expressions){
		Apply apply = ExpressionUtil.createApply(function, expressions);

		Apply negatedApply = compact(ExpressionUtil.createApply(PMMLFunctions.NOT, apply));

		assertEquals(negatedFunction, negatedApply.requireFunction());
		assertEquals(Arrays.asList(expressions), negatedApply.getExpressions());

		apply = compact(ExpressionUtil.createApply(PMMLFunctions.NOT, negatedApply));

		assertEquals(function, apply.requireFunction());
		assertEquals(Arrays.asList(expressions), apply.getExpressions());
	}

	static
	private Apply compact(Apply apply){
		ExpressionCompactor compactor = new ExpressionCompactor();
		compactor.applyTo(apply);

		return apply;
	}
}
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
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpressionCompactorTest {

	@Test
	public void compactLogicalExpression(){
		FieldRef fieldRef = new FieldRef(FieldName.create("x"));

		Apply first = createApply("equal", fieldRef, createConstant("1"));

		Apply leftLeftChild = createApply("equal", fieldRef, createConstant("2/L/L"));
		Apply leftRightChild = createApply("equal", fieldRef, createConstant("2/L/R"));

		Apply leftChild = createApply("or", leftLeftChild, leftRightChild);
		Apply rightChild = createApply("equal", fieldRef, createConstant("2/R"));

		Apply second = createApply("or", leftChild, rightChild);

		Apply third = createApply("equal", fieldRef, createConstant("3"));

		Apply apply = compact(createApply("or", first, second, third));

		assertEquals(Arrays.asList(first, leftLeftChild, leftRightChild, rightChild, third), apply.getExpressions());
	}

	@Test
	public void compactNegationExpression(){
		FieldRef fieldRef = new FieldRef(FieldName.create("x"));

		checkNegation("isMissing", "isNotMissing", fieldRef);

		Constant constant = createConstant("0");

		checkNegation("equal", "notEqual", fieldRef, constant);
		checkNegation("lessThan", "greaterOrEqual", fieldRef, constant);
		checkNegation("lessOrEqual", "greaterThan", fieldRef, constant);;
	}

	static
	private void checkNegation(String function, String negatedFunction, Expression... expressions){
		Apply apply = createApply(function, expressions);

		Apply negatedApply = compact(createApply("not", apply));

		assertEquals(negatedFunction, negatedApply.getFunction());
		assertEquals(Arrays.asList(expressions), negatedApply.getExpressions());

		apply = compact(createApply("not", negatedApply));

		assertEquals(function, apply.getFunction());
		assertEquals(Arrays.asList(expressions), apply.getExpressions());
	}

	static
	private Apply createApply(String function, Expression... expressions){
		Apply apply = new Apply(function);

		for(Expression expression : expressions){
			apply.addExpressions(expression);
		}

		return apply;
	}

	static
	private Constant createConstant(String value){
		Constant constant = new Constant(value);

		return constant;
	}

	static
	private Apply compact(Apply apply){
		ExpressionCompactor compactor = new ExpressionCompactor();
		compactor.applyTo(apply);

		return apply;
	}
}
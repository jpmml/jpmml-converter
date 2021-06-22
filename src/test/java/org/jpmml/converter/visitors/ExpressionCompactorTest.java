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
import org.dmg.pmml.DataType;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.PMMLFunctions;
import org.jpmml.converter.PMMLUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpressionCompactorTest {

	@Test
	public void compactLogicalExpression(){
		FieldRef fieldRef = new FieldRef(FieldName.create("x"));

		Apply first = PMMLUtil.createApply(PMMLFunctions.EQUAL, fieldRef, PMMLUtil.createConstant("1", DataType.STRING));

		Apply leftLeftChild = PMMLUtil.createApply(PMMLFunctions.EQUAL, fieldRef, PMMLUtil.createConstant("2/L/L", DataType.STRING));
		Apply leftRightChild = PMMLUtil.createApply(PMMLFunctions.EQUAL, fieldRef, PMMLUtil.createConstant("2/L/R", DataType.STRING));

		Apply leftChild = PMMLUtil.createApply(PMMLFunctions.OR, leftLeftChild, leftRightChild);
		Apply rightChild = PMMLUtil.createApply(PMMLFunctions.EQUAL, fieldRef, PMMLUtil.createConstant("2/R", DataType.STRING));

		Apply second = PMMLUtil.createApply(PMMLFunctions.OR, leftChild, rightChild);

		Apply third = PMMLUtil.createApply(PMMLFunctions.EQUAL, fieldRef, PMMLUtil.createConstant("3", DataType.STRING));

		Apply apply = compact(PMMLUtil.createApply(PMMLFunctions.OR, first, second, third));

		assertEquals(PMMLFunctions.OR, apply.getFunction());
		assertEquals(Arrays.asList(first, leftLeftChild, leftRightChild, rightChild, third), apply.getExpressions());
	}

	@Test
	public void compactConcatExpression(){
		FieldRef hours = new FieldRef(FieldName.create("hours"));
		FieldRef minutes = new FieldRef(FieldName.create("minutes"));
		FieldRef seconds = new FieldRef(FieldName.create("seconds"));

		Constant separator = PMMLUtil.createConstant(":", DataType.STRING);

		Apply apply = compact(PMMLUtil.createApply(PMMLFunctions.CONCAT, hours, PMMLUtil.createApply(PMMLFunctions.CONCAT, separator, minutes), PMMLUtil.createApply(PMMLFunctions.CONCAT, separator, seconds)));

		assertEquals(PMMLFunctions.CONCAT, apply.getFunction());
		assertEquals(Arrays.asList(hours, separator, minutes, separator, seconds), apply.getExpressions());
	}

	@Test
	public void compactNegationExpression(){
		FieldRef fieldRef = new FieldRef(FieldName.create("x"));

		checkNegation(PMMLFunctions.ISMISSING, PMMLFunctions.ISNOTMISSING, fieldRef);

		Constant constant = PMMLUtil.createConstant(0);

		checkNegation(PMMLFunctions.EQUAL, PMMLFunctions.NOTEQUAL, fieldRef, constant);
		checkNegation(PMMLFunctions.LESSTHAN, PMMLFunctions.GREATEROREQUAL, fieldRef, constant);
		checkNegation(PMMLFunctions.LESSOREQUAL, PMMLFunctions.GREATERTHAN, fieldRef, constant);
	}

	static
	private void checkNegation(String function, String negatedFunction, Expression... expressions){
		Apply apply = PMMLUtil.createApply(function, expressions);

		Apply negatedApply = compact(PMMLUtil.createApply(PMMLFunctions.NOT, apply));

		assertEquals(negatedFunction, negatedApply.getFunction());
		assertEquals(Arrays.asList(expressions), negatedApply.getExpressions());

		apply = compact(PMMLUtil.createApply(PMMLFunctions.NOT, negatedApply));

		assertEquals(function, apply.getFunction());
		assertEquals(Arrays.asList(expressions), apply.getExpressions());
	}

	static
	private Apply compact(Apply apply){
		ExpressionCompactor compactor = new ExpressionCompactor();
		compactor.applyTo(apply);

		return apply;
	}
}
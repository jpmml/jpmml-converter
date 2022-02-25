/*
 * Copyright (c) 2021 Villu Ruusmann
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.dmg.pmml.Apply;
import org.dmg.pmml.Constant;
import org.dmg.pmml.DataType;
import org.dmg.pmml.Expression;
import org.dmg.pmml.FieldRef;
import org.dmg.pmml.PMMLFunctions;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExpressionUtilTest {

	@Test
	public void isString(){
		FeatureResolver featureResolver = new FeatureResolver(){

			@Override
			public Feature resolveFeature(String name){
				return ExpressionUtilTest.stringFeatures.get(name);
			}
		};

		assertFalse(ExpressionUtil.isString(createConstant("Hello World!", null), featureResolver));
		assertTrue(ExpressionUtil.isString(createConstant("Hello World!", DataType.STRING), featureResolver));

		assertFalse(ExpressionUtil.isString(new FieldRef("x"), featureResolver));

		assertTrue(ExpressionUtil.isString(new FieldRef("a"), featureResolver));
		assertTrue(ExpressionUtil.isString(new FieldRef("b"), featureResolver));

		Expression expression = PMMLUtil.createApply(PMMLFunctions.CONCAT, PMMLUtil.createConstant("Hello World!", null), new FieldRef("x"));

		assertTrue(ExpressionUtil.isString(expression, featureResolver));

		assertFalse(ExpressionUtil.isString(createIfApply("x", null), featureResolver));
		assertTrue(ExpressionUtil.isString(createIfApply("a", null), featureResolver));
		assertFalse(ExpressionUtil.isString(createIfApply("a", "x"), featureResolver));
		assertTrue(ExpressionUtil.isString(createIfApply("a", "b"), featureResolver));
	}

	static
	private Constant createConstant(Object value, DataType dataType){
		return PMMLUtil.createConstant(value, dataType);
	}

	static
	private Apply createIfApply(String trueName, String falseName){
		Apply apply = PMMLUtil.createApply(PMMLFunctions.IF,
			createConstant(Boolean.TRUE, null),
			new FieldRef(trueName)
		);

		if(falseName != null){
			apply.addExpressions(new FieldRef(falseName));
		}

		return apply;
	}

	private static final Map<String, Feature> stringFeatures = new LinkedHashMap<>();

	static {
		PMMLEncoder encoder = new PMMLEncoder();

		stringFeatures.put("a", new ObjectFeature(encoder, "a", DataType.STRING));
		stringFeatures.put("b", new ObjectFeature(encoder, "b", DataType.STRING));
	}
}
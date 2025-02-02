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
package org.jpmml.converter;

import java.util.Arrays;

import org.dmg.pmml.DataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeUtilTest {

	@Test
	public void getDataType(){
		assertEquals(DataType.INTEGER, TypeUtil.getDataType(1));
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType(1.0d));

		assertEquals(DataType.INTEGER, TypeUtil.getDataType("1"));
		assertEquals(DataType.INTEGER, TypeUtil.getDataType("1.0"));
		assertEquals(DataType.DOUBLE, TypeUtil.getDataType("1.1"));
		assertEquals(DataType.STRING, TypeUtil.getDataType("one"));

		assertEquals(DataType.INTEGER, TypeUtil.getDataType(Arrays.asList("1", "2", "3")));
		assertEquals(DataType.INTEGER, TypeUtil.getDataType(Arrays.asList("1", "2.0", "3")));
		assertEquals(DataType.STRING, TypeUtil.getDataType(Arrays.asList("1", "2.1", "3")));
		assertEquals(DataType.STRING, TypeUtil.getDataType(Arrays.asList("1", "two", "3")));
	}
}
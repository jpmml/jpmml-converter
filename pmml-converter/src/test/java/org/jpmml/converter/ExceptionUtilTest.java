/*
 * Copyright (c) 2026 Villu Ruusmann
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
import java.util.List;
import java.util.Set;

import org.dmg.pmml.DataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionUtilTest {

	@Test
	public void formatCount(){
		assertEquals("no elements", ExceptionUtil.formatCount(0, "element"));
		assertEquals("one element", ExceptionUtil.formatCount(1, "element"));
		assertEquals("2 elements", ExceptionUtil.formatCount(2, "element"));
	}

	@Test
	public void formatClass(){
		assertEquals("int", ExceptionUtil.formatClass(int.class));
		assertEquals("int[]", ExceptionUtil.formatClass(int[].class));

		assertEquals("java.lang.Object", ExceptionUtil.formatClass(Object.class));
		assertEquals("java.lang.Object[]", ExceptionUtil.formatClass(Object[].class));
	}

	@Test
	public void formatName(){
		assertEquals("\'x\'", ExceptionUtil.formatName("x"));
	}

	@Test
	public void formatNames(){
		assertEquals("\'x1\', \'x2\', \'x3\'", ExceptionUtil.formatNames(Arrays.asList("x1", "x2", "x3")));
	}

	@Test
	public void formatNameList(){
		assertEquals("[\'a\', \'b\']", ExceptionUtil.formatNameList(List.of("a", "b")));
		assertEquals("[\'a\', \'b\']", ExceptionUtil.formatNameList(Set.of("a", "b")));
	}

	@Test
	public void formatNameSet(){
		assertEquals("{\'a\', \'b\'}", ExceptionUtil.formatNameSet(List.of("a", "b")));
		assertEquals("{\'a\', \'b\'}", ExceptionUtil.formatNameSet(Set.of("a", "b")));
	}

	@Test
	public void formatValue(){
		assertEquals("string", ExceptionUtil.formatValue(DataType.STRING));
	}

	@Test
	public void formatValues(){
		assertEquals("integer, float, double", ExceptionUtil.formatValues(Arrays.asList(DataType.INTEGER, DataType.FLOAT, DataType.DOUBLE)));
	}
}
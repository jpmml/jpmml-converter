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
package org.jpmml.converter;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PMMLUtilTest {

	@Test
	public void formatValue(){
		assertEquals("1", PMMLUtil.formatValue(1d));
		assertEquals("1", PMMLUtil.formatValue(1.0d));
	}

	@Test
	public void formatArrayValue(){
		assertEquals("", PMMLUtil.formatArrayValue(Arrays.<String>asList()));

		assertEquals("one two three", PMMLUtil.formatArrayValue(Arrays.asList("one", "two", "three")));
		assertEquals("one \" two \" three", PMMLUtil.formatArrayValue(Arrays.asList("one", " two ", "three")));
	}
}
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
import java.util.Collections;

import org.dmg.pmml.DataType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ValueUtilTest {

	@Test
	public void getDataType(){
		assertEquals(DataType.INTEGER, ValueUtil.getDataType(Arrays.asList("1", "2", "3")));
		assertEquals(DataType.DOUBLE, ValueUtil.getDataType(Arrays.asList("1", "2.0", "3")));
		assertEquals(DataType.STRING, ValueUtil.getDataType(Arrays.asList("1", "two", "3")));
	}

	@Test
	public void formatValue(){
		assertEquals("1", ValueUtil.formatValue(1d));
		assertEquals("1", ValueUtil.formatValue(1.0d));
	}

	@Test
	public void formatArrayValue(){
		assertEquals("", ValueUtil.formatArrayValue(Collections.<String>emptyList()));

		assertEquals("one two three", ValueUtil.formatArrayValue(Arrays.asList("one", "two", "three")));
		assertEquals("one \" two \" three", ValueUtil.formatArrayValue(Arrays.asList("one", " two ", "three")));
	}

	@Test
	public void asInteger(){
		assertEquals((Integer)0, ValueUtil.asInteger((Integer)0));
		assertEquals((Integer)0, ValueUtil.asInteger((Long)0L));
		assertEquals((Integer)0, ValueUtil.asInteger((Float)0f));
		assertEquals((Integer)0, ValueUtil.asInteger((Double)0d));

		try {
			ValueUtil.asInteger((Double)0.5d);

			fail();
		} catch(IllegalArgumentException iae){
			// Ignored
		}

		try {
			ValueUtil.asInteger((Double)(Integer.MAX_VALUE + 0.5d));

			fail();
		} catch(IllegalArgumentException iae){
			// Ignored
		}
	}
}
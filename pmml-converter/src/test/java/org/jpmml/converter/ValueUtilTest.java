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

import org.dmg.pmml.PMMLConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValueUtilTest {

	@Test
	public void isNaN(){
		assertFalse(ValueUtil.isNaN(PMMLConstants.NOT_A_NUMBER));

		assertTrue(ValueUtil.isNaN(Float.NaN));
		assertTrue(ValueUtil.isNaN(Double.NaN));
	}

	@Test
	public void isZero(){
		assertTrue(ValueUtil.isZero(0));
		assertTrue(ValueUtil.isZero(-0f));
		assertTrue(ValueUtil.isZero(0f));
		assertTrue(ValueUtil.isZero(0d));
		assertTrue(ValueUtil.isZero(-0d));

		float floatUlp = Math.ulp(0f);

		assertFalse(ValueUtil.isZero(0f - floatUlp));
		assertFalse(ValueUtil.isZero(0f + floatUlp));

		double doubleUlp = Math.ulp(0d);

		assertFalse(ValueUtil.isZero(0d - doubleUlp));
		assertFalse(ValueUtil.isZero(0d + doubleUlp));
	}

	@Test
	public void isOne(){
		assertTrue(ValueUtil.isOne(1));
		assertTrue(ValueUtil.isOne(1f));
		assertTrue(ValueUtil.isOne(1d));
	}

	@Test
	public void equals(){
		assertTrue(ValueUtil.equals(-0f, 0));
		assertTrue(ValueUtil.equals(0f, 0));
		assertTrue(ValueUtil.equals(-0d, 0));
		assertTrue(ValueUtil.equals(0d, 0));

		assertTrue(ValueUtil.equals(-0d, 0f));
		assertTrue(ValueUtil.equals(0d, 0f));
	}

	@Test
	public void asInteger(){
		assertEquals((Integer)0, ValueUtil.asInteger((Integer)0));
		assertEquals((Integer)0, ValueUtil.asInteger((Long)0L));
		assertEquals((Integer)0, ValueUtil.asInteger((Float)0f));
		assertEquals((Integer)0, ValueUtil.asInteger((Double)0d));

		assertThrows(ConversionException.class, () -> ValueUtil.asInteger((Double)0.5d));
		assertThrows(ConversionException.class, () -> ValueUtil.asInteger((Double)(Integer.MAX_VALUE + 0.5d)));
	}

	@Test
	public void narrow(){
		assertEquals((byte)-1, ValueUtil.narrow(-1d));
		assertEquals((byte)0, ValueUtil.narrow(0d));
		assertEquals((byte)1, ValueUtil.narrow(1d));

		assertEquals((short)-129, ValueUtil.narrow(Byte.MIN_VALUE - 1L));
		assertEquals((byte)-128, ValueUtil.narrow(Byte.MIN_VALUE));
		assertEquals((byte)127, ValueUtil.narrow(Byte.MAX_VALUE));
		assertEquals((short)128, ValueUtil.narrow(Byte.MAX_VALUE + 1L));

		assertEquals((int)-32769, ValueUtil.narrow(Short.MIN_VALUE - 1L));
		assertEquals((short)-32768, ValueUtil.narrow(Short.MIN_VALUE));
		assertEquals((short)32767, ValueUtil.narrow(Short.MAX_VALUE));
		assertEquals((int)32768, ValueUtil.narrow(Short.MAX_VALUE + 1L));
	}
}
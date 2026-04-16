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
import java.util.Collections;

import org.dmg.pmml.DataField;
import org.dmg.pmml.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldUtilTest {

	@Test
	public void addValue(){
		DataField dataField = new DataField();

		FieldUtil.addValue(dataField, Value.Property.MISSING, -999);

		assertEquals(Collections.singletonList(-999), FieldUtil.getValues(dataField, Value.Property.MISSING));

		FieldUtil.addValue(dataField, Value.Property.MISSING, -999);

		assertEquals(Collections.singletonList(-999), FieldUtil.getValues(dataField, Value.Property.MISSING));
	}

	@Test
	public void addValues(){
		DataField dataField = new DataField();

		FieldUtil.addValues(dataField, Arrays.asList(0, 0, 1));

		assertEquals(Arrays.asList(0, 1), FieldUtil.getValues(dataField));
	}
}
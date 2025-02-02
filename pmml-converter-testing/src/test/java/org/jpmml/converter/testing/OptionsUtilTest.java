/*
 * Copyright (c) 2022 Villu Ruusmann
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
package org.jpmml.converter.testing;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OptionsUtilTest {

	@Test
	public void generateOptionsMatrix(){
		Map<String, Object> options = new LinkedHashMap<>();
		options.put("boolean", Boolean.TRUE);

		List<Map<String, Object>> optionsMatrix = OptionsUtil.generateOptionsMatrix(options);

		assertEquals(1, optionsMatrix.size());

		options.put("boolean", new Boolean[]{false, true});

		optionsMatrix = OptionsUtil.generateOptionsMatrix(options);

		assertEquals(2, optionsMatrix.size());

		assertEquals(Collections.singletonMap("boolean", false), optionsMatrix.get(0));
		assertEquals(Collections.singletonMap("boolean", true), optionsMatrix.get(1));

		options.put("integer", 0);

		optionsMatrix = OptionsUtil.generateOptionsMatrix(options);

		assertEquals(2, optionsMatrix.size());

		options.put("integer", new Integer[]{0, 1, 2});

		optionsMatrix = OptionsUtil.generateOptionsMatrix(options);

		assertEquals((2 * 3), optionsMatrix.size());

		Map<String, Object> expectedOptions = new LinkedHashMap<>();
		expectedOptions.put("boolean", false);
		expectedOptions.put("integer", 0);

		assertEquals(expectedOptions, optionsMatrix.get(0));

		expectedOptions.put("boolean", true);
		expectedOptions.put("integer", 2);

		assertEquals(expectedOptions, optionsMatrix.get(5));
	}
}
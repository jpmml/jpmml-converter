/*
 * Copyright (c) 2017 Villu Ruusmann
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

abstract
class MatrixUtilTest {

	protected static final List<String>[] COLUMNS = new List[]{
		Arrays.asList("11", "21", "31"),
		Arrays.asList("12", "22", "32"),
		Arrays.asList("13", "23", "33"),
		Arrays.asList("14", "24", "34"),
	};

	protected static final List<String>[] ROWS = new List[]{
		Arrays.asList("11", "12", "13", "14"),
		Arrays.asList("21", "22", "23", "24"),
		Arrays.asList("31", "32", "33", "34"),
	};
}
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FortranMatrixUtilTest extends MatrixUtilTest {

	@Test
	public void getColumn(){
		List<String> values = Arrays.asList(FortranMatrixUtilTest.DATA);

		List<String>[] columns = MatrixUtilTest.COLUMNS;
		for(int i = 0; i < columns.length; i++){
			assertEquals(columns[i], FortranMatrixUtil.getColumn(values, 3, 4, i));
		}
	}

	@Test
	public void getRow(){
		List<String> values = Arrays.asList(FortranMatrixUtilTest.DATA);

		List<String>[] rows = MatrixUtilTest.ROWS;
		for(int i = 0; i < rows.length; i++){
			assertEquals(rows[i], FortranMatrixUtil.getRow(values, 3, 4, i));
		}
	}

	private static final String[] DATA = {
		"11", "21", "31",
		"12", "22", "32",
		"13", "23", "33",
		"14", "24", "34",
	};
}
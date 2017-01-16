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

import java.util.ArrayList;
import java.util.List;

public class FortranMatrixUtil extends MatrixUtil {

	private FortranMatrixUtil(){
	}

	/**
	 * @param values A column-major matrix.
	 */
	static
	public <E> List<E> getColumn(List<E> values, int rows, int columns, int index){
		validateSize(values, rows, columns);

		int offset = (index * rows);

		return values.subList(offset, offset + rows);
	}

	/**
	 * @param values A column-major matrix.
	 */
	static
	public <E> List<E> getRow(List<E> values, int rows, int columns, int index){
		validateSize(values, rows, columns);

		List<E> result = new ArrayList<>(columns);

		for(int column = 0; column < columns; column++){
			E value = values.get((column * rows) + index);

			result.add(value);
		}

		return result;
	}
}
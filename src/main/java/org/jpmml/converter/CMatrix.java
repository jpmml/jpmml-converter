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

import java.util.List;

public class CMatrix<V> extends Matrix<V> {

	public CMatrix(List<V> values, int rows, int columns){
		super(values, rows, columns);
	}

	@Override
	public List<V> getRowValues(int row){
		return CMatrixUtil.getRow(getValues(), getRows(), getColumns(), row);
	}

	@Override
	public List<V> getColumnValues(int column){
		return CMatrixUtil.getColumn(getValues(), getRows(), getColumns(), column);
	}
}
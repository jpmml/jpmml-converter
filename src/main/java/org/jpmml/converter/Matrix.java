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
import java.util.Objects;

abstract
public class Matrix<V> {

	private List<V> values = null;

	private int rows = 0;

	private int columns = 0;


	public Matrix(List<V> values, int rows, int columns){
		setValues(values);
		setRows(rows);
		setColumns(columns);
	}

	abstract
	public List<V> getRowValues(int row);

	abstract
	public List<V> getColumnValues(int column);

	public List<V> getValues(){
		return this.values;
	}

	private void setValues(List<V> values){
		this.values = Objects.requireNonNull(values);
	}

	public int getRows(){
		return this.rows;
	}

	private void setRows(int rows){
		this.rows = rows;
	}

	public int getColumns(){
		return this.columns;
	}

	private void setColumns(int columns){
		this.columns = columns;
	}
}
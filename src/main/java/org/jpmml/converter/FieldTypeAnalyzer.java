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

import java.util.HashMap;
import java.util.Map;

import org.dmg.pmml.DataType;
import org.dmg.pmml.FieldName;
import org.jpmml.model.visitors.AbstractVisitor;

abstract
public class FieldTypeAnalyzer extends AbstractVisitor {

	private Map<FieldName, DataType> fieldDataTypes = new HashMap<FieldName, DataType>();


	public void addDataType(FieldName field, DataType dataType){
		DataType fieldDataType = this.fieldDataTypes.get(field);
		if(fieldDataType == null){
			this.fieldDataTypes.put(field, dataType);

			return;
		}

		switch(fieldDataType){
			case STRING:
				return;
			case DOUBLE:
				switch(dataType){
					case STRING:
						this.fieldDataTypes.put(field, dataType);
						return;
					case DOUBLE:
					case INTEGER:
					case BOOLEAN:
						return;
					default:
						throw new IllegalArgumentException();
				}
			case INTEGER:
				switch(dataType){
					case STRING:
					case DOUBLE:
						this.fieldDataTypes.put(field, dataType);
						return;
					case INTEGER:
					case BOOLEAN:
						return;
					default:
						throw new IllegalArgumentException();
				}
			case BOOLEAN:
				switch(dataType){
					case STRING:
					case DOUBLE:
					case INTEGER:
						this.fieldDataTypes.put(field, dataType);
						return;
					case BOOLEAN:
						return;
					default:
						throw new IllegalArgumentException();
				}
			default:
				throw new IllegalArgumentException();
		}
	}

	public DataType getDataType(FieldName field){
		return this.fieldDataTypes.get(field);
	}
}
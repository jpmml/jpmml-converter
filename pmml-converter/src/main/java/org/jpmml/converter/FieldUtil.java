/*
 * Copyright (c) 2024 Villu Ruusmann
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
import java.util.Iterator;
import java.util.List;

import org.dmg.pmml.Field;
import org.dmg.pmml.HasDiscreteDomain;
import org.dmg.pmml.Value;

public class FieldUtil {

	private FieldUtil(){
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> List<?> getValues(F field){
		return getValues(field, null);
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> List<?> getValues(F field, Value.Property property){
		List<Object> result = new ArrayList<>();

		if(property == null){
			property = Value.Property.VALID;
		}

		List<Value> pmmlValues = field.getValues();
		for(Value pmmlValue : pmmlValues){

			if(property == pmmlValue.getProperty()){
				result.add(pmmlValue.requireValue());
			}
		}

		return result;
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> void addValues(F field, List<?> values){
		addValues(field, null, values);
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> void addValues(F field, Value.Property property, List<?> values){

		if(property == Value.Property.VALID){
			property = null;
		}

		List<Value> pmmlValues = field.getValues();
		for(Object value : values){
			Value pmmlValue = new Value(value)
				.setProperty(property);

			pmmlValues.add(pmmlValue);
		}
	}

	static
	public <F extends Field<F> & HasDiscreteDomain<F>> void clearValues(F field, Value.Property property){

		if(property == null){
			property = Value.Property.VALID;
		}

		List<Value> pmmlValues = field.getValues();
		for(Iterator<Value> it = pmmlValues.iterator(); it.hasNext(); ){
			Value pmmlValue = it.next();

			if(pmmlValue.getProperty() == property){
				it.remove();
			}
		}
	}
}
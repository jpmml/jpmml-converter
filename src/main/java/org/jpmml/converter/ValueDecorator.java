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
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import org.dmg.pmml.DataField;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.Value;
import org.jpmml.model.ValueUtil;

public class ValueDecorator implements Decorator {

	private Value.Property property = null;

	private List<String> values = new ArrayList<>();


	protected ValueDecorator(Value.Property property){
		setProperty(property);
	}

	@Override
	public void decorate(DataField dataField, MiningField miningField){
		Value.Property property = getProperty();
		List<String> values = getValues();

		if(values.size() > 0){
			PMMLUtil.addValues(dataField, values, property);
		}
	}

	public Value.Property getProperty(){
		return this.property;
	}

	private ValueDecorator setProperty(Value.Property property){
		this.property = property;

		return this;
	}

	public List<String> getValues(){
		return this.values;
	}

	public ValueDecorator addValues(Object... values){
		getValues().addAll(Lists.transform(Arrays.asList(values), ValueUtil::toString));

		return this;
	}
}
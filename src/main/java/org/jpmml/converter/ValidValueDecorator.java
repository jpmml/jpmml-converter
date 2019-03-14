/*
 * Copyright (c) 2016 Villu Ruusmann
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

import org.dmg.pmml.DataField;
import org.dmg.pmml.Interval;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.Value;

public class ValidValueDecorator extends ValueDecorator {

	private List<Interval> intervals = new ArrayList<>();


	public ValidValueDecorator(){
		super(Value.Property.VALID);
	}

	@Override
	public void decorate(DataField dataField, MiningField miningField){
		List<Interval> intervals = getIntervals();

		if(intervals.size() > 0){
			PMMLUtil.addIntervals(dataField, intervals);
		}

		super.decorate(dataField, miningField);
	}

	public List<Interval> getIntervals(){
		return this.intervals;
	}

	public ValidValueDecorator addIntervals(Interval... intervals){
		getIntervals().addAll(Arrays.asList(intervals));

		return this;
	}

	@Override
	public ValidValueDecorator addValues(Object... values){
		return (ValidValueDecorator)super.addValues(values);
	}
}
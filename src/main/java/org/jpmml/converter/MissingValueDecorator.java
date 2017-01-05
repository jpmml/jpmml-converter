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
import org.dmg.pmml.MiningField;
import org.dmg.pmml.MissingValueTreatmentMethod;
import org.dmg.pmml.Value;

public class MissingValueDecorator implements FieldDecorator {

	private String missingValueReplacement = null;

	private MissingValueTreatmentMethod missingValueTreatment = null;

	private List<String> missingValues = new ArrayList<>();


	@Override
	public void decorate(DataField dataField, MiningField miningField){
		List<String> missingValues = getMissingValues();

		if(missingValues.size() > 0){
			PMMLUtil.addValues(dataField, missingValues, Value.Property.MISSING);
		}

		miningField
			.setMissingValueReplacement(getMissingValueReplacement())
			.setMissingValueTreatment(getMissingValueTreatment());
	}

	public String getMissingValueReplacement(){
		return this.missingValueReplacement;
	}

	public MissingValueDecorator setMissingValueReplacement(String missingValueReplacement){
		this.missingValueReplacement = missingValueReplacement;

		return this;
	}

	public MissingValueTreatmentMethod getMissingValueTreatment(){
		return this.missingValueTreatment;
	}

	public MissingValueDecorator setMissingValueTreatment(MissingValueTreatmentMethod missingValueTreatment){
		this.missingValueTreatment = missingValueTreatment;

		return this;
	}

	public MissingValueDecorator addMissingValues(String... missingValues){
		getMissingValues().addAll(Arrays.asList(missingValues));

		return this;
	}

	public List<String> getMissingValues(){
		return this.missingValues;
	}
}
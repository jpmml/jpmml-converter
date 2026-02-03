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

import org.dmg.pmml.MiningField;
import org.dmg.pmml.MissingValueTreatmentMethod;
import org.jpmml.model.ToStringHelper;

public class MissingValueDecorator extends Decorator {

	private MissingValueTreatmentMethod missingValueTreatment = null;

	private Object missingValueReplacement = null;


	public MissingValueDecorator(MissingValueTreatmentMethod missingValueTreatment, Object missingValueReplacement){

		if(missingValueTreatment == MissingValueTreatmentMethod.RETURN_INVALID){

			if(missingValueReplacement != null){
				throw new IllegalArgumentException();
			}
		} else

		{
			if(missingValueReplacement != null && ValueUtil.isNaN(missingValueReplacement)){
				throw new SchemaException("Expected a valid value as a missing value replacement value, got an invalid value (NaN)");
			}
		}

		setMissingValueTreatment(missingValueTreatment);
		setMissingValueReplacement(missingValueReplacement);
	}

	@Override
	public boolean isReplaceable(){
		return (getMissingValueTreatment() == MissingValueTreatmentMethod.AS_IS);
	}

	@Override
	public void decorate(MiningField miningField){
		miningField
			.setMissingValueTreatment(getMissingValueTreatment())
			.setMissingValueReplacement(getMissingValueReplacement());
	}

	@Override
	protected ToStringHelper toStringHelper(){
		return super.toStringHelper()
			.add("missingValueTreatment", getMissingValueTreatment())
			.add("missingValueReplacement", getMissingValueReplacement());
	}

	public MissingValueTreatmentMethod getMissingValueTreatment(){
		return this.missingValueTreatment;
	}

	private void setMissingValueTreatment(MissingValueTreatmentMethod missingValueTreatment){
		this.missingValueTreatment = missingValueTreatment;
	}

	public Object getMissingValueReplacement(){
		return this.missingValueReplacement;
	}

	private void setMissingValueReplacement(Object missingValueReplacement){
		this.missingValueReplacement = missingValueReplacement;
	}
}
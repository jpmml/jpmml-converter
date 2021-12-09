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

import org.dmg.pmml.InvalidValueTreatmentMethod;
import org.dmg.pmml.MiningField;

public class InvalidValueDecorator implements Decorator {

	private InvalidValueTreatmentMethod invalidValueTreatment = null;

	private Object invalidValueReplacement = null;


	public InvalidValueDecorator(InvalidValueTreatmentMethod invalidValueTreatment, Object invalidValueReplacement){

		if((InvalidValueTreatmentMethod.AS_VALUE).equals(invalidValueTreatment)){

			if(invalidValueReplacement == null){
				throw new IllegalArgumentException();
			}
		} else

		{
			if(invalidValueReplacement != null){
				throw new IllegalArgumentException();
			}
		}

		setInvalidValueTreatment(invalidValueTreatment);
		setInvalidValueReplacement(invalidValueReplacement);
	}

	@Override
	public void decorate(MiningField miningField){
		miningField
			.setInvalidValueTreatment(getInvalidValueTreatment())
			.setInvalidValueReplacement(getInvalidValueReplacement());
	}

	public InvalidValueTreatmentMethod getInvalidValueTreatment(){
		return this.invalidValueTreatment;
	}

	private void setInvalidValueTreatment(InvalidValueTreatmentMethod invalidValueTreatment){
		this.invalidValueTreatment = invalidValueTreatment;
	}

	public Object getInvalidValueReplacement(){
		return this.invalidValueReplacement;
	}

	private void setInvalidValueReplacement(Object invalidValueReplacement){
		this.invalidValueReplacement = invalidValueReplacement;
	}
}
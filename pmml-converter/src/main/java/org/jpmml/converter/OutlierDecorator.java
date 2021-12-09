/*
 * Copyright (c) 2018 Villu Ruusmann
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
import org.dmg.pmml.OutlierTreatmentMethod;

public class OutlierDecorator implements Decorator {

	private OutlierTreatmentMethod outlierTreatment = null;

	private Number lowValue = null;

	private Number highValue = null;


	public OutlierDecorator(OutlierTreatmentMethod outlierTreatment, Number lowValue, Number highValue){

		if(outlierTreatment == null || (OutlierTreatmentMethod.AS_IS).equals(outlierTreatment)){

			if(lowValue != null || highValue != null){
				throw new IllegalArgumentException();
			}
		}

		setOutlierTreatment(outlierTreatment);

		setLowValue(lowValue);
		setHighValue(highValue);
	}

	@Override
	public void decorate(MiningField miningField){
		miningField
			.setOutlierTreatment(getOutlierTreatmentMethod())
			.setLowValue(getLowValue())
			.setHighValue(getHighValue());
	}

	public OutlierTreatmentMethod getOutlierTreatmentMethod(){
		return this.outlierTreatment;
	}

	private void setOutlierTreatment(OutlierTreatmentMethod outlierTreatment){
		this.outlierTreatment = outlierTreatment;
	}

	public Number getLowValue(){
		return this.lowValue;
	}

	private void setLowValue(Number lowValue){
		this.lowValue = lowValue;
	}

	public Number getHighValue(){
		return this.highValue;
	}

	private void setHighValue(Number highValue){
		this.highValue = highValue;
	}
}
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

import org.dmg.pmml.DataField;
import org.dmg.pmml.MiningField;
import org.dmg.pmml.OutlierTreatmentMethod;

public class OutlierDecorator implements Decorator {

	private OutlierTreatmentMethod outlierTreatment = null;

	private Double lowValue = null;

	private Double highValue = null;


	@Override
	public void decorate(DataField dataField, MiningField miningField){
		OutlierTreatmentMethod outlierTreatment = getOutlierTreatmentMethod();

		Double lowValue = getLowValue();
		Double highValue = getHighValue();

		if(outlierTreatment != null && !(OutlierTreatmentMethod.AS_IS).equals(outlierTreatment)){
			miningField.setOutlierTreatment(outlierTreatment)
				.setLowValue(lowValue)
				.setHighValue(highValue);
		}
	}

	public OutlierTreatmentMethod getOutlierTreatmentMethod(){
		return this.outlierTreatment;
	}

	public OutlierDecorator setOutlierTreatment(OutlierTreatmentMethod outlierTreatment){
		this.outlierTreatment = outlierTreatment;

		return this;
	}

	public Double getLowValue(){
		return this.lowValue;
	}

	public OutlierDecorator setLowValue(Double lowValue){
		this.lowValue = lowValue;

		return this;
	}

	public Double getHighValue(){
		return this.highValue;
	}

	public OutlierDecorator setHighValue(Double highValue){
		this.highValue = highValue;

		return this;
	}
}
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

import org.dmg.pmml.FieldUsageType;
import org.dmg.pmml.MiningField;

public class MiningFieldComparator extends FieldComparator<MiningField> {

	@Override
	public int compare(MiningField left, MiningField right){
		FieldUsageType leftUsageType = left.getUsageType();
		FieldUsageType rightUsageType = right.getUsageType();

		int usageTypeDiff = (leftUsageType).compareTo(rightUsageType);
		if(usageTypeDiff != 0){
			return usageTypeDiff;
		}

		return super.compare(left, right);
	}
}
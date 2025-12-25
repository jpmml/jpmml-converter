/*
 * Copyright (c) 2020 Villu Ruusmann
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
package org.jpmml.converter.visitors;

import java.util.Collections;

import org.dmg.pmml.DataField;
import org.dmg.pmml.DataType;
import org.dmg.pmml.PMMLConstants;
import org.dmg.pmml.Value.Property;
import org.dmg.pmml.VisitorAction;
import org.jpmml.converter.FieldUtil;
import org.jpmml.model.visitors.AbstractVisitor;

public class NaNAsMissingDecorator extends AbstractVisitor {

	@Override
	public VisitorAction visit(DataField dataField){
		DataType dataType = dataField.requireDataType();

		switch(dataType){
			case FLOAT:
			case DOUBLE:
				FieldUtil.addValues(dataField, Property.MISSING, Collections.singletonList(PMMLConstants.NOT_A_NUMBER));
				break;
			default:
				break;
		}

		return super.visit(dataField);
	}
}
/*
 * Copyright (c) 2026 Villu Ruusmann
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
import org.dmg.pmml.DataType;
import org.dmg.pmml.InvalidValueTreatmentMethod;
import org.dmg.pmml.MissingValueTreatmentMethod;
import org.dmg.pmml.OpType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelEncoderTest {

	@Test
	public void addRemoveDecorator(){
		ModelEncoder encoder = new ModelEncoder();

		DataField dataField = encoder.createDataField("x", OpType.CONTINUOUS, DataType.DOUBLE);

		InvalidValueDecorator passInvalidValues = new InvalidValueDecorator(InvalidValueTreatmentMethod.AS_IS, null);
		InvalidValueDecorator rejectInvalidValues = new InvalidValueDecorator(InvalidValueTreatmentMethod.RETURN_INVALID, null);

		MissingValueDecorator passMissingValues = new MissingValueDecorator(MissingValueTreatmentMethod.AS_IS, null);
		MissingValueDecorator rejectMissingValues = new MissingValueDecorator(MissingValueTreatmentMethod.RETURN_INVALID, null);

		assertNull(encoder.getDecorator(dataField, InvalidValueDecorator.class));
		assertNull(encoder.getDecorator(dataField, MissingValueDecorator.class));
		assertNull(encoder.getDecorator(dataField, OutlierDecorator.class));

		assertTrue(encoder.addDecorator(dataField, passInvalidValues));
		assertTrue(encoder.addDecorator(dataField, passMissingValues));

		assertFalse(encoder.addDecorator(dataField, rejectInvalidValues));
		assertFalse(encoder.addDecorator(dataField, rejectMissingValues));

		assertSame(passInvalidValues, encoder.removeDecorator(dataField, InvalidValueDecorator.class));
		assertSame(passMissingValues, encoder.removeDecorator(dataField, MissingValueDecorator.class));

		assertTrue(encoder.addDecorator(dataField, rejectInvalidValues));
		assertTrue(encoder.addDecorator(dataField, rejectMissingValues));

		assertSame(rejectInvalidValues, encoder.getDecorator(dataField, InvalidValueDecorator.class));
		assertSame(rejectMissingValues, encoder.getDecorator(dataField, MissingValueDecorator.class));
		assertNull(encoder.getDecorator(dataField, OutlierDecorator.class));
	}
}
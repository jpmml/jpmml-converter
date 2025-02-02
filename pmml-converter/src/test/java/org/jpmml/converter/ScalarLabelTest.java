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

import java.util.Arrays;

import org.dmg.pmml.DataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScalarLabelTest {

	@Test
	public void categoricalLabel(){
		ScalarLabel scalarLabel = new CategoricalLabel("y", DataType.INTEGER, Arrays.asList("1", "2", "3"));

		assertFalse(scalarLabel.isAnonymous());

		scalarLabel = scalarLabel.toAnonymousLabel();

		assertTrue(scalarLabel.isAnonymous());
	}

	@Test
	public void continuousLabel(){
		ScalarLabel scalarLabel = new ContinuousLabel("y", DataType.DOUBLE);

		assertFalse(scalarLabel.isAnonymous());

		scalarLabel = scalarLabel.toAnonymousLabel();

		assertTrue(scalarLabel.isAnonymous());
	}
}
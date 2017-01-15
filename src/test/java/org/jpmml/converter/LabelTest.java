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
import org.dmg.pmml.FieldName;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LabelTest {

	@Test
	public void categoricalLabel(){
		Label label = new CategoricalLabel(FieldName.create("y"), DataType.INTEGER, Arrays.asList("1", "2", "3"));

		assertNotNull(label.getName());

		label = label.toAnonymousLabel();

		assertNull(label.getName());
	}

	@Test
	public void continuousLabel(){
		Label label = new ContinuousLabel(FieldName.create("y"), DataType.DOUBLE);

		assertNotNull(label.getName());

		label = label.toAnonymousLabel();

		assertNull(label.getName());
	}
}
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaUtilTest {

	@Test
	public void formatFeature(){
		assertEquals("feature", SchemaUtil.formatTypeString(Feature.class));

		assertEquals("categorical feature", SchemaUtil.formatTypeString(CategoricalFeature.class));
		assertEquals("boolean feature", SchemaUtil.formatTypeString(BooleanFeature.class));

		assertEquals("wildcard feature", SchemaUtil.formatTypeString(WildcardFeature.class));
	}

	@Test
	public void formatLabel(){
		assertEquals("label", SchemaUtil.formatTypeString(Label.class));

		Label label = new Label(){
		};

		assertEquals("label", SchemaUtil.formatTypeString(label.getClass()));

		assertEquals("scalar label", SchemaUtil.formatTypeString(ScalarLabel.class));
		assertEquals("discrete label", SchemaUtil.formatTypeString(DiscreteLabel.class));
		assertEquals("categorical label", SchemaUtil.formatTypeString(CategoricalLabel.class));

		assertEquals("multi label", SchemaUtil.formatTypeString(MultiLabel.class));
	}
}
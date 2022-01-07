/*
 * Copyright (c) 2021 Villu Ruusmann
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
package org.jpmml.converter.testing;

import org.jpmml.converter.FieldNamePrefixes;
import org.jpmml.converter.FieldNameUtil;

public interface Fields {

	String AUDIT_ADJUSTED = "Adjusted";
	String AUDIT_PROBABILITY_TRUE = FieldNameUtil.create(FieldNamePrefixes.PROBABILITY, 1);
	String AUDIT_PROBABILITY_FALSE = FieldNameUtil.create(FieldNamePrefixes.PROBABILITY, 0);

	String AUTO_MPG = "mpg";

	String IRIS_SPECIES = "Species";
	String IRIS_PROBABILITY_SETOSA = FieldNameUtil.create(FieldNamePrefixes.PROBABILITY, "setosa");
	String IRIS_PROBABILITY_VERSICOLOR = FieldNameUtil.create(FieldNamePrefixes.PROBABILITY, "versicolor");
	String IRIS_PROBABILITY_VIRGINICA = FieldNameUtil.create(FieldNamePrefixes.PROBABILITY, "virginica");
}
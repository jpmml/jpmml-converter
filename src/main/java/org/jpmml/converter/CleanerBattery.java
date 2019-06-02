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

import org.jpmml.converter.visitors.DerivedOutputFieldTransformer;
import org.jpmml.model.VisitorBattery;
import org.jpmml.model.visitors.DataDictionaryCleaner;
import org.jpmml.model.visitors.DerivedFieldRelocator;
import org.jpmml.model.visitors.MiningSchemaCleaner;
import org.jpmml.model.visitors.TransformationDictionaryCleaner;

public class CleanerBattery extends VisitorBattery {

	public CleanerBattery(){
		add(TransformationDictionaryCleaner.class);
		add(DerivedFieldRelocator.class);
		add(DataDictionaryCleaner.class);

		add(DerivedOutputFieldTransformer.class);

		// Most "aggressive", should be applied last,
		// when all surviving fields are on their final locations
		add(MiningSchemaCleaner.class);
	}
}
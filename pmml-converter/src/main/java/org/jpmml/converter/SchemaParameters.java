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

/**
 * @see HasSchemaOptions
 */
public interface SchemaParameters extends Parameters {

	String NAME_INPUT_FLOAT = Parameters.PREFIX_OPTION + HasSchemaOptions.OPTION_INPUT_FLOAT;
	String DESCRIPTION_INPUT_FLOAT = "Allow field data type updates";

	String NAME_NUMERIC = Parameters.PREFIX_OPTION + HasSchemaOptions.OPTION_NUMERIC;
	String DESCRIPTION_NUMERIC = "Simplify non-numeric splits into numeric splits";

	String NAME_TARGET_CATEGORIES = "--target-categories";
	String DESCRIPTION_TARGET_CATEGORIES = "Target categories. Defaults to 0-based index [0, 1, .., num_class - 1]";

	String NAME_TARGET_NAME = "--target-name";
	String DESCRIPTION_TARGET_NAME = "Target name. Defaults to \"" + SchemaParameters.DEFAULT_TARGET_NAME + "\"";
	String DEFAULT_TARGET_NAME = "_target";
}

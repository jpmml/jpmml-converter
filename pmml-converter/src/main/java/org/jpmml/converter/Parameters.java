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

public interface Parameters {

	String PREFIX_OPTION = "--X-";

	int BLOCK_IO = 100;
	int BLOCK_SCHEMA = 200;
	int BLOCK_MODEL = 300;
	int BLOCK_SCHEMA_OPTIONS = 400;
	int BLOCK_MODEL_OPTIONS = 500;

	String NAME_HELP = "--help";
	String DESCRIPTION_HELP = "Show the list of configuration options and exit";

	String NAME_PMML_OUTPUT = "--pmml-output";
	String DESCRIPTION_PMML_OUTPUT = "PMML output file";
}

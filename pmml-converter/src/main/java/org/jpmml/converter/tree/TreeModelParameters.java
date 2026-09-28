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
package org.jpmml.converter.tree;

import org.jpmml.converter.ModelParameters;
import org.jpmml.converter.Parameters;

/**
 * @see HasTreeModelOptions
 */
public interface TreeModelParameters extends ModelParameters {

	String NAME_COMPACT = Parameters.PREFIX_OPTION + HasTreeModelOptions.OPTION_COMPACT;
	String DESCRIPTION_COMPACT = "Compact binary splits into multi-way splits";

	String NAME_FLAT = Parameters.PREFIX_OPTION + HasTreeModelOptions.OPTION_FLAT;
	String DESCRIPTION_FLAT = "Flatten nested same-field splits into sibling splits";

	String NAME_NODE_ID = Parameters.PREFIX_OPTION + HasTreeModelOptions.OPTION_NODE_ID;
	String DESCRIPTION_NODE_ID = "Keep node identifiers";

	String NAME_NODE_RECORDCOUNT = Parameters.PREFIX_OPTION + HasTreeModelOptions.OPTION_NODE_RECORDCOUNT;
	String DESCRIPTION_NODE_RECORDCOUNT = "Keep node record counts";

	String NAME_PRUNE = Parameters.PREFIX_OPTION + HasTreeModelOptions.OPTION_PRUNE;
	String DESCRIPTION_PRUNE = "Prune redundant and unreachable splits";
}

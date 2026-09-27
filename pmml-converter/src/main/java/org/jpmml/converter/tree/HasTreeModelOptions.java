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

import java.util.List;

import org.jpmml.converter.HasModelOptions;
import org.jpmml.converter.tree.visitors.TreeModelCompactor;
import org.jpmml.converter.tree.visitors.TreeModelFlattener;
import org.jpmml.converter.tree.visitors.TreeModelPruner;

public interface HasTreeModelOptions extends HasModelOptions {

	/**
	 * @see TreeModelCompactor
	 */
	String OPTION_COMPACT = "compact";

	/**
	 * @see TreeModelFlattener
	 */
	String OPTION_FLAT = "flat";

	String OPTION_NODE_ID = "node_id";

	/**
	 * @see TreeModelPruner
	 */
	String OPTION_PRUNE = "prune";

	List<String> OPTIONS_TREEMODEL_TRANSFORMATIONS = List.of(HasTreeModelOptions.OPTION_COMPACT, HasTreeModelOptions.OPTION_FLAT, HasTreeModelOptions.OPTION_PRUNE);

	List<String> OPTIONS_NODE = List.of(HasTreeModelOptions.OPTION_NODE_ID);
}
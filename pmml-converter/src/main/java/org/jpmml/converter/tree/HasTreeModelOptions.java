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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jpmml.converter.HasModelOptions;
import org.jpmml.converter.HasNativeConfiguration;
import org.jpmml.converter.tree.visitors.NodeIdCleaner;
import org.jpmml.converter.tree.visitors.NodeRecordCountCleaner;
import org.jpmml.converter.tree.visitors.TreeModelCompactor;
import org.jpmml.converter.tree.visitors.TreeModelFlattener;
import org.jpmml.converter.tree.visitors.TreeModelPruner;

public interface HasTreeModelOptions extends HasModelOptions, HasNativeConfiguration {

	/**
	 * @see TreeModelCompactor
	 */
	String OPTION_COMPACT = "compact";

	/**
	 * @see TreeModelFlattener
	 */
	String OPTION_FLAT = "flat";

	/**
	 * @see NodeIdCleaner
	 */
	String OPTION_NODE_ID = "node_id";

	/**
	 * @see NodeRecordCountCleaner
	 */
	String OPTION_NODE_RECORDCOUNT = "node_record_count";

	/**
	 * @see TreeModelPruner
	 */
	String OPTION_PRUNE = "prune";

	List<String> OPTIONS_TREEMODEL_TRANSFORMATIONS = List.of(HasTreeModelOptions.OPTION_COMPACT, HasTreeModelOptions.OPTION_FLAT, HasTreeModelOptions.OPTION_PRUNE);

	List<String> OPTIONS_NODE = List.of(HasTreeModelOptions.OPTION_NODE_ID, HasTreeModelOptions.OPTION_NODE_RECORDCOUNT);

	@Override
	default
	public Map<String, ?> getNativeConfiguration(){
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(HasTreeModelOptions.OPTION_COMPACT, Boolean.FALSE);
		result.put(HasTreeModelOptions.OPTION_FLAT, Boolean.FALSE);
		result.put(HasTreeModelOptions.OPTION_PRUNE, Boolean.FALSE);

		return result;
	}
}
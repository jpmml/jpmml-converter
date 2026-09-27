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

import org.jpmml.converter.tree.visitors.RegressionTreeModelCompactor;
import org.jpmml.converter.tree.visitors.RegressionTreeModelFlattener;
import org.jpmml.converter.tree.visitors.RegressionTreeModelPruner;
import org.jpmml.converter.tree.visitors.TreeModelCompactor;
import org.jpmml.converter.tree.visitors.TreeModelFlattener;
import org.jpmml.converter.tree.visitors.TreeModelPruner;

public class RegressionTreeModelTransformerBuilder extends TreeModelTransformerBuilder {

	@Override
	protected TreeModelCompactor createCompactor(){
		return new RegressionTreeModelCompactor();
	}

	@Override
	protected TreeModelFlattener createFlattener(){
		return new RegressionTreeModelFlattener();
	}

	@Override
	protected TreeModelPruner createPruner(){
		return new RegressionTreeModelPruner();
	}
}
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

import org.jpmml.converter.tree.visitors.ClassificationTreeModelCompactor;
import org.jpmml.converter.tree.visitors.ClassificationTreeModelFlattener;
import org.jpmml.converter.tree.visitors.ClassificationTreeModelPruner;
import org.jpmml.converter.tree.visitors.TreeModelCompactor;
import org.jpmml.converter.tree.visitors.TreeModelFlattener;
import org.jpmml.converter.tree.visitors.TreeModelPruner;

public class ClassificationTreeModelTransformerBuilder extends TreeModelTransformerBuilder {

	@Override
	protected TreeModelCompactor createCompactor(){
		return new ClassificationTreeModelCompactor();
	}

	@Override
	protected TreeModelFlattener createFlattener(){
		return new ClassificationTreeModelFlattener();
	}

	@Override
	protected TreeModelPruner createPruner(){
		return new ClassificationTreeModelPruner();
	}
}
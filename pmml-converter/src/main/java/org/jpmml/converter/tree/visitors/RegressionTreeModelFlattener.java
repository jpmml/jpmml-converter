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
package org.jpmml.converter.tree.visitors;

import org.dmg.pmml.MiningFunction;
import org.dmg.pmml.VisitorAction;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.model.UnsupportedAttributeException;

public class RegressionTreeModelFlattener extends TreeModelFlattener {

	@Override
	public VisitorAction visit(TreeModel treeModel){
		MiningFunction miningFunction = treeModel.requireMiningFunction();

		switch(miningFunction){
			case REGRESSION:
				break;
			case CLASSIFICATION:
				return VisitorAction.SKIP;
			default:
				throw new UnsupportedAttributeException(treeModel, miningFunction);
		}

		return super.visit(treeModel);
	}

	@Override
	protected void mergeNode(Node parentNode, Node node){
		clearScore(parentNode);
		clearScores(parentNode);

		initScore(parentNode, node);
		initScores(parentNode, node);

		replaceChildWithGrandchildren(parentNode, node);
	}
}
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
package org.jpmml.converter.visitors;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.dmg.pmml.False;
import org.dmg.pmml.Predicate;
import org.dmg.pmml.tree.Node;

public class TreeModelPruner extends AbstractTreeModelTransformer {

	@Override
	public void enterNode(Node node){
		Object defaultChild = node.getDefaultChild();

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			for(Iterator<Node> it = children.iterator(); it.hasNext(); ){
				Node child = it.next();

				if(defaultChild != null && equalsNode(defaultChild, child)){
					continue;
				}

				Predicate predicate = child.getPredicate();

				if(predicate instanceof False){
					it.remove();
				}
			}
		}
	}

	@Override
	public void exitNode(Node node){
		Predicate predicate = node.getPredicate();

		if(node.hasNodes()){
			List<Node> children = node.getNodes();

			if(children.size() == 1){
				Node child = children.get(0);

				Predicate childPredicate = child.getPredicate();

				if(Objects.equals(predicate, childPredicate)){
					node.setDefaultChild(null);

					initScore(node, child);
					initDefaultChild(node, child);
					replaceChildWithGrandchildren(node, child);
				}
			}
		}
	}
}
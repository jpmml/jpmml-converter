/*
 * Copyright (c) 2019 Villu Ruusmann
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

import org.dmg.pmml.tree.BranchNode;
import org.dmg.pmml.tree.Node;

public class CountingBranchNode extends BranchNode {

	private Double recordCount = null;


	public CountingBranchNode(){
	}

	public CountingBranchNode(Node node){
		setId(node.getId());
		setScore(node.getScore());
		setRecordCount(node.getRecordCount());
		setDefaultChild(node.getDefaultChild());
		setPredicate(node.getPredicate());

		if(node.hasNodes()){
			(getNodes()).addAll(node.getNodes());
		}
	}

	@Override
	public Double getRecordCount(){
		return this.recordCount;
	}

	@Override
	public CountingBranchNode setRecordCount(Double recordCount){
		this.recordCount = recordCount;

		return this;
	}
}
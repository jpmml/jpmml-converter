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

import java.util.ArrayList;
import java.util.List;

import org.dmg.pmml.ScoreDistribution;
import org.dmg.pmml.tree.Node;
import org.dmg.pmml.tree.SimpleNode;

public class ClassifierNode extends SimpleNode {

	private Object id = null;

	private Double recordCount = null;

	private Object defaultChild = null;

	private List<ScoreDistribution> scoreDistributions = null;

	private List<Node> nodes = null;


	public ClassifierNode(){
	}

	public ClassifierNode(Node node){
		setId(node.getId());
		setScore(node.getScore());
		setRecordCount(node.getRecordCount());
		setDefaultChild(node.getDefaultChild());
		setPredicate(node.getPredicate());

		if(node.hasScoreDistributions()){
			(getScoreDistributions()).addAll(node.getScoreDistributions());
		} // End if

		if(node.hasNodes()){
			(getNodes()).addAll(node.getNodes());
		}
	}

	@Override
	public Object getId(){
		return this.id;
	}

	@Override
	public ClassifierNode setId(Object id){
		this.id = id;

		return this;
	}

	@Override
	public Double getRecordCount(){
		return this.recordCount;
	}

	@Override
	public ClassifierNode setRecordCount(Double recordCount){
		this.recordCount = recordCount;

		return this;
	}

	@Override
	public Object getDefaultChild(){
		return this.defaultChild;
	}

	@Override
	public ClassifierNode setDefaultChild(Object defaultChild){
		this.defaultChild = defaultChild;

		return this;
	}

	@Override
	public boolean hasScoreDistributions(){
		return (this.scoreDistributions != null) && (this.scoreDistributions.size() > 0);
	}

	@Override
	public List<ScoreDistribution> getScoreDistributions(){

		if(this.scoreDistributions == null){
			this.scoreDistributions = new ArrayList<>();
		}

		return this.scoreDistributions;
	}

	@Override
	public boolean hasNodes(){
		return (this.nodes != null) && (this.nodes.size() > 0);
	}

	@Override
	public List<Node> getNodes(){

		if(this.nodes == null){
			this.nodes = new ArrayList<>();
		}

		return this.nodes;
	}
}
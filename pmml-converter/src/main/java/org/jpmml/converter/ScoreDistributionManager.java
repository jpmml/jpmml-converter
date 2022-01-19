/*
 * Copyright (c) 2018 Villu Ruusmann
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

import java.util.List;

import org.dmg.pmml.HasScoreDistributions;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.ScoreDistribution;
import org.jpmml.model.PMMLObjectCache;

public class ScoreDistributionManager {

	private PMMLObjectCache<ScoreDistribution> cache = new PMMLObjectCache<>();


	public <E extends PMMLObject & HasScoreDistributions<E>> void addScoreDistributions(E object, List<?> values, double[] recordCounts){
		List<ScoreDistribution> scoreDistributions = object.getScoreDistributions();

		for(int i = 0; i < values.size(); i++){
			Object value = values.get(i);
			double recordCount = recordCounts[i];

			ScoreDistribution scoreDistribution = createScoreDistribution(value, recordCount);

			scoreDistributions.add(scoreDistribution);
		}
	}

	public ScoreDistribution createScoreDistribution(Object value, double recordCount){
		ScoreDistribution scoreDistribution = new ScoreDistribution()
			.setValue(value)
			.setRecordCount(ValueUtil.narrow(recordCount));

		return intern(scoreDistribution);
	}

	public ScoreDistribution intern(ScoreDistribution scoreDistribution){
		return this.cache.intern(scoreDistribution);
	}
}
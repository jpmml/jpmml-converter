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
import org.dmg.pmml.ScoreFrequency;
import org.dmg.pmml.ScoreProbability;
import org.jpmml.model.PMMLObjectCache;

public class ScoreDistributionManager {

	private PMMLObjectCache<ScoreDistribution> cache = new PMMLObjectCache<>();


	public <E extends PMMLObject & HasScoreDistributions<E>> void addScoreDistributions(E object, List<?> values, List<? extends Number> recordCounts, List<? extends Number> probabilities){
		List<ScoreDistribution> scoreDistributions = object.getScoreDistributions();

		for(int i = 0; i < values.size(); i++){
			Object value = values.get(i);
			Number recordCount = (recordCounts != null ? recordCounts.get(i) : null);

			ScoreDistribution scoreDistribution;

			if(probabilities != null){
				Number probability = probabilities.get(i);

				scoreDistribution = createScoreProbability(value, recordCount, probability);
			} else

			{
				scoreDistribution = createScoreFrequency(value, recordCount);
			}

			scoreDistributions.add(scoreDistribution);
		}
	}

	public ScoreDistribution createScoreFrequency(Object value, Number recordCount){
		ScoreDistribution scoreDistribution = new ScoreFrequency(value, recordCount);

		return intern(scoreDistribution);
	}

	public ScoreDistribution createScoreProbability(Object value, Number recordCount, Number probability){
		ScoreDistribution scoreDistribution = new ScoreProbability(value, recordCount, probability);

		return intern(scoreDistribution);
	}

	public ScoreDistribution intern(ScoreDistribution scoreDistribution){
		return this.cache.intern(scoreDistribution);
	}

	static
	public <E extends Comparable<E>> int indexOfMax(List<E> values){
		int result = -1;

		E maxValue = null;

		for(int i = 0; i < values.size(); i++){
			E value = values.get(i);

			if(maxValue == null || value.compareTo(maxValue) > 0){
				result = i;

				maxValue = value;
			}
		}

		return result;
	}
}
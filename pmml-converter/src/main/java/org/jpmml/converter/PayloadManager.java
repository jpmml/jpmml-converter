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

import org.dmg.pmml.ComplexScoreDistribution;
import org.dmg.pmml.HasScoreDistributions;
import org.dmg.pmml.HasScores;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.Score;
import org.dmg.pmml.ScoreDistribution;
import org.dmg.pmml.ScoreFrequency;
import org.dmg.pmml.ScoreProbability;
import org.jpmml.model.PMMLObjectCache;

public class PayloadManager {

	private PMMLObjectCache<ScoreDistribution> scoreDistributionCache = new PMMLObjectCache<>();

	private PMMLObjectCache<Score> scoreCache = new PMMLObjectCache<>();


	public <E extends PMMLObject & HasScoreDistributions<E>> void addScoreDistributions(E object, List<? extends Number> recordCounts, List<? extends Number> probabilities, CategoricalLabel categoricalLabel){
		List<ScoreDistribution> scoreDistributions = object.getScoreDistributions();

		String targetField = null;
		List<?> values = categoricalLabel.getValues();

		for(int i = 0; i < values.size(); i++){
			Object value = values.get(i);
			Number recordCount = (recordCounts != null ? recordCounts.get(i) : null);

			ScoreDistribution scoreDistribution;

			if(probabilities != null){
				Number probability = probabilities.get(i);

				scoreDistribution = createScoreProbability(targetField, value, recordCount, probability);
			} else

			{
				scoreDistribution = createScoreFrequency(targetField, value, recordCount);
			}

			scoreDistributions.add(scoreDistribution);
		}
	}

	public ScoreDistribution createScoreFrequency(Object value, Number recordCount){
		return createScoreFrequency(null, value, recordCount);
	}

	public ScoreDistribution createScoreFrequency(String targetField, Object value, Number recordCount){
		ScoreDistribution scoreDistribution;

		if(targetField != null){
			scoreDistribution = new ComplexScoreDistribution(value, recordCount)
				.setTargetField(targetField);
		} else

		{
			scoreDistribution = new ScoreFrequency(value, recordCount);
		}

		return intern(scoreDistribution);
	}

	public ScoreDistribution createScoreProbability(Object value, Number recordCount, Number probability){
		return createScoreProbability(null, value, recordCount, probability);
	}

	public ScoreDistribution createScoreProbability(String targetField, Object value, Number recordCount, Number probability){
		ScoreDistribution scoreDistribution;

		if(targetField != null){
			scoreDistribution = new ComplexScoreDistribution(value, recordCount)
				.setProbability(probability)
				.setTargetField(targetField);
		} else

		{
			scoreDistribution = new ScoreProbability(value, recordCount, probability);
		}

		return intern(scoreDistribution);
	}

	public <E extends PMMLObject & HasScores<E>> void addScores(E object, List<?> values, MultiLabel multiLabel){
		List<Score> scores = object.getScores();

		List<ScalarLabel> scalarLabels = multiLabel.toScalarLabels();

		if(scalarLabels.size() != values.size()){
			throw new IllegalArgumentException();
		}

		for(int i = 0; i < scalarLabels.size(); i++){
			ScalarLabel scalarLabel = scalarLabels.get(i);
			Object value = values.get(i);

			String targetField = scalarLabel.getName();

			Score score = createScore(targetField, value);

			scores.add(score);
		}
	}

	public Score createScore(String targetField, Object value){
		Score score = new Score(targetField, value);

		return intern(score);
	}

	public ScoreDistribution intern(ScoreDistribution scoreDistribution){
		return this.scoreDistributionCache.intern(scoreDistribution);
	}

	public Score intern(Score score){
		return this.scoreCache.intern(score);
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
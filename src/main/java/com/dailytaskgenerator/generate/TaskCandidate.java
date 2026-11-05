package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.model.DailyTask;
import lombok.Value;

@Value
public class TaskCandidate
{
	DailyTask task;
	/**
	 * Relative selection weight within its provider; must be positive.
	 */
	double weight;
}

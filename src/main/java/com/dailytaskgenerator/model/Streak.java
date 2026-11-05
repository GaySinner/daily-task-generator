package com.dailytaskgenerator.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consecutive UTC days on which every daily task was finished.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Streak
{
	private int current;
	private int best;
	/**
	 * Last day every task was finished, as an ISO date; null if never.
	 */
	private String lastDate;

	/**
	 * Records that every task was finished on {@code today}. Recording the same day twice is a no-op.
	 */
	public Streak record(String today)
	{
		if (today.equals(lastDate))
		{
			return this;
		}
		boolean continues = lastDate != null && LocalDate.parse(lastDate).plusDays(1).toString().equals(today);
		int next = continues ? current + 1 : 1;
		return new Streak(next, Math.max(best, next), today);
	}

	/**
	 * The streak as of {@code today}: still alive if the last finished day was today or yesterday.
	 */
	public int currentAsOf(String today)
	{
		if (lastDate == null)
		{
			return 0;
		}
		LocalDate last = LocalDate.parse(lastDate);
		LocalDate now = LocalDate.parse(today);
		return last.equals(now) || last.plusDays(1).equals(now) ? current : 0;
	}
}

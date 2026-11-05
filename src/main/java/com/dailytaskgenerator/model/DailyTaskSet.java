package com.dailytaskgenerator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One account's tasks for one UTC day.
 */
@Data
@NoArgsConstructor
public class DailyTaskSet
{
	/**
	 * ISO-8601 UTC date, e.g. "2026-11-05".
	 */
	private String date;
	private int rerollCount;
	private List<DailyTask> tasks = new ArrayList<>();

	public long completedCount()
	{
		return tasks.stream().filter(DailyTask::isDone).count();
	}

	public DailyTaskSet copy()
	{
		DailyTaskSet s = new DailyTaskSet();
		s.date = date;
		s.rerollCount = rerollCount;
		s.tasks = tasks.stream().map(DailyTask::copy).collect(Collectors.toList());
		return s;
	}
}

package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.CustomTask;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Offers tasks the user imported from their own file. They are completed by ticking them off.
 */
@Singleton
public class CustomTaskProvider implements TaskProvider
{
	private final DailyTaskGeneratorConfig config;
	private volatile List<CustomTask> tasks = Collections.emptyList();

	@Inject
	CustomTaskProvider(DailyTaskGeneratorConfig config)
	{
		this.config = config;
	}

	public void setTasks(List<CustomTask> tasks)
	{
		this.tasks = List.copyOf(tasks);
	}

	public int taskCount()
	{
		return tasks.size();
	}

	@Override
	public boolean isEnabled()
	{
		return config.customTasks();
	}

	@Override
	public double categoryWeight()
	{
		return config.customTaskWeight();
	}

	@Override
	public List<TaskCandidate> candidates(PlayerSnapshot snapshot)
	{
		List<TaskCandidate> out = new ArrayList<>();
		for (CustomTask t : tasks)
		{
			out.add(new TaskCandidate(buildTask(t), t.getWeight()));
		}
		return out;
	}

	static DailyTask buildTask(CustomTask custom)
	{
		DailyTask task = new DailyTask();
		task.setId("custom:" + custom.getTitle().toLowerCase());
		task.setType(TaskType.CUSTOM);
		task.setTitle(custom.getTitle());
		task.setDescription(custom.getDescription());
		task.setWikiPage(custom.getWikiPage());
		return task;
	}
}

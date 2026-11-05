package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.DiaryRegion;
import com.dailytaskgenerator.data.DiaryTier;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Suggests the lowest unfinished tier of each region's diary. The game doesn't expose individual
 * diary tasks or their requirements, so tasks are per tier.
 */
@Singleton
public class DiaryTaskProvider implements TaskProvider
{
	private final DailyTaskGeneratorConfig config;

	@Inject
	DiaryTaskProvider(DailyTaskGeneratorConfig config)
	{
		this.config = config;
	}

	@Override
	public boolean isEnabled()
	{
		return config.diaryTasks();
	}

	@Override
	public List<TaskCandidate> candidates(PlayerSnapshot snapshot)
	{
		if (!snapshot.isMembers())
		{
			return Collections.emptyList();
		}
		int maxTier = config.diaryMaxTier().ordinal();
		List<TaskCandidate> out = new ArrayList<>();
		for (DiaryRegion region : DiaryRegion.values())
		{
			for (DiaryTier tier : DiaryTier.values())
			{
				if (tier.ordinal() > maxTier)
				{
					break;
				}
				if (!snapshot.isDiaryTierComplete(region, tier))
				{
					// Easier tiers are more likely, as with combat achievements.
					out.add(new TaskCandidate(buildTask(region, tier), maxTier - tier.ordinal() + 1));
					break;
				}
			}
		}
		return out;
	}

	static DailyTask buildTask(DiaryRegion region, DiaryTier tier)
	{
		DailyTask task = new DailyTask();
		task.setId("diary:" + region.name() + ":" + tier.name());
		task.setType(TaskType.DIARY);
		task.setDiaryRegion(region);
		task.setDiaryTier(tier);
		task.setWikiPage(region.getWikiPage());
		task.setTitle("Complete the " + region.getDisplayName() + " " + tier.getDisplayName() + " diary");
		task.setDescription("Achievement diary · work on any of its tasks");
		return task;
	}
}

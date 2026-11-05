package com.dailytaskgenerator.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

public class TaskGeneratorTest
{
	private final TaskGenerator generator = new TaskGenerator(List.of(new SkillTaskProvider(SkillTaskProviderTest.config(false))));
	private final PlayerSnapshot snapshot = TestSnapshots.allSkillsAt(50, true);

	private static List<String> ids(List<DailyTask> tasks)
	{
		return tasks.stream().map(DailyTask::getId).collect(Collectors.toList());
	}

	@Test
	public void sameSeedSameTasks()
	{
		long seed = TaskGenerator.seed(42L, "2026-11-05", 0);
		assertEquals(ids(generator.generate(snapshot, 4, seed)), ids(generator.generate(snapshot, 4, seed)));
	}

	@Test
	public void differentDayDifferentTasks()
	{
		// Not guaranteed for any single pair of days, so compare a week's worth.
		Set<List<String>> seen = new java.util.HashSet<>();
		for (int d = 1; d <= 7; d++)
		{
			seen.add(ids(generator.generate(snapshot, 4, TaskGenerator.seed(42L, "2026-11-0" + d, 0))));
		}
		assertTrue(seen.size() > 1);
	}

	@Test
	public void noDuplicatesAndRespectsCount()
	{
		List<DailyTask> tasks = generator.generate(snapshot, 6, 1L);
		assertEquals(6, tasks.size());
		assertEquals(6, ids(tasks).stream().distinct().count());
	}

	@Test
	public void pickExcludesIds()
	{
		List<DailyTask> first = generator.generate(snapshot, 4, 1L);
		Set<String> exclude = Set.copyOf(ids(first));
		List<DailyTask> more = generator.pick(snapshot, 4, exclude, new Random(2L));
		more.forEach(t -> assertTrue(!exclude.contains(t.getId())));
	}

	@Test
	public void runsOutGracefully()
	{
		PlayerSnapshot f2p = TestSnapshots.allSkillsAt(50, false);
		assertEquals(SkillTaskProvider.FREE_TO_PLAY_SKILLS.size(), generator.generate(f2p, 50, 3L).size());
	}

	@Test
	public void seedDependsOnRerollCount()
	{
		assertNotEquals(TaskGenerator.seed(42L, "2026-11-05", 0), TaskGenerator.seed(42L, "2026-11-05", 1));
	}
}

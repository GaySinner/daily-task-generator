package com.dailytaskgenerator.generate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import com.dailytaskgenerator.data.CustomTask;
import com.dailytaskgenerator.model.TaskType;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class CustomTaskProviderTest
{
	private static CustomTaskProvider provider(int weight)
	{
		CustomTaskProvider p = new CustomTaskProvider(new DailyTaskGeneratorConfig()
		{
			@Override
			public int customTaskWeight()
			{
				return weight;
			}
		});
		p.setTasks(List.of(new CustomTask("Do a thing", null, 1, null), new CustomTask("Do another", "desc", 2, "Barrows")));
		return p;
	}

	@Test
	public void buildsCustomTasksWithStableIds()
	{
		List<TaskCandidate> c = provider(3).candidates(TestSnapshots.allSkillsAt(50, true));
		assertEquals(2, c.size());
		assertEquals("custom:do a thing", c.get(0).getTask().getId());
		assertEquals(TaskType.CUSTOM, c.get(0).getTask().getType());
		assertEquals(2, c.get(1).getWeight(), 0);
		assertEquals("Barrows", c.get(1).getTask().getWikiPage());
	}

	@Test
	public void higherCategoryWeightPicksCustomMoreOften()
	{
		PlayerSnapshot snapshot = TestSnapshots.allSkillsAt(50, true);
		SkillTaskProvider skills = new SkillTaskProvider(SkillTaskProviderTest.config(false));
		int light = customPicks(new TaskGenerator(List.of(skills, provider(1))), snapshot);
		int heavy = customPicks(new TaskGenerator(List.of(skills, provider(10))), snapshot);
		assertTrue(heavy > light);
	}

	private static int customPicks(TaskGenerator gen, PlayerSnapshot snapshot)
	{
		int n = 0;
		Random random = new Random(7);
		for (int i = 0; i < 500; i++)
		{
			n += (int) gen.pick(snapshot, 1, java.util.Set.of(), random).stream()
				.filter(t -> t.getType() == TaskType.CUSTOM).count();
		}
		return n;
	}
}

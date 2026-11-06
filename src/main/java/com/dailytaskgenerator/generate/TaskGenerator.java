package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Picks tasks from providers with a seeded {@link Random}, so the same account, day and reroll
 * count always give the same tasks for the same player state.
 */
public class TaskGenerator
{
	private final Collection<TaskProvider> providers;

	public TaskGenerator(Collection<TaskProvider> providers)
	{
		this.providers = providers;
	}

	/**
	 * Seeds must be well spread: {@link Random}'s first outputs barely change between nearby seeds,
	 * so seeds one reroll apart would otherwise keep picking the same category.
	 */
	public static long seed(long accountHash, String date, int rerollCount)
	{
		long h = mix(accountHash);
		h = mix(h ^ date.hashCode());
		return mix(h ^ rerollCount);
	}

	/**
	 * SplitMix64 finalizer.
	 */
	private static long mix(long z)
	{
		z += 0x9E3779B97F4A7C15L;
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	public List<DailyTask> generate(PlayerSnapshot snapshot, int count, long seed)
	{
		return pick(snapshot, count, Set.of(), new Random(seed));
	}

	/**
	 * Picks up to {@code count} tasks whose ids are not in {@code excludeIds}.
	 */
	public List<DailyTask> pick(PlayerSnapshot snapshot, int count, Set<String> excludeIds, Random random)
	{
		List<List<TaskCandidate>> pools = new ArrayList<>();
		Map<List<TaskCandidate>, Double> poolWeights = new IdentityHashMap<>();
		for (TaskProvider provider : providers)
		{
			if (!provider.isEnabled())
			{
				continue;
			}
			List<TaskCandidate> pool = provider.candidates(snapshot).stream()
				.filter(c -> !excludeIds.contains(c.getTask().getId()))
				.collect(Collectors.toCollection(ArrayList::new));
			if (!pool.isEmpty())
			{
				pools.add(pool);
				poolWeights.put(pool, Math.max(provider.categoryWeight(), 0.01));
			}
		}

		List<DailyTask> out = new ArrayList<>();
		while (out.size() < count && !pools.isEmpty())
		{
			// Each category with candidates left gets a chance by its weight; empty ones drop out.
			List<TaskCandidate> pool = weightedPool(pools, poolWeights, random);
			TaskCandidate chosen = weightedRemove(pool, TaskCandidate::getWeight, random);
			out.add(chosen.getTask());
			if (pool.isEmpty())
			{
				pools.remove(pool);
			}
		}
		return out;
	}

	private static List<TaskCandidate> weightedPool(List<List<TaskCandidate>> pools,
		Map<List<TaskCandidate>, Double> weights, Random random)
	{
		// Equal weights take the plain path so default behaviour is unchanged.
		boolean equal = pools.stream().map(weights::get).distinct().count() == 1;
		if (equal)
		{
			return pools.get(random.nextInt(pools.size()));
		}
		return weightedPick(pools, weights::get, random);
	}

	private static <T> T weightedPick(List<T> items, ToDoubleFunction<T> weight, Random random)
	{
		double total = 0;
		for (T item : items)
		{
			total += weight.applyAsDouble(item);
		}
		double r = random.nextDouble() * total;
		for (T item : items)
		{
			r -= weight.applyAsDouble(item);
			if (r < 0)
			{
				return item;
			}
		}
		return items.get(items.size() - 1);
	}

	private static <T> T weightedRemove(List<T> items, ToDoubleFunction<T> weight, Random random)
	{
		double total = 0;
		for (T item : items)
		{
			total += weight.applyAsDouble(item);
		}
		double r = random.nextDouble() * total;
		for (int i = 0; i < items.size(); i++)
		{
			r -= weight.applyAsDouble(items.get(i));
			if (r < 0)
			{
				return items.remove(i);
			}
		}
		return items.remove(items.size() - 1);
	}
}

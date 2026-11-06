package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.List;

public interface TaskProvider
{
	boolean isEnabled();

	/**
	 * How likely this provider's category is to be picked relative to others; 1 is the default.
	 */
	default double categoryWeight()
	{
		return 1;
	}

	/**
	 * All tasks the player could be given right now. Must not touch {@code Client}.
	 */
	List<TaskCandidate> candidates(PlayerSnapshot snapshot);
}

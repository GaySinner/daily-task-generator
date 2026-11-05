package com.dailytaskgenerator.generate;

import com.dailytaskgenerator.player.PlayerSnapshot;
import java.util.List;

public interface TaskProvider
{
	boolean isEnabled();

	/**
	 * All tasks the player could be given right now. Must not touch {@code Client}.
	 */
	List<TaskCandidate> candidates(PlayerSnapshot snapshot);
}

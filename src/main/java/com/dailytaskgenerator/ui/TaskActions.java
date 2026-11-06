package com.dailytaskgenerator.ui;

/**
 * Callbacks from the panel. Invoked on the Swing thread.
 */
public interface TaskActions
{
	void setManuallyCompleted(String taskId, boolean done);

	void reroll(String taskId);

	void regenerate();

	void openSettings();

	void importCustomTasks();
}

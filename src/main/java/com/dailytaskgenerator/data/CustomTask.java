package com.dailytaskgenerator.data;

import lombok.Value;

/**
 * One task from the user's imported file.
 */
@Value
public class CustomTask
{
	String title;
	/**
	 * May be null.
	 */
	String description;
	/**
	 * Relative weight among custom tasks; positive.
	 */
	double weight;
	/**
	 * OSRS Wiki page or search term for the task's link; null for no link. Only set from JSON files.
	 */
	String wikiPage;
}

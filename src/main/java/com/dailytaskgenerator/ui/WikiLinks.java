package com.dailytaskgenerator.ui;

import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.TaskType;
import javax.annotation.Nullable;
import okhttp3.HttpUrl;

final class WikiLinks
{
	private static final HttpUrl WIKI_BASE = HttpUrl.get("https://oldschool.runescape.wiki");

	private WikiLinks()
	{
	}

	/**
	 * Skills link straight to their page. Quest and monster names don't always match the wiki's
	 * page titles exactly, so those go through the wiki search, which jumps to the page on an exact
	 * match and otherwise shows the closest results.
	 */
	@Nullable
	static String url(DailyTask task)
	{
		String term = term(task);
		if (term == null || term.isEmpty())
		{
			return null;
		}
		HttpUrl.Builder url = WIKI_BASE.newBuilder().addPathSegment("w");
		if (task.getType() == TaskType.SKILL)
		{
			url.addPathSegment(term);
		}
		else
		{
			url.addPathSegment("Special:Search").addQueryParameter("search", term);
		}
		return url.addQueryParameter("utm_source", "runelite").build().toString();
	}

	@Nullable
	private static String term(DailyTask task)
	{
		if (task.getWikiPage() != null)
		{
			return task.getWikiPage();
		}
		// Tasks saved before wiki links existed.
		if (task.getSkill() != null)
		{
			return task.getSkill().getName();
		}
		if (task.getQuest() != null)
		{
			return task.getQuest().getName();
		}
		return task.getTitle();
	}
}

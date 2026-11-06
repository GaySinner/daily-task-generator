package com.dailytaskgenerator.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.Test;

public class CustomTaskParserTest
{
	private static List<CustomTask> parse(String text) throws IOException
	{
		return CustomTaskParser.parse(new BufferedReader(new StringReader(text)), new Gson());
	}

	@Test
	public void parsesTitleDescriptionAndWeight() throws IOException
	{
		List<CustomTask> tasks = parse("# my list\n\nDo a Barrows run | Bring food | 2.5\nPlant trees\n");
		assertEquals(2, tasks.size());
		assertEquals("Do a Barrows run", tasks.get(0).getTitle());
		assertEquals("Bring food", tasks.get(0).getDescription());
		assertEquals(2.5, tasks.get(0).getWeight(), 0);
		assertEquals("Plant trees", tasks.get(1).getTitle());
		assertNull(tasks.get(1).getDescription());
		assertEquals(1, tasks.get(1).getWeight(), 0);
	}

	@Test
	public void badWeightFallsBackToDefault() throws IOException
	{
		assertEquals(1, parse("A | b | lots").get(0).getWeight(), 0);
		assertEquals(1, parse("A | b | -3").get(0).getWeight(), 0);
	}

	@Test
	public void skipsDuplicatesAndEmptyTitles() throws IOException
	{
		assertEquals(1, parse("Task\ntask\n | no title\n").size());
	}

	@Test
	public void capsTaskCount() throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < CustomTaskParser.MAX_TASKS + 50; i++)
		{
			sb.append("task ").append(i).append('\n');
		}
		assertEquals(CustomTaskParser.MAX_TASKS, parse(sb.toString()).size());
	}

	@Test
	public void parsesJson() throws IOException
	{
		List<CustomTask> tasks = parse("[{\"title\":\"Barrows\",\"description\":\"Food\",\"weight\":2,\"wikiPage\":\"Barrows\"},"
			+ "{\"title\":\"Bare\"}, {\"description\":\"no title\"}, 5, {\"title\":\"bare\"}]");
		assertEquals(2, tasks.size());
		assertEquals("Barrows", tasks.get(0).getWikiPage());
		assertEquals(2, tasks.get(0).getWeight(), 0);
		assertNull(tasks.get(1).getDescription());
		assertNull(tasks.get(1).getWikiPage());
		assertEquals(1, tasks.get(1).getWeight(), 0);
	}

	@Test
	public void jsonWithLeadingBomAndBadWeight() throws IOException
	{
		List<CustomTask> tasks = parse("\uFEFF  [{\"title\":\"A\",\"weight\":\"heavy\"},{\"title\":\"B\",\"weight\":-1}]");
		assertEquals(2, tasks.size());
		assertEquals(1, tasks.get(0).getWeight(), 0);
		assertEquals(1, tasks.get(1).getWeight(), 0);
	}

	@Test(expected = IOException.class)
	public void invalidJsonIsAnError() throws IOException
	{
		parse("[{\"title\": ");
	}
}

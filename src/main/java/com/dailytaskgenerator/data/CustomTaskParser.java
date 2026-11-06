package com.dailytaskgenerator.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses a task file in one of two formats, chosen by its first non-blank character.
 * <ul>
 * <li>JSON, if it starts with {@code [}: an array of objects with {@code title} and optional
 * {@code description}, {@code weight} and {@code wikiPage}.</li>
 * <li>Otherwise plain text: one task per line as {@code title | description | weight}. The
 * description and weight are optional; blank lines and lines starting with {@code #} are skipped.</li>
 * </ul>
 */
public final class CustomTaskParser
{
	public static final int MAX_TASKS = 500;
	private static final int MAX_CHARS = 1_000_000;
	private static final int MAX_TITLE = 100;
	private static final int MAX_DESCRIPTION = 200;
	private static final int MAX_WIKI_PAGE = 100;

	private CustomTaskParser()
	{
	}

	/**
	 * @throws IOException if the file can't be read, or looks like JSON but isn't valid
	 */
	public static List<CustomTask> parse(BufferedReader reader, Gson gson) throws IOException
	{
		String text = readAll(reader);
		if (text.replace("﻿", "").trim().startsWith("["))
		{
			return parseJson(text.replace("﻿", ""), gson);
		}
		return parseText(text);
	}

	private static String readAll(BufferedReader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		char[] buf = new char[8192];
		int n;
		while (sb.length() < MAX_CHARS && (n = reader.read(buf)) != -1)
		{
			sb.append(buf, 0, n);
		}
		return sb.toString();
	}

	static List<CustomTask> parseText(String text) throws IOException
	{
		Collector out = new Collector();
		BufferedReader lines = new BufferedReader(new StringReader(text));
		String line;
		while ((line = lines.readLine()) != null && !out.full())
		{
			out.add(parseLine(line));
		}
		return out.tasks;
	}

	static List<CustomTask> parseJson(String text, Gson gson) throws IOException
	{
		JsonElement root;
		try
		{
			root = gson.fromJson(text, JsonElement.class);
		}
		catch (JsonParseException e)
		{
			throw new IOException("Invalid JSON", e);
		}
		if (root == null || !root.isJsonArray())
		{
			throw new IOException("Expected a JSON array");
		}
		Collector out = new Collector();
		for (JsonElement el : root.getAsJsonArray())
		{
			if (out.full())
			{
				break;
			}
			if (el.isJsonObject())
			{
				out.add(fromObject(el.getAsJsonObject()));
			}
		}
		return out.tasks;
	}

	private static CustomTask fromObject(JsonObject o)
	{
		double weight = 1;
		if (o.has("weight") && o.get("weight").isJsonPrimitive() && o.get("weight").getAsJsonPrimitive().isNumber())
		{
			weight = o.get("weight").getAsDouble();
		}
		return build(string(o, "title"), string(o, "description"), weight, string(o, "wikiPage"));
	}

	private static String string(JsonObject o, String key)
	{
		JsonElement e = o.get(key);
		return e != null && e.isJsonPrimitive() ? e.getAsString() : null;
	}

	static CustomTask parseLine(String line)
	{
		line = line.replace("﻿", "").trim();
		if (line.isEmpty() || line.startsWith("#"))
		{
			return null;
		}
		String[] parts = line.split("\\|", -1);
		double weight = 1;
		if (parts.length > 2)
		{
			try
			{
				weight = Double.parseDouble(parts[2].trim());
			}
			catch (NumberFormatException ignored)
			{
				// Keep the default weight.
			}
		}
		return build(parts[0], parts.length > 1 ? parts[1] : null, weight, null);
	}

	/**
	 * Trims and limits the fields; null if there is no usable title.
	 */
	private static CustomTask build(String title, String description, double weight, String wikiPage)
	{
		title = clean(title, MAX_TITLE);
		if (title == null)
		{
			return null;
		}
		if (!(weight > 0 && weight <= 1000))
		{
			weight = 1;
		}
		return new CustomTask(title, clean(description, MAX_DESCRIPTION), weight, clean(wikiPage, MAX_WIKI_PAGE));
	}

	private static String clean(String s, int max)
	{
		if (s == null)
		{
			return null;
		}
		s = s.trim();
		if (s.isEmpty())
		{
			return null;
		}
		return s.length() > max ? s.substring(0, max) : s;
	}

	/**
	 * Accumulates tasks, dropping nulls and duplicate titles.
	 */
	private static final class Collector
	{
		final List<CustomTask> tasks = new ArrayList<>();
		private final Set<String> seen = new HashSet<>();

		boolean full()
		{
			return tasks.size() >= MAX_TASKS;
		}

		void add(CustomTask task)
		{
			if (task != null && seen.add(task.getTitle().toLowerCase()))
			{
				tasks.add(task);
			}
		}
	}
}

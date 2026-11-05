package com.dailytaskgenerator.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

public class StreakTest
{
	@Test
	public void firstDayStartsAtOne()
	{
		Streak s = new Streak().record("2026-11-05");
		assertEquals(1, s.getCurrent());
		assertEquals(1, s.getBest());
	}

	@Test
	public void consecutiveDaysExtend()
	{
		Streak s = new Streak().record("2026-11-05").record("2026-11-06").record("2026-11-07");
		assertEquals(3, s.getCurrent());
		assertEquals(3, s.getBest());
	}

	@Test
	public void sameDayTwiceIsNoOp()
	{
		Streak s = new Streak().record("2026-11-05");
		assertSame(s, s.record("2026-11-05"));
	}

	@Test
	public void gapResetsButKeepsBest()
	{
		Streak s = new Streak().record("2026-11-05").record("2026-11-06").record("2026-11-09");
		assertEquals(1, s.getCurrent());
		assertEquals(2, s.getBest());
	}

	@Test
	public void crossesMonthAndYearBoundaries()
	{
		assertEquals(2, new Streak().record("2026-11-30").record("2026-12-01").getCurrent());
		assertEquals(2, new Streak().record("2026-12-31").record("2027-01-01").getCurrent());
	}

	@Test
	public void currentAsOfExpiresAfterAMissedDay()
	{
		Streak s = new Streak().record("2026-11-05").record("2026-11-06");
		assertEquals(2, s.currentAsOf("2026-11-06"));
		// Still alive the next day, until that day ends unfinished.
		assertEquals(2, s.currentAsOf("2026-11-07"));
		assertEquals(0, s.currentAsOf("2026-11-08"));
		assertEquals(0, new Streak().currentAsOf("2026-11-08"));
	}
}

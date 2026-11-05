package com.dailytaskgenerator.notify;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompletionMessage
{
	OFF("Off", false, false),
	CHAT("Chat message", true, false),
	POPUP("Pop-up", false, true),
	BOTH("Chat and pop-up", true, true);

	private final String displayName;
	private final boolean chat;
	private final boolean popup;

	@Override
	public String toString()
	{
		return displayName;
	}
}

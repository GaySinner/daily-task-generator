package com.dailytaskgenerator.notify;

import com.dailytaskgenerator.DailyTaskGeneratorConfig;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.WidgetNode;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetModalMode;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

/**
 * Tells the player about completed tasks with a chat message and/or the game's own pop-up banner
 * (the one used for collection log items). Both are client-side only; nothing is sent to the server.
 * All methods must be called on the client thread.
 */
@Slf4j
@Singleton
public class TaskNotifier
{
	/**
	 * notification_display_init(title, text, colour): fills in the notification interface and starts
	 * its animation. Not in ScriptID; the animation scripts it runs are ScriptID.NOTIFICATION_START
	 * and NOTIFICATION_DELAY.
	 */
	private static final int NOTIFICATION_DISPLAY_INIT = 3343;
	/**
	 * Use the interface's default colour.
	 */
	private static final int DEFAULT_COLOUR = -1;
	/**
	 * Frames to wait for the banner to appear before treating the pop-up as broken.
	 */
	private static final int MAX_FRAMES_TO_APPEAR = 100;

	private final Client client;
	private final ClientThread clientThread;
	private final ChatMessageManager chatMessageManager;
	private final DailyTaskGeneratorConfig config;

	private final Deque<String[]> pendingPopups = new ArrayDeque<>();
	private WidgetNode showing;
	/**
	 * Set if the pop-up failed once, so the rest of the session falls back to chat.
	 */
	private boolean popupBroken;

	@Inject
	TaskNotifier(Client client, ClientThread clientThread, ChatMessageManager chatMessageManager, DailyTaskGeneratorConfig config)
	{
		this.client = client;
		this.clientThread = clientThread;
		this.chatMessageManager = chatMessageManager;
		this.config = config;
	}

	public void taskCompleted(String taskTitle, long done, int total)
	{
		notify("Daily Task Complete!", "Daily task complete: ", taskTitle + " (" + done + "/" + total + ")");
	}

	public void allCompleted(int streak)
	{
		String days = streak == 1 ? "1 day" : streak + " days";
		notify("Daily Tasks Complete!", "All daily tasks complete! ", "Streak: " + days);
	}

	private void notify(String popupTitle, String chatPrefix, String text)
	{
		CompletionMessage mode = config.completionMessage();
		boolean popup = mode.isPopup() && !popupBroken;
		if (mode.isChat() || (mode.isPopup() && popupBroken))
		{
			chat(chatPrefix, text);
		}
		if (popup)
		{
			pendingPopups.add(new String[]{popupTitle, text, chatPrefix});
		}
	}

	private void chat(String prefix, String text)
	{
		String message = new ChatMessageBuilder()
			.append(ChatColorType.HIGHLIGHT)
			.append(prefix)
			.append(ChatColorType.NORMAL)
			.append(text)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(message)
			.build());
	}

	/**
	 * Shows the next queued pop-up once the notification slot is free, including of the game's own
	 * pop-ups. Call once per game tick.
	 */
	public void tick()
	{
		if (showing != null || pendingPopups.isEmpty())
		{
			return;
		}
		int component = notificationComponent();
		if (component == -1)
		{
			return;
		}
		if (client.getComponentTable().get(component) != null)
		{
			// The game is showing its own notification; ours waits its turn.
			return;
		}

		String[] next = pendingPopups.poll();
		try
		{
			WidgetNode node = client.openInterface(component, InterfaceID.NOTIFICATION_DISPLAY, WidgetModalMode.MODAL_CLICKTHROUGH);
			showing = node;
			client.runScript(NOTIFICATION_DISPLAY_INIT, next[0], next[1], DEFAULT_COLOUR);
			closeWhenFinished(node, next);
		}
		catch (RuntimeException e)
		{
			log.debug("Couldn't show notification pop-up; using chat messages instead", e);
			closeShowing();
			fallBackToChat(next);
		}
	}

	private void closeWhenFinished(WidgetNode node, String[] message)
	{
		int[] frames = {0};
		boolean[] appeared = {false};
		clientThread.invokeLater(() ->
		{
			if (showing != node)
			{
				return true;
			}
			Widget container = client.getWidget(InterfaceID.NotificationDisplay.CONTAINER);
			if (container != null && container.getWidth() > 0)
			{
				// Still animating or on screen.
				appeared[0] = true;
				return false;
			}
			if (!appeared[0] && ++frames[0] < MAX_FRAMES_TO_APPEAR)
			{
				return false;
			}
			closeShowing();
			if (!appeared[0])
			{
				log.debug("Notification pop-up never appeared; using chat messages instead");
				fallBackToChat(message);
			}
			return true;
		});
	}

	private void fallBackToChat(String[] message)
	{
		popupBroken = true;
		chat(message[2], message[1]);
		while (!pendingPopups.isEmpty())
		{
			String[] queued = pendingPopups.poll();
			chat(queued[2], queued[1]);
		}
	}

	private void closeShowing()
	{
		if (showing != null)
		{
			WidgetNode node = showing;
			showing = null;
			client.closeInterface(node, true);
		}
	}

	/**
	 * The current layout's notification slot, or -1 if this layout doesn't have one.
	 */
	private int notificationComponent()
	{
		switch (client.getTopLevelInterfaceId())
		{
			case InterfaceID.TOPLEVEL:
				return InterfaceID.Toplevel.NOTIFICATIONS;
			case InterfaceID.TOPLEVEL_OSRS_STRETCH:
				return InterfaceID.ToplevelOsrsStretch.NOTIFICATIONS;
			case InterfaceID.TOPLEVEL_PRE_EOC:
				return InterfaceID.ToplevelPreEoc.NOTIFICATIONS;
			case InterfaceID.TOPLEVEL_DISPLAY:
				return InterfaceID.ToplevelDisplay.NOTIFICATIONS;
			case InterfaceID.TOPLEVEL_OSM:
				return InterfaceID.ToplevelOsm.NOTIFICATIONS;
			default:
				return -1;
		}
	}

	/**
	 * Drops queued pop-ups and closes ours if it's open, e.g. on logout or shutdown.
	 */
	public void reset()
	{
		pendingPopups.clear();
		closeShowing();
	}
}

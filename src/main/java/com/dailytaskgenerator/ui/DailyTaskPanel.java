package com.dailytaskgenerator.ui;

import com.dailytaskgenerator.model.DailyTask;
import com.dailytaskgenerator.model.DailyTaskSet;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.SwingUtil;

/**
 * Sidebar panel. All methods must be called on the Swing thread.
 */
public class DailyTaskPanel extends PluginPanel
{
	private final TaskActions actions;
	private final JLabel summary = new JLabel();
	private final JLabel subtitle = new JLabel();
	private final JLabel streakLabel = new JLabel();
	private final JPanel taskList = new JPanel(new GridBagLayout());
	private final JButton regenerate = new JButton("Regenerate all");

	public DailyTaskPanel(TaskActions actions)
	{
		this.actions = actions;
		setLayout(new BorderLayout(0, 8));
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel title = new JLabel("Daily Tasks");
		title.setFont(FontManager.getRunescapeBoldFont());
		summary.setFont(FontManager.getRunescapeSmallFont());
		subtitle.setFont(FontManager.getRunescapeSmallFont());
		subtitle.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		streakLabel.setFont(FontManager.getRunescapeSmallFont());
		streakLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		BufferedImage gear = ImageUtil.loadImageResource(DailyTaskPanel.class, "/com/dailytaskgenerator/settings.png");
		JButton settings = new JButton(new ImageIcon(gear));
		SwingUtil.removeButtonDecorations(settings);
		settings.setRolloverIcon(new ImageIcon(ImageUtil.alphaOffset(gear, -80)));
		settings.setToolTipText("Open plugin settings");
		settings.addActionListener(e -> actions.openSettings());

		JPanel titleRow = new JPanel(new BorderLayout());
		titleRow.add(title, BorderLayout.WEST);
		titleRow.add(settings, BorderLayout.EAST);

		JPanel header = new JPanel(new GridLayout(0, 1));
		header.add(titleRow);
		header.add(summary);
		header.add(subtitle);
		header.add(streakLabel);
		add(header, BorderLayout.NORTH);

		add(taskList, BorderLayout.CENTER);

		regenerate.setToolTipText("Replace all unfinished tasks (uses one reroll)");
		regenerate.addActionListener(e -> actions.regenerate());
		add(regenerate, BorderLayout.SOUTH);

		showLoggedOut();
	}

	private static String days(int n)
	{
		return n == 1 ? "1 day" : n + " days";
	}

	public void showLoggedOut()
	{
		summary.setText("Log in to generate tasks.");
		subtitle.setText(" ");
		streakLabel.setText(" ");
		regenerate.setVisible(false);
		taskList.removeAll();
		revalidate();
		repaint();
	}

	public void showTasks(DailyTaskSet set, int rerollsLeft, int streak, int bestStreak)
	{
		streakLabel.setText("Streak: " + days(streak) + (bestStreak > streak ? " · best " + days(bestStreak) : ""));
		int total = set.getTasks().size();
		summary.setText(set.completedCount() + " / " + total + " complete");
		subtitle.setText(set.getDate() + " · resets 00:00 UTC · " + rerollsLeft + " reroll" + (rerollsLeft == 1 ? "" : "s") + " left");

		taskList.removeAll();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 6, 0);

		if (total == 0)
		{
			JLabel empty = new JLabel("<html>No eligible tasks. Enable more task types in the plugin settings.</html>", SwingConstants.CENTER);
			empty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			taskList.add(empty, c);
		}
		for (DailyTask task : set.getTasks())
		{
			taskList.add(new TaskCard(task, rerollsLeft > 0, actions), c);
			c.gridy++;
		}

		boolean anyOpen = set.getTasks().stream().anyMatch(t -> !t.isDone());
		regenerate.setVisible(true);
		regenerate.setEnabled(rerollsLeft > 0 && (anyOpen || total == 0));

		revalidate();
		repaint();
	}
}

package com.dailytaskgenerator.ui;

import com.dailytaskgenerator.model.DailyTask;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

class TaskCard extends JPanel
{
	private static final Color DONE_COLOR = new Color(0x7CCB6E);

	TaskCard(DailyTask task, boolean canReroll, TaskActions actions)
	{
		setLayout(new BorderLayout(0, 4));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 3, 0, 0, task.getType().getColor()),
			BorderFactory.createEmptyBorder(6, 8, 6, 8)));

		JLabel type = new JLabel(task.getType().getDisplayName().toUpperCase());
		type.setFont(FontManager.getRunescapeSmallFont());
		type.setForeground(task.getType().getColor());

		JLabel title = new JLabel(html(task.getTitle()));
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(task.isDone() ? DONE_COLOR : Color.WHITE);

		JPanel header = new JPanel(new GridLayout(0, 1));
		header.setOpaque(false);
		header.add(type);
		header.add(title);
		if (task.getDescription() != null)
		{
			JLabel desc = new JLabel(html(task.getDescription()));
			desc.setFont(FontManager.getRunescapeSmallFont());
			desc.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			header.add(desc);
		}
		add(header, BorderLayout.NORTH);

		if (task.hasProgress())
		{
			JProgressBar bar = new JProgressBar(0, task.getTargetXp());
			bar.setValue(task.getProgressXp());
			bar.setStringPainted(true);
			NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
			bar.setString(nf.format(task.getProgressXp()) + " / " + nf.format(task.getTargetXp()));
			bar.setForeground(task.isDone() ? DONE_COLOR : task.getType().getColor());
			bar.setBackground(ColorScheme.DARK_GRAY_COLOR);
			bar.setPreferredSize(new Dimension(0, 16));
			add(bar, BorderLayout.CENTER);
		}

		JPanel footer = new JPanel(new BorderLayout());
		footer.setOpaque(false);

		JCheckBox done = new JCheckBox("Done", task.isDone());
		done.setOpaque(false);
		// A task the game confirmed can't be un-ticked; it would just complete again.
		done.setEnabled(!task.isCompleted());
		done.setToolTipText(task.isCompleted() ? "Completed automatically" : "Mark as done");
		done.addActionListener(e -> actions.setManuallyCompleted(task.getId(), done.isSelected()));
		footer.add(done, BorderLayout.WEST);

		JButton reroll = new JButton("Reroll");
		reroll.setEnabled(canReroll && !task.isDone());
		reroll.setToolTipText("Replace this task with a different one");
		reroll.addActionListener(e -> actions.reroll(task.getId()));
		footer.add(reroll, BorderLayout.EAST);

		add(footer, BorderLayout.SOUTH);
	}

	private static String html(String text)
	{
		String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		return "<html>" + escaped + "</html>";
	}
}

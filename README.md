# Daily Task Generator

A RuneLite plugin that gives you a fresh set of tasks each day, chosen to suit the character you're logged in on.

## Task types

| Type | Example | Chosen from | Completes when |
|---|---|---|---|
| Skill | *Gain 20,000 Fishing XP*, *Reach 62 Agility* | Your skills, favouring lower ones by default. The XP goal grows with your level and is capped by the **Effort** setting. | You gain the XP. Progress made on another client or on mobile counts too. |
| Quest | *Complete Dragon Slayer I*, *Continue The Dig Site* | Unfinished quests and miniquests whose listed requirements you meet. Quests you've already started come up more often. | The quest is finished. |
| Diary | *Complete the Ardougne Easy diary* | The lowest unfinished tier in each region, up to the **Hardest tier** setting. | The game marks that tier complete. |
| Combat | *Noxious Foe* (Easy) | Combat achievement tasks you haven't done, up to the **Hardest tier** setting. Easier tiers come up more often. | The game marks the task complete. |
| Custom | *Do a Barrows run* | Tasks you import from your own file, boosted by the **Custom task weight** setting. | You tick it off. |

Each task card has a **Wiki** link to the skill, quest, diary or monster page, and a **Done** box for ticking tasks off yourself.

## Custom tasks

Click **Import custom tasks** in the side panel and pick a `.txt` or `.json` file. The tasks are saved in your RuneLite config, so the file is only read once. New tasks can appear from your next reroll or the next day.

Plain text, one task per line as `title | description | weight`. Description and weight are optional; blank lines and lines starting with `#` are ignored.

```
# my list
Do a Barrows run | Bring food | 2
Plant a tree
```

JSON, an array of objects. Only `title` is required; `wikiPage` adds a Wiki link to the card.

```json
[
  { "title": "Do a Barrows run", "description": "Bring food", "weight": 2, "wikiPage": "Barrows" }
]
```

A task's weight is how likely it is relative to your other custom tasks (default 1). The **Custom task weight** setting controls how much more likely custom tasks are than any other task type.

## How a day works

- Tasks reset at **00:00 UTC**, the same time as the game's daily reset.
- Each account, and each world type such as seasonal, gets its own list. The list stays the same across logins and client restarts.
- **Reroll** replaces one unfinished task. **Regenerate all** replaces every unfinished task. Both use up the day's rerolls (the **Rerolls per day** setting).
- Finishing every task in a day adds to your **streak**, which is shown at the top of the panel.
- When a task completes, the plugin can show a chat message, the game's pop-up banner (like a new collection log item), both, or nothing. Only you see these. Tasks you tick yourself are only announced if **Announce ticked tasks** is on.
- The gear icon in the panel opens the plugin's settings.

## Data and limits

- All requirement and task data comes from the game client's cache, so it stays current with game updates. The plugin makes no network requests and sends nothing to any server.
- Quest requirements cover skills, quest points and prerequisite quests. Requirements that aren't in the game's quest data, such as items or kudos, aren't checked.
- Diary tasks are a whole tier rather than single tasks. The game doesn't expose its individual diary tasks or their requirements.
- Diary and combat achievement tasks are only offered on members worlds.
- The pop-up uses the game's own notification banner. If it can't be shown, the plugin falls back to chat messages for the rest of the session.

## Development

```
./gradlew build   # compile and run unit tests
./gradlew run     # start a RuneLite development client with the plugin loaded
```

To log in to the development client with a Jagex account, follow [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## License

BSD 2-Clause. See [LICENSE](LICENSE).

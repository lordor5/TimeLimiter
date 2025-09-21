# TimeLimiter

You know that one person who's always on the server? Yeah, we all know who we're talking about. Whether they're hogging resources or just lurking around, TimeLimiter is here to save the day. By managing access to the server based on playtime, everyone has the same amount of fun.

This fork aims to limit the time while giving every one the same overall amount of time to play and give the players freedom to play whenever they want to. In my servers, the majority of players play during weekends but some players prefer playing during the week, so limiting the amount of time by day will not work for me because players that play more often will have more time that players that can play much longer but fewer days.

This plugin works by having 2 counters:

- Global timer: tracks the maximum of time players can play and will be updated every hour
- Per player timer: tracks how much time the player has been playing

If the player counter exceeds the global timer, the player will be kicked

This way, every one can manage his play time how they want and still have the same play time as every body else

## Installation

Make sure you have [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) installed.

1. Clone the repository:
2. Navigate to the project directory:
3. Compile the project:
   ```cmd
   set PATH=%PATH%;C:\Users\hugob\Downloads\gradle-9.0.0\bin
   gradle build
   ```

### Config

- `timePerHour` is the minutes that every player gets every hour 2.5min/hour is 1h per day
- `timeLimit` can be modified to start with some time in minutes

### Comands

- /check will show your time and remaining time
- /check player will show the time and remaining time of a player
- /settime player minutes will update the played time of a player (needs timelimiter.settime)

commands can be executed from the server panel or Minecraft

## Contributing

This is a fork of https://github.com/JeffreyWangDev/TimeLimiter and vibe coded to fit my needs. I'm not proud of it but it works and that's all I need.

If you want to contribute, go ahead, open a pull request.

## License

This project is licensed under the MIT License.

---

If you encounter any issues or have questions, feel free to open an issue on GitHub.

Happy Time Managing!

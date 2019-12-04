# 'Suspicion!' AI Agent

## Authors
- Keegan Saunders
- Juan Becerra
- Emanuel Hernández
- Sean Mullarkey

## Overview
Suspicion is a board game where players must navigate rooms, collect gems, and attempt to guess the identity of other teams. In this 
CS 457: Artificial Intelligence project, we are tasked with creating an AI agent that makes intelligent moves using its percepts. Our 
goal is to get a higher score than agents that make random guesses/moves, and to beat other intelligent agents a portion of the time.

## Compilation/Running
To compile the program, navigate to src and run:
<br>
<code>
$-> javac *.java
</code>
<br>
Then, follow this usage pattern:
<br>
<code>
$-> java Suspicion [-display text|gui][-tournament x][-delay x][-loadplayer player.class][-loadplayers list_name.txt]
</code>
<br>
Or, simply use this command to run and get a simple output (This pins our agent against 7 dumb agents):
<br>
<code>
$-> java Suspicion -loadplayers dumbplayers.txt
</code>
<br>

## Approach
// TODO

## Challenges
// TODO

## Testing
// TODO

## Results
// TODO

## Notes
- Almost all of our work will be in RBot.java
- Take the gem color that you have the least of! This will immediately increase performance against RBotDumb.class

## References
- Rules: http://www.boardgamecapital.com/game_rules/suspicion.pdf
- Game Review: http://www.geekyhobbies.com/suspicion-2016-wonder-forge-board-game-review-and-rules/

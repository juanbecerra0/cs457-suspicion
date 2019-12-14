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
For this project, we had a few goals set for making our agent perform better within this environment. This is what we 
have attempted to implement:

- Pick best dice move
- Pick best card action (move)
- Pick best card action (get)
- Pick best card action (ask)
- Pick most likely identities at end of the game

### Dice and Card Movement
These two sections were combined as the logic for choosing where to move is very similar.

To find the best move for adjacent and anywhere moves, we call getBestMoveAdjacent and getBestMoveAnywhere respectively.
In addition, we call a helper method getVisiblePieceCount, which returns how many players can see you (by row and col) 
given an x and y coordinate. Both adjacent and anywhere methods work very similarly, but where anywhere checks every slot 
on the board for the optimal spot to move (move card), adjacent only checks adjacent positions to the current location (dice).

Our logic follows this: If we are moving a player other than ourselves, we want to move the piece to a more "isolated" position,
or a position where the player is viewed by fewer people. If we are moving ourselves, we want to move to more populated areas.
This way, when we ask players or we are asked, it is easier to deduct who other players are and harder for other players to 
deduct where we are. While moving other players to more isolated positions won't help us too much (as there are several other players
moving other players), we found that moving ourself to more populated areas slightly increased our win rates (around 3%-6% from given
agent).

### Picking Gems
In the case where we get to select a gem where our piece is currently placed, we used a combination of two ideas:

- Pick the gem that we have the least of (so we can get bonus points for having sets of 3 gems)
- Pick the gem that has the most players currently on (so it is difficult to deduct our identity to other agents)

Obviously, these two ideas don't always result in the same gem selection, so we used a scoring system for each gem. The pseudocode is as follows:

{gemColor}Score = (OurTotalGems / {gemColor}Count) * ({gemColor}StandingOn / totalStandingOn)

This combination approach resulted in significantly better results (around 10% - 15%).

### Asking the best visible character
// TODO

### Picking the correct agent identities
// TODO

## Challenges
// TODO

## Testing
// TODO

## Results
// TODO

## References
- Rules: http://www.boardgamecapital.com/game_rules/suspicion.pdf
- Game Review: http://www.geekyhobbies.com/suspicion-2016-wonder-forge-board-game-review-and-rules/

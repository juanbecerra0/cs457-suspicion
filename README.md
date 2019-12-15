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

### Asking the best player if they can see a piece
Initially, for finding the best player to ask if they can see a piece, we used a simple approach of 
selecting the player we know the least about. This meant iterating through the list of other players 
and choosing the one with the highest possible guest name count. This certainly isn't the most optimal way to
make this decision, but it doesn't hurt to ask questions about players we know little about. This indeed yields 
better scores.

Later, we decided to also determine where the most isolated piece was, and we guessed who this piece may be using this function:

// TODO may change when we figure out entropy
isolationScore = 1 / possibleGuestNamesSum

Of course, this is only taken into consideration when the isolated piece is within the domain of possible guest names of a 
player. Otherwise, the score is recorded as 1 / the length of the list of other players.

With both methods returning a score, we rated each player based off of this function:

chooseScore = isolationScore * ambiguityScore

... and chose the player with the highest score.

### Picking the correct agent identities
By this point, we came to a realization. Our bot was guessing the identities of dumb-bots nearly 100% of the time 
without having to modify our final guess at all. Our current optimizations were already cutting down other player domains 
to a single character by the end of games. In a test against 7 dumb bots and 1000 iterations, meaning...

7 bots * 7 points per guess * 1000 tests = 49000 possible points

... we almost always got a perfect score of 49000.

// TODO should we bother changing shit?

## Challenges
Initially, accessing our agent's percepts was one of the most challenging aspects of this assignment. We needed access 
attributes like the current board state, our gem count, opponent info, and our knowledge base. We weren't sure if we were 
running an older version of the class source code, but we ended up developing a lot of work-arounds and helper functions 
for accessing these attributes. For example, to keep count of our current gems, the int[] gemCount attribute never seemed 
to actually get updated, so we instead created integer variables to keep track of them, and we incremented the right ones 
by parsing our getPlayerActions() output string before we returned. Other examples include parsing the input board string 
from getPlayerActions() and iterating through key-value pairs of hashmaps, like the "pieces" and "players" attributes.

As somewhat novice players to the Suspicion board game, a lot of the decisions our agent made were based off of what we 
would do given our little experience. When playing the game for the first time in class, we often wanted to isolate other 
pieces while keeping our own piece with several other agents in view. In addition, we were always trying to balance the gems 
that we were collecting in order to cash in on the bonus points for sets of three gems, but we were also trying to not seem 
suspicios to other players by selecting gem colors that few other players had. We're not sure of these are the most "optimal" 
strategies, but over all, they seemed to help us win significantly more games than our initial random approach.

We weren't able to fully implement some features relating to entropy as we could not get them fully working. That being 
said, we're pretty proud of some of the probability and information theoretic-based approaches, namely our approach to picking 
gems and picking the best people to ask. We beleive that, while we probably could have made better inferences, our system 
of taking several "get-closer-to-winning" decisions and combining their probabilities into a final score really helped us 
make some great decisions, especially in making inferences for other players' identities.

## Testing
// TODO

## Results
// TODO

## References
- Rules: http://www.boardgamecapital.com/game_rules/suspicion.pdf
- Game Review: http://www.geekyhobbies.com/suspicion-2016-wonder-forge-board-game-review-and-rules/

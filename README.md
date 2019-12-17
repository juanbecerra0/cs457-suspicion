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
These were the results of running our shell script "Tournaments.sh" (actual results may vary):

Dumb players Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBonk.class0,4900,6197,11097,91
RBotDumb.class7,2982,5902,8884,3
RBotDumb.class3,2954,5888,8842,2
RBotDumb.class1,3024,5883,8907,1
RBotDumb.class2,2961,5776,8737,1
RBotDumb.class6,2905,5922,8827,1
RBotDumb.class5,2912,5771,8683,1
RBotDumb.class4,3031,5906,8937,0


players S1 Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBotS1.class7,4886,6042,10928,17
RBotS1.class6,4886,6095,10981,16
RBotS1.class5,4893,5975,10868,14
RBotS1.class3,4886,6003,10889,12
RBonk.class0,4893,5962,10855,12
RBotS1.class4,4893,6057,10950,11
RBotS1.class1,4886,5981,10867,9
RBotS1.class2,4886,5964,10850,9


players S4 Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBonk.class0,4900,6217,11117,30
RBotS4.class1,4669,5885,10554,13
RBotS4.class2,4599,5873,10472,12
RBotS4.class7,4627,5850,10477,12
RBotS4.class6,4494,5955,10449,10
RBotS4.class5,4578,5824,10402,9
RBotS4.class4,4557,5812,10369,8
RBotS4.class3,4466,5806,10272,6


players S5 Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBonk.class0,4900,6063,10963,37
RBotS5.class1,4438,5842,10280,14
RBotS5.class6,4270,5843,10113,12
RBotS5.class3,4396,5778,10174,10
RBotS5.class5,4410,5845,10255,8
RBotS5.class7,4452,5807,10259,7
RBotS5.class2,4382,5795,10177,7
RBotS5.class4,4319,5877,10196,5


players S7 Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBonk.class0,4893,6554,11447,100
RBotS7.class7,3780,3876,7656,0
RBotS7.class3,3850,3800,7650,0
RBotS7.class5,3976,3798,7774,0
RBotS7.class4,3850,3840,7690,0
RBotS7.class6,3843,3842,7685,0
RBotS7.class2,3808,3824,7632,0
RBotS7.class1,3780,3903,7683,0


players V4 Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBotV4.class2,4900,6692,11592,16
RBotV4.class6,4900,6682,11582,15
RBotV4.class1,4900,6586,11486,15
RBonk.class0,4900,6470,11370,13
RBotV4.class3,4900,6656,11556,11
RBotV4.class4,4900,6646,11546,11
RBotV4.class5,4900,6621,11521,10
RBotV4.class7,4900,6558,11458,9


players Tournament
Current tournament results...
PlayerName,GuessScore,GemScore,TotalScore,wins
RBotV4.class7,4900,6280,11180,31
RBotS1.class2,4774,6259,11033,24
RBonk.class0,4900,6221,11121,22
RBotS4.class3,4536,5907,10443,12
RBotS5.class4,4487,5955,10442,8
RBotS6.class5,4361,5713,10074,3
RBotS7.class6,3640,3381,7021,0
RBotDumb.class1,2779,5894,8673,0
Each Tournament is out of 100 games

## Results
Our agent performs very well against dumb bots (and by extension, student 7) as it wins 
the vast majority of games. We also consistently beat some of the other provided agents. Against,
S1 and V4, we play reasonably well, landing some place in the middle among all of our tests. We 
likely could have gained better results with a more optimal way of calculating entropy and 
information gain with some of our decisions, but overall, we're happy with the results of 
our agent.

## References
- Rules: http://www.boardgamecapital.com/game_rules/suspicion.pdf
- Game Review: http://www.geekyhobbies.com/suspicion-2016-wonder-forge-board-game-review-and-rules/

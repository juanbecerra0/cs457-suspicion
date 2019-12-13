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
- Pick best 

## Challenges
// TODO

## Testing
// TODO

## Results
// TODO

## Notes

### 12/4
- Almost all of our work will be in RBot.java
- Take the gem color that you have the least of! This will immediately increase performance against RBotDumb.class

### 12/5
- Guests (The players on the board)
    - "Buford Barnswallow"
    - "Earl of Volesworthy"
    - "Mildred Wellington"
    - "Nadia Bwalya",
    - "Viola Chung"
    - "Dr. Ashraf Najem"
    - "Remy La Rocque"
    - "Lily Nesbit"
    - "Trudie Mudge"
    - "Stefano Laconi"
- Card Actions (Draw random card, do an action)
    - "get,yellow:ask,Remy La Rocque,"
    - "get,:viewDeck"
    - "get,red:ask,Nadia Bwalya,"
    - "get,green:ask,Lily Nesbit,"
    - "viewDeck:ask,Buford Barnswallow,"
    - "get,red:ask,Earl of Volesworthy,"
    - "get,:ask,Nadia Bwalya,"
    - "get,green:ask,Stefano Laconi,"
    - "get,yellow:viewDeck"
    - "get,:ask,Dr. Ashraf Najem,"
    - "get,green:viewDeck"
    - "get,red:viewDeck"
    - "get,:ask,Mildred Wellington,"
    - "get,:move,"
    - "get,:ask,Earl of Volesworthy,"
    - "get,:ask,Remy La Rocque,"
    - "viewDeck:ask,Viola Chung,"
    - "get,:ask,Stefano Laconi,"
    - "get,:ask,Viola Chung,"
    - "get,:viewDeck"
    - "get,:ask,Lily Nesbit,"
    - "get,yellow:ask,Mildred Wellington,"
    - "get,:ask,Buford Barnswallow,"
    - "get,:move,"
    - "move,:ask,Dr. Ashraf Najem,"
    - "get,:viewDeck"
    - "get,:ask,Trudie Mudge,"
    - "move,:ask,Trudie Mudge,"
- Dice Actions (Roll both, move the players in adjacent rooms)
    - Dice 1
        - "Buford Barnswallow"
        - "Earl of Volesworthy"
        - "Mildred Wellington"
        - "Viola Chung"
        - "Dr. Ashraf Najem"
        - "?" (anyone you want)
    - Dice 2
        - "Nadia Bwalya"
        - "Remy La Rocque"
        - "Lily Nesbit"
        - "Trudie Mudge"
        - "Stefano Laconi"
        - "?" (anyone you want)

### 12/6
- Deciding moving which player and where would be best
    - Move players in decision tree
        - Sum up who you can see vs who you can't see
        - Choose whichever decision brings greater amount of players to cross out
        - Check what you have originally picked, and pick decision that most reduces your guess domain
            - Check the probability of 'yes' or 'no'
            - For example, (2:3) vs (4:1) can be very subjective (is it worth to choose 4?)
- Picking gems
    - Have an idea of what other player gems are currently on
        - Pick a gem color that does not reveal your identity
        - Pick gem colors that other players are also on (this makes it hard for other agents to detect you)
    - Take personal probability weighted score and ensure that yours' is better than other players
        - I.E. (1/5)(7) where (1/5) is probability of guessing your identity and (7) is what the score is worth
        - Ensure that your gain (say, three points for gem) is better than theoretical agent scores

### 12/11
- New code that checks entropy
- Compare all color assignments for players
    - Count the number of color occurences for each world state
    - For a given player, the largest number for colors is what the agent should most likely consider

## References
- Rules: http://www.boardgamecapital.com/game_rules/suspicion.pdf
- Game Review: http://www.geekyhobbies.com/suspicion-2016-wonder-forge-board-game-review-and-rules/

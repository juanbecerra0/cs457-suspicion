#!/bin/bash

javac *.java
i=0
rval=0

printf "Each Tournament is out of 100 games\n" >> TournamentResults.txt

#for Tournaments for dumb players
printf "\n\nDumb players Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers dumbplayers.txt > output.log
echo "Done with Dumb Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))

#for Tournaments for S1 players
printf "\n\nplayers S1 Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers playersS1.txt > output.log
echo "Done with S1 Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))

#for Tournaments for S4 players
printf "\n\nplayers S4 Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers playersS4.txt > output.log
echo "Done with S4 Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))

#for Tournaments for S5 players
printf "\n\nplayers S5 Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers playersS5.txt > output.log
echo "Done with S5 Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))


#uncomment the next block to play S6, S6 just takes a while to run
: '
#for Tournaments for S6 players
printf "\n\nplayers S6 Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers playersS6.txt > output.log
echo "Done with S6 Players"
tail -n 10 output.log >> TournamentResults.txt
rm -f output.log


i=$((i+1))
'

#for Tournaments for S7 players
printf "\n\nplayers S7 Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers playersS7.txt > output.log
echo "Done with S7 Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))

#for Tournaments for V4 players
printf "\n\nplayers V4 Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers playersV4.txt > output.log
echo "Done with V4 Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))

#for Tournaments for an assortment of random players
printf "\n\nplayers Tournament\n" >> TournamentResults.txt
java Suspicion -tournament 100 -loadplayers players.txt > output.log
echo "Done with Players"
tail -n 10 output.log >> TournamentResults.txt
tail -n 10 output.log >> output.txt
grep -i "RBonk" output.txt > output2.txt
let "rval = rval + $(grep -Eo '[0-9]+$' output2.txt)"
rm -f output.log
rm -f output.txt
rm -f output2.txt

i=$((i+1))

echo "Average: $rval / $(($i * 100))"
echo $cat TournamentResutls.txt
rm -f TournamentResults.txt

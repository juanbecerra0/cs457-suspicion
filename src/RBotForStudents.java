import java.util.*;

/** This is the base class for computer player/bots. 
 * 
 */

public class RBotForStudents extends Bot
{
    Random r = new Random();
    HashMap<String, Piece> pieces; // Keyed off of guest name
    Board board;
    Piece me;
    HashMap<String, Player> players; // Keyed off of player name
    String otherPlayerNames[];
    TextDisplay display;

    int[] gemCounts = new int[3];

    public static class Board
    {
        public Room rooms[][];
        public String gemLocations;

        public class Room
        {
            public final boolean gems[] = new boolean[3];
            public final String[] availableGems;
            public final int row;
            public final int col;
            private HashMap<String, Piece> pieces;

            public void removePlayer(Piece piece)
            {
                removePlayer(piece.name);
                piece.col=-1;
                piece.row=-1;
            }

            public void removePlayer(String name)
            {
                pieces.remove(name);
            }
            
            public void addPlayer(Piece piece)
            {
                piece.col=this.col;
                piece.row=this.row;
                pieces.put(piece.name, piece);
            }

            public Room(boolean red, boolean green, boolean yellow, int row, int col)
            {
                pieces = new HashMap<String, Piece>();
                this.row = row;
                this.col = col;
                gems[Suspicion.RED]=red;
                gems[Suspicion.GREEN]=green;
                gems[Suspicion.YELLOW]=yellow;
                String temp="";
                if(red) temp += "red,";
                if(green) temp += "green,";
                if(yellow) temp += "yellow,";
                availableGems = (temp.substring(0,temp.length()-1)).split(",");
            }
        }

        public void movePlayer(Piece player, int row, int col)
        {
            rooms[player.row][player.col].removePlayer(player);
            rooms[row][col].addPlayer(player);
        }
        
        public void clearRooms()
        {
            rooms=new Room[3][4];
            int x=0, y=0;
            boolean red, green, yellow;
        
            for(String gems:gemLocations.trim().split(":"))
            {
                if(gems.contains("red")) red=true;
                else red=false;
                if(gems.contains("green")) green=true;
                else green=false;
                if(gems.contains("yellow")) yellow=true;
                else yellow=false;
                rooms[x][y] = new Room(red,green,yellow,x,y);
                y++;
                x += y/4;
                y %= 4;
            }
        }

        public Board(String piecePositions, HashMap<String, Piece> pieces, String gemLocations)
        {
            Piece piece;
            this.gemLocations=gemLocations;
            clearRooms();
            int col=0;
            int row=0;
            if(piecePositions==null)
            {
                System.out.println("huh?");
            }
            for(String room:piecePositions.split(":",-1)) // Split out each room
            {
                room = room.trim();
                if(room.length()!=0) for(String guest: room.split(",")) // Split guests out of each room
                {
                    guest = guest.trim();
                    piece = pieces.get(guest);
                    rooms[row][col].addPlayer(piece);
                }
                col++;
                row = row + col/4;
                col = col%4;
            }
        }
    }

    public Piece getPiece(String name)
    {
        return pieces.get(name);
    }

    public class Player
    {
        public String playerName;
        public ArrayList<String> possibleGuestNames;

//Remove guest names that are not in the possibleGuests from this players list of possible guest names 
        public void adjustKnowledge(ArrayList<String> possibleGuests)
        {
            Iterator<String> it = possibleGuestNames.iterator();
            while(it.hasNext())
            {
                String g;
                if(!possibleGuests.contains(g=it.next())) 
                {
                    it.remove();
                }
            }
        }


//@@@ Remove a guest name from the list of possible guests names for this player
        public void adjustKnowledge(String notPossibleGuest)
        {
            Iterator<String> it = possibleGuestNames.iterator();
            while(it.hasNext())
            {
                if(it.next().equals(notPossibleGuest)) 
                {
                    it.remove();
                    break;
                }
            }
        }

        public Player(String name, String[] guests)
        {
            playerName = name;
            possibleGuestNames = new ArrayList<String>();
            for(String g: guests)
            {
                possibleGuestNames.add(g);
            }
        }
    }

    public class Piece
    {
        public int row, col;
        public String name;

        public Piece(String name)
        {
            this.name = name;
        }
    }

    private String[] getPossibleMoves(Piece p)
    {
        LinkedList<String> moves=new LinkedList<String>();
        if(p.row > 0) moves.push((p.row-1) + "," + p.col);
        if(p.row < 2) moves.push((p.row+1) + "," + p.col);
        if(p.col > 0) moves.push((p.row) + "," + (p.col-1));
        if(p.col < 3) moves.push((p.row) + "," + (p.col+1));

        return moves.toArray(new String[moves.size()]);
    }


    public String getPlayerActions(String d1, String d2, String card1, String card2, String board) throws Suspicion.BadActionException
    {
        this.board = new Board(board, pieces, gemLocations);
        String actions = "";

        // Random move for dice1
        if(d1.equals("?")) d1 = guestNames[r.nextInt(guestNames.length)];
        Piece piece = pieces.get(d1);
        String[] moves = getPossibleMoves(piece);
        int movei = r.nextInt(moves.length);
        actions += "move," + d1 + "," + moves[movei];
        this.board.movePlayer(piece, Integer.parseInt(moves[movei].split(",")[0]), Integer.parseInt(moves[movei].split(",")[1])); // Perform the move on my board

        // Random move for dice2
        if(d2.equals("?")) d2 = guestNames[r.nextInt(guestNames.length)];
        piece = pieces.get(d2);
        moves = getPossibleMoves(piece);
        movei = r.nextInt(moves.length);
        actions += ":move," + d2 + "," + moves[movei];
        this.board.movePlayer(piece, Integer.parseInt(moves[movei].split(",")[0]), Integer.parseInt(moves[movei].split(",")[1])); // Perform the move on my board

        // which card
        int i = r.nextInt(2);
        actions += ":play,card"+(i+1);

        String card = i==0?card1:card2;


        for(String cardAction: card.split(":")) // just go ahead and do them in this order
        {
            if(cardAction.startsWith("move")) 
            {
                String guest;
                guest = guestNames[r.nextInt(guestNames.length)];
                piece = pieces.get(guest);
                //moves = getPossibleMoves(piece);
                actions += ":move," + guest + "," + r.nextInt(3) + "," + r.nextInt(4);
            }
            else if(cardAction.startsWith("viewDeck")) 
            {
                actions += ":viewDeck";
            }
            else if(cardAction.startsWith("get")) 
            {
//@@@ You SHOULD replace this with code that optimizes this decision
                if(cardAction.equals("get,")) actions += ":get," + this.board.rooms[me.row][me.col].availableGems[r.nextInt(this.board.rooms[me.row][me.col].availableGems.length)];
                else actions += ":" + cardAction;
            }
            else if(cardAction.startsWith("ask")) 
            {
                actions += ":" + cardAction + otherPlayerNames[r.nextInt(otherPlayerNames.length)]; 
            }
        }
        return actions;
    }

    private int countGems(String gem)
    {
        if(gem.equals("red")) return gemCounts[Suspicion.RED];
        else if(gem.equals("green")) return gemCounts[Suspicion.GREEN];
        else return gemCounts[Suspicion.YELLOW];
    }

//@@@ This function returns a list of guests who are in a room that contains the
//@@@ given gemcolor
    private ArrayList<String> getGuestsInRoomWithGem(String board, String gemcolor)
    {
        Board b = new Board(board, pieces, gemLocations);
        int gem=-1;
        if(gemcolor.equals("yellow")) gem = Suspicion.YELLOW;
        else if(gemcolor.equals("green")) gem = Suspicion.GREEN;
        else if(gemcolor.equals("red")) gem = Suspicion.RED;
        ArrayList<String> possibleGuests = new ArrayList<String>();

        int y=0,x=0;
        for(String guests: board.trim().split(":"))
        {
            //only get people from rooms with the gem
            if(b.rooms[y][x].gems[gem] && guests.trim().length()>0)
            {
                for(String guest:guests.trim().split(","))
                {
                    possibleGuests.add(guest.trim());
                }
            }
            x++;
            y+=x/4;
            x%=4;
        }
        
        return possibleGuests;
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board, String actions)
    {
    }

//@@@ Here we added code to update the possible guest name list for a player if we see that player 
//@@@ play a get gem action card
    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board[], String actions)
    {
        if(player.equals(this.playerName)) return; // If player is me, return
        // Check for a get action and use the info to update player knowledge
        if(cardPlayed.split(":")[0].equals("get,") || cardPlayed.split(":")[1].equals("get,"))
        {
            int splitindex;
            String[] split = actions.split(":");
            String get;
            if(split[3].indexOf("get")>=0) splitindex=3;
            else splitindex=4;
            get=split[splitindex];
            String gem = get.split(",")[1];
            // board[splitIndex+1] will have the state of the board when the gem was taken
            if(board[splitindex]!=null) // This would indicate an error in the action
            {
                ArrayList<String> possibleGuests = getGuestsInRoomWithGem(board[splitindex],gem);
                players.get(player).adjustKnowledge(possibleGuests);

                /*System.out.println("***************************************************************");
                System.out.println("***************************************************************");
                System.out.println("***************************************************************");
                System.out.println(player + " took a gem!");
                System.out.println("People who could take this gem: " + possibleGuests);
                System.out.println("Possible guests: " + players.get(player).possibleGuestNames);
                display.displayBoard(board[splitindex]);
                System.out.println("***************************************************************");
                System.out.println("***************************************************************");
                System.out.println("***************************************************************");*/
            }
        }
    }

    private boolean canSee(Piece p1, Piece p2) // returns whether or not these two pieces see each 
    {
        return (p1.row==p2.row || p1.col == p2.col);
    }

    
    public void answerAsk(String guest, String player, String board, boolean canSee)
    {
        Board b = new Board(board, pieces, gemLocations);
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = pieces.get(guest);  // retrieve the guest 
        for(String k : pieces.keySet())
        {
            Piece p2 = pieces.get(k);
            if((canSee && canSee(p1,p2)) || (!canSee && !canSee(p1,p2))) possibleGuests.add(p2.name);
        }
        //System.out.println("Adjusting knowledge about " + player + " to : " + possibleGuests);
        players.get(player).adjustKnowledge(possibleGuests);
    }

//@@@ added code to adjust our KBs for all players to exclude the player we drew from the guest names deck
    public void answerViewDeck(String player)
    {
        for(String k:players.keySet())
        {
            players.get(k).adjustKnowledge(player);
        }
    }

//@@@ Calculate for each player and each possible guest ID for that player, 
//@@@ the number of valid assignments (or valid world states) where
//@@@ that player can be assigned that particular guest ID
//@@@ 
//@@@ For example, if you have two players with possible guest ids as follows
//@@@ 
//@@@ Fred [red, purple, green]
//@@@ Tom  [red, green]
//@@@ 
//@@@ Then for Fred, the counts are: red=1, purple=2, green=1
//@@@ For Tom the counts are red=2 and green=2
//@@@ 
//@@@ So purple *might* be the most likely color to assign Fred, given that more possible world states exist with that assignment
//@@@ 
//@@@ 
    private int calcGuestNameStats(int p, ArrayList<String> assignedIDs, HashMap<String, HashMap<String, Integer>> guestNameCounts)
    {
        int totalCount=0;
        //@@@ this is the base case for the recursion
        //@@@ if we hit this, we've got a full assignment, so we update the counts on the assigned ids
        if(p>=otherPlayerNames.length)
        {
            totalCount++;
            for(int x=0;x<otherPlayerNames.length;x++)
            {
                //@@@ this increments the counts on the assigned ids
                Integer i = guestNameCounts.get(otherPlayerNames[x]).get(assignedIDs.get(x)) + 1;
                guestNameCounts.get(otherPlayerNames[x]).put(assignedIDs.get(x),i);
            }
        }
        //@@@ This is the recursive part where we try all possible valid combinations of assigning IDs
        else for(String gid : players.get(otherPlayerNames[p]).possibleGuestNames)
        {
            if(!assignedIDs.contains(gid))
            {
                assignedIDs.add(gid);
                totalCount+=calcGuestNameStats(p+1, assignedIDs, guestNameCounts);
                assignedIDs.remove(gid);
            }
        }
        return totalCount;
    }

    private void printGuestNameCounts(HashMap<String, HashMap<String, Integer>> guestNameCounts)
    {
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
        for(String name: otherPlayerNames)
        {
            System.out.print(name);
            for(String gid: players.get(name).possibleGuestNames)
            {
                System.out.print(", "+gid+"="+guestNameCounts.get(name).get(gid));
            }
            System.out.println();
        }
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
    }

    public class GuestNameStats
    {
        public HashMap<String, HashMap<String, Integer>> guestNameCounts;
        public int totalCount;

        public GuestNameStats(HashMap<String, HashMap<String, Integer>> guestNameCounts, int totalCount)
        {
            this.guestNameCounts = guestNameCounts;
            this.totalCount = totalCount;
        }
    }

    private GuestNameStats calcGuestNameStats()
    {
        int totalCount=0;
        /* This is a 2 level hash.  The first level is keyed on otherPlayerNames, and the second level
           is keyed on possibleGuestIDs */
        HashMap<String, HashMap<String, Integer>> guestNameCounts = new HashMap<String, HashMap<String, Integer>>();
        for(String name: otherPlayerNames)
        {
            HashMap<String, Integer> c;
            guestNameCounts.put(name, c = new HashMap<String, Integer>());
            for(String gid: players.get(name).possibleGuestNames)
            {
                c.put(gid, 0);
            }
        }
        totalCount += calcGuestNameStats(0,new ArrayList<String>(), guestNameCounts);

        //System.out.println("TotalCount="+totalCount);

        return new GuestNameStats(guestNameCounts, totalCount);
    }

//@@@ One of the things you can do is remove zero count guest IDs from a players KB
//@@@ So for example, if we have the following
//@@@ 
//@@@ Fred=[red,green,yellow]
//@@@ Tom =[red,green]
//@@@ Nancy=[red,green]
//@@@ 
//@@@ you canuse the counts to remove both red and green from Fred's KB, as there
//@@@ are no valid world states that assign Fred those guest ids.
//@@@ 
    private void cleanUpZeroCountIDs(GuestNameStats stats)
    {
        for(String name:otherPlayerNames)
        {
            //@@@ Retrieve the guest name counts for this player
            HashMap<String, Integer> counts = stats.guestNameCounts.get(name);
            //@@@ Go through it and collect all the guest names that have non-zero counts
            //@@@ then adjust your KB for this player so they only have the non-zero count guest IDs
            //players.get(name).adjustKnowledge(nonzero);
        }
    }

    private void cleanupZeroCountIDs()
    {
        GuestNameStats stats = calcGuestNameStats();
        cleanUpZeroCountIDs(stats);
    }

    public String reportGuesses()
    {
        String rval="";
        for(String k:players.keySet())
        {
            Player p = players.get(k);
            rval += k;
            for(String g: p.possibleGuestNames)
            {
                rval += ","+g;
            }
            rval+=":";
        }
        return rval.substring(0,rval.length()-1);
    }

    public RBotForStudents(String playerName, String guestName, int numStartingGems, String gemLocations, String[] playerNames, String[] guestNames)
    {
        super(playerName, guestName, numStartingGems, gemLocations, playerNames, guestNames);
        display = new TextDisplay(gemLocations);
        pieces = new HashMap<String, Piece>();
        ArrayList<String> possibleGuests = new ArrayList<String>();
        for(String name:guestNames)
        {
            pieces.put(name, new Piece(name));
            if(!name.equals(guestName)) possibleGuests.add(name);
        }
        me = pieces.get(guestName);

        players = new HashMap<String, Player>();
        for(String str: playerNames)
        {
            if(!str.equals(playerName)) players.put(str, new Player(str, possibleGuests.toArray(new String[possibleGuests.size()])));
        }

        otherPlayerNames = players.keySet().toArray(new String[players.size()]);
    }
}



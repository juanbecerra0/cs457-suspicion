import java.util.*;

/**
 * This is the base class for computer player/bots.
 * 
 */

public class RBonk extends Bot {
    Random r = new Random();
    HashMap<String, Piece> pieces; // Keyed off of guest name
    Board board;
    Piece me;
    HashMap<String, Player> players; // Keyed off of player name
    String otherPlayerNames[];
    TextDisplay display;

    int[] gemCounts = new int[3];
    int[][][] gemArray = new int[3][4][3];  // Red, Green, Yellow

    // @@@ Assumes all guest IDs in the player's possible list are equally probable
    private double calcInformationEntropySimple(Player player) {
        return -Math.log(1.0 / player.possibleGuestNames.size()) / Math.log(2.0);
        // return player.possibleGuestNames.size();
    }

    public double calcInformationEntropySimple() {
        double rval = 0.0;
        for (String player : otherPlayerNames) {
            rval += calcInformationEntropySimple(players.get(player));
        }

        return rval;
    }

    private double calcInformationEntropyComplex(String player, GuestNameStats gns) {
        double entropy = 0.0;
        // System.out.println("Calculating entropy for " + player + " " +
        // gns.totalCount);
        for (Integer count : gns.guestNameCounts.get(player).values()) {
            // System.out.print("" + count + ", ");
            if (count <= 0)
                continue;
            entropy += -(((double) count) / gns.totalCount) * Math.log(((double) count) / gns.totalCount)
                    / Math.log(2.0);
        }
        // System.out.println("Entropy = " + entropy);
        return entropy;
    }

    public double calcInformationEntropyComplex(GuestNameStats gns) {
        double rval = 0.0;
        for (String player : otherPlayerNames) {
            rval += calcInformationEntropyComplex(player, gns);
        }

        return rval;
    }

    public static class Board {
        public Room rooms[][];
        public String gemLocations;

        public class Room {
            public final boolean gems[] = new boolean[3];
            public final String[] availableGems;
            public final int row;
            public final int col;
            private HashMap<String, Piece> pieces;

            public void removePlayer(Piece piece) {
                removePlayer(piece.name);
                piece.col = -1;
                piece.row = -1;
            }

            public void removePlayer(String name) {
                pieces.remove(name);
            }

            public void addPlayer(Piece piece) {
                piece.col = this.col;
                piece.row = this.row;
                pieces.put(piece.name, piece);
            }

            public Room(boolean red, boolean green, boolean yellow, int row, int col) {
                pieces = new HashMap<String, Piece>();
                this.row = row;
                this.col = col;
                gems[Suspicion.RED] = red;
                gems[Suspicion.GREEN] = green;
                gems[Suspicion.YELLOW] = yellow;
                String temp = "";
                if (red)
                    temp += "red,";
                if (green)
                    temp += "green,";
                if (yellow)
                    temp += "yellow,";
                availableGems = (temp.substring(0, temp.length() - 1)).split(",");
            }
        }

        public void movePlayer(Piece player, int row, int col) {
            rooms[player.row][player.col].removePlayer(player);
            rooms[row][col].addPlayer(player);
        }

        public void clearRooms() {
            rooms = new Room[3][4];
            int x = 0, y = 0;
            boolean red, green, yellow;

            for (String gems : gemLocations.trim().split(":")) {
                if (gems.contains("red"))
                    red = true;
                else
                    red = false;
                if (gems.contains("green"))
                    green = true;
                else
                    green = false;
                if (gems.contains("yellow"))
                    yellow = true;
                else
                    yellow = false;
                rooms[x][y] = new Room(red, green, yellow, x, y);
                y++;
                x += y / 4;
                y %= 4;
            }
        }

        public Board(String piecePositions, HashMap<String, Piece> pieces, String gemLocations) {
            Piece piece;
            this.gemLocations = gemLocations;
            clearRooms();
            int col = 0;
            int row = 0;
            if (piecePositions == null) {
                System.out.println("huh?");
            }
            for (String room : piecePositions.split(":", -1)) // Split out each room
            {
                room = room.trim();
                if (room.length() != 0)
                    for (String guest : room.split(",")) // Split guests out of each room
                    {
                        guest = guest.trim();
                        piece = pieces.get(guest);
                        rooms[row][col].addPlayer(piece);
                    }
                col++;
                row = row + col / 4;
                col = col % 4;
            }
        }
    }

    public Piece getPiece(String name) {
        return pieces.get(name);
    }

    public class Player {
        public String playerName;
        public ArrayList<String> possibleGuestNames;

        // Remove guest names that are not in the possibleGuests from this players list
        // of possible guest names
        public void adjustKnowledge(ArrayList<String> possibleGuests) {
            Iterator<String> it = possibleGuestNames.iterator();
            while (it.hasNext()) {
                String g;
                if (!possibleGuests.contains(g = it.next())) {
                    it.remove();
                }
            }
        }

        // @@@ Remove a guest name from the list of possible guests names for this
        // player
        public void adjustKnowledge(String notPossibleGuest) {
            Iterator<String> it = possibleGuestNames.iterator();
            while (it.hasNext()) {
                if (it.next().equals(notPossibleGuest)) {
                    it.remove();
                    break;
                }
            }
        }

        public Player(String name, String[] guests) {
            playerName = name;
            possibleGuestNames = new ArrayList<String>();
            for (String g : guests) {
                possibleGuestNames.add(g);
            }
        }
    }

    public class Piece {
        public int row, col;
        public String name;

        public Piece(String name) {
            this.name = name;
        }
    }

    private String[] getPossibleMoves(Piece p) {
        LinkedList<String> moves = new LinkedList<String>();
        if (p.row > 0)
            moves.push((p.row - 1) + "," + p.col);
        if (p.row < 2)
            moves.push((p.row + 1) + "," + p.col);
        if (p.col > 0)
            moves.push((p.row) + "," + (p.col - 1));
        if (p.col < 3)
            moves.push((p.row) + "," + (p.col + 1));

        return moves.toArray(new String[moves.size()]);
    }

    /**
     * @author Juan Becerra
     * 
     * Gets the number of pieces that a piece would be able to see from a 
     * specified x and y coordinate, given a formatted board string.
     * Used in getBestMoveAdjacent and Anywhere.
     */
    private int getVisiblePiecesCount(int x, int y, String board) {
        // Split the string by : seperated tokens
        String[] tokens = board.split("[:]", -1);    // 12 tokens
        
        // Create a 2D array of ints representing the occurences of tokens in the string
        int[][] pieceCountBoard = new int[3][4];
        int k = 0;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 4; j++) {
                String thisToken = tokens[k++];
                if(thisToken.length() == 0) {
                    pieceCountBoard[i][j] = 0;
                } else if (!thisToken.contains(",")) {
                    pieceCountBoard[i][j] = 1;
                } else {
                    pieceCountBoard[i][j] = thisToken.split("[,]").length;
                }
            }
        }

        // Finally, count the pieces adjacent/at the piece at location x,y
        int count = 0;
        count+= pieceCountBoard[x][y] - 1;  // Currently standing here
        
        int tempx = x;
        int tempy = y;

        while(--tempx >= 0) {   // left
            count+= pieceCountBoard[tempx][tempy];
        }
        tempx = x;
        while(++tempx <= 2) {   // right
            count+= pieceCountBoard[tempx][tempy];
        }
        tempx = x;
        while(--tempy >= 0) {   // up
            count+= pieceCountBoard[tempx][tempy];
        }
        tempy = y;
        while(++tempy <= 3) {   // down
            count+= pieceCountBoard[tempx][tempy];
        }
        tempy = y;

        return count;
    }

    /**
     * Takes a piece object and string array of possible moves.
     * Returns the best possible move index (from string array).
     * Used in dice moves.
     */
    private int getBestMoveAdjacent(Piece piece, String board, String[] moves, boolean lookForLeast) {
        int returnIndex = -1;

        if(lookForLeast) {
            // Get the number of visible pieces from all adjacent positions
            int thisIndex = 0;
            int bestCount = 10000;
            for(String coordString : moves) {
                String[] coordSplit = coordString.split("[,]");
                int thisCount = getVisiblePiecesCount(Integer.valueOf(coordSplit[0]), Integer.valueOf(coordSplit[1]), board);
                if(thisCount < bestCount) {
                    bestCount = thisCount;
                    returnIndex = thisIndex;
                }
                thisIndex++;
            }
        } else {
            // Get the number of visible pieces from all adjacent positions
            int thisIndex = 0;
            int bestCount = -1;
            for(String coordString : moves) {
                String[] coordSplit = coordString.split("[,]");
                int thisCount = getVisiblePiecesCount(Integer.valueOf(coordSplit[0]), Integer.valueOf(coordSplit[1]), board);
                if(thisCount > bestCount) {
                    bestCount = thisCount;
                    returnIndex = thisIndex;
                }
                thisIndex++;
            }
        }

        return returnIndex;
    }

    /**
     * Takes a piece object.
     * Returns the best possible move coordinates as a integer array
     * where returnPair[0] is x and returnPair[1] is y.
     * Used in "move," cards.
     */
    private int[] getBestMoveAnywhere(Piece piece, String board, boolean lookForLeast) {
        int[] returnPair = new int[2];
        returnPair[0] = -1;
        returnPair[1] = -1;

        if(lookForLeast) {
            // Iterate through every possible index
            int bestCount = 10000;
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 4; j++) {
                    int thisCount = getVisiblePiecesCount(i, j, board);
                    if(thisCount < bestCount) {
                        bestCount = thisCount;
                        returnPair[0] = i;
                        returnPair[1] = j;
                    }
                }
            }
        } else {
            // Iterate through every possible index
            int bestCount = -1;
            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 4; j++) {
                    int thisCount = getVisiblePiecesCount(i, j, board);
                    if(thisCount > bestCount) {
                        bestCount = thisCount;
                        returnPair[0] = i;
                        returnPair[1] = j;
                    }
                }
            }
        }

        return returnPair;
    }

    /**
     * Takes a string representation of board.
     * Returns an array of integers
     * arr[0] - redCount
     * arr[1] - greenCount
     * arr[2] - yellowCount
     */
    private int[] getColorDistribution(String board) {
        // Split the string by : seperated tokens
        String[] tokens = board.split("[:]", -1);    // 12 tokens
        int[] counts = new int[3];  // Red green yellow

        // Create a 2D array of ints representing the occurences of tokens in the string
        int[][] pieceCountBoard = new int[3][4];
        int k = 0;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 4; j++) {
                String thisToken = tokens[k++];
                if(thisToken.length() == 0) {
                    pieceCountBoard[i][j] = 0;
                } else if (!thisToken.contains(",")) {
                    pieceCountBoard[i][j] = 1;
                } else {
                    pieceCountBoard[i][j] = thisToken.split("[,]").length;
                }

                // Count occurences
                if(pieceCountBoard[i][j] > 0) {
                    if(gemArray[i][j][0] == 1) {
                        counts[0]++;
                    }
                    if(gemArray[i][j][1] == 1) {
                        counts[1]++;
                    }
                    if(gemArray[i][j][2] == 1) {
                        counts[2]++;
                    }
                }
            }
        }

        return counts;
    }

    private int redGems = 0;
    private int greenGems = 0;
    private int yellowGems = 0; 

    /** 
     * When we need to pick a color, this returns a string of the 
     * best color to pick.
     * 
    */
    private String getBestColor(String board) {
        int[] colors = getColorDistribution(board);
        int total = 0;

        for(int i = 0; i < colors.length; i++) {
            total += colors[i];
        }

        int totalHeld = redGems + greenGems + yellowGems;

        double redScore = (Double.valueOf(totalHeld) / Double.valueOf(redGems)) * (Double.valueOf(colors[0]) / Double.valueOf(total));
        double greenScore = (Double.valueOf(totalHeld) / Double.valueOf(greenGems)) * (Double.valueOf(colors[1]) / Double.valueOf(total));
        double yellowScore = (Double.valueOf(totalHeld) / Double.valueOf(yellowGems)) * (Double.valueOf(colors[2]) / Double.valueOf(total));

        boolean canPickRed = false;
        boolean canPickGreen = false;
        boolean canPickYellow = false;
        if(gemArray[me.row][me.col][0] == 1) {
            canPickRed = true;
        }
        if(gemArray[me.row][me.col][1] == 1) {
            canPickGreen = true;
        }
        if(gemArray[me.row][me.col][2] == 1) {
            canPickYellow = true;
        }

        if (canPickRed && canPickGreen && canPickYellow) {
            if (redScore >= greenScore && redScore >= yellowScore)
                return "red";
            else if (greenScore >= redScore && greenScore >= yellowScore)
                return "green";
            else
                return "yellow";
        } else if (canPickRed && canPickGreen && !canPickYellow) {
            if (redScore >= greenScore)
                return "red";
            else
                return "green";
        } else if (canPickRed && !canPickGreen && canPickYellow) {
            if (redScore >= yellowScore)
                return "red";
            else
                return "yellow";
        } else if (!canPickRed && canPickGreen && canPickYellow) {
            if (greenScore >= yellowScore)
                return "green";
            else
                return "yellow";
        } else if (canPickRed && !canPickGreen && !canPickYellow) {
            return "red";
        } else if (!canPickRed && canPickGreen && !canPickYellow) {
            return "green";
        } else if (!canPickRed && !canPickGreen && canPickYellow) {
            return "yellow";
        } else {
            System.out.println("Something has gone horribly wrong");
            return null;
        }
    }

    private static void increment(Map<String, Integer> map, String key){ //hash map value incrementer
	map.putIfAbsent(key,0);
	map.put(key,map.get(key)+1);
    }

    /**
     * Given the name of a piece, returns the name of a 
     * player that we should ask "Can you see {argument}"
     */
    private String getBestPlayerToAsk(String canYouSee) {
    	Map<String, Integer> PlayerView = new HashMap(); //a map to store how many players can see each piece 
	
	  for (String i : pieces.keySet()) {	//for each player
          	Piece p1 = pieces.get(i);
	    	for (String j : pieces.keySet()) { //can they see each other?
           		Piece p2 = pieces.get(j); //if they can	
            	 	if (canSee(p1, p2)){
				increment(PlayerView,p2.name); //increment the # of people who can see that piece
		 	}
  	          	//System.out.println("\n\n\n "+ p1.name + " can see " + p2.name);
       	    	}
          }
	  //System.out.println("\n\nPLAYER VIEW: " + PlayerView);
	  int min = Integer.MAX_VALUE;
	  String retval = ""; 
	  for(Map.Entry<String, Integer> entry : PlayerView.entrySet()) { //retrieve the piece the least players can see
    	  	if(entry.getValue() < min) {
        		min = entry.getValue();
        		retval = entry.getKey();
    		}
	  }

	//uncomment next block if you want to ask for the piece that most players can see        
/*	
	int max = Integer.MIN_VALUE;
        String retval = "";
        for(Map.Entry<String, Integer> entry : PlayerView.entrySet()) {
            if(entry.getValue() > max) {
               max = entry.getValue();
               retval = entry.getKey();
	    }
	}                                                                                                  
                                                                                                            
	//  System.out.println("THE MINIMUM IS :" + min + "\nKEY IS: " + retval)     
	return retval;
    }
*/
    private boolean madeGemArray = false;

    public String getPlayerActions(String d1, String d2, String card1, String card2, String board)
            throws Suspicion.BadActionException {
        this.board = new Board(board, pieces, gemLocations);
        String actions = "";

        if(!madeGemArray) {
            initGemArray(this.board.gemLocations);
            madeGemArray = true;
        }

        // Random move for dice1
        if (d1.equals("?"))
            d1 = guestNames[r.nextInt(guestNames.length)];
        Piece piece = pieces.get(d1);
        String[] moves = getPossibleMoves(piece);
        int movei;
        if(piece.name == me.name) {
            movei = getBestMoveAdjacent(piece, board, moves, true);
        } else {
            movei = getBestMoveAdjacent(piece, board, moves, false); // Using new method
        }
        actions += "move," + d1 + "," + moves[movei];
        this.board.movePlayer(piece, Integer.parseInt(moves[movei].split(",")[0]),
                Integer.parseInt(moves[movei].split(",")[1])); // Perform the move on my board

        // Random move for dice2
        if (d2.equals("?"))
            d2 = guestNames[r.nextInt(guestNames.length)];
        piece = pieces.get(d2);
        moves = getPossibleMoves(piece);
        if(piece.name == me.name) {
            movei = getBestMoveAdjacent(piece, board, moves, true);
        } else {
            movei = getBestMoveAdjacent(piece, board, moves, false); // Using new method
        }
        actions += ":move," + d2 + "," + moves[movei];
        this.board.movePlayer(piece, Integer.parseInt(moves[movei].split(",")[0]),
                Integer.parseInt(moves[movei].split(",")[1])); // Perform the move on my board

        // which card
        int i = r.nextInt(2);
        actions += ":play,card" + (i + 1);

        String card = i == 0 ? card1 : card2;

        for (String cardAction : card.split(":")) // just go ahead and do them in this order
        {
            if (cardAction.startsWith("move")) {
                String guest;
                guest = guestNames[r.nextInt(guestNames.length)];   // May change later
                piece = pieces.get(guest);
                int[] bestMove;
                if(piece.name == me.name) {
                    bestMove = getBestMoveAnywhere(piece, board, true);
                } else {
                    bestMove = getBestMoveAnywhere(piece, board, false);
                }

                actions += ":move," + guest + "," + bestMove[0] + "," + bestMove[1];
            } else if (cardAction.startsWith("viewDeck")) {
                actions += ":viewDeck";
            } else if (cardAction.startsWith("get")) {
                String color = "";
                // @@@ You SHOULD replace this with code that optimizes this decision
                if (cardAction.equals("get,")) {        
                    color = getBestColor(board);
                    actions += ":get," + color;
                } else {
                    String[] tokens = cardAction.split("[,]");
                    color = tokens[1];
                    actions += ":" + cardAction;
                }
                // Increment the correct color
                if(color.equals("red")) {
                    redGems++;
                } else if (color.equals("green")) {
                    greenGems++;
                } else if (color.equals("yellow")) {
                    yellowGems++;
                }

            } else if (cardAction.startsWith("ask")) {
                // TODO ask the right person a questions
                String personToAsk = getBestPlayerToAsk(cardAction.split(",")[1]);
                actions += ":" + cardAction + otherPlayerNames[r.nextInt(otherPlayerNames.length)];
            }
        }
        return actions;
    }

    // @@@ This function returns a list of guests who are in a room that contains
    // the
    // @@@ given gemcolor
    private ArrayList<String> getGuestsInRoomWithGem(String board, String gemcolor) {
        Board b = new Board(board, pieces, gemLocations);
        int gem = -1;
        if (gemcolor.equals("yellow"))
            gem = Suspicion.YELLOW;
        else if (gemcolor.equals("green"))
            gem = Suspicion.GREEN;
        else if (gemcolor.equals("red"))
            gem = Suspicion.RED;
        ArrayList<String> possibleGuests = new ArrayList<String>();

        int y = 0, x = 0;
        for (String guests : board.trim().split(":")) {
            // only get people from rooms with the gem
            if (b.rooms[y][x].gems[gem] && guests.trim().length() > 0) {
                for (String guest : guests.trim().split(",")) {
                    possibleGuests.add(guest.trim());
                }
            }
            x++;
            y += x / 4;
            x %= 4;
        }

        return possibleGuests;
    }

    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board,
            String actions) {
    }

    // @@@ Here we added code to update the possible guest name list for a player if
    // we see that player
    // @@@ play a get gem action card
    public void reportPlayerActions(String player, String d1, String d2, String cardPlayed, String board[],
            String actions) {
        if (player.equals(this.playerName))
            return; // If player is me, return
        // Check for a get action and use the info to update player knowledge
        if (cardPlayed.split(":")[0].equals("get,") || cardPlayed.split(":")[1].equals("get,")) {
            int splitindex;
            String[] split = actions.split(":");
            String get;
            if (split[3].indexOf("get") >= 0)
                splitindex = 3;
            else
                splitindex = 4;
            get = split[splitindex];
            String gem = get.split(",")[1];
            // board[splitIndex+1] will have the state of the board when the gem was taken
            if (board[splitindex] != null) // This would indicate an error in the action
            {
                ArrayList<String> possibleGuests = getGuestsInRoomWithGem(board[splitindex], gem);
                players.get(player).adjustKnowledge(possibleGuests);

                /*
                 * System.out.println(
                 * "***************************************************************");
                 * System.out.println(
                 * "***************************************************************");
                 * System.out.println(
                 * "***************************************************************");
                 * System.out.println(player + " took a gem!");
                 * System.out.println("People who could take this gem: " + possibleGuests);
                 * System.out.println("Possible guests: " +
                 * players.get(player).possibleGuestNames);
                 * display.displayBoard(board[splitindex]); System.out.println(
                 * "***************************************************************");
                 * System.out.println(
                 * "***************************************************************");
                 * System.out.println(
                 * "***************************************************************");
                 */
            }
        }
    }

    private boolean canSee(Piece p1, Piece p2) // returns whether or not these two pieces see each
    {
        return (p1.row == p2.row || p1.col == p2.col);
    }

    public void answerAsk(String guest, String player, String board, boolean canSee) {
        Board b = new Board(board, pieces, gemLocations);
        ArrayList<String> possibleGuests = new ArrayList<String>();
        Piece p1 = pieces.get(guest); // retrieve the guest
        for (String k : pieces.keySet()) {
            Piece p2 = pieces.get(k);
            if ((canSee && canSee(p1, p2)) || (!canSee && !canSee(p1, p2)))
                possibleGuests.add(p2.name);
        }
        // System.out.println("Adjusting knowledge about " + player + " to : " +
        // possibleGuests);
        players.get(player).adjustKnowledge(possibleGuests);
    }

    // @@@ added code to adjust our KBs for all players to exclude the player we
    // drew from the guest names deck
    public void answerViewDeck(String player) {
        for (String k : players.keySet()) {
            players.get(k).adjustKnowledge(player);
        }
    }

    // @@@ Calculate for each player and each possible guest ID for that player,
    // @@@ the number of valid assignments (or valid world states) where
    // @@@ that player can be assigned that particular guest ID
    // @@@
    // @@@ For example, if you have two players with possible guest ids as follows
    // @@@
    // @@@ Fred [red, purple, green]
    // @@@ Tom [red, green]
    // @@@
    // @@@ Then for Fred, the counts are: red=1, purple=2, green=1
    // @@@ For Tom the counts are red=2 and green=2
    // @@@
    // @@@ So purple *might* be the most likely color to assign Fred, given that
    // more possible world states exist with that assignment
    // @@@
    // @@@
    private int calcGuestNameStats(int p, ArrayList<String> assignedIDs,
            HashMap<String, HashMap<String, Integer>> guestNameCounts) {
        int totalCount = 0;
        // TODO
        // @@@ this is the base case for the recursion
        // @@@ if we hit this, we've got a full assignment, so we update the counts on
        // the assigned ids
        if (p >= otherPlayerNames.length) {
            totalCount++;
            for (int x = 0; x < otherPlayerNames.length; x++) {
                // @@@ this increments the counts on the assigned ids
                Integer i = guestNameCounts.get(otherPlayerNames[x]).get(assignedIDs.get(x)) + 1;
                guestNameCounts.get(otherPlayerNames[x]).put(assignedIDs.get(x), i);
            }
        }
        // @@@ This is the recursive part where we try all possible valid combinations
        // of assigning IDs
        else
            for (String gid : players.get(otherPlayerNames[p]).possibleGuestNames) {
                if (!assignedIDs.contains(gid)) {
                    assignedIDs.add(gid);
                    totalCount += calcGuestNameStats(p + 1, assignedIDs, guestNameCounts);
                    assignedIDs.remove(gid);
                }
            }
        return totalCount;
    }

    private void printGuestNameCounts(HashMap<String, HashMap<String, Integer>> guestNameCounts) {
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
        for (String name : otherPlayerNames) {
            System.out.print(name);
            for (String gid : players.get(name).possibleGuestNames) {
                System.out.print(", " + gid + "=" + guestNameCounts.get(name).get(gid));
            }
            System.out.println();
        }
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
        System.out.println("****************************************************************************");
    }

    public class GuestNameStats {
        public HashMap<String, HashMap<String, Integer>> guestNameCounts;
        public int totalCount;

        public GuestNameStats(HashMap<String, HashMap<String, Integer>> guestNameCounts, int totalCount) {
            this.guestNameCounts = guestNameCounts;
            this.totalCount = totalCount;
        }
    }

    private GuestNameStats calcGuestNameStats() {
        int totalCount = 0;
        /*
         * This is a 2 level hash. The first level is keyed on otherPlayerNames, and the
         * second level is keyed on possibleGuestIDs
         */
        HashMap<String, HashMap<String, Integer>> guestNameCounts = new HashMap<String, HashMap<String, Integer>>();
        for (String name : otherPlayerNames) {
            HashMap<String, Integer> c;
            guestNameCounts.put(name, c = new HashMap<String, Integer>());
            for (String gid : players.get(name).possibleGuestNames) {
                c.put(gid, 0);
            }
        }
        totalCount += calcGuestNameStats(0, new ArrayList<String>(), guestNameCounts);

        // System.out.println("TotalCount="+totalCount);

        return new GuestNameStats(guestNameCounts, totalCount);
    }

    // @@@ One of the things you can do is remove zero count guest IDs from a
    // players KB
    // @@@ So for example, if we have the following
    // @@@
    // @@@ Fred=[red,green,yellow]
    // @@@ Tom =[red,green]
    // @@@ Nancy=[red,green]
    // @@@
    // @@@ you canuse the counts to remove both red and green from Fred's KB, as
    // there
    // @@@ are no valid world states that assign Fred those guest ids.
    // @@@
    private void cleanUpZeroCountIDs(GuestNameStats stats) {
        for (String name : otherPlayerNames) {
            // @@@ Retrieve the guest name counts for this player
            HashMap<String, Integer> counts = stats.guestNameCounts.get(name);
            // @@@ Go through it and collect all the guest names that have non-zero counts
            // @@@ then adjust your KB for this player so they only have the non-zero count
            // guest IDs
            // players.get(name).adjustKnowledge(nonzero);
        }
    }

    private void cleanupZeroCountIDs() {
        GuestNameStats stats = calcGuestNameStats();
        cleanUpZeroCountIDs(stats);
    }

    public String reportGuesses() {
        String rval = "";
        for (String k : players.keySet()) {
            Player p = players.get(k);
            rval += k;
            for (String g : p.possibleGuestNames) {
                rval += "," + g;
            }
            rval += ":";
        }
        return rval.substring(0, rval.length() - 1);
    }

    private void initGemArray(String dumbGemString) {
        // Split the string by : seperated tokens
        String[] tokens = dumbGemString.split("[:]", -1);    // 12 tokens
        
        // Create a 2D array of ints representing the occurences of tokens in the string
        int k = 0;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 4; j++) {
                String thisToken = tokens[k++];
                gemArray[i][j][0] = 0;
                gemArray[i][j][1] = 0;
                gemArray[i][j][2] = 0;
                if (!thisToken.contains(",")) {
                    if(thisToken.trim().equals("red")) {
                        gemArray[i][j][0] = 1;
                    } else if(thisToken.trim().equals("green")) {
                        gemArray[i][j][1] = 1;
                    } else if(thisToken.trim().equals("yellow")) {
                        gemArray[i][j][2] = 1;
                    }
                } else {
                    String[] thisTokenTokenized = thisToken.split("[,]");
                    for(String t : thisTokenTokenized) {
                        if(t.trim().equals("red")) {
                            gemArray[i][j][0] = 1;
                        } else if(t.trim().equals("green")) {
                            gemArray[i][j][1] = 1;
                        } else if(t.trim().equals("yellow")) {
                            gemArray[i][j][2] = 1;
                        }
                    }
                }
                //System.out.println("Gems at location (" + i + ", " + j + "): " + gemArray[i][j][0] + " " + gemArray[i][j][1] + " " + gemArray[i][j][2]);
            }
        }
    }

    public RBonk(String playerName, String guestName, int numStartingGems, String gemLocations,
            String[] playerNames, String[] guestNames) {
        super(playerName, guestName, numStartingGems, gemLocations, playerNames, guestNames);
        display = new TextDisplay(gemLocations);
        pieces = new HashMap<String, Piece>();
        ArrayList<String> possibleGuests = new ArrayList<String>();
        for (String name : guestNames) {
            pieces.put(name, new Piece(name));
            if (!name.equals(guestName))
                possibleGuests.add(name);
        }
        me = pieces.get(guestName);

        players = new HashMap<String, Player>();
        for (String str : playerNames) {
            if (!str.equals(playerName))
                players.put(str, new Player(str, possibleGuests.toArray(new String[possibleGuests.size()])));
        }

        otherPlayerNames = players.keySet().toArray(new String[players.size()]);
    }
}

import java.util.*;

/* N is the number of players (including the captains); always greater than 2, even
** m is the amount strength affects elimination; greater than 0
** A and B are the strengths of each captain, A picks first
** prints out the equilibrium order in which players are picked */

public class Schoolyard {

    // define constants
    private static final int TEAM_A = 1;
    private static final int TEAM_B = 2;
    private static final int UNCLAIMED = 0;

    // private instance variable
    private Node root;

    public class Node {
        protected int data;
        protected boolean bold;
        protected ArrayList<Node> children;

        // constructors
        public Node(int data) {
            this.data = data;
            children = new ArrayList(0);
            bold = false;
        }

        public Node(int data, boolean bold) {
            this.data = data;
            this.bold = bold;
            children = new ArrayList(0);
        }

        public void setChildren(Node[] t) {
            // EFFECT: copy the array t, into the array children, effectively
            // setting all the chidern of this node simultaneouly
            int l = children.size();
            for (int i = t.length - 1; i >= 0; i--) {
                children.add(l, t[i]);
            }
        }

        public Node getChild(int i) {
            // EFFECT: returns the child at index i
            return children.get(i);
        }

        public void setBold(boolean bool) {
            // EFFECT: set the bold value of this node to bool
            bold = bool;
        }

        public boolean isLeaf() {
            return (children.size() == 0);
        }

        public Node getBold() {
            for (int i = 0; i < children.size(); i++) {
                if (getChild(i).bold) {
                    return getChild(i);
                }
            }
            System.out.println("No bold child found");
            return null;
        }

        // modified code from toString() and print() methods of
        // https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram/42449385#42449385
        // to print general tree
        public String toString() {
            StringBuilder buffer = new StringBuilder(50);
            print(buffer, "", "");
            return buffer.toString();
        }

        public void print(StringBuilder buffer, String prefix, String childrenPrefix) {
            buffer.append(prefix);
            if (bold) {
                buffer.append("= " + data);
            } else {
                buffer.append("- " + data);
            }
            buffer.append('\n');
            for (Iterator<Node> it = children.iterator(); it.hasNext();) {
                Node next = it.next();
                if (it.hasNext()) {
                    next.print(buffer, childrenPrefix + "| ", childrenPrefix + "|   ");
                } else {
                    next.print(buffer, childrenPrefix + "| ", childrenPrefix + "    ");
                }
            }
        }
    }

    public static void main(String[] args) {
        Schoolyard s = new Schoolyard();
        s.schoolyard(4, 1, 0, 1);
    }

    // constructor for a schoolyard
    private Schoolyard() {
        root = new Node(-1, true);
    }

    private void schoolyard(int N, double m, int A, int B) {
        // parameter validation
        if (!checkParams(N, m, A, B))
            return;

        // generate and initialize players array
        int[] players = new int[N];
        for (int i = 0; i < players.length; i++) {
            if (i == A)
                players[i] = TEAM_A;
            else if (i == B)
                players[i] = TEAM_B;
            else
                players[i] = UNCLAIMED;
        }

        // Backwards induction
        // number of players unclaimed
        int numLeft = N - 2;
        int[] roster = getRoster(numLeft, players, N, m, A, B, root);
        System.out.println("Final roster:");
        printRoster(roster);
        // System.out.println("Final tree:");
        // StringBuilder b = new StringBuilder();
        // root.print(b, "", "");
        // System.out.println(b);
    }

    private boolean checkParams(int N, double m, int A, int B) {
        if (N <= 2) {
            System.out.println("N must be greater than 2");
            return false;
        }
        if (N % 2 != 0) {
            System.out.println("N must be even");
            return false;
        }
        if (m <= 0) {
            System.out.println("m must be greater than 0");
            return false;
        }
        if (A == B) {
            System.out.println("A and B must have distinct strengths");
            return false;
        }
        if (A < 0 || A >= N) {
            System.out.println("A's strength must lie between 0 and N-1, inclusive");
            return false;
        }
        if (B < 0 || B >= N) {
            System.out.println("B's strength must lie between 0 and N-1, inclusive");
            return false;
        }
        return true;
    }

    private void printRoster(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == TEAM_A)
                System.out.println(i + ": TEAM A");
            else if (arr[i] == TEAM_B)
                System.out.println(i + ": TEAM B");
            else if (arr[i] == UNCLAIMED)
                System.out.println(i + ": UNCLAIMED");
        }
    }

    private void printLeft(int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.println(i + ": " + arr[i]);
        }
    }

    private String generateTabs(int N, int numLeft) {
        String tab = "";
        for (int i = 0; i < (N - numLeft - 2); i++) {
            tab += "\t";
        }
        return tab;
    }

    // returns an array of the final team roster
    // numLeft = number of players unclaimed, players = initial roster
    private int[] getRoster(int numLeft, int[] players, int N, double m, int A, int B, Node parent) {
        // generate left array
        int[] left = getLeft(numLeft, players);
        Node[] leftNodes = getLeftNodes(left);
        parent.setChildren(leftNodes);
        /*
         * Prints child nodes: for (int i = 0; i < parent.children.size(); i++) {
         * System.out.println("Child index " + i + ", data " + parent.getChild(i).data);
         * } System.out.println("Parent node: " + parent.data);
         */
        String tab = generateTabs(N, numLeft);

        // base case: 2 players left, A is choosing
        if (numLeft == 2) {
            int Sa = sum(TEAM_A, players);
            int Sb = sum(TEAM_B, players);

            int chosen;
            double f1 = probLoss(TEAM_A, Sa + left[0], Sb + left[1]) * probWorst(TEAM_A, N, m, A, B, Sa, left[0]);
            double f2 = probLoss(TEAM_A, Sa + left[1], Sb + left[0]) * probWorst(TEAM_A, N, m, A, B, Sa, left[1]);

            if (Math.min(f1, f2) == f1) {
                chosen = left[0];
                players[chosen] = TEAM_A;
                players[left[1]] = TEAM_B;
                System.out.println(tab + "2 players left, A chooses " + chosen);
                System.out.println(tab + "1 player left, B chooses " + left[1]);
                parent.getChild(0).setBold(true);
            } else {
                chosen = left[1];
                players[chosen] = TEAM_A;
                players[left[0]] = TEAM_B;
                System.out.println(tab + "2 players left, A chooses " + chosen);
                System.out.println(tab + "1 player left, B chooses " + left[0]);
                parent.getChild(1).setBold(true);
            }
            return players;
        }

        // if an odd number of players is left, B is choosing in this round
        if (numLeft % 2 == 1) {
            // probs is array containing probability that B's captain will get eliminated
            // given the jth player left is chosen
            double[] probs = new double[numLeft];

            for (int j = 0; j < numLeft; j++) {
                System.out.println(tab + numLeft + " players left, B chooses " + left[j]);
                // B chooses the jth player left, left[j]
                players[left[j]] = TEAM_B;

                // call the getRoster function on this players array to retrieve an array of
                // what will be chosen subsequently
                int[] roster = getRoster(numLeft - 1, players, N, m, A, B, parent.getChild(j));

                // calculate probability of elimination from final team roster
                int Sa = sum(TEAM_A, roster);
                int Sb = sum(TEAM_B, roster);
                probs[j] = probLoss(TEAM_B, Sa, Sb) * probWorst(TEAM_B, N, m, A, B, roster);

                // unclaim all players in left
                for (int i = 0; i < left.length; i++) {
                    players[left[i]] = UNCLAIMED;
                }
            }

            // B will choose candidate resulting in minimum chance of elimination
            int chosenLeft = indexOfSmallest(probs);
            System.out.print(tab + "Resolving branch: ");
            parent.getChild(chosenLeft).setBold(true);

            // return players array modified to reflect player chosen
            // this one is B, next is A, next is B, ...
            boolean isTeamB = true;
            Node top = parent;
            System.out.print(numLeft + " players left");
            while (!top.isLeaf()) {
                // get bold child and add to players
                top = top.getBold();
                if (isTeamB) {
                    players[top.data] = TEAM_B;
                    System.out.print(", B chooses " + top.data);
                } else {
                    players[top.data] = TEAM_A;
                    System.out.print(", A chooses " + top.data);
                }
                isTeamB = !isTeamB;
            }
            // 1 remaining unclaimed player that belongs to B
            int[] updated = claimRemaining(players);
            System.out.println();
            return updated;
        }

        // if an even number of players is left, A is choosing in this round
        if (numLeft % 2 == 0) {
            // probs is array containing probability that A's captain will get eliminated
            // given the jth player left is chosen
            double[] probs = new double[numLeft];

            for (int j = 0; j < numLeft; j++) {
                System.out.println(tab + numLeft + " players left, A chooses " + left[j]);
                // A chooses the jth player left, left[j]
                players[left[j]] = TEAM_A;

                // call the getRoster function on this players array to retrieve an array of
                // what will be chosen subsequently
                int[] roster = getRoster(numLeft - 1, players, N, m, A, B, parent.getChild(j));

                // calculate probability of elimination from final team roster
                int Sa = sum(TEAM_A, roster);
                int Sb = sum(TEAM_B, roster);
                probs[j] = probLoss(TEAM_A, Sa, Sb) * probWorst(TEAM_A, N, m, A, B, roster);

                // unclaim all players in left
                for (int i = 0; i < left.length; i++) {
                    players[left[i]] = UNCLAIMED;
                }
            }
            // A will choose candidate resulting in minimum chance of elimination
            System.out.print(tab + "Resolving branch: ");
            int chosenLeft = indexOfSmallest(probs);
            parent.getChild(chosenLeft).setBold(true);

            // return players array modified to reflect player chosen
            boolean isTeamA = true;
            Node top = parent;
            System.out.print(numLeft + " players left");
            while (!top.isLeaf()) {
                // get bold child and add to players
                top = top.getBold();
                if (isTeamA) {
                    players[top.data] = TEAM_A;
                    System.out.print(", A chooses " + top.data);
                } else {
                    players[top.data] = TEAM_B;
                    System.out.print(", B chooses " + top.data);
                }
                isTeamA = !isTeamA;
            }
            // 1 remaining unclaimed that belongs to B
            System.out.println();
            int[] updated = claimRemaining(players);
            return updated;
        }

        System.out.println("Using wrong return statement in getRoster");
        return players;
    }

    private Node[] getLeftNodes(int[] left) {
        Node[] nodes = new Node[left.length];
        for (int i = 0; i < left.length; i++) {
            Node n = new Node(left[i]);
            nodes[i] = n;
        }
        return nodes;
    }

    private int[] claimRemaining(int[] players) {
        boolean happened = false;
        for (int i = 0; i < players.length; i++) {
            if (players[i] == UNCLAIMED) {
                if (happened) {
                    System.out.println("Incorrectly using this method. Multiple players unclaimed.");
                } else {
                    players[i] = TEAM_B;
                    happened = true;
                }
            }
        }
        return players;
    }

    // generate left array, containing strengths of unclaimed players in increasing
    // order
    private int[] getLeft(int numLeft, int[] players) {
        // System.out.println("NumLeft: " + numLeft);
        // printRoster(players);
        int j = 0;
        int[] left = new int[numLeft];
        for (int i = 0; i < players.length; i++) {
            if (players[i] == UNCLAIMED) {
                left[j] = i;
                j++;
            }
        }
        // System.out.println("Printing left:");
        // printLeft(left);
        return left;
    }

    // returns index of smallest element in list array
    private int indexOfSmallest(double[] list) {
        int minIndex = 0;
        double minValue = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] < minValue) {
                minValue = list[i];
                minIndex = i;
            }
        }
        // System.out.println("Min value: " + minValue);
        // System.out.println("Min index: " + minIndex);
        return minIndex;
    }

    // returns sum for specified team from players array
    private int sum(int team, int[] players) {
        int sum = 0;
        for (int i = 0; i < players.length; i++) {
            if (players[i] == team) {
                sum += i;
            }
        }
        return sum;
    }

    // calculates the probability of specified team losing team challenge
    // Sa = strength of team A, Sb = strength of team B, team = specifies team
    private double probLoss(int team, int Sa, int Sb) {
        if (team == TEAM_A)
            return Sb * 1.0 / (Sa + Sb);
        else if (team == TEAM_B)
            return Sa * 1.0 / (Sa + Sb);
        else {
            System.out.println("Error in probLoss: Please enter a valid int for team");
            return -1;
        }
    }

    // calculates the probability of team captain of specified team doing the worst
    // in the individual challenge
    private double probWorst(int team, int N, double m, int A, int B, int[] players) {
        // calculate team strength
        int teamStrength = sum(team, players);

        // calculate individual strength
        int indivStrength;
        if (team == TEAM_A)
            indivStrength = A;
        else if (team == TEAM_B)
            indivStrength = B;
        else {
            System.out.println("Error in probWorst: Please enter a valid int for team");
            return -1;
        }

        // calculate probability of losing challenge against teammates
        return (N - m * indivStrength) / (N * N * 0.5 - m * teamStrength);
    }

    // calculates the probability of team captain of specified team doing the worst
    // in the individual challenge
    // sum = strength of team so far, p = strength of new player added to team
    private double probWorst(int team, int N, double m, int A, int B, int sum, int p) {
        // calculate team strength
        int teamStrength = sum + p;

        // calculate individual strength
        int indivStrength;
        if (team == TEAM_A)
            indivStrength = A;
        else if (team == TEAM_B)
            indivStrength = B;
        else {
            System.out.println("Error in probWorst: Please enter a valid int for team");
            return -1;
        }

        // calculate probability of losing challenge against teammates
        return (N - m * indivStrength) / (N * N * 0.5 - m * teamStrength);
    }
}

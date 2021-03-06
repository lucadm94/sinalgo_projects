package projects.coloring_d2.nodes.nodeImplementations;
        import java.awt.Color;
        import java.awt.Graphics;
        import java.util.*;
        import sinalgo.configuration.WrongConfigurationException;
        import sinalgo.gui.transformation.PositionTransformation;
        import sinalgo.nodes.Connections;
        import sinalgo.nodes.Node;
        import sinalgo.nodes.NodeOutgoingConnectionsList;
        import sinalgo.nodes.edges.Edge;
        import sinalgo.nodes.messages.Inbox;
        import projects.coloring_d2.nodes.timers.*;
        import projects.coloring_d2.nodes.messages.*;
        import sinalgo.nodes.messages.Message;

public class CNodeD2 extends Node {
    private int color;
    private final int nb = 9;      // number of colors
    private final Color tab[] =
            {Color.BLUE,Color.CYAN,Color.GREEN,Color.DARK_GRAY,Color.MAGENTA,Color.ORANGE,Color.PINK,Color.RED,Color.WHITE,Color.YELLOW,Color.BLACK,Color.LIGHT_GRAY,Color.GRAY};
    private Hashtable<Integer,state> neighborStates;

    // declare containers for for distance 2 nodes (outgoing connections of my neighbours and state of neighbours of my neighbours)
    private Hashtable<Integer,state> d2NeighborStates;
    public NodeOutgoingConnectionsList d2OutgoingConnections = new NodeOutgoingConnectionsList(true);

    public Hashtable<Integer,state> getNeighborStates(){
        return neighborStates;
    }
    public state getNeighborState(int id){
        return neighborStates.get(id);
    }

    public int getColorInt(){
        return color;
    }
    public Color RGBColor(){
        return tab[getColorInt()];
    }
    public void setColor(int c) {
        this.color=c;
    }
    public void initColor(int range){
        setColor((int) (Math.random() * range) % range);
    }
    public void compute(){

        boolean same=false;

        boolean SC[]=new boolean[nb];
        for (int i=0;i<SC.length;i++)
            SC[i]=false;

        Iterator<Edge> it=this.outgoingConnections.iterator();
        while(it.hasNext()){
            Edge e=it.next();
            state tmp=neighborStates.get(e.endNode.ID); // get state of the pointed neighbor from  neighbor hash  table
            if(tmp!=null){
                if(tmp.color==this.getColorInt()){
                    same=true;
                }
                SC[tmp.color]=true;
            }
        }


        // cycle only on d2 neighbours connections (outgoing connections of my neighbour)
        Iterator<Edge> d2neigh_it=this.d2OutgoingConnections.iterator();
        while(d2neigh_it.hasNext()) {
            Edge d2e = d2neigh_it.next();   // connections of my neignbours
            if (d2e.endNode.ID != this.ID) {
                state n_tmp = d2NeighborStates.get(d2e.endNode.ID); // get state of the pointed neighbor from  neighbor hash  table
                if (n_tmp != null) {
                    if (n_tmp.color == this.getColorInt()) {
                        same = true;
                    }


                    SC[n_tmp.color] = true;
                }
            }
        }



        if (same){
            int dispo=0;
            for (int i=0;i<SC.length;i++)
                if(SC[i]==false) dispo++;
            if (dispo == 0) return;
            int choix= ((int) (Math.random() * 10000)) % dispo + 1; // chose a random number between 1 and number of available colors
            int i=0;
            // practically i chose the first available color after skipping choix unavailable colors
            while(choix > 0){
                if(SC[i]==false)
                    choix--;
                if(choix>0) i++;
            }
            this.setColor(i);
        }
    }

    public void handleMessages(Inbox inbox) {
        if(inbox.hasNext()==false) return;
        while(inbox.hasNext()){
            Message msg=inbox.next();

            if(msg instanceof CMessage){

                //System.out.println("[Node "+ this.ID + "] I had this node advertised via message : " + (String) ((CMessage) msg).neighborStates.toString());


                Iterator<Edge> adv_d2neigh_it=((CMessage) msg).d2OutgoingConnections.iterator();
                while(adv_d2neigh_it.hasNext()) {
                    Edge e = adv_d2neigh_it.next();
                    if (e.startNode.ID != this.ID && e.endNode.ID!=this.ID){
                        try {
                            // todo: check which of these lines are causing null pointer exception
                            d2OutgoingConnections.remove(e.startNode, e.endNode);
                            d2OutgoingConnections.add(e.startNode, e.endNode, false);
                            d2NeighborStates.put(e.endNode.ID, d2NeighborStates.get(e.endNode.ID));
                            System.out.println("[Node "+ this.ID + "] Added edge :" + e.startNode.ID + " - " + e.endNode.ID + " -- color:" + d2NeighborStates.get(e.endNode.ID));
                        }

                        catch (Exception exc) {
                            System.out.println(exc);
                            System.out.println("[Node "+ this.ID + "] Error adding edge " + e.startNode.ID + " - " + e.endNode.ID);
                        }
                    }

                }

                state tmp=new state(((CMessage) msg).color);
                neighborStates.put(((CMessage) msg).id,tmp);









                compute();
            }
        }
    }

    public void preStep() {}
    public void init() {
        initColor(nb);
        (new CTimer(this,50)).startRelative(50,this);
        this.neighborStates = new Hashtable< Integer, state >
                ( this.outgoingConnections.size() );
    }
    public void neighborhoodChange() {}
    public void postStep() {}
    public String toString() {
        String s = "Node(" + this.ID + ") [";
        Iterator<Edge> edgeIter = this.outgoingConnections.iterator();
        while(edgeIter.hasNext()){
            Edge e = edgeIter.next();
            Node n = e.endNode;
            s+=n.ID+" ";
        }
        return s + "]";
    }
    public void checkRequirements() throws WrongConfigurationException {}
    public void draw(Graphics g,PositionTransformation pt,boolean highlight) {
        Color c;
        this.setColor(this.RGBColor());
        String text = ""+this.ID;
        c=Color.BLACK;
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 20, c);
    }
}
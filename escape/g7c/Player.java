package escape.g7c;
//org.apache.commons.math3.distribution.*;
import java.util.List;
import java.util.*;
import java.util.Random;
import java.io.*;
import javafx.util.*;


public class Player implements escape.sim.Player {
	private Random rand;
	private int n = 0;
	private int lastOddMove= -1;
	private int lastEvenMove=-1;
	private int lasttolastEvenMove = -1;
	private int lastMove = -1;
	private int turn=-1;
	private boolean updated_probs = false;

	private ArrayList<Integer> prob;
	private ArrayList<Integer> inverse_prob;
	private int total_weights;
	private ArrayList<ArrayList<Integer> >  estimates;
	private Boolean two_handles = (Boolean)false;

	private int found = -1;
	private ArrayList<Integer> handle_owned;
	private ArrayList<Integer> handle_owner;
	private int free_handles;

	private ArrayList<Boolean> is_taken;
	private ArrayList<Boolean> has_handle;

	ArrayList<Integer> lasttoLastEvenConflicts =null;
	ArrayList<Integer> lastEvenConflicts=null;


	private double stubborness = 0;
	private double eps = 0.17;
	private double mn = 0.000005;
	private int seen_enough = 6;
	HashMap <Pair<Integer,Integer>,Integer> hm = new <Pair<Integer,Integer>,Integer> HashMap();
	//Vector<Integer> double_check = new V
	public Player() {
		rand = new Random();
		//prob = new Vector<Double>();
	}
	
	public void updateProb(List<Integer> conflicts)
	{
			for(int i = 0;i<n;i++)
			{
				if(handle_owner.get(i)!=-1)
					prob.set(i,0);
			}
	}
	public void updateProbWithEstimates()
	{

	}
	public void updateEstimates(List<Integer> conflicts)
	{

	}

	public void updateConflicts(List<Integer> conflicts)
	{

		if(this.turn%2==1)
		{
			if(this.turn == 1)
			{
				lastEvenConflicts = new ArrayList<Integer>(conflicts);
			}
			else
			{
				lasttoLastEvenConflicts = lastEvenConflicts;

				lastEvenConflicts = new ArrayList<Integer>(conflicts);
			}
		}
		else
		{
			if(this.turn>2)
			{	Set<Integer> conf = new HashSet<Integer> ();
				conf.addAll(conflicts);
				for(Integer c:lasttoLastEvenConflicts)
				{
					if(!conf.contains(c))
					{
						if(handle_owner.get(lasttolastEvenMove)==-1)
							this.free_handles-=1;
						handle_owner.set(lasttolastEvenMove,(Integer)c);
					}
				}
			}
		}
	}
	public int getNonConflictMove(ArrayList<Integer>  prob)
	{
		int tmp = getRandomTurn(prob);
		while(tmp == lastMove || handle_owner.get(tmp)!=-1)
			tmp = getRandomTurn(prob);
		return tmp;
	}

	public int getLinearNonConflictMove()
	{
		int tmp = (lastEvenMove+1)%n;
		while(tmp == lastMove || tmp==lastEvenMove|| handle_owner.get(tmp)!=-1)
		{		if(tmp==lastMove)
					two_handles = true;
				tmp = (tmp+1)%n;
		}
		return tmp;
	}


	public int getRandomTurn(ArrayList<Integer> prob)
	{
	    int d = rand.nextInt(total_weights)+1;
	    int sum = 0;
	    for(int i= 0;i<n;i++)
	    {
	        sum+=prob.get(i);
	        if(d<=sum)
	        {
	            return i;
	        }

	    }

	    return n-1;
	}
	public int getAway() //Change this to as noncflicitng a move as possible
	{
		//Vector<Double> inverse_prob = new Vector<Double> (Collections.nCopies(n,0.0));
		for(int i = 0;i< n;i++)
		{
			inverse_prob.set(i,1000);

		}
		//Normalize(inverse_prob);
		return getNonConflictMove(inverse_prob);
	}

	public int getLinearAway() //Change this to as noncflicitng a move as possible
	{


		int tmp = (lastMove + n-1)%n;
		while(tmp==lastMove)
		{
			 tmp = (tmp + n-1)%n;
		}

		return tmp;
	}
	public void initVectors()
	{
			prob = new ArrayList<Integer>(Collections.nCopies(this.n,(Integer)1000));
			inverse_prob = new ArrayList<Integer>(Collections.nCopies(this.n,(Integer)1000));
			total_weights=1000*n;

			handle_owned =new ArrayList<Integer>(Collections.nCopies(this.n+1,(Integer)(-1)));
			handle_owner =new ArrayList<Integer>(Collections.nCopies(this.n+1,(Integer)(-1)));

			is_taken =new ArrayList<Boolean>(Collections.nCopies(this.n+1,(Boolean)(false)));
			has_handle =new ArrayList<Boolean>(Collections.nCopies(this.n+1,(Boolean)(false)));

	}
	@Override
	public int init(int n) {

		this.turn = 0;
		this.n = n;
		this.free_handles = this.n;

		initVectors();
		int nextMove = -1;


		nextMove = getLinearNonConflictMove();

		System.out.print("Nextmove: ");
		System.out.println(nextMove);

		System.out.print("lastMove: ");
		System.out.println(lastMove);

		lastMove = nextMove;
		lastEvenMove = lastMove;
		lasttolastEvenMove = -1;
		return lastMove + 1;






	}

	// Strategy: (Just for demostration, may not work.)
	// 1. If no one grabs your handle, stay.
	// 2. Grab the i-th handle where i is the id of some player who grab the same handle as you do.
	public int EvenMove(List<Integer> conflicts,Boolean simple)
	{
		int nextMove = -1;
		updateConflicts(conflicts);
		if(found!=-1)
		{

			nextMove = found;


		}
		else
		{
			//System.out.print("Still Looking:\n");
			//System.out.println(prob);
			nextMove = getLinearNonConflictMove();

		}
		if(found==-1)
		{		System.out.print("Nextmove: ");
				System.out.println(nextMove);

				System.out.print("lastMove: ");
				System.out.println(lastMove);

				System.out.print("Turn: " +Integer.toString(this.turn) + " Free handles: "+ Integer.toString(this.free_handles));

		}

		lastMove = nextMove;
		lasttolastEvenMove = lastEvenMove;
		lastEvenMove = lastMove;


		return lastMove + 1;

	}

	public int OddMove(List<Integer> conflicts,Boolean simple)
	{	int nextMove = -1;
		updateConflicts(conflicts);
		if(conflicts.size()==0 || found!=-1)
		{
			found = lastMove;
			//System.out.println("Setting Found :" + Integer.toString(found));


		}

			if(lasttolastEvenMove==-1)
			{	nextMove = getLinearAway();


			}
			else
			{
				 nextMove = lasttolastEvenMove;

				if(nextMove==lastMove)
					nextMove = getLinearAway();
			 }

			 lastMove = nextMove;
			lastOddMove = lastMove;
			return lastMove +1;



		 //System.out.println("lastOddMove "+lastOddMove);
	}

	public int EvenMoveSecondary(List<Integer> conflicts)
	{
		int nextMove = -1;
		if(found!=-1)
		{
			nextMove = found;
		}
		else
		{
			nextMove = getNonConflictMove(this.prob);
		}

		lastMove = nextMove;
		lasttolastEvenMove = lastEvenMove;
		lastEvenMove = lastMove;


		return lastMove + 1;
	}

	public int OddMoveSecondary(List<Integer> conflicts)
	{
		if(conflicts.size()==0)
		{
			found = lastMove;
		}

		int nextMove = -1;
		nextMove = getAway();

	lastMove = nextMove;
	 lastOddMove = lastMove;
	 return lastMove +1;

	}

	public int MoveMain(List<Integer> conflicts)
	{
		if(turn%2==0)
		{
				return EvenMove(conflicts,true);
		}
		else //On even turns, just make sure you can come back.
		{

				return OddMove(conflicts,true);
		}
	}

	public int MoveSecondary(List<Integer> conflicts)
	{
		if(turn%2==0)
		{
				return EvenMoveSecondary(conflicts);
		}
		else //On even turns, just make sure you can come back.
		{

				return OddMoveSecondary(conflicts);
		}
	}
	@Override
	public int attempt(List<Integer> conflicts) {
		++ turn;

		if(this.free_handles>3)
		{
				return MoveMain(conflicts);
		}

		else
		{
			if(!updated_probs)
			{	updateProb(conflicts);
				updated_probs = true;
			}
			return MoveSecondary(conflicts);

		}

		//return lastMove+1;
	}

}

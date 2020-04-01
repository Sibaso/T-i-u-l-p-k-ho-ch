package lol;

import java.util.ArrayList;
import java.util.HashSet;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class TSP {

	static {
		System.loadLibrary("jniortools");
	}
	
	int N=5;
	double INF=Double.POSITIVE_INFINITY;
	MPSolver solver= new MPSolver("TSP solver",MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
	MPVariable X[][]=new MPVariable[N][N];
	int[][] c = {
			{0,4,2,5,6}, {2,0,5,2,7}, {1,2,0,6,3}, {7,5,8,0,3}, {1,2,4,3,0}}; 
	
	private int findNext(int s){
		    for(int i = 0; i < N; i++) if(i != s && X[s][i].solutionValue() > 0) return i;
		    return -1;
	}
	public ArrayList<Integer> extractCycle(int s){
		    ArrayList<Integer> L = new ArrayList<Integer>();
		    int x = s;
		    while(true){
		      L.add(x);    x = findNext(x);
		      int rep = -1;
		      for(int i = 0; i < L.size(); i++)if(L.get(i) == x){
		        rep = i; break; }
		      if(rep != -1){
		        ArrayList<Integer> rL = new ArrayList<Integer>();
		        for(int i = rep; i < L.size(); i++) rL.add(L.get(i));
		        return rL;
		      }
		    }
		  }

	private void createSEC(HashSet<ArrayList<Integer>> S){
	    for(ArrayList<Integer> C: S){
	      MPConstraint sc = solver.makeConstraint(0, C.size() -1);
	      for(int i: C){
	        for(int j : C) if(i != j){
	          sc.setCoefficient(X[i][j], 1);
	        }
	      }
	    }
	  }

	private void createVAR() {
		
	}
	
	public void solveTSP() {
		
		
		HashSet<ArrayList<Integer>> S = new HashSet();
		boolean[]mark=new boolean[N];
		boolean found=false;
		while(!found) {
			createSEC(S);
			final MPSolver.ResultStatus resultStatus = solver.solve();
		    if (resultStatus != MPSolver.ResultStatus.OPTIMAL) {
		    	System.err.println("The problem does not have an optimal solution!");
		        return;
		    }
		    System.out.println("obj = " + solver.objective().value());
		    for(int i=0;i<N;i++)mark[i]=false;
		    for(int i=0;i<N;i++)if(!mark[i]) {
		    	ArrayList<Integer>C=extractCycle(i);
		    	if(C.size()<N) {
		    		S.add(C);
		    		for(int j:C) {
		    			mark[j]=true;
		    		}
		    	}
		    	else {
		    		found=true;
		    		break;
		    	}
		    }
		}
		ArrayList<Integer>tour=extractCycle(0);
	    for(int i=0;i<tour.size();i++)System.out.print(tour.get(i)+"->");
	    System.out.println(tour.get(0));
		
		
	
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		TSP app=new TSP();
		app.solveTSP();
	}

}

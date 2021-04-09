package test;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import java.util.Random;

public class KGraphPartitioning {
	int[][] c;
	int INF = Integer.MAX_VALUE;
	int V,K,alpha;
	MPVariable X[][];
	MPVariable Y[][];
	MPVariable max, min;
	
	static {
		System.loadLibrary("jniortools");
	}
	
	public KGraphPartitioning(int V, int K, int alpha, int seed) {
		this.V = V;
		this.K = K;
		this.alpha = alpha;
		Random r = new Random(seed);
		c = new int[V][V];
		for(int u = 0; u < V; u++) {
			for(int v = u+1; v < V; v++) {
				c[v][u] = r.nextInt(10);
				c[u][v] = c[v][u];
			}
		}
		for(int u = 0; u < V; u++) {
			for(int v = 0; v < V; v++) {
				System.out.print(c[u][v]+" ");
			}System.out.println();
		}
	}
	
	public void solve() {
		MPSolver solver= new MPSolver("KGraphPartitioning",MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		X = new MPVariable[V][K];
		Y = new MPVariable[V][V];
		max = solver.makeIntVar(V/K-alpha, V/K+alpha,  "max");
		min = solver.makeIntVar(V/K-alpha, V/K+alpha,  "min");
		for(int u = 0; u < V; u++) {
			for(int j = 0; j < K; j++) {
				X[u][j] = solver.makeIntVar(0, 1,  "X[" + u + "," + j + "]");
			}
		}
		for(int u = 0; u < V; u++) {
			for(int v = 0; v < V; v++) {
				Y[u][v] = solver.makeIntVar(0, 1,  "Y[" + u + "," + v + "]");
			}
		}
		for(int u = 0; u < V; u++) {
			MPConstraint e = solver.makeConstraint(1, 1);
			for(int j = 0; j < K; j++) {
				e.setCoefficient(X[u][j], 1);
			}
		}
		for(int j = 0; j < K; j++) {
			MPConstraint e = solver.makeConstraint(-V, 0);
			MPConstraint d = solver.makeConstraint(0, V);
			e.setCoefficient(max, -1);
			d.setCoefficient(min, -1);
			for(int u = 0; u < V; u++) {
				e.setCoefficient(X[u][j], 1);
				d.setCoefficient(X[u][j], 1);
			}
		}
		MPConstraint f = solver.makeConstraint(0, alpha);
		f.setCoefficient(max, 1);
		f.setCoefficient(min, -1);
		
		for(int i = 0; i < K; i++) {
			for(int j = 0; j < K; j++) {
				if(i == j)continue;
				for(int u = 0; u < V; u++) {
					for(int v = 0; v < V; v++) {
						if(u == v)continue;
						MPConstraint e = solver.makeConstraint(-3, INF);
						MPConstraint d = solver.makeConstraint(-INF, 5);
						e.setCoefficient(X[u][i], -2);
						e.setCoefficient(X[v][j], -2);
						e.setCoefficient(Y[u][v], 1);
						d.setCoefficient(X[u][i], 2);
						d.setCoefficient(X[v][j], 2);
						d.setCoefficient(Y[u][v], 1);
					}
				}
			}
		}
//		for(int i = 0; i < K; i++) {
//			for(int u = 0; u < V; u++) {
//				for(int v = 0; v < V; v++) {
//					if(u == v)continue;
//					MPConstraint e = solver.makeConstraint(-4, INF);
//					MPConstraint d = solver.makeConstraint(-INF, 4);
//					e.setCoefficient(X[u][i], -2);
//					e.setCoefficient(X[v][i], -2);
//					e.setCoefficient(Y[u][v], 1);
//					d.setCoefficient(X[u][i], 2);
//					d.setCoefficient(X[v][i], 2);
//					d.setCoefficient(Y[u][v], 1);
//				}
//			}
//		}
		MPObjective obj=solver.objective();
		for(int u = 0; u < V-1; u++) {
			for(int v = u+1; v < V; v++) {
				obj.setCoefficient(Y[u][v], c[u][v]);
			}
		}
		obj.setMinimization();
		MPSolver.ResultStatus rs=solver.solve();
		
		if(rs != MPSolver.ResultStatus.OPTIMAL) {
			System.out.println("no solution ");
		}
		else {
			System.out.println("solution "+obj.value());
			System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
			for(int j = 0; j < K; j++) {
				System.out.println("tap con "+j+" gom:");
				for(int u = 0; u < V; u++) {
					if(X[u][j].solutionValue() == 1)System.out.println("dinh "+u);
				}
			}
			for(int u = 0; u < V; u++) {
				for(int v = 0; v < V; v++) {
					if(Y[u][v].solutionValue() == 1)System.out.println(u+" khong cung tap voi "+v+" : +"+c[u][v]);
				}
			}
			System.out.println("max "+max.solutionValue());
			System.out.println("min "+min.solutionValue());
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		KGraphPartitioning kgp = new KGraphPartitioning(6,2,1,0);
		kgp.solve();
	}

}

package lol;

import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class ExampleMIP {

	static {
		System.loadLibrary("jniortools");
	}
	
	public void solveMIP() {
		double INF=Double.POSITIVE_INFINITY;
		MPSolver solver= new MPSolver("SimpleMIP",MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
		MPVariable x1=solver.makeIntVar(50, 100, "x1");
		MPVariable x2=solver.makeNumVar(0, 200, "x2");
		
		MPConstraint c1=solver.makeConstraint(17,27);
		c1.setCoefficient(x1, 1);
		c1.setCoefficient(x2, -20);
		MPConstraint c2=solver.makeConstraint(20,20);
		c2.setCoefficient(x1, 2);
		c2.setCoefficient(x2, -3);
		
		MPObjective obj=solver.objective();
		obj.setCoefficient(x1, 3);
		obj.setCoefficient(x2, 1);
		obj.setMaximization();
		
		MPSolver.ResultStatus rs=solver.solve();
		
		if(rs != MPSolver.ResultStatus.OPTIMAL) {
			System.out.println("no solution ");
		}
		else {
			System.out.println("obj: "+ obj.value());
			System.out.println("x1: "+ x1.solutionValue());
			System.out.println("x2: "+ x2.solutionValue());
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ExampleMIP e=new ExampleMIP();
		e.solveMIP();

	}

}

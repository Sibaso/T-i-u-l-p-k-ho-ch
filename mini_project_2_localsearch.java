package test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.eq.Eq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.RouteIndex;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;

public class mini_project_2_localsearch {

	int N,K;
	int INF=Integer.MAX_VALUE;
	int[][]D;//D[i][j] la khoang cach giua 2 point i,j
	int[]Q;//Q[k] la max people xe k co the cho
	ArrayList<Point> start; 
	ArrayList<Point> end;
	ArrayList<Point> clientStartPoints;
	ArrayList<Point> clientEndPoints;
	ArrayList<Point> allPoints; 
	ArrayList<Point>[] valiPoints; 
	ArcWeightsManager awm;// luu tru trong so tren canh noi giua cac point 
	NodeWeightsManager nwm;// luu tru so nguoi them vao routes khi di qua cac point 
	NodeWeightsManager[] res;// can bang
	NodeWeightsManager maxPeo;
	NodeWeightsManager npm;
	HashMap<Move, Integer> tabu;
	int tbl = 100;
	VRManager mgr; 
	VarRoutesVR XR;
	ConstraintSystemVR CS; 
	IFunctionVR obj;
	IFunctionVR[] traDis;// traDis[k] la travel distance cua route k  
	Random r = new Random();

	public mini_project_2_localsearch(int N,int K, int seed) {
		this.N=N;
		this.K=K;
		D=new int[2*N+1][2*N+1];
		Q=new int[K];
		data_generator(seed);
	}
	
	public void data_generator(int seed) {
		Random R = new Random(seed);
//		while(true) {
//			int check_sum=0;
//			for(int i=0;i<K;i++) {
//				Q[i]=R.nextInt(N)+1;
//				check_sum+=Q[i];
//			}
//			if(check_sum==N)break;
//		}
		for(int i=0;i<K;i++) {
			Q[i]=R.nextInt(N)+1;
		}
		for(int i=0;i<K;i++) {
			System.out.print(Q[i]+" ");
		}System.out.println();
		for(int i=0;i<=2*N;i++) {
			for(int j=0;j<=2*N;j++) {
				D[i][j]=R.nextInt(9)+1;
				if(i==j)D[i][j]=0;
				System.out.print(D[i][j]+" ");
			}System.out.println();
		}System.out.println();
	}
	
	public void mapping(){ 
		start = new ArrayList<Point>(); 
		end = new ArrayList<Point>(); 
		clientStartPoints = new ArrayList<Point>(); 
		clientEndPoints = new ArrayList<Point>(); 
		allPoints = new ArrayList<Point>();
		valiPoints = new ArrayList[N];
		// khoi tao cac diem bat dau va ket thuc cua cac xe (route) 
		for(int k = 1; k <= K; k++){ 
			Point s = new Point(0); 
			Point t = new Point(0); 
			start.add(s);   
			end.add(t); 
			allPoints.add(s);  
			allPoints.add(t); 
		}
		for(int i = 1; i <= N; i++){ 
			Point p = new Point(i);
			Point q = new Point(i+N);
			clientStartPoints.add(p);
			clientEndPoints.add(q);
			allPoints.add(p);
			allPoints.add(q);
		} 
		awm = new ArcWeightsManager(allPoints); 
		nwm = new NodeWeightsManager(allPoints);
		res = new NodeWeightsManager[N];
		maxPeo = new NodeWeightsManager(allPoints);
		npm = new NodeWeightsManager(allPoints);
		for(int k = 0; k < K; k++) {
			Point ps = start.get(k);
			Point pe = start.get(k);
			nwm.setWeight(ps, 0);
			nwm.setWeight(pe, 0);
			maxPeo.setWeight(ps, Q[k]);
			npm.setWeight(ps, 0);
			npm.setWeight(pe, 0);
			for(Point q: allPoints){ 
				awm.setWeight(ps,q, D[ps.ID][q.ID]); 
				awm.setWeight(pe,q, D[pe.ID][q.ID]); 
			}
		}
		for(int n = 0; n < N; n++) {
			Point ps = clientStartPoints.get(n);
			Point pe = clientEndPoints.get(n);
			nwm.setWeight(ps, 1);
			nwm.setWeight(pe, -1);
			npm.setWeight(ps, 1);
			npm.setWeight(pe, 0);
			for(Point q: allPoints){ 
				awm.setWeight(ps,q, D[ps.ID][q.ID]);
				awm.setWeight(pe,q, D[pe.ID][q.ID]); 
			}
		}
		for(int n = 1; n <= N; n ++) {
			res[n-1] = new NodeWeightsManager(allPoints);
			for(Point p: clientStartPoints) {
				if(p.ID == n)res[n-1].setWeight(p, 1);
				else res[n-1].setWeight(p, 0);
			}
			for(Point p: clientEndPoints) {
				if(p.ID == n + N)res[n-1].setWeight(p, -1);
				else res[n-1].setWeight(p, 0);
			}
			for(Point p: start)res[n-1].setWeight(p, 0);
			for(Point p: end)res[n-1].setWeight(p, 0);
		}
	}

	public void stateModel(){ 
		mgr = new VRManager(); 
		XR = new VarRoutesVR(mgr); 
		for(int i = 0; i < start.size(); i++){ 
			Point s = start.get(i); 
			Point t = end.get(i); 
			XR.addRoute(s, t);// them 1 route vao phuong an (s --> t) 
		} 
		// khai bao XR co the se di qua diem p
		for(Point p: clientStartPoints) XR.addClientPoint(p);
		for(Point p: clientEndPoints) XR.addClientPoint(p);
		// thiet lap rang buoc 
		CS = new ConstraintSystemVR(mgr); 
		AccumulatedWeightNodesVR accMaxContain = new AccumulatedWeightNodesVR(XR, maxPeo);
		AccumulatedWeightNodesVR accPeo = new AccumulatedWeightNodesVR(XR, nwm);//tich luy people 
		AccumulatedWeightEdgesVR accDis = new AccumulatedWeightEdgesVR(XR, awm);//tich luy distance
		AccumulatedWeightNodesVR accNumPeo = new AccumulatedWeightNodesVR(XR, npm);
		traDis = new IFunctionVR[K];// travel distance of routes
		for(int k = 1; k <= K; k++){ 
			Point tk = XR.endPoint(k);
			traDis[k-1] = new AccumulatedEdgeWeightsOnPathVR(accDis, tk);
			CS.post(new Leq(1, new AccumulatedNodeWeightsOnPathVR(accNumPeo, tk)));
		}
		for(int n = 1; n <= N; n++) {
			Point te = clientEndPoints.get(n-1);
			//diem i phai dung truoc va cung route voi diem i+N
			AccumulatedWeightNodesVR accVali = new AccumulatedWeightNodesVR(XR, res[n-1]);
			IFunctionVR vali = new AccumulatedNodeWeightsOnPathVR(accVali, te);
			CS.post(new Eq(vali, 0));
			Point ts = clientStartPoints.get(n-1);
			//xe k khong cho cung luc qua Q[k] nguoi
			//tai vi tri 1 nguoi len xe, xe khong chua qua so nguoi quy dinh
			IFunctionVR numPeo = new AccumulatedNodeWeightsOnPathVR(accPeo, ts);
			IFunctionVR maxContain = new AccumulatedNodeWeightsOnPathVR(accMaxContain, ts);
			CS.post(new Leq(numPeo, maxContain));
		}
		obj = new TotalCostVR(XR, awm);// tong khoang cach di chuyen cua K xe (route)
		mgr.close();
	}
		
	public void initialSolution(){ 
		ArrayList<Point> listPoints = new ArrayList<Point>(); 
		for(int k = 1; k <= XR.getNbRoutes(); k++){ 
			listPoints.add(XR.startPoint(k)); 
		} 
		for(int n = 0; n < N; n++){ 
			Point x = listPoints.get(r.nextInt(listPoints.size()));
			Point p = clientStartPoints.get(n);
			mgr.performAddOnePoint(p, x);
			listPoints.add(p);
			Point q = clientEndPoints.get(n);
			mgr.performAddOnePoint(q, p);
			listPoints.add(q);
			//System.out.println(XR.toString() + "violations = " + CS.violations() + ", cost = " + obj.getValue());
		}
		System.out.println(XR.toString() + "violations = " + CS.violations() + ", cost = " + obj.getValue());
	}

	class Move{ 
		Point x; 
		Point xn;
		Point x1;
		Point x1n;
		Point y;
		Point yn;
		Point y1;
		Point y1n;
		ArrayList<Point>X;
		ArrayList<Point>Y;
		public Move(Point x, Point xn, Point y, Point yn){ 
			this.x = x; 
			this.xn = xn;
			this.y = y;
			this.yn = yn;
		}
		public Move(Point x, Point y) {
			this.x = x;
			this.y = y;
		}
		public Move(Point x, Point xn, Point x1, Point x1n, Point y, Point yn, Point y1, Point y1n) {
			this.x = x;
			this.xn = xn;
			this.x1 = x1;
			this.x1n = x1n;
			this.y = y;
			this.yn = yn;
			this.y1 = y1;
			this.y1n = y1n;
		}
		public Move(ArrayList<Point> X, ArrayList<Point> Y) {
			this.X = X;
			this.Y = Y;
		}
	} 
	
	public void exploreOnePointMove(ArrayList<Move> cand) {
		cand.clear(); 
		int minDeltaC = Integer.MAX_VALUE; 
		double minDeltaF = minDeltaC; 
		for(int k = 1; k <= K; k++) {
			for(Point x = XR.next(XR.startPoint(k)); x != XR.endPoint(k); x = XR.next(x)){
				for(Point y = XR.startPoint(k); y != XR.endPoint(k); y = XR.next(y)){
					if(x != y && x != XR.next(y)) {
						int deltaC = CS.evaluateOnePointMove(x, y);
						double deltaF = obj.evaluateOnePointMove(x, y);
						if(deltaC < 0 || deltaC == 0 && deltaF < 0) {
							if(deltaC < minDeltaC || deltaC == minDeltaC && deltaF < minDeltaF){ 
								cand.clear(); 
								cand.add(new Move(x, y));
								minDeltaC = deltaC; 
								minDeltaF = deltaF; 
							}else if(deltaC == minDeltaC && deltaF == minDeltaF) 
								cand.add(new Move(x, y));
						}
					}
				}
			}
		}
	}
	
	public void exploreNeighborhood(ArrayList<Move> cand){ 
		cand.clear(); 
		int minDeltaC = Integer.MAX_VALUE; 
		double minDeltaF = minDeltaC; 
		for(int n = 0; n < N; n++) {
			Point x = clientStartPoints.get(n);
			Point xn = clientEndPoints.get(n);
			for(int k = 1; k <= K; k++){
				for(Point y = XR.startPoint(k); y != XR.endPoint(k); y = XR.next(y))if(y != x && y != xn){
					for(Point yn = y; yn != XR.endPoint(k); yn = XR.next(yn))if(yn != x && yn != xn){
						int deltaC = CS.evaluateTwoPointsMove(x, xn, y, yn); 
						double deltaF = obj.evaluateTwoPointsMove(x, xn, y, yn); 
						if(deltaC < 0 || deltaC == 0 && deltaF < 0) {
							if(deltaC < minDeltaC || deltaC == minDeltaC && deltaF < minDeltaF){ 
								cand.clear(); 
								cand.add(new Move(x, xn, y, yn));
								minDeltaC = deltaC;
								minDeltaF = deltaF;
							}else if(deltaC == minDeltaC && deltaF == minDeltaF)
								cand.add(new Move(x, xn, y, yn));
						}
					}
				}
			}
		}
	}
	
	
	public void exploreLargeNeighborhood(ArrayList<Move> cand) {
		cand.clear(); 
		int minDeltaC = Integer.MAX_VALUE; 
		double minDeltaF = minDeltaC;
		int index = r.nextInt(clientStartPoints.size());
		Point x = clientStartPoints.get(index), xn = clientEndPoints.get(index); 
		index = r.nextInt(clientStartPoints.size());
		Point x1 = clientStartPoints.get(index), x1n = clientEndPoints.get(index);
		int k = r.nextInt(K)+1;
		int k1 = r.nextInt(K)+1;
		for(Point y = XR.startPoint(k); y != XR.endPoint(k); y = XR.next(y))if(y!=x&&y!=xn&&y!=x1&&y!=x1n)
			for(Point yn = y; yn != XR.endPoint(k); yn = XR.next(yn))if(yn!=x&&yn!=xn&&yn!=x1&&yn!=x1n)
				for(Point y1 = XR.startPoint(k1); y1 != XR.endPoint(k1); y1 = XR.next(y1))if(y1!=x&&y1!=xn&&y1!=x1&&y1!=x1n)
					for(Point y1n = y1; y1n != XR.endPoint(k1); y1n = XR.next(y1n))if(y1n!=x&&y1n!=xn&&y1n!=x1&&y1n!=x1n){
						if(!XR.checkPerformFourPointsMove(x, xn, x1, x1n, y, yn, y1, y1n))continue;
						int deltaC = CS.evaluateFourPointsMove(x, xn, x1, x1n, y, yn, y1, y1n); 
						double deltaF = obj.evaluateFourPointsMove(x, xn, x1, x1n, y, yn, y1, y1n);
						if(!(deltaC < 0 || deltaC == 0 && deltaF < 0)) continue;
							if(deltaC < minDeltaC || deltaC == minDeltaC && deltaF < minDeltaF){ 
								cand.clear(); 
								cand.add(new Move(x, xn, x1, x1n, y, yn, y1, y1n));
								minDeltaC = deltaC;
								minDeltaF = deltaF;
							}else if(deltaC == minDeltaC && deltaF == minDeltaF)
								cand.add(new Move(x, xn, x1, x1n, y, yn, y1, y1n));
					}
	}
	
	public void exploreSwapNeighborhood(ArrayList<Move> cand) {
		cand.clear(); 
		Point x, xn, x1, x1n;
		Point y, yn, y1, y1n;
		Move m;
		while(true) {
			int index = r.nextInt(clientStartPoints.size());
			x = clientStartPoints.get(index); xn = clientEndPoints.get(index);
			while(true) {
				index = r.nextInt(clientStartPoints.size());
				x1 = clientStartPoints.get(index);
				if(x!=x1)break;
			}
			x1 = clientStartPoints.get(index); x1n = clientEndPoints.get(index);
			y = XR.prev(x1); yn = XR.prev(x1n); y1 = XR.prev(x); y1n = XR.prev(xn);
			while(y == x || y == xn || y == x1 || y == x1n)
				y = XR.prev(y);
			while(yn == x || yn == xn || yn == x1 || yn == x1n)
				yn = XR.prev(yn);
			while(y1 == x || y1 == xn || y1 == x1 || y1 == x1n)
				y1 = XR.prev(y1);
			while(y1n == x || y1n == xn || y1n == x1 || y1n == x1n)
				y1n = XR.prev(y1n);
			m = new Move(x, xn, x1, x1n, y, yn, y1, y1n);
			if(!tabu.containsKey(m))break;
		}
		cand.add(m);
		tabu.put(m, 1);
		//System.out.println(x+","+xn+","+x1+","+x1n+","+y+","+yn+","+y1+","+y1n);
	}
	
	public void tabuSearch() {
		initialSolution();
		System.out.println("\nSearching...\n");
		tabu = new HashMap<Move, Integer>();
		int it = 0, maxIter = 1000;
		int bestC = Integer.MAX_VALUE; 
		double bestF = bestC; 
		while(it < maxIter) {
			int index = r.nextInt(clientStartPoints.size());
			Point x = clientStartPoints.get(index);
			Point xn = clientEndPoints.get(index);
			index = r.nextInt(clientStartPoints.size());
			Point y = clientStartPoints.get(index);
			Point yn = clientEndPoints.get(index);
			while(y == x || y == xn || yn == x || yn == xn) {
				index = r.nextInt(clientStartPoints.size());
				y = clientStartPoints.get(index);
				yn = clientEndPoints.get(index);
			}
			Move m = new Move(x, xn, y, yn);
			if(!tabu.containsKey(m)) {
				tabu.put(m, 1);
				mgr.performTwoPointsMove(m.x, m.xn, m.y, m.yn);
				int C = CS.violations();
				double F = obj.getValue();
				if(C < bestC || C == bestC && F < bestF) {
					bestC = C;
					bestF = F;
					System.out.println("----NEW BEST----");
				}
				System.out.println("Step " + it + " move two point: " + m.x + ", " + m.xn);
				System.out.println(XR.toString() + "violations = " 
									+ CS.violations() + ", cost = " + obj.getValue());
				it++;
			}else {
				int deltaC = CS.evaluateTwoPointsMove(x, xn, y, yn); 
				double deltaF = obj.evaluateTwoPointsMove(x, xn, y, yn); 
				if((deltaC < 0 || deltaC == 0 && deltaF < 0)){ 
					mgr.performTwoPointsMove(m.x, m.xn, m.y, m.yn);
					bestC = CS.violations();
					bestF = obj.getValue();
					System.out.println("Step " + it + " move two point: " + m.x + ", " + m.xn);
					System.out.println(XR.toString() + "violations = " 
										+ CS.violations() + ", cost = " + obj.getValue());
					it++;
				}
			}
		}
		System.out.println("BEST Solution: violations = " 
				+ bestC + ", cost = " + bestF);
	}
	
	public void localSearch(int numSearch) {
		initialSolution();
		tabu = new HashMap<Move, Integer>();
		System.out.println("\nSearching...\n");
		int it = 0, maxIter = 50;
		ArrayList<Move> cand = new ArrayList<Move>();
		int bestC = Integer.MAX_VALUE;
		double bestF = bestC;
		if(numSearch == 2) {
			while(it < maxIter){
				exploreNeighborhood(cand); 
				if(cand.size() <= 0) {
					System.out.println("Reach local optimum\n");
					//if(stop)break;
					exploreSwapNeighborhood(cand);
					if(cand.size() <= 0) {
						System.out.println("Reach local optimum\n"); 
					}
					Move m = cand.get(r.nextInt(cand.size()));
					mgr.performFourPointsMove(m.x, m.xn, m.x1, m.x1n, m.y, m.yn, m.y1, m.y1n);
					int C = CS.violations();
					double F = obj.getValue();
					System.out.println("Step " + it + " Move four point: " + m.x + ", " + m.xn + ", " + m.x1 + ", " + m.x1n);
					System.out.println(XR.toString() + "violations = " 
										+ C + ", cost = " + F);
					it++;
					if(C < bestC || C == bestC && F < bestF) {
						bestC = C;
						bestF = F;
						System.out.println("----NEW BEST----");
					}
					continue;
				}
				Move m = cand.get(r.nextInt(cand.size()));
				mgr.performTwoPointsMove(m.x, m.xn, m.y, m.yn);
				int C = CS.violations();
				double F = obj.getValue();
				System.out.println("Step " + it + " move two point: " + m.x + ", " + m.xn);
				System.out.println(XR.toString() + "violations = " 
									+ C + ", cost = " + F);
				if(C < bestC || C == bestC && F < bestF) {
					bestC = C;
					bestF = F;
					System.out.println("----NEW BEST----");
				}		
				it++; 
			}
			System.out.println("BEST Solution: violations = " 
					+ bestC + ", cost = " + bestF);
		}
		else if(numSearch == 4) {
			while(it < maxIter){
				exploreLargeNeighborhood(cand); 
				if(cand.size() <= 0) {
					System.out.println("Reach local optimum"); 
					break;
				}
				Move m = cand.get(r.nextInt(cand.size()));
				System.out.println("Step " + it + " Move four point: " + m.x + ", " + m.xn + ", " + m.x1 + ", " + m.x1n);
				mgr.performFourPointsMove(m.x, m.xn, m.x1, m.x1n, m.y, m.yn, m.y1, m.y1n);
				System.out.println(XR.toString() + "violations = " 
									+ CS.violations() + ", cost = " + obj.getValue());
				it++;
			}
			while(it < maxIter){
				exploreNeighborhood(cand); 
				if(cand.size() <= 0) {
					System.out.println("Reach local optimum"); 
					break;
				}
				Move m = cand.get(r.nextInt(cand.size()));
				System.out.println("Step " + it + " move two point: " + m.x + ", " + m.xn);
				mgr.performTwoPointsMove(m.x, m.xn, m.y, m.yn);
				System.out.println(XR.toString() + "violations = " 
									+ CS.violations() + ", cost = " + obj.getValue());
				it++; 
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		mini_project_2_localsearch model=new mini_project_2_localsearch(10,4,1);
		model.mapping();
		model.stateModel();
		long start = System.currentTimeMillis();    
		model.localSearch(2);
		//model.tabuSearch();
		long end = System.currentTimeMillis();
        long t = end - start;
        System.out.println("Search time: " + t + " millisecond");
	}

}
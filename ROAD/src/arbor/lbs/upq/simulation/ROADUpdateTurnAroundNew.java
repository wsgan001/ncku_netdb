package arbor.lbs.upq.simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import arbor.foundation.time.ExecTimer;
import arbor.lbs.uqp.algorithm.ChoosePathUnit;
import arbor.lbs.uqp.algorithm.VisitedObject;
import arbor.lbs.uqp.graph.dijkstra.Dijkstra;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.MsgObj;
import arbor.lbs.uqp.graph.util.MsgObjPath;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.ShortCutSet;
import arbor.lbs.uqp.graph.util.Vertex;

public class ROADUpdateTurnAroundNew {
	//static int num_k = 5;
	ArrayList<Integer> queryList;
	ArrayList<Integer> insertList;
	//HashMap<Integer,Integer> visitNodeMap = new HashMap<Integer,Integer>();
	
    public static void main(String[] args) throws IOException {
    	/**
    	 * Code sequence:
    	 * 1. load graph pool (serialize)
    	 * 2. load rnet hierarchy (serialize)
    	 * 3. load object pool
    	 * 4. check knn sequentially
    	 */
    	int num_k = Integer.valueOf(args[4]);
    	int updateFreq = Integer.valueOf(args[5]);
    	int factor = Integer.valueOf(args[9]);
    	int winSize = Integer.valueOf(args[10]);
    	int intputSize = Integer.valueOf(args[11]);
    	ROADUpdateTurnAroundNew road = new ROADUpdateTurnAroundNew(args[0],args[1],args[2],args[3],args[7]);
    	
		double currT = 0;
		double turnAroundT = 0;
		double totalExecT = 0;
    	int cases = 0;
    	
    	double maxTAT = 0, minTAT = 100;
    	ExecTimer timer = new ExecTimer();
    	//int querySize = road.queryList.size();
    	int qIdx = 0, uIdx = 0;
    	double execTime = 0, arrivalTime = 0;
    	BufferedReader in = new BufferedReader(new FileReader(args[6]));
		String str;
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(args[8])));
		int inputCnt = 0;
		double prevArrT = 0;
		double averageTAT = 0;
		int averageCnt = 0;
		double tmpMaxTAT = 0;
		double tmpMinTAT = 1;
		while ((str = in.readLine()) != null) {
			String[] strSplit = str.split(" ");
			arrivalTime = Double.valueOf(strSplit[1]);
			if (strSplit[0].equals("Q")) {
				timer.setNanoStartTime("Query Performance");
				ArrayList<VisitedObject> results = road.getKNN(road.queryList.get(qIdx), num_k);
				execTime = timer.setNanoEndTime().getMillisSeconds();
				qIdx++;
				if (results.size()!=num_k) 
					cases++;
			}
			else {
				
				timer.setNanoStartTime("Query Performance");
				int vID = road.insertList.get(uIdx);
				if (uIdx%2==0) {
					road.insertObject(vID);
				}
				else {
					road.deleteObject(vID);
				}
				execTime = timer.setNanoEndTime().getMillisSeconds();
				uIdx++;
			}
        	
    		//execTime /= 1000;
    		//System.out.println(currT+" "+arrivalT[l]+" "+execTime);
    		totalExecT += execTime;
    		double printTAT = 0;
    		if (arrivalTime>=currT) {
    			turnAroundT += execTime;
    			currT = arrivalTime+execTime;
    			if (execTime>maxTAT)
    				maxTAT = execTime;
    			if (execTime<minTAT)
    				minTAT = execTime;
    			printTAT = execTime;
    			//printTAT = 0;
    		}
    		else {
    			double waitingT = currT-arrivalTime;
    			turnAroundT += waitingT;
    			turnAroundT += execTime;
    			currT = currT+execTime;
    			if (waitingT+execTime>maxTAT)
    				maxTAT = waitingT+execTime;
    			if (waitingT+execTime<minTAT)
    				minTAT = waitingT+execTime;
    			printTAT =  waitingT+execTime;
    			//printTAT =  waitingT;
    		}
    		if (arrivalTime==prevArrT) {
    			averageTAT += printTAT;
    			averageCnt++;
    			if (printTAT>tmpMaxTAT)
    				tmpMaxTAT = printTAT;
    			if (printTAT<tmpMinTAT)
    				tmpMinTAT = printTAT;
    		}
    		else {
    			out.write(prevArrT+" "+(double)averageTAT/averageCnt+" "+averageCnt+"\n");
    			//out.write(prevArrT+" "+tmpMaxTAT+" "+averageCnt+"\n");
    			averageCnt = 1;
    			tmpMaxTAT = printTAT;
    			tmpMinTAT = printTAT;
    			averageTAT = printTAT;
    		}
    		prevArrT = arrivalTime;
    		//if (inputCnt%10==9)
    		//	out.write(printTAT+"\n");
    		//System.out.println(currT+" "+turnAroundT+" "+cnt);
    		if (qIdx%1000==999) {
    			//System.out.println("ROAD Turn Around Time:"+turnAroundT);
    	    	//System.out.println("ROAD Total Exec Time:"+totalExecT);
    		}
    		inputCnt++;
    	}
		in.close();
		out.close();
    	System.out.println(uIdx);
    	System.out.println("ROAD Turn Around Time:"+turnAroundT);
    	System.out.println("ROAD Total Exec Time:"+totalExecT);
    	System.out.println(cases);
    	System.out.println("KNN Search Complete");
    	System.out.println("MAX Turn Around Time:"+maxTAT);
    	System.out.println("MIN Turn Around Time:"+minTAT);
    	//int threshold = (int) (querySize*0.02);
    	//road.printNodeExpansion(args[0],threshold);
    }
	
    /*
	private void printNodeExpansion(String fn, int threshold ) throws IOException {
		// TODO Auto-generated method stub
		fn += ".cnt_"+String.valueOf(threshold)+".txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fn)));
		writer.write("x,y"+"\n");
		Iterator<Integer> visitNodeIter = visitNodeMap.keySet().iterator();
		while (visitNodeIter.hasNext()) {
			int nID = visitNodeIter.next();
			int cnt = visitNodeMap.get(nID);
			if (cnt>threshold) {
				Vertex v = GraphPool.getSignleton().getVertex(nID);
				if (v.isBorderNode(4)) {
					double[] xy = v.getloc().m_pCoords;
					writer.write(String.valueOf(xy[0]));
					writer.write(",");
					writer.write(String.valueOf(xy[1]));
					writer.write("\n");
				}
			}
		}
		writer.close();
	}*/

	private void insertObject(int vID) {
		// TODO Auto-generated method stub
		Vertex v = GraphPool.getSignleton().getVertex(vID);
		
		int msgSize = MsgObjPool.getSignleton().getMsgSize();
		
		MsgObj obj = new MsgObj(msgSize, v.getloc().getCoord(0), v.getloc().getCoord(1));
		obj.setAssocVertex(vID);
		MsgObjPool.getSignleton().insertMsg(obj);
		v.insertAssocMsgObj(msgSize);
		  
		  int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		  for (int i=0; i<=maxLevel; i++) {
			  int rID = v.getInsideRNetID(i);
			  RNet rnet = RNetHierarchy.getSignleton().getRNet(rID);
			  //rnet.setContainObj(true); 
			  rnet.increaseObjCnt(1);
		  }
	}
	
	private void deleteObject(int vID) {
		// TODO Auto-generated method stub
		Vertex v = GraphPool.getSignleton().getVertex(vID);
		
		v.deleteOneAssocMsgObj();
		
		  int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		  for (int i=0; i<=maxLevel; i++) {
			  int rID = v.getInsideRNetID(i);
			  RNet rnet = RNetHierarchy.getSignleton().getRNet(rID);
			  //rnet.setContainObj(true); 
			  rnet.decreaseObjCnt(1);
		  }
	}

	public ROADUpdateTurnAroundNew(String networkFn, String hierFn, String msgFn,
			String queryFn, String updateFn) throws IOException {
		GraphPool.loadGraph(networkFn);
		RNetHierarchy.loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		
		String fn = queryFn+".loc.txt";
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fn)));
		writer.write("x,y"+"\n");
		
		queryList = new ArrayList<Integer>();
		BufferedReader in = new BufferedReader(new FileReader(queryFn));
		String str;
		while ((str = in.readLine()) != null) {
			Integer vID = Integer.valueOf(str);
			queryList.add(vID);
			Vertex v = GraphPool.getSignleton().getVertex(vID);
			double[] xy = v.getloc().m_pCoords;
			writer.write(String.valueOf(xy[0]));
			writer.write(",");
			writer.write(String.valueOf(xy[1]));
			writer.write("\n");
		}
		in.close();
		writer.close();
		
		insertList = new ArrayList<Integer>();
		in = new BufferedReader(new FileReader(updateFn));
		while ((str = in.readLine()) != null) {
			Integer vID = Integer.valueOf(str);
			insertList.add(vID);
		}
		in.close();
	}
	public ROADUpdateTurnAroundNew() {
		
	}
	private boolean doSettle(TreeSet<MsgObjPath> settleSet, TreeSet<MsgObjPath> unSettleSetTreeSet,
			              TreeSet<MsgObjPath> checkSet) {
		double minInUnsettle;
		
		Object[] key = checkSet.toArray();
		for (int i=0;i<key.length;i++) {
			MsgObjPath p = (MsgObjPath)key[i];
			unSettleSetTreeSet.add(p);
			
		}		

		minInUnsettle = unSettleSetTreeSet.first().cost;
		boolean finish = false;		
		while (!finish) {
			MsgObjPath p = unSettleSetTreeSet.first();
			if (p.hasTouchObj()) {
				unSettleSetTreeSet.pollFirst();
				settleSet.add(p);				
			}
			else {
				finish = true;
			}
		}
		int num_k=0;
		if (settleSet.size() >= num_k) {
			return true;
		}
		else {
			return false;
		}
				
	}
    public ArrayList<MsgObjPath> getRoadKNN(int srcVID) {
    	Vertex srcV = GraphPool.getSignleton().getVertex(srcVID);
    	RNet leafR = RNetHierarchy.getSignleton().getRNet(srcV.getLeafRnetID());
    	TreeSet<Path> queue = new TreeSet<Path>();
    	TreeSet<MsgObjPath> settleSet = new TreeSet<MsgObjPath>();
    	TreeSet<MsgObjPath> unSettleSet = new TreeSet<MsgObjPath>();
    	
    	boolean finish = false;
    	
    	if (leafR.hasMsgObjInside()) {    		
    		Dijkstra dijkstra = new Dijkstra(leafR.getGraph());
    		dijkstra.execute(srcVID);
    		TreeSet<MsgObjPath> msgset = dijkstra.getPath2Objects(leafR);
    		    		
    		finish=doSettle(settleSet, unSettleSet, msgset);
    		
    		if (!finish) {
    		  TreeSet<MsgObjPath> borset = dijkstra.getPath2Borders(leafR);
    		  finish = doSettle(settleSet, unSettleSet, borset);
    		}    		  
    	}
    	else { 
    		Dijkstra dijkstra = new Dijkstra(leafR.getGraph());
    		dijkstra.execute(srcVID);
    		TreeSet<MsgObjPath> borset = dijkstra.getPath2Borders(leafR);
    		finish = doSettle(settleSet, unSettleSet, borset);
    		
    	}
    	
    	TreeSet<ChoosePathUnit> pqueue = new TreeSet<ChoosePathUnit>();
    	while (!finish) {
    		MsgObjPath first = unSettleSet.pollFirst();
    		int visitedLevel = first.currentVisitedRNetLevel;
    		int vID = first.getTailVID();
    		Vertex v = GraphPool.getSignleton().getVertex(vID);
    		if (v.getHighestBorderLevel() < 0) {
    			System.out.println("v must be a border");
    			System.exit(0);
    		}
    		
    		RNet highR = RNetHierarchy.getSignleton().getRNet(v.getInsideRNetID(v.getHighestBorderLevel()));
    		    		
    		if (!highR.isLeafRNet()) {
    			ChoosePathUnit unit = new ChoosePathUnit();
        		unit.rnet = highR.getRNetID();
        		unit.path = first.clone();
        		pqueue.add(unit);
    		}
    		else {
    			/*leaf, search silbing*/
    			List<Integer> nlist = v.getNeighbors();
    			for (int i=0;i<nlist.size();i++) {
    				Vertex anoV = GraphPool.getSignleton().getVertex(nlist.get(i));
    				
    			}
    		}
    		
    	}
    	
    	return null;
    }
    public ArrayList<VisitedObject> getKNN(int srcVID, int k) throws IOException {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	int expNodeCnt = 0;
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
    	int popsize=0;
    	while ((queue.size()>0)&&(kNNs.size()<k)) {
    		/*
    		if (expNodeCnt%50==0) { //simulate reading data from page
    			String input = "test.txt";
    			BufferedReader in = new BufferedReader(new FileReader(input));
    			String str;
    			while ((str = in.readLine()) != null) {
    				int lng = str.length();
    				if (lng==0)
    					System.out.println("The string is empty.");
    				
    			}
    			in.close();
    		}*/
    		VisitedObject popObj = queue.pollFirst();
    		//popsize++;
    		if ((visitedN.contains(popObj.getVID()))||(visitedO.contains(popObj.getMsgID()))) {
    			continue;
    		}
    		if (popObj.getVID()>=0) {  //pop up object is a node
    			Vertex popVertex = GraphPool.getSignleton().getVertex(popObj.getVID());
    			LinkedList<Integer> msgObj = popVertex.getAssocMsgObj();
    			for (int i=0; i<msgObj.size(); i++) {
    				VisitedObject oneObj = new VisitedObject(-1,msgObj.get(i),popObj.getDist()); 
    				queue.add(oneObj);
    			}
    			ChoosePath(queue,popVertex,popObj.getDist(),visitedN,visitedO);
    			expNodeCnt++;
    			visitedN.add(popObj.getVID());
    			/*
    			if (visitNodeMap.containsKey(popObj.getVID())) {
    				int cnt = visitNodeMap.get(popObj.getVID());
    				cnt++;
    				visitNodeMap.put(popObj.getVID(), cnt);
    			}
    			else {
    				visitNodeMap.put(popObj.getVID(), 1);
    			}*/
    		}
    		else { //pop up object is a msgObj
    			kNNs.add(popObj);
    			visitedO.add(popObj.getMsgID());
    		}
    	}
    	
		return kNNs;
    }
    private void ChoosePath(TreeSet<VisitedObject> queue, Vertex popVertex,
			double dist, HashSet<Integer> visitedN, HashSet<Integer> visitedO) {
		// TODO Auto-generated method stub
    	int leafRID = popVertex.getLeafRnetID();
    	RNet leafRnet = RNetHierarchy.getSignleton().getRNet(leafRID);
    	int leafLevel = leafRnet.getLevel();
    	if (popVertex.isBorderNode(leafLevel)) {
    		int highestLevel = popVertex.getHighestBorderLevel();
    		for (int i=highestLevel; i<=leafLevel; i++) {
    			int tmpRID = popVertex.getInsideRNetID(i);
    	    	RNet tmpRNet = RNetHierarchy.getSignleton().getRNet(tmpRID);
    	    	if (tmpRNet.hasMsgObjInside()) {
    	    		if (tmpRNet.isLeafRNet()) {
    	    			List<Integer> neighbors = popVertex.getNeighbors();
    	        		for (int j = 0; j<neighbors.size(); j++) {
    	        			int id = neighbors.get(j).intValue();
    	        			Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
    	        			double newDist = dist+e.getWeight();
    	        			VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
    	    				queue.add(oneObj);
    	        		}
    	    		}
    	    		continue;
    	    	}
    	    	else {
    	    		ShortCutSet scSet = tmpRNet.getShortCutSet(popVertex.getId());
    	    		if (scSet==null)
    	    			break;
    	    		HashMap<Integer,Path> scMap = scSet.getSCMap();
    	    		Iterator<Integer> scMIter = scMap.keySet().iterator();
    	    		while (scMIter.hasNext()) {
    	    			Integer destID = (Integer)scMIter.next();
    	    			double newDist = dist+scMap.get(destID).cost;
    	    			VisitedObject oneObj = new VisitedObject(destID.intValue(),-1,newDist); 
    	    			queue.add(oneObj);
    	    		}
    	    		List<Integer> neighbors = popVertex.getNeighbors();
	        		for (int j = 0; j<neighbors.size(); j++) {
	        			int id = neighbors.get(j).intValue();
	        			Vertex neiVertex = GraphPool.getSignleton().getVertex(id);
	        			if (neiVertex.getInsideRNetID(i)!=popVertex.getInsideRNetID(i)) {
	        				Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
    	        			double newDist = dist+e.getWeight();
    	        			VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
    	    				queue.add(oneObj);
	        			}
	        		}
    	    		break;
    	    	}
    		}
    	}
    	else {
    		List<Integer> neighbors = popVertex.getNeighbors();
    		for (int i = 0; i<neighbors.size(); i++) {
    			int id = neighbors.get(i).intValue();
    			Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
    			double newDist = dist+e.getWeight();
    			VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
				queue.add(oneObj);
    		}
    	}
		
	}
}

package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;

import static uk.ac.bris.cs.scotlandyard.model.Piece.*;

public class MyAi implements Ai {


	@Nonnull @Override public String name() { return "MyAi"; }

	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			@Nonnull AtomicBoolean terminate) {

		var moves = board.getAvailableMoves().asList();
		Map<Move, Double> Scores = new HashMap<>();   //Map a move to a score
		//only doing for MRx
		for (Move move : moves){
			double score = Score(move, board);
			Scores.put(move, score);
		}
		return max(Scores, List.copyOf(moves));

	}


	public double Score(Move move, Board board) {  //Calculating avarage distance from move.destination to other detectives location.
		return move.visit(new Move.Visitor<Double>() {
			@Override
			public Double visit(Move.SingleMove move) {
				double SumOfDist = 0;
				ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> Map = board.getSetup().graph;
				Map<Integer, Double> distance = Dijkstra(Map, move.destination); //return list of shortest distance from the move destination to all other nodes
				List<Piece> detectives = new ArrayList<>();
				List<Piece> Players = List.copyOf(board.getPlayers());
				for (Piece player : Players) detectives.add(player);
				detectives.removeIf(Piece::isMrX);
				List<Integer> DetectiveLocation = new ArrayList<>();
				for (Piece detective : detectives){
					if (!board.getDetectiveLocation((Detective) detective).isEmpty()) {
						SumOfDist += distance.get(board.getDetectiveLocation((Detective) detective).get());  //Get the sum of all the distances from the move destination to
					}                                             //the location of detectives.
				}

				return SumOfDist/detectives.size();
			}
			@Override
			public Double visit(Move.DoubleMove move) {
				double SumOfDist = 0;
				ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> Map = board.getSetup().graph;
				Map<Integer, Double> distance = Dijkstra(Map, move.destination2); //return list of shortest distance from the move destination to all other nodes
				List<Piece> detectives = new ArrayList<>();
				List<Piece> Players = List.copyOf(board.getPlayers());
				for (Piece player : Players) detectives.add(player);
				detectives.removeIf(Piece::isMrX);
				List<Integer> DetectiveLocation = new ArrayList<>();
				for (Piece detective : detectives){
					SumOfDist += distance.get(board.getDetectiveLocation((Detective) detective).get());  //Get the sum of all the distances from the move destination to
					//the location of detectives.
				}

				return SumOfDist/detectives.size();
			}
		});


	}
	public Map<Integer, Double> Dijkstra(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> Map, Integer source){
		List<Integer> Nodes = new ArrayList<>();
		for (int Node : Map.nodes()) Nodes.add(Node);
		List<Integer> Visited = new ArrayList<>();
		Map<Integer, Double> dist = new HashMap<>();
		for (Integer node : Nodes){
			if (node.equals( source)) dist.put(node, 0.0);
			else dist.put(node, 999999.0);
		}// initializing dist.
		int counter = 0;
		while (!Nodes.isEmpty()){
			counter ++;
			Integer node = min(dist, Nodes);   //remove the node in Nodes with min distance
			Nodes.remove(node);
			Visited.add(node);

			for (Integer adjacentNode : Map.adjacentNodes(node)) {
				if (dist.get(node) + weight(node, adjacentNode) < dist.get(adjacentNode))
					dist.replace(adjacentNode, dist.get(node) + weight(node, adjacentNode));
			}
		}


		return dist;
	}
	public Integer weight(Integer source, Integer adjacent){ //weighting function calculating relative distance to adjacent nodes.
		return 1;
	}
	public  <T> T min(Map<T, Double> dist, List<T> nodes){ // Take a map, a list of keys and returns the key in that list with the minimum key value.
		Double Min = 999999.0;
		T MinNode = null;
		for (Map.Entry<T, Double> entry : dist.entrySet()){
			if(entry.getValue() < Min && nodes.contains(entry.getKey())){
				Min = entry.getValue();
				MinNode = entry.getKey();
			}
		}
		return MinNode;
	}
	public  <T> T max(Map<T, Double> dist, List<T> nodes){ // Take a map, a list of keys and returns the key in that list with the maximum key value.
		Double Max = -999999.0;
		T MaxNode = null;
		for (Map.Entry<T, Double> entry : dist.entrySet()){
			if(entry.getValue() > Max && nodes.contains(entry.getKey())){
				Max = entry.getValue();
				MaxNode = entry.getKey();
			}
		}
		return MaxNode;
	}

}


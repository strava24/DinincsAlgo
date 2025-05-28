# Dinic's Algorithm with Dijkstra's Optimization

This repository holds a solution for maximum flow problem in weighted networks.

## Usage

unzip the benchmarks zip file and run the application. The networks are designed in the following manner.

```
4         // 4 nodes, numbered 0...3; node 0 is the source, node 3 the target.        
0  1  6   // Edge from node 0 to node 1 with capacity 6     
0  2  4   // Edge from node 0 to node 2 with capacity 4     
1  2  2   // Edge from node 1 to node 2 with capacity 2     
1  3  3   // Edge from node 1 to node 3 with capacity 3 
2  3  5   // Edge from node 2 to node 3 with capacity 5
```

## Algorithm 
 - Building a level graph using BFS
 - Using Dijkstra (with max-capacity priority) to find augmenting paths within that level graph

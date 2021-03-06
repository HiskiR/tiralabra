package pathfinding.algorithms;

import pathfinding.datastructures.MinHeap;
import pathfinding.datastructures.Node;

/**
 *
 * Class contains JPS-algorithm
 */
public class JPS {

    private char[][] grid; //map as a grid
    private double[][] gGrid; //grid containing distances from start
    private boolean[][] visited; //keeps track of which nodes have been visited
    private int gridLength;
    private int gridWidth;
    private int[] endPoint;
    private MinHeap heap;
    private Node endNode;
    //keeps count of the most time consuming operations performed 
    private int operations;

    //possible directions
    private final int[][] direction = {{-1, 0}, {1, 0}, {0, -1}, {0, 1},
                                       {-1, -1}, {-1, 1}, {1, 1}, {1, -1}};

    /**
     * Heuristic function.
     *
     * @param x yhe x-coordinate of the node
     * @param y the y-coordinate of the node
     * @return the euclidean distance from given coordinates to the end point.
     */
    private double heuristic(int y, int x) {
        int yd = y - endPoint[0];
        int xd = x - endPoint[1];
        return Math.sqrt(yd * yd + xd * xd);
    }

    /**
     * Returns the shortest path from the start point to the end point.
     *
     * @param map map as a grid
     * @param start starting point
     * @param end ending point
     * @return length of the shortest path if a path is found. else -1
     */
    public double shortestPath(char[][] map, int[] start, int[] end) {
        gridLength = map.length;
        gridWidth = map[0].length;
        grid = map;
        gGrid = new double[gridLength][gridWidth]; //grid containing g-values
        visited = new boolean[gridLength][gridWidth];
        endPoint = end;
        heap = new MinHeap(gridLength * gridWidth);
        operations = 0;

        if (map[start[0]][start[1]] == '@' || map[end[0]][end[1]] == '@') {
            return -1;
        }

        for (int i = 0; i < gridLength; i++) {
            for (int j = 0; j < gridWidth; j++) {
                gGrid[i][j] = Integer.MAX_VALUE;
            }
        }
        if (search(start[0], start[1])) {
            return gGrid[end[0]][end[1]];
        }
        return -1;
    }

    /**
     * Handles the search.
     *
     * @param y the y-coordinate of the starting point
     * @param x the x-coordinate of the starting point
     * @return true if the end point was reached, else false
     */
    private boolean search(int y, int x) {
        gGrid[y][x] = 0;
        heap.add(new Node(y, x, 0, heuristic(y, x)));
        operations++;

        while (!heap.isEmpty()) {
            Node n = heap.poll();
            operations++;
            int newY = n.getY();
            int newX = n.getX();

            visited[newY][newX] = true;

            if (newY == endPoint[0] && newX == endPoint[1]) {
                endNode = n;
                return true;
            }
            successors(n);
        }
        return false;
    }

    /**
     * Finds successors of a node.
     *
     * @param node the node whose successors are being searched.
     */
    private void successors(Node node) {
        int[][] neighbors = findNeighbors(node);

        for (int[] n : neighbors) {
            if (n == null) {
                continue;
            }
            int y = node.getY();
            int x = node.getX();
            int[] jumpPoint = jump(n[0], n[1], y, x);

            if (jumpPoint != null) {

                int jy = jumpPoint[0];
                int jx = jumpPoint[1];

                if (visited[jy][jx]) {
                    continue;
                }

                //distance from start to the jump point
                double newG = Math.sqrt(abs(y - jy) * abs(y - jy)
                        + abs(x - jx) * abs(x - jx)) + node.getG();
                if (newG <= gGrid[jy][jx]) {
                    gGrid[jy][jx] = newG;
                    Node newNode = new Node(jy, jx, newG, heuristic(jy, jx));
                    newNode.setParent(node);
                    heap.add(newNode);
                    operations++;
                }
            }
        }

    }

    /**
     * Searches for a jump point.
     *
     * @param y the y-coordinate of the node from which the jump point is being
     * searched.
     * @param x the x-coordinate of the node from which the jump point is being
     * searched.
     * @param py the y-coordinate of the node towards of which the jump point is
     * being searched.
     * @param px the x-coordinate of the node towards of which the jump point is
     * being searched.
     * @return the coordinates of the jump point
     */
    private int[] jump(int y, int x, int py, int px) {

        int[] jumpPoint = new int[]{y, x};

        if (!valid(y, x)) {
            return null;
        }

        if (y == endPoint[0] && x == endPoint[1]) {
            return jumpPoint;
        }

        int dy = y - py;
        int dx = x - px;

        //diagonal forced neighbors
        if (dy != 0 && dx != 0) {

            if (valid(y + dy, x - dx) && !valid(y, x - dx)
                    || valid(y - dy, x + dx) && !valid(y - dy, x)) {
                return jumpPoint;
            }
            //checking for vertical and horizontal jump points
            if (jump(y + dy, x, y, x) != null || jump(y, x + dx, y, x) != null) {
                return jumpPoint;
            }
        } else {
            //vertical forced neighbors
            if (dy != 0) {
                if (valid(y + dy, x + 1) && !valid(y, x + 1)
                        || valid(y + dy, x - 1) && !valid(y, x - 1)) {
                    return jumpPoint;
                }
                //horizontal forced neighbors
            } else {
                if (valid(y + 1, x + dx) && !valid(y + 1, x)
                        || valid(y - 1, x + dx) && !valid(y - 1, x)) {
                    return jumpPoint;
                }
            }
        }

        return jump(y + dy, x + dx, y, x);
    }

    /**
     * Searches neighbors for the given node.
     *
     * @param node the node of which neighbors are being searched.
     * @return list of coordinates of the found neighbors
     */
    private int[][] findNeighbors(Node node) {
        int[][] neighbors = new int[8][];
        int i = 0;
        Node parent = node.getParent();
        int y = node.getY();
        int x = node.getX();

        //if the node has a parent some neighbors can be ignored
        if (parent != null) {
            //direction
            int dy = (node.getY() - parent.getY())
                    / max(abs(node.getY() - parent.getY()), 1);
            int dx = (node.getX() - parent.getX())
                    / max(abs(node.getX() - parent.getX()), 1);

            //diagonal neighbors
            if (dy != 0 && dx != 0) {
                if (valid(y + dy, x)) {
                    neighbors[i++] = new int[]{y + dy, x};
                }
                if (valid(y, x + dx)) {
                    neighbors[i++] = new int[]{y, x + dx};
                }
                if (valid(y + dy, x + dx)) {
                    neighbors[i++] = new int[]{y + dy, x + dx};
                }
                if (!valid(y - dy, x)) {
                    neighbors[i++] = new int[]{y - dy, x + dx};
                }
                if (!valid(y, x - dx)) {
                    neighbors[i++] = new int[]{y + dy, x - dx};
                }
            } else {
                //vertical neighbors
                if (dy == 0) {
                    if (valid(y, x + dx)) {
                        neighbors[i++] = new int[]{y, x + dx};
                    }
                    if (!valid(y + 1, x)) {
                        neighbors[i++] = new int[]{y + 1, x + dx};
                    }
                    if (!valid(y - 1, x)) {
                        neighbors[i++] = new int[]{y - 1, x + dx};
                    }
                    //horizontal neighbors
                } else {
                    if (valid(y + dy, x)) {
                        neighbors[i++] = new int[]{y + dy, x};
                    }
                    if (!valid(y, x + 1)) {
                        neighbors[i++] = new int[]{y + dy, x + 1};
                    }
                    if (!valid(y, x - 1)) {
                        neighbors[i++] = new int[]{y + dy, x - 1};
                    }
                }
            }
        } else {
            for (i = 0; i < direction.length; i++) {
                int newY = y + direction[i][0];
                int newX = x + direction[i][1];
                if (valid(newY, newX)) {

                    neighbors[i] = new int[]{newY, newX};
                }
            }
        }
        return neighbors;
    }

    /**
     * Returns the found path and jump points.
     *
     *
     * @return the found path as an ASCII-grid.
     */
    public char[][] getPath() {
        char[][] path = grid;
        Node node = endNode;
        while (node != null) {
            int y = node.getY();
            int x = node.getX();
            path[y][x] = 'j';
            if (node.getParent() != null) {
                int py = node.getParent().getY();
                int px = node.getParent().getX();

                while (true) {
                    if (y == py && x == px) {
                        break;
                    }
                    if (py > y && px > x) {
                        path[--py][--px] = 'j';
                    } else if (py < y && px > x) {
                        path[++py][--px] = 'j';
                    } else if (py > y && px < x) {
                        path[--py][++px] = 'j';
                    } else if (py < y && px < x) {
                        path[++py][++px] = 'j';
                    } else if (py == y && px > x) {
                        path[py][--px] = 'j';
                    } else if (py == y && px < x) {
                        path[py][++px] = 'j';
                    } else if (py > y && px == x) {
                        path[--py][px] = 'j';
                    } else if (py < y && px == x) {
                        path[++py][px] = 'j';
                    }
                }
            }
            node = node.getParent();
        }
        return path;
    }

    /**
     * Checks if the given coordinates are inside the grid and are not blocked.
     *
     * @param y y-coordinate.
     * @param x x-coordinate.
     * @return true if coordinates are valid, else false
     */
    private boolean valid(int y, int x) {
        return !(y < 0 || x < 0 || y >= gridLength
                || x >= gridWidth || grid[y][x] == '@');
    }

    private int abs(int n) {
        return n > 0 ? n : -n;
    }

    private int max(int a, int b) {
        return a > b ? a : b;
    }

    /**
     * Returns the number of the most time consuming operations performed. The
     * most time consuming operation is adding to the heap and polling from it.
     *
     * @return the the number of nodes added to the heap and polled from it.
     */
    public int getOperations() {
        return operations;
    }
}

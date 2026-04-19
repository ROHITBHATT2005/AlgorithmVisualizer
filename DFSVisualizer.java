import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
public class DFSVisualizer extends JFrame{
    private int N = 8;
    private boolean[][] graph;
    private int[] nodeX, nodeY;
    private final int RADIUS = 22;
    private final int MARGIN = 80;
    private List<AnimationStep> steps;
    private int currentStep;
    private Timer timer;
    private GraphPanel graphPanel;
    private JTextArea logArea;
    private JPanel visitedPanel;
    private JLabel[] visitedLabels;
    private static class AnimationStep {
        enum Type{
            VISIT_NODE,VISIT_EDGE,LOG,FINISH
        }
        Type type;
        int node, from, to;
        String message;
        AnimationStep(Type t,int n,int f,int toNode,String msg){
            type = t;
            node = n;
            from = f;
            to = toNode;
            message = msg;
        }
    }
    public DFSVisualizer(){
        setTitle("DFS Graph Connectivity");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        generateConnectedGraph();
        graphPanel = new GraphPanel();
        graphPanel.setBackground(Color.WHITE);
        graphPanel.setPreferredSize(new Dimension(850, 500));
        add(graphPanel, BorderLayout.CENTER);
        visitedPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        visitedPanel.add(new JLabel("Visited:"));
        visitedLabels = new JLabel[N];
        for (int i = 0; i < N; i++) {
            visitedLabels[i] = new JLabel("F", SwingConstants.CENTER);
            visitedLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            visitedLabels[i].setOpaque(true);
            visitedLabels[i].setBackground(Color.WHITE);
            visitedLabels[i].setPreferredSize(new Dimension(45, 35));
            visitedPanel.add(visitedLabels[i]);
        }
        add(visitedPanel, BorderLayout.NORTH);
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
        graphPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                calculateNodePositions();
                graphPanel.repaint();
            }
        });
        buildAnimationSteps();
        SwingUtilities.invokeLater(() -> startAnimation());
    }
    private void generateConnectedGraph() {
        graph = new boolean[N][N];
        Random rand = new Random(123);
        for(int i = 1; i < N; i++){
            int parent = rand.nextInt(i);
            graph[i][parent] = graph[parent][i] = true;
        }
        int extraEdges = 5;
        for(int e = 0; e < extraEdges; e++){
            int u = rand.nextInt(N);
            int v = rand.nextInt(N);
            if(u != v && !graph[u][v]){
                graph[u][v] = graph[v][u] = true;
            }
        }
    }
    private void calculateNodePositions() {
        int w = graphPanel.getWidth(), h = graphPanel.getHeight();
        if(w <= 0 || h <= 0){
            return;
        }
        nodeX = new int[N];
        nodeY = new int[N];
        int cx = w / 2, cy = h / 2;
        int radius = Math.min(w, h)/2 - MARGIN;
        if(radius < 50){
            radius = 50;
        }
        Random rand = new Random(42);
        for(int i = 0; i < N; i++){
            double angle = 2 * Math.PI * i / N;
            nodeX[i] = cx + (int) (radius * Math.cos(angle)) + rand.nextInt(20) - 10;
            nodeY[i] = cy + (int) (radius * Math.sin(angle)) + rand.nextInt(20) - 10;
        }
    }
    private void buildAnimationSteps(){
        steps = new ArrayList<>();
        boolean[] visited = new boolean[N];
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, -1});
        steps.add(new AnimationStep(AnimationStep.Type.LOG, -1, -1, -1, "Starting DFS from node 0"));
        while(!stack.isEmpty()){
            int[] top = stack.pop();
            int node = top[0], parent = top[1];
            if(!visited[node]){
                visited[node] = true;
                steps.add(new AnimationStep(AnimationStep.Type.VISIT_NODE, node, -1, -1, null));
                steps.add(new AnimationStep(AnimationStep.Type.LOG, -1, -1, -1, "Visiting node " + node + (parent != -1 ? " from " + parent : "")));
                if(parent != -1 && graph[parent][node]) {
                    steps.add(new AnimationStep(AnimationStep.Type.VISIT_EDGE, -1, parent, node, null));
                }
                for(int i = N - 1; i >= 0; i--) {
                    if (graph[node][i]) {
                        stack.push(new int[]{i, node});
                    }
                }
            }
        }
        boolean connected = true;
        for(boolean v : visited){
            if(!v){
                connected = false;
            }
        }
        steps.add(new AnimationStep(AnimationStep.Type.LOG, -1, -1, -1, connected ? "Graph is CONNECTED" : "Graph is NOT CONNECTED"));
        steps.add(new AnimationStep(AnimationStep.Type.FINISH, -1, -1, -1, "Done"));
    }
    private void startAnimation(){
        timer = new Timer(800,e ->{
            if(currentStep < steps.size()){
                AnimationStep s = steps.get(currentStep);
                if(s.type == AnimationStep.Type.VISIT_NODE){
                    visitedLabels[s.node].setText("T");
                    visitedLabels[s.node].setBackground(new Color(100, 200, 100));
                    graphPanel.setVisitedNode(s.node);
                }
                else if (s.type == AnimationStep.Type.VISIT_EDGE){
                    graphPanel.setTraversedEdge(s.from, s.to);
                }
                else if (s.type == AnimationStep.Type.LOG && s.message != null){
                    logArea.append(s.message + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
                else if (s.type == AnimationStep.Type.FINISH){
                    timer.stop();
                }
                graphPanel.repaint();
                currentStep++;
            } 
            else{
                timer.stop();
            }
        });
        timer.start();
    }
    private class GraphPanel extends JPanel{
        private int visitedNode = -1, edgeFrom = -1, edgeTo = -1;
        public void setVisitedNode(int n){
            visitedNode = n;
        }
        public void setTraversedEdge(int f, int t){
            edgeFrom = f;
            edgeTo = t;
        }
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if (nodeX == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for(int i = 0; i < N; i++){
                for(int j = i + 1; j < N; j++){
                    if(graph[i][j]){
                        if((edgeFrom == i && edgeTo == j) || (edgeFrom == j && edgeTo == i)) {
                            g2.setColor(Color.RED);
                            g2.setStroke(new BasicStroke(3));
                        }
                        else{
                            g2.setColor(Color.DARK_GRAY);
                            g2.setStroke(new BasicStroke(2));
                        }
                        g2.drawLine(nodeX[i], nodeY[i], nodeX[j], nodeY[j]);
                    }
                }
            }
            for(int i = 0; i < N; i++){
                g2.setColor(visitedLabels[i].getBackground() == new Color(100, 200, 100) ? new Color(100, 200, 100) : new Color(200, 220, 255));
                g2.fillOval(nodeX[i] - RADIUS, nodeY[i] - RADIUS, 2 * RADIUS, 2 * RADIUS);
                g2.setColor(Color.BLACK);
                g2.drawOval(nodeX[i] - RADIUS, nodeY[i] - RADIUS, 2 * RADIUS, 2 * RADIUS);
                g2.drawString(String.valueOf(i), nodeX[i] - 6, nodeY[i] + 6);
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DFSVisualizer().setVisible(true));
    }
}
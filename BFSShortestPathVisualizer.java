import java.awt.*;
import java.util.*;
import javax.swing.*;
public class BFSShortestPathVisualizer extends JFrame{
    private final int N = 5;
    private final int[][] graph ={
        {0,2,9,4,7},
        {2,0,6,3,8},
        {9,6,0,5,1},
        {4,3,5,0,2},
        {7,8,1,2,0}
    };
    private final int start = 0, end = 4;
    private int[] dist;
    private Queue<Integer> queue;
    private int currentNode = -1, currentFrom = -1;
    private JPanel graphPanel;
    private JLabel[] distLabels;
    private JTextArea log;
    public BFSShortestPathVisualizer(){
        setTitle("BFS Shortest Path");
        setSize(800,600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        graphPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                int w = getWidth(),h = getHeight();
                int r = Math.min(w,h)/3;
                int cx = w/2, cy = h/2;
                Point[] pos = new Point[N];
                for (int i = 0;i< N; i++) {
                    double a = 2*Math.PI*i/N;
                    pos[i] = new Point(cx +(int)(r*Math.cos(a)),cy + (int)(r*Math.sin(a)));
                }
                for(int i = 0;i < N;i++){
                    for(int j = i+1;j<N;j++){
                        if(graph[i][j] > 0){
                            g.setColor((currentFrom == i && currentNode == j) || (currentFrom == j && currentNode == i) ? Color.RED : Color.GRAY);
                            g.drawLine(pos[i].x, pos[i].y, pos[j].x, pos[j].y);
                            g.drawString(graph[i][j] + "", (pos[i].x + pos[j].x) / 2, (pos[i].y + pos[j].y) / 2);
                        }
                    }
                }
                for(int i = 0;i < N; i++){
                    g.setColor(i == start ? Color.GREEN : i == end ? Color.RED : (dist[i] < Integer.MAX_VALUE ? Color.CYAN : Color.WHITE));
                    g.fillOval(pos[i].x-20,pos[i].y-20,40,40);
                    g.setColor(Color.BLACK);
                    g.drawString(i+"",pos[i].x-5,pos[i].y + 5);
                }
            }
        };
        add(graphPanel, BorderLayout.CENTER);
        JPanel distPanel = new JPanel();
        distLabels = new JLabel[N];
        for (int i = 0; i < N; i++) {
            distLabels[i] = new JLabel("∞", SwingConstants.CENTER);
            distLabels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            distLabels[i].setOpaque(true);
            distLabels[i].setPreferredSize(new Dimension(50, 30));
            distPanel.add(distLabels[i]);
        }
        add(distPanel, BorderLayout.NORTH);
        log = new JTextArea(8,40);
        log.setEditable(false);
        add(new JScrollPane(log),BorderLayout.SOUTH);
        new Thread(this::runBFS).start();
    }
    private void runBFS(){
        dist = new int[N];
        Arrays.fill(dist,Integer.MAX_VALUE);
        dist[start] = 0;
        queue = new LinkedList<>();
        queue.add(start);
        updateDistDisplay(start, 0);
        log("Start node "+start+" distance 0");
        visitNode(start, -1, 0);
        while(!queue.isEmpty()){
            int u = queue.poll();
            for(int v = 0;v < N;v++){
                if (graph[u][v] > 0 && dist[v] > dist[u] + graph[u][v]) {
                    dist[v] = dist[u] + graph[u][v];
                    updateDistDisplay(v,dist[v]);
                    queue.add(v);
                    log("Relax edge "+ u +"->"+ v +" new distance="+ dist[v]);
                    visitNode(v,u,dist[v]);
                }
            }
        }
        if(dist[end] == Integer.MAX_VALUE){
            log("No path to " + end);
        }
        else{
            log("Shortest path distance = " + dist[end]);
        }
    }
    private void updateDistDisplay(int node, int value){
        SwingUtilities.invokeLater(() -> distLabels[node].setText(value +""));
        sleep(800);
    }
    private void visitNode(int node,int from,int distVal){
        SwingUtilities.invokeLater(() ->{
            currentNode = node;
            currentFrom = from;
            graphPanel.repaint();
        });
        sleep(800);
    }
    private void log(String msg){
        SwingUtilities.invokeLater(() -> log.append(msg + "\n"));
    }
    private void sleep(int ms){
        try{
            Thread.sleep(ms);
        } 
        catch(InterruptedException e){
        }
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new BFSShortestPathVisualizer().setVisible(true));
    }
}
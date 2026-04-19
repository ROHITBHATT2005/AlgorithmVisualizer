import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
public class ZeroOneKnapsackVisualizer extends JFrame{
    private final int[] values = {1, 4, 5, 7};
    private final int[] weights = {1, 3, 4, 5};
    private final int capacity = 7;
    private final int n = values.length;
    private int[][] dp;
    private JLabel[] valuesLabels,weightsLabels;
    private JLabel[][] tableLabels;
    private JTextArea logArea;
    private List<Runnable> actions = new ArrayList<>();
    private Timer timer;
    private int actionIndex = 0;
    public ZeroOneKnapsackVisualizer(){
        setTitle("0/1 Knapsack Visualizer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100,700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        buildGUI();
        buildAnimationSteps();
        SwingUtilities.invokeLater(()->startAnimation());
    }
    private void buildGUI(){
        JPanel valuesPanel = new JPanel(new GridLayout(1, n + 1));
        valuesPanel.add(new JLabel("Values",SwingConstants.CENTER));
        valuesLabels = new JLabel[n];
        for(int i = 0; i < n; i++){
            valuesLabels[i] = new JLabel(String.valueOf(values[i]),SwingConstants.CENTER);
            styleLabel(valuesLabels[i]);
            valuesPanel.add(valuesLabels[i]);
        }
        JPanel weightsPanel = new JPanel(new GridLayout(1,n + 1));
        weightsPanel.add(new JLabel("Weights",SwingConstants.CENTER));
        weightsLabels = new JLabel[n];
        for (int i = 0; i < n; i++){
            weightsLabels[i] = new JLabel(String.valueOf(weights[i]), SwingConstants.CENTER);
            styleLabel(weightsLabels[i]);
            weightsPanel.add(weightsLabels[i]);
        }
        dp = new int[n + 1][capacity + 1];
        JPanel tablePanel = new JPanel(new GridLayout(n + 1, capacity + 1));
        tableLabels = new JLabel[n+1][capacity+1];
        for(int i = 0; i <= n; i++){
            for(int j = 0; j <= capacity; j++){
                JLabel label = new JLabel("0",SwingConstants.CENTER);
                styleLabel(label);
                tablePanel.add(label);
                tableLabels[i][j] = label;
            }
        }
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(valuesPanel);
        top.add(weightsPanel);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(tablePanel), BorderLayout.CENTER);
        logArea = new JTextArea(10, 60);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
    }
    private void styleLabel(JLabel label){
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setPreferredSize(new Dimension(60, 30));
    }
    private void buildAnimationSteps(){
        actions.add(() -> log("Initializing DP table"));
        for(int i = 0; i <= n; i++){
            for(int j = 0; j <= capacity; j++){
                if(i == 0 || j == 0){
                    final int fi = i, fj = j;
                    actions.add(() -> patchCell(fi, fj, 0));
                    actions.add(() -> depatchCell(fi, fj));
                }
                else{
                    final int ci = i, cj = j;
                    final int wi = weights[i - 1];
                    final int vi = values[i - 1];
                    final int prevRow = i - 1;
                    final int prevColTake = cj - wi;
                    final int prevColSkip = cj;
                    if(wi <= cj){
                        actions.add(() -> selectWeight(prevRow));
                        actions.add(() -> selectValue(prevRow));
                        actions.add(this::delay);
                        actions.add(() -> selectCell(prevRow, prevColTake));
                        actions.add(() -> selectCell(prevRow, prevColSkip));
                        actions.add(this::delay);
                        final int take = vi + dp[prevRow][prevColTake];
                        final int skip = dp[prevRow][prevColSkip];
                        actions.add(() -> log("Take = " + take + ", Skip = " + skip));
                        if(take > skip){
                            dp[ci][cj] = take;
                            actions.add(() -> log("Take item " + prevRow));
                            actions.add(() -> patchCell(ci, cj, take));
                        }
                        else{
                            dp[ci][cj] = skip;
                            actions.add(() -> log("Skip item " + prevRow));
                            actions.add(() -> patchCell(ci, cj, skip));
                        }
                        actions.add(this::delay);
                        actions.add(() -> depatchCell(ci, cj));
                        actions.add(() -> deselectCell(prevRow, prevColSkip));
                        actions.add(() -> deselectCell(prevRow, prevColTake));
                        actions.add(() -> deselectWeight(prevRow));
                        actions.add(() -> deselectValue(prevRow));
                    }
                    else{
                        dp[ci][cj] = dp[prevRow][cj];
                        actions.add(() -> log("Item too heavy, copy from above"));
                        actions.add(() -> patchCell(ci, cj, dp[ci][cj]));
                        actions.add(this::delay);
                        actions.add(() -> depatchCell(ci, cj));
                    }
                }
            }
        }
        actions.add(() -> log("Best value = " + dp[n][capacity]));
    }
    private void selectValue(int idx){
        setColor(valuesLabels[idx], Color.YELLOW);
    }
    private void deselectValue(int idx){
        setColor(valuesLabels[idx],Color.WHITE);
    }
    private void selectWeight(int idx){
        setColor(weightsLabels[idx],Color.YELLOW);
    }
    private void deselectWeight(int idx){
        setColor(weightsLabels[idx],Color.WHITE);
    }
    private void selectCell(int i, int j){
        setColor(tableLabels[i][j],Color.CYAN);
    }
    private void deselectCell(int i,int j){
        setColor(tableLabels[i][j],Color.WHITE);
    }
    private void patchCell(int i,int j,int value){
        tableLabels[i][j].setText(String.valueOf(value));
        setColor(tableLabels[i][j],Color.GREEN);
    }
    private void depatchCell(int i,int j){
        setColor(tableLabels[i][j],Color.WHITE);
    }
    private void setColor(JLabel label,Color bg){
        label.setBackground(bg);
        label.repaint();
    }
    private void delay(){
    }
    private void log(String msg){
        logArea.append(msg+"\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    private void startAnimation(){
        timer = new Timer(800,e ->{
            if(actionIndex < actions.size()){
                actions.get(actionIndex).run();
                actionIndex++;
            }
            else{
                timer.stop();
            }
        });
        timer.start();
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new ZeroOneKnapsackVisualizer().setVisible(true));
    }
}
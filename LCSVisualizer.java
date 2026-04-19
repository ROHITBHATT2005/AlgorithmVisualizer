import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
public class LCSVisualizer extends JFrame{
    private final String string1 = "AGGTAB";
    private final String string2 = "GXTXAYB";
    private final int m = string1.length();
    private final int n = string2.length();
    private final int[][] dp = new int[m + 1][n + 1];
    private JLabel[] string1Labels,string2Labels;
    private JLabel[][] tableLabels;
    private JTextArea logArea;
    private List<Runnable> actions = new ArrayList<>();
    private Timer timer;
    private int actionIndex = 0;
    public LCSVisualizer(){
        setTitle("LCS Visualizer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000,700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        buildGUI();
        buildAnimationSteps();
        SwingUtilities.invokeLater(()->startAnimation());
    }
    private void buildGUI(){
        JPanel string1Panel = new JPanel(new GridLayout(m+1,1));
        string1Panel.add(new JLabel("String 1",SwingConstants.CENTER));
        string1Labels = new JLabel[m];
        for(int i = 0;i < m;i++){
            string1Labels[i] = new JLabel(String.valueOf(string1.charAt(i)),SwingConstants.CENTER);
            styleLabel(string1Labels[i]);
            string1Panel.add(string1Labels[i]);
        }
        JPanel string2Panel = new JPanel(new GridLayout(1,n+1));
        string2Panel.add(new JLabel("String 2",SwingConstants.CENTER));
        string2Labels = new JLabel[n];
        for(int j = 0;j < n;j++){
            string2Labels[j] = new JLabel(String.valueOf(string2.charAt(j)), SwingConstants.CENTER);
            styleLabel(string2Labels[j]);
            string2Panel.add(string2Labels[j]);
        }
        JPanel tablePanel = new JPanel(new GridLayout(m + 1, n + 1));
        tableLabels = new JLabel[m+1][n+1];
        for(int i = 0;i <= m;i++){
            for(int j = 0;j <= n;j++){
                JLabel label = new JLabel("0", SwingConstants.CENTER);
                styleLabel(label);
                tablePanel.add(label);
                tableLabels[i][j] = label;
            }
        }
        JPanel top = new JPanel(new BorderLayout());
        top.add(string2Panel,BorderLayout.CENTER);
        JPanel center = new JPanel(new BorderLayout());
        center.add(string1Panel,BorderLayout.WEST);
        center.add(tablePanel,BorderLayout.CENTER);
        add(top,BorderLayout.NORTH);
        add(center,BorderLayout.CENTER);
        logArea = new JTextArea(10,50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced",Font.PLAIN,12));
        add(new JScrollPane(logArea),BorderLayout.SOUTH);
    }
    private void styleLabel(JLabel label){
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setPreferredSize(new Dimension(50,30));
    }
    private void buildAnimationSteps(){
        actions.add(()->log("Initializing DP table"));
        for(int i = 0;i <= m; i++){
            for(int j = 0;j <= n; j++){
                if(i == 0 || j == 0){
                    final int fi = i,fj = j;
                    actions.add(()->patchCell(fi,fj,0));
                    actions.add(()->depatchCell(fi,fj));
                } 
                else{
                    final int ci = i, cj = j;
                    final int pi = i - 1, pj = j - 1;
                    final int upRow = i - 1, leftCol = j - 1;
                    if(string1.charAt(pi) == string2.charAt(pj)){
                        actions.add(()->selectString1(pi));
                        actions.add(this::delay);
                        actions.add(()->selectString2(pj));
                        actions.add(this::delay);
                        actions.add(()->selectCell(pi, pj));
                        actions.add(this::delay);
                        dp[ci][cj] = dp[pi][pj] + 1;
                        actions.add(()->patchCell(ci,cj,dp[ci][cj]));
                        actions.add(()->deselectString1(pi));
                        actions.add(()->deselectString2(pj));
                        actions.add(()->deselectCell(pi,pj));
                    }
                    else{
                        final int r = upRow,c = cj;
                        final int r2 = ci,c2 = leftCol;
                        actions.add(()->selectCell(r, c));
                        actions.add(this::delay);
                        actions.add(()->selectCell(r2, c2));
                        actions.add(this::delay);
                        dp[ci][cj] = Math.max(dp[upRow][cj], dp[ci][leftCol]);
                        actions.add(()->patchCell(ci, cj, dp[ci][cj]));
                        actions.add(()->deselectCell(r, c));
                        actions.add(()->deselectCell(r2, c2));
                    }
                    actions.add(()->depatchCell(ci,cj));
                }
            }
        }
        actions.add(()->log("Backtracking to find LCS"));
        int i = m, j = n;
        StringBuilder lcs = new StringBuilder();
        while(i >= 1 && j >= 1){
            final int fi = i, fj = j;
            actions.add(() -> selectCell(fi, fj));
            actions.add(this::delay);
            if (string1.charAt(i - 1) == string2.charAt(j - 1)) {
                final int ci = i - 1, cj = j - 1;
                actions.add(() -> selectString1(ci));
                actions.add(this::delay);
                actions.add(() -> selectString2(cj));
                actions.add(this::delay);
                lcs.insert(0, string1.charAt(ci));
                final String currentLCS = lcs.toString();
                actions.add(() ->log("Take '" + string1.charAt(ci) + "' → LCS = "+ currentLCS));
                i--;
                j--;
                actions.add(() -> deselectString1(ci));
                actions.add(() -> deselectString2(cj));
            }
            else if(dp[i - 1][j] > dp[i][j - 1]) {
                actions.add(() -> log("Move up"));
                i--;
            }
            else{
                actions.add(() -> log("Move left"));
                j--;
            }
            actions.add(() -> deselectCell(fi, fj));
        }
        actions.add(() -> log("LCS length = " + dp[m][n] + ", LCS = " + lcs.toString()));
    }
    private void selectString1(int idx) {
        setColor(string1Labels[idx],Color.YELLOW);
    }
    private void deselectString1(int idx) {
        setColor(string1Labels[idx],Color.WHITE);
    }
    private void selectString2(int idx) {
        setColor(string2Labels[idx],Color.YELLOW);
    }
    private void deselectString2(int idx) {
        setColor(string2Labels[idx],Color.WHITE);
    }
    private void selectCell(int i,int j){
        setColor(tableLabels[i][j],Color.CYAN);
    }
    private void deselectCell(int i,int j){
        setColor(tableLabels[i][j],Color.WHITE);
    }
    private void patchCell(int i,int j,int value){
        tableLabels[i][j].setText(String.valueOf(value));
        setColor(tableLabels[i][j],Color.GREEN);
    }
    private void depatchCell(int i,int j) {
        setColor(tableLabels[i][j],Color.WHITE);
    }
    private void setColor(JLabel label,Color bg){
        label.setBackground(bg);
        label.repaint();
    }
    private void delay(){
    }
    private void log(String msg){
        logArea.append(msg +"\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    private void startAnimation(){
        timer = new Timer(800,e->{
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
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LCSVisualizer().setVisible(true));
    }
}
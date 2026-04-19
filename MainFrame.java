import java.awt.*;
import javax.swing.*;
public class MainFrame extends JFrame{
    public MainFrame(){
        setTitle("Algorithm Visualizer - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450,400);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JButton lcsBtn = new JButton("LCS (Longest Common Subsequence)");
        lcsBtn.addActionListener(e ->new LCSVisualizer().setVisible(true));
        add(lcsBtn,gbc);
        gbc.gridy++;
        JButton knapsackBtn = new JButton("0/1 Knapsack");
        knapsackBtn.addActionListener(e ->new ZeroOneKnapsackVisualizer().setVisible(true));
        add(knapsackBtn,gbc);
        gbc.gridy++;
        JButton bfsBtn = new JButton("BFS Shortest Path");
        bfsBtn.addActionListener(e->new BFSShortestPathVisualizer().setVisible(true));
        add(bfsBtn,gbc);
        gbc.gridy++;
        JButton jsdBtn = new JButton("Job Sequencing with Deadline");
        jsdBtn.addActionListener(e->new JobSequencingVisualizer().setVisible(true));
        add(jsdBtn,gbc);
        gbc.gridy++;
        JButton dfsBtn = new JButton("DFS Visualizer");
        dfsBtn.addActionListener(e->new DFSVisualizer().setVisible(true));
        add(dfsBtn,gbc);
        gbc.gridy++;
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new MainFrame().setVisible(true));
    }
}
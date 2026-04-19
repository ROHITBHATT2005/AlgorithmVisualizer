import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class JobSequencingVisualizer extends JFrame {
    private String[] jobId = {"a", "b", "c", "d", "e"};
    private int[] deadline = {2, 1, 2, 1, 3};
    private int[] profit = {100, 19, 27, 25, 15};
    private final int N = deadline.length;

    private JLabel[] scheduleLabels, jobIdLabels, deadlineLabels, profitLabels;
    private JTextArea logArea;
    private List<Runnable> actions = new ArrayList<>();
    private Timer timer;
    private int stepIndex = 0;
    private int[] slot;
    private String[] result;

    public JobSequencingVisualizer() {
        setTitle("Job Sequencing with Deadlines");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        buildGUI();
        buildAnimationSteps();
        startAnimation();
    }

    private void buildGUI() {
        JPanel tracersPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        JPanel scheduleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        scheduleRow.add(new JLabel("Schedule:"));
        scheduleLabels = new JLabel[N];
        for (int i = 0; i < N; i++) {
            scheduleLabels[i] = new JLabel("-", SwingConstants.CENTER);
            styleLabel(scheduleLabels[i]);
            scheduleRow.add(scheduleLabels[i]);
        }
        tracersPanel.add(scheduleRow);

        JPanel jobRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        jobRow.add(new JLabel("Job Ids:"));
        jobIdLabels = new JLabel[N];
        for (int i = 0; i < N; i++) {
            jobIdLabels[i] = new JLabel(jobId[i], SwingConstants.CENTER);
            styleLabel(jobIdLabels[i]);
            jobRow.add(jobIdLabels[i]);
        }
        tracersPanel.add(jobRow);

        JPanel deadlineRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        deadlineRow.add(new JLabel("Deadlines:"));
        deadlineLabels = new JLabel[N];
        for (int i = 0; i < N; i++) {
            deadlineLabels[i] = new JLabel(String.valueOf(deadline[i]), SwingConstants.CENTER);
            styleLabel(deadlineLabels[i]);
            deadlineRow.add(deadlineLabels[i]);
        }
        tracersPanel.add(deadlineRow);

        JPanel profitRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        profitRow.add(new JLabel("Profit:"));
        profitLabels = new JLabel[N];
        for (int i = 0; i < N; i++) {
            profitLabels[i] = new JLabel(String.valueOf(profit[i]), SwingConstants.CENTER);
            styleLabel(profitLabels[i]);
            profitRow.add(profitLabels[i]);
        }
        tracersPanel.add(profitRow);

        add(tracersPanel, BorderLayout.CENTER);

        logArea = new JTextArea(8, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
    }

    private void styleLabel(JLabel label) {
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setPreferredSize(new Dimension(60, 30));
    }

    private void buildAnimationSteps() {
        actions.add(() -> log("Sorting jobs by profit..."));
        for (int i = 0; i < N - 1; i++) {
            for (int j = 0; j < N - i - 1; j++) {
                if (profit[j] < profit[j + 1]) {
                    int tempProfit = profit[j];
                    profit[j] = profit[j + 1];
                    profit[j + 1] = tempProfit;
                    int tempDeadline = deadline[j];
                    deadline[j] = deadline[j + 1];
                    deadline[j + 1] = tempDeadline;
                    String tempJob = jobId[j];
                    jobId[j] = jobId[j + 1];
                    jobId[j + 1] = tempJob;

                    final int p1 = j, p2 = j + 1;
                    actions.add(() -> updateJobRow(p1, p2));
                    actions.add(() -> updateDeadlineRow(p1, p2));
                    actions.add(() -> updateProfitRow(p1, p2));
                    actions.add(this::delay);
                }
            }
        }
        actions.add(() -> log("Sorting finished."));

        slot = new int[N];
        result = new String[N];
        for (int i = 0; i < N; i++) {
            slot[i] = 0;
            result[i] = "-";
            final int idx = i;
            actions.add(() -> scheduleLabels[idx].setText("-"));
        }
        actions.add(() -> log("All slots free."));

        for (int i = 0; i < N; i++) {
            final int current = i;
            actions.add(() -> selectJobId(current));
            actions.add(() -> selectDeadline(current));
            actions.add(this::delay);
            actions.add(() -> log("Scheduling job " + jobId[current] + " (profit " + profit[current] + ", deadline " + deadline[current] + ")"));

            int limit = Math.min(N, deadline[current]) - 1;
            boolean scheduled = false;
            for (int j = limit; j >= 0; j--) {
                final int slotPos = j;
                if (slot[slotPos] == 0) {
                    actions.add(() -> log("Found free slot " + slotPos));
                    actions.add(() -> patchSchedule(slotPos, jobId[current]));
                    actions.add(this::delay);
                    result[slotPos] = jobId[current];
                    slot[slotPos] = 1;
                    actions.add(() -> depatchSchedule(slotPos));
                    actions.add(() -> log("Scheduled in slot " + slotPos));
                    scheduled = true;
                    break;
                } else {
                    actions.add(() -> log("Slot " + slotPos + " occupied, trying previous"));
                }
            }
            if (!scheduled) actions.add(() -> log("No free slot, job discarded"));
            actions.add(() -> deselectJobId(current));
            actions.add(() -> deselectDeadline(current));
            actions.add(this::delay);
        }
        actions.add(() -> {
            StringBuilder sb = new StringBuilder("Final schedule: ");
            for (String s : result) sb.append(s).append(" ");
            log(sb.toString());
        });
    }

    private void updateJobRow(int p1, int p2) {
        jobIdLabels[p1].setText(jobId[p1]);
        jobIdLabels[p2].setText(jobId[p2]);
    }
    private void updateDeadlineRow(int p1, int p2) {
        deadlineLabels[p1].setText(String.valueOf(deadline[p1]));
        deadlineLabels[p2].setText(String.valueOf(deadline[p2]));
    }
    private void updateProfitRow(int p1, int p2) {
        profitLabels[p1].setText(String.valueOf(profit[p1]));
        profitLabels[p2].setText(String.valueOf(profit[p2]));
    }
    private void selectJobId(int idx) { setBackground(jobIdLabels[idx], Color.YELLOW); }
    private void deselectJobId(int idx) { setBackground(jobIdLabels[idx], Color.WHITE); }
    private void selectDeadline(int idx) { setBackground(deadlineLabels[idx], Color.YELLOW); }
    private void deselectDeadline(int idx) { setBackground(deadlineLabels[idx], Color.WHITE); }
    private void patchSchedule(int idx, String job) {
        scheduleLabels[idx].setText(job);
        setBackground(scheduleLabels[idx], Color.GREEN);
    }
    private void depatchSchedule(int idx) { setBackground(scheduleLabels[idx], Color.WHITE); }
    private void setBackground(JLabel label, Color bg) { label.setBackground(bg); label.repaint(); }
    private void delay() { }
    private void log(String msg) { logArea.append(msg + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }

    private void startAnimation() {
        timer = new Timer(400, e -> {
            if (stepIndex < actions.size()) {
                actions.get(stepIndex).run();
                stepIndex++;
            } else timer.stop();
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JobSequencingVisualizer().setVisible(true));
    }
}
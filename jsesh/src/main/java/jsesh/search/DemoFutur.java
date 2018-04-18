package jsesh.search;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * A demo for SwingWorker and cancellation.
 */
public class DemoFutur {

    JFrame frame = new JFrame("demo");
    JButton startButton = new JButton("start");
    JButton stopButton = new JButton("stop");
    JTextArea textArea = new JTextArea(10, 20);
    MyWorker currentWorker = null;

    public DemoFutur() {
        frame.setLayout(new FlowLayout());
        frame.add(startButton);
        frame.add(stopButton);
        frame.add(textArea);

        startButton.addActionListener(this::startMe);
        stopButton.addActionListener(this::stopMe);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void stopMe(ActionEvent actionEvent) {
        if (currentWorker != null) {
            currentWorker.cancel(true);
        }
    }

    private void startMe(ActionEvent actionEvent) {
        if (currentWorker != null) {
            currentWorker.cancel(true);
            try {
                int val= currentWorker.get();
            } catch (CancellationException|InterruptedException|ExecutionException e) {
                e.printStackTrace();
            }
        }
        currentWorker = new MyWorker();
        currentWorker.execute();
    }

    private class MyWorker extends SwingWorker<Integer, Integer> {
        private int val = 0;

        @Override
        protected Integer doInBackground() {
            int cpt = 0;
            try {
                while (true) {
                    Thread.sleep(1);
                    for (int i= 0; i < 10000000; i++) {
                        double y= Math.sin(2);
                    }
                    cpt++;
                    this.publish(cpt);
                }
            } catch (InterruptedException e) {
                return cpt;
            }
        }

        @Override
        protected void process(List<Integer> chunks) {
            if (!chunks.isEmpty())
                textArea.setText(chunks.get(chunks.size() - 1).toString());
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DemoFutur());
    }
}

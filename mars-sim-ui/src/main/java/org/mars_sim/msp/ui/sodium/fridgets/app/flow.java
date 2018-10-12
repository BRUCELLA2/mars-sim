package org.mars_sim.msp.ui.sodium.fridgets.app;

import javax.swing.*;

import org.mars_sim.msp.core.sodium.*;
import org.mars_sim.msp.ui.sodium.fridgets.*;

import java.util.ArrayList;

public class flow {
    public static void main(String[] args) {
        JFrame frame = new JFrame("flow");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(Transaction.run(() -> {
            FrButton ok = new FrButton(new Cell<>("OK"));
            FrButton cancel = new FrButton(new Cell<>("Cancel"));
            ArrayList<Fridget> fridgets = new ArrayList<>();
            fridgets.add(ok);
            fridgets.add(cancel);
            Fridget dialog = new FrFlow(FrFlow.Direction.HORIZONTAL,
                fridgets);
            Listener l =
                ok.sClicked.listen(
                    u -> System.out.println("OK"))
                  .append(
                      cancel.sClicked.listen(
                          u -> System.out.println("Cancel")
                      )
                  );
            return new FrView(frame, dialog) {
                public void removeNotify() {
                    super.removeNotify();
                    l.unlisten();
                }
            };
        }));
        frame.setSize(360,120);
        frame.setVisible(true);
    }
}


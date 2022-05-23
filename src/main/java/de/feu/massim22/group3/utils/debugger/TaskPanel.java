package de.feu.massim22.group3.utils.debugger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.util.List;
import java.awt.geom.Rectangle2D;

class TaskPanel extends JPanel {

    TaskPanel(TaskInfo task) {
        MigLayout layout = new MigLayout("insets 10", "[left] [right] [right]");
        setLayout(layout);
        setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Titel:");
        add(titleLabel);
        JLabel title = new JLabel(task.name);
        add(title, "wrap");

        JLabel deadlineLabel = new JLabel("Frist:");
        add(deadlineLabel);
        JLabel deadline = new JLabel(String.valueOf(task.deadline));
        add(deadline, "wrap");

        JLabel reward = new JLabel(String.valueOf(task.reward + " $"));
        reward.setFont(reward.getFont().deriveFont(20f));
        add(reward, "cell 2 0 2 2, wrap");

        TaskMapPanel mapPanel = new TaskMapPanel(task.requirements);
        add(mapPanel, "cell 0 2, span, wrap");

        mapPanel.setMinimumSize(new Dimension(150, 150));
    }

    private class TaskMapPanel extends JPanel {
        
        private List<Thing> things;
        private int extensionSize = 2;

        TaskMapPanel(List<Thing> things) {
            this.things = things;
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            Dimension d = getSize();
            double cellWidth = d.width / (2 * extensionSize + 1);
            g.setFont(g.getFont().deriveFont(16f));
            // Draw Grid
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 5; x++) {
                    CellType t = CellType.FREE;
                    Rectangle2D.Double rect = new Rectangle2D.Double(x * cellWidth, y * cellWidth, cellWidth, cellWidth);
                    CellUtils.draw(g2d, t, rect, x, y, "", false);
                }
            }
            // Draw Data
            for (Thing t : things) {
                CellType type;

                switch (t.type) {
                    case "b0": type = CellType.BLOCK_0; break;
                    case "b1": type = CellType.BLOCK_1; break;
                    case "b2": type = CellType.BLOCK_2; break;
                    case "b3": type = CellType.BLOCK_3; break;
                    case "b4": type = CellType.BLOCK_4; break;
                    default: type = CellType.FREE;
                }
                Rectangle2D.Double rect = new Rectangle2D.Double((t.x + extensionSize) * cellWidth, (t.y + extensionSize) * cellWidth, cellWidth, cellWidth);
                CellUtils.draw(g2d, type, rect, t.x, t.y, "", false);
            }
            // Draw Agent
            Rectangle2D.Double rect = new Rectangle2D.Double(extensionSize * cellWidth, extensionSize * cellWidth, cellWidth, cellWidth);
            CellUtils.draw(g2d, CellType.TEAMMATE, rect, extensionSize, extensionSize, "", false);
        }
    }
}

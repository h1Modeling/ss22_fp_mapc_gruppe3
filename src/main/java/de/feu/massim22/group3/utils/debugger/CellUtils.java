package de.feu.massim22.group3.utils.debugger;

import java.awt.*;

import javax.swing.plaf.ColorUIResource;

import de.feu.massim22.group3.agents.Desires.BDesires.GroupDesireTypes;
import de.feu.massim22.group3.map.CellType;
import java.awt.geom.Rectangle2D;

class CellUtils {
    private static Color black = new ColorUIResource(36, 36, 36);
    private static Color dispenser0 = new ColorUIResource(113, 117, 72);
    private static Color dispenser1 = new ColorUIResource(186, 178, 23);
    private static Color dispenser2 = new ColorUIResource(191, 184, 41);
    private static Color dispenser3 = new ColorUIResource(191, 184, 41);
    private static Color dispenser4 = new ColorUIResource(191, 184, 41);

    static void draw(Graphics2D g2d, CellType t, Rectangle2D.Double rect, int x, int y, String name, boolean selected) {
        draw(g2d, t, rect, x, y, name, selected, GroupDesireTypes.NONE);
    }

    static void draw(Graphics2D g2d, CellType t, Rectangle2D.Double rect, int x, int y, String name, boolean selected, String groupDesire) {
        int greyValue = (x % 2 == 0 && y % 2 == 0) || (x % 2 == 1 && y % 2 == 1) ? 238 : 221;
        int unknownValue = (x % 2 == 0 && y % 2 == 0) || (x % 2 == 1 && y % 2 == 1) ? 88 : 71;
        switch (t) {
        case TEAMMATE:
            drawAgent(g2d, rect, new Color(28, 113, 216), greyValue, name, selected, groupDesire);
            break;
        case OBSTACLE:
            g2d.setColor(black);
            g2d.fill(rect);
            break;
        case UNKNOWN:
            g2d.setColor(new Color(unknownValue, unknownValue, unknownValue));
            g2d.fill(rect);
            break;
        case FREE:
            g2d.setColor(new ColorUIResource(greyValue, greyValue, greyValue));
            g2d.fill(rect);
            break;
        case DISPENSER_0:
            drawDispenser(g2d, rect, dispenser0, "d0");
            break;
        case DISPENSER_1:
            drawDispenser(g2d, rect, dispenser1, "d1");
            break;
        case DISPENSER_2:
            drawDispenser(g2d, rect, dispenser2, "d2");
            break;
        case DISPENSER_3:
            drawDispenser(g2d, rect, dispenser3, "d3");
            break;
        case DISPENSER_4:
            drawDispenser(g2d, rect, dispenser4, "d4");
            break;
        case BLOCK_0:
            drawBlock(g2d, rect, dispenser0, "b0");
            break;
        case BLOCK_1:
            drawBlock(g2d, rect, dispenser1, "b1");
            break;
        case BLOCK_2:
            drawBlock(g2d, rect, dispenser2, "b2");
            break;
        case BLOCK_3:
            drawBlock(g2d, rect, dispenser3, "b3");
            break;
        case BLOCK_4:
            drawBlock(g2d, rect, dispenser4, "b4");
            break;
        case ENEMY:
            drawAgent(g2d, rect, new Color(237, 51, 59), greyValue, "E", false, GroupDesireTypes.NONE);
            break;
        default:
            g2d.draw(rect);
        }
    }

    private static void drawDispenser(Graphics2D g2d, Rectangle2D.Double rect, Color c, String name) {
        g2d.setColor(new ColorUIResource(222, 80, 23));
        g2d.fill(rect);
        Rectangle2D.Double innerRect = new Rectangle2D.Double(rect.x + rect.width * 0.15, rect.y + rect.width * 0.15, rect.width * 0.7, rect.height * 0.7);
        g2d.setColor(c);
        g2d.fill(innerRect);
        drawCenteredString(g2d, name, rect, Color.WHITE);
    }

    private static void drawBlock(Graphics2D g2d, Rectangle2D.Double rect, Color c, String name) {
        Color outline = c.darker();
        Rectangle2D.Double r = new Rectangle2D.Double(rect.x + 0.5, rect.y + 0.5, rect.width - 1, rect.height - 1);
        g2d.setColor(c);
        g2d.fill(r);
        g2d.setColor(outline);
        g2d.draw(r);
        drawCenteredString(g2d, name, rect, Color.WHITE);
    }

    static void drawCenteredString(Graphics g, String text, Rectangle2D.Double rect, Color color) {
        if (text == null) return; 
        Font font = g.getFont();
        FontMetrics metrics = g.getFontMetrics(font);

        int x = (int)(rect.x + (rect.width - metrics.stringWidth(text)) / 2);
        int y = (int)(rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent());
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private static void drawAgent(Graphics2D g2d, Rectangle2D.Double rect, Color c, int background, String name, boolean selected, String groupDesire) {
        Color backgroundC = CellUtils.getAgentTaskColor(groupDesire, background);
        Color backgroundColor = selected ? new Color(51, 209, 121) : backgroundC;
        g2d.setColor(backgroundColor);
        g2d.fill(rect);
        g2d.setColor(black);
        Rectangle2D.Double lineRect1 = new Rectangle2D.Double(rect.x, rect.y + rect.height * 0.42, rect.width, rect.height * 0.16);
        g2d.fill(lineRect1);
        Rectangle2D.Double lineRect2 = new Rectangle2D.Double(rect.x + rect.width * 0.42, rect.y, rect.width * 0.16, rect.height);
        g2d.fill(lineRect2);
        Rectangle2D.Double innerRect = new Rectangle2D.Double(rect.x + rect.width * 0.15, rect.y + rect.width * 0.15, rect.width * 0.7, rect.height * 0.7);
        g2d.setColor(c);
        g2d.fill(innerRect);
        g2d.setColor(new Color(255, 255, 255));
        g2d.draw(innerRect);
        drawCenteredString((Graphics)g2d, name, rect, Color.WHITE);
    }

    private static Color getAgentTaskColor(String desire, int background) {
        switch (desire) {
            case GroupDesireTypes.TASK: return new Color(255, 120, 0);
            case GroupDesireTypes.EXPLORE: return new Color(192, 87, 203);
            case GroupDesireTypes.GUARD: return new Color(87, 127, 137);
            case GroupDesireTypes.RECEIVE_ATTACH: return new Color(255, 255, 0);
            case GroupDesireTypes.DELIVER_ATTACH: return new Color(186, 255, 0);
            default: return new Color(background, background, background);
        }
    }
}

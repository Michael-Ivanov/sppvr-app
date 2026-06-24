package com.cardio.sppvr.ui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Интерактивная капча-пазл: пользователь должен кликнуть на фрагменты
 * в правильном порядке (1→2→3→4), чтобы собрать исходное изображение.
 * Фрагменты отображаются в сетке 2×2 в перемешанном порядке.
 */
public class PuzzleCaptcha extends JPanel {

    private static final int FRAGMENT_COUNT = 4;
    private static final int GRID_SIZE = 2;
    private static final int CELL_SIZE = 100;

    private final PuzzleFragment[] fragments = new PuzzleFragment[FRAGMENT_COUNT];
    private final List<Integer> displayOrder = new ArrayList<>();
    private int expectedNextId = 1;
    private boolean solved = false;

    public PuzzleCaptcha() {
        setLayout(new GridLayout(GRID_SIZE, GRID_SIZE, 4, 4));
        setPreferredSize(new Dimension(CELL_SIZE * GRID_SIZE + 8, CELL_SIZE * GRID_SIZE + 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));

        loadFragments();
        shuffle();
    }

    private void loadFragments() {
        for (int i = 1; i <= FRAGMENT_COUNT; i++) {
            String path = "/captcha/" + i + ".png";
            BufferedImage image = loadImage(path);
            if (image != null) {
                BufferedImage scaled = scaleImage(image, CELL_SIZE, CELL_SIZE);
                fragments[i - 1] = new PuzzleFragment(i, scaled);
            }
        }
    }

    private BufferedImage loadImage(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Не удалось загрузить ресурс: " + path);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки изображения " + path + ": " + e.getMessage());
            return null;
        }
    }

    private BufferedImage scaleImage(BufferedImage src, int width, int height) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return scaled;
    }

    public void shuffle() {
        displayOrder.clear();
        for (int i = 0; i < FRAGMENT_COUNT; i++) {
            displayOrder.add(i);
        }
        Collections.shuffle(displayOrder);

        expectedNextId = 1;
        solved = false;
        for (PuzzleFragment f : fragments) {
            if (f != null) f.reset();
        }

        rebuildGrid();
    }

    private void rebuildGrid() {
        removeAll();
        for (int idx : displayOrder) {
            PuzzleFragment fragment = fragments[idx];
            if (fragment == null) continue;
            JLabel cell = new JLabel(new ImageIcon(fragment.getImage()));
            cell.setHorizontalAlignment(SwingConstants.CENTER);
            cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            cell.setOpaque(true);
            cell.setBackground(Color.WHITE);

            cell.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (solved) return;
                    handleClick(fragment, cell);
                }
            });

            add(cell);
        }
        revalidate();
        repaint();
    }

    private void handleClick(PuzzleFragment fragment, JLabel cell) {
        if (fragment.isLocked()) return;

        if (fragment.getId() == expectedNextId) {
            fragment.setLocked(true);
            cell.setBackground(new Color(200, 255, 200));
            cell.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 0), 2));
            expectedNextId++;

            if (expectedNextId > FRAGMENT_COUNT) {
                solved = true;
            }
        } else {
            expectedNextId = 1;
            for (Component c : getComponents()) {
                if (c instanceof JLabel) {
                    c.setBackground(Color.WHITE);
                    ((JLabel) c).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                }
            }
            for (PuzzleFragment f : fragments) {
                if (f != null) f.reset();
            }
            cell.setBackground(new Color(255, 200, 200));
            cell.setBorder(BorderFactory.createLineBorder(new Color(200, 0, 0), 2));
        }
    }

    public boolean isSolved() {
        return solved;
    }

    /**
     * Внутренний класс — фрагмент пазла с уникальным ID (1—4)
     * и признаком фиксации (кликнут в правильном порядке).
     */
    private static class PuzzleFragment {
        private final int id;
        private final BufferedImage image;
        private boolean locked;

        PuzzleFragment(int id, BufferedImage image) {
            this.id = id;
            this.image = image;
            this.locked = false;
        }

        int getId() { return id; }
        BufferedImage getImage() { return image; }
        boolean isLocked() { return locked; }
        void setLocked(boolean locked) { this.locked = locked; }
        void reset() { this.locked = false; }
    }
}

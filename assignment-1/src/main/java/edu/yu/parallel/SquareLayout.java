package edu.yu.parallel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class SquareLayout implements LayoutManager {
    private int gap;
    private int sideMargin;

    public SquareLayout(int gap, int sideMargin) {
        this.gap = gap;
        this.sideMargin = sideMargin;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return parent.getPreferredSize();
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return parent.getMinimumSize();
    }

    @Override
    public void layoutContainer(Container parent) {
        int n = parent.getComponentCount();
        if (n == 0) return;

        Dimension size = parent.getSize();
        int totalGap = (n - 1) * gap + 2 * sideMargin;
        int width = (size.width - totalGap) / n;
        int height = size.height;

        int squareSide = Math.min(width, height);

        int x = sideMargin;
        int y = (size.height - squareSide) / 2;

        for (int i = 0; i < n; i++) {
            Component comp = parent.getComponent(i);
            comp.setBounds(x + (i * (squareSide + gap)), y, squareSide, squareSide);
        }
    }
}

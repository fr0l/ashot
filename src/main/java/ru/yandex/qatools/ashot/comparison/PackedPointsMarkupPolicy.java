package ru.yandex.qatools.ashot.comparison;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * @author Frolov Viacheslav rovner@yandex-team.ru
 *
 */

public class PackedPointsMarkupPolicy extends DiffMarkupPolicy {

    private int[] deposedPoints = null;
    private int[] diffPoints = new int[10000];
    private int count = 0;

    private BufferedImage transparentMarkedImage = null;
    private int minX;
    private int minY;

    @Override
    public BufferedImage getMarkedImage() {
        if (!marked) {
            markDiffPoints(diffImage);
            marked = true;
        }
        return diffImage;
    }

    @Override
    public BufferedImage getTransparentMarkedImage() {
        if (transparentMarkedImage == null) {
            int width = diffImage.getWidth();
            int height = diffImage.getHeight();
            transparentMarkedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            fillTransparentAlpha(width, height, transparentMarkedImage);
            markDiffPoints(transparentMarkedImage);
        }
        return transparentMarkedImage;
    }

    @Override
    public void addDiffPoint(int x, int y) {
        if (diffPoints.length <= count) {
            int[] newDiffPoints = new int[count + 10000];
            System.arraycopy(diffPoints, 0, newDiffPoints, 0, count);
            diffPoints = newDiffPoints;
        }
        diffPoints[count++] = (x << 16) | y;
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackedPointsMarkupPolicy) {
            PackedPointsMarkupPolicy item = (PackedPointsMarkupPolicy) obj;
            if (count != item.count) {
                return false;
            }

            return Arrays.equals(deposedPoints, item.deposedPoints);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getDeposedPoints());
    }

    @Override
    public boolean hasDiff() {
        return count > diffSizeTrigger;
    }

    @Override
    public int getDiffSize() {
        return count;
    }

    protected void markDiffPoints(BufferedImage image) {
        int rgb = diffColor.getRGB();
        for (int i = 0; i < count; i++) {
            int point = diffPoints[i];
            image.setRGB(point >>> 16, (point << 16) >>> 16, rgb);
        }
    }

    private int[] getDeposedPoints() {
        if (deposedPoints == null) {
            deposedPoints = deposeReference();
        }
        return deposedPoints;
    }

    private int[] deposeReference() {
        int[] referenced = new int[count];

        for (int i = 0; i < count; i++) {
            int point = diffPoints[i];
            int refX = ((point >>> 16) - minX);
            int refY = (((point << 16) >>> 16) - minY);
            referenced[i]= refX << 16 | refY;
        }
        return referenced;
    }
}

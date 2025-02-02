/**
 * MIT License
 *
 * Copyright (c) 2023 Matthias Finke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.pottgames.lesscolors;

import com.github.ajalt.colormath.calculate.DifferenceKt;
import com.github.ajalt.colormath.model.LAB;
import com.github.ajalt.colormath.model.Oklab;
import com.github.ajalt.colormath.model.RGB;

/**
 * Represents a color with components and color space information.
 */
public class Color {
    private final float      component1;
    private final float      component2;
    private final float      component3;
    private final float      component4;
    private final ColorSpace colorSpace;


    /**
     * This is a low level constructor, consider using one of the static creation functions like
     * {@link Color#fromRgb(float, float, float, float)} first.<br> Constructs a new color object with the specified
     * components and color space.
     *
     * @param component1 The first color component.
     * @param component2 The second color component.
     * @param component3 The third color component.
     * @param component4 The fourth color component.
     * @param space      The color space in which the components are defined.
     */
    public Color(float component1, float component2, float component3, float component4, ColorSpace space) {
        this.component1 = component1;
        this.component2 = component2;
        this.component3 = component3;
        this.component4 = component4;
        this.colorSpace = space;
    }


    /**
     * Creates a new Color object from RGBA values ranging from 0.0 to 1.0.
     *
     * @param r     The red component.
     * @param g     The green component.
     * @param b     The blue component.
     * @param alpha The alpha component.
     * @return A Color object representing the specified RGBA values.
     */
    public static Color fromRgb(float r, float g, float b, float alpha) {
        return new Color(r, g, b, alpha, ColorSpace.RGB);
    }


    /**
     * Creates a new Color object from RGBA integers, each component has 8 bits of precision.
     *
     * @param r     The red component as an integer (0-255).
     * @param g     The green component as an integer (0-255).
     * @param b     The blue component as an integer (0-255).
     * @param alpha The alpha component as an integer (0-255).
     * @return A Color object representing the specified RGBA values.
     */
    public static Color fromRgbInt(int r, int g, int b, int alpha) {
        float component1 = r / 255f;
        float component2 = g / 255f;
        float component3 = b / 255f;
        float component4 = alpha / 255f;
        return new Color(component1, component2, component3, component4, ColorSpace.RGB);
    }


    /**
     * Creates a new Color object from an ARGB integer representation.
     *
     * @param argb The ARGB integer value.
     * @return A Color object representing the specified ARGB value.
     */
    public static Color fromArgbInt(int argb) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        return fromRgbInt(r, g, b, a);
    }


    /**
     * Creates a new Color object from LAB color space components.
     *
     * @param l     The lightness component (L) in LAB color space.
     * @param a     The green-red component (a) in LAB color space.
     * @param b     The blue-yellow component (b) in LAB color space.
     * @param alpha The alpha (transparency) component.
     * @return A Color object representing the specified LAB color space values.
     */
    public static Color fromLab(float l, float a, float b, float alpha) {
        return new Color(l, a, b, alpha, ColorSpace.LAB);
    }


    /**
     * Converts the color to the RGB color space.
     *
     * @return A new Color object representing the color in the RGB color space.
     */
    public Color toRgb() {
        switch (this.colorSpace) {
            case RGB:
                return this.copy();
            case LAB:
                int[] rgb = ColorConversionUtil.labToRgb(component1, component2, component3, component4);
                return Color.fromRgbInt(rgb[0], rgb[1], rgb[2], rgb[3]);
            default:
                return null;
        }
    }


    /**
     * Converts the color to the LAB color space.
     *
     * @return A new Color object representing the color in the LAB color space.
     */
    public Color toLab() {
        switch (this.colorSpace) {
            case RGB:
                int r = (int) (component1 * 255f);
                int g = (int) (component2 * 255f);
                int b = (int) (component3 * 255f);
                int a = (int) (component4 * 255f);
                float[] lab = ColorConversionUtil.rgbToLab(r, g, b, a);
                return Color.fromLab(lab[0], lab[1], lab[2], lab[3]);
            case LAB:
                return this.copy();
            default:
                return null;
        }
    }


    /**
     * Converts the color to a 32-bit ARGB integer representation.
     *
     * @return An integer value representing the color in ARGB format.
     */
    public int toArgbInt() {
        Color color = this.colorSpace == ColorSpace.RGB ? this : this.toRgb();
        int r = (int) (color.component1 * 255f);
        int g = (int) (color.component2 * 255f);
        int b = (int) (color.component3 * 255f);
        int a = (int) (color.component4 * 255f);
        assert r >= 0 && r <= 255;
        assert g >= 0 && g <= 255;
        assert b >= 0 && b <= 255;
        assert a >= 0 && a <= 255;
        return b | g << 8 | r << 16 | a << 24;
    }


    /**
     * Calculates the LAB color distance between this color and another color.
     *
     * @param other The other color.
     * @return The LAB color distance between the two colors, using the CIE2000 difference metric.
     */
    public float labDistance(Color other) {
        com.github.ajalt.colormath.Color comp1 = this.colorSpace == ColorSpace.LAB ?
                LAB.Companion.invoke(this.component1, this.component2, this.component3, this.component4) :
                RGB.Companion.invoke(this.component1, this.component2, this.component3, this.component4);
        com.github.ajalt.colormath.Color comp2 = other.colorSpace == ColorSpace.LAB ?
                LAB.Companion.invoke(other.component1, other.component2, other.component3, other.component4) :
                RGB.Companion.invoke(other.component1, other.component2, other.component3, other.component4);
        return DifferenceKt.differenceCIE2000(comp1, comp2);
    }


    /**
     * Calculates the Oklab color distance between this color and another color.
     *
     * @param other The other color.
     * @return The Oklab color distance between the two colors, using Euclidean distance in Oklab space.
     */
    public float oklabDistance(Color other) {
        Oklab comp1 = this.colorSpace == ColorSpace.LAB ?
                LAB.Companion.invoke(this.component1, this.component2, this.component3, this.component4).toOklab() :
                RGB.Companion.invoke(this.component1, this.component2, this.component3, this.component4).toOklab();
        Oklab comp2 = other.colorSpace == ColorSpace.LAB ?
                LAB.Companion.invoke(other.component1, other.component2, other.component3, other.component4).toOklab() :
                RGB.Companion.invoke(other.component1, other.component2, other.component3, other.component4).toOklab();
        return DifferenceKt.euclideanDistance(comp1, comp2);
    }


    /**
     * Creates a copy of this color.
     *
     * @return A new Color object that is a copy of this color.
     */
    public Color copy() {
        return new Color(component1, component2, component3, component4, colorSpace);
    }


    /**
     * Checks if this color is equal to another object.
     *
     * @param o The object to compare with.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Color color = (Color) o;
        return Float.compare(component1, color.component1) == 0 && Float.compare(component2, color.component2) == 0 &&
               Float.compare(component3, color.component3) == 0 && Float.compare(component4, color.component4) == 0 &&
               colorSpace == color.colorSpace;
    }


    /**
     * Generates a hash code for this Color object.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        int result = (component1 != 0f ? Float.floatToIntBits(component1) : 0);
        result = 31 * result + (component2 != 0f ? Float.floatToIntBits(component2) : 0);
        result = 31 * result + (component3 != 0f ? Float.floatToIntBits(component3) : 0);
        result = 31 * result + (component4 != 0f ? Float.floatToIntBits(component4) : 0);
        result = 31 * result + (colorSpace != null ? colorSpace.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        switch (this.colorSpace) {
            case LAB:
                return "Color {L=" + component1 + ", A=" + component2 + ", B=" + component3 + ", Alpha=" + component4 +
                       "}";
            case RGB:
                return "Color {Red=" + component1 + ", Green=" + component2 + ", Blue=" + component3 + ", Alpha=" +
                       component4 + "}";
            default:
                return "Color {invalid colorspace}";
        }
    }


    /**
     * Enumeration of supported color spaces.
     */
    public enum ColorSpace {
        RGB, LAB
    }

}

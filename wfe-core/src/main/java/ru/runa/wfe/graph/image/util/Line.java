/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.graph.image.util;

/**
 * Presents line equation in form y(x) = k*x + b.
 * 
 * @author dofs
 */
public class Line {
    private final double k;
    private final double b;
    private final boolean kExists;

    public Line(double k, double b, boolean kExists) {
        this.k = k;
        this.b = b;
        this.kExists = kExists;
    }

    public double getB() {
        return b;
    }

    public double getK() {
        return k;
    }

    public boolean isKExists() {
        return kExists;
    }
}

/*
* Siteswap Generator: Android App for generating juggling siteswaps
* Copyright (C) 2017 Tilman Sinning
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package siteswaplib;

/**
 * Created by tilman on 29.10.17.
 */

public class InterfaceFilter extends PatternFilter {

    public InterfaceFilter(Siteswap pattern, Type type) {
        super(pattern, type);
    }

    @Override
    public String toString() {
        String str;

        if (mType == Type.INCLUDE)
            str = new String("Include Interface: ");
        else
            str = new String("Exclude Interface: ");
        str += mPattern.toString();
        return str;
    }

    @Override
    public boolean isFulfilled(Siteswap siteswap) {

        Siteswap siteswapInterface = siteswap.toInterface(Siteswap.FREE);

        if (mType == Type.INCLUDE)
            return siteswapInterface.isPattern(mPattern);
        return !siteswapInterface.isPattern(mPattern);
    }


    @Override
    public boolean isPartlyFulfilled(Siteswap siteswap, int index) {

        if (mType == Type.EXCLUDE) {
            return isFulfilled(siteswap);
        }

        // Intefaces that need to be included are not checked for partly generated siteswaps
        // at the moment
        return true;
    }
}

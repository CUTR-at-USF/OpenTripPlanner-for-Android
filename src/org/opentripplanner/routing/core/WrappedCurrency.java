/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.core;

import java.util.Currency;
import java.util.Locale;

public class WrappedCurrency {
    private Currency value;
    
    public WrappedCurrency() {
        value = null;
    }

    public WrappedCurrency(Currency value) {
        this.value = value;
    }
    
    public WrappedCurrency(String name) {
        value = Currency.getInstance(name);
    }

    public int getDefaultFractionDigits() {
        return value.getDefaultFractionDigits();
    }
    
    public String getCurrencyCode() {
        return value.getCurrencyCode();
    }
    
    public String getSymbol() {
        return value.getSymbol();
    }
    
    public String getSymbol(Locale l) {
        return value.getSymbol(l);
    }

    public String toString() {
        return value.toString();
    }
    
    public boolean equals(Object o) {
        if (o instanceof Currency) {
            return value.equals(o);
        }
        WrappedCurrency c = (WrappedCurrency) o;
        return value.equals(c.value);
    }
    
    public Currency getCurrency() {
        return value;
    }
}

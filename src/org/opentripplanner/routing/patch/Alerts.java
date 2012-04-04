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

package org.opentripplanner.routing.patch;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Alerts implements Serializable {
    private static final long serialVersionUID = 8305126586053909836L;

    public static final String defaultLanguage = "en";

    @Element(required=false)
    public TranslatedString alertHeaderText;

    @Element(required=false)
    public TranslatedString alertDescriptionText;

    @Element(required=false)
    public TranslatedString alertUrl;

    //null means unknown
    @Element(required=false)
    public String effectiveStartDate;

    public static HashSet<Alerts> newSimpleAlertSet(String text) {
        Alerts note = createSimpleAlerts(text);
        HashSet<Alerts> notes = new HashSet<Alerts>(1);
        notes.add(note);
        return notes;
    }

    public static Alerts createSimpleAlerts(String text) {
        Alerts note = new Alerts();
        note.alertHeaderText = new TranslatedString();
        note.alertHeaderText.addTranslation(defaultLanguage, text);
        return note;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Alerts)) {
            return false;
        }
        Alerts ao = (Alerts) o;
        if (alertDescriptionText == null) {
            if (ao.alertDescriptionText != null) {
                return false;
            }
        } else {
            if (!alertDescriptionText.equals(ao.alertDescriptionText)) {
                return false;
            }
        }
        if (alertHeaderText == null) {
            if (ao.alertHeaderText != null) {
                return false;
            }
        } else {
            if (!alertHeaderText.equals(ao.alertHeaderText)) {
                return false;
            }
        }
        if (alertUrl == null) {
            return ao.alertUrl == null;
        } else {
            return alertUrl.equals(ao.alertUrl);
        }
    }

    public int hashCode() {
        return (alertDescriptionText == null ? 0 : alertDescriptionText.hashCode())
                + (alertHeaderText == null ? 0 : alertHeaderText.hashCode())
                + (alertUrl == null ? 0 : alertUrl.hashCode());
    }
}

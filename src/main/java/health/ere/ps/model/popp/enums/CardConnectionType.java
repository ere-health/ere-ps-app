/*
 * Copyright (Date see Readme), gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package health.ere.ps.model.popp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CardConnectionType {
    CONTACT_STANDARD("contact-standard", true),
    CONTACTLESS_STANDARD("contactless-standard", true),
    CONTACT_CONNECTOR("contact-connector", false),
    CONTACTLESS_CONNECTOR("contactless-connector", false),
    CONTACT_CONNECTOR_VIA_STANDARD_TERMINAL("contact-connector-via-standard-terminal", true),
    CONTACT_VIRTUAL("contact-virtual", false),
    CONTACTLESS_VIRTUAL("contactless-virtual", false),
    G3("g3", false),
    UNKNOWN("unknown", false);

    private final String type;
    private final boolean needsCardReader;

    CardConnectionType(String type, boolean needsCardReader) {
        this.type = type;
        this.needsCardReader = needsCardReader;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    public boolean requiresCardReader() {
        return needsCardReader;
    }

    @JsonCreator
    public static CardConnectionType fromType(String type) {
        for (CardConnectionType t : values()) {
            if (t.type.equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown card connection type: " + type);
    }
}

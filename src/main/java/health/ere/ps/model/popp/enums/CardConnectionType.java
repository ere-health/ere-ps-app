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
  CONTACT_STANDARD("contact-standard"),
  CONTACTLESS_STANDARD("contactless-standard"),
  CONTACT_CONNECTOR("contact-connector"),
  CONTACTLESS_CONNECTOR("contactless-connector"),
  UNKNOWN("unknown");

  private final String connectionType;

  CardConnectionType(final String connectionType) {
    this.connectionType = connectionType;
  }

  @JsonValue
  public String getType() {
    return connectionType;
  }

  @JsonCreator
  public static CardConnectionType fromType(final String type) {
    for (final CardConnectionType messageType : values()) {
      if (messageType.connectionType.equals(type)) {
        return messageType;
      }
    }
    throw new IllegalArgumentException("Unknown card connection type: " + type);
  }
}

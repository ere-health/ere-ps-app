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

package health.ere.ps.model.popp.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class ScenarioStep implements Serializable {

  @Serial private static final long serialVersionUID = -445491751548134905L;

  /** ISO/IEC 7816-4 command APDU as hexadecimal string. */
  @JsonProperty("commandApdu")
  @NonNull
  private String commandApdu;

  /** List of expected status words in the corresponding response APDU as hexadecimal strings. */
  @JsonProperty("expectedStatusWords")
  @NonNull
  private List<String> expectedStatusWords;

  public ScenarioStep(final String commandApdu, final List<String> expectedStatusWords) {
    this.commandApdu = commandApdu;
    this.expectedStatusWords = expectedStatusWords;
  }
}

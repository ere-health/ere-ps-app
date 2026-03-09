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
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import static health.ere.ps.model.popp.enums.EnumPoPPMessageTypes.SCENARIO_RESPONSE_MESSAGE;

/**
 * Bunch of response APDU received from an eHC. Sent by a client to the server after the client
 * received either a StandardScenarioMessage or a ConnectorScenarioMessage.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScenarioResponseMessage extends PoPPMessage implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /**
   * List of ISO/IEC 7816-4 response APDU as hexadecimal strings. The responses from the smartcard.
   * The 1st element corresponds to the 1st command APDU from a Scenario. The n-th element
   * corresponds to the n-th command APDU from a Scenario. The array is empty if and only if a
   * Scenario does not contain any command APDU. Response from the smartcard encoded in hexadecimal
   * characters [0-9a-f]. Contains at least 4 characters (status word).
   */
  @JsonProperty("responses")
  @NonNull
  private List<String> steps;

  public ScenarioResponseMessage(final @NotNull List<String> steps) {
    this.steps = steps;
    this.type = SCENARIO_RESPONSE_MESSAGE;
  }

  public List<String> getSteps() {
    return Collections.unmodifiableList(steps);
  }
}

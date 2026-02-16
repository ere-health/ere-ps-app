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
import health.ere.ps.model.popp.enums.EnumPoPPMessageTypes;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Sent by the server to the client to execute zero, one or more command APDUs by an electronic
 * Health-Card (eHC).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public final class StandardScenarioMessage extends PoPPMessage implements Serializable {

  @Serial private static final long serialVersionUID = 5254368164751960542L;

  /** Version of the scenario message. version = "1.0.0": actual value of "version". */
  @JsonProperty("version")
  @NonNull
  private String version;

  /**
   * Session identifier for the scenario message. The value is taken from
   * "StartMessage.clientSessionId".
   */
  @JsonProperty("clientSessionId")
  @NonNull
  private String clientSessionId;

  /**
   * An integer in range [0, 32767] preventing replay attacks within a sequence of
   * "ConnectorScenarioMessage" i.e., a group of more than one "ConnectorScenarioMessage" where all
   * included "StandardScenarioMessage" share the same "clientSessionId". The first
   * "StandardScenarioMessage" in a sequence has "sequenceCounter=0". In the next
   * "StandardScenarioMessage" the "sequenceCounter" is incremented by one.
   */
  @JsonProperty("sequenceCounter")
  private int sequenceCounter;

  /**
   * An integer in range [0, 32767] indicating the time span in milliseconds for the PoPP-Service
   * between receiving a "ScenarioResultMessage" till the (expected) send time of the next
   * "StandardScenarioMessage" or "ConnectorScenarioMessage". A Connector or client uses this
   * information to detect a timeout. The special value "timeSpan=0" indicates that this is the last
   * "StandardScenarioMessage" in a sequence.
   */
  @JsonProperty("timeSpan")
  private int timeSpan;

  /** List of steps in the scenario. */
  @JsonProperty("steps")
  @NonNull
  private List<ScenarioStep> steps;

  @Builder
  private StandardScenarioMessage(
      final String version,
      final String clientSessionId,
      final int sequenceCounter,
      final int timeSpan,
      final List<ScenarioStep> steps) {
    this.version = version;
    this.clientSessionId = clientSessionId;
    this.sequenceCounter = sequenceCounter;
    this.timeSpan = timeSpan;
    this.steps = steps;
    this.type = EnumPoPPMessageTypes.STANDARD_SCENARIO_MESSAGE;
  }
}

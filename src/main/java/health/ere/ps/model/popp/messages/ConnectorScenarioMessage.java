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

import health.ere.ps.model.popp.enums.EnumPoPPMessageTypes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public final class ConnectorScenarioMessage extends PoPPMessage implements Serializable {

  @Serial private static final long serialVersionUID = -5965692740221251032L;

  /** Version of the scenario message. version = "1.0.0": actual value of "version". */
  @JsonProperty("version")
  @NonNull
  private String version;

  /**
   * JWT according to RFC 7519 with a StandardScenarioMessage as payload. The payload is signed by
   * the PoPP-Service.
   */
  @NonNull
  @JsonProperty("signedScenario")
  private String signedScenario;

  public ConnectorScenarioMessage(final @NotNull String version, final @NotNull String signedScenario) {
    this.version = version;
    this.signedScenario = signedScenario;
    this.type = EnumPoPPMessageTypes.CONNECTOR_SCENARIO_MESSAGE;
  }
}

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import java.io.Serial;
import java.io.Serializable;

import health.ere.ps.model.popp.enums.EnumPoPPMessageTypes;
import lombok.Getter;

@Getter
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = As.EXISTING_PROPERTY,
    property = "type",
    visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = StartMessage.class, name = "Start"),
  @JsonSubTypes.Type(value = StandardScenarioMessage.class, name = "StandardScenario"),
  @JsonSubTypes.Type(value = ScenarioResponseMessage.class, name = "ScenarioResponse"),
  @JsonSubTypes.Type(value = ConnectorScenarioMessage.class, name = "ConnectorScenario"),
  @JsonSubTypes.Type(value = ErrorMessage.class, name = "Error"),
  @JsonSubTypes.Type(value = TokenMessage.class, name = "Token"),
})
public abstract class PoPPMessage implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** The constant type identifier */
  @JsonProperty("type")
  protected EnumPoPPMessageTypes type;
}

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import static health.ere.ps.model.popp.enums.EnumPoPPMessageTypes.ERROR_MESSAGE;

/** Sent by the server to indicate an error. */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorMessage extends PoPPMessage implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** The error code */
  @JsonProperty("errorCode")
  @NonNull
  private String errorCode;

  /** A human-readable error message */
  @JsonProperty("errorDetail")
  private String errorDetail;

  @Builder
  private ErrorMessage(final String errorCode, final String errorDetail) {
    this.errorCode = errorCode;
    this.errorDetail = errorDetail;
    this.type = ERROR_MESSAGE;
  }
}

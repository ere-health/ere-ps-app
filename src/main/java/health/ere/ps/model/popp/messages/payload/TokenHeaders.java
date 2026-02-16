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

package health.ere.ps.model.popp.messages.payload;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Representation of POPP Token Headers as described in the OpenAPI schema. */
@Getter
@ToString
@EqualsAndHashCode
@Builder
public final class TokenHeaders {

  /** The type of the token. */
  private final String typ;

  /** The algorithm used to sign the token. */
  private final String alg;

  /** The key identifier of the key used to sign the token. */
  private final String kid;

  /** The X.509 certificate chain used to sign the token. */
  private final List<String> x5c;

  /** Constructor with validation. */
  public TokenHeaders(
      final String typ, final String alg, final String kid, final List<String> x5c) {
    this.typ = typ;
    this.alg = alg;
    this.kid = kid;
    this.x5c = x5c != null ? List.copyOf(x5c) : Collections.emptyList();
  }
}

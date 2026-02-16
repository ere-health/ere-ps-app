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

import health.ere.ps.model.popp.enums.ProofMethod;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** Schema for the claims of a PoPP token as contained in the JWT. */
@Getter
@ToString
@EqualsAndHashCode
@Builder
public final class TokenClaims {

  /** The version of the PoPP token format. */
  private final String version;

  /** The issuer of the token. */
  private final String iss;

  /** The time the token was issued. */
  private final long iat;

  /** The proof method used. */
  private final ProofMethod proofMethod;

  /** The time the patient proof was performed. */
  private final long patientProofTime;

  /** The patient identifier (KVNR). */
  private final String patientId;

  /** The insurer identifier (IKNR). */
  private final String insurerId;

  /** The actor identifier (Telematik-ID). */
  private final String actorId;

  /** Profession OID of the actor. */
  private final String actorProfessionOid;

  private TokenClaims(
      final String version,
      final String iss,
      final long iat,
      final ProofMethod proofMethod,
      final long patientProofTime,
      final String patientId,
      final String insurerId,
      final String actorId,
      final String actorProfessionOid) {
    this.version = version;
    this.iss = iss;
    this.iat = iat;
    this.proofMethod = proofMethod;
    this.patientProofTime = patientProofTime;
    this.patientId = patientId;
    this.insurerId = insurerId;
    this.actorId = actorId;
    this.actorProfessionOid = actorProfessionOid;
  }
}

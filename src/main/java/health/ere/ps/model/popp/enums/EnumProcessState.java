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

import lombok.Getter;

@Getter
public enum EnumProcessState {
  INITIAL("INITIAL"),
  START_MESSAGE_SENT("START_MESSAGE_SENT"),
  MASTER_FILE_SELECT_MESSAGE_SENT("MASTER_FILE_SELECT_MESSAGE_SENT"),
  READ_VERSION_OF_EF_V2_MESSAGE_SENT("READ_VERSION_OF_EF_V2_MESSAGE_SENT"),
  KEY_EXCHANGE_STARTED("KEY_EXCHANGE_STARTED"),
  KEY_EXCHANGE_PROGRESS_1_OF_3("KEY_EXCHANGE_PROGRESS_1_OF_3"),
  KEY_EXCHANGE_PROGRESS_2_OF_3("KEY_EXCHANGE_PROGRESS_2_OF_3"),
  KEY_EXCHANGE_PROGRESS_3_OF_3("KEY_EXCHANGE_PROGRESS_3_OF_3"),
  KEY_EXCHANGE_FINISHED("KEY_EXCHANGE_FINISHED");

  private final String state;

  EnumProcessState(final String state) {
    this.state = state;
  }
}

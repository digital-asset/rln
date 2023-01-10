/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.message;

import com.rln.client.kafkaClient.message.fields.MessageIdWithStepsAndPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EnrichedPacs008 {

    @NotBlank(message = "GroupId should not be blank")
    private String groupId;

    @NotEmpty(message = "messages should not be empty")
    private MessageIdWithStepsAndPayload[] messages;
}

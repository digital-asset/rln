/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.message;

import com.rln.client.kafkaClient.message.fields.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApproveRejectProposal {
    @NotBlank(message = "GroupId should not be blank")
    private String groupId;

    @NotBlank(message = "MessageId should not be blank")
    private String messageId;

    private Status status;

    @NotBlank(message = "Reason should not be blank")
    private String reason;

    @NotBlank(message = "bankBic should not be blank")
    private String bankBic;
}

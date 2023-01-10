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
public class FinalizeRejectSettlement {
    @NotBlank(message = "GroupId should not be blank")
    private String groupId;
    private Status status;
    private String reason;
}

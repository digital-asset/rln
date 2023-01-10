/**
 * Copyright (c) 2022, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.rln.client.kafkaClient.message.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Step {

    @NotBlank
    private String approver;

    private String sender;

    private String receiver;

    @NotEmpty(message = "Amount should not be blank")
    private double amount;

    @NotEmpty(message = "Label should not be blank")
    private String label;
}

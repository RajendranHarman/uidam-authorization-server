/********************************************************************************
 * Copyright (c) 2023-24 Harman International 
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0  
 *  
 * <p> Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * <p>SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.ecsp.oauth2.server.core.service;

import org.eclipse.ecsp.oauth2.server.core.client.UserManagementClient;
import org.eclipse.ecsp.oauth2.server.core.response.dto.PasswordPolicyResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordPolicyServiceTest {
    private static final int INTEGER_TWENTY = 20;
    private static final int INTEGER_EIGHT = 8;
    private static final int INTEGER_THREE = 3;
    private static final int INTEGER_TWO = 2;
    private PasswordPolicyService passwordPolicyService;
    private PasswordPolicyResponseDto policy;

    @BeforeEach
    void setUp() {
        // UserManagementClient is not needed for direct message tests
        passwordPolicyService = new PasswordPolicyService(null);
        policy = new PasswordPolicyResponseDto();
    }

    @Test
    void testNullPolicyReturnsEmptyList() {
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(null, false);
        assertTrue(messages.isEmpty());
    }

    @Test
    void testAllFieldsZeroReturnsBaseMessage() {
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertEquals(1, messages.size());
        assertEquals("Password must not contain email/username", messages.get(0));
    }

    @Test
    void testMinLengthMessage() {
        policy.setMinLength(INTEGER_EIGHT);
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertTrue(messages.stream().anyMatch(m -> m.contains("8 characters minimum")));
    }

    @Test
    void testMaxLengthMessage() {
        policy.setMaxLength(INTEGER_TWENTY);
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertTrue(messages.stream().anyMatch(m -> m.contains("20 characters maximum")));
    }

    @Test
    void testUppercaseLowercaseDigitsMessages() {
        policy.setMinUppercase(1);
        policy.setMinLowercase(1);
        policy.setMinDigits(1);
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertTrue(messages.stream().anyMatch(m -> m.contains("Uppercase")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("Lowercase")));
        assertTrue(messages.stream().anyMatch(m -> m.contains("Number")));
    }

    @Test
    void testSpecialCharsMessages() {
        policy.setAllowedSpecialChars("!@#");
        policy.setMinSpecialChars(INTEGER_TWO);
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertTrue(messages.stream().anyMatch(m -> m.contains("special character and should not contain space")));
    }

    @Test
    void testConsecutiveLettersMessage() {
        policy.setMinConsecutiveLettersLength(INTEGER_THREE);
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertTrue(messages.stream()
                .anyMatch(m -> m.contains("a sequence of " + INTEGER_THREE + " characters from the username ")));
    }

    @Test
    void testExcludedSpecialCharsMessage() {
        policy.setExcludedSpecialChars("[]{}");
        List<String> messages = passwordPolicyService.getPasswordPolicyMessages(policy, false);
        assertTrue(messages.stream().anyMatch(m -> m.contains("should not contain special characters: []{}")));
    }

    @Test
    void testSetupPasswordPolicyWithDefaults() {
        // Use a mock UserManagementClient to avoid NPE
        UserManagementClient mockClient = Mockito.mock(UserManagementClient.class);
        Mockito.when(mockClient.getPasswordPolicy()).thenReturn(null);
        PasswordPolicyService service = new PasswordPolicyService(mockClient) {
            @Override
            public void setupPasswordPolicy(Model model, boolean ignoreConsecutiveLetters) {
                super.setupPasswordPolicy(model, ignoreConsecutiveLetters);
            }
        };
        Model model = new ConcurrentModel();
        service.setupPasswordPolicy(model, true);
        assertNotNull(model.getAttribute("pwdMin"));
        assertNotNull(model.getAttribute("pwdMax"));
        assertNotNull(model.getAttribute("pwdNote"));
    }
}

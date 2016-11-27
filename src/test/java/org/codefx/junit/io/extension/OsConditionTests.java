/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.codefx.junit.io.extension;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;

class OsConditionTests {

	// CONTAINER -------------------------------------------------------------------

	@Test
	void evaluateContainer_notAnnotated_enabled() throws Exception {
		ContainerExtensionContext context = createContextReturning(UnconditionalTestCase.class);
		OsCondition condition = new OsCondition();

		ConditionEvaluationResult result = condition.evaluate(context);

		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains(OsCondition.NO_CONDITION_PRESENT);
	}

	@Test
	void evaluateContainer_disabledOnOtherOs_enabled() throws Exception {
		ContainerExtensionContext context = createContextReturning(DisabledOnNixTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = conditionOnWindows.evaluate(context);

		assertEnabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateContainer_disabledOnThisOs_disabled() throws Exception {
		ContainerExtensionContext context = createContextReturning(DisabledOnNixTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.NIX);

		ConditionEvaluationResult result = conditionOnWindows.evaluate(context);

		assertDisabledOn(result, OS.NIX);
	}

	@Test
	void evaluateContainer_enabledOnOtherOs_disabled() throws Exception {
		ContainerExtensionContext context = createContextReturning(EnabledOnNixTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = conditionOnWindows.evaluate(context);

		assertDisabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateContainer_enabledOnThisOs_enabled() throws Exception {
		ContainerExtensionContext context = createContextReturning(EnabledOnNixTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.NIX);

		ConditionEvaluationResult result = conditionOnWindows.evaluate(context);

		assertEnabledOn(result, OS.NIX);
	}

	// TESTS -------------------------------------------------------------------

	@Test
	void evaluateMethod_notAnnotated_enabled() throws Exception {
		TestExtensionContext context = createContextReturning(UnconditionalTestCase.class, "unconditionalTest");
		OsCondition condition = new OsCondition();

		ConditionEvaluationResult result = condition.evaluate(context);

		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains(OsCondition.NO_CONDITION_PRESENT);
	}

	@Test
	void evaluateMethod_disabledOnOtherOs_enabled() throws Exception {
		TestExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "disabledOnNixTest");
		OsCondition condition = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = condition.evaluate(context);

		assertEnabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateMethod_disabledOnThisOs_disabled() throws Exception {
		TestExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "disabledOnNixTest");
		OsCondition condition = new OsCondition(() -> OS.NIX);

		ConditionEvaluationResult result = condition.evaluate(context);

		assertDisabledOn(result, OS.NIX);
	}

	@Test
	void evaluateMethod_enabledOnOtherOs_disabled() throws Exception {
		TestExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "enabledOnNixTest");
		OsCondition condition = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = condition.evaluate(context);

		assertDisabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateMethod_enabledOnThisOs_enabled() throws Exception {
		TestExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "enabledOnNixTest");
		OsCondition condition = new OsCondition(() -> OS.NIX);

		ConditionEvaluationResult result = condition.evaluate(context);

		assertEnabledOn(result, OS.NIX);
	}

	// HELPER -------------------------------------------------------------------

	private static ContainerExtensionContext createContextReturning(Class<?> type) throws NoSuchMethodException {
		ContainerExtensionContext context = mock(ContainerExtensionContext.class);
		when(context.getElement()).thenReturn(asElement(type));
		return context;
	}

	private static Optional<AnnotatedElement> asElement(Class<?> type) throws NoSuchMethodException {
		return asElement(type, "");
	}

	private static TestExtensionContext createContextReturning(Class<?> type, String methodName)
			throws NoSuchMethodException {
		TestExtensionContext context = mock(TestExtensionContext.class);
		when(context.getElement()).thenReturn(asElement(type, methodName));
		return context;
	}

	private static Optional<AnnotatedElement> asElement(Class<?> type, String methodName) throws NoSuchMethodException {
		if (methodName.isEmpty())
			return Optional.of(type);
		return Optional.of(type.getDeclaredMethod(methodName, (Class<?>[]) null));
	}

	private static void assertEnabledOn(ConditionEvaluationResult result, OS os) {
		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains(format(OsCondition.TEST_ENABLED, os));
	}

	private static void assertDisabledOn(ConditionEvaluationResult result, OS os) {
		assertThat(result.isDisabled()).isTrue();
		assertThat(result.getReason()).contains(format(OsCondition.TEST_DISABLED, os));
	}

	// TEST CASES -------------------------------------------------------------------

	private static class UnconditionalTestCase {

		void unconditionalTest() {
		}

	}

	@DisabledOnOs(OS.NIX)
	private static class DisabledOnNixTestCase {
	}

	@EnabledOnOs(OS.NIX)
	private static class EnabledOnNixTestCase {
	}

	private static class EnabledAndDisabledTestMethods {

		@DisabledOnOs(OS.NIX)
		void disabledOnNixTest() {
		}

		@EnabledOnOs(OS.NIX)
		void enabledOnNixTest() {
		}

	}

}

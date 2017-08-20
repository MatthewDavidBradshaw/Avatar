package com.matthewtamlin.avatar.rules.avatar_rule.with_running;

import com.google.testing.compile.JavaFileObjects;
import com.matthewtamlin.avatar.rules.AvatarRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.tools.JavaFileObject;

import static org.mockito.Mockito.mock;

public class TestAvatarRuleWithBadCode {
	private static final JavaFileObject BAD_CODE = JavaFileObjects.forSourceString("", "public final abstract Thing {}");
	
	@Test
	public void testEvaluate_compilationFails_allowFailure() throws Throwable {
		final TestRule rule = AvatarRule
				.builder()
				.withSourceFileObjects(BAD_CODE)
				.withSuccessfulCompilationRequired(false)
				.build();
		
		evaluate(rule);
	}
	
	@Test(expected = RuntimeException.class)
	public void testEvaluate_compilationFails_disallowFailure() throws Throwable {
		final TestRule rule = AvatarRule
				.builder()
				.withSourceFileObjects(BAD_CODE)
				.withSuccessfulCompilationRequired(true)
				.build();
		
		evaluate(rule);
	}
	
	private void evaluate(final TestRule rule) throws Throwable {
		rule.apply(mock(Statement.class), mock(Description.class)).evaluate();
	}
}
package io.roach.bank;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdhocTest {
    @Test
    public void testPatterns() {
        Pattern pattern = Pattern.compile("(aws|gcp|azure|az)-");
        Matcher matcher = pattern.matcher("aws-us-west-1");
        Assertions.assertTrue(matcher.find());

        StringBuilder sb = new StringBuilder();
        matcher.appendReplacement(sb, "");
        matcher.appendTail(sb);

        System.out.println(sb);
        Assertions.assertEquals("us-west-1", sb.toString());
    }

    @Test
    public void testPatterns2() {
        Pattern pattern = Pattern.compile("(aws|gcp|azure|az)-");
        Matcher matcher = pattern.matcher("az-us-west-1");
        Assertions.assertTrue(matcher.find());

        StringBuilder sb = new StringBuilder();
        matcher.appendReplacement(sb, "");
        matcher.appendTail(sb);

        System.out.println(sb);
        Assertions.assertEquals("us-west-1", sb.toString());
    }

    @Test
    public void testPatterns3() {
        Pattern pattern = Pattern.compile("(aws|gcp|azure|az)-");
        Matcher matcher = pattern.matcher("us-west-1");
        Assertions.assertFalse(matcher.find());
    }
}

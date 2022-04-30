package io.roach.bank;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class AdHocTest {
    @Test
    public void test() {
        List<String> regions = Arrays.asList("ap_xcvzxc", "es_adsf");
        regions=  regions.stream().filter(s -> s.matches("^[abcd].*")).collect(Collectors.toList());
        System.out.println(regions);

    }
}

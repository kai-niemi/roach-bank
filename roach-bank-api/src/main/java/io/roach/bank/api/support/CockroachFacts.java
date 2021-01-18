package io.roach.bank.api.support;

import java.util.Arrays;
import java.util.List;

public abstract class CockroachFacts {
    public static final List<String> FACTS = Arrays.asList(
            "A cockroach can live for a week without its head. Due to their open circulatory system, and the fact that they breathe through little holes in each of their body segments, "
                    + "they are not dependent on the mouth or head to breathe. The roach only dies because without a mouth, it can't drink water and dies of thirst.",
            "A cockroach can hold its breath for 40 minutes, and can even survive being submerged under water for half an hour. They hold their breath often to help regulate their loss of water.",
            "Cockroaches can run up to three miles in an hour, which means they can spread germs and bacteria throughout a home very quickly.",
            "Newborn German cockroaches become adults in as little as 36 days. In fact, the German cockroach is the most common of the cockroaches and has been implicated in outbreaks of illness and allergic reactions in many people.",
            "A one-day-old baby cockroach, which is about the size of a speck of dust, can run almost as fast as its parents.",
            "The American cockroach has shown a marked attraction to alcoholic beverages, especially beer. They are most likely attracted by the alcohol mixed with hops and sugar.",
            "The world's largest cockroach (which lives in South America) is six inches long with a one-foot wingspan.",
            "Cockroaches are believed to have originated more than 280 million years ago, in the Carboniferous era.",
            "There are more than 4,000 species of cockroaches worldwide, including the most common species, the German cockroach, in addition to other common species, the brownbanded cockroach and American cockroach.",
            "Because they are cold-blooded insects, cockroaches can live without food for one month, but will only survive one week without water.",
            "Cockroaches can eat anything.",
            "Some cockroaches can grow as long as 3 inches.",
            "Roaches can live up to a week without their head.",
            "Cockroaches can survive immense nuclear radiation."
    );

    public static String nextFact() {
        return RandomData.selectRandom(FACTS);
    }

    public static String nextFact(int limit) {
        String fact = RandomData.selectRandom(FACTS);
        return fact.substring(0, Math.min(limit, fact.length()));
    }
}

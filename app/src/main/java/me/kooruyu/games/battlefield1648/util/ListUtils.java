package me.kooruyu.games.battlefield1648.util;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class ListUtils {

    public static <T> List<T> getRandomSubList(List<T> input, int subsetSize) {
        return getRandomSubList(input, subsetSize, new Random());
    }

    public static <T> List<T> getRandomSubList(List<T> input, int subsetSize, Random random) {
        int inputSize = input.size();

        if (subsetSize > inputSize) {
            throw new IllegalArgumentException(
                    "subsetSize has to be at least as long as the list:"
                            + " Size " + inputSize
                            + " subsetSize " + subsetSize
            );
        }

        if (inputSize == subsetSize) {
            return input;
        }

        for (int i = 0; i < subsetSize; i++) {
            int indexToSwap = i + random.nextInt(inputSize - i);
            T temp = input.get(i);
            input.set(i, input.get(indexToSwap));
            input.set(indexToSwap, temp);
        }
        return input.subList(0, subsetSize);
    }

    public static <T> T getRandomElement(Set<T> input, Random random) {
        int randomIndex = random.nextInt(input.size());

        int i = 0;

        for (T element : input) {
            if (randomIndex == i) {
                return element;
            }
            i++;
        }
        throw new RuntimeException("This should never happen!"); //should never happen
    }
}

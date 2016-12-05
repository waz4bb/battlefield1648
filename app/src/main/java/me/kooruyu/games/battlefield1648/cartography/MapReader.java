package me.kooruyu.games.battlefield1648.cartography;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MapReader {

    public static char[][] readMap(InputStream rawStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(rawStream));

        String[] size = reader.readLine().split("x");
        char[][] mapData = new char[Integer.parseInt(size[1])][Integer.parseInt(size[0])];

        String line;
        for (int y = 0; (line = reader.readLine()) != null; y++) {
            mapData[y] = line.toCharArray();
        }

        rawStream.close();

        return mapData;
    }
}

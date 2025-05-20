package org.assignments;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class UserInputReader {

    public String readPlaceName() throws Exception {
        System.out.print("Enter a place name: ");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        return bufferedReader.readLine();
    }
}

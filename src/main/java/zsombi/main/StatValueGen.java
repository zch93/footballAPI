package zsombi.main;

import java.util.ArrayList;

public class StatValueGen<T> {

    ArrayList<T> list = new ArrayList();


    public StatValueGen() {

    }

    public void addElement(T element) {
        list.add(element);
    }

    public String toString() {

        String s = list.toString();
        int start = s.indexOf("[") + 1;
        int end = s.lastIndexOf("]");

        String returnString = "";

        if (start > -1 && end > start) {
            returnString = s.substring(start, end);
        }

        return returnString;
    }

}

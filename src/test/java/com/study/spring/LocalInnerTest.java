package com.study.spring;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocalInnerTest {

    public static void main(String[] args) {

        LocalInnerTest test = new LocalInnerTest();
        List<String> list = new ArrayList<String>(10);
        Object obj = test.func(list);
        for (int i = 0; i < 5; i++)
            System.out.println(obj);

    }

    public Object func(List<String> list) {

        list.add("hello ");

        class LocalInner {

            public String toString() {

                StringBuilder sb = new StringBuilder();
                Iterator it = list.iterator();

                while (it.hasNext())
                    sb.append(it.next());

                list.add("hi ");

                return sb.toString();

            }

        }
        ; // end of class, LocalInnerTest.func().LocalInner

        return new LocalInner();

    }
}

package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.Pizza;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        String test1 = "12345";
        String test2 = "123#5";
        System.out.println(test2.substring(2));


        Pizza pizza1 = new Pizza("a", 2000);
        Pizza pizza2 = new Pizza("a", 2000);
        System.out.println(pizza2.equals(pizza1));

        LocalDate localDate = LocalDate.now();
        DayOfWeek[] days = new DayOfWeek[3];
        days[0] = DayOfWeek.FRIDAY;
        days[1] = DayOfWeek.MONDAY;
        days[2] = DayOfWeek.THURSDAY;

        System.out.println(Arrays.stream(days).toList().contains(localDate.getDayOfWeek()));


    }
}

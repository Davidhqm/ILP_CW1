package uk.ac.ed.inf;

import org.junit.Test;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DataStructureTest {


    public static boolean isDateValid(String date){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setLenient(false);
        try{
            df.parse(date);
            return true;
        }catch (ParseException e){
            return false;
        }
    }

        @Test
    public void test1(){
        PriorityQueue<WeightedLngLat> test = new PriorityQueue<>();
        LngLat sample1 = new LngLat(2.1, 2.1);
        LngLat sample2 = new LngLat(5.1, 9.1);
        LngLat sample3 = new LngLat(9.1, 8.1);
        LngLat sample4 = new LngLat(1093.1, 4.1);
        test.add(new WeightedLngLat(sample1, -0.002));
        test.add(new WeightedLngLat(sample2, 0.001));
        test.add(new WeightedLngLat(sample3, 0.00001));
        test.add(new WeightedLngLat(sample4, 2));
        Iterator<WeightedLngLat> it = test.iterator();
        while(it.hasNext()){
            System.out.println(test.remove().getLngLat());
        }
    }

    @Test
    public void test2(){
        LngLat sample1 = new LngLat(2.1, 2.1);
        LngLat sample2 = new LngLat(2.1, 2.1);

    }

    @Test
    public void test3(){
        ArrayList<Character> test = new ArrayList<>();
        test.add(0,'a');
        test.add(0,'b');
        test.add(0,'c');
        test.add(0,'d');
        test.add(0,'e');

        for ( Character character : test){
            System.out.println(character);
        }
    }

    @Test
    public void testHashmap(){
        HashMap<String, String> test = new HashMap<>();
        test.put("A", null);
        test.put("B", "A");
        test.put("C", "B");
        test.put("D", "C");
        String iterate = "D";
        while(iterate != null){
            System.out.println(iterate);
            iterate = test.get(iterate);
        }
    }

    @Test
    public void testEnum() {
        String date = "2008-02-29";
        System.out.println(isDateValid(date));

    }




}

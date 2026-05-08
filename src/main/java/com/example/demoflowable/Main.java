package com.example.demoflowable;

import java.util.*;

// 注意类名必须为 Main, 不要有任何 package xxx 信息
public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        // 注意 hasNext 和 hasNextLine 的区别
        long x = 3;
        long y = 40;
        long n = 4;
        Set<Long> nums = new HashSet<>((int)n);
        nums.addAll(Arrays.asList(2L, 3L, 4L, 4L));
        List<Long> list = new ArrayList<>(nums);
        if (x > y) {
            System.out.println(0);
            return;
        }
        list.sort(Comparator.reverseOrder());
        for (int i = 0; i < list.size(); i++) {
            x *= list.get(i);
            if(x > y) {
                System.out.println(i + 1);
            }
        }
        System.out.println(-1);

    }
}
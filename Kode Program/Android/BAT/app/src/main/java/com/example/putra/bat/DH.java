package com.example.putra.bat;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

//Diffie Hellman algorithm
public class DH {
    public static BigInteger p;
    public static BigInteger q;
    public static BigInteger r;
    public static BigInteger x;
    public static BigInteger ValX;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void main() {
        // 1. Set Q from R1 * R2
        //R1 value calculate from RSA in Android
        //R2 value calculate from RSA in Arduino
        q = MainActivity.r2.multiply(RSA.r);

        //2. P from n1^n2 mod e1*e2
        String MinTemp = "210";
        BigInteger Min = new BigInteger(MinTemp);
        //n1, e1 value from RSA in Android
        //n2, e2 value from RSA in Arduino
        p = RSA.n.modPow(MainActivity.n2.subtract(Min),MainActivity.e2.multiply(RSA.e));

        //random for set x
        int randomNum = ThreadLocalRandom.current().nextInt(7, 70 + 1);
        //3. Set x value
        x = new BigInteger(String.valueOf(randomNum));
        //4. Encrypt x Value with Diffie Hellman algorithm
        ValX = p.modPow(x,q);
    }
}

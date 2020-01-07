package com.example.putra.bat;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

//RSA algorithm
public class RSA {
    public static BigInteger p;
    public static BigInteger q;
    public static BigInteger n;
    public static BigInteger phi;
    public static BigInteger e;
    public static BigInteger d;
    public static BigInteger r;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void main() {
        // 1. Find large primes p and q
        //p = largePrime(32);
        //q = largePrime(32);

        //use hardcone value for primes p and q
        int Q = RandomPrime();
        String nP = "11";//hardcode
        String nQ = String.valueOf(Q);//hardcode
        p = new BigInteger(nP);//hardcode
        q = new BigInteger(nQ);//hardcode
        if (Q == 13){
            //this hardcode value
            String nE = "37";
            e = new BigInteger(nE);
            String nD = "13";
            d = new BigInteger(nD);
        }else if (Q == 17){
            //this hardcode value
            String nE = "7";
            e = new BigInteger(nE);
            String nD = "23";
            d = new BigInteger(nD);
        }else if (Q == 19){
            //this hardcode value
            String nE = "23";
            e = new BigInteger(nE);
            String nD = "47";
            d = new BigInteger(nD);
        }else if (Q == 23){
            //this hardcode value
            String nE = "13";
            e = new BigInteger(nE);
            String nD = "17";
            d = new BigInteger(nD);
        }else if (Q == 29){
            //this hardcode value
            String nE = "17";
            e = new BigInteger(nE);
            String nD = "33";
            d = new BigInteger(nD);
        }

        // 2. Compute n from p and q
        // n is mod for private & public keys, n bit length is key length
		/*String nN = "551";//hardcode
		BigInteger n = new BigInteger(nN);//hardcode
        */
        n = p.multiply(q);

        Random random = new Random();
        int R = ThreadLocalRandom.current().nextInt(4, e.intValue());
        r = BigInteger.valueOf(R);

        // 3. Compute Phi(n) (Euler's totient function)
        // Phi(n) = (p-1)(q-1)
        // BigIntegers are objects and must use methods for algebraic operations
        phi = getPhi(p, q);

        // 4. Find an int e such that 1 < e < Phi(n) 	and gcd(e,Phi) = 1
        //e = genE(phi);//this generate e value
        //use hardcode value
        //String nE = "37";
        //e = new BigInteger(nE);//this hardcode value

        // 5. Calculate d where  d ≡ e^(-1) (mod Phi(n))
        //d = extEuclid(e, phi)[1];
        //String nD = "13";
        //d = new BigInteger(nD);

        // encryption / decryption example
        //String message = "123";
        //String[] splitMsg = message.split("");
        //BigInteger[] chipMsg = new BigInteger[100];
        //BigInteger[] encMsg = new BigInteger[100];

        // Convert string to numbers using a cipher
        //BigInteger cipherMessage = stringCipher(message);
        // Encrypt the ciphered message
        //BigInteger encrypted = encrypt(cipherMessage, e, n);
        // Decrypt the encrypted message
        //BigInteger decrypted = decrypt(encrypted, d, n);
        // Uncipher the decrypted message to text
        //String restoredMessage = cipherToString(decrypted);

    }


    /**
     * Takes a string and converts each character to an ASCII decimal value
     * Returns BigInteger
     */
    public static int RandomPrime() {
        int[] CHOICES = { 13,17,19,23,29 };
        Random rand = new Random();
        int choice = CHOICES[rand.nextInt(CHOICES.length)];
        return choice;
    }


    /** 3. Compute Phi(n) (Euler's totient function)
     *  Phi(n) = (p-1)(q-1)
     *	BigIntegers are objects and must use methods for algebraic operations
     */
    public static BigInteger getPhi(BigInteger p, BigInteger q) {
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        return phi;
    }

    /**
     * Recursive implementation of Euclidian algorithm to find greatest common denominator
     * Note: Uses BigInteger operations
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        if (b.equals(BigInteger.ZERO)) {
            return a;
        } else {
            return gcd(b, a.mod(b));
        }
    }


    /** Recursive EXTENDED Euclidean algorithm, solves Bezout's identity (ax + by = gcd(a,b))
     * and finds the multiplicative inverse which is the solution to ax ≡ 1 (mod m)
     * returns [d, p, q] where d = gcd(a,b) and ap + bq = d
     * Note: Uses BigInteger operations
     */
    public static BigInteger[] extEuclid(BigInteger a, BigInteger b) {
        //System.out.println("va " + a);
        //System.out.println("vb " + b);
        if (b.equals(BigInteger.ZERO)) return new BigInteger[] {
                a, BigInteger.ONE, BigInteger.ZERO
        }; // { a, 1, 0 }
        BigInteger[] vals = extEuclid(b, a.mod(b));
        //System.out.println("vVals " + vals);
        BigInteger d = vals[0];
        //System.out.println("vd " + d);
        BigInteger p = vals[2];
        //System.out.println("vp " + p);
        BigInteger q = vals[1].subtract(a.divide(b).multiply(vals[2]));
        //System.out.println("vq " + q);

		/*System.out.println("d " + d);
		System.out.println("p " + p);
		System.out.println("q " + q);*/

        return new BigInteger[] {
                d, p, q
        };
    }

    /**
     * generate e by finding a Phi such that they are coprimes (gcd = 1)
     *
     */
    public static BigInteger genE(BigInteger phi) {
        Random rand = new Random();
        BigInteger e = new BigInteger(16, rand);
        System.out.println("1 " + e);
        do {
            e = new BigInteger(16, rand);
            System.out.println("2 " + e);
            while (e.min(phi).equals(phi)) { // while phi is smaller than e, look for a new e
                e = new BigInteger(16, rand);
                System.out.println("3 " + e);
            }
        } while (!gcd(e, phi).equals(BigInteger.ONE)); // if gcd(e,phi) isnt 1 then stay in loop
        System.out.println("4 " + e);
        return e;
    }

    public static BigInteger encrypt(BigInteger message, BigInteger e, BigInteger n) {
        return message.modPow(e, n);
    }

    public static BigInteger decrypt(BigInteger message, BigInteger d, BigInteger n) {
        return message.modPow(d, n);
    }
}

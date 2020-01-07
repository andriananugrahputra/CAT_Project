#include <AES.h>
#include <AES_config.h>
#include <printf.h>

#include <SoftwareSerial.h> // use the software uart
#include <string.h> 
#include "BigNumber.h"

AES aes ;
unsigned int keyLength [3] = {128, 192, 256};

//set pin numbers
SoftwareSerial bluetooth(0, 1); // RX, TX
const int ledPin = 2;         //const won't change
const int buzzPin = 7; 
const int buttonPin = 4;
BigNumber password;
String validation = "null";    //variables for validation status
String temp = "0";

unsigned long ms;
unsigned long second;
long elapsedTime ;

//---public and private data---//
String varE = "E";
String varN = "N";
String varR = "R";
String varY = "Y";
//---======================---//

String strCharAndValSend;

BigNumber one = 1;
BigNumber Q;
BigNumber P;
int tempY = random(7, 70);
int p = 13;
int q = 17;
int n;
int phi;
int e = 11;
int d = 35;
int r = 9;
int t;
int n2;
int e2;
int r2;
int x;
int y;
int k;
int endPublic = 0;

//==============//
byte *key = (unsigned char*)k; // encryption key
byte plain[] = "123321"; 
unsigned long long int myIv = 36753562;
byte iv [N_BLOCK] ;
const int plainPaddedLength = sizeof(plain) + (N_BLOCK - ((sizeof(plain)-1) % 16)); // length of padded plaintext [B]
byte check [plainPaddedLength]; // decrypted plaintext  
unsigned long mcrs = micros ();
String ByteToStr;
//=============//

//variables will change
int buttonState = 0;          //variables for reading the pushbutton status

BigNumber modpow(BigNumber msg, BigNumber n, BigNumber m) {
  //p = base
  //n = exponent
  //m = modulus
  //w = temp
  BigNumber val_base = BigNumber (msg);
  BigNumber val_exponent = BigNumber (n);
  BigNumber val_modulus = BigNumber (m);
  BigNumber i = BigNumber (1);
  BigNumber w = BigNumber (1);

  while (val_exponent != 0) {
    w *= val_base;
    --val_exponent;
  }
  w %= val_modulus;
  return w;
}

void setup() {
  Serial.begin(9600);
  pinMode(ledPin, OUTPUT);    //initialize the LED pin as an output
  pinMode(buzzPin, OUTPUT);    //initialize the Buzzer pin as an output
  pinMode(buttonPin, INPUT);  //initialize the pushbutton pin as an output
  delay(200);
  delay(3000); // wait for settings to take affect. 
  validation = String("null");
  BigNumber::begin ();
  BigNumber::setScale (0);
  n = p*q;
  phi = (p-1)*(q-1);
}
 
void loop() {
  BigNumber equal;
  // some big numbers
  BigNumber a, b, c;
  
  buttonState = digitalRead(buttonPin); //read the state of the pushbutton value
  if(Serial.available()){
    temp = Serial.readString();
    Serial.println(temp);
    //looping for check character
    for(int i = 0; i <temp.length(); i++){
      //if recive value e from android
      if(temp[0] == 'e'){
        Serial.println("======================================================================");
        //read string
        //remove first character
        temp.remove(0, 1);
        e2 = BigNumber (temp.c_str());
        
        strCharAndValSend = varE + e;
        //send e value to android
        Serial.println(strCharAndValSend);
        Serial.println(e2);
        ms = micros();
        second = millis();
      }
      //if recive value n from android
      if(temp[0] == 'n'){
        Serial.println("---FIRST DATA----");
        Serial.println((micros() - ms));
        Serial.println(ms);
        Serial.println((millis() - second));
        Serial.println(second);
        Serial.println("---");
        temp.remove(0, 1);
        n2 = BigNumber (temp.c_str());
        strCharAndValSend = varN + n;
        Serial.println(strCharAndValSend);
        Serial.println(n2);
      }
      //if recive value r from android
      if(temp[0] == 'r'){
        BigNumber tempR2;
        temp.remove(0, 1);
        tempR2 = BigNumber (temp.c_str());
        r2 = modpow(tempR2, d, n);
        t = modpow(r, e2, n2);
        strCharAndValSend = varR + t;
        Serial.println(strCharAndValSend);
        Serial.println(r2);
        endPublic = 1;
        int temp = 0;
        if(endPublic == 1){
          Q = r2*r;
          P = modpow(n2, n-210, e*e2);
          y = modpow(P, tempY, Q);
          temp = 1;
          if(temp == 1){
            strCharAndValSend = varY + y;
            Serial.println(strCharAndValSend);
          }
        }
      }
      if(temp[0] == 'x'){
        BigNumber tempX;
        temp.remove(0, 1);
        tempX = BigNumber (temp.c_str());
        Serial.print("nilaiX");
        Serial.println(tempX);
        k = modpow(tempX, tempY, Q);
        //strCharAndValSend = varR + t;
        //Serial.println(strCharAndValSend);
        Serial.println(k);
        //endPublic = 1;
        Serial.println("---LAST DATA---");
        Serial.println((micros() - ms));
        Serial.println(ms);
        Serial.println((millis() - second));
        Serial.println(second);
        Serial.println("---");
        Serial.println("======================================================================");
      }
      if(temp[0] == 'k'){
        temp.remove(0, 1);
        password = BigNumber (temp.c_str());
        aes.do_aes_decrypt(password,aes.get_size(),check,key,iv);
        ByteToStr = check;
        if(ByteToStr.equalsIgnoreCase("123321")){
          validation = String("true");
        }else{
          validation = String("false");    
        }
      }
    }
  }
  
  if(validation.equalsIgnoreCase("null") && buttonState == LOW){
    while(9999999999){
      digitalWrite(buzzPin, HIGH);
      delay(1000);
      digitalWrite(buzzPin, LOW);
      delay(1000); 
    }
  }
  if(validation.equalsIgnoreCase("true") && buttonState == LOW){
    digitalWrite(ledPin, HIGH);
    delay(1000);
    digitalWrite(ledPin, LOW);
  }if(validation.equalsIgnoreCase("false") && buttonState == LOW){
    while(9999999999){
      digitalWrite(buzzPin, HIGH);
      delay(1000);
      digitalWrite(buzzPin, LOW);
      delay(1000); 
    }
  }
  
}



void aesTest (int bits)
{
  aes.iv_inc();
  
  byte iv [N_BLOCK] ;
  int plainPaddedLength = sizeof(plain) + (N_BLOCK - ((sizeof(plain)-1) % 16)); // length of padded plaintext [B]
  byte check [plainPaddedLength]; // decrypted plaintext
  
  aes.set_IV(myIv);
  aes.get_IV(iv);
 
  unsigned long ms = micros ();
 
  aes.set_IV(myIv);
  aes.get_IV(iv);
  
  ms = micros ();
  aes.do_aes_decrypt(password,aes.get_size(),check,key,bits,iv); 
  
  aes.printArray(check,(bool)true); //print decrypted plain with no padding
  
  Serial.print("- iv:      ");
  aes.printArray(iv,16); //print iv
  printf("\n===================================================================================\n");
}

